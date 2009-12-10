package org.jjoost.collections.base;

public class HashTableType {

	public static enum Type {
		SERIAL , SYNCHRONIZED , NON_BLOCKING ; 
//		, PARTITIONED_BLOCKING , PARTITIONED_NON_BLOCKING ;
	}
	
	private final Type type ;
	private final boolean linked ;
	public Type type() { return type ; }
	public boolean linked() { return linked ; }
	public HashTableType(Type type, boolean linked) { this.type = type ; this.linked = linked ; }

	public static HashTableType serial() { return SERIAL ; }
	public static HashTableType blocking() { return BLOCKING ; }
	public static HashTableType nonBlocking() { return NON_BLOCKING ; }
	public static HashTableType serial(boolean linked) { return SERIAL_LINKED ; }
	public static HashTableType blocking(boolean linked) { return BLOCKING_LINKED ; }
	public static HashTableType nonBlocking(boolean linked) { return NON_BLOCKING_LINKED ; }
//	public static final HashTableType partitionBlocking(int partitions) { return new PartitionedHashTableType(Type.PARTITIONED_BLOCKING, partitions) ; }
//	public static final HashTableType partitionedNonBlocking(int partitions) { return new PartitionedHashTableType(Type.PARTITIONED_BLOCKING, partitions) ; }
	
	private static final HashTableType SERIAL = new HashTableType(Type.SERIAL, false) ;
	private static final HashTableType BLOCKING = new HashTableType(Type.SYNCHRONIZED, false) ;
	private static final HashTableType NON_BLOCKING = new HashTableType(Type.NON_BLOCKING, false) ;
	
	private static final HashTableType SERIAL_LINKED = new HashTableType(Type.SERIAL, true) ;
	private static final HashTableType BLOCKING_LINKED = new HashTableType(Type.SYNCHRONIZED, true) ;
	private static final HashTableType NON_BLOCKING_LINKED = new HashTableType(Type.NON_BLOCKING, true) ;
	
	public static final class PartitionedHashTableType extends HashTableType {
		private final int partitions ;
		private PartitionedHashTableType(Type type, int partitions) {
			super(type, false) ;
			this.partitions = partitions;
		}
		public int partitions() { return partitions ; }
	}
	
}
