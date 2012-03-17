package qorebot;

import qorebot.plugins.Plugin;
import qorebot.plugins.PluginLoader;
import qorebot.plugins.Pluginable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

/**
 * The main bot interface, handling all connecting/disconnecting and messages.
 * 
 * @author Ralph Broenink
 */
public class QoreBot extends PircBot {
	/** The bot version */
	public static final String VERSION = "QoreBot 1.0";
	/** The login name */
	public static final String LOGIN = "qorebot";
	/** The bot finger */
	public static final String FINGER = "QoreBot 1.0";

	private String server, nick;
	private HashSet<Channel> channels;
	private HashSet<User> users;
	private HashSet<Plugin> plugins;

	/**
	 * Builds the QoreBot using VERSION, LOGIN AND FINGER but doesn't connect to
	 * it.
	 */
	public QoreBot() {
		this.setVerbose(true);
		this.setAutoNickChange(true);
		this.setFinger(FINGER);
		this.setVersion(VERSION);
		this.setLogin(LOGIN);

		this.channels = new HashSet<Channel>();
		this.users = new HashSet<User>();
		this.plugins = new HashSet<Plugin>();

		this.loadPlugins();
	}

	/**
	 * Connects with a given server. If this.isConnected() == true, this method
	 * doesn't do anything.
	 * 
	 * @param server
	 *            The server to connect to
	 * @param nick
	 *            The nick to login with
	 * @return True if everything went ok (not necessarily equal to isConnected)
	 */
	public boolean connect(String server, String nick) {
		this.server = server;
		this.nick = nick;
		try {
			if (!this.isConnected()) {
				this.setName(nick);
				this.connect(server);
			}
			return true;
		} catch (NickAlreadyInUseException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE,
					"Connecting failed due to nickname conflict.", ex);
			return false;
		} catch (IrcException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE,
							"Connecting failed because the IRC server actively refused.", ex);
			return false;
		} catch (java.io.IOException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE,
					"Connecting failed due to a link I/O failure.", ex);
			return false;
		}
	}

	// -------------------------------------------------------------------------
	// Channel methods
	// -------------------------------------------------------------------------

	/**
	 * Retrieves all channels the bot is aware of. Note this method is
	 * inproperly named because PircBot already includes getChannels().
	 * 
	 * @return A set of all channels
	 */
	public Set<Channel> getChannelSet() {
		return this.channels;
	}

	/**
	 * Retrieves the Channel for a given channel name. Returns null if not
	 * found.
	 * 
	 * @param channel
	 *            The channel name
	 */
	public Channel getChannel(String channel) {
		for (Channel c : this.channels)
			if (c.getName().equals(channel))
				return c;
		Logger.getLogger(Database.class.getName()).log(Level.FINE,
				"Could not find channel ''{0}''.", channel);
		return null;
	}

	/**
	 * Checks whether a specific channel exists.
	 * 
	 * @param channel
	 *            The channel name
	 */
	public boolean isChannel(String channel) {
		if (channel.charAt(0) != '#')
			return false;

		for (Channel c : this.channels)
			if (c.getName().equals(channel))
				return true;

		return false;
	}

	/**
	 * Loads all channels from the database into Channels and connects to them
	 * if the autojoin setting is set.
	 */
	protected void loadChannels() {
		Statement st = Database.gs();
		if (st == null)
			return;

		try {
			ResultSet result = st.executeQuery("SELECT id,name,autojoin,`key` FROM channels");
			while (result.next()) {
				Channel ch = new Channel(this, result.getInt("id"), result.getString("name"), result.getString("key"));
				this.channels.add(ch);
				if (result.getBoolean("autojoin"))
					ch.join();
			}
		} catch (SQLException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed to retrieve channel list.", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
	}

	// -------------------------------------------------------------------------
	// User methods
	// -------------------------------------------------------------------------

	/**
	 * Alias for this.getUser(User.createUniqueId(nick, login, hostname))
	 * 
	 * @see #getUser(java.lang.String)
	 * @see User#createUniqueId(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public User getUser(String nick, String login, String hostname) {
		return this.getUser(User.createUniqueId(nick, login, hostname));
	}

	/**
	 * Retrieves a user by its unique IRC identifier, or a new user with this id
	 * when such a user doesn't exist.
	 * 
	 * @param uniqueId
	 *            The IRC unique identifier
	 */
	public User getUser(String uniqueId) {
		for (User u : this.users) {
			if (u.getUniqueId().equals(uniqueId))
				return u;
		}
		User user = new User(this, uniqueId);
		this.users.add(user);
		return user;
	}

	/**
	 * Retrieves an user by its nickname.
	 * 
	 * @param nickname
	 *            The nickname to look for
	 * @return The User object associated with this nickname, or null if none
	 *         found.
	 */
	public User getUserByNickname(String nickname) {
		for (User u : this.users) {
			if (u.getNickname().equals(nickname))
				return u;
		}
		return null;
	}

	/**
	 * Retrieves an user by its username. Note that this does not look up the
	 * username in the database; if the user is not connected, the user won't be
	 * found.
	 * 
	 * @param username
	 *            The username to look for
	 * @return The User object associated with this username, or null if none
	 *         found.
	 */
	public User getUserByUsername(String username) {
		for (User u : this.users) {
			if (u.getUsername().equals(username))
				return u;
		}
		return null;
	}

	/**
	 * Retrieves all known users.
	 * 
	 * @return All known users.
	 */
	public Set<User> getUsers() {
		return this.users;
	}

	// -------------------------------------------------------------------------
	// Plugin methods
	// -------------------------------------------------------------------------

	/**
	 * Retrieves the Plugin for a given plugin name. Returns null if not found.
	 * 
	 * @param plugin
	 *            The plugin name
	 */
	public Plugin getPlugin(String plugin) {
		for (Plugin p : this.plugins)
			if (p.getName().equals(plugin))
				return p;
		Logger.getLogger(Database.class.getName()).log(Level.FINE,
				"Could not find plugin ''{0}''.", plugin);
		return null;
	}

	/**
	 * Retrieves all loaded plugins.
	 */
	public Set<Plugin> getPlugins() {
		return this.plugins;
	}

	/**
	 * Loads all plugins from the database.
	 */
	protected final void loadPlugins() {
		Statement st = Database.gs();
		if (st == null)
			return;

		try {
			ResultSet result = st.executeQuery("SELECT id,name,autoregister_users,autoregister_channels FROM plugins");
			while (result.next()) {
				String name = "";
				name = result.getString("name");
				
				Plugin plugin = this.createPlugin(name);

				this.initPlugin(plugin, result.getInt("id"), name,
						result.getBoolean("autoregister_channels"),
						result.getBoolean("autoregister_users"));
			}
		} catch (SQLException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed to retrieve channel list.", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
	}

	/**
	 * Creates a plugin based on its name.
	 * 
	 * @param name
	 *            A name starting with plugins. or file:
	 * @return An instance of the Plugin.
	 */
	public Plugin createPlugin(String name) {
		try {
			// Plugin plugin = (Plugin) Class.forName(name).newInstance();
			Class<?> cl = (new PluginLoader(QoreBot.class.getClassLoader())).loadClass(name);
			if (cl != null) {
				Object ob = cl.newInstance();
				return (Plugin) ob;
			} else {
				Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
						"Failed loading plugin {0}", name);
			}
		} catch (InstantiationException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed loading plugin " + name, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed loading plugin " + name, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed loading plugin " + name, ex);
		} catch (ClassCastException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed loading plugin " + name, ex);
		} catch (Exception ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed loading plugin " + name, ex);
		}
		return null;
	}

	/**
	 * Initializes a plugin (acts as constructor)
	 * 
	 * @param plugin
	 *            The plugin
	 * @param id
	 *            The id
	 * @param name
	 *            The plugin name
	 * @param autoregisterChannels
	 * @param autoregisterUsers
	 */
	public void initPlugin(Plugin plugin, int id, String name, boolean autoregisterChannels, boolean autoregisterUsers) {
		this.plugins.add(plugin);
		plugin.init(this, id, name, autoregisterChannels, autoregisterUsers);
		this.registerPlugin(plugin);
	}

	/**
	 * Reloads a plugin.
	 * 
	 * @param p
	 */
	public void reloadPlugin(Plugin p) {
		String name = p.getName();
		int id = p.getId();
		boolean autoregisterChannels = p.isAutoregisterChannels();
		boolean autoregisterUsers = p.isAutoregisterUsers();
		this.plugins.remove(p);
		for (Channel c : this.channels)
			c.unregister(p);
		for (User u : this.users)
			u.unregister(p);

		// Plugin plugin = (Plugin) Class.forName(name).newInstance();
		Plugin plugin = this.createPlugin(name);

		this.initPlugin(plugin, id, name, autoregisterChannels,
				autoregisterUsers);

	}

	/**
	 * Loads all plugins for the given Pluginable object (either a User or a
	 * Channel). If the plugin should be registered,
	 * {@link Pluginable#register(Plugin)} is called, otherwise,
	 * {@link Pluginable#unregister(Plugin)} is called.
	 * 
	 * @param pluginable
	 *            The object for which plugins should be registered.
	 */
	public void registerPlugins(Pluginable pluginable) {

		// Prepare the SQL query
		PreparedStatement st = null;
		int id = 0;
		if (pluginable instanceof Channel) {
			st = Database.gps("SELECT plugin_id FROM plugins_channels WHERE channel_id = ?");
			id = ((Channel) pluginable).getId();
		} else if (pluginable instanceof User) {
			st = Database.gps("SELECT plugin_id FROM plugins_users WHERE user_id = ?");
			id = ((User) pluginable).getId();
		}

		// Check whether we have succeeded in the previous step
		if (st == null)
			return;

		// Start listing all plugins
		HashSet<Integer> registerPlugins = new HashSet<Integer>();

		try {
			st.setInt(1, id);
			ResultSet result = st.executeQuery();

			while (result.next()) {
				registerPlugins.add(result.getInt("plugin_id"));
			}
		} catch (SQLException ex) {
			Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
					"Failed to retrieve plugins for pluginable", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}

		// Now loop over all plugins and check whether they should be registered
		for (Plugin p : this.plugins) {
			// If it's a channel, and autoregisterChannels is true
			// or a user, and autoregisterUsers is true
			// or the plugin list contains this id
			// then register
			if (((pluginable instanceof Channel && p.isAutoregisterChannels()) 
					|| (pluginable instanceof User && p.isAutoregisterUsers()))
					|| registerPlugins.contains(p.getId()))
				pluginable.register(p);
			else
				pluginable.unregister(p);
		}
	}

	/**
	 * Registers a plugin to all channels and users it should register to.
	 * 
	 * @param plugin
	 *            The plugin to register
	 */
	public void registerPlugin(Plugin plugin) {
		// If it's set to autoregister, register to all channels.
		if (plugin.isAutoregisterChannels()) {
			for (Channel c : this.channels)
				c.register(plugin);

			// Otherwise, check for all channels in the database
		} else {
			PreparedStatement st = Database.gps("SELECT channel_id FROM plugins_channels WHERE plugin_id = ?");
			if (st == null)
				return;

			try {
				st.setInt(1, plugin.getId());
				ResultSet result = st.executeQuery();

				HashSet<Integer> registerChannels = new HashSet<Integer>();
				while (result.next()) {
					registerChannels.add(result.getInt("channel_id"));
				}
				for (Channel c : this.channels) {
					if (registerChannels.contains(c.getId()))
						c.register(plugin);
				}
			} catch (SQLException ex) {
				Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
						"Failed to retrieve channels for plugin", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}

		// Now for all users, check if we should autoregister at all users.
		if (plugin.isAutoregisterUsers()) {
			for (User u : this.users)
				u.register(plugin);

			// Otherwise, we will check for all users in the database.
		} else {
			PreparedStatement st = Database.gps("SELECT user_id FROM plugins_users WHERE plugin_id = ?");
			if (st == null)
				return;
			try {
				st.setInt(1, plugin.getId());
				ResultSet result = st.executeQuery();

				HashSet<Integer> registerUsers = new HashSet<Integer>();
				while (result.next()) {
					registerUsers.add(result.getInt("user_id"));
				}
				for (User u : this.users) {
					if (u.isIdentified() && registerUsers.contains(u.getId()))
						u.register(plugin);
				}
			} catch (SQLException ex) {
				Logger.getLogger(QoreBot.class.getName()).log(Level.SEVERE,
						"Failed to retrieve channels for plugin", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
	}

	// -------------------------------------------------------------------------
	// Handles all bot calls
	// -------------------------------------------------------------------------

	@Override
	protected void onConnect() {
		this.loadChannels();
	}

	/**
	 * Retries to connect ten times, with increasing delay (60s, 70s, 80s, ...)
	 */
	@Override
	protected void onDisconnect() {
		int retry = 0;
		while (!this.isConnected() && retry < 10) {
			try {
				Thread.sleep(60000 + retry * 10000);
				this.connect(this.server, this.nick);
			} catch (InterruptedException e) {
				// don't really care about this
			}
		}
		retry++;
	}

	// Don't do anything

	@Override
	protected void onIncomingChatRequest(DccChat chat) {
	}

	@Override
	protected void onIncomingFileTransfer(DccFileTransfer transfer) {
	}

	@Override
	protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
	}

	@Override
	protected void onServerResponse(int code, String response) {
		// Whois reply handler
		if (code == QoreBot.RPL_WHOISUSER) {
			StringTokenizer tokenizer = new StringTokenizer(response);
			tokenizer.nextToken(); // not interesting info (we get our Nick back)
			String nick = tokenizer.nextToken();
			String login = tokenizer.nextToken();
			String hostname = tokenizer.nextToken();
			this.onWhoisUser(nick, login, hostname);
		}
	}

	@Override
	protected void onUnknown(String line) {
	}

	// Send to both all channels and all users

	@Override
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		User user = this.getUser(oldNick, login, hostname);
		user.setUniqueId(User.createUniqueId(newNick, login, hostname));

		for (Channel c : this.channels) {
			c.receive(new Event(EventType.CHANNEL_ONNICKCHANGE, c, user, oldNick, newNick));
		}
		user.receive(new Event(EventType.USER_ONNICKCHANGE, user, oldNick, newNick));
	}

	@Override
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		for (Channel c : this.channels) {
			c.receive(new Event(EventType.CHANNEL_ONQUIT, c, source, reason));
		}
		source.receive(new Event(EventType.USER_ONQUIT, source, reason));
	}

	// Send to either a channel or a user

	@Override
	protected void onAction(String sender, String login, String hostname, String target, String action) {
		User user = this.getUser(sender, login, hostname);
		if (this.isChannel(target)) {
			Channel c = this.getChannel(target);
			c.receive(new Event(EventType.CHANNEL_ONACTION, c, user, action));
		} else
			user.receive(new Event(EventType.USER_ONACTION, user, action));
	}

	@Override
	protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		if (this.isChannel(target)) {
			Channel c = this.getChannel(target);
			c.receive(new Event(EventType.CHANNEL_ONNOTICE, c, source, notice));
		} else
			source.receive(new Event(EventType.USER_ONNOTICE, source, notice));
	}

	// Only send it to users

	@Override
	protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		User u = this.getUser(sourceNick, sourceLogin, sourceHostname);
		u.receive(new Event(EventType.USER_ONUSERMODE, u, mode));
	}

	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		User u = this.getUser(sender, login, hostname);
		u.receive(new Event(EventType.USER_ONPRIVATEMESSAGE, u, message));
	}

	// Send to channels

	@Override
	protected void onChannelInfo(String channel, int userCount, String topic) {
		Channel c = this.getChannel(channel);
		c.receive(new Event(EventType.CHANNEL_ONCHANNELINFO, c, userCount,
				topic));
	}

	@Override
	protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONDEOP, c, source, recipient));
	}

	@Override
	protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONDEVOICE, c, source, recipient));
	}

	@Override
	protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONINVITE, c, source));
	}

	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		Channel c = this.getChannel(channel);
		User user = this.getUser(sender, login, hostname);
		c.receive(new Event(EventType.CHANNEL_ONJOIN, c, user));
	}

	@Override
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		Channel c = this.getChannel(channel);
		User kicker = this.getUser(kickerNick, kickerLogin, kickerHostname);
		c.receive(new Event(EventType.CHANNEL_ONKICK, c, kicker, recipientNick, reason));
	}

	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		Channel c = this.getChannel(channel);
		User user = this.getUser(sender, login, hostname);
		c.receive(new Event(EventType.CHANNEL_ONMESSAGE, c, user, message));
	}

	@Override
	protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONMODE, c, source, mode));
	}

	@Override
	protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONOP, c, source, recipient));
	}

	@Override
	protected void onPart(String channel, String sender, String login, String hostname) {
		Channel c = this.getChannel(channel);
		User user = this.getUser(sender, login, hostname);
		c.receive(new Event(EventType.CHANNEL_ONPART, c, user));
	}

	@Override
	protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVECHANNELBAN, c, source,
				hostmask));
	}

	@Override
	protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVECHANNELKEY, c, source,
				key));
	}

	@Override
	protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVECHANNELLIMIT, c, source));
	}

	@Override
	protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVEINVITEONLY, c, source));
	}

	@Override
	protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVEMODERATED, c, source));
	}

	@Override
	protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVENOEXTERNALMESSAGES, c, source));
	}

	@Override
	protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVEPRIVATE, c, source));
	}

	@Override
	protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVESECRET, c, source));
	}

	@Override
	protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONREMOVETOPICPROTECTION, c, source));
	}

	@Override
	protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETCHANNELBAN, c, source, hostmask));
	}

	@Override
	protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETCHANNELKEY, c, source, key));
	}

	@Override
	protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETCHANNELLIMIT, c, source, limit));
	}

	@Override
	protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETINVITEONLY, c, source));
	}

	@Override
	protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETMODERATED, c, source));
	}

	@Override
	protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETNOEXTERNALMESSAGES, c, source));
	}

	@Override
	protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETPRIVATE, c, source));
	}

	@Override
	protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETSECRET, c, source));
	}

	@Override
	protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONSETTOPICPROTECTION, c, source));
	}

	@Override
	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		Channel c = this.getChannel(channel);
		c.receive(new Event(EventType.CHANNEL_ONTOPIC, c, topic, setBy, date,
				changed));
	}

	@Override
	protected void onUserList(String channel, org.jibble.pircbot.User[] users) {
		Channel c = this.getChannel(channel);
		c.receive(new Event(EventType.CHANNEL_ONUSERLIST, c, users));
		// Perform a WHOIS on every user we don't know yet
		for (org.jibble.pircbot.User u : users) {
			if (this.getUserByNickname(u.getNick()) == null)
					this.whois(u.getNick());
		}
	}

	@Override
	protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Channel c = this.getChannel(channel);
		User source = this.getUser(sourceNick, sourceLogin, sourceHostname);
		c.receive(new Event(EventType.CHANNEL_ONVOICE, c, source, recipient));
	}
	
	// -------------------------------------------------------------------------
	// Extra methods
	// -------------------------------------------------------------------------
	/**
     * Requests WHOIS information from the server.
     * 
     * @param nick    The nick of the user to whois.
     */
    public final void whois(String nick) {
        this.sendRawLine("WHOIS " +  nick);
    }
    
    /**
     * Receive information about a whois request. Is used to create users when
     * this is required.
     * 
     * @param nick The nickname of the user
     * @param login The login name of the user
     * @param hostname The hostname of the user
     */
    protected void onWhoisUser(String nick, String login, String hostname) {
    	this.getUser(nick, login, hostname);
    }
}
