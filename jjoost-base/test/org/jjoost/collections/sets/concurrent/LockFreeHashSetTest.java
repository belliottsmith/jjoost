package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.sets.base.HashSet;
import org.jjoost.collections.sets.base.HashSetTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class LockFreeHashSetTest extends HashSetTest {

	private final LockFreeHashSet<String> set = new LockFreeHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public HashSet<String, ?> getSet() {
		return set ;
	}
	
	protected int capacity() {
		return getSet().capacity() ;
	}

	protected void resize(int capacity) {
		getSet().resize(capacity) ;
	}

}
