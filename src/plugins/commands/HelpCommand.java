package plugins.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jibble.pircbot.Colors;

import qorebot.Channel;
import qorebot.Database;
import qorebot.User;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.IOCommand;

/**
 * Core command that provides help information. Help messages are stored in the
 * database and can be edited or created by new plugins.
 *
 * @author Ralph Broenink
 */
public class HelpCommand extends IOCommand {

	@Override
	public List<String> supportedCommands() {
		ArrayList<String> s = new ArrayList<String>();
		Collections.addAll(s, "help");
		return s;
	}

	@Override
	public String handleMessage(Channel channel, User user, List<String> arguments) {
		// Get the key from the arguments or use 'default'
		String key = null;
		if (arguments.size() == 1) {
			key = "default";
		} else {
			key = Command.getArgumentConcat(arguments, 1);
		}
		
		if (key.equals("topics")) {
			PreparedStatement st = Database.gps("SELECT code FROM help WHERE public = 1 ORDER BY code");
			if (st == null)
				return Command.wrapErrorMessage("An error occured while retrieving help information.");
		
			try {
				ResultSet result = st.executeQuery();
				String res = "The following general help topics are available: ";
				while (result.next()) {
					res += result.getString("code") + (result.isLast() ? "" : ", ");
				}
				res += "    Presence or absence of a help topic does not imply presence/absence of a plugin or command.";
				return res;
				
			} catch (SQLException ex) {
				Logger.getLogger(HelpCommand.class.getName()).log(Level.SEVERE, "Could not load help topics", ex);
				return Command.wrapErrorMessage("An error occured while retrieving help information.");
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
			
		} else {
			// Query the database
			PreparedStatement st = Database.gps("SELECT title,content FROM help WHERE code = ?");
			if (st == null)
				return Command.wrapErrorMessage("An error occured while retrieving help information.");
			
			try {
				st.setString(1, key);
				ResultSet result = st.executeQuery();
				String res = null;
				while (result.next()) {
					// Print title if specified
					String title = result.getString("title");
					if (title.length() > 0)
						Command.sendMessage(channel, user, Colors.UNDERLINE + title);
					
					// Print content line by line
					String content = result.getString("content");
					Scanner scanner = new Scanner(content);
					while (scanner.hasNextLine()) {
						res = scanner.nextLine();
						if (scanner.hasNextLine() && res.length() > 0)
							Command.sendMessage(channel, user, res);
					}
				}
				// no topics printed
				if (res == null)
					return Command.wrapErrorMessage("Specified topic ("+key+") was not found. Use !help for more information.");
				else
					return res;
				
			} catch (SQLException ex) {
				Logger.getLogger(HelpCommand.class.getName()).log(Level.SEVERE,
						"Could not load the help topic", ex);
				return Command.wrapErrorMessage("An error occured while retrieving help information.");
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException ex1) {
				}
			}
		}
	}
}
