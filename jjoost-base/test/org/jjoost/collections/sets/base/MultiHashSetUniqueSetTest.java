package org.jjoost.collections.sets.base;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;

public abstract class MultiHashSetUniqueSetTest extends HashSetTest {
	
	protected abstract MultiSet<String> getMultiSet();
	
	protected Set<String> getSet() {
		return getMultiSet().unique();
	}
	
	@Override
	protected boolean add(String v) {
		getMultiSet().add(v);
		return getMultiSet().add(v);
	}

	@Override
	protected String put(String v) {
		getMultiSet().put(v);
		return getMultiSet().put(v);
	}

	@Override
	protected int putAll(Iterable<String> vals) {
		throw new IllegalStateException();
	}

	@Override
	protected String putIfAbsent(String val) {
		final String r = getMultiSet().putIfAbsent(val);
		assertEquals(val, getMultiSet().putIfAbsent(val));
		return r;
	}


	public void testPutAll_whenNotPresent() {
	}
	
	public void testPutAll_whenPresent() {
	}
	
	public void testPut_whenNotPresent() {
	}
	
	public void testPut_whenPresent() {
	}
	
	public void testAdd_whenNotPresent() {
	}
	
	public void testAdd_whenPresent() {
	}

	public void testFirst_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String q1 = "q";
		final String q2 = new String("q");
		put(a1);
		add(a2) ; // add should not alter the state of the set, as an equal value is already present
		assertSame(a1, first(a1));
		put(q1);
		put(q2);
		assertSame(q1, first(q1));
		checkAndClear(2);
	}

	public void testGet_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String q1 = "q";
		final String q2 = new String("q");
		put(a1);
		add(a2) ; // add should not alter the state of the set, as an equal value is already present
		assertSame(a1, get(a1));
		put(q1);
		put(q2);
		assertSame(q1, get(q1));
		checkAndClear(2);
	}

}
