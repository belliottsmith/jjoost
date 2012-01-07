package org.jjoost.collections;

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

import org.jjoost.collections.maps.concurrent.HashLockHashMap;
import org.jjoost.collections.maps.concurrent.LockFreeHashMap;

public class ConcurrencySpeedTest {

	// actual thread count is thread count * 3!
	static final int itemCount = 1 << 21;

	//	static final int threadCount = 3;
//	static final int bucketCount = 1 << 10;
//	static final int bucketCount = 1 << 19;
//	static final int myConcurrencyLevel = 1 << 8;
//	static final int jdkConcurrencyLevel = 1 << 8;
//	static final float loadFactor = 1000f;
//	static final float loadFactor = 10000.75f;
	static final long startTime = System.currentTimeMillis();
	static final Long[] toInsert;
	static final boolean[] toDelete;
//	static ExecutorService internal = Executors.newFixedThreadPool(threadCount * 3);
//	static final ExecutorCompletionService<String> exec = new ExecutorCompletionService<String>(internal);
	
	static {
		final Random rnd = new Random(0);
		final HashSet<Long> alreadyUsing = new HashSet<Long>();
		toInsert = new Long[itemCount];
		toDelete = new boolean[itemCount];
		for (int i = 0; i != itemCount ; i++) {
			Long v = rnd.nextLong();
			while (!alreadyUsing.add(v)) {
				v = rnd.nextLong();
			}
			toInsert[i] = v;
		}
		int bc = 0 ; long bs = 0;
		int delCount = 0;
		for (int i = 0 ; i != itemCount ; i++) {
			if (bc == 0) {
				bs = rnd.nextLong();
				bc = 64;
			}
			if ((bs & 1) == 1) {
				toDelete[i] = true;
				delCount++;
			}			
			bs >>= 1;
			bc--;
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService internal = Executors.newFixedThreadPool(15);
		ExecutorCompletionService<String> exec = new ExecutorCompletionService<String>(internal);
		double sum = 0;
		double sumsq = 0;
		final int c = 10;
		for (int i = -5 ; i != c ; i++) {
//			final long len = doJDK(exec, i, itemCount, 10, 1000, 4096, 0.75f);
			final long len = doJjoost(exec, i, itemCount, 10, 1000, 0.75f);
			if (i >= 0) {
				sum += len;
				sumsq += (len * len);
			}
		}
		internal.shutdownNow();
		System.out.println("avg ms: " + (sum / c));
		System.out.println("stdev : " + stddev(sum, sumsq, c));
	}

	public static long doJjoost(ExecutorCompletionService<String> exec, final int run, final int itemCount, final int threadCount, final int bucketCount, final float loadFactor) throws InterruptedException, ExecutionException {
		final long start = System.currentTimeMillis();
//		final HashLockHashMap<Long, Long> map = new HashLockHashMap<Long, Long>(bucketCount, loadFactor);
		final LockFreeHashMap<Long, Long> map = new LockFreeHashMap<Long, Long>(bucketCount, loadFactor);
		final List<Future<String>> results = new ArrayList<Future<String>>();
		final AtomicIntegerArray putLimit = new AtomicIntegerArray(threadCount);
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T;
					final int endi = (itemCount / threadCount) * (T + 1);
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i];
						if (map.put(v, v) != null) {
							System.out.println("failed to put value " + (v));
							throw new IllegalStateException();
						}
						if (i % 10 == 0)
							putLimit.set(T, i + 1);
					}
					putLimit.set(T, endi);
				}
			}, "inserter " + i));
		}
		final AtomicIntegerArray deleteLimit = new AtomicIntegerArray(threadCount);
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T;
					final int endi = (itemCount / threadCount) * (T + 1);
					for (int i = starti ; i != endi ; i++) {
						while (putLimit.get(T) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (toDelete[i]) {
							final Long v = toInsert[i];
							if (map.remove(v) == 0) {
								System.out.println("failed to delete expected value " + v);
								throw new IllegalStateException();
							}
						}
						if (i % 10 == 0)
							deleteLimit.set(T, i + 1);
					}
					deleteLimit.set(T, endi);
				}
			}, "deleter " + i));
		}
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T;
					final int endi = (itemCount / threadCount) * (T + 1);
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i];
						final boolean expect = !toDelete[i];
						int limit;
						while ((limit = deleteLimit.get(T)) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (expect && map.get(v) == null) {
							System.out.println(v + " disappeared when it shouldn't have (record " + i + " of " + endi + ", limit " + limit + ")");
							throw new IllegalStateException();
						}
						if (!expect && map.get(v) != null) {
							System.out.println(v + " still present when it shouldn't be (record " + i + " of " + endi + ", limit " + limit + ")");
							throw new IllegalStateException();
						}
					}
				}
			}, "deleter " + i));
		}
		for (int i = 0 ; i != threadCount * 3 ; i++) {
			exec.take().get();
		}
		final long finish = System.currentTimeMillis();
		final long secondsSinceAppStartup = (finish - startTime) / 1000;
		System.out.println("Mine: " + (finish - start) + "ms @ " + String.format("% 2d:% 2d   (run %4d)", secondsSinceAppStartup / 60, secondsSinceAppStartup % 60, run));
		return finish - start;
	}
	public static long doJDK(ExecutorCompletionService<String> exec, final int run, final int itemCount, final int threadCount, final int bucketCount, final int concurrencyLevel, final float loadFactor) throws InterruptedException, ExecutionException {
		final long start = System.currentTimeMillis();
		final ConcurrentHashMap<Long, Long> map = new ConcurrentHashMap<Long, Long>(bucketCount, (float)loadFactor, concurrencyLevel);
		final List<Future<String>> results = new ArrayList<Future<String>>();
		final AtomicIntegerArray putLimit = new AtomicIntegerArray(threadCount);
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T;
					final int endi = (itemCount / threadCount) * (T + 1);
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i];
						if (map.put(v, v) != null) {
							System.out.println("failed to put value " + (v));
						}
						if (i % 100 == 0)
							putLimit.set(T, i + 1);
					}
					putLimit.set(T, endi);
				}
			}, "inserter " + i));
		}
		final AtomicIntegerArray deleteLimit = new AtomicIntegerArray(threadCount);
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T;
					final int endi = (itemCount / threadCount) * (T + 1);
					for (int i = starti ; i != endi ; i++) {
						while (putLimit.get(T) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (toDelete[i]) {
							final Long v = toInsert[i];
							if (map.remove(v) == null) {
								System.out.println("failed to delete expected value " + v);
							}
						}
						if (i % 100 == 0)
							deleteLimit.set(T, i + 1);
					}
					deleteLimit.set(T, endi);
				}
			}, "deleter " + i));
		}
		for (int i = 0 ; i != threadCount ; i++) {
			final int T = i;
			results.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					final int starti = (itemCount / threadCount) * T;
					final int endi = (itemCount / threadCount) * (T + 1);
					for (int i = starti ; i != endi ; i++) {
						final Long v = toInsert[i];
						final boolean expect = !toDelete[i];
						while (deleteLimit.get(T) <= i) {
							try { Thread.sleep(10) ; } catch (Exception e) { }
						}
						if (expect && map.get(v) == null) {
							System.out.println(v + " disappeared when it shouldn't have");
						}
						if (!expect && map.get(v) != null) {
							System.out.println(v + " still present when it shouldn't be");
						}
					}
				}
			}, "deleter " + i));
		}
		for (int i = 0 ; i != threadCount * 3 ; i++) {
			exec.take().get();
		}
		final long finish = System.currentTimeMillis();
		final long secondsSinceAppStartup = (finish - startTime) / 1000;
		System.out.println("JDK: " + (finish - start) + "ms @ " + String.format("% 2d:% 2d   (run %4d)", secondsSinceAppStartup / 60, secondsSinceAppStartup % 60, run));
		return finish - start;
	}
	
    public static double stddev(double sum, double sumsquares, int num) {
    	return Math.sqrt(var(sum, sumsquares, num));
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
    		return 0;
    	final double Ex = (sum / num);
    	final double Ex2 = sumsquares / num;
    	final double result = Ex2 - (Ex * Ex);
    	// prevent rounding errors from giving a negative variance
    	if (result < 0 && result > -0.0000000001) 
    		return 0;
    	else
    		return result;
    }

}
