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

}

