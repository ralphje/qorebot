package plugins.commands;


import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	public List<String> supportedCommands() {
		return Command.createList("command", "commands");
	}
	
	@Override
	public List<String> listedCommands(Channel channel, User user) {
		if (user.hasLevel(UserLevel.ADMINISTRATOR, channel))
			return Command.createList("command", "commands");
		else
			return Command.createList("commands");
	}
	
	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		try {
			Plugin commandPlugin = this.getPlugin();
			if (commandPlugin == null) {
				Command.sendErrorMessage(channel, user, 
						"The plugin plugins.CommandPlugin is not loaded, which is kinda weird.");
				return null;
			}
			
			if (msg.isCommand("commands")) {
				listCommands(channel, user, commandPlugin);
				
			} else if (msg.isCommand("command")) {
				
				if (!Command.checkPermissions("managing commands", UserLevel.ADMINISTRATOR, channel, user))
					return null;
		
			
				List<String> arguments = this.parseArguments(channel, user, msg);
	
				if (arguments.size() == 1) { // No arguments
					/*
					 * Command: !command 
					 * Shows all loaded commands.
					 */
					listCommand(channel, user, commandPlugin);
	
				} else if (arguments.get(1).toLowerCase().equals("install")) {
					/*
					 * Command: !plugin install <plugin> <autoloadC> <autoloadU> 
					 * Installs a plugin into the database.
					 */
					if (!Command.checkPermissions("installing commands", UserLevel.OWNER, channel, user))
						return null;
					
					if (arguments.size() < 5) { // Only !plugin load is passed
						Command.sendErrorMessage(channel, user, "Invalid command. Use '!help command' for more information.");
					} else { // Argument is passed
						this.installCommand(channel, user, commandPlugin,
								arguments.get(2), 
								Command.stringToBool(arguments.get(3)),
								Command.stringToBool(arguments.get(4)));
					}
					
				} else if (arguments.get(1).toLowerCase().equals("load")) {
					/*
					 * Command: !command load <command> 
					 * Loads the command temporarily for the current user or channel
					 */
					
					if (arguments.size() == 2) { // Only !plugin load is passed
						Command.sendErrorMessage(channel, user, "Invalid command. Use '!help command' for more information.");
					} else { // Argument is passed
						this.loadCommand(channel, user, commandPlugin, arguments.get(2));
					}
	
				} else if (arguments.get(1).toLowerCase().equals("unload")) {
					/*
					 * Command: !command load <command> 
					 * Loads the command temporarily for the current user or channel
					 */
					
					if (arguments.size() == 2) { // Only !plugin load is passed
						Command.sendErrorMessage(channel, user, "Invalid command. Use '!help command' for more information.");
					} else { // Argument is passed
						this.unloadCommand(channel, user, commandPlugin, arguments.get(2));
					}
					
				} else if (arguments.get(1).toLowerCase().equals("add")) {
					/*
					 * Command: !command add [command] 
					 * Loads the command permanent for  the current user or channel
					 */
					
					if (arguments.size() == 2) { // Only !plugin add is passed
						Command.sendErrorMessage(channel, user, "Invalid command. Use '!help command' for more information.");
						
					} else {
						this.addCommand(channel, user, commandPlugin, arguments.get(2));
					}
	
				} else if (arguments.get(1).toLowerCase().equals("reload")) {
					/*
					 * Command: !command reload 
					 * Reloads all commands
					 */
					
					this.reloadCommands(channel, user, commandPlugin);
					
				} else {
					Command.sendErrorMessage(channel, user, "Invalid command. Use '!help command' for more information.");
				}
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
		Set<Command> commands = null;
		Set<String> userCommands = new TreeSet<String>();
		if (channel == null) {
			commands = (Set<Command>) plugin.getClass().getMethod("getCommands", User.class).invoke(plugin, user);
		} else {
			commands = (Set<Command>) plugin.getClass().getMethod("getCommands", Channel.class).invoke(plugin, channel);
		}
		for (Command c : commands) {
			userCommands.addAll(c.listedCommands(channel, user));
		}
		String result = "The following commands are supported for " + (channel == null ? user.getNickname() : channel.getName()) + ": ";
		for (String s : userCommands) {
			result += s + ", ";
		}
		result = result.substring(0,result.length()-2);
		Command.sendMessage(channel, user, result);
	}
	
	/**
	 * Lists all loaded commands (their plugin names) for the given channel/user.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	@SuppressWarnings("unchecked")
	private void listCommand(Channel channel, User user, Plugin plugin) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
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
	 * Installs the command
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void installCommand(Channel channel, User user, Plugin plugin, String command, boolean autoregisterChannels, boolean autoregisterUsers) throws IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException {
		Object o = null;
		try {
			o = plugin.getClass().getMethod("createCommand", String.class).invoke(plugin, command);
		} catch (InvocationTargetException e) { }

		// Check if the command is loaded
		if (o == null) {
			Command.sendErrorMessage(channel, user, "Command could not be found.");

		} else {
			// Loads the plugin
			if (Command.install(plugin, command, autoregisterChannels, autoregisterUsers)) {
				if (autoregisterChannels && autoregisterUsers) {
					Command.sendMessage(channel, user, "Command was successfully installed and loaded for every channel and user.");
				} else if (autoregisterChannels) {
					Command.sendMessage(channel, user, "Command was successfully installed and loaded for every channel.");
				} else if (autoregisterUsers) {
					Command.sendMessage(channel, user, "Command was successfully installed and loaded for every user.");
				} else {
					Command.sendMessage(channel, user, "Command was successfully installed. Use 'load' or 'add' to load the plugin.");
				}
			} else {
				Command.sendErrorMessage(channel, user, "Command installation failed. Maybe the plugin was already installed.");
			}
		}
	}
	

	/**
	 * Loads the plugin for the given channel/user
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void addCommand(Channel channel, User user, Plugin plugin, String command) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// Find the command instance in the Bot
		Object o = plugin.getClass().getMethod("getCommand", String.class).invoke(plugin, command);
		if (o == null) {
			Command.sendErrorMessage(channel, user, "Command is unknown. Have you already installed it?");
			
		} else {
			Command c = (Command) o;
			// Loads the plugin
			if (channel == null) {
				plugin.getClass().getMethod("add", Command.class, User.class).invoke(plugin, c, user);
			} else {
				plugin.getClass().getMethod("add", Command.class, Channel.class).invoke(plugin, c, channel);
			}
			Command.sendMessage(channel, user,
					"Command loaded. For permanent use, please use 'add'");
		}
	}
	

	/**
	 * Loads the plugin for the given channel/user
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void loadCommand(Channel channel, User user, Plugin plugin, String command) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// Find the command instance in the Bot
		Object o = plugin.getClass().getMethod("getCommand", String.class).invoke(plugin, command);
		if (o == null) {
			Command.sendErrorMessage(channel, user, "Command is unknown. Have you already installed it?");
			
		} else {
			Command c = (Command) o;
			// Loads the plugin
			if (channel == null) {
				plugin.getClass().getMethod("register", Command.class, User.class).invoke(plugin, c, user);
			} else {
				plugin.getClass().getMethod("register", Command.class, Channel.class).invoke(plugin, c, channel);
			}
			Command.sendMessage(channel, user,
					"Command loaded. For permanent use, please use 'add'");
		}
	}
	
	/**
	 * Unloads the plugin for the given channel/user
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void unloadCommand(Channel channel, User user, Plugin plugin, String command) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// Find the command instance in the Bot
		Object o = plugin.getClass().getMethod("getCommand", String.class).invoke(plugin, command);
		if (o == null) {
			Command.sendErrorMessage(channel, user, "Command is unknown, so it isn't loaded.");
			
		} else {
			Command c = (Command) o;
			// Loads the plugin
			if (channel == null) {
				plugin.getClass().getMethod("unregister", Command.class, User.class).invoke(plugin, c, user);
			} else {
				plugin.getClass().getMethod("unregister", Command.class, Channel.class).invoke(plugin, c, channel);
			}
			Command.sendMessage(channel, user,
					"Command succesfully unloaded.");
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

				Command.sendMessage(c, u, "All commands are reloaded.");
			}
		}).start();
	}
	
}
