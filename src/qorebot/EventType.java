package qorebot;

/**
 * Representation of a bot event type. This only lists the different types, for
 * more information about events, see {@link Event}
 *
 * @author Ralph Broenink
 */
public enum EventType {
    /**
     * Called when a User is created.
     *
     * Param: user user
     */
    PLUGIN_ONCREATEUSER,
    /**
     * Called when a Channel is created.
     *
     * Param: channel channel
     */
    PLUGIN_ONCREATECHANNEL,

    
    // -------------------------------------------------------------------------
    
    
    /**
     * This event is called whenever an ACTION is sent from a user.
     * 
     * Param: user    source
     * Param: string1 action
     */
    USER_ONACTION,
    
    /**
     * This event is called whenever someone (possibly us) changes nick on any
     * of the channels that we are on.
     *
     * Param: user    source
     * Param: string1 oldNick
     * Param: string2 newNick
     */
    USER_ONNICKCHANGE,
    
    /**
     * This event is called whenever we receive a notice.
     *
     * Param: user    source
     * Param: string1 notice
     */
    USER_ONNOTICE,

    /**
     * This event is called whenever a private message is sent to the bot.
     *
     * Param: user    source
     * Param: string1 message
     */
    USER_ONPRIVATEMESSAGE,
    
    /**
     * This event is called whenever someone (possibly us) quits from the server
     *
     * Param: user    source
     * Param: string1 reason
     */
    USER_ONQUIT,
    
    /**
     * Called when the mode of the current user is set.
     *
     * Param: user    source
     * Param: string1 mode
     */
    USER_ONUSERMODE,

    
    // -------------------------------------------------------------------------

    
    /**
     * This event is called whenever an ACTION is sent from a user.
     *
     * Param: channel channel
     * Param: user    sender
     * Param: string1 action
     */
    CHANNEL_ONACTION,
    
    /**
     * After calling the listChannels() method in PircBot, the server will start
     * to send us information about each channel on the server.
     *
     * Param: channel channel
     * Param: int1    userCount
     * Param: string1 topic
     */
    CHANNEL_ONCHANNELINFO,
    
    /**
     * Called when a user (possibly us) gets operator status taken away.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 recipient
     */
    CHANNEL_ONDEOP,
    
    /**
     * Called when a user (possibly us) gets voice status removed.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 recipient
     */
    CHANNEL_ONDEVOICE,
    
    /**
     * Called when we are invited to a channel by a user.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONINVITE,
    
    /**
     * This event is called whenever someone (possibly us) joins a channel
     * which we are on.
     *
     * Param: channel channel
     * Param: user    sender
     */
    CHANNEL_ONJOIN,
    
    /**
     * This event is called whenever someone (possibly us) is kicked from any
     * of the channels that we are in.
     *
     * Param: channel channel
     * Param: user    kicker
     * Param: string1 recipientNick
     * Param: string2 reason
     */
    CHANNEL_ONKICK,
    
    /**
     * This method is called whenever a message is sent to a channel.
     *
     * Param: channel channel
     * Param: user    sender
     * Param: string1 message
     */
    CHANNEL_ONMESSAGE,
    
    /**
     * Called when the mode of a channel is set.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 mode
     */
    CHANNEL_ONMODE,
    
    /**
     * This event is called whenever someone (possibly us) changes nick on any
     * of the channels that we are on.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 oldNick
     * Param: string2 newNick
     */
    CHANNEL_ONNICKCHANGE,
    
    /**
     * This event is called whenever we receive a notice.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 notice
     */
    CHANNEL_ONNOTICE,
    
    /**
     * Called when a user (possibly us) gets granted operator status for a
     * channel.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 recipient
     */
    CHANNEL_ONOP,
    
    /**
     * This event is called whenever someone (possibly us) parts a channel which
     * we are on.
     *
     * Param: channel channel
     * Param: user    sender
     */
    CHANNEL_ONPART,
    
    /**
     * This event is called whenever someone (possibly us) quits from the server
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 reason
     */
    CHANNEL_ONQUIT,
    
    /**
     * Called when a hostmask ban is removed from a channel.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 hostmask
     */
    CHANNEL_ONREMOVECHANNELBAN,
    
    /**
     * Called when a channel key is removed.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 key
     */
    CHANNEL_ONREMOVECHANNELKEY,
    
    /**
     * Called when the user limit is removed for a channel.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONREMOVECHANNELLIMIT,
    
    /**
     * Called when a channel has 'invite only' removed.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONREMOVEINVITEONLY,
    
    /**
     * Called when a channel has moderated mode removed.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONREMOVEMODERATED,
    
    /**
     * Called when a channel is set to allow messages from any user, even if
     * they are not actually in the channel.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONREMOVENOEXTERNALMESSAGES,
    
    /**
     * Called when a channel is marked as not being in private mode.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONREMOVEPRIVATE,
    
    /**
     * Called when a channel has 'secret' mode removed.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONREMOVESECRET,
    
    /**
     * Called when topic protection is removed for a channel.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONREMOVETOPICPROTECTION,
    
    /**
     * Called when a user (possibly us) gets banned from a channel.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 hostmask
     */
    CHANNEL_ONSETCHANNELBAN,
    
    /**
     * Called when a channel key is set.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 key
     */
    CHANNEL_ONSETCHANNELKEY,
    
    /**
     * Called when a user limit is set for a channel.
     *
     * Param: channel channel
     * Param: user    source
     * Param: int1    limit
     */
    CHANNEL_ONSETCHANNELLIMIT,
    
    /**
     * Called when a channel is set to 'invite only' mode.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONSETINVITEONLY,
    
    /**
     * Called when a channel is set to 'moderated' mode.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONSETMODERATED,
    
    /**
     * Called when a channel is set to only allow messages from users that are
     * in the channel.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONSETNOEXTERNALMESSAGES,
    
    /**
     * Called when a channel is marked as being in private mode.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONSETPRIVATE,
    
    /**
     * Called when a channel is set to be in 'secret' mode.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONSETSECRET,
    
    /**
     * Called when topic protection is enabled for a channel.
     *
     * Param: channel channel
     * Param: user    source
     */
    CHANNEL_ONSETTOPICPROTECTION,
    
    /**
     * This event is called whenever a user sets the topic, or when the bot
     * joins a new channel and discovers its topic.
     *
     * Param: channel channel
     * Param: string1 topic
     * Param: string2 setBy
     * Param: long1   date
     * Param: bool1   changed
     */
    CHANNEL_ONTOPIC,
    
    /**
     * This method is called when we receive a user list from the server after
     * joining a channel.
     *
     * Param: channel channel
     * Param: users   users
     */
    CHANNEL_ONUSERLIST,
    
    /**
     * Called when a user (possibly us) gets voice status granted in a channel.
     *
     * Param: channel channel
     * Param: user    source
     * Param: string1 recipient
     */
    CHANNEL_ONVOICE,

    
    // -------------------------------------------------------------------------

    
    /**
     * Represents 'no event' and is therefore an errorenous state.
     */
    UNKNOWN;
}
