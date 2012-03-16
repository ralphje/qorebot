package qorebot.plugins.commands;

import java.util.List;


import qorebot.Channel;
import qorebot.User;
import qorebot.plugins.commands.message.CommandMessage;

/**
 * A simple I/O command. Given a CommandMessage, this Command first parses all
 * arguments and then does something with it.
 * 
 * You should keep in mind that every IOCommand is handled in the same thread as
 * the receiving thread. This means that you shouldn't use this command type for
 * more complex commands. Use {@link ThreadedCommand} instead.
 * 
 * @author Ralph Broenink
 */
public abstract class IOCommand extends Command {

	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		if (this.isHandled(channel, user, msg)) {
			List<String> list = this.parseArguments(channel, user, msg);
			return this.handleMessage(channel, user, list);
		} else {
			return null;
		}
	}

	/**
	 * Takes an argument list (containing strings) and returns another string,
	 * which will be sent to any implementing command.
	 * 
	 * @param channel
	 *            The channel the message was received on
	 * @param user
	 *            The user the message was sent by
	 * @param msg
	 *            The message. The list may contain null values.
	 * @return A string result or null if no result
	 */
	public abstract String handleMessage(Channel channel, User user, List<String> msg);

	/**
	 * Returns true if the given message is parsed by this command. If this
	 * returns false, handleMessage won't be executed.
	 * 
	 * @param channel
	 *            The channel the message was received on. May be null.
	 * @param user
	 *            The user who sent the message. May not be null.
	 * @param msg
	 *            The sent message. May not be null.
	 * @return True if the given message should be parsed by this command
	 */
	public abstract boolean isHandled(Channel channel, User user, CommandMessage msg);

}
