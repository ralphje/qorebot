package qorebot.plugins.commands.message;


/**
 * Representation of a message. A message can be either a command (containing
 * messages) or a string.
 * 
 * @author Ralph Broenink
 */
public abstract class Message {
	private CommandMessage parent = null;

	/**
	 * Creates the message with a set parent.
	 * 
	 * @param parent
	 *            The parent of this message, or null.
	 */
	public Message(CommandMessage parent) {
		this.parent = parent;
	}

	/**
	 * Retrieves the parent message.
	 * 
	 * @return The parent message or null if it doesn't exist.
	 */
	public Message getParent() {
		return parent;
	}
}
