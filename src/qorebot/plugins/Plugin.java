package qorebot.plugins;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import qorebot.Channel;
import qorebot.Database;
import qorebot.Event;
import qorebot.EventType;
import qorebot.QoreBot;
import qorebot.User;

/**
 * The base class for all plugins. Should be extended.
 * 
 * @author Ralph Broenink
 */
public abstract class Plugin {
	private QoreBot bot;
	private int id;
	private boolean autoregisterChannels = false;
	private boolean autoregisterUsers = false;
	private String name = null;

	/**
	 * Initializes the plugin.
	 * 
	 * @param bot
	 *            The bot this plugin works on
	 * @param id
	 *            The id of the plugin, or -1 if this is dynamically loaded
	 * @param name
	 *            The name of the plugin
	 * @param autoregisterChannels
	 *            True when the plugin should autoregister with new channels
	 * @param autoregisterUsers
	 *            True when the plugin should autoregister with new users
	 */
	public void init(QoreBot bot, int id, String name, boolean autoregisterChannels, boolean autoregisterUsers) {
		this.bot = bot;
		this.id = id;
		this.name = name;
		this.autoregisterChannels = autoregisterChannels;
		this.autoregisterUsers = autoregisterUsers;
	}

	// -------------------------------------------------------------------------
	// Plugin management
	// -------------------------------------------------------------------------

	/**
	 * Sets whether this plugin should autoregister with new channels.
	 * 
	 * @param autoregister
	 *            The new value
	 */
	public void setAutoregisterChannels(boolean autoregister) {
		this.autoregisterChannels = autoregister;
		this.setAutoregister(autoregister, "autoregister_channels");
	}

	/**
	 * Sets whether this plugin should autoregister with new users.
	 * 
	 * @param autoregister
	 *            The new value
	 */
	public void setAutoregisterUsers(boolean autoregister) {
		this.autoregisterUsers = autoregister;
		this.setAutoregister(autoregister, "autoregister_users");
	}

