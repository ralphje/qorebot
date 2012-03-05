package command;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.Colors;
import qorebot.*;
import qorebot.plugins.Plugin;

/**
 * A command is something like a plugin, with the main difference that it works
 * on a direct or channel message. It is linked to the bot with the
 * CommandPlugin.
 * 
 * @author Ralph Broenink
 */
public abstract class Command {
	public static final String PREFIX = "!";

	private Plugin plugin;
	private int id;
	private boolean autoregisterChannels = false;
	private boolean autoregisterUsers = false;
	private String name = null;

	/**
	 * Initializes the command.
	 * 
	 * @param plugin
	 *            The plugin that is the owner of this Command. Note that this
	 *            should be a CommandPlugin, as an invocation to parseArguments
	 *            assumes this is a CommandPlugin.
	 * @param id
	 *            The id of the command
	 * @param name
	 *            The name of the command
	 * @param autoregisterChannels
	 *            True when the command should autoregister with new channels
	 * @param autoregisterUsers
	 *            True when the command should autoregister with new users
	 */
	public void init(Plugin plugin, int id, String name,
			boolean autoregisterChannels, boolean autoregisterUsers) {
		this.plugin = plugin;
		this.id = id;
		this.name = name;
		this.autoregisterChannels = autoregisterChannels;
		this.autoregisterUsers = autoregisterUsers;
	}

