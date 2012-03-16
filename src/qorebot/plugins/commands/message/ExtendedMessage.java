package qorebot.plugins.commands.message;


import qorebot.Channel;
import qorebot.User;

/**
 * An extended message is nothing more then a CommandMessage with information
 * like the sending user and channel.
 * 
 * @author Ralph Broenink
 */
public class ExtendedMessage extends CommandMessage {
	private Channel channel;
	private User user;
	private CommandMessage msg;

	/**
	 * Creates a new extended message.
	 * 
	 * @param channel
	 *            The channel the message was sent through, if any, or null
	 * @param user
	 *            The user the message was sent by
	 * @param msg
	 *            The Message that was sent
	 */
	public ExtendedMessage(Channel channel, User user, CommandMessage msg) {
		super(null);
		this.channel = channel;
		this.user = user;
		this.msg = msg;
	}

	/**
	 * Retrieves the channel
	 */
	public Channel getChannel() {
		return this.channel;
	}

	/**
	 * Retrieves the user
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Retrieves the message
	 */
	public CommandMessage getMessage() {
		return this.msg;
	}
}