	/**
	 * Updates the given property in the database for this plugin.
	 * 
	 * @param autoregister
	 *            The new value for the property
	 * @param property
	 *            The name of the column that should be updated WARNING: This is
	 *            not SQL injection safe.
	 */
	private void setAutoregister(boolean autoregister, String property) {
		if (this.id > 0) {
			PreparedStatement st = Database.gps("UPDATE plugins SET " + property + " = ? WHERE id = ?");
			if (st == null)
				return;
			try {
				st.setBoolean(1, autoregister);
				st.setInt(2, this.id);
				st.executeUpdate();
			} catch (SQLException ex) {
				Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE,
						"Failed to update autoregister setting", ex);
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
	 * Permanently installs the plugin to a Pluginable object.
	 * 
	 * @param pluginable
	 *            The object to which to install the plugin, either a user or a
	 *            channel.
	 */
	public void add(Pluginable pluginable) {
		// Get the SQL query straight
		PreparedStatement st = null;
		int id = 0;
		if (pluginable instanceof Channel) {
			st = Database.gps("INSERT INTO plugins_channels(plugin_id, channel_id) VALUES(?,?)");
			id = ((Channel) pluginable).getId();
		} else if (pluginable instanceof User) {
			st = Database.gps("INSERT INTO plugins_users(plugin_id, user_id) VALUES(?,?)");
			id = ((User) pluginable).getId();
		}
		
		if (st == null)
			return;
		
		// Insert into the database
		try {
			st.setInt(1, this.getId());
			st.setInt(2, id);
			st.executeUpdate();
			
		} catch (SQLException ex) {
			Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE,
					"Failed to add the plugin", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
		
		// Add to the object
		pluginable.register(this);
	}

	/**
	 * Installs the Plugin with the given name into the bot.
	 * 
	 * @param bot
	 *            The bot in which we should install the plugin
	 * @param name
	 *            The name of the plugin to install.
	 * @param autoregisterChannels
	 *            True if the plugin should be registered automatically to every
	 *            channel.
	 * @param autoregisterUsers
	 *            True if the plugin should be registered automatically to every
	 *            user.
	 * @return True iff the installation succeeded.
	 */
	public static boolean install(QoreBot bot, String name, boolean autoregisterChannels, boolean autoregisterUsers) {
		// Load plugin and check if it exists.
		Plugin plugin = bot.createPlugin(name);
		if (plugin == null) {
			Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE,
					"Could not load and thus not install the plugin " + name);
			return false;
		}
		
		// Insert the plugin to the plugins table
		PreparedStatement st = Database.gps("INSERT INTO plugins(name, autoregister_channels, autoregister_users) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		if (st == null)
			return false;
		
		ResultSet keys = null;
		int id = 0;
		
		try {
			st.setString(1, name);
			st.setBoolean(2, autoregisterChannels);
			st.setBoolean(3, autoregisterUsers);
			st.executeUpdate();
			
			keys = st.getGeneratedKeys();
			keys.next();
			id = keys.getInt(1);
		} catch (SQLException ex) {
			Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE,
					"Failed to install the plugin", ex);
			return false;
		} finally {
			try {
				if (keys != null)
					keys.close();
			} catch (SQLException ex1) {
			}
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}

		// Call the installation handler of this plugin
		plugin.handleInstalled();
		
		// Update the bot with the new information
		bot.initPlugin(plugin, id, name, autoregisterChannels, autoregisterUsers);
		
		return true;
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------
	
	/**
	 * Returns the bot this plugin works on.
	 */
	public final QoreBot getBot() {
		return this.bot;
	}

	/**
	 * Checks whether this plugin should autoregister with new channels.
	 */
	public boolean isAutoregisterChannels() {
		return this.autoregisterChannels;
	}

	/**
	 * Checks whether this plugin should autoregister with new users.
	 */
	public boolean isAutoregisterUsers() {
		return this.autoregisterUsers;
	}

	/**
	 * Returns the id of the plugin.
	 */
	public final int getId() {
		return this.id;
	}

	/**
	 * Returns the name of the plugin.
	 */
	public final String getName() {
		return this.name;
	}
	
	/**
	 * The install method is called when the Plugin is being installed. This
	 * can be used to create a table structure and/or provide additional data.
	 * Please note that this function may be called multiple times.
	 */
	public void handleInstalled() {
	}
	
	// -------------------------------------------------------------------------
	// isImplemented method
	// -------------------------------------------------------------------------

	/**
	 * Checks whether the method (defined in Event) is handled by this class.
	 * 
	 * @param method
	 *            The method to check for
	 * @return True if the defined method is handled by this plugin
	 */
	public abstract boolean isImplemented(EventType method);


	// -------------------------------------------------------------------------
	// Event handlers
	// -------------------------------------------------------------------------
	
	/**
	 * Receives an event from a pluginable object. Should not be overriden by
	 * any plugin. Passes the event to handleEvent if the event is implemented
	 * by this plugin.
	 * 
	 * @param e
	 *            The event to handle
	 */
	public void receive(Event e) {
		if (this.isImplemented(e.getEvent()))
			this.handleEvent(e);
	}

	/**
	 * Handles a received event. May be overriden by an implementing class; the
	 * base functionality is to redirect the event to methods like onMessage.
	 * 
	 * @param e
	 *            The event to pass.
	 */
	public void handleEvent(Event e) {
		this.redirect(e);
	}

	/**
	 * Redirects the event to implementing methods. Should not be overriden;
	 * changes can be made in the handleEvent-method.
	 * 
	 * @param e
	 *            The event to redirect.
	 */
	protected final void redirect(Event e) {
		switch (e.getEvent()) {
		case PLUGIN_ONCREATEUSER:
			this.onCreateUser(e.getUser());
			break;
		case PLUGIN_ONCREATECHANNEL:
			this.onCreateChannel(e.getChannel());
			break;

		case USER_ONACTION:
			this.onAction(e.getUser(), e.getString1());
			break;
		case USER_ONNICKCHANGE:
			this.onNickChange(e.getUser(), e.getString1(), e.getString2());
			break;
		case USER_ONNOTICE:
			this.onNotice(e.getUser(), e.getString1());
			break;
		case USER_ONPRIVATEMESSAGE:
			this.onPrivateMessage(e.getUser(), e.getString1());
			break;
		case USER_ONQUIT:
			this.onQuit(e.getUser(), e.getString1());
			break;
		case USER_ONUSERMODE:
			this.onUserMode(e.getUser(), e.getString1());
			break;

		case CHANNEL_ONACTION:
			this.onAction(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONCHANNELINFO:
			this.onChannelInfo(e.getChannel(), e.getInt1(), e.getString1());
			break;
		case CHANNEL_ONDEOP:
			this.onDeop(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONDEVOICE:
			this.onDeVoice(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONINVITE:
			this.onInvite(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONJOIN:
			this.onJoin(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONKICK:
			this.onKick(e.getChannel(), e.getUser(), e.getString1(), e.getString2());
			break;
		case CHANNEL_ONMESSAGE:
			this.onMessage(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONMODE:
			this.onMode(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONNICKCHANGE:
			this.onNickChange(e.getChannel(), e.getUser(), e.getString1(), e.getString2());
			break;
		case CHANNEL_ONNOTICE:
			this.onNotice(e.getChannel(), e.getUser(), e.getString1(), e.getString2());
			break;
		case CHANNEL_ONOP:
			this.onOp(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONPART:
			this.onPart(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONQUIT:
			this.onQuit(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONREMOVECHANNELBAN:
			this.onRemoveChannelBan(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONREMOVECHANNELKEY:
			this.onRemoveChannelKey(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONREMOVECHANNELLIMIT:
			this.onRemoveChannelLimit(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONREMOVEINVITEONLY:
			this.onRemoveInviteOnly(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONREMOVEMODERATED:
			this.onRemoveModerated(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONREMOVENOEXTERNALMESSAGES:
			this.onRemoveNoExternalMessages(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONREMOVESECRET:
			this.onRemoveSecret(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONREMOVETOPICPROTECTION:
			this.onRemoveTopicProtection(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONSETCHANNELBAN:
			this.onSetChannelBan(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONSETCHANNELKEY:
			this.onSetChannelKey(e.getChannel(), e.getUser(), e.getString1());
			break;
		case CHANNEL_ONSETCHANNELLIMIT:
			this.onSetChannelLimit(e.getChannel(), e.getUser(), e.getInt1());
			break;
		case CHANNEL_ONSETINVITEONLY:
			this.onSetInviteOnly(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONSETMODERATED:
			this.onSetModerated(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONSETNOEXTERNALMESSAGES:
			this.onSetNoExternalMessages(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONSETSECRET:
			this.onSetSecret(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONSETTOPICPROTECTION:
			this.onSetTopicProtection(e.getChannel(), e.getUser());
			break;
		case CHANNEL_ONTOPIC:
			this.onTopic(e.getChannel(), e.getString1(), e.getString1(), e.getLong1(), e.getBool1());
			break;
		case CHANNEL_ONUSERLIST:
			this.onUserList(e.getChannel(), e.getUsers());
			break;
		case CHANNEL_ONVOICE:
			this.onVoice(e.getChannel(), e.getUser(), e.getString1());
			break;

		default:
			Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE,
					"Unredirectable event: {0}", e);
			break;
		}
	}
	

	// -------------------------------------------------------------------------
	// Method stubs
	// -------------------------------------------------------------------------

	/** Called when an User is created */
	public void onCreateUser(User user) {
	}

	/** Called when a Channel is created. */
	public void onCreateChannel(Channel channel) {
	}

	/**
	 * @see QoreBot#onAction(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onAction(User source, String action) {
	}

	/**
	 * @see QoreBot#onNickChange(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onNickChange(User source, String oldNick, String newNick) {
	}

	/**
	 * @see QoreBot#onNotice(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onNotice(User source, String notice) {
	}

	/**
	 * @see QoreBot#onPrivateMessage(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onPrivateMessage(User source, String message) {
	}

	/**
	 * @see QoreBot#onQuit(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void onQuit(User source, String reason) {
	}

	/**
	 * @see QoreBot#onUserMode(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onUserMode(User source, String mode) {
	}

	/**
	 * @see QoreBot#onAction(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onAction(Channel channel, User sender, String action) {
	}

	/** @see QoreBot#onChannelInfo(java.lang.String, int, java.lang.String) */
	public void onChannelInfo(Channel channel, int userCount, String topic) {
	}

	/**
	 * @see QoreBot#onDeop(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onDeop(Channel channel, User source, String recipient) {
	}

	/**
	 * @see QoreBot#onDeVoice(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onDeVoice(Channel channel, User source, String recipient) {
	}

	/**
	 * @see QoreBot#onInvite(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onInvite(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onJoin(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void onJoin(Channel channel, User sender) {
	}

	/**
	 * @see QoreBot#onKick(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onKick(Channel channel, User kicker, String recipientNick,
			String reason) {
	}

	/**
	 * @see QoreBot#onMessage(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onMessage(Channel channel, User sender, String message) {
	}

	/**
	 * @see QoreBot#onMode(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onMode(Channel channel, User source, String mode) {
	}

	/**
	 * @see QoreBot#onNickChange(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onNickChange(Channel channel, User user, String oldNick,
			String newNick) {
	}

	/**
	 * @see QoreBot#onNotice(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onNotice(Channel channel, User source, String target,
			String notice) {
	}

	/**
	 * @see QoreBot#onOp(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onOp(Channel channel, User source, String recipient) {
	}

	/**
	 * @see QoreBot#onPart(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void onPart(Channel channel, User sender) {
	}

	/**
	 * @see QoreBot#onQuit(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void onQuit(Channel channel, User source, String reason) {
	}

	/**
	 * @see QoreBot#onRemoveChannelBan(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onRemoveChannelBan(Channel channel, User source, String hostmask) {
	}

	/**
	 * @see QoreBot#onRemoveChannelKey(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onRemoveChannelKey(Channel channel, User source, String key) {
	}

	/**
	 * @see QoreBot#onRemoveChannelLimit(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onRemoveChannelLimit(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onRemoveInviteOnly(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onRemoveInviteOnly(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onRemoveModerated(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onRemoveModerated(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onRemoveNoExternalMessages(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onRemoveNoExternalMessages(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onRemovePrivate(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onRemovePrivate(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onRemoveSecret(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onRemoveSecret(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onRemoveTopicProtection(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onRemoveTopicProtection(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onSetChannelBan(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onSetChannelBan(Channel channel, User source, String hostmask) {
	}

	/**
	 * @see QoreBot#onSetChannelKey(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void onSetChannelKey(Channel channel, User source, String key) {
	}

	/**
	 * @see QoreBot#onSetChannelLimit(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, int)
	 */
	public void onSetChannelLimit(Channel channel, User source, int limit) {
	}

	/**
	 * @see QoreBot#onSetInviteOnly(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onSetInviteOnly(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onSetModerated(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onSetModerated(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onSetNoExternalMessages(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onSetNoExternalMessages(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onSetPrivate(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onSetPrivate(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onSetSecret(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onSetSecret(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onSetTopicProtection(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onSetTopicProtection(Channel channel, User source) {
	}

	/**
	 * @see QoreBot#onTopic(java.lang.String, java.lang.String, java.lang.String,
	 *      long, boolean)
	 */
	public void onTopic(Channel channel, String topic, String setBy, long date,
			boolean changed) {
	}

	/** @see QoreBot#onUserList(java.lang.String, org.jibble.pircbot.User[]) */
	public void onUserList(Channel channel, org.jibble.pircbot.User[] users) {
	}

	/**
	 * @see QoreBot#onVoice(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void onVoice(Channel channel, User source, String recipient) {
	}

}
