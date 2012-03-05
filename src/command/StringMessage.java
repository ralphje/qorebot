package command;

/**
 * Representation of a string part of a message.
 * @author ralphje
 */
public class StringMessage extends Message {
    private String message = null;

    /**
     * Creates a new StringMessage.
     * @param parent The parent of this message
     * @param message The string message.
     */
    public StringMessage(CommandMessage parent, String message) {
        super(parent);
        this.message = message;
    }

    /**
     * Retrieves the string message.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "\"" + this.message + "\"";
    }
}
