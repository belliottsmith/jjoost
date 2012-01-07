package org.jjoost.collections.sets.base;

import java.util.Arrays;
import java.util.Iterator;

import org.jjoost.util.Iters;

public abstract class LinkedHashSetTest extends HashSetTest {

	public void testIterator() {
		checkEmpty();
		put(null);
		put("a");
		put("b");
		put("c");
		put("q");
		checkIteratorContents(Arrays.asList(null, "a", "b", "c", "q").iterator(), iterator(), true);
		checkAndClear(5);
	}
	
	// test concurrent modifications
	
	public void testIteratorRemovals() {		
		checkEmpty();
		put(null);
		put("a");
		put("b");
		put("c");
		put("q");
		checkIteratorContents(Arrays.asList(null, "a", "b", "c", "q").iterator(), Iters.destroyAsConsumed(iterator()), true);
		assertFalse(contains(null));
		assertFalse(contains("a"));
		assertFalse(contains("b"));
		assertFalse(contains("c"));
		assertFalse(contains("q"));
		checkAndClear(0);
		put(null);
		put("a");
		put("b");
		put("c");
		put("q");
		final Iterator<String> expect = Arrays.asList(null, "a", "b", "c", "q").iterator();
		final Iterator<String> actual = iterator();
		while (expect.hasNext() && actual.hasNext()) {
			final String e = expect.next();
			final String a = actual.next();
			assertSame(e, a);
			if (a != null && a.equals("a"))
				actual.remove();
		}
		actual.remove();
		assertEquals(expect.hasNext(), actual.hasNext());
		checkAndClear(3);
	}
	
}
