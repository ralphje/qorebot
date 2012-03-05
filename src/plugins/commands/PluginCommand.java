package plugins.commands;

import command.Command;
import command.CommandMessage;
import command.ThreadedCommand;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jibble.pircbot.Colors;
import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;
import qorebot.plugins.Plugin;
import qorebot.plugins.Pluginable;

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
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		if (!Command.checkPermissions("managing plugins",
				UserLevel.ADMINISTRATOR, channel, user))
			return null;

		if (msg.isCommand("plugin")) {
			List<String> arguments = this.parseArguments(channel, user, msg);

			/*
			 * Command: !plugin Shows all loaded plugins.
			 */
			// No arguments
			if (arguments.size() == 1) {

				// It's not a channel, but a nick
				if (channel == null) {
					user.sendMessage("The following plugins are loaded for "
							+ user.getNickname() + ":");
					String r = "";
					for (Plugin p : user.getPlugins())
						r += p.getName() + "; ";
					user.sendMessage(r);

					// It's a channel.
				} else {
					channel.sendMessage("The following plugins are loaded for "
							+ channel.getName() + ":");
					String r = "";
					for (Plugin p : channel.getPlugins())
						r += p.getName() + "; ";
					channel.sendMessage(r);
				}

			/*
			 * Command: !plugin load <plugin> 
			 * Loads the plugin temporarily for the current user or channel
			 */
				
			} else if (arguments.get(1).toLowerCase().equals("load")) {
				if (arguments.size() == 2) {
					// Only !plugin load is passed
					this.error(channel, user);

					// Argument is passed.
				} else {
					// Get plugin
					Plugin p = null;
					if (channel == null)
						p = user.getBot().createPlugin(arguments.get(2));
					else
						p = channel.getBot().createPlugin(arguments.get(2));

					// Check if the plugin is loaded
					if (p == null) {
						Command.sendMessage(channel, user, Colors.BOLD
								+ Colors.RED + "Loading plugin failed.");

					} else {
						// Loads the plugin
						p.init(user.getBot(), -1, arguments.get(2), false,
								false);
						Pluginable pl = (channel == null ? user : channel);
						pl.register(p);
						Command.sendMessage(channel, user,
								"Plugin loaded. For permanent use, please use 'add'");
					}
				}

			/*
			 * Command: !plugin add [plugin] 
			 * Loads the plugin permanent for  the current user or channel
			 */
			} else if (arguments.get(1).toLowerCase().equals("add")) {
				if (arguments.size() == 2) {
					this.error(channel, user);
				} else {
					// TODO: Implement
					Command.sendMessage(channel, user, Colors.BOLD + Colors.RED
							+ "Not implemented yet.");
				}

			/*
			 * Command: !plugin reload 
			 * Reloads all plugins
			 */
			} else if (arguments.get(1).toLowerCase().equals("reload")) {

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
						} catch (InterruptedException ex) {
						}

						try {
							Set<Plugin> plgins = new HashSet<Plugin>(u.getBot()
									.getPlugins());
							for (Plugin p : plgins)
								u.getBot().reloadPlugin(p);
						} catch (Exception e) {
							Command.sendMessage(c, u, Colors.BOLD + Colors.RED
											+ "Fatal error while reloading plugins. Got error "
											+ e.getClass().getName() + " "
											+ e.getMessage());
						}

						Command.sendMessage(c, u, "All plugins are reloaded");
					}
				}).start();

			} else {
				this.error(channel, user);
			}
		}
		return null;
	}

	@Override
	public boolean isHandled(Channel channel, User user, CommandMessage msg) {
		return msg.isCommand("plugin");
	}

	private void error(Channel channel, User user) {
		Command.sendMessage(channel, user, Colors.BOLD + Colors.RED
				+ "Invalid command. Use '!help plugin' for more information.");
	}
}
