package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.maps.base.HashMap;
import org.jjoost.collections.maps.base.HashMapKeySetTest;
import org.jjoost.util.Equalities;
import org.jjoost.util.Rehashers;

public class LockFreeHashMapKeySetTest extends HashMapKeySetTest {

	@Override
	protected HashMap<String, String, ?> createMap() {
		return new LockFreeHashMap<String, String>(Rehashers.identity(), Equalities.object());
	}
	
}
