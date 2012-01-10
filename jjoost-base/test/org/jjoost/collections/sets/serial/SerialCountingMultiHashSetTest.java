package org.jjoost.collections.sets.serial;

import org.jjoost.collections.sets.base.MultiHashSetTest;
import org.jjoost.util.Equalities;
import org.jjoost.util.Rehashers;

public class SerialCountingMultiHashSetTest extends MultiHashSetTest {

	private final SerialCountingMultiHashSet<String> set = new SerialCountingMultiHashSet<String>(Rehashers.identity(), Equalities.object());
	
	public SerialCountingMultiHashSet<String> getSet() {
		return set;
	}
	
	protected int capacity() {
		return getSet().capacity();
	}

	@Override
	protected boolean duplicatesGrowTable() {
		return false;
	}

}
