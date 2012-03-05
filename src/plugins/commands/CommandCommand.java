package plugins.commands;

import command.Command;
import command.CommandMessage;
import command.ThreadedCommand;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.Colors;
import plugins.CommandPlugin;
import qorebot.Channel;
import qorebot.User;
import qorebot.UserLevel;
import qorebot.plugins.Plugin;
import qorebot.plugins.Pluginable;

/**
 * Core command that handles plugin-based commands. This command is capable of
 * (re)loading plugins into a specific channel or user.
 * 
 * The minimum privilege level is LEVEL_ADMINISTRATOR for all channel-related
 * tasks and LEVEL_OWNER for all global tasks.
 *
 * @author Ralph Broenink
 */
public class CommandCommand extends ThreadedCommand {

    @Override
    public String handleMessage(Channel channel, User user, CommandMessage msg) {
        try {
            if (!Command.checkPermissions("managing commands", UserLevel.ADMINISTRATOR, channel, user))
                return null;

            if (msg.isCommand("command")) {
                List<String> arguments = this.parseArguments(channel, user, msg);

                // Retrieve the command plugin
                Plugin commandPlugin = this.getPlugin();
                if (commandPlugin == null) {
                    Command.sendMessage(channel, user, Colors.BOLD + Colors.RED + "The plugin plugins.CommandPlugin is not loaded, which is kinda weird.");
                    return null;
                }

                /* Command: !command
                 * Shows all loaded commands.
                 */
                if (arguments.size() == 1) {
                    if (channel == null) {
                        user.sendMessage("The following commands are loaded for " + user.getNickname() + ":");
                        String r = "";
                        Set<Command> s = (Set<Command>) commandPlugin.getClass().getMethod("getCommands", User.class).invoke(commandPlugin, user);
                        for (Command c : s)
                            r += c.getName() + "; ";
                        user.sendMessage(r);
                    } else {
                        channel.sendMessage("The following commands are loaded for " + channel.getName() + ":");
                        String r = "";
                        Set<Command> s = (Set<Command>) commandPlugin.getClass().getMethod("getCommands", Channel.class).invoke(commandPlugin, channel);
                        for (Command c : s)
                            r += c.getName() + "; ";
                        channel.sendMessage(r);
                    }

                /* Command: !command load
                 * Loads the command temporarily for the current user or channel
                 */
                } else if (arguments.get(1).toLowerCase().equals("load")) {
                    if (arguments.size() == 2) {
                        this.error(channel, user);
                    } else {
                        Object o = null;
                        try {
                        	o = commandPlugin.getClass().getMethod("createCommand", String.class).invoke(commandPlugin, arguments.get(2));
                        } catch (InvocationTargetException e) { }
                        
                        if (o == null) {
                            Command.sendMessage(channel, user,
                                    Colors.BOLD + Colors.RED + "Loading plugin failed.");
                        } else {
                            Command c = (Command) o;
                            c.init(commandPlugin, -1, arguments.get(2), false, false);
                            if (channel == null) {
                                commandPlugin.getClass().getMethod("register", Command.class, User.class).invoke(commandPlugin, c, user);
                            } else {
                                commandPlugin.getClass().getMethod("register", Command.class, Channel.class).invoke(commandPlugin, c, channel);
                            }
                            Command.sendMessage(channel, user, "Command loaded. For permanent use, please use 'add'");
                        }
                    }

                /* Command: !command add
                 * Loads the command permanent for the current user or channel
                 */
                } else if (arguments.get(1).toLowerCase().equals("add")) {
                    if (arguments.size() == 2) {
                        this.error(channel, user);
                    } else {
                        Command.sendMessage(channel, user, Colors.BOLD + Colors.RED + "Not implemented yet.");
                    }

                } else if (arguments.get(1).toLowerCase().equals("reload")) {
                    final Channel c = channel;
                    final User u = user;
                    final Plugin cp = commandPlugin;
                    (new Thread() {
                        @Override
                        public void run() {
                            Command.sendMessage(c, u, "Reloading all commands...");
                            try { Thread.sleep(100); } catch (InterruptedException ex) { }

                            try {
                                Set<Command> cmds = new HashSet<Command>((Set<Command>) cp.getClass().getMethod("getCommands").invoke(cp));
                                for (Command c : cmds)
                                    cp.getClass().getMethod("reloadCommand", Command.class).invoke(cp, c);
                            } catch (Exception e) {
                                Command.sendMessage(c, u, Colors.BOLD + Colors.RED + "Fatal error while reloading commands. Got error " + e.getClass().getName() + " " + e.getMessage());
                            }

                            Command.sendMessage(c, u, "All commands are reloaded");
                        }
                    }).start();

                } else {
                    this.error(channel, user);
                }
            }
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CommandCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CommandCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(CommandCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(CommandCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CommandCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean isHandled(Channel channel, User user, CommandMessage msg) {
        return msg.isCommand("command");
    }

    private void error(Channel channel, User user) {
        Command.sendMessage(channel, user, Colors.BOLD + Colors.RED + "Invalid command. Use '!help plugin' for more information.");
    }
}
