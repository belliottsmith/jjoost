package org.jjoost.util;

import java.util.Comparator;

/**
 * Defines a some simple utility methods for Objects
 * 
 * @author b.elliottsmith
 */
public class Objects {

    /**
	 * Perform simple object equality on the arguments without throwing an error if either argument is <code>null</code>
	 * 
	 * @param a
	 *            an object
	 * @param b
	 *            an object
	 * @return <code>true</code> if <code>a</code> and <code>b</code> are equal, <code>false</code> otherwise
	 */
    public static boolean equalQuick(final Object a, final Object b) {
    	return a == b || ((a != null & b != null) && a.equals(b)) ;
    }
    
	@SuppressWarnings("unchecked")
	private static final Comparator<Comparable> COMPARABLE_COMPARATOR = new Comparator<Comparable>() {
		@Override
		public int compare(Comparable o1, Comparable o2) {
			return o1.compareTo(o2) ;
		}
	} ;
	
	/**
	 * Return a <code>Comparator</code> which delegates to a <code>Comparable</code> object's <code>compareTo()</code> method
	 * 
	 * @return a <code>Comparator</code> which delegates to a <code>Comparable</code> object's <code>compareTo()</code> method
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> Comparator<E> getComparableComparator() {
		return (Comparator<E>) COMPARABLE_COMPARATOR ;
	}

	/**
	 * Convert the provided argument to a <code>String</code>, return "null" of the argument is <code>null</code>
	 * 
	 * @param o
	 *            an object
	 * @return <code>o == null ? "null" : o.toString()</code>
	 */
	public static String toString(Object o) {
		return o == null ? "null" : o.toString() ;
	}
	
}
