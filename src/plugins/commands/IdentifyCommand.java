package plugins.commands;

import command.Command;
import command.CommandMessage;
import command.ThreadedCommand;
import java.util.List;
import qorebot.Channel;
import qorebot.User;

/**
 * Core plugin that handles the identification of users. Several commands are
 * implemented: !unidentify, !identify, !register, !changepass and !whoami.
 *
 * The first two handle identification. Unidentifying (logging out) will remove
 * all permissions; identifying will request permissions. Register and changepass
 * are some management features available for everyone and whoami is meant to
 * check the current logged in state.
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
        /* Command: !unidentify
         * Removes the current identification
         */
        if (msg.isCommand("unidentify")) {
            if (!user.isIdentified())
                Command.sendMessage(channel, user, "You are currently not identified.", true);
            else {
                user.unidentify();
                Command.sendMessage(channel, user, "You aren't identified anymore.", true);
            }

        /* Command: !identify <username> <password>
         * Identifies a user with the bot
         */
        } else if (msg.isCommand("identify")) {
            if (channel != null) {
                Command.sendErrorMessage(channel, user, "Identification is only possible in private chat.", true);
            } else {
                if (user.isIdentified())
                    user.sendMessage("You are already identified.");
                else {
                    List<String> cmd = this.parseArguments(channel, user, msg);
                    if (cmd.size() < 3) {
                        Command.sendErrorMessage(channel, user, "Correct syntax is !identify <username> <password>", true);
                    } else {
                        if (user.identify(cmd.get(1), cmd.get(2))) {
                            user.sendMessage("Successfully identified. Remember to use !unidentify if you are at a public location!");
                        } else {
                            Command.sendErrorMessage(channel, user, "Identification failed. Did you enter your password correctly?", true);
                        }
                    }
                }
            }

        /* Command: !register <username> <password>
         * Registers an unidentified user with the bot.
         */
        } else if (msg.isCommand("register")) {
            if (channel != null) {
                Command.sendErrorMessage(channel, user, "Registration is only possible in private chat.", true);
            } else {
                if (user.isIdentified())
                    user.sendMessage("You are already identified. Please use !unidentify to logout.");
                else {
                    List<String> cmd = this.parseArguments(channel, user, msg);
                    if (cmd.size() < 3) {
                        Command.sendErrorMessage(channel, user, "Correct syntax is !register <username> <password>", true);
                    } else {
                        if (user.register(cmd.get(1), cmd.get(2))) {
                            user.sendMessage("Successfully registered. Use !identify to identify yourself now.");
                        } else {
                            Command.sendErrorMessage(channel, user, "Registration failed. The username might already exist.", true);
                        }
                    }
                }
            }

        /* Command: !changepass <oldPass> <newPass>
         * Changes a password for the curernt user.
         */
        } else if (msg.isCommand("changepass")) {
            if (channel != null) {
                Command.sendErrorMessage(channel, user, "Changing your password is only possible in private chat.", true);
            } else {
                List<String> cmd = this.parseArguments(channel, user, msg);
                if (cmd.size() < 3) {
                    Command.sendErrorMessage(channel, user, "Correct syntax is !changepass <oldPass> <newPass>", true);
                } else {
                    if (user.changePassword(cmd.get(1), cmd.get(2))) {
                        user.sendMessage("Password successfully changed");
                    } else {
                        Command.sendErrorMessage(channel, user, "Password was not changed. Did you enter your current password correctly?", true);
                    }
                }
            }

        /* Command: !whoami
         * Gives a string with the current user information.
         */
        } else if (msg.isCommand("whoami")) {
            String result;
            if (user.isIdentified()) {
                result = "You are identified as " + user.getUsername() + ". Your effective level is '" + user.getLevel(channel).toString() + "'.";
            } else {
                result = "You are currently not identified. Please use !identify in a private chat to identify. ";
            }
            Command.sendMessage(channel, user, result, true);

        }
        return null;
    }

}
