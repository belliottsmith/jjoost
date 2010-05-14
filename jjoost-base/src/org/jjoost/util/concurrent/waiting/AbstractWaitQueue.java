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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author b.elliottsmith
 */
public abstract class AbstractWaitQueue implements WaitQueue {

	protected abstract class Node extends AbstractWaitHandle {
		
		protected final Node next ;

		protected Node(Thread thread, Node next) {
			super(thread) ;
			this.next = next;
		}
		
	}

	private volatile Node head;
	private static final AtomicReferenceFieldUpdater<AbstractWaitQueue, Node> headUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractWaitQueue.class, Node.class, "head") ;

	abstract protected Node newNode(Thread thread, Node next) ;
	
	protected boolean stillWaiting(Node test) {
		Node list = head ;
		while (list != null) {
			if (test == list)
				return true ;
			list = list.next ;
		}
		return false ;
	}
	
	public WaitHandle register() {
		final Thread thread = Thread.currentThread();
		while (true) {
			final Node handle = newNode(thread, head) ;
			if (headUpdater.compareAndSet(this, handle.next, handle))
				return handle ;
		}
	}
	
	public void wake() {
		while (true) {
			final Node head = this.head ;
			if (head == null)
				break ;
			if (headUpdater.compareAndSet(this, head, null)) {
				wake(head) ;
				break ;
			}
		}
	}
	
	protected abstract void wake(Node list) ;

}