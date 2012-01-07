package org.jjoost.collections.sets.base;

import java.util.Arrays;
import java.util.Iterator;

import org.jjoost.collections.AbstractTest;
import org.jjoost.collections.Set;
import org.jjoost.util.Iters;

public abstract class GeneralSetTest extends AbstractTest {
	
	protected final Set<String> set = getSet();

	protected abstract Set<String> getSet();
	protected abstract String put(String v);
	protected abstract boolean add(String v);
	
	protected void checkEmpty() {
		assertEquals(0, set.size());
	}
	
	protected void checkSize(int expect) {
		assertEquals(expect, set.size());
	}
	
	protected void checkAndClear(int expect) {
		assertEquals(expect, set.clear());
	}
	
	public void testIsEmpty_whenEmpty() {
		assertTrue(set.isEmpty());
	}
	
	public void testIsEmpty_whenNotEmpty() {
		put("a");
		assertFalse(set.isEmpty());
		set.clear();
	}
	
	public void testSize_whenEmpty() {
		checkEmpty();
	}
	
	public void testSize_whenNotEmpty() {
		checkEmpty();
		put("a");
		put("b");
		put("c");
		set.remove("a");
		checkSize(2);
		set.clear();
	}
	
	public void testTotalCount_whenEmpty() {
		assertEquals(0, set.totalCount());
	}
	
	public void testTotalCount_whenNotEmpty() {
		checkEmpty();
		put("a");
		put("b");
		put("c");
		set.remove("a");
		assertEquals(2, set.totalCount());
		set.clear();
	}
	
	public void testUniqueCount_whenEmpty() {
		assertEquals(0, set.uniqueCount());
	}
	
	public void testUniqueCount_whenNotEmpty() {
		checkEmpty();
		put("a");
		put("b");
		put("c");
		set.remove("a");
		assertEquals(2, set.uniqueCount());
		set.clear();
	}
	
	public void testClear_whenEmpty() {
		checkEmpty();
		checkAndClear(0);
	}
	
	public void testClear_whenNotEmpty() {
		checkEmpty();
		put("a");
		put("b");
		put("c");
		put(null);
		checkAndClear(4);
	}
	
	public void testClearAndReturn_whenEmpty() {
		checkEmpty();
		checkIteratorContents(Iters.emptyIterator(), set.clearAndReturn(), true);
	}
	
	public void testClearAndReturn_whenNotEmpty() {
		checkEmpty();
		put("a");
		put(null);
		// also perform removes just to test the no-op remove() on this Iterator
		checkIteratorContents(Arrays.asList(null, "a").iterator(), Iters.destroyAsConsumed(set.clearAndReturn()), false);
		checkAndClear(0);
	}
	
	public void testPut_whenNotPresent() {
		checkEmpty();
		assertEquals(null, put("a"));
		assertEquals(null, put("b"));
		assertEquals(null, put(null));
		checkAndClear(3);
	}
	
