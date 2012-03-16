package qorebot;

import qorebot.plugins.Plugin;
import qorebot.plugins.Pluginable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a channel the bot is connected to.
 * 
 * @author Ralph Broenink
 */
public class Channel extends Pluginable {
	private QoreBot bot;

	private int id;
	private String name;
	private String key;

	/**
	 * Creates the channel, but doesn't join it.
	 * 
	 * @param bot
	 *            The bot working on this channel. Should not be null.
	 * @param id
	 *            An unique integer identifying the channel.
	 * @param name
	 *            The name of the channel. Should not be null.
	 */
	public Channel(QoreBot bot, int id, String name) {
		this(bot, id, name, null);
	}

	/**
	 * Creates the channel, but doesn't join it.
	 * 
	 * @param bot
	 *            The bot working on this channel. Should not be null.
	 * @param id
	 *            An unique integer identifying the channel.
	 * @param name
	 *            The name of the channel. Should not be null.
	 * @param key
	 *            The key for the channel. May be null.
	 */
	public Channel(QoreBot bot, int id, String name, String key) {
		this.bot = bot;
		this.id = id;
		this.name = name;
		this.key = key;

		this.bot.registerPlugins(this);
		for (Plugin p : this.bot.getPlugins())
			p.receive(new Event(EventType.PLUGIN_ONCREATECHANNEL, this));
	}

	/**
	 * Retrieves the current bot.
	 */
	public QoreBot getBot() {
		return this.bot;
	}

	/**
	 * Retrieves the channel name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Retrieves the current id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Retrieves the current key
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Changes the current key.
	 * 
	 * @param key
	 *            The new key for this channel. May be null to unset.
	 */
	public void setKey(String key) {
		this.key = key;

		PreparedStatement st = Database.gps("UPDATE channels SET key = ? WHERE id = ?");
		try {
			if (st != null) {
				st.setString(1, key);
				st.setInt(1, this.getId());
				st.executeUpdate();
			}
		} catch (SQLException ex) {
			Logger.getLogger(Channel.class.getName()).log(Level.SEVERE,
					"Failed to update channel key", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex) {
			}
		}
	}

	/**
	 * Checks whether the bot is an op in this channel.
	 * 
	 * @return True if the current bot is op in the channel.
	 */
	public boolean isOp() {
		return this.isOp(this.bot.getNick());
	}

	/**
	 * Checks whether the given User is op in this channel.
	 * 
	 * @param user
	 *            The user to check for.
	 * @return True if the given user is op in this channel
	 */
	public boolean isOp(User user) {
		return this.isOp(user.getNickname());
	}

	/**
	 * Checks whether the given username is op in this channel.
	 * 
	 * @param username
	 *            The username to check for.
	 * @return True if the given user is op in this channel
	 */
	public boolean isOp(String username) {
		org.jibble.pircbot.User[] users = this.bot.getUsers(this.name);
		for (int i = 0; i < users.length; i++) {
			if (users[i].getNick().equals(username)) {
				return users[i].isOp();
			}
		}
		return false;
	}

	/**
	 * Checks whether the bot has voice in this channel.
	 * 
	 * @return True if the current bot has voice in the channel.
	 */
	public boolean hasVoice() {
		return this.hasVoice(this.bot.getNick());
	}

	/**
	 * Checks whether the given User has voice in this channel.
	 * 
	 * @param user
	 *            The user to check for.
	 * @return True if the given user has voice in this channel
	 */
	public boolean hasVoice(User user) {
		return this.hasVoice(user.getNickname());
	}

	/**
	 * Checks whether the given username has voice in this channel.
	 * 
	 * @param username
	 *            The username to check for.
	 * @return True if the given user has voice in this channel
	 */
	public boolean hasVoice(String username) {
		org.jibble.pircbot.User[] users = this.bot.getUsers(this.name);
		for (int i = 0; i < users.length; i++) {
			if (users[i].getNick().equals(username)) {
				return users[i].hasVoice();
			}
		}
		return false;
	}

	/**
	 * Joins this channel. Uses a key if provided one before.
	 */
	public void join() {
		if (this.key == null)
			this.bot.joinChannel(this.name);
		else
			this.bot.joinChannel(this.name, this.key);
	}

	/**
	 * Joins this channel with a given key.
	 * 
	 * @param key
	 *            The key of the channel. Should not be null.
	 */
	public void join(String key) {
		this.bot.joinChannel(this.name, key);
	}

	/**
	 * Leave the channel
	 */
	public void part() {
		this.bot.partChannel(this.name);
	}

	/**
	 * Leave the channel with a reason
	 * 
	 * @param reason
	 *            The reason to leave. Should not be null.
	 */
	public void part(String reason) {
		this.bot.partChannel(this.name, reason);
	}

