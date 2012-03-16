package qorebot.plugins.commands;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


import qorebot.Channel;
import qorebot.User;
import qorebot.plugins.commands.message.CommandMessage;
import qorebot.plugins.commands.message.ExtendedMessage;

/**
 * A command that uses a queue to handle all requests to it. When a message is
 * already being handled, any following message will be put in a queue and
 * handled FIFO.
 * 
 * Since the command is executed in a seperate thread, it's by design not
 * possible to return the result to any calling command. Therefore, an
 * implementing class should send any messages by itself.
 * 
 * @author Ralph Broenink
 */
public abstract class ThreadedCommand extends Command implements Runnable {
	private BlockingQueue<ExtendedMessage> queue = null;

	/**
	 * Receives a message from the PluginCommand. When this is the first msg,
	 * obviously, there isn't a queue created yet. Therefore, it will create one
	 * when necessary. After that, it will queue the message for handling. If
	 * the queue is full, it will ignore the event.
	 * 
	 * Since the message is handled concurrent, there is no return value known
	 * upon call.
	 * 
	 * @see Command#receive(qorebot.Channel, qorebot.User,
	 *      qorebot.plugins.commands.message.CommandMessage)
	 */
	@Override
	public String receive(Channel channel, User user, CommandMessage msg) {
		if (this.isHandled(channel, user, msg)) {
			if (this.queue == null) {
				this.queue = new LinkedBlockingQueue<ExtendedMessage>();
				new Thread(this).start();
			}
			this.queue.offer(new ExtendedMessage(channel, user, msg));
		}
		return null;
	}

	/**
	 * This method continues to check whether there's a new item in the queue to
	 * handle. When a new one is available, it will execute the handleEvent-
	 * method.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				ExtendedMessage e = this.queue.take();
				this.handleMessage(e.getChannel(), e.getUser(), e.getMessage());
			} catch (InterruptedException ex) {
			}
		}
	}

	/**
	 * Returns true if the given message is parsed by this command. If this
	 * returns false, handleMessage won't be executed.
	 * 
	 * @param channel
	 *            The channel the message was received on. May be null.
	 * @param user
	 *            The user who sent the message. May not be null.
	 * @param msg
	 *            The sent message. May not be null.
	 * @return True if the given message should be parsed by this command
	 */
	public abstract boolean isHandled(Channel channel, User user, CommandMessage msg);
}
