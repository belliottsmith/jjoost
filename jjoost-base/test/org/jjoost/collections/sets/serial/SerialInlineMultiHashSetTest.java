package org.jjoost.collections.sets.serial;

import org.jjoost.collections.sets.base.MultiHashSetTest;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialInlineMultiHashSetTest extends MultiHashSetTest {

	private final SerialInlineMultiHashSet<String> set = new SerialInlineMultiHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public SerialInlineMultiHashSet<String> getSet() {
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
