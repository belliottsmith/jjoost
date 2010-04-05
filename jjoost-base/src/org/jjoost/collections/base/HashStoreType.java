package org.jjoost.collections.base;

public class HashStoreType {

	public static enum Type {
		SERIAL , LINKED_SERIAL , SYNCHRONIZED , LINKED_SYNCHRONIZED , LOCK_FREE , LINKED_LOCK_FREE, HASH_LOCK ; 
	}
	
	private final Type type ;
	public Type type() { return type ; }
	public HashStoreType(Type type) { this.type = type ; }

	public static HashStoreType serial() { return SERIAL ; }
	public static HashStoreType blocking() { return BLOCKING ; }
	public static HashStoreType lockFree() { return LOCK_FREE ; }
	public static HashStoreType hashLock() { return HASH_LOCK ; }
	public static HashStoreType linkedSerial() { return LINKED_SERIAL ; }
	public static HashStoreType linkedBlocking() { return LINKED_BLOCKING ; }
	public static HashStoreType linkedLockFree() { return LINKED_LOCK_FREE ; }
	
	private static final HashStoreType SERIAL = new HashStoreType(Type.SERIAL) ;
	private static final HashStoreType BLOCKING = new HashStoreType(Type.SYNCHRONIZED) ;
	private static final HashStoreType LOCK_FREE = new HashStoreType(Type.LOCK_FREE) ;
	private static final HashStoreType HASH_LOCK = new HashStoreType(Type.HASH_LOCK) ;
	
	private static final HashStoreType LINKED_SERIAL = new HashStoreType(Type.SERIAL) ;
	private static final HashStoreType LINKED_BLOCKING = new HashStoreType(Type.SYNCHRONIZED) ;
	private static final HashStoreType LINKED_LOCK_FREE = new HashStoreType(Type.LOCK_FREE) ;
	
}
