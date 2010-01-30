package org.jjoost.collections.sets.serial;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;

import org.jjoost.collections.sets.base.MultiHashSetTest;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialCountingMultiHashSetTest extends MultiHashSetTest {

	private final SerialCountingMultiHashSet<String> set = new SerialCountingMultiHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public SerialCountingMultiHashSet<String> getSet() {
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
