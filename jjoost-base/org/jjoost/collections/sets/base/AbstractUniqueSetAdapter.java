package org.jjoost.collections.sets.base;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.Set;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.util.Equality;
import org.jjoost.util.Filters;
import org.jjoost.util.Iters;

public abstract class AbstractUniqueSetAdapter<V> implements Set<V> {

	private static final long serialVersionUID = -4614054305733007946L;

	protected abstract AnySet<V> set() ; 
	
	public AbstractUniqueSetAdapter() { }

	private Iterator<V> uniq(Iterator<V> iter) {
		return Filters.apply(Filters.unique(set().equality()), iter) ;
	}
	
	private Iterable<V> uniq(Iterable<V> iter) {
		return Filters.apply(Filters.unique(set().equality()), iter) ;
	}
	
	@Override
	public Set<V> copy() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public V get(V key) {
		return set().first(key) ;
	}

	@Override
	public boolean add(V val) {
		throw new UnsupportedOperationException() ;
	}
	
	@Override
	public V put(V val) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public int putAll(Iterable<V> vals) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public V putIfAbsent(V val) {
		return set().putIfAbsent(val) ;
	}

	@Override
	public int size() {
		return set().uniqueCount() ;
	}

	@Override
	public int clear() {
		return Iters.count(clearAndReturn()) ;
	}

	@Override
	public Iterator<V> clearAndReturn() {
		return uniq(set().clearAndReturn()) ;
	}

	@Override
	public Iterable<V> all(V value) {
		return list(value) ;
	}

	@Override
	public List<V> list(V value) {
		final V find = first(value) ;
		if (find == null) {
			if (value == null) {
				return new UniformList<V>(null, set().contains(null) ? 1 : 1) ;
			} else {
				return Collections.emptyList() ;
			}
		} else {
			return new UniformList<V>(find, 1) ;
		}
	}

	@Override
	public int remove(V value, int removeAtMost) {
		if (removeAtMost < 1) 			
			return Math.min(1, set().remove(value, removeAtMost)) ;
		return Math.min(1, set().remove(value)) ;
	}

	@Override
	public int remove(V value) {
		return Math.min(1, set().remove(value)) ;
	}

	@Override
	public Iterable<V> removeAndReturn(V value, int removeAtMost) {
		if (removeAtMost < 1) 			
			return uniq(set().removeAndReturn(value, removeAtMost)) ;
		return uniq(set().removeAndReturn(value)) ;
	}

	@Override
	public Iterable<V> removeAndReturn(V value) {
		return uniq(set().removeAndReturn(value)) ;
	}

	@Override
	public V removeAndReturnFirst(V value, int removeAtMost) {
		if (removeAtMost < 1) 			
			return set().removeAndReturnFirst(value, removeAtMost) ;
		return set().removeAndReturnFirst(value) ;
	}

	@Override
	public V removeAndReturnFirst(V value) {
		return set().removeAndReturnFirst(value) ;
	}

	@Override
	public void shrink() {
		set().shrink() ;
	}

	@Override
	public boolean contains(V value) {
		return set().contains(value) ;
	}

	@Override
	public int count(V value) {
		return set().contains(value) ? 1 : 0 ;
	}

	@Override
	public Equality<? super V> equality() {
		return set().equality() ;
	}

	@Override
	public V first(V value) {
		return set().first(value) ;
	}

	@Override
	public boolean isEmpty() {
		return set().isEmpty() ;
	}

	@Override
	public boolean permitsDuplicates() {
		return false ;
	}

	@Override
	public int totalCount() {
		return set().uniqueCount() ;
	}

	@Override
	public Set<V> unique() {
		return this ;
	}

	@Override
	public int uniqueCount() {
		return set().uniqueCount() ;
	}

	@Override
	public Boolean apply(V v) {
		return set().apply(v) ;
	}
	
}
