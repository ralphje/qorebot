package qorebot.plugins;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class loader used to load Plugins and therefore allowing plugins to reload.
 * 
 * Two types of classes may be loaded using the PluginLoader: 
 * 1. 
 * Classes starting with plugins.; these classes will be looked for at the 
 * location of the code source of the protection domain of the PluginLoader. 
 * 2. 
 * Class names starting with 'file:'; these will be looked for at the specified 
 * location.
 * 
 * @author Jakob Jenkov
 * @author Ralph Broenink
 * @see http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
 */
public class PluginLoader extends ClassLoader {
	/**
	 * Creates a new PluginLoader, with as parent the loader of this class.
	 */
	public PluginLoader() {
		this(PluginLoader.class.getClassLoader());
	}

	/**
	 * Creates a new PluginLoader with as its parent loader the given parent.
	 * 
	 * @param parent
	 *            The parent ClassLoader
	 */
	public PluginLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// Get the url for the name
		String path = "";
		if (!name.startsWith("plugins.")) {
			// Name deosn't start with plugins.
			if (name.startsWith("file:"))
				path = name;
			else {
				// Name should be loaded by its super class
				// This happens to all classes referenced by stuff loaded by
				// this loader. So, if QoreBot is referenced in a plugin, it 
				// will pass through this loader.
				return super.loadClass(name);
			}
		} else {
			path = PluginLoader.class.getProtectionDomain().getCodeSource().getLocation().toString()
					+ name.replace(".", File.separator) + ".class";
		}

		try {
			// Set up the connection
			URL url = new URL(path);
			URLConnection connection = url.openConnection();
			InputStream input = connection.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			// Load the data
			int data = input.read();
			while (data != -1) {
				buffer.write(data);
				data = input.read();
			}
			
			// Close the connection and create a ByteArray
			input.close();
			byte[] classData = buffer.toByteArray();

			return this.defineClass(name, classData, 0, classData.length);

		} catch (MalformedURLException e) {
			Logger.getLogger(PluginLoader.class.getName()).log(Level.SEVERE,
					"Failed loading " + name, e);
		} catch (IOException e) {
			Logger.getLogger(PluginLoader.class.getName()).log(Level.SEVERE,
					"Failed loading " + name, e);
		}

		return null;
	}

}
