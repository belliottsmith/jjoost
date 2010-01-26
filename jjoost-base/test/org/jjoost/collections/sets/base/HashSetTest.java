package org.jjoost.collections.sets.base;

import java.util.Arrays ;
import java.util.Iterator ;

import org.jjoost.collections.AbstractTest ;
import org.jjoost.collections.sets.base.HashSet ;
import org.jjoost.util.Iters ;

public abstract class HashSetTest extends AbstractTest {
	
	protected final HashSet<String, ?> set = createSet() ;
	/**
	 * @return a hash set using Rehashers.identity() and Equalities.object(), with a table size of 16
	 */
	protected abstract HashSet<String, ?> createSet() ;
	protected abstract String put(String v) ;
	protected abstract boolean add(String v) ;
	
	protected void checkEmpty() {
		assertEquals(0, set.size()) ;
	}
	
	protected void checkSize(int expect) {
		assertEquals(expect, set.size()) ;
	}
	
	protected void checkAndClear(int expect) {
		assertEquals(expect, set.clear()) ;
	}
	
	public void testIsEmpty_whenEmpty() {
		assertTrue(set.isEmpty()) ;
	}
	
	public void testIsEmpty_whenNotEmpty() {
		put("a") ;
		assertFalse(set.isEmpty()) ;
		set.clear() ;
	}
	
	public void testSize_whenEmpty() {
		checkEmpty() ;
	}
	