	/**
	 * Sets whether this command should autoregister with new channels.
	 * 
	 * @param autoregister
	 *            The new value
	 */
	public void setAutoregisterChannels(boolean autoregister) {
		this.autoregisterChannels = autoregister;

		if (this.id > 0) {
			PreparedStatement st = Database
					.gps("UPDATE commands SET autoregister_channels = ? WHERE id = ?");
			if (st == null)
				return;
			try {
				st.setBoolean(1, autoregister);
				st.setInt(2, this.id);
				st.executeUpdate();
			} catch (SQLException ex) {
				Logger.getLogger(Command.class.getName()).log(Level.SEVERE,
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
	 * Sets whether this command should autoregister with new users.
	 * 
	 * @param autoregister
	 *            The new value
	 */
	public void setAutoregisterUsers(boolean autoregister) {
		this.autoregisterUsers = autoregister;

		if (this.id > 0) {
			PreparedStatement st = Database
					.gps("UPDATE commands SET autoregister_users = ? WHERE id = ?");
			if (st == null)
				return;
			try {
				st.setBoolean(1, autoregister);
				st.setInt(2, this.id);
				st.executeUpdate();
			} catch (SQLException ex) {
				Logger.getLogger(Command.class.getName()).log(Level.SEVERE,
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
	 * Checks whether this command should autoregister with new channels.
	 */
	public boolean isAutoregisterChannels() {
		return this.autoregisterChannels;
	}

	/**
	 * Checks whether this command should autoregister with new users.
	 */
	public boolean isAutoregisterUsers() {
		return this.autoregisterUsers;
	}

	/**
	 * Returns the bot this command is loaded for.
	 */
	public final Plugin getPlugin() {
		return this.plugin;
	}

	/**
	 * Returns the id of the command.
	 */
	public final int getId() {
		return this.id;
	}

	/**
	 * Returns the name of the command.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Receives a message from an user.
	 * 
	 * @param user
	 *            The user who sent the message
	 * @param msg
	 *            The message.
	 * @see #receive(qorebot.Channel, qorebot.User, command.CommandMessage)
	 */
	public final String receive(User user, CommandMessage msg) {
		return this.receive(null, user, msg);
	}

	/**
	 * Receives a message via a channel. Should not be overriden by any command.
	 * 
	 * @param channel
	 *            The channel the message was sent to
	 * @param user
	 *            The user who sent the message
	 * @param msg
	 *            The message
	 */
	public String receive(Channel channel, User user, CommandMessage msg) {
		return this.handleMessage(channel, user, msg);
	}

	/**
	 * Does something with the message.
	 * 
	 * @param channel
	 *            The channel the message was sent to. Might be null.
	 * @param user
	 *            The sender of the message
	 * @param msg
	 *            The message itself
	 * @return The result value, or null if none
	 */
	public abstract String handleMessage(Channel channel, User user,
			CommandMessage msg);

	/**
	 * Takes a CommandMessage and parses all subcommands and strings into one
	 * list of strings. As commands may return null, the returned list may
	 * contain null values.
	 * 
	 * Since the Command is unable to get access to the owning plugin since this
	 * is a plugin loaded by the PluginLoader,
	 * {@link plugins.CommandPlugin#parseMessage(qorebot.Channel, qorebot.User, command.CommandMessage)}
	 * is executed directly on {@link #getPlugin()}. This may cause weird
	 * problems, but is the only way to perform the action.
	 * 
	 * @param channel
	 *            The channel the CommandMessage was sent through
	 * @param user
	 *            The sender of the message
	 * @param msg
	 *            The message
	 * @return A list of Strings containing the results of all commandmessages.
	 */
	public List<String> parseArguments(Channel channel, User user,
			CommandMessage msg) {
		ArrayList<String> result = new ArrayList<String>();
		for (Message m : msg.getMessages()) {
			if (m instanceof StringMessage) {
				result.add(((StringMessage) m).getMessage());
			} else {
				String res = null;
				try {
					// since we can't retrieve the CommandPlugin (due to layer
					// separation)
					// we should call parseMessage this weird way.
					Object o = this.plugin
							.getClass()
							.getMethod("parseMessage", Channel.class,
									User.class, CommandMessage.class)
							.invoke(this.plugin, channel, user, m);
					if (o != null)
						res = (String) o;
				} catch (NoSuchMethodException ex) {
					Logger.getLogger(Command.class.getName()).log(Level.SEVERE,
							null, ex);
				} catch (SecurityException ex) {
					Logger.getLogger(Command.class.getName()).log(Level.SEVERE,
							null, ex);
				} catch (IllegalAccessException ex) {
					Logger.getLogger(Command.class.getName()).log(Level.SEVERE,
							null, ex);
				} catch (IllegalArgumentException ex) {
					Logger.getLogger(Command.class.getName()).log(Level.SEVERE,
							null, ex);
				} catch (InvocationTargetException ex) {
					Logger.getLogger(Command.class.getName()).log(Level.SEVERE,
							null, ex);
				}
				result.add(res);
			}
		}
		return result;
	}

	/**
	 * Concatenates the given list of arguments to a single string, containing
	 * all elements between begin and end (including). For example, a call with
	 * arguments ['a', 'b', 'c', 'd'], 1, 3 will return 'b c d'.
	 * 
	 * Note that begin <= end. If begin > end, nothing is returned. If begin is
	 * larger then the size of the argument list, an empty string is returned.
	 * If begin == end, the beginth element is returned.
	 * 
	 * @param arguments
	 *            The list to concatenate with
	 * @param begin
	 *            The first id of the element to concatenate
	 * @param end
	 *            The ending id of the element to concatenate. If this is larger
	 *            then the size of the argument list, then the last element is
	 *            assumed.
	 */
	public static String getArgumentConcat(List<String> arguments, int begin,
			int end) {
		String concat = "";
		for (int i = begin; i <= end && i < arguments.size(); i++) {
			concat = concat + " " + arguments.get(i);
		}

		// Trim off the first space
		if (concat.length() > 0)
			concat = concat.substring(1);

		return concat;
	}

	/**
	 * Similar to getArgumentConcat, but assuming that all arguments after begin
	 * should be concatenated.
	 * 
	 * @param arguments
	 *            The argument list
	 * @param begin
	 *            The first id
	 */
	public static String getArgumentConcat(List<String> arguments, int begin) {
		return Command
				.getArgumentConcat(arguments, begin, arguments.size() - 1);
	}

	/**
	 * Sends a message to the channel if this is not null, or to the user
	 * otherwise.
	 * 
	 * @param channel
	 *            The channel to check for
	 * @param user
	 *            The user to send the message to
	 * @param message
	 *            The message to send.
	 */
	public static void sendMessage(Channel channel, User user, String message) {
		if (channel == null)
			user.sendMessage(message);
		else
			channel.sendMessage(message);
	}

	/**
	 * Sends a message to the channel if this is not null, or to the user
	 * otherwise.
	 * 
	 * @param channel
	 *            The channel to check for
	 * @param user
	 *            The user to send the message to
	 * @param message
	 *            The message to send.
	 * @param personal
	 *            If true, the user is personally addressed in a channel
	 */
	public static void sendMessage(Channel channel, User user, String message,
			boolean personal) {
		if (channel == null)
			user.sendMessage(message);
		else if (personal)
			channel.sendMessage(user.getNickname() + ": " + message);
		else
			channel.sendMessage(message);
	}

	/**
	 * Sends a error message to the channel if this is not null, or to the user
	 * otherwise.
	 * 
	 * @param channel
	 *            The channel to check for
	 * @param user
	 *            The user to send the message to
	 * @param message
	 *            The message to send.
	 */
	public static void sendErrorMessage(Channel channel, User user,
			String message) {
		if (channel == null)
			user.sendMessage(Colors.BOLD + Colors.RED + message);
		else
			channel.sendMessage(Colors.BOLD + Colors.RED + message);
	}

	/**
	 * Sends a error message to the channel if this is not null, or to the user
	 * otherwise.
	 * 
	 * @param channel
	 *            The channel to check for
	 * @param user
	 *            The user to send the message to
	 * @param message
	 *            The message to send.
	 * @param personal
	 *            If true, the user is personally addressed in a channel
	 */
	public static void sendErrorMessage(Channel channel, User user,
			String message, boolean personal) {
		if (channel == null)
			user.sendMessage(message);
		else if (personal)
			channel.sendMessage(Colors.BOLD + Colors.RED + user.getNickname()
					+ ": " + message);
		else
			channel.sendMessage(Colors.BOLD + Colors.RED + message);
	}

	/**
	 * Checks whether the given user has the required privilege level. If not,
	 * it will send the user a message telling that the operation failed.
	 * 
	 * @param action
	 *            The action trying to perform (to be pasted after 'for')
	 * @param requiredLevel
	 *            The required user level
	 * @param channel
	 *            The channel
	 * @param user
	 *            The user
	 * @return True if the user as sufficient permissions.
	 */
	public static boolean checkPermissions(String action, UserLevel requiredLevel,
			Channel channel, User user) {
		UserLevel actualLevel = UserLevel.UNKNOWN;
		if (channel == null) {
			actualLevel = user.getLevel();
		} else {
			actualLevel = user.getLevel(channel);
		}
		if (actualLevel.compareTo(requiredLevel) < 0) {
			String message = "You don't have the required permisson for "
					+ action + ". Your level is '"
					+ actualLevel.toString() + "', " + "but '"
					+ requiredLevel.toString() + "' is required.";

			Command.sendErrorMessage(channel, user, message, true);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks whether the given command is in the input.
	 * 
	 * @param command
	 *            The command to be checked for
	 * @param input
	 *            The (first) argument to check whether it is the given command
	 * @return True if the input equals the command or the command prefixed with
	 *         the default prefix.
	 */
	public static boolean isCommand(String input, String command) {
		return input.equals(command) || input.equals(Command.PREFIX + command);
	}
}
