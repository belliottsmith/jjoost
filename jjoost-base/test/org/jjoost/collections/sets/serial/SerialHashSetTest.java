package org.jjoost.collections.sets.serial;

import org.jjoost.collections.sets.base.HashSet;
import org.jjoost.collections.sets.base.HashSetTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialHashSetTest extends HashSetTest {

	private final SerialHashSet<String> set = new SerialHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
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
