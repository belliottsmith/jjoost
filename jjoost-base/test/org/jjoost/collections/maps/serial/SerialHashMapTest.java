package org.jjoost.collections.maps.serial;

import org.jjoost.collections.maps.base.HashMap ;
import org.jjoost.collections.maps.base.HashMapTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialHashMapTest extends HashMapTest {

	@Override
	protected HashMap<String, String, ?> createMap() {
		return new SerialHashMap<String, String>(Rehashers.identity(), Equalities.object()) ;
	}
	
}
