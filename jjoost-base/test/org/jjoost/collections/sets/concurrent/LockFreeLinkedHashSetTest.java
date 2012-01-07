package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.sets.base.HashSet;
import org.jjoost.collections.sets.base.LinkedHashSetTest;
import org.jjoost.util.Equalities;
import org.jjoost.util.Rehashers;

public class LockFreeLinkedHashSetTest extends LinkedHashSetTest {

	private final LockFreeLinkedHashSet<String> set = new LockFreeLinkedHashSet<String>(Rehashers.identity(), Equalities.object());
	
	public HashSet<String, ?> getSet() {
		return set;
	}
	
	protected int capacity() {
		return getSet().capacity();
	}

	protected void resize(int capacity) {
		getSet().resize(capacity);
	}

}
