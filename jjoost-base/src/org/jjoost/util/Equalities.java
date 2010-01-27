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
	 * Returns an Equality for default object equality, delegating to <code>Object.equals()</code> and
	 * <code>Object.hashCode()</code>, but handling nulls gracefully
	 * 
	 * @return standard object <code>Equality</code>
	 */
	public static Equality<Object> object() { return OBJECT ; } 
    private static final Equality<Object> OBJECT = new ObjectEquality() ;
    
    /**
     * Standard object equality, delegating to <code>Object.equals()</code> and <code>Object.hashCode()</code>, but handling nulls
	 * gracefully
	 * 
     * @author b.elliottsmith
     */
    public static class ObjectEquality implements Equality<Object> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(Object a, Object b) { return Objects.equalQuick(a, b) ; }
		public final int hash(Object k) { return k == null ? 0 : k.hashCode() ; }
    } ;
    
    /**
	 * Returns an <code>Equality</code> over <code>String</code> for case insensitive equality (<code>a.equalsIgnoreCase(b)</code>);
	 * <code>hash()</code> is expensive to compute as it performs <code>toLowerCase().hashCode()</code>, however a faster implementation
	 * will be provided that computes an ASCII/UTF-7 only lower case hash in the near future.
	 * 
	 * @return case insensitive <code>String Equality</code>
	 */
    public static Equality<String> caseInsensitive() { return CASE_INSENSITIVE ; } 
    private static final Equality<String> CASE_INSENSITIVE = new CaseInsensitiveEquality() ;
    /**
     * an <code>Equality</code> over <code>String</code> for case insensitive equality (<code>a.equalsIgnoreCase(b)</code>);
	 * <code>hash()</code> is expensive to compute as it performs <code>toLowerCase().hashCode()</code>
	 * 
     * @author b.elliottsmith
     */
    public static class CaseInsensitiveEquality implements Equality<String> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	public final boolean equates(String a, String b) { return a.equalsIgnoreCase(b) ; }
    	public final int hash(String k) { return k.toLowerCase().hashCode() ; }
    } ;
    
    /**
	 * Returns an <code>Equality</code> for identity equality, i.e. like <code>IdentityHashMap</code>. Delegates to language equality (
	 * <code>a == b</code>) and <code>System.identityHashCode()</code>
	 * 
	 * @return identity <code>Equality</code>
	 */
	public static Equality<Object> identity() { return IDENTITY ; } 
    private static final Equality<Object> IDENTITY = new IdentityEquality() ;
    /**
     * an <code>Equality</code> for identity equality, i.e. like <code>IdentityHashMap</code>. Delegates to language equality (
	 * <code>a == b</code>) and <code>System.identityHashCode()
     * 
     * @author b.elliottsmith
     */
    public static class IdentityEquality implements Equality<Object> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	public final boolean equates(Object a, Object b) { return a == b ; }
		public final int hash(Object k) { return System.identityHashCode(k) ; }
    } ;

    /**
	 * Returns an <code>Equality</code> over objects of type <code>byte[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
	 * @return <code>byte[] Equality </code>
	 */
	public static Equality<byte[]> byteArray() { return BYTE_ARRAY ; } 
    private static final Equality<byte[]> BYTE_ARRAY = new ByteArrayEquality() ;
    /**
     * an <code>Equality</code> over objects of type <code>byte[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
     * @author b.elliottsmith
     */
    public static class ByteArrayEquality implements Equality<byte[]> {
    	private static final long serialVersionUID = -6611748225612686746L ;
    	public final boolean equates(byte[] a, byte[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(byte[] k) { return Arrays.hashCode(k) ; }
    } ;
    
    /**
	 * Returns an <code>Equality</code> over objects of type <code>int[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
	 * @return <code>int[] Equality </code>
	 */
	public static Equality<int[]> intArray() { return INT_ARRAY ; } 
    private static final Equality<int[]> INT_ARRAY = new IntArrayEquality() ;
    /**
     * an <code>Equality</code> over objects of type <code>int[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
     * @author b.elliottsmith
     */
    public static class IntArrayEquality implements Equality<int[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(int[] a, int[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(int[] k) { return Arrays.hashCode(k) ; }
    } ;

    /**
	 * Returns an <code>Equality</code> over objects of type <code>long[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
	 * @return <code>long[] Equality </code>
	 */
	public static Equality<long[]> longArray() { return LONG_ARRAY ; } 
    private static final Equality<long[]> LONG_ARRAY = new LongArrayEquality() ;
    /**
     * an <code>Equality</code> over objects of type <code>long[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
     * @author b.elliottsmith
     */
    public static class LongArrayEquality implements Equality<long[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(long[] a, long[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(long[] k) { return Arrays.hashCode(k) ; }
    } ;

    /**
	 * Returns an <code>Equality</code> over objects of type <code>Object[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
	 * @return <code>Object[] Equality </code>
	 */
	public static Equality<Object[]> objectArray() { return OBJECT_ARRAY ; } 
    private static final Equality<Object[]> OBJECT_ARRAY = new ObjectArrayEquality() ;
    /**
     * an <code>Equality</code> over objects of type <code>Object[]</code>, delegating to <code>java.util.Arrays.equals(a, b)</code>
	 * and java.util.<code>Arrays.hashCode(k)</code>
	 * 
     * @author b.elliottsmith
     */
    public static class ObjectArrayEquality implements Equality<Object[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final boolean equates(Object[] a, Object[] b) { return java.util.Arrays.equals(a, b) ; }
		public final int hash(Object[] k) { return Arrays.hashCode(k) ; }
    } ;
    
    /**
	 * Returns an <code>Equality</code> over objects of type <code>Entry<K V></code>, for provided <code>K</code> and <code>V</code>; wraps
	 * a separate <code>Equality</code> for comparing the key and value portions of the <code>Entry</code>.
	 * 
	 * @param keyEq
	 *            the <code>Equality</code> to use to compare the key portion of an <code>Entry</code>
	 * @param valEq
	 *            the <code>Equality</code> to use to compare the value portion of an <code>Entry</code>
	 */
    public static <K, V> Equality<Entry<K, V>> forMapEntries(Equality<? super K> keyEq, Equality<? super V> valEq) { return new EntryEquality<K, V>(keyEq, valEq) ; }
    
    /**
     * an <code>Equality</code> over objects of type <code>Entry<K V></code>, for provided <code>K</code> and <code>V</code>; wraps
	 * a separate <code>Equality</code> for comparing the key and value portions of the <code>Entry</code>.
	 * 
     * @author b.elliottsmith
     */
    public static class EntryEquality<K, V> implements Equality<Entry<K, V>> {
    	
    	private static final long serialVersionUID = -6611748225612686746L ;
    	
    	/**
    	 * the <code>Equality</code> to use to compare the key portion of an <code>Entry</code>
    	 */
    	protected final Equality<? super K> keyEq ;
    	
    	/**
    	 * the <code>Equality</code> to use to compare the value portion of an <code>Entry</code>
    	 */
    	protected final Equality<? super V> valEq ;
    	
		/**
		 * Construct a new EntryEquality
		 * 
		 * @param keyEq
		 *            the <code>Equality</code> to use to compare the key portion of an <code>Entry</code>
		 * @param valEq
		 *            the <code>Equality</code> to use to compare the value portion of an <code>Entry</code>
		 */
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
		/**
		 * @return the <code>Equality</code> to use to compare the key portion of an <code>Entry</code>
		 */
		public final Equality<? super K> getKeyEquality() { return keyEq ; }
		/**
		 * @return the <code>Equality</code> to use to compare the value portion of an <code>Entry</code>
		 */
		public final Equality<? super V> getValueEquality() { return valEq ; }		
    } ;
    
	/**
	 * Return an <code>Equality</code> which delegates to the provided <code>Equality</code>, but passes the resulting <code>hash()</code>
	 * through the provided <code>Rehasher</code> before returning it
	 * 
	 * @param delegate
	 *            the <code>Equality</code> to delegate to
	 * @param rehasher
	 *            the <code>Rehasher</code> to pass the hash through
	 * @return an <code>Equality</code> which rehashes the hashes returned by the delegate
	 */
	public static final <V> Equality<V> rehashing(Equality<V> delegate,
			Rehasher rehasher) {
		return new RehashingEquality<V>(delegate, rehasher);
	}

	/**
	 * an <code>Equality</code> which delegates to the provided <code>Equality</code>, but passes the resulting <code>hash()</code>
	 * through the provided <code>Rehasher</code> before returning it
	 * 
	 * @author b.elliottsmith
	 */
	public static final class RehashingEquality<V> implements Equality<V> {
		
		private static final long serialVersionUID = -2720117974468199395L;
		private final Equality<V> delegate;
		private final Rehasher rehasher;

		/**
		 * Create a new RehashingEquality
		 * 
		 * @param delegate
		 *            the <code>Equality</code> to delegate to
		 * @param rehasher
		 *            the <code>Rehasher</code> to pass the hash through
		 */
		public RehashingEquality(Equality<V> delegate, Rehasher rehasher) {
			super();
			this.delegate = delegate;
			this.rehasher = rehasher;
		}

		@Override
		public int hash(V o) {
			return rehasher.rehash(delegate.hash(o));
		}

		@Override
		public boolean equates(V a, V b) {
			return delegate.equates(a, b);
		}
	}

}