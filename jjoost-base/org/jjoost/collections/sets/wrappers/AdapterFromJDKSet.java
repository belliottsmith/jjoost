package org.jjoost.collections.sets.wrappers;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;

import org.jjoost.collections.Set ;
import org.jjoost.collections.lists.UniformList ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;

public class AdapterFromJDKSet<V> implements Set<V> {
	
	private static final long serialVersionUID = -4114089352987855164L ;
	
	private final java.util.Set<V> set ;
	public AdapterFromJDKSet(java.util.Set<V> map) {
		super() ;
		this.set = map ;
	}
	
	@Override
	public Set<V> copy() {
		throw new UnsupportedOperationException() ;
	}
	@Override
	public V get(V key) {
		return set.contains(key) ? key : null ;
	}
	@Override
	public V put(V val) {
		return set.add(val) ? null : val ;
	}
	@Override
	public int size() {
		return set.size() ;
	}
	@Override
	public int clear() {
		final int size = set.size() ;
		set.clear() ;
		return size ;
	}
	@Override
	public Iterator<V> clearAndReturn() {
		final List<V> r = new ArrayList<V>() ;
		r.addAll(set) ;
		r.clear() ;
		return r.iterator() ;
	}
	@Override
	public int putAll(Iterable<V> vals) {
		int c = 0 ;
		for (V v : vals) 
			if (set.add(v))
				c++ ;
		return c ;
	}
	@Override
	public V putIfAbsent(V val) {
		if (!set.contains(val)) {
			set.add(val) ;
			return null ;			
		}
		return val ;
	}
	@Override
	public int remove(V value) {		
		return set.remove(value) ? 0 : 1 ;
	}
	@Override
	public Iterable<V> removeAndReturn(V value) {
		if (set.remove(value))
			return new UniformList<V>(value, 1) ;
		return Collections.emptyList() ;
	}
	@Override
	public V removeAndReturnFirst(V value) {
		return set.remove(value) ? null : value ;
	}
	@Override
	public void shrink() {
	}
	@Override
	public Iterable<V> all(V value) {
		if (set.contains(value))
			return new UniformList<V>(value, 1) ;
		return Collections.emptyList() ;
	}
	@Override
	public boolean contains(V value) {
		return set.contains(value) ;
	}
	@Override
	public int count(V value) {
		return set.contains(value) ? 1 : 0 ;
	}
	@Override
	public V first(V value) {
		return set.contains(value) ? value : null ;
	}
	@Override
	public boolean isEmpty() {
		return set.isEmpty() ;
	}
	@Override
	public List<V> list(V value) {
		if (set.contains(value))
			return new UniformList<V>(value, 1) ;
		return Collections.emptyList() ;
	}
	@Override
	public boolean permitsDuplicates() {
		return false ;
	}
	@Override
	public int totalCount() {
		return set.size() ;
	}
	@Override
	public Set<V> unique() {
		return this ;
	}
	@Override
	public int uniqueCount() {
		return set.size() ;
	}
	@Override
	public Iterator<V> iterator() {
		return set.iterator() ;
	}
	@Override
	public Boolean apply(V v) {
		return set.contains(v) ;
	}

	@Override
	public int remove(V value, int removeAtMost) {
		if (removeAtMost < 1) {
			if (removeAtMost < 0)
				throw new IllegalArgumentException("Cannot remove less than zero items") ;
			return 0 ;
		}
		return remove(value) ;
	}
	
	@Override
	public Iterable<V> removeAndReturn(V val, int removeAtMost) {
		if (removeAtMost < 1) {
			if (removeAtMost < 0)
				throw new IllegalArgumentException("Cannot remove less than zero items") ;
			return Collections.emptyList() ;
		}
		return removeAndReturn(val) ;
	}
	
	@Override
	public V removeAndReturnFirst(V val, int removeAtMost) {
		if (removeAtMost < 1) {
			if (removeAtMost < 0)
				throw new IllegalArgumentException("Cannot remove less than zero items") ;
			return null ;
		}
		return removeAndReturnFirst(val) ;
	}

	@Override
	public Equality<? super V> equality() {
		return Equalities.object() ;
	}

}
