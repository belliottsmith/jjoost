package org.jjoost.collections.sets.serial;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;

import org.jjoost.collections.HashSetTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialHashSetTest extends HashSetTest {

	@Override
	protected SerialHashSet<String> createSet() {
		return new SerialHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	}

	@Override
	protected String put(String v) {
		return set.put(v) ;
	}	
	
	public void testIteratorConcurrentModifications() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		final Iterator<?> iter = set.iterator() ;
		iter.next() ;
		set.put("b") ;
		try {
			iter.next() ;
			assertTrue(false) ;
		} catch (ConcurrentModificationException e) {			
		}
		try {
			iter.remove() ;
			assertTrue(false) ;
		} catch (ConcurrentModificationException e) {			
		}		
		checkAndClear(3) ;
	}
	
}
