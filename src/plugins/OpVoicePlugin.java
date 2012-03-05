package plugins;

import qorebot.Channel;
import qorebot.EventType;
import qorebot.User;
import qorebot.UserLevel;
import qorebot.plugins.Plugin;

/**
 * Plugin that has strong relations to the OpVoiceCommand; when a user joins a
 * channel and it is identified, then it is given op if the user is operator
 * and gives voice to users.
 *
 * This plugin is a perfect example of something simple that can be implemented
 * simply.
 *
 * There's obviously no point in adding this plugin to users, as only a Channel
 * event is handled.
 *
 * @author Ralph Broenink
 */
public class OpVoicePlugin extends Plugin {

    @Override
    public boolean isImplemented(EventType method) {
        return method == EventType.CHANNEL_ONJOIN;
    }

    @Override
    public void onJoin(Channel channel, User sender) {
        if (sender.isIdentified()) {
            if (sender.hasLevel(UserLevel.OPERATOR, channel) && !channel.isOp(sender)) {
                channel.op(sender);
            } else if (sender.hasLevel(UserLevel.USER, channel) && !channel.isOp(sender) && !channel.hasVoice(sender)) {
                channel.voice(sender);
            }
        }
    }

}
