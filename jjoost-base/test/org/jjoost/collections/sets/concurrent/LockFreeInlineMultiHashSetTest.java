package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.sets.base.MultiHashSetTest;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class LockFreeInlineMultiHashSetTest extends MultiHashSetTest {

	private final LockFreeInlineMultiHashSet<String> set = new LockFreeInlineMultiHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public LockFreeInlineMultiHashSet<String> getSet() {
		return set ;
	}
	
	protected int capacity() {
		return getSet().capacity() ;
	}

	protected void resize(int capacity) {
		getSet().resize(capacity) ;
	}
	
	@Override
	protected boolean duplicatesGrowTable() {
		return false;
	}

}
