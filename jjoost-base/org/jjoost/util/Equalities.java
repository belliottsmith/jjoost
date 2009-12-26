package org.jjoost.util;

import java.util.Arrays;
import java.util.Map.Entry;

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
    public static class ObjectEquality implements Equality<Object> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(Object a, Object b) { return Objects.equalQuick(a, b) ; }
		public final int hash(Object k) { return k == null ? 0 : k.hashCode() ; }
    } ;
    
    /**
     * identity equality (a == b)
     */
	public static Equality<Object> identity() { return IDENTITY ; } 
    private static final Equality<Object> IDENTITY = new IdentityEquality() ;
    public static class IdentityEquality implements Equality<Object> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	public final boolean equates(Object a, Object b) { return a == b ; }
		public final int hash(Object k) { return System.identityHashCode(k) ; }
    } ;

    /**
     * byte[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<byte[]> byteArray() { return BYTE_ARRAY ; } 
    private static final Equality<byte[]> BYTE_ARRAY = new ByteArrayEquality() ;
    public static class ByteArrayEquality implements Equality<byte[]> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	public final boolean equates(byte[] a, byte[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(byte[] k) { return Arrays.hashCode(k) ; }
    } ;
    
    /**
     * int[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<int[]> intArray() { return INT_ARRAY ; } 
    private static final Equality<int[]> INT_ARRAY = new IntArrayEquality() ;
    public static class IntArrayEquality implements Equality<int[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(int[] a, int[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(int[] k) { return Arrays.hashCode(k) ; }
    } ;

    /**
     * long[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<long[]> longArray() { return LONG_ARRAY ; } 
    private static final Equality<long[]> LONG_ARRAY = new LongArrayEquality() ;
    public static class LongArrayEquality implements Equality<long[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(long[] a, long[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(long[] k) { return Arrays.hashCode(k) ; }
    } ;

    /**
     * Object[] equality, (java.util.Arrays.equals(a, b))
     */
	public static Equality<Object[]> objectArray() { return OBJECT_ARRAY ; } 
    private static final Equality<Object[]> OBJECT_ARRAY = new ObjectArrayEquality() ;
    public static class ObjectArrayEquality implements Equality<Object[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(Object[] a, Object[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(Object[] k) { return Arrays.hashCode(k) ; }
    } ;
    
//    /**
//     * Comparator equality
//     */
//    public static <E> Equality<E> forComparator(Comparator<E> cmp) { return new ComparatorEquality<E>(cmp) ; } 
//    public static class ComparatorEquality<E> implements Equality<E> {
//    	private static final long serialVersionUID = -6611748225612686746L ;
//    	private final Comparator<E> cmp ;
//    	public ComparatorEquality(Comparator<E> cmp) {
//			this.cmp = cmp;
//		}
//		public final boolean equates(E a, E b) { return cmp.compare(a, b) == 0 ; }
//    } ;
//    
    /**
     * Entry equality
     */
    public static <K, V> Equality<Entry<K, V>> forMapEntries(Equality<? super K> keyEq, Equality<? super V> valEq) { return new EntryEquality<K, V>(keyEq, valEq) ; } 
    public static class EntryEquality<K, V> implements Equality<Entry<K, V>> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	protected final Equality<? super K> keyEq ;
    	protected final Equality<? super V> valEq ;
		public EntryEquality(Equality<? super K> keyEq,
				Equality<? super V> valEq) {
			this.keyEq = keyEq;
			this.valEq = valEq;
		}
		@Override
		public final boolean equates(Entry<K, V> a, Entry<K, V> b) {
			return keyEq.equates(a.getKey(), b.getKey()) && valEq.equates(a.getValue(), b.getValue()) ;
		}
		public final int hash(Entry<K, V> o) {
			return keyEq.hash(o.getKey()) ;
		}
		public final Equality<? super K> getKeyEquality() { return keyEq ; }
		public final Equality<? super V> getValueEquality() { return valEq ; }		
    } ;
    
	public static final <V> Equality<V> rehashing(Equality<V> delegate,
			Rehasher rehasher) {
		return new RehashingEquality<V>(delegate, rehasher);
	}

	public static final class RehashingEquality<V> implements Equality<V> {
		private static final long serialVersionUID = -2720117974468199395L;
		private final Equality<V> delegate;
		private final Rehasher rehasher;

		public RehashingEquality(Equality<V> delegate, Rehasher rehasher) {
			super();
			this.delegate = delegate;
			this.rehasher = rehasher;
		}

		@Override
		public int hash(V o) {
			return rehasher.hash(delegate.hash(o));
		}

		@Override
		public boolean equates(V a, V b) {
			return delegate.equates(a, b);
		}
	}

}
