package org.jjoost.util;

import java.util.Arrays;

public class Hashers {

    /**
     * ordinary object hash code (k.hashCode())
     */
	public static final Hasher<Object> object() { return OBJECT ; }
    private static final Hasher<Object> OBJECT = new ObjectHasher() ;
    private static final class ObjectHasher implements Hasher<Object> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final int hash(Object k) { return k.hashCode() ; }
    } ;

    /**
     * identity hash code
     */
	public static final Hasher<Object> identityHash() { return IDENTITY ; }
    private static final Hasher<Object> IDENTITY = new IdentityHasher() ;
    private static final class IdentityHasher implements Hasher<Object> {
		private static final long serialVersionUID = 5451996420739109559L ;
		public final int hash(Object k) { return System.identityHashCode(k) ; }
    } ;

    /**
     * byte[] hash code
     */
	public static final Hasher<byte[]> byteArray() { return BYTE_ARRAY ; }
    private static final Hasher<byte[]> BYTE_ARRAY = new ByteArrayHasher() ;
    private static final class ByteArrayHasher implements Hasher<byte[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final int hash(byte[] k) { return Arrays.hashCode(k) ; }
    } ;

    /**
     * int[] hash code
     */
	public static final Hasher<int[]> intArray() { return INT_ARRAY ; }
    private static final Hasher<int[]> INT_ARRAY = new IntArrayHasher() ;
    private static final class IntArrayHasher implements Hasher<int[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final int hash(int[] k) { return Arrays.hashCode(k) ; }
    } ;


    /**
     * long[] hash code
     */
	public static final Hasher<long[]> longArray() { return LONG_ARRAY ; }
    private static final Hasher<long[]> LONG_ARRAY = new LongArrayHasher() ;
    private static final class LongArrayHasher implements Hasher<long[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final int hash(long[] k) { return Arrays.hashCode(k) ; }
    } ;

    /**
     * Object[] hash code
     */
	public static final Hasher<Object[]> objectArray() { return OBJECT_ARRAY ; }
    private static final Hasher<Object[]> OBJECT_ARRAY = new ObjectArrayHasher() ;
    private static final class ObjectArrayHasher implements Hasher<Object[]> {
		private static final long serialVersionUID = -6611748225612686746L ;
		public final int hash(Object[] k) { return Arrays.hashCode(k) ; }
    } ;

    public static final <V> Hasher<V> rehashing(Hasher<V> hasher, Rehasher rehasher) {
    	return new RehashingHasher<V>(hasher, rehasher) ;
    }
    public static final class RehashingHasher<V> implements Hasher<V> {
		private static final long serialVersionUID = -2720117974468199395L;
		private final Hasher<V> hasher ;
    	private final Rehasher rehasher ;
		public RehashingHasher(Hasher<V> hasher, Rehasher rehasher) {
			super();
			this.hasher = hasher;
			this.rehasher = rehasher;
		}
		@Override
		public int hash(V o) {
			return rehasher.hash(hasher.hash(o)) ;
		}
    }
    
}
