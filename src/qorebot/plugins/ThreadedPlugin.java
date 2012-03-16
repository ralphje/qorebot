package qorebot.plugins;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import qorebot.Event;

/**
 * A plugin that uses a queue to handle all requests to it. When an event is
 * already being handled, any following event will be put in a queue and handled
 * FIFO.
 * 
 * @author Ralph Broenink
 */
public abstract class ThreadedPlugin extends Plugin implements Runnable {
	private BlockingQueue<Event> queue = null;

	/**
	 * Receives an Event from a Channel or User. When this is the first event,
	 * obviously, there isn't a queue created yet. Therefore, it will create one
	 * when neccesary. After that, it will queue the event for handling. If the
	 * queue is full, it will ignore the event.
	 * 
	 * @param e
	 *            The event to handle.
	 */
	@Override
	public void receive(Event e) {
		// this.handlePluginEvent(e);
		if (this.isImplemented(e.getEvent())) {
			if (this.queue == null) {
				this.queue = new LinkedBlockingQueue<Event>();
				new Thread(this).start();
			}
			this.queue.offer(e);
		}
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
				Event e = this.queue.take();
				this.handleEvent(e);
			} catch (InterruptedException ex) {
			}
		}
	}
}
