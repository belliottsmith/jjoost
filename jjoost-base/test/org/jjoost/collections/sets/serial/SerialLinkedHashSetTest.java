package org.jjoost.collections.sets.serial;


import org.jjoost.collections.sets.base.HashSet;
import org.jjoost.collections.sets.base.LinkedHashSetTest;
import org.jjoost.util.Equalities;
import org.jjoost.util.Rehashers;

public class SerialLinkedHashSetTest extends LinkedHashSetTest {

	private final SerialLinkedHashSet<String> set = new SerialLinkedHashSet<String>(Rehashers.identity(), Equalities.object());
	
	public HashSet<String, ?, ?> getSet() {
		return set;
	}
	
	protected int capacity() {
		return getSet().capacity();
	}

}
