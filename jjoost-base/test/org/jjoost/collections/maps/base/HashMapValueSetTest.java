package org.jjoost.collections.maps.base;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.sets.base.MultiHashSetTest;

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
		return map.add(Integer.toString(++i), v);
	}
	@Override
	protected int capacity() {
		return map.capacity() ;
	}
	@Override
	protected String put(String v) {
		return map.put(Integer.toString(++i), v) ;
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

}
