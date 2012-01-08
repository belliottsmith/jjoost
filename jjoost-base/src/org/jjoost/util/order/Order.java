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

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jjoost.util.Objects;

/**
 * This class provides functionality on top of comparators, such as sorting, searching and range matching.
 * 
 * @author b.elliottsmith
 */
public class Order<E> {

	private final Comparator<E> cmp;

	/**
	 * Construct a new Ordering from the provided comparator 
	 * @param comparator the comparator that will define the ordering
	 */
	public Order(Comparator<E> comparator) {
		super();
		this.cmp = comparator;
	}

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
    public int floor(final List<? extends E> a, final E key, final int fromIndex, final int toIndex) {

        int i = fromIndex - 1;
        int j = toIndex;
        // a[-1] ^= -infinity

        while (i < j - 1) {

            // { a[i] <= v ^ a[j] > v }

            final int m = (i + j) >>> 1;
            final E v = a.get(m);

            if (cmp.compare(v, key) <= 0) i = m;
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
    public int ceil(final List<? extends E> a, final E key, final int fromIndex, final int toIndex) {

    	final Comparator<E> comparator = this.cmp;
    	
        int i = fromIndex -1;
        int j = toIndex;

        while (i < j - 1) {

            // { a[i] < v ^ a[j] >= v }

            final int m = (i + j) >>> 1;
            final E v = a.get(m);

            if (comparator.compare(v, key) >= 0) j = m;
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
    public int floor(List<? extends E> a, E key) {
        return floor(a, key, 0, a.size());
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
    public int ceil(List<? extends E> a, E key) {
        return ceil(a, key, 0, a.size());
    }

    
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
    public int floor(final E[] a, final E key, final int fromIndex, final int toIndex) {
    	
    	int i = fromIndex - 1;
    	int j = toIndex;
    	// a[-1] ^= -infinity
    	
    	while (i < j - 1) {
    		
    		// { a[i] <= v ^ a[j] > v }
    		
    		final int m = (i + j) >>> 1;
    		final E v = a[m];
    		
    		if (cmp.compare(v, key) <= 0) i = m;
    		else j = m;
    		
    		// { a[m] > v  =>        a[j] > v        =>      a[i] <= v ^ a[j] > v }
    		// { a[m] <= v =>        a[i] <= v       =>      a[i] <= v ^ a[j] > v }
    		
    	}
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
    public int ceil(final E[] a, final E key, final int fromIndex, final int toIndex) {
    	
    	final Comparator<E> comparator = this.cmp;
    	
    	int i = fromIndex -1;
    	int j = toIndex;
    	
    	while (i < j - 1) {
    		
    		// { a[i] < v ^ a[j] >= v }
    		
    		final int m = (i + j) >>> 1;
    		final E v = a[m];
    		
    		if (comparator.compare(v, key) >= 0) j = m;
    		else i = m;
    		
    		// { a[m] >= v  =>        a[j] >= v       =>      a[i] < v ^ a[j] >= v }
    		// { a[m] < v   =>        a[i] < v        =>      a[i] < v ^ a[j] >= v }
    		
    	}
    	return j;
    	
    }
    
    /**
     * Yields the maximum index in the range <code>a[0, a.length)</code> containing a value that is less than or equal to the provided key.
     * The method requires (but does not check) that the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); 
     * a result of -1 indicates no value less than or equal to the key exists in the range
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param key key to find
     * @return maximum index in the range containing a value that is less than or equal to the provided key 
     */
    public int floor(E[] a, E key) {
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
    public int ceil(E[] a, E key) {
    	return ceil(a, key, 0, a.length);
    }

    /**
     * Returns the first matching key in the provided array, if the array is sorted in ascending order and the key exists
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param key key to find
     * @return a matching value
     */
    public E binarySearch(E[] a, E key) {
    	return binarySearch(a, key, 0, a.length);
    }
    
    /**
     * Returns the first matching key in the provided array in the range a[fromIndex, toIndex), if the array is sorted in ascending order and the key exists
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param fromIndex first index to look in
     * @param toIndex first index to exclude from search (i.e. exclusive upper bound)
     * @param key key to find
     * @return a matching value
     */
    public E binarySearch(E[] a, E key, int fromIndex, int toIndex) {
    	final int floor = floor(a, key);
    	return floor >= fromIndex ? a[floor] : null;
    }
    
    /**
     * Returns the first matching key in the provided list, if the array is sorted in ascending order and the key exists
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param key key to find
     * @return a matching value
     */
    public E binarySearch(List<? extends E> a, E key) {
    	return binarySearch(a, key, 0, a.size());
    }
    
    /**
     * Returns the first matching key in the provided list in the range a[fromIndex, toIndex), if the array is sorted in ascending order and the key exists
     * 
     * @param a list to look in, where this.isOrdered(a) holds
     * @param fromIndex first index to look in
     * @param toIndex first index to exclude from search (i.e. exclusive upper bound)
     * @param key key to find
     * @return a matching value
     */
    public E binarySearch(List<? extends E> a, E key, int fromIndex, int toIndex) {
    	final int floor = floor(a, key);
    	return floor >= fromIndex ? a.get(floor) : null;
    }
    
    /**
     * Returns the maximum of the two provided objects, as determined by this Ordering's Comparator
     * @param a an object
     * @param b an object
     * @return maximum of a and b
     */
    public E max(E a, E b) {
    	return cmp.compare(a, b) > 0 ? b : a;
    }
    
    /**
     * Returns the minimum of the two provided objects, as determined by this Ordering's Comparator
     * @param a an object
     * @param b an object
     * @return minimum of a and b
     */
    public E min(E a, E b) {
    	return cmp.compare(a, b) < 0 ? b : a;
    }
    
    /**
     * Returns the maximum value occurring in the provided Iterator, as determined by this Ordering's Comparator
     * @param iter a set of values to determine the maximum of
     * @return the maximum value of those provided
     */
    public E max(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return null;
    	E max = iter.next();
    	while (iter.hasNext()) {
    		final E next = iter.next();
    		if (cmp.compare(next, max) > 0)
    			max = next;
    	}
    	return max;
    }
    
    /**
     * Returns the minimum value occurring in the provided Iterator, as determined by this Ordering's Comparator
     * @param iter a set of values to determine the minimum of
     * @return the minimum value of those provided
     */
    public E min(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return null;
    	E min = iter.next();
    	while (iter.hasNext()) {
    		final E next = iter.next();
    		if (cmp.compare(next, min) < 0)
    			min = next;
    	}
    	return min;
    }
    
    /**
     * Returns the maximum non-null value occurring in the provided Iterator, as determined by this Ordering's Comparator
     * @param iter a set of values to determine the maximum of
     * @return the maximum non-null value of those provided
     */
    public E maxIgnoreNulls(Iterator<E> iter) {
    	E max = null;
    	while (max == null && iter.hasNext())
    		max = iter.next();
    	while (iter.hasNext()) {
    		final E next = iter.next();
    		if (next != null && cmp.compare(next, max) > 0)
    			max = next;
    	}
    	return max;
    }
    
    /**
     * Returns the minimum non-null value occurring in the provided Iterator, as determined by this Ordering's Comparator
     * @param iter a set of values to determine the minimum of
     * @return the minimum non-null value of those provided
     */
    public E minIgnoreNulls(Iterator<E> iter) {
    	E min = null;
    	while (min == null && iter.hasNext())
    		min = iter.next();
    	while (iter.hasNext()) {
    		final E next = iter.next();
    		if (next != null && cmp.compare(next, min) < 0)
    			min = next;
    	}
    	return min;
    }
    
//    public E max(E a, E b, int treatNullAsCompareResultIfOnLeft) {
//    	final int c;
//    	if (a == null) c = treatNullAsCompareResultIfOnLeft;
//    	else if (b == null) c = -treatNullAsCompareResultIfOnLeft;
//    	else c = cmp.compare(a, b);
//    	return c > 0 ? b : a;
//    }
//    
//    public E min(E a, E b, int treatNullAsCompareResultIfOnLeft) {
//    	final int c;
//    	if (a == null) c = treatNullAsCompareResultIfOnLeft;
//    	else if (b == null) c = -treatNullAsCompareResultIfOnLeft;
//    	else c = cmp.compare(a, b);
//    	return c < 0 ? b : a;
//    }
//    
//    public E min(E a, E b, boolean treatNullAsPositiveInfinity) {
//    	return min(a, b, -1);
//    }
//    
//    public E max(E a, E b, boolean treatNullAsNegativeInfinity) {
//    	return max(a, b, 1);
//    }
//    
    
    /**
     * Returns true if the provided iterator yields values in ascending order of this Ordering's Comparator
     * @param iter an iterator to test for ordering
     * @return true if in ascending order
     */
    public boolean isOrdered(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return true;
    	E last = iter.next();
    	while (iter.hasNext()) {
    		final E next = iter.next();
    		if (!(cmp.compare(last, next) <= 0))
    			return false;
    		last = next;
    	}
    	return true;
    }
    
    /**
     * Returns true if the provided iterable yields values in ascending order of this Ordering's Comparator
     * @param iter an iterable to test for ordering
     * @return true if in ascending order
     */
    public boolean isOrdered(Iterable<E> iter) {
    	return isOrdered(iter.iterator());
    }
    
    /**
     * Returns true if the provided array yields values in ascending order of this Ordering's Comparator
     * @param a an array to test for ordering
     * @return true if in ascending order
     */
    public boolean isOrdered(E[] a) {
    	if (a.length <= 1)
    		return true;
    	E last = a[0];
    	for (int i = 1 ; i != a.length ; i++) {
    		final E next = a[i];
    		if (!(cmp.compare(last, next) <= 0))
    			return false;
    		last = next;
    	}
    	return true;
    }
    
    /**
     * Returns true if the provided iterator yields values in ascending order of this Ordering's Comparator, with no duplicate values
     * @param iter an iterator to test for ordering
     * @return true if in strictly ascending order
     */
    public boolean isStrictlyOrdered(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return true;
    	E last = iter.next();
    	while (iter.hasNext()) {
    		final E next = iter.next();
    		if (!(cmp.compare(last, next) < 0))
    			return false;
    		last = next;
    	}
    	return true;
    }
    
    /**
     * Returns true if the provided iterable yields values in ascending order of this Ordering's Comparator, with no duplicate values
     * @param iter an iterable to test for ordering
     * @return true if in strictly ascending order
     */
    public boolean isStrictlyOrdered(Iterable<E> iter) {
    	return isOrdered(iter.iterator());
    }
    
    /**
     * Returns true if the provided array yields values in ascending order of this Ordering's Comparator, with no duplicate values
     * @param a an array to test for ordering
     * @return true if in strictly ascending order
     */
    public boolean isStrictlyOrdered(E[] a) {
    	if (a.length <= 1)
    		return true;
    	E last = a[0];
    	for (int i = 1 ; i != a.length ; i++) {
    		final E next = a[i];
    		if (!(cmp.compare(last, next) < 0))
    			return false;
    		last = next;
    	}
    	return true;
    }
    
    /**
     * @return this Ordering's Comparator 
     */
    public Comparator<E> getComparator() {
    	return cmp;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
	private static final Order<?> FOR_COMPARABLE = new Order<Comparable>(Objects.<Comparable>getComparableComparator());

	/**
	 * Return an Ordering over Comparable objects of the type specified by the type parameter
	 * @param <E> the type of Comparable the Ordering should be type-safe for
	 * @return an Ordering over objects of type Comparable
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> Order<E> forComparable() {
		return (Order<E>) FOR_COMPARABLE;
	}
	
	/**
	 * Return an Ordering for the provided Comparator
	 * @param cmp the Comparator to back the Ordering by
	 * @return an Ordering for the provided Comparator
	 */
	public Order<E> from(Comparator<E> cmp) {
		return new Order<E>(cmp);
	}

    private static final int LINEAR_INTERSECT_CUTOFF = 3;
    
    /**
     * merges two sorted arrays in time proportional to the size of the result, or the logarithm of the larger input, whichever is greater
     * 
     * @param <E>
     * @param m1
     * @param m2
     * @return
     */
    public E[] intersect(E[] m1, E[] m2) {
    	return intersect(m1, 0, m1.length, m2, 0, m2.length);
    }
    
	/**
	 * merges two sorted arrays in time proportional to the size of the result, or the logarithm of the larger input, whichever is greater
	 * 
	 * @param <E>
	 * @param m1
	 * @param m2
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public E[] intersect(E[] m1, int s1, int e1, E[] m2, int s2, int e2) {
		final int l1 = e1 - s1;
		final int l2 = e2 - s2;
		if ((l1 == 0) | (l2 == 0)) {
			return (E[]) Array.newInstance(m1.getClass().getComponentType(), 0);
		}
		E[] r;
		final int rc;
		final E[][] trg = (E[][]) Array.newInstance(m1.getClass(), 1);
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
	// trg/smaller/trgout between them allow us to only allocate a new array if the result is different to the smaller of the sources (i.e. one is not a subset of the other)
	@SuppressWarnings("unchecked")
	public int intersect(E[] m1, int s1, int e1, E[] m2, int s2, int e2, E[] trg, E[] smaller, int count, E[][] trgout) {
		final int mid1 = s1 + ((e1 - s1) >> 1);
		final E mid1val = m1[mid1];
		int e21 = floor(m2, mid1val, s2, e2);
		final int s22 = e21 + 1;
		if (e21 < s2 || cmp.compare(mid1val, m2[e21]) != 0) {
			e21 = e21 + 1;
		}
		final int len11 = mid1 - s1;
		final int len21 = e21 - s2;
		if ((len11 > 0) & (len21 > 0)) {
			if (len11 < len21) {
				if (len21 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m1, s1, mid1, m2, s2, e21, trg, smaller, count, trgout);
				} else {
					count = intersect(m1, s1, mid1, m2, s2, e21, trg, smaller, count, trgout);
				}
			} else {
				if (len11 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m2, s2, e21, m1, s1, mid1, trg, smaller, count, trgout);
				} else {
					count = intersect(m2, s2, e21, m1, s1, mid1, trg, smaller, count, trgout);
				}
			}
			if (trg == null && trgout[0] != null) {
				trg = trgout[0];
			}
		}
		if (e21 != s22) {
			if (trg != null) {
				trg[count++] = mid1val;
			} else if (smaller[count] == mid1val) {
				count++;
			} else {
				trgout[0] = trg = (E[]) Array.newInstance(m1.getClass().getComponentType(), smaller.length);
				System.arraycopy(smaller, 0, trg, 0, count);
				trg[count++] = mid1val;
			}
		}
		final int len12 = e1 - (mid1 + 1);
		final int len22 = e2 - s22;
		if ((len12 > 0) & (len22 > 0)) {
			if (len12 < len22) {
				if (len22 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m1, mid1 + 1, e1, m2, s22, e2, trg, smaller, count, trgout);
				} else {
					count = intersect(m1, mid1 + 1, e1, m2, s22, e2, trg, smaller, count, trgout);
				}
			} else {
				if (len12 <= LINEAR_INTERSECT_CUTOFF) {
					count = linearIntersect(m2, s22, e2, m1, mid1 + 1, e1, trg, smaller, count, trgout);
				} else {
					count = intersect(m2, s22, e2, m1, mid1 + 1, e1, trg, smaller, count, trgout);
				}
			}
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	private int linearIntersect(E[] m1, int s1, int e1, E[] m2, int s2, int e2, E[] trg, E[] smaller, int count, E[][] trgout) {
		for (int i = s1, j = s2 ; i != e1 && j != e2 ;) {
			final E v1 = m1[i], v2 = m2[j];
			final int d = cmp.compare(v1, v2);
			if (d == 0) {
				if (trg != null) {
					trg[count++] = v1;
				} else if (smaller[count] == v1) {
					count++;
				} else {
					trgout[0] = trg = (E[]) Array.newInstance(m1.getClass().getComponentType(), smaller.length);
					System.arraycopy(smaller, 0, trg, 0, count);
					trg[count++] = v1;
				}
				i++; j++;
			} else if (d < 0) {
				i++;
			} else {
				j++;
			}
		}
		return count;
	}

}
