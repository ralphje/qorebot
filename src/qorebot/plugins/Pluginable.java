package qorebot.plugins;

import java.util.HashSet;
import java.util.Set;
import qorebot.Event;

/**
 * Class for all pluginable objects. Pluginable objects should send events using
 * the #update() method. All registered plugins receive this event.
 *
 * @author Ralph Broenink
 */
public abstract class Pluginable {
    private Set<Plugin> plugins = new HashSet<Plugin>();

    /**
     * Registers a plugin to this object
     * @param plugin The plugin to register
     */
    public void register(Plugin plugin) {
        this.plugins.add(plugin);
    }

    /**
     * Unregisters a plugin from this object
     * @param plugin The plugin to unregister
     */
    public void unregister(Plugin plugin) {
        this.plugins.remove(plugin);
    }

    /**
     * Retrieves the current set of plugins
     */
    public Set<Plugin> getPlugins() {
        return this.plugins;
    }

    /**
     * Updates all registered plugins with the given event.
     * @param event The event to update with.
     */
    protected void update(Event event) {
        for (Plugin p : this.plugins)
            p.receive(event);
    }
}
