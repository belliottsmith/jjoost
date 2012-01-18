//package org.jjoost.collections.sets.concurrent;
//
//import org.jjoost.collections.MultiSet;
//import org.jjoost.collections.sets.base.MultiHashSetUniqueSetTest;
//import org.jjoost.util.Equalities;
//import org.jjoost.util.Rehashers;
//
//public class LockFreeCountingMultiHashSetUniqueSetTest extends MultiHashSetUniqueSetTest {
//
//	private final LockFreeCountingMultiHashSet<String> set = new LockFreeCountingMultiHashSet<String>(Rehashers.identity(), Equalities.object());
//	
//	public MultiSet<String> getMultiSet() {
//		return set;
//	}
//	
//	protected int capacity() {
//		return set.capacity();
//	}
//
//	protected void resize(int capacity) {
//		set.resize(capacity);
//	}
//	
//}
