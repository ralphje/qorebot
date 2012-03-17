package qorebot.plugins.commands.message;

import java.util.ArrayList;
import java.util.List;

import qorebot.plugins.commands.Command;


/**
 * A message representing a command and therefore existing of several commands.
 * 
 * @author Ralph Broenink
 */
public class CommandMessage extends Message {
	private List<Message> messages;

	/**
	 * Creates a new StringMessage.
	 * 
	 * @param parent
	 *            The parent of this message
	 * @param message
	 *            The string message to convert to Messages
	 */
	public CommandMessage(CommandMessage parent, String message) {
		super(parent);
		this.messages = new ArrayList<Message>();
		CommandMessage.splitMessage(this, message);
	}

	/**
	 * Creates a new CommandMessage.
	 * 
	 * @param parent
	 *            The parent of this message
	 * @param messages
	 *            The children of this message.
	 */
	public CommandMessage(CommandMessage parent, List<Message> messages) {
		super(parent);
		this.messages = messages;
	}

	/**
	 * Creates a new CommandMessage.
	 * 
	 * @param parent
	 *            The parent of this message
	 * @param message
	 *            The child of this message.
	 */
	public CommandMessage(CommandMessage parent, Message message) {
		super(parent);
		this.messages = new ArrayList<Message>();
		this.messages.add(message);
	}

	/**
	 * Creates the CommandMessage with a parent.
	 * 
	 * @param parent
	 */
	public CommandMessage(CommandMessage parent) {
		super(parent);
		this.messages = new ArrayList<Message>();
	}

	/**
	 * Retrieves the string message.
	 */
	public List<Message> getMessages() {
		return this.messages;
	}

	/**
	 * Adds a message to this CommandMessage.
	 * 
	 * @param m
	 *            The message to add
	 */
	public void addMessage(Message m) {
		this.messages.add(m);
	}

	/**
	 * Returns whether this Message is the specified command by checking whether
	 * the first element of this message is actually a string message and equals
	 * the given string or the prefixed string.
	 * 
	 * @param s
	 *            The command
	 */
	public boolean isCommand(String s) {
		if (this.getMessages().get(0) instanceof StringMessage) {
			String msg = ((StringMessage) this.getMessages().get(0)).getMessage();
			return Command.isCommand(msg, s);
		} else
			return false;
	}

	/**
	 * Splits a message in usable chuncks. A subcommand (starting with PREFIX)
	 * should be grouped with {}. A string containing whitespace characters
	 * should be grouped with "". In all other cases, everything is separated
	 * with white space characters.
	 * 
	 * Returns the message as an array, split in chucks. Every command will be
	 * converted to another array.
	 * 
	 * @param message
	 */
	public static CommandMessage splitMessage(CommandMessage parent, String message) {
		int level = 0;
		boolean firstWordInCommand = true;
		boolean inString = false;
		String matchedWord = "";
		message = message.trim();
		int length = message.length();

		for (int i = 0; i < length; i++) {
			char c = message.charAt(i);

			// When we're at a deeper level then 0, we should recurse it.
			// However, that should only be done when we're back at level 0.

			// Test whether this is an escape character
			if (c == '\\' && i + 1 < length) {
				char n = message.charAt(i + 1);

				// This is an escape character when it's followed by
				// ", \, { or }
				// However, when we're at level > 0, we should let the escape
				// character where it was.
				if (n == '"' || n == '\\' || n == '{' || n == '}') {
					if (level > 0)
						matchedWord += c;
					matchedWord += n;
					i++; // skip an extra letter
					
					// Not an escape character
				} else {
					matchedWord += c;
				}

			// When we can co any level lower (closer to 0), and we're
			// currently
			// not in a string, and we receive a } (which is not escaped)
			} else if (!inString && level > 0 && c == '}') {
				level--;
				if (level == 0) {
					parent.addMessage(new CommandMessage(parent, matchedWord));
					matchedWord = "";
				} else {
					matchedWord += c;
				}

			// When we find a { (and we're not in a string) and we're not at
			// the 0th level, we should add the { to our history.
			} else if (!inString && c == '{') {
				if (level != 0) {
					matchedWord += c;
				}
				level++;

			// Implictly, a command starting with a ! is a new level.
			} else if (!inString && !firstWordInCommand && c == '!' && Character.isWhitespace(message.charAt(i - 1))) {
				matchedWord += c;
				level++;

			} else if (level > 0) {
				matchedWord += c;

			// When we encouter a string delimiter, we should either end or
			// start the string.
			} else if (c == '"') {
				if (inString) {
					// Test whether the next character is a whitespace
					// character.
					// If not, it's not a special case.
					if (i + 1 < length) {
						char n = message.charAt(i + 1);
						if (Character.isWhitespace(n)) {
							inString = false;
							parent.addMessage(new StringMessage(parent, matchedWord));
							matchedWord = "";
						} else {
							matchedWord += c;
						}
					} else {
						inString = false;
						parent.addMessage(new StringMessage(parent, matchedWord));
						matchedWord = "";
					}
				} else {
					// Test whether the prev character is a whitespace
					// character.
					// If not, it's not a special case.
					if (i > 0) {
						char p = message.charAt(i - 1);
						if (Character.isWhitespace(p)) {
							inString = true;
						} else {
							matchedWord += c;
						}
					} else {
						inString = true;
					}
				}
			// When we're not in a string and encouter a whitespace
			// character,
			// we should end the current word.
			// When we've found the last character, we should finish off.
			} else if (!inString && (Character.isWhitespace(c) || i + 1 == length)) {
				
				if (i + 1 == length && !Character.isWhitespace(c))
					matchedWord += c;

				if (matchedWord.length() > 0) {
					if (!firstWordInCommand && matchedWord.startsWith(Command.PREFIX)) {
						if (level > 0) {
							parent.addMessage(new CommandMessage(parent, matchedWord));
						} else {
							CommandMessage cm = new CommandMessage(parent);
							cm.addMessage(new StringMessage(cm, matchedWord));
							parent.addMessage(cm);
						}
					} else {
						if (level > 0)
							parent.addMessage(new CommandMessage(parent, matchedWord));
						else
							parent.addMessage(new StringMessage(parent, matchedWord));
					}
					matchedWord = "";
				}
				firstWordInCommand = false;

				// No special case.
			} else {
				matchedWord += c;
			}
		}

		if (matchedWord.length() > 0) {
			if (!firstWordInCommand && matchedWord.startsWith(Command.PREFIX)) {
				if (level > 0) {
					parent.addMessage(new CommandMessage(parent, matchedWord));
				} else {
					CommandMessage cm = new CommandMessage(parent);
					cm.addMessage(new StringMessage(cm, matchedWord));
					parent.addMessage(cm);
				}
			} else {
				if (level > 0)
					parent.addMessage(new CommandMessage(parent, matchedWord));
				else
					parent.addMessage(new StringMessage(parent, matchedWord));
			}
			matchedWord = "";
		}

		return parent;
	}

	@Override
	public String toString() {
		String s = "{";

		for (Message m : this.messages)
			s += " " + m.toString();
		s += " }";
		return s;
	}

}
