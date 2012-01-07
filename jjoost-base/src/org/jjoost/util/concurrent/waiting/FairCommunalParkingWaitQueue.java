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

import org.jjoost.util.Equality;

public final class FairCommunalParkingWaitQueue<E> extends FairAbstractCommunalWaitQueue<E> {

	protected static final class Node<E> extends FairAbstractCommunalWaitQueue.Node<E> {

		protected Node(Thread thread, E resource) {
			super(thread, resource);
		}

		@Override
		protected void pause() {
			LockSupport.park();
		}

		@Override
		protected void pauseNanos(long nanos) {
			LockSupport.parkNanos(nanos);
		}

		@Override
		protected void pauseUntil(long until) {
			LockSupport.parkUntil(until);
		}

		@Override
		protected boolean stillWaiting() {
			return waiting != 0;
		}

	}
	
	public FairCommunalParkingWaitQueue() {
		super();
	}

	public FairCommunalParkingWaitQueue(Equality<? super E> equality) {
		super(equality);
	}

	@Override
	protected FairAbstractCommunalWaitQueue.Node<E> newNode( Thread thread, E resource) {
		return new Node<E>(thread, resource);
	}
	
}

