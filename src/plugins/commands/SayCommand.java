package plugins.commands;

import java.util.List;

import qorebot.Channel;
import qorebot.User;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.IOCommand;
import qorebot.plugins.commands.message.CommandMessage;

/**
 * Simple command that can be used to let the bot literally say things.
 *
 * @author Ralph Broenink
 */
public class SayCommand extends IOCommand {
	@Override
	public List<String> supportedCommands() {
		return Command.createList("help");
	}
	
	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		if (this.isHandled(channel, user, msg)) {
			if (msg.isCommand("say")) {
				List<String> list = this.parseArguments(channel, user, msg);
				return this.handleMessage(channel, user, list);
			} else {
				return msg.toString();
			}
		} else {
			return null;
		}
	}
	
	@Override
	public boolean isHandled(Channel channel, User user, CommandMessage msg) {
		return msg.isCommand("say") || msg.isCommand("parse");
	}

	@Override
	public String handleMessage(Channel channel, User user, List<String> arguments) {
		if (arguments.size() == 1) {
			return Command.wrapErrorMessage("Usage: !say <message>");
		} else {
			return Command.getArgumentConcat(arguments, 1);
		}
	}

}
