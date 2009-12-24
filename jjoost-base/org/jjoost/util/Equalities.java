package org.jjoost.util;

import java.util.Comparator;

/**
 * Default implementations of <code>Equality</code>.
 * 
 * @author b.elliottsmith
 */
public class Equalities {
	
    /**
     * Standard object equality (a.equals(b))
     * 
     * @return Equality
     */
	public static Equality<Object> object() { return OBJECT ; } 
    private static final Equality<Object> OBJECT = new ObjectEquality() ;
    private static final class ObjectEquality implements Equality<Object> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(Object a, Object b) { return Objects.equalQuick(a, b) ; }
    } ;
    
    /**
     * identity equality (a == b)
     */
	public static Equality<Object> identity() { return IDENTITY ; } 
    private static final Equality<Object> IDENTITY = new IdentityEquality() ;
    private static final class IdentityEquality implements Equality<Object> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	public final boolean equates(Object a, Object b) { return a == b ; }
    } ;

    /**
     * byte[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<byte[]> byteArray() { return BYTE_ARRAY ; } 
    private static final Equality<byte[]> BYTE_ARRAY = new ByteArrayEquality() ;
    private static final class ByteArrayEquality implements Equality<byte[]> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	public final boolean equates(byte[] a, byte[] b) { return java.util.Arrays.equals(a, b) ; }
    } ;
    
    /**
     * int[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<int[]> intArray() { return INT_ARRAY ; } 
    private static final Equality<int[]> INT_ARRAY = new IntArrayEquality() ;
    private static final class IntArrayEquality implements Equality<int[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(int[] a, int[] b) { return java.util.Arrays.equals(a, b) ; }
    } ;

    /**
     * long[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<long[]> longArray() { return LONG_ARRAY ; } 
    private static final Equality<long[]> LONG_ARRAY = new LongArrayEquality() ;
    private static final class LongArrayEquality implements Equality<long[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(long[] a, long[] b) { return java.util.Arrays.equals(a, b) ; }
    } ;

    /**
     * Object[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<Object[]> objectArray() { return OBJECT_ARRAY ; } 
    private static final Equality<Object[]> OBJECT_ARRAY = new ObjectArrayEquality() ;
    private static final class ObjectArrayEquality implements Equality<Object[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(Object[] a, Object[] b) { return java.util.Arrays.equals(a, b) ; }
    } ;
    
    /**
     * Comparator equality
     */
    public static <E> Equality<E> forComparator(Comparator<E> cmp) { return new ComparatorEquality<E>(cmp) ; } 
    private static final class ComparatorEquality<E> implements Equality<E> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	private final Comparator<E> cmp ;
    	public ComparatorEquality(Comparator<E> cmp) {
			this.cmp = cmp;
		}
		public final boolean equates(E a, E b) { return cmp.compare(a, b) == 0 ; }
    } ;
    
}
