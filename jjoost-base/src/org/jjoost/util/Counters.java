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

package org.jjoost.util;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Some default <code>Counter</code> implementations
 * 
 * @author b.elliottsmith
 *
 */
public class Counters {

	/**
	 * Returns a new thread safe <code>Counter</code> which uses compare and set operations to modify its value
	 * @return a new thread safe <code>Counter</code> which uses compare and set operations to modify its value
	 */
	public static Counter newThreadSafeCounter() {
		return new ThreadSafeCounter();
	}
	
	/**
	 * Returns a <code>Counter</code> which ignores all updates to its value and always returns a value less than zero.
	 * @return a <code>Counter</code> which ignores all updates to its value and always returns a value less than zero.
	 */
	public static Counter newDoNothingCounter() {
		return new DontCounter();
	}
	
	/**
	 * Returns a regular <code>Counter</code> which performs ordinary addition with no concurrency guarantees
	 * @return a regular <code>Counter</code> which performs ordinary addition with no concurrency guarantees
	 */
	public static Counter newCounter() {
		return new SerialCounter();
	}
	
	/**
	 * a thread safe <code>Counter</code> which uses compare and set operations to modify its value
	 * 
	 * @author b.elliottsmith
	 */
	public static class ThreadSafeCounter implements Counter {

		private volatile int count;
		private static final AtomicIntegerFieldUpdater<ThreadSafeCounter> countUpdater = AtomicIntegerFieldUpdater.newUpdater(ThreadSafeCounter.class, "count");
		
		@Override
		public boolean add(int i) {
			while (true) {
				final int c = count;
				final int n = c + i;
				if (n < 0)
					return false;
				if (countUpdater.compareAndSet(this, c, n))
					return true;
			}
		}

		@Override
		public int get() {
			return count;
		}

		@Override
		public Counter newInstance() {
			return new ThreadSafeCounter();
		}

	}
	
	/**
	 * Returns a regular <code>Counter</code> which performs ordinary addition with no concurrency guarantees
	 * 
	 * @author b.elliottsmith
	 */
	public static class SerialCounter implements Counter {

		private int count;
		
		@Override
		public boolean add(int i) {
			final int n = count + i;
			if (n >= 0) {
				count = n;
				return true;
			}
			return false;
		}

		@Override
		public int get() {
			return count;
		}
		
		@Override
		public Counter newInstance() {
			return new SerialCounter();
		}
		
	}
	
	/**
	 * a <code>Counter</code> which ignores all updates to its value and always returns a value less than zero.
	 * 
	 * @author b.elliottsmith
	 */
	public static class DontCounter implements Counter {
		@Override
		public boolean add(int i) {
			return true;
		}
		@Override
		public int get() {
			return Integer.MIN_VALUE;
		}
		@Override
		public Counter newInstance() {
			return new DontCounter();
		}
	}
	
}
