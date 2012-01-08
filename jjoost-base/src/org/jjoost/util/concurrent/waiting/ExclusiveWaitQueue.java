package org.jjoost.util.concurrent.waiting;

import java.util.concurrent.locks.LockSupport;

/**
 * Superbly named "Single Thread At A Time" Wait Queue - like STWaitQueue, except the owning thread may change over its lifespan, so long as there is never more than one owner
 * 
 * @author b.elliottsmith
 */
public class ExclusiveWaitQueue extends AbstractWaitHandle implements WaitQueue, WaitHandle {

	public ExclusiveWaitQueue(int spinNanos) {
		super(spinNanos);
	}

	public ExclusiveWaitQueue() {
		this(0);
	}

	private volatile int waiting;

	/* (non-Javadoc)
	 * @see continuum.concurrency.WaitQueue#waitHandle()
	 */
	@Override
	public WaitHandle register() {
		if (!THREAD.compareAndSet(this, null, Thread.currentThread())) {
			throw new IllegalStateException("There is already a thread waiting on this queue, and only one thread may use it at a time");
		}
		waiting = 1;
		return this;
	}

	@Override
	public void wakeOne() {
		final Thread t = thread;
		if (t != null) {
			waiting = 0;
			LockSupport.unpark(t);
		}
	}

	@Override
	public boolean waiting() {
		return waiting != 0;
	}

	@Override
	public void wakeAll() {
		wakeOne();
	}

	@Override
	public void cancel() {
		thread = null;
	}
	
	@Override
	public boolean valid() {
		return thread == Thread.currentThread();
	}
	
}