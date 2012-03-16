package qorebot;

import qorebot.plugins.Plugin;
import qorebot.plugins.Pluginable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Crypt;

/**
 * Represents an user
 * 
 * @author Ralph Broenink
 */
public class User extends Pluginable {

	private QoreBot bot;

	private int id = 0;
	private String username = null;
	private UserLevel minLevel = UserLevel.UNKNOWN;
	private String uniqueId = null;
	private boolean identified = false;

	/**
	 * Creates a new user based on the unique identifier of this IRC user. When
	 * an user is identified with this id, then it will be identified as the
	 * user with the (minimum) level in the database.
	 * 
	 * When the user could not be identified, the user will be created with user
	 * level LEVEL_NONE.
	 * 
	 * When something goes wrong, the user will have LEVEL_UNKNOWN.
	 * 
	 * @param bot
	 *            The bot the user is linked to
	 * @param uniqueId
	 *            The generated unique IRC id.
	 */
	public User(QoreBot bot, String uniqueId) {
		this.bot = bot;
		this.uniqueId = uniqueId;
		this.identified = false;
		this.username = null;
		this.id = 0;

		PreparedStatement st = Database.gps("SELECT id, username, level FROM users WHERE last_unique_id = ? LIMIT 0,1");

		if (st == null) {
			this.minLevel = UserLevel.UNKNOWN;
		} else {
			try {
				st.setString(1, uniqueId);
				ResultSet result = st.executeQuery();
				if (result.next()) {
					this.id = result.getInt("id");
					this.minLevel = UserLevel.fromInteger(result.getInt("level"));
					this.username = result.getString("username");
					this.identified = true;
				} else {
					this.minLevel = UserLevel.NONE;
				}
			} catch (SQLException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE,
						"Failed to user information for " + uniqueId, ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}

		this.bot.registerPlugins(this);
		for (Plugin p : this.bot.getPlugins())
			p.receive(new Event(EventType.PLUGIN_ONCREATEUSER, this));
	}

	/**
	 * Creates an identified user from the given username, but this user should
	 * be disposed as soon as we're done with it -- it has no real use, except
	 * for some management commands. Its use is, for that reason, deprecated.
	 * 
	 * Several other warnings are not misplaced: this user is identified, but it
	 * is NOT identified based on the !identify-command nor the last known
	 * unique ID. Therefore its IRC-like commands can't be used either.
	 * 
	 * So, any user created with this constructor DOES NOT EXIST.
	 * 
	 * @deprecated Use the objects that can be found in the bot - those are
	 *             linked to something.
	 * @param username
	 *            The user id to identify.
	 */
	public User(String username) {
		this.uniqueId = null;
		this.identified = false;
		this.username = username;
		this.id = 0;

		PreparedStatement st = Database.gps("SELECT id, level FROM users WHERE username = ? LIMIT 0,1");
		if (st == null) {
			this.minLevel = UserLevel.UNKNOWN;
			this.username = null;
		} else {
			try {
				st.setString(1, username);
				ResultSet result = st.executeQuery();

				if (result.next()) {
					this.minLevel = UserLevel.fromInteger(result.getInt("level"));
					this.id = result.getInt("id");
					this.identified = true;
				} else {
					this.minLevel = UserLevel.NONE;
					this.username = null;
				}
			} catch (SQLException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE,
						"Failed to user information for " + username, ex);
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
	}

	/**
	 * Retrieves the current bot.
	 */
	public QoreBot getBot() {
		return this.bot;
	}

	/**
	 * Returns whether the current user is identified (and therefore the id and
	 * username have correct values)
	 */
	public boolean isIdentified() {
		return this.identified;
	}

	/**
	 * Tries to identify the user with given username and password.
	 * 
	 * @param username
	 *            The username to identify for.
	 * @param password
	 *            The password to identify with.
	 * @return True if the user was successfully identified. Please note that a
	 *         result of false does not mean that the user is not identified.
	 */
	public boolean identify(String username, String password) {
		boolean success = false;
		PreparedStatement st = Database.gps("SELECT id, username, level FROM users WHERE username = ? AND password = ? LIMIT 0,1");

		if (st == null) {
			this.unidentify();
		} else {
			try {
				st.setString(1, username);
				st.setString(2, Crypt.generateMD5(password));
				ResultSet result = st.executeQuery();
				if (result.next()) {
					this.id = result.getInt("id");
					this.minLevel = UserLevel.fromInteger(result.getInt("level"));
					this.username = result.getString("username");
					this.identified = true;
					success = true;
					this.setUniqueId(uniqueId);

					this.getBot().registerPlugins(this);
					for (Plugin p : this.bot.getPlugins())
						p.receive(new Event(EventType.PLUGIN_ONCREATEUSER, this));

				} // else: invalid password -> don't do anything
			} catch (SQLException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE,
						"Failed to user information for " + uniqueId, ex);
				this.unidentify();
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
		return success;
	}

	/**
	 * Unidentifies the current user. Only useful for testing purposes.
	 */
	public void unidentify() {
		this.identified = false;
		this.username = null;
		this.id = 0;
		this.minLevel = UserLevel.NONE;

		this.getBot().registerPlugins(this);
		for (Plugin p : this.bot.getPlugins())
			p.receive(new Event(EventType.PLUGIN_ONCREATEUSER, this));

		User.clearLastUniqueId(this.uniqueId);
	}

	/**
	 * Checks whether the given password is valid for the current user.
	 * 
	 * @param password
	 *            The password to check
	 * @return True if the password is valid.
	 */
	public boolean checkPassword(String password) {
		if (!this.isIdentified())
			return false;

		boolean valid = false;

		PreparedStatement st = Database.gps("SELECT id FROM users WHERE username = ? AND password = ? LIMIT 0,1");

		if (st != null) {
			try {
				st.setString(1, this.username);
				st.setString(2, Crypt.generateMD5(password));
				ResultSet result = st.executeQuery();
				if (result.next()) {
					valid = true;
				} // else: invalid password -> don't do anything
			} catch (SQLException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE,
						"Failed to change password", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
		return valid;
	}

	/**
	 * Changes the password for the current user.
	 * 
	 * @param oldPassword
	 *            The current password
	 * @param newPassword
	 *            The new password
	 * @return False if either the connection failed, the old password is
	 *         invalid or the current user is not identified.
	 */
	public boolean changePassword(String oldPassword, String newPassword) {
		boolean success = false;
		if (this.isIdentified() && this.checkPassword(oldPassword)) {
			PreparedStatement st = Database.gps("UPDATE users SET password = ? WHERE id = ?");
			if (st == null)
				return false;

			try {
				st.setString(1, Crypt.generateMD5(newPassword));
				st.setInt(2, this.id);
				st.executeUpdate();
				success = true;
			} catch (SQLException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE,
						"Failed to change password.", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
		return success;
	}

	/**
	 * Registers the current user. The method doesn't check consistency, as this
	 * is done in the database. This might cause some non-errors to appear in
	 * the log file.
	 * 
	 * The checks should be something along the lines of unique usernames,
	 * unique last IDs and non-empty passwords (that last one is not done
	 * either).
	 * 
	 * This method does NOT identify the user.
	 * 
	 * @param username
	 *            The username to register with
	 * @param password
	 *            The password to register with
	 * @return True if registration was succesful.
	 */
	public boolean register(String username, String password) {
		boolean success = false;
		if (!this.isIdentified()) {
			PreparedStatement st = Database.gps("INSERT INTO users(username,password,level,last_unique_id) VALUES(?,?,?,?)");
			if (st == null)
				return false;

			try {
				st.setString(1, username);
				st.setString(2, Crypt.generateMD5(password));
				st.setInt(3, UserLevel.IDENTIFIED.toInteger());
				st.setString(4, this.uniqueId);
				st.executeUpdate();
				success = true;
				// TODO Fails are handled ugly.
			} catch (SQLException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE,
						"Failed to register user.", ex);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
		return success;
	}

	/**
	 * Returns the current user database id.
	 * 
	 * @return The id when the user is identified, or 0 when not identified
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Creates an (unique) identifier for an user based on its nickname, login
	 * and hostname.
	 * 
	 * @param nick
	 *            The nickname of the user
	 * @param login
	 *            The login name of the user
	 * @param hostname
	 *            The hostname of the user
	 * @return nick!login@hostname
	 */
	public static String createUniqueId(String nick, String login, String hostname) {
		return nick + "!" + login + "@" + hostname;
	}

	/**
	 * Checks whether the identifier has the form of a unique identifier.
	 * 
	 * @param identifier
	 *            The identifier to check.
	 * @return true if the identifier contains an @ and an !
	 */
	public static boolean isUniqueId(String identifier) {
		return identifier.contains("@") && identifier.contains("!");
	}

	/**
	 * Retrieves the current user's unique IRC identifier.
	 * 
	 * @return the unique id or null if not linked to an irc connection
	 */
	public String getUniqueId() {
		return this.uniqueId;
	}

	/**
	 * Changes the unique id of the current user. Should not be called unless
	 * there's some good reason for.
	 * 
	 * @param uniqueId
	 *            The new unique identifier
	 * @return true if the change succeeded.
	 */
	public boolean setUniqueId(String uniqueId) {
		return this.setUniqueId(uniqueId, true);
	}

	/**
	 * Changes the unique id of the current user. Should not be called unless
	 * there's some good reason for.
	 * 
	 * @param uniqueId
	 *            The new unique identifier
	 * @param updateLastUniqueId
	 *            If true, tries to update the database field with the last
	 *            known unique id of the identified curr. user.
	 * @return true if the change succeeded.
	 */
	public boolean setUniqueId(String uniqueId, boolean updateLastUniqueId) {
		String prevUniqueId = this.uniqueId;
		this.uniqueId = uniqueId;

		if (this.isIdentified() && updateLastUniqueId) {
			User.clearLastUniqueId(uniqueId);
			User.clearLastUniqueId(prevUniqueId);

			PreparedStatement st = Database.gps("UPDATE users SET last_unique_id = ? WHERE id = ?");
			if (st == null)
				return false;

			try {
				st.setString(1, uniqueId);
				st.setInt(2, this.id);
				st.executeUpdate();

				return true;
			} catch (SQLException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE,
						"Failed to update last unique id.", ex);
				return false;
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		} else {
			return true;
		}
	}

	/**
	 * Clears the given uniqueId from the database. This is useful for some
	 * operations that set the uniqueId or in cases when the unique ID should
	 * not be set.
	 * 
	 * @param uniqueId
	 *            The uniqueId to clear
	 * @return True if the removal was successful
	 */
	public static boolean clearLastUniqueId(String uniqueId) {
		PreparedStatement st = Database.gps("UPDATE users SET last_unique_id = NULL WHERE last_unique_id = ?");
		if (st == null)
			return false;

		try {
			st.setString(1, uniqueId);
			st.executeUpdate();

			return true;
		} catch (SQLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE,
					"Failed to clear last unique id.", ex);
			return false;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
	}

	/**
	 * Retrieves the current user's nickname, based on its unique identifier
	 * 
	 * @return the username or null if not linked to an irc connection
	 */
	public String getNickname() {
		if (this.uniqueId == null)
			return null;
		String[] user = this.uniqueId.split("\\!");
		return user[0];
	}

	/**
	 * Retrieves the current user's minimum privilege level.
	 */
	public UserLevel getLevel() {
		return this.minLevel;
	}

	/**
	 * Retrieves the current user's username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Retrieves the user level for this channel. If the user level is unknown
	 * or lower then the user minimum, the user minimum is returned.
	 * 
	 * @param channel
	 *            The channel to retrieve the information for.
	 * @return The user level for this channel.
	 */
	public UserLevel getLevel(Channel channel) {
		UserLevel level = this.getLevel();

		if (!this.isIdentified() || channel == null)
			return level;

		PreparedStatement st = Database.gps("SELECT level FROM channels_users WHERE channel_id = ? AND user_id = ? LIMIT 0,1");

		if (st == null)
			return UserLevel.UNKNOWN;

		try {
			st.setInt(1, channel.getId());
			st.setInt(2, this.getId());
			ResultSet result = st.executeQuery();
			if (result.next()) {
				UserLevel channelLevel = UserLevel.fromInteger(result.getInt("level"));
				level = UserLevel.max(level, channelLevel);
			}
		} catch (SQLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE,
					"Failed to retrieve user level.", ex);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
		return level;

	}

	/**
	 * Updates the minimum user level.
	 * 
	 * @param level
	 *            The new user level. Should be a constant.
	 * @return true when the update succeeds
	 */
	public boolean setLevel(UserLevel level) {
		if (!this.isIdentified())
			return false;

		PreparedStatement st = Database.gps("UPDATE users SET level = ? WHERE id = ?");
		if (st == null)
			return false;

		try {
			st.setInt(1, level.toInteger());
			st.setInt(2, this.id);
			st.executeUpdate();

			this.minLevel = level;
			return true;
		} catch (SQLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE,
					"Failed to update user level.", ex);
			return false;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
	}

	/**
	 * Updates the channel level of a user.
	 * 
	 * @param channel
	 *            The channel to set the data for. Should not be null.
	 * @param level
	 *            The new user level. Should be one of the constants.
	 * @return true when the update succeeds
	 */
	public boolean setLevel(Channel channel, UserLevel level) {
		if (!this.isIdentified())
			return false;

		PreparedStatement st = Database.gps("REPLACE INTO channels_users SET channel_id = ?, user_id = ?, level = ?");
		if (st == null)
			return false;

		try {
			st.setInt(1, channel.getId());
			st.setInt(2, this.getId());
			st.setInt(3, level.toInteger());
			st.executeUpdate();

			return true;
		} catch (SQLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE,
					"Failed to update user channel level.", ex);
			return false;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
	}

	/**
	 * Removes a channel level from the database.
	 * 
	 * @param channel
	 *            The channel to set the data for. Should not be null.
	 * @return true when the removal succeeds
	 */
	public boolean removeLevel(Channel channel) {
		if (!this.isIdentified())
			return false;

		PreparedStatement st = Database.gps("DELETE FROM channels_users WHERE channel_id = ? AND user_id = ?");
		if (st == null)
			return false;

		try {
			st.setInt(1, channel.getId());
			st.setInt(2, this.getId());
			st.executeUpdate();

			return true;
		} catch (SQLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE,
					"Failed to remove user channel level.", ex);
			return false;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException ex1) {
			}
		}
	}

	/**
	 * Checks whether this user has the given level (at least).
	 * 
	 * @param level
	 *            The required level
	 * @return True if this.getLevel() gte level
	 */
	public boolean hasLevel(UserLevel level) {
		return this.getLevel().compareTo(level) >= 0;
	}

	/**
	 * Checks whether this user has the given level at the given channel (at
	 * least)
	 * 
	 * @param level
	 *            The required level
	 * @param channel
	 *            The channel to check for
	 * @return True if this.getLevel(channel) gte level
	 */
	public boolean hasLevel(UserLevel level, Channel channel) {
		return this.getLevel(channel).compareTo(level) >= 0;
	}

	/**
	 * Tries to determine the level from a given string. The string can be
	 * either a number representing the integer value of the level, or the
	 * string representation returned by {@link UserLevel#toString()}
	 * 
	 * @param level
	 *            The string representation to convert. Must not be null.
	 * @return The level number if determined, or User.LEVEL_UNKOWN if none
	 *         found.
	 */
	public static UserLevel fromLevelString(String level) {
		try {
			int levInt = Integer.parseInt(level);
			UserLevel ul = UserLevel.fromInteger(levInt);
			if (ul != null)
				return ul;
			else
				return UserLevel.UNKNOWN;

		} catch (NumberFormatException nfe) {
		} // fall through

		try {
			UserLevel ul = UserLevel.valueOf(level.toUpperCase());
			return ul;
		} catch (IllegalArgumentException e) {
			return UserLevel.UNKNOWN;
		}
	}

	/**
	 * Sends an action to this User.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendAction(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendAction(String action) {
		this.bot.sendAction(this.getNickname(), action);
	}

	/**
	 * Sends a CTCP command to this User.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendCTCPCommand(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendCTCPCommand(String command) {
		this.bot.sendCTCPCommand(this.getNickname(), command);
	}

	/**
	 * Sends a message to this User.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendMessage(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendMessage(String message) {
		this.bot.sendMessage(this.getNickname(), message);
	}

	/**
	 * Sends a notice to this User.
	 * 
	 * @see org.jibble.pircbot.PircBot#sendNotice(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendNotice(String notice) {
		this.bot.sendNotice(this.getNickname(), notice);
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
}