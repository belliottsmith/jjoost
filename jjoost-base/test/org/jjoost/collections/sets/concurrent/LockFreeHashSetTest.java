package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.HashSetTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class LockFreeHashSetTest extends HashSetTest {

	@Override
	protected LockFreeHashSet<String> createSet() {
		return new LockFreeHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	}	

	@Override
	protected String put(String v) {
		return set.put(v) ;
	}	
	
}
