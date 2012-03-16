package plugins.commands;


import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jibble.pircbot.Colors;

import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;
import qorebot.plugins.Plugin;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.ThreadedCommand;
import qorebot.plugins.commands.message.CommandMessage;

/**
 * Core command that handles plugin-based commands. This command is capable of
 * (re)loading plugins into a specific channel or user.
 * 
 * The minimum privilege level is LEVEL_ADMINISTRATOR for all channel-related
 * tasks and LEVEL_OWNER for all global tasks.
 * 
 * The available commands are:
 * 
 * !command                Shows all loaded commands 
 * !command load [command] Temporarily loads one command 
 * !command add [command]  Permanently adds a command
 * !command reload         Reloads all commands
 * 
 * @author Ralph Broenink
 */
public class CommandCommand extends ThreadedCommand {

	@Override
	public boolean isHandled(Channel channel, User user, CommandMessage msg) {
		return msg.isCommand("command");
	}
	
	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		if (!Command.checkPermissions("managing commands", UserLevel.ADMINISTRATOR, channel, user))
			return null;

		Plugin commandPlugin = this.getPlugin();
		if (commandPlugin == null) {
			Command.sendErrorMessage(channel, user, 
					"The plugin plugins.CommandPlugin is not loaded, which is kinda weird.");
			return null;
		}
		
		try {
			List<String> arguments = this.parseArguments(channel, user, msg);

			if (arguments.size() == 1) { // No arguments
				/*
				 * Command: !command 
				 * Shows all loaded commands.
				 */
				listCommands(channel, user, commandPlugin);
				
			} else if (arguments.get(1).toLowerCase().equals("load")) {
				/*
				 * Command: !command load <command> 
				 * Loads the command temporarily for the current user or channel
				 */
				
				if (arguments.size() == 2) { // Only !plugin load is passed
					Command.sendErrorMessage(channel, user, "Invalid command. Use '!help plugin' for more information.");
				} else { // Argument is passed
					this.loadCommand(channel, user, commandPlugin, arguments.get(2));
				}

			} else if (arguments.get(1).toLowerCase().equals("add")) {
				/*
				 * Command: !command add [command] 
				 * Loads the command permanent for  the current user or channel
				 */
				
				if (arguments.size() == 2) { // Only !plugin add is passed
					Command.sendErrorMessage(channel, user, "Invalid command. Use '!help plugin' for more information.");
					
				} else {
					// TODO: Implement
					Command.sendErrorMessage(channel, user,  "Not implemented yet.");
				}

			} else if (arguments.get(1).toLowerCase().equals("reload")) {
				/*
				 * Command: !command reload 
				 * Reloads all commands
				 */
				
				this.reloadCommands(channel, user, commandPlugin);
				
			} else {
				Command.sendErrorMessage(channel, user, "Invalid command. Use '!help plugin' for more information.");
			}
		 
		} catch (IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | SecurityException
				| InvocationTargetException e) {
			Logger.getLogger(CommandCommand.class.getName()).log(Level.SEVERE, null, e);
		}
		return null;
	}


	
	/**
	 * Lists all commands for the given channel/user.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	@SuppressWarnings("unchecked")
	private void listCommands(Channel channel, User user, Plugin plugin) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// It's not a channel, but a nick
		if (channel == null) {
			user.sendMessage("The following commands are loaded for " + user.getNickname() + ":");
			String r = "";
			Set<Command> s = (Set<Command>) plugin.getClass().getMethod("getCommands", User.class).invoke(plugin, user);
			for (Command c : s)
				r += c.getName() + "; ";
			user.sendMessage(r);
		} else {
			channel.sendMessage("The following commands are loaded for " + channel.getName() + ":");
			String r = "";
			Set<Command> s = (Set<Command>) plugin.getClass().getMethod("getCommands", Channel.class).invoke(plugin, channel);
			for (Command c : s)
				r += c.getName() + "; ";
			channel.sendMessage(r);
		}
	}
	
	/**
	 * Loads the command for the given channel/user
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */
	private void loadCommand(Channel channel, User user, Plugin plugin, String command) throws IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException, InvocationTargetException {
		Object o = null;
		try {
			o = plugin.getClass().getMethod("createCommand", String.class).invoke(plugin, command);
		} catch (InvocationTargetException e) { }
		
		if (o == null) {
			Command.sendMessage(channel, user,
					Colors.BOLD + Colors.RED + "Loading plugin failed.");
		} else {
			Command c = (Command) o;
			c.init(plugin, -1, command, false, false);
			if (channel == null) {
				plugin.getClass().getMethod("register", Command.class, User.class).invoke(plugin, c, user);
			} else {
				plugin.getClass().getMethod("register", Command.class, Channel.class).invoke(plugin, c, channel);
			}
			Command.sendMessage(channel, user, "Command loaded. For permanent use, please use 'add'");
		}
	}
	
	/**
	 * Reloads all commands in the given channel.
	 */
	private void reloadCommands(Channel channel, User user, Plugin plugin) {
		// This looks a bit weird, but since we're working in a thread
		// and that reloading stuff is difficult, we're doing it this
		// way.
		final Channel c = channel;
		final User u = user;
		final Plugin cp = plugin;
		(new Thread() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				Command.sendMessage(c, u, "Reloading all commands...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) { }

				try {
					Set<Command> cmds = new HashSet<Command>((Set<Command>) cp.getClass().getMethod("getCommands").invoke(cp));
					for (Command c : cmds)
						cp.getClass().getMethod("reloadCommand", Command.class).invoke(cp, c);
				} catch (Exception e) {
					Command.sendErrorMessage(c, u, "Fatal error while reloading commands. Got error "
									+ e.getClass().getName() + " "
									+ e.getMessage());
				}

				Command.sendMessage(c, u, "All commands are reloaded (except those added by !command load)");
			}
		}).start();
	}
	
}
