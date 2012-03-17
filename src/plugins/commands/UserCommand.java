package plugins.commands;


import java.util.List;
import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.ThreadedCommand;
import qorebot.plugins.commands.message.CommandMessage;

/**
 * A command for handling user management. Different commands are present.
 * TODO: Add documentation
 * 
 * @author Ralph Broenink
 */
public class UserCommand extends ThreadedCommand {

	@Override
	public List<String> supportedCommands() {
		return Command.createList("user");
	}

	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		List<String> arguments = this.parseArguments(channel, user, msg);

		/*
		 * Command: !user
		 */
		if (arguments.size() < 2) {
			if (Command.checkPermissions("viewing channel permissions", UserLevel.ADMINISTRATOR, channel, user)) {
				// TODO: Impelment
				// Command.sendMessage(channel, user,
				// "The following users have channel specific permissions:");
				Command.sendErrorMessage(channel, user, "Not implemented yet.");
				return null;
			}

		} else {
			// Get the affected user
			
			User affectedUser = user.getBot().getUserByNickname(arguments.get(1));
			String nickname = null;

			if (affectedUser == null) {
				if (arguments.get(1).startsWith("~")) {
					// If the username starts with a '~', we are managing by username
					// This allows management by 
					if (Command.checkPermissions("managing by username", UserLevel.ADMINISTRATOR, channel, user)) {
						nickname = arguments.get(1).substring(1);
						affectedUser = this.getUserByNickname(nickname); // we know  this isn't nice
						if (!affectedUser.isIdentified()) {
							Command.sendErrorMessage(channel, user, "This username could not be found.", true);
						} else {
							Command.sendMessage(channel, user, "You are editing an user by its username. This method is not recommended.", true);
							
						}
					}
				} else {
					Command.sendErrorMessage(channel, user,
									"I can't determine who " + arguments.get(1)
									+ " is. Assuming you "
									+ "didn't make a typo, this user probably joined before the "
									+ "bot did and hasn't done anything the bot noticed. This "
									+ "user is at least not identified (yet), although implicit "
									+ "identification can occur when he/she does something. "
									+ "You could try using '~<username>' (deprecated).", true);
				}
			} else {
				nickname = affectedUser.getNickname();
			}
			
			if (!affectedUser.isIdentified()) {
				Command.sendMessage(channel, user, nickname + " is not identified.", true);
			} else {
				if (arguments.size() == 2) {
					/*
					 * Command: !user <nick>
					 * Retrieves the current permissions.
					 */
					Command.sendMessage(channel, user, nickname + " is a " 
							+ affectedUser.getLevel(channel).toString(), true);
				
				} else if (arguments.get(2).equals("channel")) {
					/*
					 * Command: !user <nick> channel <level>
					 * Change channel permissions
					 */
					
					if (arguments.size() == 3) {
						Command.sendErrorMessage(channel, user, "Correct syntax is !user <nick> channel <level>", true);
					} else if (Command.checkPermissions("changing channel permissions", UserLevel.ADMINISTRATOR, channel, user)) {
						this.changeChannelLevel(channel, user, affectedUser, nickname, arguments.get(3));
					}
				} else if (arguments.get(2).equals("user")) {
					/*
					 * Command: !user <nick> user <level>
					 * Change user permissions
					 */
					
					if (arguments.size() == 3) {
						Command.sendErrorMessage(channel, user, "Correct syntax is !user <nick> user <level>", true);
					} else if (Command.checkPermissions("changing user permissions", UserLevel.OWNER, channel, user)) {
						this.changeUserLevel(channel, user, affectedUser, nickname, arguments.get(3));
					}
				}
			}
		}

		return null; // will not return anything
	}
	
	/**
	 * Changes the channel level of the affectedUser to permission
	 */
	private void changeChannelLevel(Channel channel, User user, User affectedUser, String nickname, String permission) {
		if (permission.equals("remove")) {
			affectedUser.removeLevel(channel);
			Command.sendMessage(channel, user, "Channel level of " + nickname + " removed.", true);
			
		} else {
			UserLevel determinedLevel = User.fromLevelString(permission);
			if (determinedLevel.compareTo(UserLevel.IDENTIFIED) < 0
					|| determinedLevel.compareTo(UserLevel.ADMINISTRATOR) > 0) {
				Command.sendErrorMessage(channel, user, "Valid levels are identified, user, operator and administrator.", true);
			} else {
				affectedUser.setLevel(channel, determinedLevel);
				Command.sendMessage(channel, user, "Channel level of " + nickname + " changed to " + determinedLevel.toString(), true);
			}
		}
	}
	
	/**
	 * Changes the user level of the affectedUser to permission
	 */
	private void changeUserLevel(Channel channel, User user, User affectedUser, String nickname, String permission) {
		UserLevel determinedLevel = User.fromLevelString(permission);
		if (determinedLevel.compareTo(UserLevel.IDENTIFIED) < 0) {
			Command.sendErrorMessage(channel, user, "Valid levels are identified, user, operator, administrator and owner.", true);
		} else {
			affectedUser.setLevel(determinedLevel);
			Command.sendMessage(channel, user,"Global level of " + nickname + " changed to " + determinedLevel.toString(), true);
		}
	}
	
	/**
	 * Returns a user by his nickname. Separate function for deprecation warning
	 */
	@SuppressWarnings("deprecation")
	private User getUserByNickname(String nickname) {
		return new User(nickname); // we know  this isn't nice
	}
}
