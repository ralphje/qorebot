package plugins.commands;

import command.Command;
import command.CommandMessage;
import command.ThreadedCommand;
import java.util.List;
import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;

/**
 * A command for handling user management. Different commands are present.
 * TODO: Add documentation
 * 
 * @author Ralph Broenink
 */
public class UserCommand extends ThreadedCommand {

	@Override
	public boolean isHandled(Channel channel, User user, CommandMessage msg) {
		return msg.isCommand("user");
	}

	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		List<String> cmd = this.parseArguments(channel, user, msg);

		/*
		 * Command: !user
		 */
		if (cmd.size() < 2) {
			if (!Command.checkPermissions("viewing channel permissions",
					UserLevel.ADMINISTRATOR, channel, user))
				return null;

			// TODO: Impelment
			// Command.sendMessage(channel, user,
			// "The following users have channel specific permissions:");
			Command.sendErrorMessage(channel, user, "Not implemented yet.");

			return null;

		} else {
			User affectedUser = user.getBot().getUserByNickname(cmd.get(1));
			String nickname = null;

			if (affectedUser == null) {
				if (cmd.get(1).startsWith("~")) {
					if (!Command.checkPermissions("managing by username",
							UserLevel.ADMINISTRATOR, channel, user))
						return null;

					affectedUser = new User(cmd.get(1).substring(1)); // we know  this isn't nice
					if (!affectedUser.isIdentified()) {
						Command.sendErrorMessage(channel, user,
								"This username could not be found.", true);
						return null;
					} else {
						Command.sendMessage(
								channel,
								user,
								"You are editing an user by its username. This method is not recommended.",
								true);
						nickname = cmd.get(1).substring(1);
					}
				} else {
					Command.sendErrorMessage(
							channel,
							user,
							"I can't determine who "
									+ cmd.get(1)
									+ " is. Assuming you "
									+ "didn't make a typo, this user probably joined before the "
									+ "bot did and hasn't done anything the bot noticed. This "
									+ "user is at least not identified (yet), although implicit "
									+ "identification can occur when he/she does something. "
									+ "You could try using '~<username>' (deprecated).",
							true);
					return null;
				}
			} else {
				nickname = affectedUser.getNickname();
			}

			/*
			 * Command: !user <nick>
			 */
			if (cmd.size() == 2 || !affectedUser.isIdentified()) {
				if (!Command.checkPermissions("viewing channel permissions",
						UserLevel.ADMINISTRATOR, channel, user))
					return null;

				if (affectedUser.isIdentified())
					Command.sendMessage(channel, user, nickname + " is a "
							+ affectedUser.getLevel(channel).toString(), true);
				else
					Command.sendMessage(channel, user, nickname
							+ " is not identified.", true);

				/*
				 * Command: !user <nick> channel <level>
				 */
			} else if (cmd.get(2).equals("channel")) {
				if (!Command.checkPermissions("changing channel permissions",
						UserLevel.ADMINISTRATOR, channel, user))
					return null;

				if (cmd.size() == 3) {
					Command.sendErrorMessage(channel, user,
							"Correct syntax is !user <nick> channel <level>",
							true);
				} else if (channel == null) {
					Command.sendErrorMessage(channel, user,
							"Command can only be executed in a channel!", true);
				} else {
					if (cmd.get(3).equals("remove")) {
						affectedUser.removeLevel(channel);
						Command.sendMessage(channel, user, "Channel level of "
								+ nickname + " removed.", true);
					} else {
						UserLevel determinedLevel = User.fromLevelString(cmd
								.get(3));
						if (determinedLevel.compareTo(UserLevel.IDENTIFIED) < 0
								|| determinedLevel
										.compareTo(UserLevel.ADMINISTRATOR) > 0) {
							Command.sendErrorMessage(
									channel,
									user,
									"Valid levels are identified, user, operator and administrator.",
									true);
						} else {
							affectedUser.setLevel(channel, determinedLevel);
							Command.sendMessage(
									channel,
									user,
									"Channel level of " + nickname
											+ " changed to "
											+ determinedLevel.toString(), true);
						}
					}
				}

				/*
				 * Command: !user <nick> user <level>
				 */
			} else if (cmd.get(2).equals("user")) {
				if (!Command.checkPermissions("changing user permissions",
						UserLevel.OWNER, channel, user))
					return null;

				if (cmd.size() == 3) {
					Command.sendErrorMessage(channel, user,
							"Correct syntax is !user <nick> user <level>", true);
				} else {
					UserLevel determinedLevel = User
							.fromLevelString(cmd.get(3));
					if (determinedLevel.compareTo(UserLevel.IDENTIFIED) < 0) {
						Command.sendErrorMessage(
								channel,
								user,
								"Valid levels are identified, user, operator, administrator and owner.",
								true);
					} else {
						affectedUser.setLevel(determinedLevel);
						Command.sendMessage(channel, user,
								"Global level of " + nickname + " changed to "
										+ determinedLevel.toString(), true);
					}
				}
			}
		}

		return null; // will not return anything
	}

}
