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

/**
 * A collection of default implementations of <code>Rehasher</code>
 * 
 * @author b.elliottsmith
 */
public class Rehashers {

	/**
	 * Return a Rehasher which uses the same algorithm as java.util.HashMap
	 * @return a Rehasher which uses the same algorithm as java.util.HashMap
	 */
	public static Rehasher jdkHashmapRehasher() { return JDK_HASHMAP_REHASHER ; }
	private static final Rehasher JDK_HASHMAP_REHASHER = new JDKHashMapRehasher() ;
	private static final class JDKHashMapRehasher implements Rehasher {
		private static final long serialVersionUID = 6015257664082605934L;
		@Override
		public final int rehash(int h) {
	        h ^= (h >>> 20) ^ (h >>> 12);
	        return h ^ (h >>> 7) ^ (h >>> 4);
		}
	}

	/**
	 * Return a Rehasher which uses the same algorithm as java.util.concurrent.ConcurrentHashMap
	 * @return a Rehasher which uses the same algorithm as java.util.concurrent.ConcurrentHashMap
	 */
	public static Rehasher jdkConcurrentHashmapRehasher() { return JDK_CONCURRENT_HASHMAP_REHASHER ; }
	private static final Rehasher JDK_CONCURRENT_HASHMAP_REHASHER = new JDKConcurrentHashMapRehasher() ;
	private static final class JDKConcurrentHashMapRehasher implements Rehasher {
		private static final long serialVersionUID = 6015257664082605934L;
		@Override
		public final int rehash(int h) {
	        h += (h <<  15) ^ 0xffffcd7d;
	        h ^= (h >>> 10);
	        h += (h <<   3);
	        h ^= (h >>>  6);
	        h += (h <<   2) + (h << 14);
	        return h ^ (h >>> 16);
		}
	}
	
	/**
	 * Return a Rehasher that does not modify its input
	 * @return a Rehasher that does not modify its input
	 */
	public static Rehasher identity() { return IDENTITY_REHASHER ; }
	private static final Rehasher IDENTITY_REHASHER = new IdentityRehasher() ;
	private static final class IdentityRehasher implements Rehasher {
		private static final long serialVersionUID = 4870639315784742277L ;
		@Override
		public final int rehash(final int h) {
	        return h ; 
		}
	}

	/**
	 * Return a Rehasher that considers its input as a bit string, and reverses each 4 bit substring within the input
	 * @return a Rehasher that flips each 4 bit substring within the input
	 */
	public static Rehasher flipEveryHalfByte() { return FLIP_HALF_BYTES_REHASHER ; }
	private static final Rehasher FLIP_HALF_BYTES_REHASHER = new FlipHalfBytesRehasher() ;
	private static final class FlipHalfBytesRehasher implements Rehasher {
		private static final long serialVersionUID = -4332293538549708634L ;
		@Override
		public final int rehash(int h) {
			h = Integer.reverse(Integer.reverseBytes(h)) ;
			return ((h >> 4) & 0x0F0F0F0F) | ((h << 4) & 0xF0F0F0F0);
		}
	}
	
	/**
	 * a Rehasher that reverses the bit string of the input
	 * @return a Rehasher that reverses the bit string of the input
	 */
	public static Rehasher flip() { return FLIP_REHASHER ; }
	private static final Rehasher FLIP_REHASHER = new FlipRehasher() ;
	private static final class FlipRehasher implements Rehasher {
		private static final long serialVersionUID = 3842239312530302269L ;
		@Override
		public final int rehash(int h) {
			return Integer.reverse(h) ;
		}
	}
	
	/**
	 * Returns a Rehasher that composes the two provided Rehashers, applying the second argument to the input first, and providing the result of this to the Rehasher provided in the first argument
	 * @param applySecond Rehasher to apply second
	 * @param applyFirst Rehasher to apply first
	 * @return a Rehasher that returns applySecond.rehash(applyFirst.rehash(h))
	 */
	public static Rehasher compose(Rehasher applySecond, Rehasher applyFirst) { return new RehashComposer(applySecond, applyFirst) ; }
	private static final class RehashComposer implements Rehasher {
		private static final long serialVersionUID = 6015257664082605934L;
		private final Rehasher applySecond;
		private final Rehasher applyFirst ;
		public RehashComposer(Rehasher applySecond, Rehasher applyFirst) {
			this.applySecond = applySecond ;
			this.applyFirst = applyFirst ;
		}
		@Override
		public final int rehash(final int h) {
			return applySecond.rehash(applyFirst.rehash(h)) ;
		}
	}
	
}
