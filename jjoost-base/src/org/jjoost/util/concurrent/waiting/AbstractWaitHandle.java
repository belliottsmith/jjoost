package org.jjoost.util.concurrent.waiting;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.jjoost.util.concurrent.atomic.AtomicRefUpdater;

public abstract class AbstractWaitHandle implements WaitHandle {
	
	protected volatile Thread thread;
	protected final int spinNanos;
	
	protected AbstractWaitHandle(Thread thread) {
		this(0, thread);
	}
	protected AbstractWaitHandle(int spinNanos, Thread thread) {
		this(spinNanos);
		this.thread = thread;
	}
	
	protected AbstractWaitHandle(int spinNanos) {
		this.spinNanos = spinNanos;
	}
	
	private final boolean spinWait(long nanos) {
		if (nanos > 0) {
			final long until = System.nanoTime() + nanos;
			while (System.nanoTime() < until) {
				if (!waiting()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public final void waitForever() throws InterruptedException {
		if (!spinWait(spinNanos)) {
			while (waiting()) {
				if (thread.isInterrupted()) {
					cancel();
					throw new InterruptedException();
				}
				LockSupport.park();
			}
		}
		cancel();
	}

	public final void waitForeverNoInterrupt() {
		while (true) {
			try {
				waitForever();
				return;
			} catch (InterruptedException e) {
			}
		}
	}
	
	public final void waitMillis(long millis) throws InterruptedException {
		waitUntil(System.currentTimeMillis() + millis);
	}

	public final void waitUntil(long until) throws InterruptedException {
		if (!spinWait(spinNanos)) {
			while (System.currentTimeMillis() < until && waiting()) {
				if (thread.isInterrupted()) {
					cancel();
					throw new InterruptedException();
				}
				LockSupport.parkUntil(until);
			}
		}
		cancel();
	}

	public final void waitNanos(long nanos) throws InterruptedException {
		final long start = System.nanoTime();
		if (!spinWait(Math.min(spinNanos, nanos))) {
			long now = start;
			while (now - start < nanos && waiting()) {
				if (thread.isInterrupted()) {
					cancel();
					throw new InterruptedException();
				}
				LockSupport.parkNanos(nanos - (now - start));
				now = System.nanoTime();
			}
		}
		cancel();
	}

	@Override
	public final void waitFor(long time, TimeUnit units) throws InterruptedException {
		waitUntil(System.currentTimeMillis() + units.toMillis(time));
	}
	
	protected static final AtomicRefUpdater<AbstractWaitHandle, Thread> THREAD = AtomicRefUpdater.get(AbstractWaitHandle.class, Thread.class, "thread");

}
