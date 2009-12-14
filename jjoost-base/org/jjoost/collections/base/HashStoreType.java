package org.jjoost.collections.base;

public class HashStoreType {

	public static enum Type {
		SERIAL , SYNCHRONIZED , LOCK_FREE , SEGMENTED ; 
	}
	
	private final Type type ;
	private final boolean linked ;
	public Type type() { return type ; }
	public boolean linked() { return linked ; }
	public HashStoreType(Type type, boolean linked) { this.type = type ; this.linked = linked ; }

	public static HashStoreType serial() { return SERIAL ; }
	public static HashStoreType blocking() { return BLOCKING ; }
	public static HashStoreType lockFree() { return NON_BLOCKING ; }
	public static HashStoreType linkedSerial() { return SERIAL_LINKED ; }
	public static HashStoreType linkedBlocking() { return BLOCKING_LINKED ; }
	public static HashStoreType linkedLockFree() { return LOCK_FREE_LINKED ; }
	public static HashStoreType segmented(HashStoreType type, int segments) { return new SegmentedHashTableType(type, segments) ; }
	
	private static final HashStoreType SERIAL = new HashStoreType(Type.SERIAL, false) ;
	private static final HashStoreType BLOCKING = new HashStoreType(Type.SYNCHRONIZED, false) ;
	private static final HashStoreType NON_BLOCKING = new HashStoreType(Type.LOCK_FREE, false) ;
	
	private static final HashStoreType SERIAL_LINKED = new HashStoreType(Type.SERIAL, true) ;
	private static final HashStoreType BLOCKING_LINKED = new HashStoreType(Type.SYNCHRONIZED, true) ;
	private static final HashStoreType LOCK_FREE_LINKED = new HashStoreType(Type.LOCK_FREE, true) ;
	
	public static final class SegmentedHashTableType extends HashStoreType {
		private final int segments ;
		private final HashStoreType underlyingType ;
		private SegmentedHashTableType(HashStoreType underlyingType, int segments) {
			super(Type.SEGMENTED, false) ;
			this.segments = segments ;
			this.underlyingType = underlyingType ;
		}
		public int segments() { return segments ; }
		public HashStoreType underlyingType() { return underlyingType ; }
	}
	
}
