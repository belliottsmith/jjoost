package org.jjoost.util;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater ;

public class Counters {

	public static Counter newThreadSafeCounter() {
		return new ThreadSafeCounter() ;
	}
	
	public static Counter newDoNothingCounter() {
		return new DontCounter() ;
	}
	
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

		@Override
		public int getUnsafe() {
			return count ;
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
		
		@Override
		public int getUnsafe() {
			return count ;
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
		@Override
		public int getUnsafe() {
			return Integer.MIN_VALUE ;
		}
	}
	
}
