package org.jjoost.collections.maps.base;

import java.util.Arrays;
import java.util.Iterator;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.sets.base.MultiHashSetTest;
import org.jjoost.util.Iters;

public abstract class HashMapValueSetTest extends MultiHashSetTest {
	
	private final HashMap<String, String, ?> map = createMap() ;
	protected abstract HashMap<String, String, ?> createMap() ;
	
	public void testPutAll_whenNotPresent() {
	}
	
	public void testPutAll_whenPresent() {
	}
	
	@Override
	public void testAdd_whenNotPresent() {
	}

	@Override
	public void testAdd_whenPresent() {
	}

	@Override
	public void testPut_whenNotPresent() {
	}
	
	@Override
	public void testPut_whenPresent() {
	}
	
	@Override
	public void testPutIfAbsent_whenNotPresent() {
	}
	
	@Override
	public void testPutIfAbsent_whenPresent() {
	}
	
	public void testAdd() {
		try {
			map.values().add(null) ;
			assertFalse(true) ;
		} catch (UnsupportedOperationException e) {			
		}
	}
	
	public void testPut() {
		try {
			map.values().put(null) ;
			assertFalse(true) ;
		} catch (UnsupportedOperationException e) {			
		}
	}
	
	public void testPutIfAbsent() {
		try {
			map.values().putIfAbsent(null) ;
			assertFalse(true) ;
		} catch (UnsupportedOperationException e) {			
		}
	}
	
	@Override
	protected MultiSet<String> getSet() {
		return map.values() ;
	}
	@Override
	protected int putAll(Iterable<String> vs) {
		throw new UnsupportedOperationException() ;
	}
	
	int i = 0 ;
	@Override
	protected boolean add(String v) {
		return map.add(Integer.toString(i++), v);
	}
	@Override
	protected int capacity() {
		return map.capacity() ;
	}
	@Override
	protected String put(String v) {
		return map.put(Integer.toString(i++), v) ;
	}
	@Override
	protected String putIfAbsent(String v) {
		throw new UnsupportedOperationException() ;
	}
	@Override
	protected void resize(int i) {
		map.resize(i) ;
	}
	@Override
	protected void shrink() {
		map.shrink() ;
	}

	protected void checkAndClear(int expect) {
		assertEquals(expect, clear()) ;
		i = 0 ;
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
		checkIteratorContents(Arrays.asList(null, "a", "a", "b", "c", "q", "q").iterator(), iterator(), true) ;
		checkAndClear(7) ;
	}
	
	public void testIteratorRemovals() {		
		checkEmpty() ;
		put(null) ;
		put("a") ;
		put("b") ;
		put("c") ;
		put("q") ;
		put("a") ;
		put("q") ;
		checkIteratorContents(Arrays.asList(null, "a", "b", "c", "q", "a", "q").iterator(), Iters.destroyAsConsumed(iterator()), true) ;
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
		final Iterator<String> expect = Arrays.asList(null, "a", "b", "c", "q", "a", "q").iterator() ;
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
	
}
