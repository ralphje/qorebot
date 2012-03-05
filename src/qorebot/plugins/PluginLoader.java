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
 * 1. Classes starting with plugins.; these classes will be looked for at the
 *    location of the ocde source of  the protection domain of the PluginLoader.
 * 2. Class names starting with 'file:'; these will be looked for at the specified
 *    location.
 *
 * @author Jakob Jenkov
 * @author Ralph Broenink
 * @see <a href="http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html">Dynamic Class Loading & Reloading</a>
 */
public class PluginLoader extends ClassLoader {
    public PluginLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        String url = "";
        if (!name.startsWith("plugins.")) {
            if (name.startsWith("file:"))
                url = name;
            else   
                return super.loadClass(name);
        } else {
            url = PluginLoader.class.getProtectionDomain().getCodeSource().getLocation().toString() + name.replace(".", File.separator) + ".class";
        }
            
        
        try {
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass(name, classData, 0, classData.length);

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
