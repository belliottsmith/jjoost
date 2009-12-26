package org.jjoost.collections.maps.nested;

import java.util.Iterator;
import java.util.Map.Entry;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.ListMap;
import org.jjoost.collections.Map;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.collections.sets.base.AbstractUniqueSetAdapter;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Functions;

public class NestedSetListMap<K, V> extends NestedSetMap<K, V, MultiSet<V>> implements ListMap<K, V> {

	private static final long serialVersionUID = -490119082143181821L;

	public NestedSetListMap(Map<K, MultiSet<V>> map,
			Equality<? super V> valueEq, Factory<MultiSet<V>> factory) {
		super(map, valueEq, factory);
	}

	protected MultiSet<Entry<K, V>> entrySet ;	
	@Override
	public MultiSet<Entry<K, V>> entries() {
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
		final Map<K, MultiSet<V>> copy = map.copy() ;
		for (Entry<K, MultiSet<V>> entry : copy.entries())
			entry.setValue(entry.getValue().copy()) ;
		return new NestedSetListMap<K, V>(copy, valueEq, factory) ;
	}
	
	protected final class EntrySet extends AbstractEntrySet implements MultiSet<Entry<K, V>> {

		private static final long serialVersionUID = 8122351713234623044L;
		private UniqueEntrySet unique ;
		
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			NestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
			return null ;
		}

				@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final V r = NestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
			return r == null ? null : new ImmutableMapEntry<K, V>(entry.getKey(), r) ;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return true ;
		}

		@Override
		public MultiSet<Entry<K, V>> copy() {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public void put(Entry<K, V> entry, int numberOfTimes) {
			for (int i = 0 ; i != numberOfTimes ; i++)
				NestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
		}
		
		public Set<Entry<K, V>> unique() {
			if (unique == null)
				unique = new UniqueEntrySet() ;
			return unique ;
		}

		private final class UniqueEntrySet extends AbstractUniqueSetAdapter<Entry<K, V>> {
			private static final long serialVersionUID = 686867617922872433L;
			@Override
			protected AnySet<Entry<K, V>> set() {
				return EntrySet.this ;
			}
			@Override
			public Iterator<Entry<K, V>> iterator() {
				
			}
		}
		
	}
	
}
