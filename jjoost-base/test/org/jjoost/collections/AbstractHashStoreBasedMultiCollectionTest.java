package org.jjoost.collections;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.jjoost.util.Iters ;
/**
 * This class is the basis for testing all implementations of Set and Map that use a regular HashStore as backing
 * @author b.elliottsmith
 *
 */
public abstract class AbstractHashStoreBasedMultiCollectionTest extends AbstractTest {

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
	protected abstract String first(String v) ;
	
	protected abstract int clear() ;
	protected abstract Iterator<String> clearAndReturn() ;
	
	protected abstract boolean isEmpty() ;
	protected abstract int uniqueCount() ;
	protected abstract int totalCount() ;
	protected abstract int capacity() ;
	protected abstract void shrink() ;
	protected abstract void resize(int i) ;
	protected abstract Iterator<String> iterator() ;
	
	protected void checkEmpty() {
		assertEquals(0, totalCount()) ;
	}
	
	protected void checkSize(int expect) {
		assertEquals(expect, totalCount()) ;
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
		put("a") ;
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
		put("a") ;
		put("q") ;
		put("b") ;
		put("c") ;
		put(null) ;
		checkAndClear(6) ;
	}
	
	public void testClearAndReturn_whenEmpty() {
		checkEmpty() ;
		checkIteratorContents(Iters.emptyIterator(), clearAndReturn(), true) ;
	}
	
	public void testClearAndReturn_whenNotEmpty() {
		checkEmpty() ;
		put("a") ;
		put("q") ;
		put("a") ;
		put(null) ;
		// also perform removes just to test the no-op remove() on this Iterator
		checkIteratorContents(Arrays.asList(null, "a", "a", "q").iterator(), Iters.destroyAsConsumed(clearAndReturn()), true) ;
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
		assertEquals(null, put("a")) ;
		assertEquals(null, put("q")) ;
		assertEquals(null, put(null)) ;
		assertEquals(null, put("a")) ;
		assertEquals(null, put("q")) ;
		assertEquals(null, put(null)) ;
		checkAndClear(6) ;
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
		assertEquals(true, add("a")) ;
		assertEquals(true, add("q")) ;
		assertEquals(true, add(null)) ;
		assertEquals(true, add("a")) ;
		assertEquals(true, add("q")) ;
		assertEquals(true, add(null)) ;
		checkAndClear(6) ;
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
		final String a1 = "a" ;
		final String a2 = new String("a") ;
		final String q1 = "q" ;
		final String q2 = new String("q") ;
		putIfAbsent(a1) ;
		putIfAbsent(q1) ;
		assertSame(a1, putIfAbsent(a2)) ;
		assertSame(q1, putIfAbsent(q2)) ;
		assertSame(a1, first(a2)) ;
		assertSame(q1, first(q2)) ;
		checkAndClear(2) ;
	}
	
	public void testRemoveAll_whenNotPresent() {
		checkEmpty() ;
		assertEquals(0, remove("a")) ;
		put("q") ;
		put("q") ;
		assertEquals(0, remove("a")) ;
		checkAndClear(2) ;
	}
	
