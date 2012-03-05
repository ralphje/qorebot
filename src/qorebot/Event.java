package qorebot;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of a bot event. For every event type, there's a list of
 * accepted (and required) parameters.
 *
 * @author Ralph Broenink
 */
public final class Event {
    private EventType event = EventType.UNKNOWN;
    private Channel channel = null;
    private User user = null;
    private String string1 = null;
    private String string2 = null;
    private int int1;
    private long long1;
    private boolean bool1;
    private org.jibble.pircbot.User[] users = null;

    /**
     * Creates an event without extra info.
     * @param event The event id
     */
    public Event(EventType event) {
        this.event = event;
    }

    /**
     * Creates an event with a given user
     * @param event The event id
     * @param user The user
     */
    public Event(EventType event, User user) {
        this(event);
        this.user = user;
    }
    
    /**
     * Creates an event with a given user and string
     * @param event The event id
     * @param user The user
     * @param string1 The first string
     */
    public Event(EventType event, User user, String string1) {
        this(event, user);
        this.string1 = string1;
    }
    
    /**
     * Creates an event with a given user and two strings
     * @param event The event id
     * @param user The user
     * @param string1 The first string
     * @param string2 The second string
     */
    public Event(EventType event, User user, String string1, String string2) {
        this(event, user, string1);
        this.string2 = string2;
    }


    /**
     * Creates an event with a given channel
     * @param event The event id
     * @param channel The channel
     */
    public Event(EventType event, Channel channel) {
        this(event);
        this.channel = channel;
    }
    
    /**
     * Creates an event with a given channel and user
     * @param event The event id
     * @param channel The channel
     * @param user The user
     */
    public Event(EventType event, Channel channel, User user) {
        this(event);
        this.channel = channel;
        this.user = user;
    }
    
    /**
     * Creates an event with a given channel, user and string
     * @param event The event id
     * @param channel The channel
     * @param user The user
     * @param string1 The first string
     */
    public Event(EventType event, Channel channel, User user, String string1) {
        this(event, channel, user);
        this.string1 = string1;
    }

    /**
     * Creates an event with a given channel, user and integer
     * @param event The event id
     * @param channel The channel
     * @param user The user
     * @param int1 The first integer
     */
    public Event(EventType event, Channel channel, User user, int int1) {
        this(event, channel, user);
        this.int1 = int1;
    }
    
    /**
     * Creates an event with a given channel, user and two strings
     * @param event The event id
     * @param channel The channel
     * @param user The user
     * @param string1 The first string
     * @param string2 The second string
     */
    public Event(EventType event, Channel channel, User user, String string1, String string2) {
        this(event, channel, user, string1);
        this.string2 = string2;
    }
    
    /**
     * Creates an event with a given channel and integer
     * @param event The event id
     * @param channel The channel
     * @param int1 The first integer
     */
    public Event(EventType event, Channel channel, int int1) {
        this(event, channel);
        this.int1 = int1;
    }
    
    /**
     * Creates an event with a given channel, integer and string
     * @param event The event id
     * @param channel The channel
     * @param int1 The first integer
     * @param string1 The first string
     */
    public Event(EventType event, Channel channel, int int1, String string1) {
        this(event, channel, int1);
        this.string1 = string1;
    }
    
    /**
     * Creates an event with a given channel, two strings, a long and a boolean
     * @param event The event id
     * @param channel The channel
     * @param string1 The first string
     * @param string2 The second string
     * @param long1 The first long
     * @param bool1 The first boolean
     */
    public Event(EventType event, Channel channel, String string1, String string2, long long1, boolean bool1) {
        this(event, channel);
        this.string1 = string1;
        this.string2 = string2;
        this.long1 = long1;
        this.bool1 = bool1;
    }
    
    /**
     * Creates an event with a given user array
     * 
     * @param event The event id
     * @param users The user array
     */
    public Event(EventType event, Channel channel, org.jibble.pircbot.User[] users) {
        this(event);
        this.channel = channel;
        this.users = users;
    }
    
    
    
    /**
     * Returns the event type
     */
    public EventType getEvent() {
        if (this.event == EventType.UNKNOWN)
            Logger.getLogger(Event.class.getName()).log(Level.SEVERE,
                    "Requested event type, but event id is not set.");
        return this.event;
    }
    
    /**
     * Returns the channel
     */
    public Channel getChannel() {
        if (this.channel == null)
            Logger.getLogger(Event.class.getName()).log(Level.WARNING,
                    "Requested channel, but channel is not set");
        return this.channel;
    }
    
    /**
     * Returns the user
     */
    public User getUser() {
        if (this.user == null)
            Logger.getLogger(Event.class.getName()).log(Level.WARNING,
                    "Requested user, but user is not set");
        return this.user;
    }
    
    /**
     * Returns the first string
     */
    public String getString1() {
        if (this.string1 == null)
            Logger.getLogger(Event.class.getName()).log(Level.WARNING,
                    "Requested string1, but string1 is not set");
        return this.string1;
    }
    
    /**
     * Returns the second string
     */
    public String getString2() {
        if (this.string2 == null)
            Logger.getLogger(Event.class.getName()).log(Level.WARNING,
                    "Requested string2, but string2 is not set");
        return this.string2;
    }
    
    /**
     * Returns the first integer
     */
    public int getInt1() {
        return this.int1;
    }
    
    /**
     * Returns the first long
     */
    public long getLong1() {
        return this.long1;
    }
    
    /**
     * Returns the first boolean
     */
    public boolean getBool1() {
        return this.bool1;
    }
    
    /**
     * Returns the user list
     */
    public org.jibble.pircbot.User[] getUsers() {
        if (this.users == null)
            Logger.getLogger(Event.class.getName()).log(Level.WARNING,
                    "Requested users, but users is not set");
        return this.users;
    }
}
