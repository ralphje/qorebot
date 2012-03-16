package plugins;

import qorebot.Channel;
import qorebot.Database;
import qorebot.EventType;
import qorebot.QoreBot;
import qorebot.User;
import qorebot.plugins.Plugin;
import qorebot.plugins.PluginLoader;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.message.CommandMessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core plugin that handles commands. Commands are very similar to plugins;
 * things are passed to them, they do something with it, can be dynamically
 * loaded and unloaded and have channel- and user-specific features.
 * 
 * Commands differ in the fact that they only respond to messages and are
 * expected to give some output. Furthermore, their output can be nested to
 * deliver some beautiful results.
 * 
 * Disabling this plugin for either users or channels will disable any features
 * implemented in a command. That can be useful if you only want plugins in a
 * specific channel. Note that disabling this plugin for only several users will
 * also disable the IdentifyCommand, which will create a vicious circle (users
 * not being able to identify and therefore can't identify).
 * 
 * @author Ralph Broenink
 */
public class CommandPlugin extends Plugin {
	private Set<Command> commands = new HashSet<Command>();

	private Map<Channel, Set<Command>> channels = new HashMap<Channel, Set<Command>>();
	private Map<User, Set<Command>> users = new HashMap<User, Set<Command>>();

	@Override
	public void init(QoreBot bot, int id, String name, boolean autoregisterChannels, boolean autoregisterUsers) {
		super.init(bot, id, name, autoregisterChannels, autoregisterUsers);
		this.loadCommands();
	}

	@Override
	public boolean isImplemented(EventType method) {
		return method == EventType.CHANNEL_ONMESSAGE
				|| method == EventType.USER_ONPRIVATEMESSAGE
				|| method == EventType.PLUGIN_ONCREATECHANNEL
				|| method == EventType.PLUGIN_ONCREATEUSER;
	}

	@Override
	public void onCreateUser(User user) {
		this.registerCommands(user);
	}

	@Override
	public void onCreateChannel(Channel channel) {
		this.registerCommands(channel);
	}

	@Override
	public void onPrivateMessage(User source, String message) {
		CommandMessage msg = new CommandMessage(null, message);
		String result = this.parseMessage(null, source, msg);
		if (result != null)
			source.sendMessage(result);
	}

	@Override
	public void onMessage(Channel channel, User sender, String message) {
		String alternatePrefix = this.getBot().getNick() + ":";

		if (!message.startsWith(Command.PREFIX)
				&& !message.startsWith(alternatePrefix))
			return;

		if (message.startsWith(alternatePrefix))
			message = message.substring(alternatePrefix.length());

		CommandMessage msg = new CommandMessage(null, message);

		String result = this.parseMessage(channel, sender, msg);
		if (result != null)
			if (channel == null) {
				sender.sendMessage(result);
			} else {
				channel.sendMessage(result);
			}
	}

	/**
	 * Retrieves the result of all commands that match either the channel (if
	 * not null) or the user (is channel is null). All commands are executed,
	 * but only the first result is returned. Commands are sorted based on their
	 * hash.
	 * 
	 * @param channel
	 *            The channel the message came through. May be null.
	 * @param sender
	 *            The sender of the message.
	 * @param message
	 *            The message.
	 * @return The first result of any command, or null if none.
	 */
	public String parseMessage(Channel channel, User sender, CommandMessage message) {
		String result = null;
		if (channel != null) {
			for (Command c : this.channels.get(channel)) {
				String r = c.receive(channel, sender, message);
				result = (result != null || r == null ? result : r);
			}
		} else {
			for (Command c : this.users.get(sender)) {
				String r = c.receive(sender, message);
				result = (result != null || r == null ? result : r);
			}
		}
		return result;
	}

	/**
	 * Loads all commands from the database.
	 */
	protected void loadCommands() {
		Statement st = Database.gs();
		if (st == null)
			return;

		try {
			ResultSet result = st.executeQuery("SELECT id,name,autoregister_users,autoregister_channels FROM commands");
			while (result.next()) {
				String name = "";
				name = result.getString("name");
				Command command = this.createCommand(name);

				this.initCommand(command, result.getInt("id"), name,
						result.getBoolean("autoregister_channels"),
						result.getBoolean("autoregister_users"));
			}
		} catch (SQLException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed to retrieve command list.", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
	}

	/**
	 * Creates a command based on its name.
	 * 
	 * @param name
	 *            A name starting with plugins. or file:
	 * @return An instance of the Command.
	 */
	public Command createCommand(String name) {
		try {
			// The pluginloader can be used, as commands are in the same package
			// as plugins (and that's the only thing it checks for)
			Class<?> cl = new PluginLoader(QoreBot.class.getClassLoader()).loadClass(name);
			if (cl != null) {
				Object ob = cl.newInstance();
				return (Command) ob;
			} else {
				Logger.getLogger(CommandPlugin.class.getName()).log(
						Level.SEVERE, "Failed loading command {0}", name);
			}
		} catch (InstantiationException ex) {
			Logger.getLogger(CommandPlugin.class.getName()).log(Level.SEVERE,
					"Failed loading command " + name, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(CommandPlugin.class.getName()).log(Level.SEVERE,
					"Failed loading command " + name, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(CommandPlugin.class.getName()).log(Level.SEVERE,
					"Failed loading command " + name, ex);
		} catch (ClassCastException ex) {
			Logger.getLogger(CommandPlugin.class.getName()).log(Level.SEVERE,
					"Failed loading command " + name, ex);
		} catch (Exception ex) {
			Logger.getLogger(CommandPlugin.class.getName()).log(Level.SEVERE,
					"Failed loading command " + name, ex);
		}
		return null;
	}

	/**
	 * Initializes a command (acts as constructor)
	 * 
	 * @param command
	 *            The command
	 * @param id
	 *            The id
	 * @param name
	 *            The plugin name
	 * @param autoregisterChannels
	 * @param autoregisterUsers
	 */
	public void initCommand(Command command, int id, String name, boolean autoregisterChannels, boolean autoregisterUsers) {
		this.commands.add(command);
		command.init(this, id, name, autoregisterChannels, autoregisterUsers);
		this.registerCommand(command);
	}

	/**
	 * Reloads a command.
	 * 
	 * @param c
	 *            The command to reload
	 */
	public void reloadCommand(Command c) {
		String name = c.getName();
		int id = c.getId();
		boolean autoregisterChannels = c.isAutoregisterChannels();
		boolean autoregisterUsers = c.isAutoregisterUsers();
		this.commands.remove(c);
		for (Channel ch : this.channels.keySet())
			this.channels.get(ch).remove(c);
		for (User u : this.users.keySet())
			this.users.get(u).remove(c);

		Command command = this.createCommand(name);

		this.initCommand(command, id, name, autoregisterChannels,
				autoregisterUsers);
	}

	/**
	 * Registers all commands for the given channel.
	 * 
	 * @param channel
	 * @see QoreBot#registerPlugins(qorebot.Channel)
	 */
	public void registerCommands(Channel channel) {
		PreparedStatement st = Database.gps("SELECT command_id FROM commands_channels WHERE channel_id = ?");
		if (st == null)
			return;

		HashSet<Integer> registerCommands = new HashSet<Integer>();

		try {
			st.setInt(1, channel.getId());
			ResultSet result = st.executeQuery();

			while (result.next()) {
				registerCommands.add(result.getInt("command_id"));
			}
		} catch (SQLException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed to retrieve commands for channel", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}

		HashSet<Command> channelCommands = new HashSet<Command>();
		for (Command c : this.commands) {
			if (c.isAutoregisterChannels()
					|| registerCommands.contains(c.getId()))
				channelCommands.add(c);
		}
		this.channels.put(channel, channelCommands);
	}

	/**
	 * Registers all commands for the given user.
	 * 
	 * @param user
	 * @see QoreBot#registerPlugins(qorebot.User)
	 */
	public void registerCommands(User user) {
		HashSet<Integer> registerCommands = new HashSet<Integer>();
		if (user.isIdentified()) {

			PreparedStatement st = Database.gps("SELECT command_id FROM commands_users WHERE user_id = ?");
			if (st == null)
				return;

			try {
				st.setInt(1, user.getId());
				ResultSet result = st.executeQuery();

				while (result.next()) {
					registerCommands.add(result.getInt("command_id"));
				}
			} catch (SQLException ex) {
				Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
						"Failed to retrieve commands for user", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}

		HashSet<Command> channelCommands = new HashSet<Command>();
		for (Command c : this.commands) {
			if (c.isAutoregisterUsers() || registerCommands.contains(c.getId()))
				channelCommands.add(c);
		}
		this.users.put(user, channelCommands);
	}

	/**
	 * Registers a command to all channels and users it should register to.
	 * 
	 * @param command
	 *            The command to register
	 * @see QoreBot#registerPlugin(qorebot.plugins.Plugin)
	 */
	public void registerCommand(Command command) {
		if (command.isAutoregisterChannels()) {
			for (Channel c : this.getBot().getChannelSet()) {
				this.register(command, c);
			}
		} else {
			PreparedStatement st = Database
					.gps("SELECT channel_id FROM commands_channels WHERE command_id = ?");
			if (st == null)
				return;
			try {
				st.setInt(1, command.getId());
				ResultSet result = st.executeQuery();

				HashSet<Integer> registerChannels = new HashSet<Integer>();
				while (result.next()) {
					registerChannels.add(result.getInt("channel_id"));
				}
				for (Channel c : this.getBot().getChannelSet()) {
					if (registerChannels.contains(c.getId())) {
						this.register(command, c);
					}
				}
			} catch (SQLException ex) {
				Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
						"Failed to retrieve channels for command", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}

		if (command.isAutoregisterUsers()) {
			for (User u : this.getBot().getUsers()) {
				this.register(command, u);
			}
		} else {
			PreparedStatement st = Database
					.gps("SELECT user_id FROM commands_users WHERE command_id = ?");
			if (st == null)
				return;
			try {
				st.setInt(1, command.getId());
				ResultSet result = st.executeQuery();

				HashSet<Integer> registerUsers = new HashSet<Integer>();
				while (result.next()) {
					registerUsers.add(result.getInt("user_id"));
				}
				for (User u : this.getBot().getUsers()) {
					if (registerUsers.contains(u.getId())) {
						this.register(command, u);
					}
				}
			} catch (SQLException ex) {
				Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
						"Failed to retrieve channels for user", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
	}

	/**
	 * Registers a command to an user
	 * 
	 * @param command
	 *            The command to register
	 * @param user
	 *            The user to register with
	 */
	public void register(Command command, User user) {
		if (!this.users.containsKey(user))
			this.users.put(user, new HashSet<Command>());
		this.users.get(user).add(command);
	}

	/**
	 * Registers a command to a channel
	 * 
	 * @param command
	 *            The command to register
	 * @param channel
	 *            The channel to register with
	 */
	public void register(Command command, Channel channel) {
		if (!this.channels.containsKey(channel))
			this.channels.put(channel, new HashSet<Command>());
		this.channels.get(channel).add(command);
	}

	/**
	 * Registers a command to this object
	 * 
	 * @param command
	 *            The command to register
	 */
	public void register(Command command) {
		this.commands.add(command);
		this.registerCommand(command);
	}

	/**
	 * Unregisters a command from this object
	 * 
	 * @param command
	 *            The command to unregister
	 */
	public void unregister(Command command) {
		this.commands.remove(command);

		for (User u : this.users.keySet())
			this.users.get(u).remove(command);
		for (Channel c : this.channels.keySet())
			this.channels.get(c).remove(command);
	}

	/**
	 * Retrieves the current set of commands
	 */
	public Set<Command> getCommands() {
		return this.commands;
	}

	/**
	 * Retrieves the set of commands loaded for the given user
	 * 
	 * @param user
	 *            The user to retrieve a set of commands from
	 */
	public Set<Command> getCommands(User user) {
		return this.users.get(user);
	}

	/**
	 * Retrieves the set of commands loaded for the given channel
	 * 
	 * @param channel
	 *            The channel to retrieve a set of commands from
	 */
	public Set<Command> getCommands(Channel channel) {
		return this.channels.get(channel);
	}

}