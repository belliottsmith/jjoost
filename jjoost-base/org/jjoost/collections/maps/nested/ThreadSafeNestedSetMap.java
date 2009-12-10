package org.jjoost.collections.maps.nested;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.jjoost.collections.ArbitraryMap;
import org.jjoost.collections.ArbitrarySet;
import org.jjoost.collections.ListSet ;
import org.jjoost.collections.ScalarMap;
import org.jjoost.collections.iters.AbstractIterable ;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters ;

public abstract class ThreadSafeNestedSetMap<K, V, S extends ArbitrarySet<V>> implements ArbitraryMap<K, V> {

	private static final long serialVersionUID = -6962291049889502542L;
	
	protected final ScalarMap<K, S> map ;
	protected final Factory<S> factory ;
	private volatile int totalCount ;
	
	@SuppressWarnings("unchecked")
	private static final AtomicIntegerFieldUpdater<ThreadSafeNestedSetMap> totalCountUpdater = AtomicIntegerFieldUpdater.newUpdater(ThreadSafeNestedSetMap.class, "totalCount") ;	
	
	protected ListSet<K> keySet ;	
	@Override
	public ListSet<K> keys() {
		if (keySet == null) {
			keySet = new KeySet() ;
		}
		return keySet  ;
	}

	public ThreadSafeNestedSetMap(ScalarMap<K, S> map, Factory<S> factory) {
		super();
		this.map = map ;
		this.factory = factory ;
	}

	@Override
	public boolean contains(K key, V value) {
		final S set = map.get(key) ;
		return set != null && set.contains(value) ;
	}
	@Override
	public boolean contains(K key) {
		return map.contains(key) ;
	}
	@Override
	public int count(K key, V value) {
		final S set = map.get(key) ;
		return set == null ? 0 : set.count(value) ;
	}
	@Override
	public int count(K key) {
		final S set = map.get(key) ;
		return set == null ? 0 : set.totalCount() ;
	}
	@Override
	public Iterable<Entry<K, V>> entries(K key) {
		final S set = map.get(key) ;
		if (set == null) 
			return java.util.Collections.emptyList() ;
		return Functions.apply(new EntryMaker<K, V>(key), set.all()) ;
	}
	@Override
	public V first(K key) {
		final S set = map.get(key) ;
		return set == null ? null : set.isEmpty() ? null : set.all().iterator().next() ;
	}
	@Override
	public List<V> list(K key) {
		final S set = map.get(key) ;
		return set == null ? null : Iters.toList(set) ;
	}
	@Override
	public int totalCount() {
		return totalCount ;
	}
	@Override
	public int uniqueKeyCount() {
		return map.totalCount() ;
	}
	@Override
	public Iterable<V> values() {
		return Iters.concat(Functions.apply(Functions.<S, Entry<K, S>>getMapEntryValueProjection(), map.entries())) ;
	}
	@Override
	public Iterable<V> values(K key) {
		final S set = map.get(key) ;
		return set == null ? null : set ;
	}
	@Override
	public V put(K key, V val) {
		final V r = map.ensureAndGet(key, factory).put(val) ;
		if (r == null)
			totalCountUpdater.incrementAndGet(this) ;
		return r ;
	}
	@Override
	public V putIfAbsent(K key, V val) {
		final V r = map.ensureAndGet(key, factory).putIfAbsent(val) ;
		if (r == null)
			totalCountUpdater.incrementAndGet(this) ;
		return r ;
	}
	
	@Override
	public boolean permitsDuplicateKeys() {
		return true ;
	}

	@Override
	public int clear() {
		int c = 0 ;
		Iterator<Entry<K, S>> iter = map.entries().iterator() ;
		while (iter.hasNext()) {
			c += iter.next().getValue().clear() ;
		}
		return c ;
	}
	
