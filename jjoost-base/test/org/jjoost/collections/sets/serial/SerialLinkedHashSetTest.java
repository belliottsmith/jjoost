package org.jjoost.collections.sets.serial;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;

import org.jjoost.collections.sets.base.HashSet;
import org.jjoost.collections.sets.base.LinkedHashSetTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialLinkedHashSetTest extends LinkedHashSetTest {

	private final SerialLinkedHashSet<String> set = new SerialLinkedHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public HashSet<String, ?> getSet() {
		return set ;
	}
	
	protected int capacity() {
		return getSet().capacity() ;
	}

	protected void resize(int capacity) {
		getSet().resize(capacity) ;
	}

	public void testIteratorConcurrentModifications() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		final Iterator<?> iter = set.iterator() ;
		iter.next() ;
		put("b") ;
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
