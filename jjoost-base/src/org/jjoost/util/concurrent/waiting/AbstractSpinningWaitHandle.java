package org.jjoost.util.concurrent.waiting;

public abstract class AbstractSpinningWaitHandle extends AbstractWaitHandle {
	
	protected AbstractSpinningWaitHandle(Thread thread) {
		super(thread) ;
	}
	
	protected final void pause() { }
	protected final void pauseUntil(long until) { }
	protected final void pauseNanos(long nanos) { }

}
