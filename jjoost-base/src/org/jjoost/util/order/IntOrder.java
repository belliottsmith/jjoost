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

package org.jjoost.util.order;

/**
 * This class provides functionality on top of comparators, such as sorting, searching and range matching.
 * 
 * @author b.elliottsmith
 */
public final class IntOrder {

    /**
     * Yields the maximum index in the range <code>a[fromIndex, toIndex)</code> containing a value that is less than or equal to the provided key.
     * The method requires (but does not check) that the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); 
     * a result of (fromIndex - 1) indicates no value less than or equal to the key exists in the range
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param key key to find
     * @param fromIndex first index to look in
     * @param toIndex first index to exclude from search (i.e. exclusive upper bound)
     * @return maximum index in the range containing a value that is less than or equal to the provided key 
     */
    public static int floor(final int[] a, final int key, final int fromIndex, final int toIndex) {

        int i = fromIndex - 1;
        int j = toIndex;
        // a[-1] ^= -infinity

        while (i < j - 1) {

            // { a[i] <= v ^ a[j] > v }

            final int m = (i + j) >>> 1;
            final int v = a[m];

            if (v <= key) i = m;
            else j = m;

            // { a[m] > v  =>        a[j] > v        =>      a[i] <= v ^ a[j] > v }
            // { a[m] <= v =>        a[i] <= v       =>      a[i] <= v ^ a[j] > v }

        }
        
        // { a[i] <= v ^ a[i+1] > v }
        return i;

    }
    
    /**
     * Yields the minimum index in the range <code>a[fromIndex, toIndex)</code> containing a value that is greater than or equal to the provided key.
     * The method requires (but does not check) that the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); 
     * a result of toIndex indicates no value greater than or equal to the key exists in the range
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param key key to find
     * @param fromIndex first index to look in
     * @param toIndex first index to exclude from search (i.e. exclusive upper bound)
     * @return minimum index in the range containing a value that is greater than or equal to the provided key 
     */
    public static int ceil(final int[] a, final int key, final int fromIndex, final int toIndex) {

        int i = fromIndex -1;
        int j = toIndex;

        while (i < j - 1) {

            // { a[i] < v ^ a[j] >= v }

            final int m = (i + j) >>> 1;
            final int v = a[m];

            if (v >= key) j = m;
            else i = m;

            // { a[m] >= v  =>        a[j] >= v       =>      a[i] < v ^ a[j] >= v }
            // { a[m] < v   =>        a[i] < v        =>      a[i] < v ^ a[j] >= v }

        }
        return j;

    }

    /**
     * Yields the maximum index in the range <code>a[0, a.length())</code> containing a value that is less than or equal to the provided key.
     * The method requires (but does not check) that the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); 
     * a result of -1 indicates no value less than or equal to the key exists in the range
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param key key to find
     * @return maximum index in the range containing a value that is less than or equal to the provided key 
     */
    public static int floor(int[] a, int key) {
        return floor(a, key, 0, a.length);
    }

    /**
     * Yields the minimum index in the range <code>a[0, a.length())</code> containing a value that is greater than or equal to the provided key.
     * The method requires (but does not check) that the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); 
     * a result of a.length() indicates no value greater than or equal to the key exists in the range
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param key key to find
     * @return minimum index in the range containing a value that is greater than or equal to the provided key 
     */
    public static int ceil(int[] a, int key) {
        return ceil(a, key, 0, a.length);
    }

    /**
     * Returns the maximum of the two provided objects, as determined by this Ordering's Comparator
     * @param a an object
     * @param b an object
     * @return maximum of a and b
     */
    public static int max(int a, int b) {
    	return Math.max(a, b);
    }
    
    /**
     * Returns the minimum of the two provided objects, as determined by this Ordering's Comparator
     * @param a an object
     * @param b an object
     * @return minimum of a and b
     */
    public static int min(int a, int b) {
    	return Math.min(a, b);
    }
    
    /**
     * Returns true if the provided array yields values in ascending order of this Ordering's Comparator
     * @param a an array to test for ordering
     * @return true if in ascending order
     */
    public static boolean isOrdered(int[] a) {
    	if (a.length <= 1)
    		return true;
    	int last = a[0];
    	for (int i = 1 ; i != a.length ; i++) {
    		final int next = a[i];
    		if (last > next)
    			return false;
    		last = next;
    	}
    	return true;
    }
    
    /**
     * Returns true if the provided array yields values in ascending order of this Ordering's Comparator, with no duplicate values
     * @param a an array to test for ordering
     * @return true if in strictly ascending order
     */
    public static boolean isStrictlyOrdered(int[] a) {
    	if (a.length <= 1)
    		return true;
    	int last = a[0];
    	for (int i = 1 ; i != a.length ; i++) {
    		final int next = a[i];
    		if (last >= next)
    			return false;
    		last = next;
    	}
    	return true;
    }

    private static final int LINEAR_INTERSECT_CUTOFF = 3;
    
    /**
     * merges two sorted arrays in time proportional to the size of th
     * 
     * @param <E>
     * @param m1
     * @param m2
     * @return
     */
    public static int[] intersect(int[] m1, int[] m2) {
    	return intersect(m1, 0, m1.length, m2, 0, m2.length);
    }
    
