package org.jjoost.util.concurrent ;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater ;
import java.util.concurrent.locks.LockSupport ;

public class ThreadQueue<Q extends ThreadQueue<Q>> {
	
	final Thread thread ;
	protected volatile Q next ;
	ThreadQueue<Q> prev ;
	boolean removed = false ;
	
	public ThreadQueue(Thread thread) {
		super();
		this.thread = thread;
	}
	
	public void wakeAll() {
		ThreadQueue<Q> next = this.next ;
		while (next != null) {
			final ThreadQueue<Q> prev = next ;
			next = next.next ;
			prev.wake() ;
		}
	}
	
	protected void wake() {
		if (!removed)
			LockSupport.unpark(thread) ;
	}
	
	protected void remove() {
		if (removed == true)
			return ;
		ThreadQueue<Q> next ;
		while (true) {
			next = this.next ;
			if (nextUpdater.compareAndSet(this, next, prev))
				break ;
		}
		// we have looped ourselves
		ThreadQueue<Q> prev = this.prev ;
		while (!nextUpdater.compareAndSet(prev, this, next)) {
			prev = prev.next ;
		}
		removed = true ;
	}
	
	public void insert(Q insert) {
		ThreadQueue<Q> node = this , next = node.next ;
		while (true) {
			while (next != null) {
				node = next ;
				next = next.next ;
			}
			insert.prev = node ;
			if (nextUpdater.compareAndSet(node, null, insert)) 
				return ;
			next = node.next ;
		}			
	}
	
	@SuppressWarnings("unchecked")
	private static final AtomicReferenceFieldUpdater<ThreadQueue, ThreadQueue> nextUpdater = AtomicReferenceFieldUpdater.newUpdater(ThreadQueue.class, ThreadQueue.class, "next") ;
	
}

