package qorebot;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This initializes the bot.
 * 
 * @author Ralph Broenink
 */
public class Main {
    /** The file to write the log to */
    public static final String LOGFILE = "qorebot.log";
    /** The default server. */
    public static final String DEFAULT_SERVER = "irc.snt.utwente.nl";
    /** The default nickname */
    public static final String DEFAULT_NICKNAME = "QoreBot";

    /**
     * Starts the QoreBot. When no command line arguments are given, it connects
     * with DEFAULT_SERVER and with DEFAULT_NICKNAME. The first parameter is the
     * server to connect to; the second parameter is the nickname.
     *
     * Logging is automatically set to log to LOGFILE.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("");
        try {
            Handler handler;
            handler = new FileHandler(LOGFILE, true);
            logger.addHandler(handler);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                    "Failed opening the error log.", ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                    "Could not add the file handler to the logger.", ex);
        }

        QoreBot bot = new QoreBot();
        if (args.length == 0)
            bot.connect(DEFAULT_SERVER, DEFAULT_NICKNAME);
        else if (args.length == 1)
            bot.connect(args[0], DEFAULT_NICKNAME);
        else
            bot.connect(args[0], args[1]);
    }

}
