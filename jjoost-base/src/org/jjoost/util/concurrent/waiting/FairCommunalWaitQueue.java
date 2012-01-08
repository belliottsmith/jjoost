/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.util.concurrent.waiting;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;

/**
 * A simple thread queue used by concurrent collections implementations to track threads that are waiting on conditions being met (e.g. in
 * the case of the <code>LockFreeHashStore</code>, a hash node being deleted or a bucket being migrated) This class is intended as a base
 * class to be extended to contain state against the waiting thread, indicating in some way the resource it is waiting on. The standard use
 * is to have a final "head" instance never containing a waiting thread, on which <code>insert()</code> is called, by the thread that is to
 * be parked, with a new link (constructed with the calling thread as argument) to be added to the end of the chain. Once the
 * <code>insert()</code> method returns a loop checking the state of the resource the thread is waiting on should be entered, within which
 * (if this check fails) the thread should be put to sleep using <code>LockSupport.park()</code>. Once the loop's condition is met, the
 * thread should call the remove() method on the link it inserted; e.g.
 * <pre> ThreadQueue waitLink = new ThreadQueue(Thead.currentThread());
 * waitingOn.insert(waitLink) ; // waitingOn is head of queue 
 * while ( {resource is locked test} ) { 
 *     LockSupport.park();
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
public abstract class FairCommunalWaitQueue<E> implements CommunalWaitQueue<E>, CommunalWaitSignal<E> {
	
	protected static abstract class Node<E> extends AbstractWaitHandle {
		
		final E resource;
		
		protected Node(Thread thread, E resource) {
			super(thread);
			this.resource = resource;
		}
		
		protected Node(int spinNanos, Thread thread, E resource) {
			super(spinNanos, thread);
			this.resource = resource;
		}

		private volatile Node<E> next;
		private Node<E> prev;
		protected volatile int waiting = 1;
		
		/**
		 * Remove this link from the chain
		 */
		public final void cancel() {
			Node<E> next;
			while (true) {
				next = this.next;
				if (nextUpdater.compareAndSet(this, next, prev))
					break;
			}
			// we have looped ourselves
			Node<E> prev = this.prev;
			while (!nextUpdater.compareAndSet(prev, this, next)) {
				prev = prev.next;
			}
		}
		
		@Override
		public boolean waiting() {
			return waiting != 0;
		}

		@Override
		public boolean valid() {
			return waiting != 0;
		}

		protected boolean tryWake() {
			return waitingUpdater.compareAndSet(this, 1, 0);
		}

		protected void forceWake() {
			waiting = 0;
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private static final AtomicIntegerFieldUpdater<Node> waitingUpdater=  AtomicIntegerFieldUpdater.newUpdater(Node.class, "waiting");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<Node, Node> nextUpdater = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");
	protected abstract Node<E> newNode(Thread thread, E resource);
	
	private final Node<E> head = newNode(null, null);
	private final Equality<? super E> equality;

	public FairCommunalWaitQueue() {
		this(Equalities.object());
	}
	
	public FairCommunalWaitQueue(Equality<? super E> equality) {
		this.equality = equality;
	}

	/**
	 * Wakes up all threads in this queue 
	 */
	public void wakeAll() {
		Node<E> next = head.next;
		while (next != null) {
			final Node<E> prev = next;
			next = next.next;
			prev.forceWake();
		}
	}

	public void wakeOne() {
		while (true) {
			Node<E> next = head.next;
			if (next == null)
				return;
			if (next.tryWake())
				return;
		}
	}
	
	/**
	 * wake up all links after this link on which application of the provided filter's accept()
	 * method returns true 
	 * 
	 * @param wake filter indicating which links should be woken
	 */
	public void wakeAll(E resource) {
		Node<E> next = head.next;
		while (next != null) {
			if (equality.equates(resource, next.resource)) {
				final Node<E> prev = next;
				next = next.next;
				prev.forceWake();
			} else {
				next = next.next;
			}
		}
	}
	
	/**
	 * wake up all links after this link on which application of the provided filter's accept()
	 * method returns true 
	 * 
	 * @param wake filter indicating which links should be woken
	 */
	public void wakeOne(E resource) {
		Node<E> next = head.next;
		while (next != null) {
			if (equality.equates(resource, next.resource)) {
				final Node<E> prev = next;
				next = next.next;
				if (prev.tryWake())
					return;
			} else {
				next = next.next;
			}
		}
	}
	
	public WaitHandle register() {
		return register(null);
	}
	
	public WaitHandle register(E resource) {
		final Node<E> handle = newNode(Thread.currentThread(), resource);
		insert(handle);
		return handle;
	}
	
	/**
	 * Insert a new link to the end of the chain this link is a member of
	 * 
	 * @param insert the link to be inserted at the end of the chain
	 */
	private void insert(Node<E> insert) {
		Node<E> node = head , next = node.next;
		while (true) {
			while (next != null) {
				node = next;
				next = next.next;
			}
			insert.prev = node;
			if (nextUpdater.compareAndSet(node, null, insert)) 
				return;
			next = node.next;
		}			
	}
	

}

