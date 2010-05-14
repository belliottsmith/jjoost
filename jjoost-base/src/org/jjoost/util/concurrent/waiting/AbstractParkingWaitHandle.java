package org.jjoost.util.concurrent.waiting;

import java.util.concurrent.locks.LockSupport;

public abstract class AbstractParkingWaitHandle extends AbstractWaitHandle {
	
	protected AbstractParkingWaitHandle(Thread thread) {
		super(thread) ;
	}
	
	protected final void pause() { 
		LockSupport.park() ;
	}
	
	protected final void pauseUntil(long until) { 
		LockSupport.parkUntil(until) ;
	}
	
	protected final void pauseNanos(long nanos) { 
		LockSupport.parkNanos(nanos) ;
	}

}
