package org.jjoost.collections.maps.base;

import org.jjoost.collections.Set;
import org.jjoost.collections.sets.base.LinkedHashSetTest;

public abstract class LinkedHashMapKeySetTest extends LinkedHashSetTest {
	
	private final HashMap<String, String, ?> map = createMap();
	protected abstract HashMap<String, String, ?> createMap();
	
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
			map.keys().add(null);
			assertFalse(true);
		} catch (UnsupportedOperationException e) {			
		}
	}
	
	public void testPut() {
		try {
			map.keys().put(null);
			assertFalse(true);
		} catch (UnsupportedOperationException e) {			
		}
	}
	
	@Override
	protected Set<String> getSet() {
		return map.keys();
	}
	@Override
	protected int putAll(Iterable<String> vs) {
		return 0;
	}
	@Override
	protected boolean add(String v) {
		return map.add(v, v);
	}
	@Override
	protected int capacity() {
		return map.capacity();
	}
	@Override
	protected String put(String v) {
		return map.put(v, v);
	}
	@Override
	protected String putIfAbsent(String v) {
		return map.putIfAbsent(v, v);
	}
	@Override
	protected void resize(int i) {
		map.resize(i);
	}
	@Override
	protected void shrink() {
		map.shrink();
	}

}
