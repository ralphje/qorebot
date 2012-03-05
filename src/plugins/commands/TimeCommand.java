package plugins.commands;

import command.CommandMessage;
import command.IOCommand;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import org.jibble.pircbot.Colors;
import qorebot.Channel;
import qorebot.User;

/**
 * A really simple example of a command; when !time is received, the time is
 * returned. When any parameters are given, those are interpreted as the format
 * for the time.
 *
 * @author Ralph Broenink
 */
public class TimeCommand extends IOCommand {

    @Override
    public boolean isHandled(Channel channel, User user, CommandMessage msg) {
        return msg.isCommand("time");
    }

    @Override
    public String handleMessage(Channel channel, User user, List<String> msg) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = null;
        if (msg.size() == 1)
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        else {
            String r = "";
            for (int i = 1; i < msg.size(); i++)
                r += msg.get(i) + " ";
            try {
                sdf = new SimpleDateFormat(r);
            } catch(IllegalArgumentException ex) {
                return Colors.BOLD + Colors.RED + "Invalid time format. Please refer to !help timeformat for valid arguments.";
            }
        }

        return "It is now " + sdf.format(cal.getTime());
    }
}
