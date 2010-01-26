package org.jjoost.collections;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import junit.framework.TestCase ;

import org.jjoost.collections.sets.base.HashSet ;
import org.jjoost.util.Iters ;
/**
 * This class is the basis for testing all implementations of Set and Map that use a regular HashStore as backing
 * @author b.elliottsmith
 *
 */
public abstract class AbstractHashStoreBasedScalarCollectionTest extends AbstractTest {

	protected abstract String put(String v) ;
	protected abstract String putIfAbsent(String v) ;
	protected abstract boolean add(String v) ;
	
	protected abstract int remove(String v) ;
	protected abstract String removeAndReturnFirst(String v) ;
	protected abstract Iterable<String> removeAndReturn(String v) ;
	
	protected abstract boolean contains(String v) ;
	protected abstract int count(String v) ;
	protected abstract List<String> list(String v) ;
	protected abstract Iterable<String> iterate(String v) ;
	protected abstract String get(String v) ;
	protected abstract String first(String v) ;
	
	protected abstract int clear() ;
	protected abstract Iterator<String> clearAndReturn() ;
	
	protected abstract boolean isEmpty() ;
	protected abstract int uniqueCount() ;
	protected abstract int totalCount() ;
	protected abstract int size() ;
	protected abstract int capacity() ;
	protected abstract void shrink() ;
	protected abstract void resize(int i) ;
	protected abstract Iterator<String> iterator() ;
	
	protected void checkEmpty() {
		assertEquals(0, size()) ;
	}
	
	protected void checkSize(int expect) {
		assertEquals(expect, size()) ;
	}
	
	protected void checkAndClear(int expect) {
		assertEquals(expect, clear()) ;
	}
	
	public void testIsEmpty_whenEmpty() {
		assertTrue(isEmpty()) ;
	}
	
	public void testIsEmpty_whenNotEmpty() {
		put("a") ;
		assertFalse(isEmpty()) ;
		clear() ;
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
		remove("a") ;
		checkSize(3) ;
		clear() ;
	}
	
	public void testTotalCount_whenEmpty() {
		assertEquals(0, totalCount()) ;
	}
	
