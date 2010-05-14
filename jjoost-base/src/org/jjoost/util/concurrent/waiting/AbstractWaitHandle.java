package org.jjoost.util.concurrent.waiting;

import java.util.concurrent.TimeUnit;

public abstract class AbstractWaitHandle implements WaitHandle {
	
	protected final Thread thread ;

	protected AbstractWaitHandle(Thread thread) {
		this.thread = thread;
	}
	
	@Override
	public final void await(long time, TimeUnit units) throws InterruptedException {
		awaitUntil(System.currentTimeMillis() + units.toMillis(time)) ;
	}
	
	@Override
	public final void await() throws InterruptedException {
		while (stillWaiting()) {
			if (thread.isInterrupted()) {
				close() ;
				throw new InterruptedException() ;
			}
			pause() ;
		}
		close() ;
	}
	
	@Override
	public final void awaitUntil(long until) throws InterruptedException {
		while (System.currentTimeMillis() < until && stillWaiting()) {
			if (thread.isInterrupted()) {
				close() ;
				throw new InterruptedException() ;
			}
			pauseUntil(until) ;
		}
		close() ;
	}
	
	@Override
	public final void awaitNanos(long nanos) throws InterruptedException {
		long now = System.nanoTime() ;
		final long start = now ;
		while (now - start < nanos && stillWaiting()) {
			if (thread.isInterrupted()) {
				close() ;
				throw new InterruptedException() ;
			}
			pauseNanos(nanos - (now - start)) ;
			now = System.nanoTime() ;
		}
		close() ;
	}
	
	@Override
	public final void awaitUninterruptibly(long time, TimeUnit units) {
		awaitUntilUninterruptibly(System.currentTimeMillis() + units.toMillis(time)) ;
	}
	
	@Override
	public final void awaitUninterruptibly() {
		while (stillWaiting())
			pause() ;		
		close() ;
	}
	
	@Override
	public final void awaitUntilUninterruptibly(long until) {
		while (System.currentTimeMillis() < until && stillWaiting())
			pauseUntil(until) ;		
		close() ;
	}
	
	@Override
	public final void awaitNanosUninterruptibly(long nanos) {
		long now = System.nanoTime() ;
		final long start = now ;
		while (now - start < nanos && stillWaiting()) {
			pauseNanos(nanos - (now - start)) ;
			now = System.nanoTime() ;
		}
		close() ;
	}

	protected abstract void close() ;
	protected abstract void wake() ;
	protected abstract boolean stillWaiting() ;
	protected abstract void pause() ;
	protected abstract void pauseUntil(long until) ;
	protected abstract void pauseNanos(long nanos) ;

}
