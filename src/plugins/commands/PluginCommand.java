package plugins.commands;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;
import qorebot.plugins.Plugin;
import qorebot.plugins.Pluginable;
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
 * !plugins              Shows all loaded plugins 
 * !plugin load [plugin] Temporarily loads one  plugin 
 * !plugin add [plugin]  Permanently adds a plugin 
 * !plugin reload        Reloads all plugins
 * 
 * @author Ralph Broenink
 */
public class PluginCommand extends ThreadedCommand {

	@Override
	public boolean isHandled(Channel channel, User user, CommandMessage msg) {
		return msg.isCommand("plugin");
	}
	
	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		if (!Command.checkPermissions("managing plugins", UserLevel.ADMINISTRATOR, channel, user))
			return null;

		if (msg.isCommand("plugin")) {
			List<String> arguments = this.parseArguments(channel, user, msg);

			if (arguments.size() == 1) { // No arguments
				/*
				 * Command: !plugin 
				 * Shows all loaded plugins.
				 */
				listPlugins(channel, user);
				
			} else if (arguments.get(1).toLowerCase().equals("load")) {
				/*
				 * Command: !plugin load <plugin> 
				 * Loads the plugin temporarily for the current user or channel
				 */
				
				if (arguments.size() == 2) { // Only !plugin load is passed
					Command.sendErrorMessage(channel, user, "Invalid command. Use '!help plugin' for more information.");
				} else { // Argument is passed
					this.loadPlugin(channel, user,arguments.get(2));
				}

			} else if (arguments.get(1).toLowerCase().equals("add")) {
				/*
				 * Command: !plugin add [plugin] 
				 * Loads the plugin permanent for  the current user or channel
				 */
				
				if (arguments.size() == 2) { // Only !plugin add is passed
					Command.sendErrorMessage(channel, user, "Invalid command. Use '!help plugin' for more information.");
					
				} else {
					// TODO: Implement
					Command.sendErrorMessage(channel, user,  "Not implemented yet.");
				}

			} else if (arguments.get(1).toLowerCase().equals("reload")) {
				/*
				 * Command: !plugin reload 
				 * Reloads all plugins
				 */
				
				this.reloadPlugins(channel, user);
				
			} else {
				Command.sendErrorMessage(channel, user, "Invalid command. Use '!help plugin' for more information.");
			}
		}
		return null;
	}


	
	/**
	 * Lists all plugins for the given channel/user.
	 */
	private void listPlugins(Channel channel, User user) {
		// It's not a channel, but a nick
		if (channel == null) {
			user.sendMessage("The following plugins are loaded for " + user.getNickname() + ":");
			String r = "";
			for (Plugin p : user.getPlugins())
				r += p.getName() + "; ";
			user.sendMessage(r);

			// It's a channel.
		} else {
			channel.sendMessage("The following plugins are loaded for " + channel.getName() + ":");
			String r = "";
			for (Plugin p : channel.getPlugins())
				r += p.getName() + "; ";
			channel.sendMessage(r);
		}
	}
	
	/**
	 * Loads the plugin for the given channel/user
	 */
	private void loadPlugin(Channel channel, User user, String plugin) {
		// Get plugin
		Plugin p = user.getBot().createPlugin(plugin);

		// Check if the plugin is loaded
		if (p == null) {
			Command.sendErrorMessage(channel, user, "Loading plugin failed.");

		} else {
			// Loads the plugin
			p.init(user.getBot(), -1, plugin, false, false);
			Pluginable pl = (channel == null ? user : channel);
			pl.register(p);
			Command.sendMessage(channel, user,
					"Plugin loaded. For permanent use, please use 'add'");
		}
	}
	
	/**
	 * Reloads all plugins in the given channel.
	 */
	private void reloadPlugins(Channel channel, User user) {
		// This looks a bit weird, but since we're working in a thread
		// and that reloading stuff is difficult, we're doing it this
		// way.
		final Channel c = channel;
		final User u = user;
		(new Thread() {
			@Override
			public void run() {
				Command.sendMessage(c, u, "Reloading all plugins...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) { }

				try {
					Set<Plugin> plgins = new HashSet<Plugin>(u.getBot().getPlugins());
					for (Plugin p : plgins)
						u.getBot().reloadPlugin(p);
				} catch (Exception e) {
					Command.sendErrorMessage(c, u, "Fatal error while reloading plugins. Got error "
									+ e.getClass().getName() + " "
									+ e.getMessage());
				}

				Command.sendMessage(c, u, "All plugins are reloaded (except those added by !plugin load)");
			}
		}).start();
	}
	
}