	public void testPut_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String b1 = "b";
		final String b2 = new String("b");
		put(a1);
		assertSame(a1, put(a2));
		assertSame(a2, put(a1));
		put(b1);
		assertSame(b1, put(b2));
		assertSame(b2, put(b1));
		put(null);
		assertEquals(null, put(null));
		checkAndClear(3);
	}
	
	public void testAdd_whenNotPresent() {
		checkEmpty();
		assertEquals(true, add("a"));
		assertEquals(true, add("b"));
		assertEquals(true, add(null));
		checkAndClear(3);
	}
	
	public void testAdd_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String b1 = "b";
		final String b2 = new String("b");
		add(a1);
		assertEquals(false, add(a2));
		add(b1);
		assertEquals(false, add(b2));
		add(null);
		assertEquals(false, add(null));
		checkAndClear(3);
	}
	
	public void testPutIfAbsent_whenNotPresent() {
		checkEmpty();
		assertEquals(null, set.putIfAbsent("a"));
		assertEquals(null, set.putIfAbsent("b"));
		assertEquals(null, set.putIfAbsent(null));
		checkAndClear(3);
	}
	
	public void testPutIfAbsent_whenPresent() {
		checkEmpty();
		set.putIfAbsent("a");
		set.putIfAbsent("b");
		assertEquals("a", set.putIfAbsent("a"));
		assertEquals("b", set.putIfAbsent("b"));
		checkAndClear(2);
	}
	
	public void testPutAll_whenNotPresent() {
		checkEmpty();
		assertEquals(4, set.putAll(Arrays.asList("a", "b", "b", null)));
		checkAndClear(4);
	}
	
	public void testPutAll_whenPresent() {
		checkEmpty();
		set.putAll(Arrays.asList("a", "b", "b", null));
		assertEquals(0, set.putAll(Arrays.asList("a", "b", "b", null)));
		checkAndClear(4);
	}
	
	public void testRemoveAll_whenNotPresent() {
		checkEmpty();
		assertEquals(0, set.remove("a"));
		put("b");
		assertEquals(0, set.remove("a"));
		checkAndClear(1);
	}
	
	public void testRemoveAll_whenPresent() {
		checkEmpty();
		put("a");
		put("b");
		assertEquals(1, set.remove("a"));
		assertEquals(1, set.remove("b"));
		put("a");
		put("b");
		assertEquals(1, set.remove("b"));
		assertEquals(1, set.remove("a"));
		checkAndClear(0);
	}
	
	public void testRemoveMultiple_whenNotPresent() {		
		checkEmpty();
		assertEquals(0, set.remove("a", 0));
		assertEquals(0, set.remove("a", 1));
		put("b");
		assertEquals(0, set.remove("a", 1));
		try {
			set.remove("a", -1);
			assertTrue(false);
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1);
	}
	
	public void testRemoveMultiple_whenPresent() {		
		checkEmpty();
		put("a");
		put("b");
		assertEquals(0, set.remove("a", 0));
		assertEquals(0, set.remove("b", 0));
		assertEquals(1, set.remove("a", 100));
		assertEquals(1, set.remove("b", 100));
		put("a");
		put("b");
		assertEquals(1, set.remove("b", 100));
		assertEquals(1, set.remove("a", 100));
		checkAndClear(0);
	}
	
	public void testRemoveAllAndReturn_whenNotPresent() {
		checkEmpty();
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a"), true);
		put("b");
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a"), true);
		checkAndClear(1);
	}
	
	public void testRemoveAllAndReturn_whenPresent() {
		checkEmpty();
		put("a");
		put("b");
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a"), true);
		checkIterableContents(Arrays.asList("b"), set.removeAndReturn("b"), true);
		put("a");
		put("b");
		checkIterableContents(Arrays.asList("b"), set.removeAndReturn("b"), true);
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a"), true);
		checkAndClear(0);
	}
	
	public void testRemoveMultipleAndReturn_whenNotPresent() {
		checkEmpty();
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 0), true);
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 1), true);
		put("b");
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 0), true);
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 1), true);
		try {
			set.removeAndReturn("a", -1);
			assertTrue(false);
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1);
	}
	
	public void testRemoveMultipleAndReturn_whenPresent() {
		checkEmpty();
		put("a");
		put("b");
		checkIterableContents(Arrays.asList(), set.removeAndReturn("a", 0), true);
		checkIterableContents(Arrays.asList(), set.removeAndReturn("b", 0), true);
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a", 1), true);
		checkIterableContents(Arrays.asList("b"), set.removeAndReturn("b", 1), true);
		put("a");
		put("b");
		checkIterableContents(Arrays.asList("b"), set.removeAndReturn("b", 1), true);
		checkIterableContents(Arrays.asList("a"), set.removeAndReturn("a", 1), true);
		checkAndClear(0);
	}
	
	public void testRemoveAllAndReturnFirst_whenNotPresent() {
		checkEmpty();
		assertEquals(null, set.removeAndReturnFirst("a", 0));
		put("b");
		assertEquals(null, set.removeAndReturnFirst("a", 0));
		try {
			set.removeAndReturnFirst("a", -1);
			assertTrue(false);
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1);
	}
	
	public void testRemoveAllAndReturnFirst_whenPresent() {
		checkEmpty();
		put("a");
		put("b");
		assertEquals("a", set.removeAndReturnFirst("a"));
		assertEquals("b", set.removeAndReturnFirst("b"));
		put("a");
		put("b");
		assertEquals("b", set.removeAndReturnFirst("b"));
		assertEquals("a", set.removeAndReturnFirst("a"));
		checkAndClear(0);
	}
	
	public void testRemoveMultipleAndReturnFirst_whenNotPresent() {
		checkEmpty();
		assertEquals(null, set.removeAndReturnFirst("a", 0));
		put("b");
		assertEquals(null, set.removeAndReturnFirst("a", 0));
		try {
			set.removeAndReturnFirst("a", -1);
			assertTrue(false);
		} catch (IllegalArgumentException e) {			
		}
		checkAndClear(1);
	}
	
	public void testRemoveMultipleAndReturnFirst_whenPresent() {
		checkEmpty();
		put("a");
		put("b");
		assertEquals(null, set.removeAndReturnFirst("a", 0));
		assertEquals(null, set.removeAndReturnFirst("b", 0));
		assertEquals("a", set.removeAndReturnFirst("a", 1));
		assertEquals("b", set.removeAndReturnFirst("b", 1));
		put("a");
		put("b");
		assertEquals("b", set.removeAndReturnFirst("b", 1));
		assertEquals("a", set.removeAndReturnFirst("a", 1));
		checkAndClear(0);
	}
	
	public void testFirst_whenNotPresent() {		
		checkEmpty();
		assertEquals(null, set.first("a"));
		put("a");
		assertEquals(null, set.first("b"));
		checkAndClear(1);
	}
	
	public void testFirst_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String b1 = "b";
		final String b2 = new String("b");
		put(a1);
		add(a2) ; // add should not alter the state of the set, as an equal value is already present
		assertSame(a1, set.first(a1));
		put(a2) ; // put with the new equal value should replace the existing value
		assertSame(a2, set.first(a1));
		put(b1);
		put(b2);
		assertSame(b2, set.first(b1));
		assertSame(a2, set.first(a1));
		checkAndClear(2);
	}

	public void testGet_whenNotPresent() {
		checkEmpty();
		assertEquals(null, set.get("a"));
		put("a");
		assertEquals(null, set.get("b"));
		checkAndClear(1);
	}
	
	public void testGet_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String b1 = "b";
		final String b2 = new String("b");
		put(a1);
		add(a2) ; // add should not alter the state of the set, as an equal value is already present
		assertSame(a1, set.get(a1));
		put(a2) ; // put with the new equal value should replace the existing value
		assertSame(a2, set.get(a1));
		put(b1);
		put(b2);
		assertSame(b2, set.get(b1));
		assertSame(a2, set.get(a1));
		checkAndClear(2);
	}

	public void testApply_whenNotPresent() {
		checkEmpty();
		assertFalse(set.apply("a"));
		put("a");
		assertFalse(set.apply("b"));
		checkAndClear(1);
	}
	
	public void testApply_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		put(a1);
		put(b1);
		assertTrue(set.apply(a1));
		assertTrue(set.apply(b1));
		checkAndClear(2);
	}
	
	public void testAll_whenNotPresent() {
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		checkIterableContents(Arrays.asList(), set.all(a1), true);
		put(a1);
		checkIterableContents(Arrays.asList(), set.all(b1), true);
		checkAndClear(1);
	}
	
	public void testAll_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		put(a1);
		put(b1);
		checkIterableContents(Arrays.asList(a1), set.all(a1), true);
		checkIterableContents(Arrays.asList(b1), set.all(b1), true);
		checkAndClear(2);
	}
	
	public void testAll_removals() {
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		put(a1);
		put(b1);
		Iterator<?> iter;
		
		iter = set.all(a1).iterator();
		assertTrue(iter.hasNext());
		assertSame(a1, iter.next());
		iter.remove();
		assertFalse(iter.hasNext());
		assertFalse(set.contains(a1));
		assertTrue(set.contains(b1));
		
		iter = set.all(b1).iterator();
		assertTrue(iter.hasNext());
		assertSame(b1, iter.next());
		iter.remove();
		assertFalse(iter.hasNext());
		assertFalse(set.contains(a1));
		assertFalse(set.contains(b1));
		
		put(a1);
		put(b1);
		
		iter = set.all(b1).iterator();
		assertTrue(iter.hasNext());
		assertSame(b1, iter.next());
		iter.remove();
		assertFalse(iter.hasNext());
		assertTrue(set.contains(a1));
		assertFalse(set.contains(b1));
		
		iter = set.all(a1).iterator();
		assertTrue(iter.hasNext());
		assertSame(a1, iter.next());
		iter.remove();
		assertFalse(iter.hasNext());
		assertFalse(set.contains(a1));
		assertFalse(set.contains(b1));
	}
	
	public void testList_whenNotPresent() {
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		checkListContents(Arrays.asList(), set.list(a1), true);
		put(a1);
		checkListContents(Arrays.asList(), set.list(b1), true);
		checkAndClear(1);
	}
	
	public void testList_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		put(a1);
		put(b1);
		checkListContents(Arrays.asList(a1), set.list(a1), true);
		checkListContents(Arrays.asList(b1), set.list(b1), true);
		checkAndClear(2);
	}
	
	public void testContains() {
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		assertEquals(false, set.contains(a1));
		assertEquals(false, set.contains(b1));
		put(a1);
		assertEquals(true, set.contains(a1));
		assertEquals(false, set.contains(b1));
		add(b1) ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(true, set.contains(a1));
		assertEquals(true, set.contains(b1));
		set.remove(a1);
		assertEquals(false, set.contains(a1));
		assertEquals(true, set.contains(b1));
		set.remove(b1);
		assertEquals(false, set.contains(a1));
		assertEquals(false, set.contains(b1));
		checkAndClear(0);
	}
	
	public void testCount() {		
		checkEmpty();
		final String a1 = "a";
		final String b1 = "b";
		assertEquals(0, set.count(a1));
		assertEquals(0, set.count(b1));
		put(a1);
		assertEquals(1, set.count(a1));
		assertEquals(0, set.count(b1));
		add(b1) ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(1, set.count(a1));
		assertEquals(1, set.count(b1));
		set.remove(a1);
		assertEquals(0, set.count(a1));
		assertEquals(1, set.count(b1));
		set.remove(b1);
		assertEquals(0, set.count(a1));
		assertEquals(0, set.count(b1));
		checkAndClear(0);
	}
	
	public void testIterator() {
		checkEmpty();
		put(null);
		put("a");
		put("b");
		put("c");
		put("d");
		checkIteratorContents(Arrays.asList(null, "a", "b", "c", "d").iterator(), set.iterator(), false);
		checkAndClear(5);
	}
	
	// test concurrent modifications
	
	public void testIteratorRemovals() {		
		checkEmpty();
		put(null);
		put("a");
		put("b");
		put("c");
		put("d");
		checkIteratorContents(Arrays.asList(null, "a", "b", "c", "d").iterator(), Iters.destroyAsConsumed(set.iterator()), false);
		assertFalse(set.contains(null));
		assertFalse(set.contains("a"));
		assertFalse(set.contains("b"));
		assertFalse(set.contains("c"));
		assertFalse(set.contains("d"));
		checkAndClear(0);
		put(null);
		put("a");
		put("b");
		put("c");
		put("d");
		final Iterator<String> actual = set.iterator();
		int c = 0;
		while (actual.hasNext()) {
			final String a = actual.next();
			if (a == null || a.equals("a"))
				actual.remove();
		}
		assertEquals(5, c);
		assertFalse(set.contains(null));
		assertFalse(set.contains("a"));
		assertTrue(set.contains("b"));
		assertTrue(set.contains("c"));
		assertTrue(set.contains("d"));
		checkAndClear(3);
	}
	
	public void testCopy_whenEmpty() {
		checkEmpty();
		final Set<String> copy = set.copy();
		assertNotSame(copy, set);
		assertEquals(set, copy);
		checkAndClear(0);
	}
	
	public void testCopy_whenNotEmpty() {
		checkEmpty();
		set.put("a");
		set.put("b");
		set.put("c");
		set.put(null);
		final Set<String> copy = set.copy();
		assertNotSame(copy, set);
		assertEquals(set, copy);
		checkAndClear(4);
	}
	
	public void testEquality() {
		assertNotNull(set.equality());
	}

	public void testUnique() {
		assertEquals(set, set.unique());
	}
	
	public void testPermitsDuplicates() {
		assertFalse(set.permitsDuplicates());
	}
	
	public void testToString() {
		checkEmpty();
		put(null);
		put("a");
		put("b");
		assertEquals("{null, a, b}", set.toString());
		checkAndClear(3);
	}
	
}
