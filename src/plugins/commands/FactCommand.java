/* THIS PLUGIN IS NOT PART OF THE OFFICIAL QOREBOT DISTRIBUTION. THE AUTHOR(S) 
 * OF THE DISTRIBUTION HAVE INCLUDED THIS PLUGIN FOR COMPLETENESS, HANDYNESS OR
 * SOME SIMILAR REASON, BUT IT IS NOT INTENDED TO BE DISCUSSED AS PART OF THE
 * OFFICIAL DISTRIBUTED SOFTWARE. THIS PLUGIN MAY OR MAY NOT BE DISTRIBUTED 
 * UNDER THE SAME LICENSE TERMS AS THE OFFICIAL DISTRIBUTION IN WHICH IT IS 
 * BUNDLED.
 * 
 * That's a whole lot of caps lock to indicate that this file is not part of
 * QoreBot or the plugins developed by the developers intended or required to be 
 * included in the main  distribution, but rather a 'fun' plugin that was 
 * developed by someone (maybe the developers themselves) or a useful plugin not
 * created by the developers. 
 * 
 * You should not rely on this plugin being present or loaded at any time during
 * development of other plugins or while working on other parts of the
 * distribution.
 * 
 * So, in short: this plugin is not loaded nor installed by default and if you 
 * want to sue someone, sue the author of this plugin and not the authors of
 * QoreBot. 
 */

package plugins.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.jibble.pircbot.Colors;

import com.google.gson.stream.JsonReader;

import qorebot.Channel;
import qorebot.User;
import qorebot.plugins.commands.Command;
import qorebot.plugins.commands.IOCommand;

/**
 * Retrieves a fact from the Amazing Fact Generator hosted at mentalfloss.com.
 *
 * @author Ralph Broenink
 */
public class FactCommand extends IOCommand {
	public static final String FACT_SOURCE = "http://mentalfloss.com/amazingfactgenerator/load-fact.php";
	
	@Override
	public List<String> supportedCommands() {
		return Command.createList("fact");
	}

	@Override
	public String handleMessage(Channel channel, User user, List<String> arguments) {
		if (arguments.size() > 2) 
			return Command.wrapErrorMessage("Usage: !fact or !fact <id>");
		else {
			// Base message
			String msg = Command.wrapErrorMessage("Sorry, your fact could not be retrieved.");
			try {
				// Prepare the url
				URL url = new URL(FACT_SOURCE);
				if (arguments.size() == 2) { 
					try {
						int id = Integer.parseInt(arguments.get(1));
						url = new URL(FACT_SOURCE + "?id=" + id);
					} catch(NumberFormatException e) {
					}
				}
			
				// Fetch the url
				BufferedReader in = new BufferedReader(new InputStreamReader(((HttpURLConnection) url.openConnection()).getInputStream()));
				
				// Creative JSON reading
				JsonReader reader = new JsonReader(in);
				reader.beginObject();
				while (reader.hasNext()) {
					if (reader.nextName().equals("post_content")) {
						msg = reader.nextString();
						// Yay, this is ugly. But it works.
						msg = msg.replace("<em>", "");
						msg = msg.replace("</em>", "");
						msg = msg.replace("<strong>", Colors.BOLD);
						msg = msg.replace("</strong>", Colors.NORMAL);
					} else {
						reader.skipValue();
					}
				}
				reader.endObject();

			} catch (IOException | IllegalStateException e) {
			}

			return msg;
			
		}
	}

}
