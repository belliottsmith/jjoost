package org.jjoost.collections.sets.concurrent;

import java.util.Iterator ;
import java.util.List ;

import org.jjoost.collections.Set ;
import org.jjoost.collections.sets.base.LinkedHashSetTest ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Rehashers ;

public class LockFreeLinkedHashSetTest extends LinkedHashSetTest {

	private final LockFreeLinkedHashSet<String> set = new LockFreeLinkedHashSet<String>(Rehashers.identity(), Equalities.object()) ;
	
	public Set<String> getSet() {
		return set ;
	}
	
	@Override
	protected String put(String v) {
		return set.put(v) ;
	}	
	
	@Override
	protected boolean add(String v) {
		return set.add(v) ;
	}
	
	protected Iterable<String> iterate(String val) {
		return set.all(val) ;
	}

	protected Boolean apply(String v) {
		return set.apply(v) ;
	}

	protected int capacity() {
		return set.capacity() ;
	}

	protected int clear() {
		return set.clear() ;
	}

	protected Iterator<String> clearAndReturn() {
		return set.clearAndReturn() ;
	}

	protected boolean contains(String value) {
		return set.contains(value) ;
	}

	protected int count(String value) {
		return set.count(value) ;
	}

	protected String first(String value) {
		return set.first(value) ;
	}

	protected String get(String key) {
		return set.get(key) ;
	}

	protected boolean isEmpty() {
		return set.isEmpty() ;
	}

	protected Iterator<String> iterator() {
		return set.iterator() ;
	}

	protected List<String> list(String val) {
		return set.list(val) ;
	}

	protected int putAll(Iterable<String> vals) {
		return set.putAll(vals) ;
	}

	protected String putIfAbsent(String val) {
		return set.putIfAbsent(val) ;
	}

	protected int remove(String val, int atMost) {
		return set.remove(val, atMost) ;
	}

	protected int remove(String val) {
		return set.remove(val) ;
	}

	protected Iterable<String> removeAndReturn(String val, int atMost) {
		return set.removeAndReturn(val, atMost) ;
	}

	protected Iterable<String> removeAndReturn(String val) {
		return set.removeAndReturn(val) ;
	}

	protected String removeAndReturnFirst(String value, int atMost) {
		return set.removeAndReturnFirst(value, atMost) ;
	}

	protected String removeAndReturnFirst(String value) {
		return set.removeAndReturnFirst(value) ;
	}

	protected void resize(int capacity) {
		set.resize(capacity) ;
	}

	protected void shrink() {
		set.shrink() ;
	}

	protected int size() {
		return set.size() ;
	}

	protected int totalCount() {
		return set.totalCount() ;
	}

	protected int uniqueCount() {
		return set.uniqueCount() ;
	}
	
}
