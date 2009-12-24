package org.jjoost.util;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater ;

/**
 * Some default <code>Counter</code> implementations
 * 
 * @author b.elliottsmith
 *
 */
public class Counters {

	/**
	 * Returns a new thread safe <code>Counter</code> which uses compare and set operations to modify its value
	 * @return
	 */
	public static Counter newThreadSafeCounter() {
		return new ThreadSafeCounter() ;
	}
	
	/**
	 * Returns a <code>Counter</code> which ignores all updates to its value and always returns a value less than zero.
	 * @return
	 */
	public static Counter newDoNothingCounter() {
		return new DontCounter() ;
	}
	
	/**
	 * Returns a regular <code>Counter</code> which performs ordinary addition with no concurrent guarantees
	 * @return
	 */
	public static Counter newCounter() {
		return new SerialCounter() ;
	}
	
	public static class ThreadSafeCounter implements Counter {

		private volatile int count ;
		private static final AtomicIntegerFieldUpdater<ThreadSafeCounter> countUpdater = AtomicIntegerFieldUpdater.newUpdater(ThreadSafeCounter.class, "count") ;
		
		@Override
		public boolean add(int i) {
			while (true) {
				final int c = count ;
				final int n = c + i ;
				if (n < 0)
					return false ;
				if (countUpdater.compareAndSet(this, c, n))
					return true ;
			}
		}

		@Override
		public int get() {
			return count ;
		}

		@Override
		public Counter newInstance() {
			return new ThreadSafeCounter() ;
		}

	}
	
	public static class SerialCounter implements Counter {

		private int count ;
		
		@Override
		public boolean add(int i) {
			final int n = count + i ;
			if (n >= 0) {
				count = n ;
				return true ;
			}
			return false ;
		}

		@Override
		public int get() {
			return count ;
		}
		
		@Override
		public Counter newInstance() {
			return new SerialCounter() ;
		}
		
	}
	
	public static class DontCounter implements Counter {
		@Override
		public boolean add(int i) {
			return true ;
		}
		@Override
		public int get() {
			return Integer.MIN_VALUE ;
		}
		@Override
		public Counter newInstance() {
			return new DontCounter() ;
		}
	}
	
}
