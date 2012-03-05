package plugins.commands;

import command.Command;
import command.CommandMessage;
import command.ThreadedCommand;
import java.util.List;
import org.jibble.pircbot.Colors;
import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;

/**
 * 
 *
 * @author Ralph Broenink
 */
public class HelpCommand extends ThreadedCommand {

    @Override
    public boolean isHandled(Channel channel, User user, CommandMessage msg) {
        return msg.isCommand("help");
    }

    @Override
    public String handleMessage(Channel channel, User user, CommandMessage msg) {
    	System.out.println(msg);
    	return null;
    }

}
