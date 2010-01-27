package org.jjoost.collections.maps.base;

import java.util.Iterator ;
import java.util.List ;

import org.jjoost.collections.Set ;
import org.jjoost.collections.sets.base.HashSetTest ;

public abstract class HashMapKeySetTest extends HashSetTest {
	
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
			map.keys().add(null) ;
			assertFalse(true) ;
		} catch (UnsupportedOperationException e) {			
		}
	}
	
	public void testPut() {
		try {
			map.keys().put(null) ;
			assertFalse(true) ;
		} catch (UnsupportedOperationException e) {			
		}
	}
	
	@Override
	protected Boolean apply(String v) {
		return map.keys().apply(v) ;
	}
	@Override
	protected Set<String> getSet() {
		return map.keys() ;
	}
	@Override
	protected int putAll(Iterable<String> vs) {
		return 0 ;
	}
	@Override
	protected int remove(String v, int c) {
		return map.keys().remove(v, c) ;
	}
	@Override
	protected Iterable<String> removeAndReturn(String v, int c) {
		return map.keys().removeAndReturn(v, c) ;
	}
	@Override
	protected String removeAndReturnFirst(String v, int c) {
		return map.keys().removeAndReturnFirst(v, c) ;
	}
	@Override
	protected boolean add(String v) {
		return map.add(v, v);
	}
	@Override
	protected int capacity() {
		return map.capacity() ;
	}
	@Override
	protected int clear() {
		return map.keys().clear() ;
	}
	@Override
	protected Iterator<String> clearAndReturn() {
		return map.keys().clearAndReturn() ;
	}
	@Override
	protected boolean contains(String v) {
		return map.keys().contains(v) ;
	}
	@Override
	protected int count(String v) {
		return map.keys().count(v) ;
	}
	@Override
	protected String first(String v) {
		return map.keys().first(v) ;
	}
	@Override
	protected String get(String v) {
		return map.keys().get(v) ;
	}
	@Override
	protected boolean isEmpty() {
		return map.keys().isEmpty() ;
	}
	@Override
	protected Iterable<String> iterate(String v) {
		return map.keys().all(v) ;
	}
	@Override
	protected Iterator<String> iterator() {
		return map.keys().iterator() ;
	}
	@Override
	protected List<String> list(String v) {
		return map.keys().list(v) ;
	}
	@Override
	protected String put(String v) {
		return map.put(v, v) ;
	}
	@Override
	protected String putIfAbsent(String v) {
		return map.putIfAbsent(v, v) ;
	}
	@Override
	protected int remove(String v) {
		return map.keys().remove(v) ;
	}
	@Override
	protected Iterable<String> removeAndReturn(String v) {
		return map.keys().removeAndReturn(v) ;
	}
	@Override
	protected String removeAndReturnFirst(String v) {
		return map.keys().removeAndReturnFirst(v) ;
	}
	@Override
	protected void resize(int i) {
		map.resize(i) ;
	}
	@Override
	protected void shrink() {
		map.shrink() ;
	}
	@Override
	protected int size() {
		return map.size() ;
	}
	@Override
	protected int totalCount() {
		return map.totalCount() ;
	}
	@Override
	protected int uniqueCount() {
		return map.uniqueKeyCount() ;
	}
	
}
