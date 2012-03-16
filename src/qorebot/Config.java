package qorebot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
	/** The path to the configuration file. */
	public static final String CONFIGURATION_FILE = "config.ini";

	/**
	 * Retrieves properties from the configuration file. The method is protected
	 * to prevent plugins from retrieving private settings.
	 * 
	 * @return A Properties object holding the properties stored in the
	 *         configuration file, or null if retrieval failed.
	 */
	protected static Properties getConfigFile() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(Config.CONFIGURATION_FILE));
			Properties prop = new Properties();
			prop.load(in);
			return prop;
		} catch (FileNotFoundException e) {
			Logger.getLogger(Config.class.getName()).log(Level.SEVERE,
					"Configuration file " + Config.CONFIGURATION_FILE + " not found.", e);
			return null;
		} catch (IOException e) {
			Logger.getLogger(Config.class.getName()).log(Level.SEVERE,
					"Failed loading configuration file " + Config.CONFIGURATION_FILE + ".", e);
			return null;
		}
	}
	
	/**
	 * Returns the provided property from the configuration file. 
	 * 
	 * @param key The key to retrieve.
	 * @return The value stored with the specified key, or null if something
	 *         went wrong (ie, the file isn't loaded or the 
	 */
	protected static String getValueFromConfigFile(String key) {
		Properties prop = Config.getConfigFile();
		if (prop != null && prop.containsKey(key)) {
			return prop.getProperty(key);
		} else {
			return null;
		}
	}
}