	public void testRemoveAll_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals(3, remove("a")) ;
		assertEquals(1, remove("q")) ;
		put("a") ;
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals(1, remove("q")) ;
		assertEquals(3, remove("a")) ;
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
		put("a") ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("a", "a", "a"), removeAndReturn("a"), true) ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q"), true) ;
		put("a") ;
		put("a") ;
		put("a") ;
		put("q") ;
		checkIterableContents(Arrays.asList("q"), removeAndReturn("q"), true) ;
		checkIterableContents(Arrays.asList("a", "a", "a"), removeAndReturn("a"), true) ;
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
		put("a") ;
		put("a") ;
		put("q") ;
		assertEquals("a", removeAndReturnFirst("a")) ;
		assertEquals(1, totalCount()) ;
		assertEquals("q", removeAndReturnFirst("q")) ;
		put("a") ;
		put("a") ;
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
		put(a2) ;
		assertSame(a1, first(a1)) ;
		put(q1) ;
		put(q2) ;
		assertSame(q1, first(q1)) ;
		checkAndClear(4) ;
	}

	public void testAll_whenNotPresent() {
		checkEmpty() ;
		checkIterableContents(Arrays.asList(), iterate("a"), true) ;
		put("a") ;
		put("a") ;
		checkIterableContents(Arrays.asList(), iterate("q"), true) ;
		checkAndClear(2) ;
	}
	
	public void testAll_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("a") ;
		put("q") ;
		put("q") ;
		checkIterableContents(Arrays.asList("a", "a"), iterate("a"), true) ;
		checkIterableContents(Arrays.asList("q", "q"), iterate("q"), true) ;
		checkAndClear(4) ;
	}
	
	public void testAll_removals() {
		checkEmpty() ;
		put("a") ;
		put("a") ;
		put("a") ;
		put("q") ;
		put("q") ;
		Iterator<?> iter ;
		
		iter = iterate("a").iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame("a", iter.next()) ;
		iter.remove() ;
		assertTrue(iter.hasNext()) ;
		assertSame("a", iter.next()) ;
		assertTrue(iter.hasNext()) ;
		assertSame("a", iter.next()) ;
		assertFalse(iter.hasNext()) ;
		assertEquals(2, count("a")) ;
		assertEquals(2, count("q")) ;
		
		iter = iterate("q").iterator() ;
		assertTrue(iter.hasNext()) ;
		assertSame("q", iter.next()) ;
		iter.remove() ;
		assertTrue(iter.hasNext()) ;
		assertSame("q", iter.next()) ;
		iter.remove() ;
		assertFalse(iter.hasNext()) ;
		assertEquals(2, count("a")) ;
		assertEquals(0, count("q")) ;
	}
	
	public void testList_whenNotPresent() {
		checkEmpty() ;
		checkListContents(Arrays.asList(), list("a"), true) ;
		put("a") ;
		checkListContents(Arrays.asList(), list("q"), true) ;
		checkAndClear(1) ;
	}
	
	public void testList_whenPresent() {
		checkEmpty() ;
		put("a") ;
		put("a") ;
		put("q") ;
		put("q") ;
		checkListContents(Arrays.asList("a", "a"), list("a"), true) ;
		checkListContents(Arrays.asList("q", "q"), list("q"), true) ;
		checkAndClear(4) ;
	}
	
	public void testContains() {
		checkEmpty() ;
		assertEquals(false, contains("a")) ;
		assertEquals(false, contains("q")) ;
		put("a") ;
		put("a") ;
		assertEquals(true, contains("a")) ;
		assertEquals(false, contains("q")) ;
		add("q") ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(true, contains("a")) ;
		assertEquals(true, contains("q")) ;
		remove("a") ;
		assertEquals(false, contains("a")) ;
		assertEquals(true, contains("q")) ;
		remove("q") ;
		assertEquals(false, contains("a")) ;
		assertEquals(false, contains("q")) ;
		checkAndClear(0) ;
	}
	
	public void testCount() {		
		checkEmpty() ;
		final String a1 = "a" ;
		final String q1 = "q" ;
		assertEquals(0, count(a1)) ;
		assertEquals(0, count(q1)) ;
		put(a1) ;
		put(a1) ;
		put(a1) ;
		assertEquals(3, count(a1)) ;
		assertEquals(0, count(q1)) ;
		add(q1) ;
		add(q1) ;
		assertEquals(3, count(a1)) ;
		assertEquals(2, count(q1)) ;
		remove(a1) ;
		assertEquals(0, count(a1)) ;
		assertEquals(2, count(q1)) ;
		remove(q1) ;
		assertEquals(0, count(a1)) ;
		assertEquals(0, count(q1)) ;
		checkAndClear(0) ;
	}
	
	public void testIterator() {
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		put("q") ;
		checkIteratorContents(Arrays.asList(null, "a", "a", "q", "q", "b", "c").iterator(), iterator(), true) ;
		checkAndClear(7) ;
	}
	
	// test concurrent modifications
	
	public void testIteratorRemovals() {		
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		put("a") ;
		put("q") ;
		checkIteratorContents(Arrays.asList(null, "a", "a", "q", "q", "b", "c").iterator(), Iters.destroyAsConsumed(iterator()), true) ;
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
		put("a") ;
		put("q") ;
		final Iterator<String> expect = Arrays.asList(null, "a", "a", "q", "q", "b", "c").iterator() ;
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
		checkAndClear(4) ;
	}
	
	public void testGrowth() {
		checkEmpty() ;
		assertEquals(16, capacity()) ;
		final String[] ss = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" } ;
		for (String s : ss) {
			put(s) ;
			put(s) ;
		}
		assertEquals(128, capacity()) ;
		for (String s : ss)
			assertEquals(2, count(s)) ;
		checkAndClear(52) ;
	}
	
	public void testShrink() {
		checkEmpty() ;
		assertEquals(16, capacity()) ;
		final String[] ss = new String[] { "a", "q" } ;
		for (String s : ss) {
			put(s) ;
			put(s) ;
		}
		shrink() ;
		assertTrue(16 > capacity()) ;
		checkAndClear(4) ;
	}
	
	public void testResize() {
		checkEmpty() ;
		
		String[] ss = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" } ;
		for (String s : ss) {
			put(s) ;
			put(s) ;
		}
		resize(8) ;
		assertEquals(8, capacity()) ;
		for (String s : ss)
			assertEquals(2, count(s)) ;
		resize(32) ;
		for (String s : ss)
			assertEquals(2, count(s)) ;
		resize(16) ;
		for (String s : ss)
			assertEquals(2, count(s)) ;
		checkAndClear(52) ;
		
		ss = new String[] { "a", "q" } ;
		for (String s : ss) {
			put(s) ;
			put(s) ;
		}
		resize(8) ;
		assertEquals(8, capacity()) ;
		for (String s : ss)
			assertEquals(2, count(s)) ;
		resize(16) ;
		assertEquals(16, capacity()) ;
		for (String s : ss)
			assertEquals(2, count(s)) ;
		resize(64) ;
		for (String s : ss)
			assertEquals(2, count(s)) ;
		checkAndClear(4) ;
	}
	
}
