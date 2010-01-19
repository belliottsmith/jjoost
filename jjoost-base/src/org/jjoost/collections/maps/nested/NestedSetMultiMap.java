package org.jjoost.collections.maps.nested;

import java.util.Map.Entry;

import org.jjoost.collections.MultiMap;
import org.jjoost.collections.Map;
import org.jjoost.collections.Set;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;

public class NestedSetMultiMap<K, V> extends NestedSetMap<K, V, Set<V>> implements MultiMap<K, V> {

	private static final long serialVersionUID = -490119082143181821L;

	public NestedSetMultiMap(Map<K, Set<V>> map, Equality<? super V> valueEq,
			Factory<Set<V>> factory) {
		super(map, valueEq, factory);
	}

	protected Set<Entry<K, V>> entrySet ;	
	@Override
	public Set<Entry<K, V>> entries() {
		if (entrySet == null) {
			entrySet = new EntrySet() ;
		}
		return entrySet ;
	}
	@Override
	public MultiMap<K, V> copy() {
		final Map<K, Set<V>> copy = map.copy() ;
		for (Entry<K, Set<V>> entry : copy.entries())
			entry.setValue(entry.getValue().copy()) ;
		return new NestedSetMultiMap<K, V>(copy, valueEq, factory) ;
	}
	
	@Override
	public Iterable<V> apply(K v) {
		return values(v) ;
	}
	
	protected final class EntrySet extends AbstractEntrySet implements Set<Entry<K, V>> {

		private static final long serialVersionUID = 8122351713234623044L;

		@Override
		public boolean add(Entry<K, V> entry) {
			return NestedSetMultiMap.this.add(entry.getKey(), entry.getValue()) ;
		}
		
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {			
			NestedSetMultiMap.this.put(entry.getKey(), entry.getValue()) ;
			return get(entry) ;
		}

		@Override
		public Set<Entry<K, V>> unique() {
			return this ;
		}
		
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final V r = NestedSetMultiMap.this.put(entry.getKey(), entry.getValue()) ;
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
		public Set<Entry<K, V>> copy() {
			throw new UnsupportedOperationException() ;
		}
		
	}

}