	@Override
	public Iterator<Entry<K, V>> clearAndReturn() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public ArbitraryMap<V, K> inverse() {
		throw new UnsupportedOperationException() ;
	}
	@Override
	public int remove(K key, V value) {
		final S set = map.get(key) ;
		if (set == null)
			return 0 ;
		final int r = set.remove(value) ;
		if (r != 0)
			totalCountUpdater.addAndGet(this, -r) ;
		return r ;
	}
	@Override
	public int remove(K key) {
		final Iterator<Entry<K, S>> removed = map.removeAndReturn(key).iterator() ;
		final int r = removed.hasNext() ? removed.next().getValue().totalCount() : 0 ;
		if (r != 0)
			totalCountUpdater.addAndGet(this, -r) ;
		return r ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V value) {
		final S set = map.get(key) ;
		if (set == null)
			return java.util.Collections.emptyList() ;
		final Iterable<V> r = set.removeAndReturn(value) ;
		{	final Iterator<?> iter = r.iterator() ;
			if (iter.hasNext())
				totalCountUpdater.addAndGet(this, -Iters.count(iter)) ;
		}
		return Functions.apply(new EntryMaker<K, V>(key), r) ;
	}
	
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		final Iterator<Entry<K, S>> removed = map.entries(key).iterator() ;
		if (!removed.hasNext())
			return java.util.Collections.emptyList() ;
		List<Entry<K, List<V>>> asList = new ArrayList<Entry<K, List<V>>>() ;
		int c = 0 ;
		while (removed.hasNext()) {
			Entry<K, S> rem = removed.next() ;
			List<V> vs = Iters.toList(rem.getValue().clearAndReturn()) ;
			asList.add(new ImmutableMapEntry<K, List<V>>(rem.getKey(), vs)) ;
			c += vs.size() ;
		}
		if (c != 0)
			totalCountUpdater.addAndGet(this, -c) ;
		return Iters.concat(Functions.apply(ThreadSafeNestedSetMap.<K, V>entryFlattener(), (Iterable<Entry<K, List<V>>>) asList)) ;
	}

	@Override
	public V removeAndReturnFirst(K key) {
		final Iterator<Entry<K, S>> removed = map.removeAndReturn(key).iterator() ;
		if (!removed.hasNext())
			return null ;
		final S set = removed.next().getValue() ;
		final Iterator<V> vals = set.iterator() ;
		if (vals.hasNext()) {
			final V r = vals.next() ;
			final int deleted = set.clear() ;
			totalCountUpdater.addAndGet(this, -deleted) ;
			return r ;
		}
		return null ;
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty() ;
	}
	
	@Override
	public void shrink() {
		map.shrink() ;
	}

	class KeySet extends AbstractIterable<K> implements ListSet<K> {
		
		private static final long serialVersionUID = 1461826147890179114L ;

		@Override
		public boolean contains(K value) {
			return ThreadSafeNestedSetMap.this.contains(value) ;
		}

		@Override
		public int count(K value) {
			return ThreadSafeNestedSetMap.this.count(value) ;			
		}

		@Override
		public int totalCount() {
			return ThreadSafeNestedSetMap.this.totalCount() ;			
		}

		@Override
		public int clear() {
			return ThreadSafeNestedSetMap.this.clear() ;			
		}

		@Override
		public void shrink() {
			ThreadSafeNestedSetMap.this.shrink() ;			
		}
		
		@Override
		public int remove(K key) {
			return ThreadSafeNestedSetMap.this.remove(key) ;			
		}

		@Override
		public Boolean apply(K v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
		}

		@Override
		public ListSet<K> copy() {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public Iterable<K> all() {
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return KeySet.this.iterator() ;
				}
			} ;
		}
		
		@Override
		public Iterable<K> all(final K key) {
			final S set = map.get(key) ;
			return new UniformList<K>(key, set == null ? 0 : set.totalCount()) ;
		}
		
		@Override
		public K first(final K key) {
			return map.keys().first(key) ;
		}
		
		@Override
		public List<K> list(final K key) {
			final S set = map.get(key) ;
			return new UniformList<K>(key, set == null ? 0 : set.totalCount()) ;
		}
		
		@Override
		public Iterator<K> iterator() {
			return Iters.concat(Functions.apply(ThreadSafeNestedSetMap.<K, V>keyRepeater(), map.entries())).iterator() ;
		}

		@Override
		public boolean isEmpty() {
			return ThreadSafeNestedSetMap.this.isEmpty() ;
		}

		@Override
		public int uniqueCount() {
			return ThreadSafeNestedSetMap.this.uniqueKeyCount() ;
		}

		@Override
		public final K put(K val) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public Iterable<K> removeAndReturn(K key) {
			return Functions.apply(Functions.<K, Entry<K, V>>getMapEntryKeyProjection(), ThreadSafeNestedSetMap.this.removeAndReturn(key)) ;
		}

		@Override
		public K removeAndReturnFirst(K key) {
			final Iterator<? extends Entry<K, S>> removed = map.removeAndReturn(key).iterator() ;
			return removed.hasNext() ? removed.next().getKey() : null ;
		}

		@Override
		public Iterable<K> unique() {
			return map.keys().unique() ;
		}

		@Override
		public K putIfAbsent(K val) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public int putAll(Iterable<K> val) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public Iterator<K> clearAndReturn() {
			return Functions.apply(Functions.<K, Entry<K, V>>getMapEntryKeyProjection(), ThreadSafeNestedSetMap.this.clearAndReturn()) ;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return true ;
		}

	}
	
	abstract class AbstractEntrySet extends AbstractIterable<Entry<K, V>> implements ArbitrarySet<Entry<K, V>> {
		
		private static final long serialVersionUID = 4037233101289518536L ;

		@Override
		public boolean contains(Entry<K, V> value) {
			return ThreadSafeNestedSetMap.this.contains(value.getKey(), value.getValue()) ;
		}

		@Override
		public int count(Entry<K, V> value) {
			return ThreadSafeNestedSetMap.this.count(value.getKey(), value.getValue()) ;
		}

		@Override
		public int totalCount() {
			return ThreadSafeNestedSetMap.this.totalCount() ;			
		}

		@Override
		public int clear() {
			return ThreadSafeNestedSetMap.this.clear() ;			
		}

		@Override
		public Iterator<Entry<K, V>> clearAndReturn() {
			return ThreadSafeNestedSetMap.this.clearAndReturn() ;
		}
		
		@Override
		public void shrink() {
			ThreadSafeNestedSetMap.this.shrink() ;			
		}
		
		@Override
		public int remove(Map.Entry<K, V> entry) {
			return ThreadSafeNestedSetMap.this.remove(entry.getKey(), entry.getValue()) ;			
		}

		@Override
		public Boolean apply(Entry<K, V> v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
		}
		
		@Override
		public Iterable<Entry<K, V>> all() {
			return new AbstractIterable<Entry<K,V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return AbstractEntrySet.this.iterator() ;
				}
			} ;
		}

		@Override
		public Iterable<Entry<K, V>> all(Entry<K, V> entry) {
			final K key = entry.getKey() ;
			final V value = entry.getValue() ;
			final S set = map.get(key) ;
			if (set == null) 
				return java.util.Collections.emptyList() ;
			return Functions.apply(new EntryMaker<K, V>(key), set.all(value)) ;
		}

		@Override
		public Entry<K, V> first(Entry<K, V> entry) {
			final K key = entry.getKey() ;
			final V value = entry.getValue() ;
			final S set = map.get(key) ;
			if (set == null)
				return null ;
			final V first = set.first(value) ;
			return first == null ? null : new ImmutableMapEntry<K, V>(key, first) ; 
		}

		@Override
		public List<Entry<K, V>> list(Entry<K, V> entry) {
			return Iters.toList(all(entry)) ;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return ThreadSafeNestedSetMap.this.entries().iterator() ;
		}
		
		@Override
		public boolean isEmpty() {
			return ThreadSafeNestedSetMap.this.isEmpty() ;
		}
		
		@Override
		public int uniqueCount() {
			return ThreadSafeNestedSetMap.this.totalCount() ;
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry) {
			return ThreadSafeNestedSetMap.this.removeAndReturn(entry.getKey(), entry.getValue()) ;
		}

		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry) {
			final Iterator<? extends Entry<K, V>> removed = ThreadSafeNestedSetMap.this.removeAndReturn(entry.getKey(), entry.getValue()).iterator() ;
			return removed.hasNext() ? removed.next() : null ;
		}

		@Override
		public int putAll(Iterable<Entry<K, V>> vals) {
			int c = 0 ;
			for (Entry<K, V> val : vals)
				if (put(val) == null)
					c++ ;
			return c ;
		}

	}

	private static final class EntryMaker<K, V> implements Function<V, Entry<K, V>> {
		private static final long serialVersionUID = -965724235732791909L;
		private final K key ;
		public EntryMaker(K key) {
			this.key = key;
		}
		@Override
		public Entry<K, V> apply(V value) {
			return new ImmutableMapEntry<K, V>(key, value) ;
		}
	}

	@SuppressWarnings("unchecked")
	private static final KeyRepeater KEY_REPEATER = new KeyRepeater() ;
	@SuppressWarnings("unchecked")
	private static final <K, V> KeyRepeater<K, V> keyRepeater() {
		return KEY_REPEATER ;
	}
	private static final class KeyRepeater<K, V> implements Function<Entry<K, ? extends ArbitrarySet<V>>, Iterable<K>> {
		private static final long serialVersionUID = -965724235732791909L;
		@Override
		public Iterable<K> apply(Entry<K, ? extends ArbitrarySet<V>> entry) {
			return new UniformList<K>(entry.getKey(), entry.getValue().totalCount()) ;
		}
	}

	@SuppressWarnings("unchecked")
	private static final EntryFlattener ENTRY_FLATTENER = new EntryFlattener () ;
	@SuppressWarnings("unchecked")
	private static final <K, V> EntryFlattener <K, V> entryFlattener() {
		return ENTRY_FLATTENER ;
	}
	private static final class EntryFlattener <K, V> implements Function<Entry<K, ? extends Iterable<V>>, Iterable<Entry<K, V>>> {
		private static final long serialVersionUID = -965724235732791909L;
		@Override
		public Iterable<Entry<K, V>> apply(Entry<K, ? extends Iterable<V>> entry) {
			return Functions.apply(new EntryMaker<K, V>(entry.getKey()), entry.getValue()) ;
		}
	}

}
