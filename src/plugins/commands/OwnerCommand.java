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
 * Core command that offers several administration commands, like !shutdown
 * and !raw. Could be safely disabled if management is not required.
 *
 * Note that, although this command checks for the right permissions, extra
 * security could be added by setting this command to only load to the users
 * with the right permissions.
 *
 * @author Ralph Broenink
 */
public class OwnerCommand extends ThreadedCommand {

    @Override
    public boolean isHandled(Channel channel, User user, CommandMessage msg) {
        return msg.isCommand("shutdown") || msg.isCommand("raw");
    }

    @Override
    public String handleMessage(Channel channel, User user, CommandMessage msg) {
        if (!Command.checkPermissions("managing the bot", UserLevel.OWNER, channel, user))
            return null;

        /* Command: !shutdown
         * Disconnects the server and shuts down the program.
         */
        if (msg.isCommand("shutdown")) {
            Command.sendMessage(channel, user, "The bot will be shut down. Goodbye!");
            this.getPlugin().getBot().quitServer("Shutdown requested by " + user.getNickname());
            System.exit(0);



        /* Command: !raw [force] <command>
         * Sends a raw text to the server. Could be used to perform some actions
         * not implemented by any other command.
         */
        } else if(msg.isCommand("raw")) {
            List<String> cmd = this.parseArguments(channel, user, msg);
            if (cmd.size() < 2) {
                Command.sendMessage(channel, user, Colors.BOLD + Colors.RED + "Correct syntax is !raw [force] <command>");
            } else {
                if (cmd.get(1).equals("force")) {
                    if (cmd.size() < 3) {
                        Command.sendMessage(channel, user, Colors.BOLD + Colors.RED + "Correct syntax is !raw [force] <command>");
                    } else {
                        String command = Command.getArgumentConcat(cmd, 2);
                        this.getPlugin().getBot().sendRawLine(command);
                        Command.sendMessage(channel, user, "Sent command '" + command + Colors.NORMAL + "' directly to server.");
                    }
                } else {
                    String command = Command.getArgumentConcat(cmd, 1);
                    this.getPlugin().getBot().sendRawLineViaQueue(command);
                    Command.sendMessage(channel, user, "Sent command '" + command + Colors.NORMAL + "' via queue to server.");
                }
            }
        }
        return null;
    }

}
