package org.jjoost.collections.sets.serial;


import org.jjoost.collections.MultiSet;
import org.jjoost.collections.sets.base.MultiHashSetUniqueSetTest;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialCountingMultiHashSetUniqueSetTest extends MultiHashSetUniqueSetTest {

	private final SerialCountingMultiHashSet<String> set = new SerialCountingMultiHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public MultiSet<String> getMultiSet() {
		return set ;
	}
	
	protected int capacity() {
		return set.capacity() ;
	}

	protected void resize(int capacity) {
		set.resize(capacity) ;
	}
	
}
