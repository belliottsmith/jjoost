package org.joost.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jjoost.collections.ListMapNesting ;
import org.jjoost.collections.MapMaker ;
import org.jjoost.collections.base.HashStoreType ;
import org.jjoost.collections.maps.concurrent.LockFreeScalarHashMap ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Hashers ;
import org.jjoost.util.Rehashers ;

public class TempHashMapTest2 {

	// actual thread count is thread count * 3!
	static final int threadCount = 3 ;
	static final int itemCount = 1 << 16 ;
	static final int bucketCount = 1 << 10 ;
//	static final int bucketCount = 1 << 19 ;
//	static final int myConcurrencyLevel = 1 << 8 ;
	static final int jdkConcurrencyLevel = 1 << 8 ;
	static final float loadFactor = 1000f ;
//	static final float loadFactor = 10000.75f ;
	static final long startTime = System.currentTimeMillis() ;
	static final Long[] toInsert ;
	static final boolean[] toDelete ;
	static final ExecutorService internal = Executors.newFixedThreadPool(threadCount * 3) ;
	static final ExecutorCompletionService<String> exec = new ExecutorCompletionService<String>(internal) ;
	
	static {
		final Random rnd = new Random(0) ;
		final HashSet<Long> alreadyUsing = new HashSet<Long>() ;
		toInsert = new Long[itemCount] ;
		toDelete = new boolean[itemCount] ;		
		for (int i = 0; i != itemCount ; i++) {
			Long v = rnd.nextLong() ;
			while (!alreadyUsing.add(v)) {
				v = rnd.nextLong() ;
			}
			toInsert[i] = v ;
		}
		int bc = 0 ; long bs = 0 ;
		int delCount = 0 ;
		for (int i = 0 ; i != itemCount ; i++) {
			if (bc == 0) {
				bs = rnd.nextLong() ;
				bc = 64 ;
			}
			if ((bs & 1) == 1) {
				toDelete[i] = true ;
				delCount++ ;
			}			
			bs >>= 1 ;
			bc-- ;
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		// run once to compile code
		doJDK(-1) ;
		doMine(-1) ;
		double sum = 0 ;
		double sumsq = 0 ;
		final int c = 10 ;
		for (int i = 0 ; i != c ; i++) {
			final long len = doJDK(i) ;
			sum += len ;
			sumsq += (len * len) ;
		}
		internal.shutdownNow() ;
		System.out.println("avg ms: " + (sum / c)) ;
		System.out.println("stdev : " + stddev(sum, sumsq, c)) ;
	}

	public static void tryInfiniteLoopManyTimes() throws InterruptedException, ExecutionException {
		for (int i = 0 ; i != 10000 ; i++) {
			System.out.println("Run " + i) ;
			tryInfiniteLoop() ;
		}
	}
	public static long doMine(int run) throws InterruptedException, ExecutionException {
		final long start = System.currentTimeMillis() ;
		final LockFreeScalarHashMap<Long, Long> map = new LockFreeScalarHashMap<Long, Long>(bucketCount == 1 ? jdkConcurrencyLevel : bucketCount, loadFactor) ;
		final List<Future<String>> results = new ArrayList<Future<String>>() ;
		final AtomicIntegerArray putLimit = new AtomicIntegerArray(threadCount) ;
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i ;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T ;
					final int endi = (itemCount / threadCount) * (T + 1) ;
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i] ;
						if (map.put(v, v) != null) {
							System.out.println("failed to put value " + (v)) ;
							throw new IllegalStateException() ;
						}
						if (i % 10 == 0)
							putLimit.set(T, i + 1) ;
					}
					putLimit.set(T, endi) ;
				}
			}, "inserter " + i)) ;
		}
		final AtomicIntegerArray deleteLimit = new AtomicIntegerArray(threadCount) ;
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i ;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T ;
					final int endi = (itemCount / threadCount) * (T + 1) ;
					for (int i = starti ; i != endi ; i++) {
						while (putLimit.get(T) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (toDelete[i]) {
							final Long v = toInsert[i] ;
							if (map.remove(v) == 0) {
								System.out.println("failed to delete expected value " + v) ;
								throw new IllegalStateException() ;
							}
						}
						if (i % 10 == 0)
							deleteLimit.set(T, i + 1) ;
					}
					deleteLimit.set(T, endi) ;
				}
			}, "deleter " + i)) ;
		}
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i ;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T ;
					final int endi = (itemCount / threadCount) * (T + 1) ;
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i] ;
						final boolean expect = !toDelete[i] ;
						int limit ;
						while ((limit = deleteLimit.get(T)) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (expect && map.get(v) == null) {
							System.out.println(v + " disappeared when it shouldn't have (record " + i + " of " + endi + ", limit " + limit + ")") ;
							throw new IllegalStateException() ;
						}
						if (!expect && map.get(v) != null) {
							System.out.println(v + " still present when it shouldn't be (record " + i + " of " + endi + ", limit " + limit + ")") ;
							throw new IllegalStateException() ;
						}
					}
				}
			}, "deleter " + i)) ;
		}
		for (int i = 0 ; i != threadCount * 3 ; i++) {
			exec.take().get() ;
		}
		final long finish = System.currentTimeMillis() ;
		final long secondsSinceAppStartup = (finish - startTime) / 1000 ;
		System.out.println("Mine: " + (finish - start) + "ms @ " + String.format("% 2d:% 2d   (run %4d)", secondsSinceAppStartup / 60, secondsSinceAppStartup % 60, run)) ;
		return finish - start ;
	}
	public static long doJDK(int run) throws InterruptedException, ExecutionException {
		final long start = System.currentTimeMillis() ;
		final ConcurrentHashMap<Long, Long> map = new ConcurrentHashMap<Long, Long>(bucketCount, (float)loadFactor, jdkConcurrencyLevel) ;
		final List<Future<String>> results = new ArrayList<Future<String>>() ;
		final AtomicIntegerArray putLimit = new AtomicIntegerArray(threadCount) ;
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i ;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T ;
					final int endi = (itemCount / threadCount) * (T + 1) ;
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i] ;
						if (map.put(v, v) != null) {
							System.out.println("failed to put value " + (v)) ;
						}
						if (i % 100 == 0)
							putLimit.set(T, i + 1) ;
					}
					putLimit.set(T, endi) ;
				}
			}, "inserter " + i)) ;
		}
		final AtomicIntegerArray deleteLimit = new AtomicIntegerArray(threadCount) ;
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i ;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T ;
					final int endi = (itemCount / threadCount) * (T + 1) ;
					for (int i = starti ; i != endi ; i++) {
						while (putLimit.get(T) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (toDelete[i]) {
							final Long v = toInsert[i] ;
							if (map.remove(v) == null) {
								System.out.println("failed to delete expected value " + v) ;
							}
						}
						if (i % 100 == 0)
							deleteLimit.set(T, i + 1) ;
					}
					deleteLimit.set(T, endi) ;
				}
			}, "deleter " + i)) ;
		}
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i ;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T ;
					final int endi = (itemCount / threadCount) * (T + 1) ;
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i] ;
						final boolean expect = !toDelete[i] ;
						while (deleteLimit.get(T) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (expect && map.get(v) == null) {
							System.out.println(v + " disappeared when it shouldn't have") ;
						}
						if (!expect && map.get(v) != null) {
							System.out.println(v + " still present when it shouldn't be") ;
						}
					}
				}
			}, "deleter " + i)) ;
		}
		for (int i = 0 ; i != threadCount * 3 ; i++) {
			exec.take().get() ;
		}
		final long finish = System.currentTimeMillis() ;
		final long secondsSinceAppStartup = (finish - startTime) / 1000 ;
		System.out.println("JDK: " + (finish - start) + "ms @ " + String.format("% 2d:% 2d   (run %4d)", secondsSinceAppStartup / 60, secondsSinceAppStartup % 60, run)) ;
		return finish - start ;
	}
	
	public static void tryInfiniteLoop() throws InterruptedException {
		final ExecutorService exec = Executors.newSingleThreadExecutor() ;
		exec.submit(new Runnable() {
			@Override
			public void run() {
				try {
					final long start = System.currentTimeMillis() ;
					tryInfiniteLoopInternal() ;
					final long finish = System.currentTimeMillis() ;
					System.out.println("Time elapsed: " + (finish - start) + "ms") ;
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}) ;
		exec.shutdown() ;
		if (!exec.awaitTermination(60, TimeUnit.SECONDS))
			throw new RuntimeException("Took way too long!") ;
	}
	public static void tryInfiniteLoopInternal() throws InterruptedException, ExecutionException {
		final int threadCount = 20 ;
		final int numCount = 10 ;
		final int loopCount = 100000 ;
		final int bucketCount = 1 ;
		final LockFreeScalarHashMap<Integer, Integer> map = new LockFreeScalarHashMap<Integer, Integer>(bucketCount, 10000f) ;
		final ExecutorService internal = Executors.newFixedThreadPool(threadCount) ;
		final ExecutorCompletionService<String> exec = new ExecutorCompletionService<String>(internal) ;
		final Random rnd = new Random() ;
		final List<Future<String>> results = new ArrayList<Future<String>>() ;
		for (int i = 0 ; i != threadCount ; i++) {
			final int I = i ;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					for (int i = 0 ; i != loopCount ; i++) {
						final boolean b = rnd.nextBoolean() ;
						final int v = rnd.nextInt(numCount) ;
						if (b) {
							map.put(v, v) ;
						} else {
							map.remove(v) ;
						}
					}
				}
			}, "runner " + i)) ;
		}
		internal.shutdown() ;
		for (int i = 0 ; i != threadCount ; i++) {
//			System.out.println(exec.take().get() + " completed") ;
			exec.take().get() ;
		}
	}

	
    public static double stddev(double sum, double sumsquares, int num) {
    	return Math.sqrt(var(sum, sumsquares, num)) ;
    }

    /**
     * variance of the numbers with the supplied sum, sum of squares, and count
     * 
     * @param sum
     * @param sumsquares
     * @param num
     * @return
     */    
    public static double var(double sum, double sumsquares, int num) {
    	if (num == 0)
    		return 0 ;    	
    	final double Ex = (sum / num) ;
    	final double Ex2 = sumsquares / num ;
    	final double result = Ex2 - (Ex * Ex) ;
    	// prevent rounding errors from giving a negative variance
    	if (result < 0 && result > -0.0000000001) 
    		return 0 ;
    	else
    		return result ;
    }

}
