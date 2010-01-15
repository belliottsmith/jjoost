package org.jjoost.collections.sets.base;

import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;
import org.jjoost.util.Equality;
import org.jjoost.util.Filters;
import org.jjoost.util.Iters;

public abstract class IterableSet<V> implements MultiSet<V> {

	private static final long serialVersionUID = 7475686519443650191L;

	public abstract Equality<? super V> equality() ;
	public abstract Iterator<V> iterator() ;
	
	private UniqueIterableSet unique ;

	@Override
	public Boolean apply(V v) {
		return Iters.contains(equality(), v, iterator()) ;
	}

	@Override
	public int clear() {
		return Iters.count(clearAndReturn()) ;
	}

	@Override
	public Iterator<V> clearAndReturn() {
		return Iters.destroyAsConsumed(iterator()) ;
	}

	@Override
	public MultiSet<V> copy() {
		throw new UnsupportedOperationException() ;
	}
	
	@Override
	public void put(V val, int numberOfTimes) {
		throw new UnsupportedOperationException() ;
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
		throw new UnsupportedOperationException() ;
	}
	
	@Override
	public int remove(V value, int removeAtMost) {
		return Filters.remove(Filters.isEqualTo(value, equality()), removeAtMost, iterator()) ;
	}

	@Override
	public int remove(V value) {
		return remove(value, Integer.MAX_VALUE) ;
	}

	@Override
	public Iterable<V> removeAndReturn(V value, int removeAtMost) {
		return Iters.toList(Filters.removeAndReturn(Filters.isEqualTo(value, equality()), removeAtMost, iterator())) ;
	}

	@Override
	public Iterable<V> removeAndReturn(V value) {
		return removeAndReturn(value, Integer.MAX_VALUE) ;
	}

	@Override
	public V removeAndReturnFirst(V value, int removeAtMost) {
		return Filters.removeAndReturnFirst(Filters.isEqualTo(value, equality()), removeAtMost, iterator()) ;
	}

	@Override
	public V removeAndReturnFirst(V value) {
		return removeAndReturnFirst(value, Integer.MAX_VALUE) ;
	}

	@Override
	public void shrink() {
	}

	@Override
	public Iterable<V> all(final V value) {
		return Filters.apply(Filters.isEqualTo(value, equality()), this) ;
	}

	@Override
	public boolean contains(V value) {
		return Iters.contains(equality(), value, iterator()) ;
	}

	@Override
	public int count(V value) {
		return Iters.count(equality(), value, iterator()) ;
	}

	@Override
	public V first(V value) {
		final Iterator<V> iter = Filters.apply(Filters.isEqualTo(value, equality()), iterator()) ;
		return iter.hasNext() ? iter.next() : null ;
	}

	@Override
	public boolean isEmpty() {
		return !iterator().hasNext() ;
	}

	@Override
	public List<V> list(V value) {
		return Iters.toList(all(value)) ;
	}

	@Override
	public boolean permitsDuplicates() {
		return true ;
	}

	@Override
	public int totalCount() {
		return Iters.count(iterator()) ;
	}

	@Override
	public Set<V> unique() {
		if (unique == null) {
			unique = new UniqueIterableSet() ;
		}
		return unique ;
	}

	@Override
	public int uniqueCount() {
		return Iters.count(unique()) ;
	}

	private final class UniqueIterableSet extends AbstractUniqueSetAdapter<V> implements Set<V> {

		private static final long serialVersionUID = -8170697306505507966L;

		@Override
		protected AnySet<V> set() {
			return IterableSet.this ;
		}

		@Override
		public Iterator<V> iterator() {
			return Filters.apply(Filters.unique(equality()), IterableSet.this.iterator()) ;
		}
		
	}
	
}
