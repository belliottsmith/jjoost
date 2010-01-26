package org.jjoost.collections.sets.base;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.jjoost.collections.AbstractHashStoreBasedScalarCollectionTest ;
import org.jjoost.collections.Set ;

public abstract class HashSetTest extends AbstractHashStoreBasedScalarCollectionTest {

	protected abstract int putAll(Iterable<String> vs) ;
	protected abstract int remove(String v, int c) ;
	protected abstract Iterable<String> removeAndReturn(String v, int c) ;
	protected abstract String removeAndReturnFirst(String v, int c) ;
	protected abstract Boolean apply(String v) ;
	protected abstract Set<String> getSet() ;

	public void testCopy_whenEmpty() {
		checkEmpty() ;
		final Set<String> copy = getSet().copy() ;
		assertNotSame(copy, getSet()) ;
		assertEquals(getSet(), copy) ;
		checkAndClear(0) ;
	}
	
	public void testCopy_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("b") ;
		put(null) ;
		final Set<String> copy = getSet().copy() ;
		assertNotSame(copy, getSet()) ;
		assertEquals(getSet(), copy) ;
		checkAndClear(4) ;
	}
	
	public void testPutAll_whenNotPresent() {
		checkEmpty() ;
		assertEquals(4, putAll(Arrays.asList("a", "b", "q", null))) ;
		checkAndClear(4) ;
	}
	
	public void testPutAll_whenPresent() {
		checkEmpty() ;
		putAll(Arrays.asList("a", "b", "q", null)) ;
		assertEquals(0, putAll(Arrays.asList("a", "b", "q", null))) ;
		checkAndClear(4) ;		
	}
	
	public void testRemoveMultiple_whenNotPresent() {		
		checkEmpty() ;
		assertEquals(0, remove("a", 0)) ;
		assertEquals(0, remove("a", 1)) ;
		put("q") ;
		assertEquals(0, remove("a", 1)) ;
		try {
			remove("a", -1) ;
			assertTrue(false) ;
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1) ;
	}
	
	public void testRemoveMultiple_whenPresent() {		
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals(0, remove("a", 0)) ;
		assertEquals(0, remove("q", 0)) ;
		assertEquals(1, remove("a", 100)) ;
		assertEquals(1, remove("q", 100)) ;
		put("a") ;
		put("q") ;
		assertEquals(1, remove("q", 100)) ;
		assertEquals(1, remove("a", 100)) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveMultipleAndReturn_whenNotPresent() {
		checkEmpty() ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a", 0), true) ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a", 1), true) ;
		put("q") ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a", 0), true) ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a", 1), true) ;
		try {
			removeAndReturn("a", -1) ;
			assertTrue(false) ;
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1) ;
	}
	
	public void testRemoveMultipleAndReturn_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a", 0), true) ;
		checkIterableContents(Arrays.asList(), removeAndReturn("q", 0), true) ;
		checkIterableContents(Arrays.asList("a"), removeAndReturn("a", 1), true) ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q", 1), true) ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q", 1), true) ;
		checkIterableContents(Arrays.asList("a"), removeAndReturn("a", 1), true) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveMultipleAndReturnFirst_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, removeAndReturnFirst("a", 0)) ;
		put("q") ;
		assertEquals(null, removeAndReturnFirst("a", 0)) ;
		try {
			removeAndReturnFirst("a", -1) ;
			assertTrue(false) ;
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1) ;
	}
	
	public void testRemoveMultipleAndReturnFirst_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals(null, removeAndReturnFirst("a", 0)) ;
		assertEquals(null, removeAndReturnFirst("q", 0)) ;
		assertEquals("a", removeAndReturnFirst("a", 1)) ;
		assertEquals("q", removeAndReturnFirst("q", 1)) ;
		put("a") ;
		put("q") ;
		assertEquals("q", removeAndReturnFirst("q", 1)) ;
		assertEquals("a", removeAndReturnFirst("a", 1)) ;
		checkAndClear(0) ;
	}
	
	public void testApply_whenNotPresent() {
		checkEmpty() ;
		assertFalse(apply("a")) ;
		put("a") ;
		assertFalse(apply("q")) ;
		checkAndClear(1) ;
	}
	
	public void testApply_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		assertTrue(apply(a1)) ;
		assertTrue(apply(q1)) ;
		checkAndClear(2) ;
	}
	
	public void testEquality() {
		assertNotNull(getSet().equality()) ;
	}

	public void testUnique() {
		assertEquals(getSet(), getSet().unique()) ;
	}
	
	public void testPermitsDuplicates() {
		assertFalse(getSet().permitsDuplicates()) ;
	}
	
	public void testToString() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("q") ;
		assertEquals("{null, a, q}", getSet().toString()) ;
		checkAndClear(3) ;
	}
	
}
