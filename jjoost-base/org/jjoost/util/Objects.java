package org.jjoost.util;

import java.util.Comparator;

/**
 * Defines a some simple utility methods for Objects, such as equating them (accounting for nulls, and equating elements of arrays), performing the min/max on Comparable objects, and converting them to a String
 * 
 * @author Benedict Elliott Smith
 */
public class Objects {

    public static <E> int compare(final Comparable<E> a, final E b) {
    	return a == null ? (b == null ? 0 : -1) : (b == null ? 1 : a.compareTo(b)) ; 
    }
    
    public static boolean equalQuick(final Object a, final Object b) {
    	return a == b || ((a != null & b != null) && a.equals(b)) ;
    }
    
	public static boolean equalDeep(Object a, Object b) {
		if (a == b | a == null | b == null)
			return a == b ;
		if (a.equals(b))
			return true ;
		if (!a.getClass().isArray() | !b.getClass().isArray())
			return false ;
		Class<?> atype = a.getClass().getComponentType() ;
		Class<?> btype = b.getClass().getComponentType() ;
		if (atype.isArray() && btype.isArray()) {
			Object[] as = (Object[]) a ;
			Object[] bs = (Object[]) b ;
			boolean eq = as.length == bs.length ;
			for (int i = 0 ; eq & i != as.length ; i++)
				eq = equalDeep(as[i], bs[i]) ;
			return eq ;
		} else if (!atype.isPrimitive()) {
			return !btype.isPrimitive() && equalDeep((Object[]) a, (Object[]) b) ;
		} else if (atype == boolean.class) {
			return btype == boolean.class && java.util.Arrays.equals((boolean[]) a, (boolean[]) b) ;
		} else if (atype == char.class) {
			return btype == char.class && java.util.Arrays.equals((char[]) a, (char[]) b) ;
		} else if (atype == byte.class) {
			return btype == byte.class && java.util.Arrays.equals((byte[]) a, (byte[]) b) ;
		} else if (atype == short.class) {
			return btype == short.class && java.util.Arrays.equals((short[]) a, (short[]) b) ;
		} else if (atype == int.class) {
			return btype == int.class && java.util.Arrays.equals((int[]) a, (int[]) b) ;
		} else if (atype == float.class) {
			return btype == float.class && java.util.Arrays.equals((float[]) a, (float[]) b) ;
		} else if (atype == long.class) {
			return btype == long.class && java.util.Arrays.equals((long[]) a, (long[]) b) ;
		} else if (atype == double.class) {
			return btype == double.class && java.util.Arrays.equals((double[]) a, (double[]) b) ;
		} else {
			throw new IllegalStateException("Shouldn't reach here if we have taken care of all primitive types!") ;
		}
	}
	
	public static boolean equalDeep(Object[] a, Object[] b) {
		if (a.length != b.length)
			return false ;
		for (int i = 0 ; i != a.length ; i++)
			if (!equalDeep(a[i], b[i]))
				return false ;
		return true ;
	}

	@SuppressWarnings("unchecked")
	private static final Comparator<Comparable> COMPARABLE_COMPARATOR = new Comparator<Comparable>() {
		@Override
		public int compare(Comparable o1, Comparable o2) {
			return o1.compareTo(o2) ;
		}
	} ;
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> Comparator<E> getComparableComparator() {
		return (Comparator<E>) COMPARABLE_COMPARATOR ;
	}

	public static String toString(Object o) {
		return o == null ? "null" : o.toString() ;
	}
	
}
