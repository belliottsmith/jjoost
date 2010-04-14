package org.jjoost.util.concurrent ;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater ;
import java.util.concurrent.locks.LockSupport ;

import org.jjoost.util.Filter ;

/**
 * A simple thread queue used by concurrent collections implementations to track threads that are waiting on conditions being met (e.g. in
 * the case of the <code>LockFreeHashStore</code>, a hash node being deleted or a bucket being migrated) This class is intended as a base
 * class to be extended to contain state against the waiting thread, indicating in some way the resource it is waiting on. The standard use
 * is to have a final "head" instance never containing a waiting thread, on which <code>insert()</code> is called, by the thread that is to
 * be parked, with a new link (constructed with the calling thread as argument) to be added to the end of the chain. Once the
 * <code>insert()</code> method returns a loop checking the state of the resource the thread is waiting on should be entered, within which
 * (if this check fails) the thread should be put to sleep using <code>LockSupport.park()</code>. Once the loop's condition is met, the
 * thread should call the remove() method on the link it inserted; e.g.
 * <pre> ThreadQueue waitLink = new ThreadQueue(Thead.currentThread()) ;
 * waitingOn.insert(waitLink) ; // waitingOn is head of queue 
 * while ( {resource is locked test} ) { 
 *     LockSupport.park() ; 
 * } 
 * waitLink.remove() ;</pre>
 * <p>
 * The <code>wake(Filter)</code> method provided is intended to act upon this information to wake threads waiting on a now (possibly) free
 * resource. For efficiency it is recommended that a custom wake({condition}) method is written, as the construction/use of a
 * <code>Filter</code> will inherently impede performance. For a relatively small number of waiting threads this implementation performs
 * well, however to accommodate ultra high parallelism it may be worth revisiting with optimisations in the future.
 * 
 * @author b.elliottsmith
 */
public class CommunalThreadQueue<Q extends CommunalThreadQueue<Q>> {
	
	final Thread thread ;
	
	/**
	 * The next ThreadQueue in the chain
	 */
	
	protected volatile Q next ;
	CommunalThreadQueue<Q> prev ;
	boolean removed = false ;
	
	/**
	 * Construct a new ThreadQueue item with the current thread as argument (this should not be used
	 * for construction of the "head" of the queue); new ThreadQueue(null) is the correct constructor. 
	 */
	public CommunalThreadQueue() {
		this(Thread.currentThread()) ;
	}
	
	/**
	 * Construct a new ThreadQueue 
	 * 
	 * @param thread thread that is waiting
	 */
	public CommunalThreadQueue(Thread thread) {
		super();
		this.thread = thread;
	}
	
	/**
	 * Wakes up all threads in this queue 
	 */
	public void wakeAll() {
		CommunalThreadQueue<Q> next = this.next ;
		while (next != null) {
			final CommunalThreadQueue<Q> prev = next ;
			next = next.next ;
			prev.wake() ;
		}
	}
	
	/**
	 * wake up all links after this link on which application of the provided filter's accept()
	 * method returns true 
	 * 
	 * @param wake filter indicating which links should be woken
	 */
	public void wake(Filter<Q> wake) {
		Q next = this.next ;
		while (next != null) {
			if (wake.accept(next)) {
				final Q prev = next ;
				next = next.next ;
				prev.wake() ;
			} else {
				next = next.next ;
			}
		}
	}
	/**
	 * Wake up the thread referenced by this link
	 */
	protected void wake() {
		if (!removed)
			LockSupport.unpark(thread) ;
	}
	
	/**
	 * Remove this link from the chain
	 */
	public void remove() {
		if (removed == true)
			return ;
		CommunalThreadQueue<Q> next ;
		while (true) {
			next = this.next ;
			if (nextUpdater.compareAndSet(this, next, prev))
				break ;
		}
		// we have looped ourselves
		CommunalThreadQueue<Q> prev = this.prev ;
		while (!nextUpdater.compareAndSet(prev, this, next)) {
			prev = prev.next ;
		}
		removed = true ;
	}
	
	/**
	 * Insert a new link to the end of the chain this link is a member of
	 * 
	 * @param insert the link to be inserted at the end of the chain
	 */
	public void insert(Q insert) {
		CommunalThreadQueue<Q> node = this , next = node.next ;
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
	private static final AtomicReferenceFieldUpdater<CommunalThreadQueue, CommunalThreadQueue> nextUpdater = AtomicReferenceFieldUpdater.newUpdater(CommunalThreadQueue.class, CommunalThreadQueue.class, "next") ;
	
}

