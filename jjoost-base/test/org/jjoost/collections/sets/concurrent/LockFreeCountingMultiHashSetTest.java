//package org.jjoost.collections.sets.concurrent;
//
//import org.jjoost.collections.sets.base.MultiHashSetTest;
//import org.jjoost.util.Equalities;
//import org.jjoost.util.Rehashers;
//
//public class LockFreeCountingMultiHashSetTest extends MultiHashSetTest {
//
//	private final LockFreeCountingMultiHashSet<String> set = new LockFreeCountingMultiHashSet<String>(Rehashers.identity(), Equalities.object());
//	
//	public LockFreeCountingMultiHashSet<String> getSet() {
//		return set;
//	}
//	
//	protected int capacity() {
//		return getSet().capacity();
//	}
//
//	protected void resize(int capacity) {
//		getSet().resize(capacity);
//	}
//
//	@Override
//	protected boolean duplicatesGrowTable() {
//		return false;
//	}
//
//}