	/**
	 * merges two sorted arrays in time proportional to the size of the result, or the logarithm of the inputs, whichever is greater
	 * 
	 * @param <E>
	 * @param m1
	 * @param m2
	 * @return
	 */
	public static int[] intersect(int[] m1, int s1, int e1, int[] m2, int s2, int e2) {
		final int l1 = e1 - s1;
		final int l2 = e2 - s2;
		if ((l1 == 0) | (l2 == 0)) {
			return new int[0];
		}
		int[] r;
		final int rc;
		final int[][] trg = new int[1][];
		if (l1 < l2) {
			r = m1;
			if (l2 <= LINEAR_INTERSECT_CUTOFF) {
				rc = linearIntersect(m1, s1, e1, m2, s2, e2, null, m1, 0, trg);			
			} else {
				rc = intersect(m1, s1, e1, m2, s2, e2, null, m1, 0, trg);			
			}
		} else {
			r = m2;
			if (l1 <= LINEAR_INTERSECT_CUTOFF) {
				rc = linearIntersect(m2, s2, e2, m1, s1, e1, null, m2, 0, trg);
			} else {
				rc = intersect(m2, s2, e2, m1, s1, e1, null, m2, 0, trg);
			}
		}
		if (trg[0] != null) {
			r = trg[0];
		}
		if (rc != r.length) {
			r = java.util.Arrays.copyOf(r, rc);
		}
		return r;
	}
	
	// assumes arrays are dup free
	public static int intersect(int[] m1, int s1, int e1, int[] m2, int s2, int e2, int[] trg, int count) {
		final int mid1 = s1 + ((e1 - s1) >> 1);
		final int mid1val = m1[mid1];
		final int e21 = floor(m2, mid1val, s2, e2);
		final int s22;
		if (e21 >= s2 && mid1val == m2[e21]) {
			s22 = e21 + 1;
		} else {
			s22 = e21;
		}
		final int len11 = mid1 - s1;
		final int len21 = e21 - s2;
		if ((len11 != 0) & (len21 != 0)) {
			if (len11 < len21) {
				count = intersect(m1, s1, mid1, m2, s2, e21, trg, count);
			} else {
				count = intersect(m2, s2, e21, m1, s1, mid1, trg, count);
			}
		}
		if (e21 != s22) {
			trg[count++] = mid1val;
		}
		final int len12 = e1 - (mid1 + 1);
		final int len22 = e2 - s22;
		if ((len12 != 0) & (len22 != 0)) {
			if (len12 < len22) {
				count = intersect(m1, mid1 + 1, e1, m2, s22, e2, trg, count);
			} else {
				count = intersect(m2, s2, e21, m1, s1, mid1, trg, count);
			}
		}
		return count;
	}
	
	// assumes arrays are dup free
	// trg/src/trgout between them allow us to only allocate a new array if the result is different to the smaller of the sources
	public static int intersect(int[] m1, int s1, int e1, int[] m2, int s2, int e2, int[] trg, int[] src, int count, int[][] trgout) {
		final int mid1 = s1 + ((e1 - s1) >> 1);
		final int mid1val = m1[mid1];
		int e21 = floor(m2, mid1val, s2, e2);
		final int s22 = e21 + 1;
		if (e21 < s2 || mid1val != m2[e21]) {
			e21 = e21 + 1;
		}
		final int len11 = mid1 - s1;
		final int len21 = e21 - s2;
		if ((len11 > 0) & (len21 > 0)) {
			if (len11 < len21) {
				if (len21 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m1, s1, mid1, m2, s2, e21, trg, src, count, trgout);
				} else {
					count = intersect(m1, s1, mid1, m2, s2, e21, trg, src, count, trgout);
				}
			} else {
				if (len11 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m2, s2, e21, m1, s1, mid1, trg, src, count, trgout);
				} else {
					count = intersect(m2, s2, e21, m1, s1, mid1, trg, src, count, trgout);
				}
			}
			if (trg == null && trgout[0] != null) {
				trg = trgout[0];
			}
		}
		if (e21 != s22) {
			if (trg != null) {
				trg[count++] = mid1val;
			} else if (src[count] == mid1val) {
				count++;
			} else {
				trgout[0] = trg = new int[src.length];
				System.arraycopy(src, 0, trg, 0, count);
				trg[count++] = mid1val;
			}
		}
		final int len12 = e1 - (mid1 + 1);
		final int len22 = e2 - s22;
		if ((len12 > 0) & (len22 > 0)) {
			if (len12 < len22) {
				if (len22 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m1, mid1 + 1, e1, m2, s22, e2, trg, src, count, trgout);
				} else {
					count = intersect(m1, mid1 + 1, e1, m2, s22, e2, trg, src, count, trgout);
				}
			} else {
				if (len12 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m2, s22, e2, m1, mid1 + 1, e1, trg, src, count, trgout);
				} else {
					count = intersect(m2, s22, e2, m1, mid1 + 1, e1, trg, src, count, trgout);
				}
			}
		}
		return count;
	}
	
	private static int linearIntersect(int[] m1, int s1, int e1, int[] m2, int s2, int e2, int[] trg, int[] src, int count, int[][] trgout) {
		for (int i = s1, j = s2 ; i != e1 && j != e2 ;) {
			final int v1 = m1[i], v2 = m2[j];
			if (v1 == v2) {
				if (trg != null) {
					trg[count++] = v1;
				} else if (src[count] == v1) {
					count++;
				} else {
					trgout[0] = trg = new int[src.length];
					System.arraycopy(src, 0, trg, 0, count);
					trg[count++] = v1;
				}
				i++; j++;
			} else if (v1 < v2) {
				i++;
			} else {
				j++;
			}
		}
		return count;
	}
}
