package plugins.commands;

import command.CommandMessage;
import command.ThreadedCommand;
import qorebot.Channel;
import qorebot.User;

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
    	channel.setTopic(this.toString());
    	return null;
    }

}