	public void testTotalCount_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("b") ;
		put("c") ;
		remove("a") ;
		assertEquals(3, totalCount()) ;
		clear() ;
	}
	
	public void testUniqueCount_whenEmpty() {
		assertEquals(0, uniqueCount()) ;
	}
	
	public void testUniqueCount_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("b") ;
		put("c") ;
		remove("a") ;
		assertEquals(3, uniqueCount()) ;
		clear() ;
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
		checkIteratorContents(Iters.emptyIterator(), clearAndReturn(), true) ;
	}
	
	public void testClearAndReturn_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put(null) ;
		// also perform removes just to test the no-op remove() on this Iterator
		checkIteratorContents(Arrays.asList(null, "a", "q").iterator(), Iters.destroyAsConsumed(clearAndReturn()), true) ;
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
		assertEquals(null, putIfAbsent("a")) ;
		assertEquals(null, putIfAbsent("q")) ;
		assertEquals(null, putIfAbsent(null)) ;
		checkAndClear(3) ;
	}
	
	public void testPutIfAbsent_whenPresent() {
		checkEmpty() ;
		putIfAbsent("a") ;
		putIfAbsent("q") ;
		assertEquals("a", putIfAbsent("a")) ;
		assertEquals("q", putIfAbsent("q")) ;
		checkAndClear(2) ;
	}
	
	public void testRemoveAll_whenNotPresent() {
		checkEmpty() ;
		assertEquals(0, remove("a")) ;
		put("q") ;
		assertEquals(0, remove("a")) ;
		checkAndClear(1) ;
	}
	
	public void testRemoveAll_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals(1, remove("a")) ;
		assertEquals(1, remove("q")) ;
		put("a") ;
		put("q") ;
		assertEquals(1, remove("q")) ;
		assertEquals(1, remove("a")) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveAllAndReturn_whenNotPresent() {
		checkEmpty() ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a"), true) ;
		put("q") ;
		checkIterableContents(Arrays.asList(), removeAndReturn("a"), true) ;
		checkAndClear(1) ;
	}
	
	public void testRemoveAllAndReturn_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("a"), removeAndReturn("a"), true) ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q"), true) ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q"), true) ;
		checkIterableContents(Arrays.asList("a"), removeAndReturn("a"), true) ;
		checkAndClear(0) ;
	}
	
	public void testRemoveAllAndReturnFirst_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, removeAndReturnFirst("a")) ;
		put("q") ;
		assertEquals(null, removeAndReturnFirst("a")) ;
		checkAndClear(1) ;
	}
	
	public void testRemoveAllAndReturnFirst_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		assertEquals("a", removeAndReturnFirst("a")) ;
		assertEquals("q", removeAndReturnFirst("q")) ;
		put("a") ;
		put("q") ;
		assertEquals("q", removeAndReturnFirst("q")) ;
		assertEquals("a", removeAndReturnFirst("a")) ;
		checkAndClear(0) ;
	}
	
	public void testFirst_whenNotPresent() {		
		checkEmpty() ;
		assertEquals(null, first("a")) ;
		put("a") ;
		assertEquals(null, first("q")) ;
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
		assertSame(a1, first(a1)) ;
		put(a2) ; // put with the new equal value should replace the existing value
		assertSame(a2, first(a1)) ;
		put(q1) ;
		put(q2) ;
		assertSame(q2, first(q1)) ;
		assertSame(a2, first(a1)) ;
		checkAndClear(2) ;
	}

	public void testGet_whenNotPresent() {
		checkEmpty() ;
		assertEquals(null, get("a")) ;
		put("a") ;
		assertEquals(null, get("q")) ;
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
		assertSame(a1, get(a1)) ;
		put(a2) ; // put with the new equal value should replace the existing value
		assertSame(a2, get(a1)) ;
		put(q1) ;
		put(q2) ;
		assertSame(q2, get(q1)) ;
		assertSame(a2, get(a1)) ;
		checkAndClear(2) ;
	}

	public void testAll_whenNotPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		checkIterableContents(Arrays.asList(), iterate(a1), true) ;
		put(a1) ;
		checkIterableContents(Arrays.asList(), iterate(q1), true) ;
		checkAndClear(1) ;
	}
	
	public void testAll_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		checkIterableContents(Arrays.asList(a1), iterate(a1), true) ;
		checkIterableContents(Arrays.asList(q1), iterate(q1), true) ;
		checkAndClear(2) ;
	}
	
	public void testAll_removals() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		Iterator<?> iter ;
		
		iter = iterate(a1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(a1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertFalse(contains(a1)) ;
		assertTrue(contains(q1)) ;
		
		iter = iterate(q1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(q1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertFalse(contains(a1)) ;
		assertFalse(contains(q1)) ;
		
		put(a1) ;
		put(q1) ;
		
		iter = iterate(q1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(q1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertTrue(contains(a1)) ;
		assertFalse(contains(q1)) ;
		
		iter = iterate(a1).iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame(a1, iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertFalse(contains(a1)) ;
		assertFalse(contains(q1)) ;
	}
	
	public void testList_whenNotPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		checkListContents(Arrays.asList(), list(a1), true) ;
		put(a1) ;
		checkListContents(Arrays.asList(), list(q1), true) ;
		checkAndClear(1) ;
	}
	
	public void testList_whenPresent() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		put(a1) ;
		put(q1) ;
		checkListContents(Arrays.asList(a1), list(a1), true) ;
		checkListContents(Arrays.asList(q1), list(q1), true) ;
		checkAndClear(2) ;
	}
	
	public void testContains() {
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		assertEquals(false, contains(a1)) ;
		assertEquals(false, contains(q1)) ;
		put(a1) ;
		assertEquals(true, contains(a1)) ;
		assertEquals(false, contains(q1)) ;
		add(q1) ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(true, contains(a1)) ;
		assertEquals(true, contains(q1)) ;
		remove(a1) ;
		assertEquals(false, contains(a1)) ;
		assertEquals(true, contains(q1)) ;
		remove(q1) ;
		assertEquals(false, contains(a1)) ;
		assertEquals(false, contains(q1)) ;
		checkAndClear(0) ;
	}
	
	public void testCount() {		
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		assertEquals(0, count(a1)) ;
		assertEquals(0, count(q1)) ;
		put(a1) ;
		assertEquals(1, count(a1)) ;
		assertEquals(0, count(q1)) ;
		add(q1) ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(1, count(a1)) ;
		assertEquals(1, count(q1)) ;
		remove(a1) ;
		assertEquals(0, count(a1)) ;
		assertEquals(1, count(q1)) ;
		remove(q1) ;
		assertEquals(0, count(a1)) ;
		assertEquals(0, count(q1)) ;
		checkAndClear(0) ;
	}
	
	public void testIterator() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		checkIteratorContents(Arrays.asList(null, "a", "q", "b", "c").iterator(), iterator(), true) ;
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
		checkIteratorContents(Arrays.asList(null, "a", "q", "b", "c").iterator(), Iters.destroyAsConsumed(iterator()), true) ;
		assertFalse(contains(null)) ;
		assertFalse(contains("a")) ;
		assertFalse(contains("b")) ;
		assertFalse(contains("c")) ;
		assertFalse(contains("q")) ;
		checkAndClear(0) ;
		put(null) ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		final Iterator<String> expect = Arrays.asList(null, "a", "q", "b", "c").iterator() ;
		final Iterator<String> actual = iterator() ;
		while (expect.hasNext() && actual.hasNext()) {
			final String e = expect.next() ;
			final String a = actual.next() ;
			assertSame(e, a) ;
			if (a != null && a.equals("a"))
				actual.remove() ;
		}
		actual.remove() ;
		assertEquals(expect.hasNext(), actual.hasNext()) ;
		checkAndClear(3) ;
	}
	
	public void testGrowth() {
		checkEmpty() ;
		assertEquals(16, capacity()) ;
		final String[] ss = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" } ;
		for (String s : ss)
			put(s) ;
		assertEquals(64, capacity()) ;
		for (String s : ss)
			contains(s) ;
		checkAndClear(26) ;
	}
	
	public void testShrink() {
		checkEmpty() ;
		assertEquals(16, capacity()) ;
		final String[] ss = new String[] { "a", "b", "c", "d" } ;
		for (String s : ss)
			put(s) ;
		shrink() ;
		assertTrue(16 > capacity()) ;
		checkAndClear(4) ;
	}
	
	public void testResize() {
		checkEmpty() ;
		
		String[] ss = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" } ;
		for (String s : ss)
			put(s) ;
		resize(8) ;
		assertEquals(8, capacity()) ;
		for (String s : ss)
			contains(s) ;
		resize(32) ;
		for (String s : ss)
			contains(s) ;
		resize(16) ;
		for (String s : ss)
			contains(s) ;
		checkAndClear(26) ;
		
		ss = new String[] { "a", "q" } ;
		for (String s : ss)
			put(s) ;
		resize(8) ;
		assertEquals(8, capacity()) ;
		for (String s : ss)
			contains(s) ;
		resize(16) ;
		assertEquals(16, capacity()) ;
		for (String s : ss)
			contains(s) ;
		resize(64) ;
		for (String s : ss)
			contains(s) ;
		checkAndClear(2) ;
	}
	
}
