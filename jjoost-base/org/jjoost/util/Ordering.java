package org.jjoost.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Ordering<E> {

	private final Comparator<E> cmp ;

	public Ordering(Comparator<E> comparator) {
		super();
		this.cmp = comparator;
	}

    /**
     * yields the maximum index in the range <code>a[fromIndex, toIndex)</code> containing a value that is less than or equal to the supplied key.
     * assumes the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of fromIndex - 1 indicates nothing less than or equal to the key exists in the range
     * 
     * @param a array
     * @param key find
     * @param fromIndex
     * @param toIndex 
     * @return
     */
    public int floor(final List<? extends E> a, final E key, final int fromIndex, final int toIndex) {

        int i = fromIndex - 1 ;
        int j = toIndex ;
        // a[-1] ^= -infinity

        while (i < j - 1) {

            // { a[i] <= v ^ a[j] > v }

            final int m = (i + j) >>> 1 ;
            final E v = a.get(m) ;

            if (cmp.compare(v, key) <= 0) i = m ;
            else j = m ;

            // { a[m] > v  =>        a[j] > v        =>      a[i] <= v ^ a[j] > v }
            // { a[m] <= v =>        a[i] <= v       =>      a[i] <= v ^ a[j] > v }

        }
        return i ;

    }
    
    /**
     * yields the minimum index in the range <code>a[fromIndex, toIndex)</code> containing a value that is greater than or equal to the supplied key.
     * assumes the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of toIndex indicates nothing greater than or equal to the key exists in the range
     *   
     * @param a
     * @param key
     * @param c comparator
     * @param fromIndex
     * @param toIndex
     * @return
     */
    public int ceil(final List<? extends E> a, final E key, final int fromIndex, final int toIndex) {

    	final Comparator<E> comparator = this.cmp ;
    	
        int i = fromIndex -1 ;
        int j = toIndex ;

        while (i < j - 1) {

            // { a[i] < v ^ a[j] >= v }

            final int m = (i + j) >>> 1 ;
            final E v = a.get(m) ;

            if (comparator.compare(v, key) >= 0) j = m ;
            else i = m ;

            // { a[m] >= v  =>        a[j] >= v       =>      a[i] < v ^ a[j] >= v }
            // { a[m] < v   =>        a[i] < v        =>      a[i] < v ^ a[j] >= v }

        }
        return j ;

    }

    /**
     * yields the maximum index in the list containing a value that is less than or equal to the supplied key.
     * assumes the list is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of -1 indicates nothing less than or equal to the key exists in the list 
     * 
     * @param a
     * @param key
     * @param c comparator
     * @return
     */
    public int floor(List<? extends E> a, E key) {
        return floor(a, key, 0, a.size()) ;
    }

    /**
     * yields the minimum index in the list containing a value that is greater than or equal to the supplied key.
     * assumes the list is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of a.length indicates nothing greater than or equal to the key exists in the list 
     * 
     * @param a
     * @param key
     * @param c comparator
     * @return
     */
    public int ceil(List<? extends E> a, E key) {
        return ceil(a, key, 0, a.size()) ;
    }

    
    /**
     * yields the maximum index in the range <code>a[fromIndex, toIndex)</code> containing a value that is less than or equal to the supplied key.
     * assumes the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of fromIndex - 1 indicates nothing less than or equal to the key exists in the range
     * 
     * @param a array
     * @param key
     * @param c comparator
     * @param fromIndex
     * @param toIndex
     * @return
     */
    public int floor(final E[] a, final E key, final int fromIndex, final int toIndex) {
    	
    	int i = fromIndex - 1 ;
    	int j = toIndex ;
    	// a[-1] ^= -infinity
    	
    	while (i < j - 1) {
    		
    		// { a[i] <= v ^ a[j] > v }
    		
    		final int m = (i + j) >>> 1 ;
    		final E v = a[m] ;
    		
    		if (cmp.compare(v, key) <= 0) i = m ;
    		else j = m ;
    		
    		// { a[m] > v  =>        a[j] > v        =>      a[i] <= v ^ a[j] > v }
    		// { a[m] <= v =>        a[i] <= v       =>      a[i] <= v ^ a[j] > v }
    		
    	}
    	return i ;
    	
    }
    
    /**
     * yields the minimum index in the range <code>a[fromIndex, toIndex)</code> containing a value that is greater than or equal to the supplied key.
     * assumes the range is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of toIndex indicates nothing greater than or equal to the key exists in the range
     *   
     * @param a
     * @param key
     * @param c comparator
     * @param fromIndex
     * @param toIndex
     * @return
     */
    public int ceil(final E[] a, final E key, final int fromIndex, final int toIndex) {
    	
    	final Comparator<E> comparator = this.cmp ;
    	
    	int i = fromIndex -1 ;
    	int j = toIndex ;
    	
    	while (i < j - 1) {
    		
    		// { a[i] < v ^ a[j] >= v }
    		
    		final int m = (i + j) >>> 1 ;
    		final E v = a[m] ;
    		
    		if (comparator.compare(v, key) >= 0) j = m ;
    		else i = m ;
    		
    		// { a[m] >= v  =>        a[j] >= v       =>      a[i] < v ^ a[j] >= v }
    		// { a[m] < v   =>        a[i] < v        =>      a[i] < v ^ a[j] >= v }
    		
    	}
    	return j ;
    	
    }
    
    /**
     * yields the maximum index in the list containing a value that is less than or equal to the supplied key.
     * assumes the list is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of -1 indicates nothing less than or equal to the key exists in the list 
     * 
     * @param a
     * @param key
     * @param c comparator
     * @return
     */
    public int floor(E[] a, E key) {
    	return floor(a, key, 0, a.length) ;
    }
    
    /**
     * yields the minimum index in the list containing a value that is greater than or equal to the supplied key.
     * assumes the list is sorted in ascending order (and, crucially, that it supports random access - do not use on LinkedList!); a result of a.length indicates nothing greater than or equal to the key exists in the list 
     * 
     * @param a
     * @param key
     * @param c comparator
     * @return
     */
    public int ceil(E[] a, E key) {
    	return ceil(a, key, 0, a.length) ;
    }

    public E binarySearch(E[] a, E key) {
    	return binarySearch(a, key, 0, a.length) ; 
    }
    
    public E binarySearch(E[] a, E key, int fromIndex, int toIndex) {
    	final int floor = floor(a, key) ;
    	return floor >= fromIndex ? a[floor] : null ;
    }
    
    public E binarySearch(List<? extends E> a, E key) {
    	return binarySearch(a, key, 0, a.size()) ; 
    }
    
    public E binarySearch(List<? extends E> a, E key, int fromIndex, int toIndex) {
    	final int floor = floor(a, key) ;
    	return floor >= fromIndex ? a.get(floor) : null ;
    }
    
    public E max(E a, E b) {
    	return cmp.compare(a, b) > 0 ? b : a ;
    }
    
    public E min(E a, E b) {
    	return cmp.compare(a, b) < 0 ? b : a ;
    }
    
    public E max(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return null ;
    	E max = iter.next() ;
    	while (iter.hasNext()) {
    		final E next = iter.next() ;
    		if (cmp.compare(next, max) > 0)
    			max = next ;
    	}
    	return max ;
    }
    
    public E min(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return null ;
    	E min = iter.next() ;
    	while (iter.hasNext()) {
    		final E next = iter.next() ;
    		if (cmp.compare(next, min) < 0)
    			min = next ;
    	}
    	return min ;
    }
    
    public E maxIgnoreNulls(Iterator<E> iter) {
    	E max = null ;
    	while (max == null && iter.hasNext())
    		max = iter.next() ;
    	while (iter.hasNext()) {
    		final E next = iter.next() ;
    		if (next != null && cmp.compare(next, max) > 0)
    			max = next ;
    	}
    	return max ;
    }
    
    public E minIgnoreNulls(Iterator<E> iter) {
    	E min = null ;
    	while (min == null && iter.hasNext())
    		min = iter.next() ;
    	while (iter.hasNext()) {
    		final E next = iter.next() ;
    		if (next != null && cmp.compare(next, min) < 0)
    			min = next ;
    	}
    	return min ;
    }
    
//    public E max(E a, E b, int treatNullAsCompareResultIfOnLeft) {
//    	final int c ;
//    	if (a == null) c = treatNullAsCompareResultIfOnLeft ;
//    	else if (b == null) c = -treatNullAsCompareResultIfOnLeft ;
//    	else c = cmp.compare(a, b) ;
//    	return c > 0 ? b : a ;
//    }
//    
//    public E min(E a, E b, int treatNullAsCompareResultIfOnLeft) {
//    	final int c ;
//    	if (a == null) c = treatNullAsCompareResultIfOnLeft ;
//    	else if (b == null) c = -treatNullAsCompareResultIfOnLeft ;
//    	else c = cmp.compare(a, b) ;
//    	return c < 0 ? b : a ;
//    }
//    
//    public E min(E a, E b, boolean treatNullAsPositiveInfinity) {
//    	return min(a, b, -1) ;
//    }
//    
//    public E max(E a, E b, boolean treatNullAsNegativeInfinity) {
//    	return max(a, b, 1) ;
//    }
//    
    public boolean isOrdered(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return true ;
    	E last = iter.next() ;
    	while (iter.hasNext()) {
    		final E next = iter.next() ;
    		if (!(cmp.compare(last, next) <= 0))
    			return false ;
    		last = next ;
    	}
    	return true ;
    }
    
    public boolean isOrdered(Iterable<E> iter) {
    	return isOrdered(iter.iterator()) ;
    }
    
    public boolean isOrdered(E[] a) {
    	if (a.length <= 1)
    		return true ;
    	E last = a[0] ;
    	for (int i = 1 ; i != a.length ; i++) {
    		final E next = a[i] ;
    		if (!(cmp.compare(last, next) <= 0))
    			return false ;
    		last = next ;
    	}
    	return true ;
    }
    
    public boolean isStrictlyOrdered(Iterator<E> iter) {
    	if (!iter.hasNext())
    		return true ;
    	E last = iter.next() ;
    	while (iter.hasNext()) {
    		final E next = iter.next() ;
    		if (!(cmp.compare(last, next) < 0))
    			return false ;
    		last = next ;
    	}
    	return true ;
    }
    
    public boolean isStrictlyOrdered(Iterable<E> iter) {
    	return isOrdered(iter.iterator()) ;
    }
    
    public boolean isStrictlyOrdered(E[] a) {
    	if (a.length <= 1)
    		return true ;
    	E last = a[0] ;
    	for (int i = 1 ; i != a.length ; i++) {
    		final E next = a[i] ;
    		if (!(cmp.compare(last, next) < 0))
    			return false ;
    		last = next ;
    	}
    	return true ;
    }
    
    public Comparator<E> getComparator() {
    	return cmp ;
    }
    
    @SuppressWarnings("unchecked")
	private static final Ordering FOR_COMPARABLE = new Ordering(Objects.<Comparable>getComparableComparator()) ;

	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> Ordering<E> forComparable() {
		return FOR_COMPARABLE ;
	}
	
	public Ordering<E> from(Comparator<E> cmp) {
		return new Ordering<E>(cmp) ;
	}
	
}