	/**
	 * Bans a hostmask from this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#ban(java.lang.String, java.lang.String)
	 */
	public void ban(String hostmask) {
		this.bot.ban(this.name, hostmask);
	}

	/**
	 * Unbans a user from this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#unBan(java.lang.String, java.lang.String)
	 */
	public void unBan(String hostmask) {
		this.bot.unBan(this.name, hostmask);
	}

	/**
	 * Grants operator privilidges to a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#op(java.lang.String, java.lang.String)
	 */
	public void op(String nick) {
		this.bot.op(this.name, nick);
	}

	/**
	 * Grants operator privilidges to a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#op(java.lang.String, java.lang.String)
	 */
	public void op(User user) {
		this.op(user.getNickname());
	}

	/**
	 * Removes operator priviliges from a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#deOp(java.lang.String, java.lang.String)
	 */
	public void deOp(String nick) {
		this.bot.deOp(this.name, nick);
	}

	/**
	 * Removes operator priviliges from a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#deOp(java.lang.String, java.lang.String)
	 */
	public void deOp(User user) {
		this.deOp(user.getNickname());
	}

	/**
	 * Grants voice privileges to a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#voice(java.lang.String, java.lang.String)
	 */
	public void voice(String nick) {
		this.bot.voice(this.name, nick);
	}

	/**
	 * Grants voice privileges to a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#voice(java.lang.String, java.lang.String)
	 */
	public void voice(User user) {
		this.voice(user.getNickname());
	}

	/**
	 * Removes voice privilidges from a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#deVoice(java.lang.String,
	 *      java.lang.String)
	 */
	public void deVoice(String nick) {
		this.bot.deVoice(this.name, nick);
	}

	/**
	 * Removes voice privilidges from a user on this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#deVoice(java.lang.String,
	 *      java.lang.String)
	 */
	public void deVoice(User user) {
		this.deVoice(user.getNickname());
	}

	/**
	 * Returns an array of all users in this Channel, giving a reason.
	 * 
	 * @see org.jibble.pircbot.PircBot#getUsers(java.lang.String)
	 */
	public org.jibble.pircbot.User[] getUsers() {
		return this.bot.getUsers(this.name);
	}

	/**
	 * Kicks a user from this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#kick(java.lang.String, java.lang.String)
	 */
	public void kick(String nick) {
		this.bot.kick(this.name, nick);
	}

	/**
	 * Kicks a user from this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#kick(java.lang.String, java.lang.String)
	 */
	public void kick(User user) {
		this.kick(user.getNickname());
	}

	/**
	 * Kicks a user from this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#kick(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void kick(String nick, String reason) {
		this.bot.kick(this.name, nick, reason);
	}

	/**
	 * Kicks a user from this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#kick(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void kick(User user, String reason) {
		this.kick(user.getNickname(), reason);
	}

	/**
	 * Sends an action to this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendAction(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendAction(String action) {
		this.bot.sendAction(this.name, action);
	}

	/**
	 * Sends a CTCP command to this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendCTCPCommand(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendCTCPCommand(String command) {
		this.bot.sendCTCPCommand(this.name, command);
	}

	/**
	 * Sends an invitation to join this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendInvite(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendInvite(String nick) {
		this.bot.sendInvite(nick, this.name);
	}

	/**
	 * Sends an invitation to join this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendInvite(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendInvite(User user) {
		this.bot.sendInvite(user.getNickname(), this.name);
	}

	/**
	 * Sends a message to this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendMessage(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendMessage(String message) {
		this.bot.sendMessage(this.name, message);
	}

	/**
	 * Sends a notice to this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendNotice(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendNotice(String notice) {
		this.bot.sendNotice(this.name, notice);
	}

	/**
	 * Sets the mode of this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#setMode(java.lang.String,
	 *      java.lang.String)
	 */
	public void setMode(String mode) {
		this.bot.setMode(this.name, mode);
	}

	/**
	 * Set the topic for this Channel.
	 * 
	 * @see org.jibble.pircbot.PircBot#setTopic(java.lang.String,
	 *      java.lang.String)
	 */
	public void setTopic(String topic) {
		this.bot.setTopic(this.name, topic);
	}

	/**
	 * Receives an event and handles it by passing it to the plugins.
	 * 
	 * @param e
	 *            The event to handle
	 */
	public void receive(Event e) {
		this.update(e);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Channel
				&& ((Channel) o).getName().equals(this.getName()) && ((Channel) o).getBot() == this.getBot());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 11 * hash + (this.bot != null ? this.bot.hashCode() : 0);
		hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}
}
