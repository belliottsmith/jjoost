package org.jjoost.collections.sets.base;

import java.util.Arrays ;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.AbstractHashStoreBasedMultiCollectionTest;
import org.jjoost.collections.MultiSet;

public abstract class MultiHashSetTest extends AbstractHashStoreBasedMultiCollectionTest {

	protected abstract MultiSet<String> getSet() ;
	
	@Override
	protected String put(String v) {
		return getSet().put(v) ;
	}	
	
	@Override
	protected boolean add(String v) {
		return getSet().add(v) ;
	}
	
	protected Iterable<String> iterate(String val) {
		return getSet().all(val) ;
	}

	protected Boolean apply(String v) {
		return getSet().apply(v) ;
	}

	protected int clear() {
		return getSet().clear() ;
	}

	protected Iterator<String> clearAndReturn() {
		return getSet().clearAndReturn() ;
	}

	protected boolean contains(String value) {
		return getSet().contains(value) ;
	}

	protected int count(String value) {
		return getSet().count(value) ;
	}

	protected String first(String value) {
		return getSet().first(value) ;
	}

	protected boolean isEmpty() {
		return getSet().isEmpty() ;
	}

	protected Iterator<String> iterator() {
		return getSet().iterator() ;
	}

	protected List<String> list(String val) {
		return getSet().list(val) ;
	}

	protected int putAll(Iterable<String> vals) {
		return getSet().putAll(vals) ;
	}

	protected String putIfAbsent(String val) {
		return getSet().putIfAbsent(val) ;
	}

	protected int remove(String val, int atMost) {
		return getSet().remove(val, atMost) ;
	}

	protected int remove(String val) {
		return getSet().remove(val) ;
	}

	protected Iterable<String> removeAndReturn(String val, int atMost) {
		return getSet().removeAndReturn(val, atMost) ;
	}

	protected Iterable<String> removeAndReturn(String val) {
		return getSet().removeAndReturn(val) ;
	}

	protected String removeAndReturnFirst(String value, int atMost) {
		return getSet().removeAndReturnFirst(value, atMost) ;
	}

	protected String removeAndReturnFirst(String value) {
		return getSet().removeAndReturnFirst(value) ;
	}

	protected void shrink() {
		getSet().shrink() ;
	}

	protected int totalCount() {
		return getSet().totalCount() ;
	}

	protected int uniqueCount() {
		return getSet().uniqueCount() ;
	}

	public void testCopy_whenEmpty() {
		checkEmpty() ;
		final MultiSet<String> copy = getSet().copy() ;
		assertNotSame(copy, getSet()) ;
		assertEquals(getSet(), copy) ;
		checkAndClear(0) ;
	}
	
	public void testCopy_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("a") ;
		put("q") ;
		put("b") ;
		put(null) ;
		final MultiSet<String> copy = getSet().copy() ;
		assertNotSame(copy, getSet()) ;
		assertEquals(getSet(), copy) ;
		checkAndClear(5) ;
	}
	
	public void testPutAll_whenNotPresent() {
		checkEmpty() ;
		assertEquals(5, putAll(Arrays.asList("a", "a", "b", "q", null))) ;
		checkAndClear(5) ;
	}
	
	public void testPutAll_whenPresent() {
		checkEmpty() ;
		putAll(Arrays.asList("a", "a", "b", "q", null)) ;
		assertEquals(5, putAll(Arrays.asList("a", "a", "b", "q", null))) ;
		checkAndClear(10) ;		
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
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals(0, remove("a", 0)) ;
		assertEquals(0, remove("q", 0)) ;
		assertEquals(1, remove("a", 1)) ;
		assertEquals(2, remove("a", 100)) ;
		assertEquals(1, remove("q", 100)) ;
		put("a") ;
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals(1, remove("q", 100)) ;
		assertEquals(1, remove("a", 1)) ;
		assertEquals(2, remove("a", 100)) ;
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
		put("a") ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a", 0), true) ;
		checkIterableContents(Arrays.asList(), removeAndReturn("q", 0), true) ;
		checkIterableContents(Arrays.asList("a"), removeAndReturn("a", 1), true) ;
		checkIterableContents(Arrays.asList("a", "a"), removeAndReturn("a", 100), true) ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q", 100), true) ;
		put("a") ;
		put("a") ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q", 100), true) ;
		checkIterableContents(Arrays.asList("a"), removeAndReturn("a", 1), true) ;
		checkIterableContents(Arrays.asList("a", "a"), removeAndReturn("a", 100), true) ;
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
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals(null, removeAndReturnFirst("a", 0)) ;
		assertEquals(null, removeAndReturnFirst("q", 0)) ;
		assertEquals("a", removeAndReturnFirst("a", 1)) ;
		assertEquals("a", removeAndReturnFirst("a", 100)) ;
		assertEquals("q", removeAndReturnFirst("q", 100)) ;
		put("a") ;
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals("q", removeAndReturnFirst("q", 100)) ;
		assertEquals("a", removeAndReturnFirst("a", 1)) ;
		assertEquals("a", removeAndReturnFirst("a", 100)) ;
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
		put("a") ;
		put("a") ;
		put("q") ;
		assertTrue(apply("a")) ;
		assertTrue(apply("q")) ;
		checkAndClear(3) ;
	}
	
	public void testEquality() {
		assertNotNull(getSet().equality()) ;
	}

	public void testUnique() {
		assertNotSame(getSet(), getSet().unique()) ;
		assertFalse(getSet().unique().permitsDuplicates()) ;
	}
	
	public void testPermitsDuplicates() {
		assertTrue(getSet().permitsDuplicates()) ;
	}
	
	public void testToString() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals("{null, a, a, q}", getSet().toString()) ;
		checkAndClear(4) ;
	}
	
}
