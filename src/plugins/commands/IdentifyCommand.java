package plugins.commands;


import java.util.List;
import qorebot.Channel;
import qorebot.User;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.ThreadedCommand;
import qorebot.plugins.commands.message.CommandMessage;

/**
 * Core plugin that handles the identification of users. Several commands are
 * implemented: !unidentify, !identify, !register, !changepass and !whoami.
 * 
 * The first two handle identification. Unidentifying (logging out) will remove
 * all permissions; identifying will request permissions. Register and
 * changepass are some management features available for everyone and whoami is
 * meant to check the current logged in state.
 * 
 * This plugin is (almost) a must to correctly manage your bot. You may want to
 * disable it for channels (most of these commands won't even work there for
 * privacy reasons). Note that enabling this for only certain uses will be
 * pointless, as those users can't identify themselves.
 * 
 * @author Ralph Broenink
 */
public class IdentifyCommand extends ThreadedCommand {

	@Override
	public boolean isHandled(Channel channel, User user, CommandMessage msg) {
		return msg.isCommand("identify") || msg.isCommand("unidentify")
				|| msg.isCommand("whoami") || msg.isCommand("changepass")
				|| msg.isCommand("register");
	}

	@Override
	public String handleMessage(Channel channel, User user, CommandMessage msg) {
		// Check whether we're in a channel for private commands
		if (channel != null) {
			String action = null;
			if (msg.isCommand("register")) {
				action = "Registration";
			} else if (msg.isCommand("identify")) {
				action = "Identification";
			} else if (msg.isCommand("changepass")) {
				action = "Changing your password";
			}
			if (action != null) {
				Command.sendErrorMessage(channel, user, action + " is only possible in private chat.", true);
				return null;
			}
		}
		// Check whether we're already identified and try to identify or
		// register.
		if (user.isIdentified() && (msg.isCommand("identify") || msg.isCommand("register"))) {
			user.sendMessage("You are already identified. Please use !unidentify to logout.");
			return null;
		}

		List<String> arguments = this.parseArguments(channel, user, msg);

		if (msg.isCommand("unidentify")) {

			/*
			 * Command: !unidentify 
			 * Removes the current identification
			 */
			this.unidentify(channel, user);

		} else if (msg.isCommand("identify")) {
			/*
			 * Command: !identify <username> <password> 
			 * Identifies a user with the bot
			 */
			if (arguments.size() < 3) {
				Command.sendErrorMessage(channel, user, "Correct syntax is !identify <username> <password>", true);
			} else {
				this.identify(channel, user, arguments.get(1), arguments.get(2));
			}

		} else if (msg.isCommand("register")) {
			/*
			 * Command: !register <username> <password> 
			 * Registers an unidentified user with the bot.
			 */
			
			if (arguments.size() < 3) {
				Command.sendErrorMessage(channel, user, "Correct syntax is !register <username> <password>", true);
			} else {
				this.register(channel, user, arguments.get(1), arguments.get(2));
			}

		} else if (msg.isCommand("changepass")) {
			/*
			 * Command: !changepass <oldPass> <newPass> 
			 * Changes a password for the curernt user.
			 */
			
			if (arguments.size() < 3) {
				Command.sendErrorMessage(channel, user, "Correct syntax is !changepass <oldPass> <newPass>", true);
			} else {
				this.changepass(channel, user, arguments.get(1), arguments.get(2));
			}

		} else if (msg.isCommand("whoami")) {
			/*
			 * Command: !whoami 
			 * Gives a string with the current user information.
			 */
			
			this.whoami(channel, user);

		}
		return null;
	}

	/**
	 * Whoami command. Gives the current user level and name to the user.
	 */
	private void whoami(Channel channel, User user) {
		String result;
		if (user.isIdentified()) {
			result = "You are identified as " + user.getUsername()
					+ ". Your effective level is '"
					+ user.getLevel(channel).toString() + "'.";
		} else {
			result = "You are currently not identified. Please use !identify in a private chat to identify. ";
		}
		Command.sendMessage(channel, user, result, true);
	}

	/**
	 * Identify command. Identify user with the given username and password
	 */
	private void identify(Channel channel, User user, String username,
			String password) {
		if (user.identify(username, password)) {
			user.sendMessage("Successfully identified. Remember to use !unidentify if you are at a public location, to prevent someone else from getting reidentified as you!");
		} else {
			Command.sendErrorMessage(channel, user, "Identification failed. Did you enter your password correctly?", true);
		}
	}

	/**
	 * Unidentify command. Removes the identification from the current user.
	 */
	private void unidentify(Channel channel, User user) {
		if (!user.isIdentified())
			Command.sendMessage(channel, user, "You are currently not identified.", true);
		else {
			user.unidentify();
			Command.sendMessage(channel, user, "You aren't identified anymore.", true);
		}
	}

	/**
	 * Registration command. Registers a new user.
	 */
	private void register(Channel channel, User user, String username, String password) {
		if (user.register(username, password)) {
			user.sendMessage("Successfully registered. Use !identify to identify yourself now.");
		} else {
			Command.sendErrorMessage(channel, user, "Registration failed. The username might already exist.", true);
		}
	}

	/** 
	 * Changes the password of the current user.
	 */
	private void changepass(Channel channel, User user, String password1, String password2) {
		if (user.changePassword(password1, password2)) {
			user.sendMessage("Password successfully changed");
		} else {
			Command.sendErrorMessage(channel, user, "Password was not changed. Did you enter your current password correctly?", true);
		}
	}
}
