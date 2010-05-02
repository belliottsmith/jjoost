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

package org.jjoost.util.concurrent ;

import java.util.concurrent.locks.LockSupport;

/**
 * @author b.elliottsmith
 */
public class ExclusiveThreadQueue {
	
	private static final class Node {
		final Thread thread ;
		final Node next ;
		public Node(Thread thread, Node next) {
			this.thread = thread;
			this.next = next;
		}
	}
	
	private Node head ;
	
	private static final long headOffset = Unsafe.fieldOffset(ExclusiveThreadQueue.class, "head") ;
	
	@SuppressWarnings("restriction")
	public void amWaiting() {
		final Thread thread = Thread.currentThread() ;
		Node head = this.head ;
		while (true) {
			final Node newHead = new Node(thread, head) ;
			if (Unsafe.INST.compareAndSwapObject(this, headOffset, head, newHead))
				return ;
			head = (Node) Unsafe.INST.getObjectVolatile(this, headOffset) ;
		}
	}
	
	@SuppressWarnings("restriction")
	public void wakeAll() {
		while (true) {
			Node head = this.head ;
			if (head == null)
				head = (Node) Unsafe.INST.getObjectVolatile(this, headOffset) ;
			if (head == null)
				break ;
			while (true) {
				if (Unsafe.INST.compareAndSwapObject(this, headOffset, head, null))
					break ;
				head = (Node) Unsafe.INST.getObjectVolatile(this, headOffset) ;
			}
			if (head == null)
				break ;
			while (head != null) {
				LockSupport.unpark(head.thread) ;
				head = head.next ;
			}
		}
	}

	@SuppressWarnings("restriction")
	public void wakeAllQuick() {
		while (true) {
			Node head = this.head ;
			if (head == null)
				break ;
			if (!Unsafe.INST.compareAndSwapObject(this, headOffset, head, null))
				return ;
			while (head != null) {
				LockSupport.unpark(head.thread) ;
				head = head.next ;
			}
		}
	}
	
}

