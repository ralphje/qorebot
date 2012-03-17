package plugins.commands;


import java.util.List;
import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.ThreadedCommand;
import qorebot.plugins.commands.message.CommandMessage;

/**
 * Adds both the op- and voice-commands to the channel. These commands can be
 * called by both users and operators to voice and/or op themselves. This only
 * works when the bot is already an op.
 * 
 * This core command can safely be omitted from production, although that's not
 * recommended - this could leave your bot the only one with ops in your channel
 * and that might be just the reason you have a bot!
 * 
 * @author Ralph Broenink
 */
public class OpVoiceCommand extends ThreadedCommand {

	@Override
	public List<String> supportedCommands() {
		return Command.createList("op", "voice");
	}

	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		// We can only do anything if we're a op
		if (channel.isOp()) {
			List<String> cmd = this.parseArguments(channel, user, msg);
			
			if (msg.isCommand("op")) {
				if (!Command.checkPermissions("requesting op", UserLevel.OPERATOR, channel, user))
					return null;

				if (cmd.size() > 1) {
					channel.op(cmd.get(1));
				} else {
					channel.op(user);
				}
				
			} else if (msg.isCommand("voice")) {
				if (!Command.checkPermissions("requesting voice", UserLevel.USER, channel, user))
					return null;

				if (cmd.size() > 1) {
					channel.voice(cmd.get(1));
				} else {
					channel.voice(user);
				}
			}
		} else {
			Command.sendErrorMessage(channel, user, "The bot is not an operator, so your mode can't be changed.", true);
		}
		return null;
	}

}
