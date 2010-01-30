package org.jjoost.collections.sets.serial;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.sets.base.MultiHashSetUniqueSetTest;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class SerialInlineMultiHashSetUniqueSetTest extends MultiHashSetUniqueSetTest {

	private final SerialInlineMultiHashSet<String> set = new SerialInlineMultiHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public MultiSet<String> getMultiSet() {
		return set ;
	}
	
	protected int capacity() {
		return set.capacity() ;
	}

	protected void resize(int capacity) {
		set.resize(capacity) ;
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
