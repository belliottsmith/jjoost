package org.jjoost.collections.maps.nested;

import java.util.Map.Entry;

import org.jjoost.collections.MultiMap;
import org.jjoost.collections.ScalarMap;
import org.jjoost.collections.ScalarSet;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.util.Factory;

public class ThreadSafeNestedSetMultiMap<K, V> extends ThreadSafeNestedSetMap<K, V, ScalarSet<V>> implements MultiMap<K, V> {

	private static final long serialVersionUID = -490119082143181821L;

	public ThreadSafeNestedSetMultiMap(ScalarMap<K, ScalarSet<V>> map, Factory<ScalarSet<V>> factory) {
		super(map, factory);
	}

	protected ScalarSet<Entry<K, V>> entrySet ;	
	@Override
	public ScalarSet<Entry<K, V>> entries() {
		if (entrySet == null) {
			entrySet = new EntrySet() ;
		}
		return entrySet ;
	}
	@Override
	public MultiMap<K, V> copy() {
		final ScalarMap<K, ScalarSet<V>> copy = map.copy() ;
		for (Entry<K, ScalarSet<V>> entry : copy.entries())
			entry.setValue(entry.getValue().copy()) ;
		return new ThreadSafeNestedSetMultiMap<K, V>(copy, factory) ;
	}
	
	@Override
	public Iterable<V> apply(K v) {
		return values(v) ;
	}
	
	protected final class EntrySet extends AbstractEntrySet implements ScalarSet<Entry<K, V>> {

		private static final long serialVersionUID = 8122351713234623044L;

		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			final V r = ThreadSafeNestedSetMultiMap.this.put(entry.getKey(), entry.getValue()) ;
			return r == null ? null : new ImmutableMapEntry<K, V>(entry.getKey(), r) ;
		}

		@Override
		public Iterable<Entry<K, V>> unique() {
			return all() ;
		}
		
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final V r = ThreadSafeNestedSetMultiMap.this.put(entry.getKey(), entry.getValue()) ;
			return r == null ? null : new ImmutableMapEntry<K, V>(entry.getKey(), r) ;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return false ;
		}

		@Override
		public Entry<K, V> get(Entry<K, V> key) {
			return first(key) ;
		}
		@Override
		public int size() {
			return totalCount() ;
		}
		public ScalarSet<Entry<K, V>> copy() {
			throw new UnsupportedOperationException() ;
		}
		
	}

}
