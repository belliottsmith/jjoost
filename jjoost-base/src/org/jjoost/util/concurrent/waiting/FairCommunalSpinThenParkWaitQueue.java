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

package org.jjoost.util.concurrent.waiting ;

import java.util.concurrent.locks.LockSupport;

import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;

public final class FairCommunalSpinThenParkWaitQueue<E> extends FairAbstractCommunalWaitQueue<E> {

	protected static final class Node<E> extends FairAbstractCommunalWaitQueue.Node<E> {

		final int maxSpins ;
		int spins ;
		
		protected Node(Thread thread, E resource, int maxSpins) {
			super(thread, resource) ;
			this.maxSpins = maxSpins ;
		}

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
			return waiting != 0 ;
		}

	}
	
	public static final int DEFAULT_MAX_SPINS = 1000 ;

	private final int maxSpins ;
	
	public FairCommunalSpinThenParkWaitQueue() {
		this(DEFAULT_MAX_SPINS);
	}
	public FairCommunalSpinThenParkWaitQueue(int maxSpins) {
		this(maxSpins, Equalities.object()) ;
	}
	public FairCommunalSpinThenParkWaitQueue(Equality<? super E> equality) {
		this(DEFAULT_MAX_SPINS, equality) ;
	}
	public FairCommunalSpinThenParkWaitQueue(int maxSpins, Equality<? super E> equality) {
		super(equality) ;
		this.maxSpins = maxSpins ;
	}

	@Override
	protected FairAbstractCommunalWaitQueue.Node<E> newNode( Thread thread, E resource) {
		return new Node<E>(thread, resource, maxSpins) ;
	}
	
}

