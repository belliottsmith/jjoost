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

/**
 * @author b.elliottsmith
 */
public final class UnfairSpinThenParkWaitQueue extends UnfairAbstractWaitQueue {

	public static final int DEFAULT_MAX_SPINS = 1000 ;
	
	private final class Node extends UnfairAbstractWaitQueue.Node {
		
		private int spins ;
		private Node(Thread thread, UnfairAbstractWaitQueue.Node next) {
			super(thread, next) ;
		}

		@Override protected void close() { }
		
		@Override 
		protected void pause() { 
			if (++spins > maxSpins)
				LockSupport.park() ;
		}
		@Override 
		protected void pauseNanos(long nanos) { 
			if (++spins > maxSpins)
				LockSupport.parkNanos(nanos) ;
		}
		
		@Override 
		protected void pauseUntil(long until) { 
			if (++spins > maxSpins)
				LockSupport.parkUntil(until) ;
		}
		
		@Override
		protected boolean stillWaiting() {
			return UnfairSpinThenParkWaitQueue.this.stillWaiting(this) ;
		}

	}

	private final int maxSpins ;	
	public UnfairSpinThenParkWaitQueue() {
		this(DEFAULT_MAX_SPINS) ;
	}

	public UnfairSpinThenParkWaitQueue(int maxSpins) {
		this.maxSpins = maxSpins;
	}

	protected final Node newNode(Thread thread, UnfairAbstractWaitQueue.Node next) {
		return new Node(thread, next) ;
	}
	
	protected final void wakeAll(UnfairAbstractWaitQueue.Node list) {
		while (list != null) {
			LockSupport.unpark(list.thread) ;
			list = list.next ;
		}
	}
	
	protected final void wakeFirst(UnfairAbstractWaitQueue.Node list) {
		LockSupport.unpark(list.thread) ;
	}
	
}