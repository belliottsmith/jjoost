package org.jjoost.collections ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.jjoost.collections.maps.serial.SerialHashMap;

public class HashSpeedTest {

	// actual thread count is thread count * 3!
	static final int maxItemCount = 1 << 20 ;

	static final long startTime = System.currentTimeMillis() ;
	static final Long[] toInsert ;
	static final boolean[] toDelete ;
	
	static {
		final Random rnd = new Random(0) ;
		final HashSet<Long> alreadyUsing = new HashSet<Long>() ;
		toInsert = new Long[maxItemCount] ;
		toDelete = new boolean[maxItemCount] ;		
		for (int i = 0; i != maxItemCount ; i++) {
			Long v = rnd.nextLong() ;
			while (!alreadyUsing.add(v)) {
				v = rnd.nextLong() ;
			}
			toInsert[i] = v ;
		}
		int bc = 0 ; long bs = 0 ;
		int delCount = 0 ;
		for (int i = 0 ; i != maxItemCount ; i++) {
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
		doJDK(-1, 10000, 1, 100000, 1, false) ;
		doJjoost(-1, 10000, 1, 100000, 1, false) ;
		final int c = 100 ;
		double sum = 0 ;
		double sumsq = 0 ;
		for (int j = 0 ; j != 10 ; j++) {
			sumsq = sum = 0 ;
			for (int i = 0 ; i != c ; i++) {
				final long len = doJDK(i, 1000000, 1, 1000000, 1, false) ;
				sum += len ;
				sumsq += (len * len) ;
			}
			System.out.println("JDK: avg ms= " + (sum / c) + ", stdev= " + stddev(sum, sumsq, c)) ;
			sumsq = sum = 0 ;
			for (int i = 0 ; i != c ; i++) {
				final long len = doJjoost(i, 1000000, 1, 1000000, 1, false) ;
				sum += len ;
				sumsq += (len * len) ;
			}
			System.out.println("Jjoost: avg ms= " + (sum / c) + ", stdev= " + stddev(sum, sumsq, c)) ;
			sumsq = sum = 0 ;
//			for (int i = 0 ; i != c ; i++) {
//				final long len = doJjoostCC(i, 1000000, 1, 1000000, 1, false) ;
//				sum += len ;
//				sumsq += (len * len) ;
//			}
//			System.out.println("Jjoost CC: avg ms= " + (sum / c) + ", stdev= " + stddev(sum, sumsq, c)) ;
		}
	}

	public static long doJjoost(final int run, final int itemCount, final int threadCount, final int bucketCount, final float loadFactor, boolean printStats) throws InterruptedException, ExecutionException {
		final long start = System.currentTimeMillis() ;
		final SerialHashMap<Long, Long> map = new SerialHashMap<Long, Long>(bucketCount, loadFactor) ;
		for (int i = 0 ; i != itemCount ; i++) {
			final Long v = toInsert[i] ;
			if (map.put(v, v) != null) {
				System.out.println("failed to put value " + (v)) ;
				throw new IllegalStateException() ;
			}
		}
		for (int i = 0 ; i != itemCount ; i++) {
			if (toDelete[i]) {
				final Long v = toInsert[i] ;
				if (map.remove(v) == 0) {
					System.out.println("failed to delete expected value " + v) ;
					throw new IllegalStateException() ;
				}
			}
		}
		for (int i = 0 ; i != itemCount ; i++) {
			final Long v = toInsert[i] ;
			final boolean expect = !toDelete[i] ;
			if (expect && map.get(v) == null) {
				System.out.println(v + " disappeared when it shouldn't have (record " + i + ")") ;
				throw new IllegalStateException() ;
			}
			if (!expect && map.get(v) != null) {
				System.out.println(v + " still present when it shouldn't be (record " + i + ")") ;
				throw new IllegalStateException() ;
			}
		}
		final long finish = System.currentTimeMillis() ;
		final long secondsSinceAppStartup = (finish - startTime) / 1000 ;
		if (printStats)
			System.out.println("Jjoost: " + (finish - start) + "ms @ " + String.format("% 2d:% 2d   (run %4d)", secondsSinceAppStartup / 60, secondsSinceAppStartup % 60, run)) ;
		return finish - start ;
	}
//	public static long doJjoostCC(final int run, final int itemCount, final int threadCount, final int bucketCount, final float loadFactor, boolean printStats) throws InterruptedException, ExecutionException {
//		final long start = System.currentTimeMillis() ;
//		final HashLockHashMap<Long, Long> map = new HashLockHashMap<Long, Long>(bucketCount, loadFactor) ;
//		for (int i = 0 ; i != itemCount ; i++) {
//			final Long v = toInsert[i] ;
//			if (map.put(v, v) != null) {
//				System.out.println("failed to put value " + (v)) ;
//				throw new IllegalStateException() ;
//			}
//		}
//		for (int i = 0 ; i != itemCount ; i++) {
//			if (toDelete[i]) {
//				final Long v = toInsert[i] ;
//				if (map.remove(v) == 0) {
//					System.out.println("failed to delete expected value " + v) ;
//					throw new IllegalStateException() ;
//				}
//			}
//		}
//		for (int i = 0 ; i != itemCount ; i++) {
//			final Long v = toInsert[i] ;
//			final boolean expect = !toDelete[i] ;
//			if (expect && map.get(v) == null) {
//				System.out.println(v + " disappeared when it shouldn't have (record " + i + ")") ;
//				throw new IllegalStateException() ;
//			}
//			if (!expect && map.get(v) != null) {
//				System.out.println(v + " still present when it shouldn't be (record " + i + ")") ;
//				throw new IllegalStateException() ;
//			}
//		}
//		final long finish = System.currentTimeMillis() ;
//		final long secondsSinceAppStartup = (finish - startTime) / 1000 ;
//		if (printStats)
//			System.out.println("Jjoost: " + (finish - start) + "ms @ " + String.format("% 2d:% 2d   (run %4d)", secondsSinceAppStartup / 60, secondsSinceAppStartup % 60, run)) ;
//		return finish - start ;
//	}
	public static long doJDK(final int run, final int itemCount, final int threadCount, final int bucketCount, final float loadFactor, boolean printStats) throws InterruptedException, ExecutionException {
		final long start = System.currentTimeMillis() ;
		final HashMap<Long, Long> map = new HashMap<Long, Long>(bucketCount, loadFactor) ;
		for (int i = 0 ; i != itemCount ; i++) {
			final Long v = toInsert[i] ;
			if (map.put(v, v) != null) {
				System.out.println("failed to put value " + (v)) ;
				throw new IllegalStateException() ;
			}
		}
		for (int i = 0 ; i != itemCount ; i++) {
			if (toDelete[i]) {
				final Long v = toInsert[i] ;
				if (map.remove(v) == 0) {
					System.out.println("failed to delete expected value " + v) ;
					throw new IllegalStateException() ;
				}
			}
		}
		for (int i = 0 ; i != itemCount ; i++) {
			final Long v = toInsert[i] ;
			final boolean expect = !toDelete[i] ;
			if (expect && map.get(v) == null) {
				System.out.println(v + " disappeared when it shouldn't have (record " + i + ")") ;
				throw new IllegalStateException() ;
			}
			if (!expect && map.get(v) != null) {
				System.out.println(v + " still present when it shouldn't be (record " + i + ")") ;
				throw new IllegalStateException() ;
			}
		}
		final long finish = System.currentTimeMillis() ;
		final long secondsSinceAppStartup = (finish - startTime) / 1000 ;
		if (printStats)
			System.out.println("JDK: " + (finish - start) + "ms @ " + String.format("% 2d:% 2d   (run %4d)", secondsSinceAppStartup / 60, secondsSinceAppStartup % 60, run)) ;
		return finish - start ;
	}
	
    public static double stddev(double sum, double sumsquares, int num) {
    	return Math.sqrt(var(sum, sumsquares, num)) ;
    }

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
