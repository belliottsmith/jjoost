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

import java.util.concurrent.locks.LockSupport;

import org.jjoost.util.concurrent.atomic.AtomicRefUpdater;

/**
 * @author b.elliottsmith
 */
public final class UnfairWaitQueue implements WaitQueue, WaitSignal {

	protected final class Node extends AbstractWaitHandle {
		
		protected final Node next;
		public Node(int spinNanos, Thread thread, Node next) {
			super(spinNanos, thread);
			this.next = next;
		}
		
		@Override
		public void cancel() { 
			thread = null;
		}
		
		@Override
		public boolean waiting() {
			return thread != null;
		}
		
		@Override
		public boolean valid() {
			return thread != null;
		}

	}

	private static final AtomicRefUpdater<UnfairWaitQueue, Node> headUpdater = AtomicRefUpdater.get(UnfairWaitQueue.class, Node.class, "head");
	
	private volatile Node head;
	private final int spinNanos;
	public UnfairWaitQueue() {
		this(0);
	}
	public UnfairWaitQueue(int spinNanos) {
		this.spinNanos = spinNanos;
	}

	public WaitHandle register() {
		final Thread thread = Thread.currentThread();
		while (true) {
			final Node handle = new Node(spinNanos, thread, head);
			if (headUpdater.compareAndSet(this, handle.next, handle))
				return handle;
		}
	}
	
	public void wakeAll() {
		while (true) {
			Node head = this.head;
			if (head == null)
				break;
			if (headUpdater.compareAndSet(this, head, null)) {
				while (head != null) {
					final Thread t = head.thread;
					if (t != null) {
						head.thread = null;
						LockSupport.unpark(t);
					}
					head = head.next;
				}
				break;
			}
		}
	}
	
	public void wakeOne() {
		while (true) {
			final Node head = this.head;
			if (head == null)
				break;
			if (headUpdater.compareAndSet(this, head, head.next)) {
				final Thread t = head.thread;
				if (t != null) {
					head.thread = null;
					LockSupport.unpark(t);
					break;
				}
			}
		}
	}
	
}