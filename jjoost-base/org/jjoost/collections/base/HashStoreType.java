package org.jjoost.collections.base;

public class HashStoreType {

	public static enum Type {
		SERIAL , SYNCHRONIZED , NON_BLOCKING ; 
//		, PARTITIONED_BLOCKING , PARTITIONED_NON_BLOCKING ;
	}
	
	private final Type type ;
	private final boolean linked ;
	public Type type() { return type ; }
	public boolean linked() { return linked ; }
	public HashStoreType(Type type, boolean linked) { this.type = type ; this.linked = linked ; }

	public static HashStoreType serial() { return SERIAL ; }
	public static HashStoreType blocking() { return BLOCKING ; }
	public static HashStoreType nonBlocking() { return NON_BLOCKING ; }
	public static HashStoreType serial(boolean linked) { return SERIAL_LINKED ; }
	public static HashStoreType blocking(boolean linked) { return BLOCKING_LINKED ; }
	public static HashStoreType nonBlocking(boolean linked) { return NON_BLOCKING_LINKED ; }
//	public static final HashTableType partitionBlocking(int partitions) { return new PartitionedHashTableType(Type.PARTITIONED_BLOCKING, partitions) ; }
//	public static final HashTableType partitionedNonBlocking(int partitions) { return new PartitionedHashTableType(Type.PARTITIONED_BLOCKING, partitions) ; }
	
	private static final HashStoreType SERIAL = new HashStoreType(Type.SERIAL, false) ;
	private static final HashStoreType BLOCKING = new HashStoreType(Type.SYNCHRONIZED, false) ;
	private static final HashStoreType NON_BLOCKING = new HashStoreType(Type.NON_BLOCKING, false) ;
	
	private static final HashStoreType SERIAL_LINKED = new HashStoreType(Type.SERIAL, true) ;
	private static final HashStoreType BLOCKING_LINKED = new HashStoreType(Type.SYNCHRONIZED, true) ;
	private static final HashStoreType NON_BLOCKING_LINKED = new HashStoreType(Type.NON_BLOCKING, true) ;
	
	public static final class PartitionedHashTableType extends HashStoreType {
		private final int partitions ;
		private PartitionedHashTableType(Type type, int partitions) {
			super(type, false) ;
			this.partitions = partitions;
		}
		public int partitions() { return partitions ; }
	}
	
}
