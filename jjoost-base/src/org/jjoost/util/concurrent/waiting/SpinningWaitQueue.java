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

/**
 * @author b.elliottsmith
 */
public final class SpinningWaitQueue extends AbstractWaitQueue {

	private final class Node extends AbstractWaitQueue.Node {
		
		private Node(Thread thread, AbstractWaitQueue.Node next) {
			super(thread, next) ;
		}

		@Override protected void wake() { }
		@Override protected void close() { }
		@Override protected void pause() { }
		@Override protected void pauseNanos(long nanos) { }
		@Override protected void pauseUntil(long until) { }
		@Override
		protected boolean stillWaiting() {
			return SpinningWaitQueue.this.stillWaiting(this) ;
		}

	}
	
	protected final Node newNode(Thread thread, AbstractWaitQueue.Node next) {
		return new Node(thread, next) ;
	}
	
	protected final void wake(AbstractWaitQueue.Node list) { }

}