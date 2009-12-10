package org.jjoost.collections.maps.nested;

import java.util.Map.Entry;

import org.jjoost.collections.ListMap;
import org.jjoost.collections.ListSet;
import org.jjoost.collections.ScalarMap;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.util.Factory;

public class ThreadSafeNestedSetListMap<K, V> extends ThreadSafeNestedSetMap<K, V, ListSet<V>> implements ListMap<K, V> {

	private static final long serialVersionUID = -490119082143181821L;

	public ThreadSafeNestedSetListMap(ScalarMap<K, ListSet<V>> map, Factory<ListSet<V>> factory) {
		super(map, factory) ;
	}

	protected ListSet<Entry<K, V>> entrySet ;	
	@Override
	public ListSet<Entry<K, V>> entries() {
		if (entrySet == null) {
			entrySet = new EntrySet() ;
		}
		return entrySet ;
	}

	@Override
	public Iterable<V> apply(K v) {
		return values(v) ;
	}

	@Override
	public ListMap<K, V> copy() {
		final ScalarMap<K, ListSet<V>> copy = map.copy() ;
		for (Entry<K, ListSet<V>> entry : copy.entries())
			entry.setValue(entry.getValue().copy()) ;
		return new ThreadSafeNestedSetListMap<K, V>(copy, factory) ;
	}
	
	protected final class EntrySet extends AbstractEntrySet implements ListSet<Entry<K, V>> {

		private static final long serialVersionUID = 8122351713234623044L;

		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			final V r = ThreadSafeNestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
			return r == null ? null : new ImmutableMapEntry<K, V>(entry.getKey(), r) ;
		}

		@Override
		public Iterable<Entry<K, V>> unique() {
			return all() ;
		}

		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final V r = ThreadSafeNestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
			return r == null ? null : new ImmutableMapEntry<K, V>(entry.getKey(), r) ;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return true ;
		}

		@Override
		public ListSet<Entry<K, V>> copy() {
			throw new UnsupportedOperationException() ;
		}

	}

}
