package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.maps.base.HashMap ;
import org.jjoost.collections.maps.base.HashMapKeySetTest ;
import org.jjoost.collections.maps.base.LinkedHashMapKeySetTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class LockFreeLinkedHashMapKeySetTest extends LinkedHashMapKeySetTest {

	@Override
	protected HashMap<String, String, ?> createMap() {
		return new LockFreeLinkedHashMap<String, String>(Rehashers.identity(), Equalities.object()) ;
	}
	
}