	public void testSize_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("b") ;
		put("c") ;
		set.remove("a") ;
		checkSize(3) ;
		set.clear() ;
	}
	
	public void testTotalCount_whenEmpty() {
		assertEquals(0, set.totalCount()) ;
	}
	
	public void testTotalCount_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("b") ;
		put("c") ;
		set.remove("a") ;
		assertEquals(3, set.totalCount()) ;
		set.clear() ;
	}
	
	public void testUniqueCount_whenEmpty() {
		assertEquals(0, set.uniqueCount()) ;
	}
	
	public void testUniqueCount_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("b") ;
		put("c") ;
		set.remove("a") ;
		assertEquals(3, set.uniqueCount()) ;
		set.clear() ;
	}
	
	public void testClear_whenEmpty() {
		checkEmpty() ;
		checkAndClear(0) ;
	}
	
	public void testClear_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("b") ;
		put("c") ;
		put(null) ;
		checkAndClear(5) ;
	}
	
	public void testClearAndReturn_whenEmpty() {
		checkEmpty() ;
		checkIteratorContents(Iters.emptyIterator(), set.clearAndReturn(), true) ;
	}
	
	public void testClearAndReturn_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put(null) ;
		// also perform removes just to test the no-op remove() on this Iterator
		checkIteratorContents(Arrays.asList(null, "a", "q").iterator(), Iters.destroyAsConsumed(set.clearAndReturn()), keepsOrdering) ;
		checkAndClear(0) ;
	}
	
	public void testPut_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, put("a")) ;
		assertEquals(null, put("q")) ;
		assertEquals(null, put(null)) ;
		checkAndClear(3) ;
	}
	
	public void testPut_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String a2 = new String("a") ;
		final String q1 = "q" ;
		final String q2 = new String("q") ;
		put(a1) ;
		assertSame(a1, put(a2)) ;
		assertSame(a2, put(a1)) ;
		put(q1) ;
		assertSame(q1, put(q2)) ;
		assertSame(q2, put(q1)) ;
		put(null) ;
		assertEquals(null, put(null)) ;
		checkAndClear(3) ;
	}
	
	public void testAdd_whenNotPresent() {
		checkEmpty() ;
		assertEquals(true, add("a")) ;
		assertEquals(true, add("q")) ;
		assertEquals(true, add(null)) ;
		checkAndClear(3) ;
	}
	
	public void testAdd_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String a2 = new String("a") ;
		final String q1 = "q" ;
		final String q2 = new String("q") ;
		add(a1) ;
		assertEquals(false, add(a2)) ;
		add(q1) ;
		assertEquals(false, add(q2)) ;
		add(null) ;
		assertEquals(false, add(null)) ;
		checkAndClear(3) ;
	}
	
	public void testPutIfAbsent_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, set.putIfAbsent("a")) ;
		assertEquals(null, set.putIfAbsent("q")) ;
		assertEquals(null, set.putIfAbsent(null)) ;
		checkAndClear(3) ;
	}
	
	public void testPutIfAbsent_whenPresent() {
		checkEmpty() ;
		set.putIfAbsent("a") ;
		set.putIfAbsent("q") ;
		assertEquals("a", set.putIfAbsent("a")) ;
		assertEquals("q", set.putIfAbsent("q")) ;
		checkAndClear(2) ;
	}
	
	public void testPutAll_whenNotPresent() {
		checkEmpty() ;
		assertEquals(4, set.putAll(Arrays.asList("a", "b", "q", null))) ;
		checkAndClear(4) ;
	}
	
	public void testPutAll_whenPresent() {
		checkEmpty() ;
		set.putAll(Arrays.asList("a", "b", "q", null)) ;
		assertEquals(0, set.putAll(Arrays.asList("a", "b", "q", null))) ;
		checkAndClear(4) ;		
	}
	
	public void testRemoveAll_whenNotPresent() {
		checkEmpty() ;
		assertEquals(0, set.remove("a")) ;
		put("q") ;
		assertEquals(0, set.remove("a")) ;
		checkAndClear(1) ;
	}
	
	public void testRemoveAll_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals(1, set.remove("a")) ;
		assertEquals(1, set.remove("q")) ;
		put("a") ;
		put("q") ;
		assertEquals(1, set.remove("q")) ;
		assertEquals(1, set.remove("a")) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveMultiple_whenNotPresent() {		
		checkEmpty() ;
		assertEquals(0, set.remove("a", 0)) ;
		assertEquals(0, set.remove("a", 1)) ;
		put("q") ;
		assertEquals(0, set.remove("a", 1)) ;
		try {
			set.remove("a", -1) ;
			assertTrue(false) ;
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1) ;
	}
	
	public void testRemoveMultiple_whenPresent() {		
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals(0, set.remove("a", 0)) ;
		assertEquals(0, set.remove("q", 0)) ;
		assertEquals(1, set.remove("a", 100)) ;
		assertEquals(1, set.remove("q", 100)) ;
		put("a") ;
		put("q") ;
		assertEquals(1, set.remove("q", 100)) ;
		assertEquals(1, set.remove("a", 100)) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveAllAndReturn_whenNotPresent() {
		checkEmpty() ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a"), true) ;
		put("q") ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a"), true) ;
		checkAndClear(1) ;
	}
	
	public void testRemoveAllAndReturn_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a"), true) ;
		checkIterableContents(Arrays.asList("q"), set.removeAndReturn("q"), true) ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("q"), set.removeAndReturn("q"), true) ;
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a"), true) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveMultipleAndReturn_whenNotPresent() {
		checkEmpty() ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 0), true) ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 1), true) ;
		put("q") ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 0), true) ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 1), true) ;
		try {
			set.removeAndReturn("a", -1) ;
			assertTrue(false) ;
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1) ;
	}
	
	public void testRemoveMultipleAndReturn_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 0), true) ;
		checkIterableContents(Arrays.asList(), set.removeAndReturn("q", 0), true) ;
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a", 1), true) ;
		checkIterableContents(Arrays.asList("q"), set.removeAndReturn("q", 1), true) ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("q"), set.removeAndReturn("q", 1), true) ;
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a", 1), true) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveAllAndReturnFirst_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, set.removeAndReturnFirst("a", 0)) ;
		put("q") ;
		assertEquals(null, set.removeAndReturnFirst("a", 0)) ;
		try {
			set.removeAndReturnFirst("a", -1) ;
			assertTrue(false) ;
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1) ;
	}
	
	public void testRemoveAllAndReturnFirst_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals("a", set.removeAndReturnFirst("a")) ;
		assertEquals("q", set.removeAndReturnFirst("q")) ;
		put("a") ;
		put("q") ;
		assertEquals("q", set.removeAndReturnFirst("q")) ;
		assertEquals("a", set.removeAndReturnFirst("a")) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveMultipleAndReturnFirst_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, set.removeAndReturnFirst("a", 0)) ;
		put("q") ;
		assertEquals(null, set.removeAndReturnFirst("a", 0)) ;
		try {
			set.removeAndReturnFirst("a", -1) ;
			assertTrue(false) ;
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1) ;
	}
	
	public void testRemoveMultipleAndReturnFirst_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals(null, set.removeAndReturnFirst("a", 0)) ;
		assertEquals(null, set.removeAndReturnFirst("q", 0)) ;
		assertEquals("a", set.removeAndReturnFirst("a", 1)) ;
		assertEquals("q", set.removeAndReturnFirst("q", 1)) ;
		put("a") ;
		put("q") ;
		assertEquals("q", set.removeAndReturnFirst("q", 1)) ;
		assertEquals("a", set.removeAndReturnFirst("a", 1)) ;
		checkAndClear(0) ;
	}
	
	public void testFirst_whenNotPresent() {		
		checkEmpty() ;
		assertEquals(null, set.first("a")) ;
		put("a") ;
		assertEquals(null, set.first("q")) ;
		checkAndClear(1) ;
	}
	
	public void testFirst_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String a2 = new String("a") ;
		final String q1 = "q" ;
		final String q2 = new String("q") ;
		put(a1) ;
		add(a2) ; // add should not alter the state of the set, as an equal value is already present
		assertSame(a1, set.first(a1)) ;
		put(a2) ; // put with the new equal value should replace the existing value
		assertSame(a2, set.first(a1)) ;
		put(q1) ;
		put(q2) ;
		assertSame(q2, set.first(q1)) ;
		assertSame(a2, set.first(a1)) ;
		checkAndClear(2) ;
	}

	public void testGet_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, set.get("a")) ;
		put("a") ;
		assertEquals(null, set.get("q")) ;
		checkAndClear(1) ;
	}
	
	public void testGet_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String a2 = new String("a") ;
		final String q1 = "q" ;
		final String q2 = new String("q") ;
		put(a1) ;
		add(a2) ; // add should not alter the state of the set, as an equal value is already present
		assertSame(a1, set.get(a1)) ;
		put(a2) ; // put with the new equal value should replace the existing value
		assertSame(a2, set.get(a1)) ;
		put(q1) ;
		put(q2) ;
		assertSame(q2, set.get(q1)) ;
		assertSame(a2, set.get(a1)) ;
		checkAndClear(2) ;
	}

	public void testApply_whenNotPresent() {
		checkEmpty() ;
		assertFalse(set.apply("a")) ;
		put("a") ;
		assertFalse(set.apply("q")) ;
		checkAndClear(1) ;
	}
	
	public void testApply_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		assertTrue(set.apply(a1)) ;
		assertTrue(set.apply(q1)) ;
		checkAndClear(2) ;
	}
	
	public void testAll_whenNotPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		checkIterableContents(Arrays.asList(), set.all(a1), true) ;
		put(a1) ;
		checkIterableContents(Arrays.asList(), set.all(q1), true) ;
		checkAndClear(1) ;
	}
	
	public void testAll_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		checkIterableContents(Arrays.asList(a1), set.all(a1), true) ;
		checkIterableContents(Arrays.asList(q1), set.all(q1), true) ;
		checkAndClear(2) ;
	}
	
	public void testAll_removals() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		Iterator<?> iter ;
		
		iter = set.all(a1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(a1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertFalse(set.contains(a1)) ;
		assertTrue(set.contains(q1)) ;
		
		iter = set.all(q1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(q1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertFalse(set.contains(a1)) ;
		assertFalse(set.contains(q1)) ;
		
		put(a1) ;
		put(q1) ;
		
		iter = set.all(q1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(q1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertTrue(set.contains(a1)) ;
		assertFalse(set.contains(q1)) ;
		
		iter = set.all(a1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(a1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertFalse(set.contains(a1)) ;
		assertFalse(set.contains(q1)) ;
	}
	
	public void testList_whenNotPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		checkListContents(Arrays.asList(), set.list(a1), true) ;
		put(a1) ;
		checkListContents(Arrays.asList(), set.list(q1), true) ;
		checkAndClear(1) ;
	}
	
	public void testList_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		checkListContents(Arrays.asList(a1), set.list(a1), true) ;
		checkListContents(Arrays.asList(q1), set.list(q1), true) ;
		checkAndClear(2) ;
	}
	
	public void testContains() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		assertEquals(false, set.contains(a1)) ;
		assertEquals(false, set.contains(q1)) ;
		put(a1) ;
		assertEquals(true, set.contains(a1)) ;
		assertEquals(false, set.contains(q1)) ;
		add(q1) ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(true, set.contains(a1)) ;
		assertEquals(true, set.contains(q1)) ;
		set.remove(a1) ;
		assertEquals(false, set.contains(a1)) ;
		assertEquals(true, set.contains(q1)) ;
		set.remove(q1) ;
		assertEquals(false, set.contains(a1)) ;
		assertEquals(false, set.contains(q1)) ;
		checkAndClear(0) ;
	}
	
	public void testCount() {		
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		assertEquals(0, set.count(a1)) ;
		assertEquals(0, set.count(q1)) ;
		put(a1) ;
		assertEquals(1, set.count(a1)) ;
		assertEquals(0, set.count(q1)) ;
		add(q1) ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(1, set.count(a1)) ;
		assertEquals(1, set.count(q1)) ;
		set.remove(a1) ;
		assertEquals(0, set.count(a1)) ;
		assertEquals(1, set.count(q1)) ;
		set.remove(q1) ;
		assertEquals(0, set.count(a1)) ;
		assertEquals(0, set.count(q1)) ;
		checkAndClear(0) ;
	}
	
	public void testIterator() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		checkIteratorContents(Arrays.asList(null, "a", "q", "b", "c").iterator(), set.iterator(), keepsOrdering) ;
		checkAndClear(5) ;
	}
	
	// test concurrent modifications
	
	public void testIteratorRemovals() {		
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		checkIteratorContents(Arrays.asList(null, "a", "q", "b", "c").iterator(), Iters.destroyAsConsumed(set.iterator()), keepsOrdering) ;
		assertFalse(set.contains(null)) ;
		assertFalse(set.contains("a")) ;
		assertFalse(set.contains("b")) ;
		assertFalse(set.contains("c")) ;
		assertFalse(set.contains("q")) ;
		checkAndClear(0) ;
		put(null) ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		final Iterator<String> expect = Arrays.asList(null, "a", "q", "b", "c").iterator() ;
		final Iterator<String> actual = set.iterator() ;
		while (expect.hasNext() && actual.hasNext()) {
			final String e = expect.next() ;
			final String a = actual.next() ;
			if (keepsOrdering) {
				assertSame(e, a) ;
			}
			if (a != null && a.equals("a"))
				actual.remove() ;
		}
		actual.remove() ;
		assertEquals(expect.hasNext(), actual.hasNext()) ;
		checkAndClear(3) ;
	}
	
	public void testCopy_whenEmpty() {
		checkEmpty() ;
		final HashSet<String, ?> copy = set.copy() ;
		assertNotSame(copy, set) ;
		assertEquals(set, copy) ;
		checkAndClear(0) ;
	}
	
	public void testCopy_whenNotEmpty() {
		checkEmpty() ;
		set.put("a") ;
		set.put("q") ;
		set.put("b") ;
		set.put(null) ;
		final HashSet<String, ?> copy = set.copy() ;
		assertNotSame(copy, set) ;
		assertEquals(set, copy) ;
		checkAndClear(4) ;
	}
	
	public void testGrowth() {
		checkEmpty() ;
		assertEquals(16, set.capacity()) ;
		final String[] ss = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" } ;
		for (String s : ss)
			set.put(s) ;
		assertEquals(64, set.capacity()) ;
		for (String s : ss)
			set.contains(s) ;
		checkAndClear(26) ;
	}
	
	public void testShrink() {
		checkEmpty() ;
		assertEquals(16, set.capacity()) ;
		final String[] ss = new String[] { "a", "b", "c", "d" } ;
		for (String s : ss)
			set.put(s) ;
		set.shrink() ;
		assertTrue(16 > set.capacity()) ;
		checkAndClear(4) ;
	}
	
	public void testResize() {
		checkEmpty() ;
		
		String[] ss = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" } ;
		for (String s : ss)
			set.put(s) ;
		set.resize(8) ;
		assertEquals(8, set.capacity()) ;
		for (String s : ss)
			set.contains(s) ;
		set.resize(32) ;
		for (String s : ss)
			set.contains(s) ;
		set.resize(16) ;
		for (String s : ss)
			set.contains(s) ;
		checkAndClear(26) ;
		
		ss = new String[] { "a", "q" } ;
		for (String s : ss)
			set.put(s) ;
		set.resize(8) ;
		assertEquals(8, set.capacity()) ;
		for (String s : ss)
			set.contains(s) ;
		set.resize(16) ;
		assertEquals(16, set.capacity()) ;
		for (String s : ss)
			set.contains(s) ;
		set.resize(64) ;
		for (String s : ss)
			set.contains(s) ;
		checkAndClear(2) ;
	}
	
	public void testEquality() {
		assertNotNull(set.equality()) ;
	}

	public void testUnique() {
		assertEquals(set, set.unique()) ;
	}
	
	public void testPermitsDuplicates() {
		assertFalse(set.permitsDuplicates()) ;
	}
	
	public void testToString() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("q") ;
		assertEquals("{null, a, q}", set.toString()) ;
		checkAndClear(3) ;
	}
	
}
