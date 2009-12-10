package org.jjoost.collections.maps;
//package org.jjoost.collections.maps;
//
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.jjoost.collections.Collections;
//import org.jjoost.collections.abstr.AbstractIterable;
//import org.jjoost.collections.abstr.AbstractMutableTreap;
//import org.jjoost.collections.sets.ArbitrarySet;
//import org.jjoost.collections.sets.OrderedReadOnlySet;
//import org.jjoost.util.FilterTotalOrder;
//import org.jjoost.util.Filters;
//import org.jjoost.util.Function;
//import org.jjoost.util.Functions;
//import org.jjoost.util.tuples.Pair;
//
//public abstract class AbstractTreapMap<K, V, N extends AbstractMutableTreap.Node<N> & Map.Entry<K, V>, T extends AbstractTreapMap<K, V, N, T>> extends AbstractMutableTreap<N> implements OrderedMap<K, V> {
//
//	private static final long serialVersionUID = 2449765657374953252L ;
//
//	protected abstract Comparable<N> createComparable(K key) ;
//	protected abstract FilterTotalOrder<N> createFilter(K key, V val) ;
//	protected abstract FilterTotalOrder<N> createFilter(K key) ;
//	protected abstract T makeTree(N head, int size) ;
//	abstract AbstractEntrySet createEntrySet() ;
//	abstract AbstractKeySet createKeySet() ;
//	
//	@SuppressWarnings("unchecked")
//	public static <V extends Comparable<V>> Comparator<V> getComparator() {
//		return (Comparator<V>) SIMPLE_COMPARATOR ;
//	}
//	@SuppressWarnings("unchecked")
//	public static <V extends Comparable<V>> Comparator<V> getComparator(Class<V> clazz) {
//		return (Comparator<V>) SIMPLE_COMPARATOR ;
//	}
//	
//	@SuppressWarnings("unchecked")
//	private static final Comparator SIMPLE_COMPARATOR = new Comparator<Comparable>() {
//		@Override
//		public int compare(Comparable o1, Comparable o2) {
//			return o1.compareTo(o2) ;
//		}
//	} ;
//
//	private final boolean permitsDuplicateKeys ;
//	private AbstractEntrySet entrySet ;
//	private AbstractKeySet keySet ;
//	
//	protected AbstractTreapMap(final boolean permitsDuplicateKeys) {
//		super() ;
//		this.permitsDuplicateKeys = permitsDuplicateKeys ;
//	}
//	protected AbstractTreapMap(final boolean permitsDuplicateKeys, N head, int size) {
//		super(head, size) ;
//		this.permitsDuplicateKeys = permitsDuplicateKeys ;
//	}
//
//	@SuppressWarnings("unchecked")
//	private static final Function VALUE_PROJECTION = new Function<Map.Entry<Object, Object>, Object>() {
//		private static final long serialVersionUID = 6140723772513448903L ;
//		@Override
//		public Object apply(Entry<Object, Object> v) {
//			return v.getValue() ;
//		}
//	} ;
//	
//	@SuppressWarnings("unchecked")
//	private static final Function KEY_PROJECTION = new Function<Map.Entry<Object, Object>, Object>() {
//		private static final long serialVersionUID = 6140723772513448903L ;
//		@Override
//		public Object apply(Entry<Object, Object> v) {
//			return v.getKey() ;
//		}
//	} ;
//	
//	@SuppressWarnings("unchecked")
//	protected static final <K, V, N extends AbstractMutableTreap.Node<N> & Map.Entry<K, V>> Function<N, V> valueProjection() {
//		return (Function<N, V>) VALUE_PROJECTION ;
//	}
//
//	@SuppressWarnings("unchecked")
//	protected static final <K, V, N extends AbstractMutableTreap.Node<N> & Map.Entry<K, V>> Function<N, K> keyProjection() {
//		return (Function<N, K>) KEY_PROJECTION ;
//	}
//	
//	@SuppressWarnings("unchecked")
//	protected static final <K, V, N extends AbstractMutableTreap.Node<N> & Map.Entry<K, V>> Function<N, Map.Entry<K, V>> entryProjection() {
//		return (Function<N, Map.Entry<K, V>>) Functions.<N>identity();
//	}
//	
//	protected static final class FilterWrapper<K, V, N extends Node<N> & Map.Entry<K, V>> implements FilterTotalOrder<N> {
//		private static final long serialVersionUID = 2875326660732397597L ;
//		private final FilterTotalOrder<? super K> wrapped ;
//		FilterWrapper(FilterTotalOrder<? super K> wrapped) {
//			super() ;
//			this.wrapped = wrapped ;
//		}
//		@Override
//		public boolean acceptBetween(N o1, N o2, boolean inclusive) {
//			return wrapped.acceptBetween(o1 == null ? null : o1.getKey(), o2 == null ? null : o2.getKey(), inclusive) ;
//		}
//		@Override
//		public boolean accept(N test) {
//			return wrapped.accept(test.getKey()) ;
//		}
//	}
//	
//	protected static final class UniqueAscKeyFilter<K, V, N extends Node<N> & Map.Entry<K, V>> implements FilterTotalOrder<N> {
//
//		private static final long serialVersionUID = 6559250511146745086L ;
//		private final Comparator<? super K> cmp ;
//		private K min = null ;
//
//		protected UniqueAscKeyFilter(Comparator<? super K> cmp) {
//			super() ;
//			this.cmp = cmp ;
//		}
//
//		@Override
//		public boolean acceptBetween(N o1, N o2, boolean inclusive) {
//			return inclusive 
//			? min == null || o2 == null || cmp.compare(min, o2.getKey()) <= 0
//			: min == null || o2 == null || cmp.compare(min, o2.getKey()) < 0 ;
//		}
//
//		@Override
//		public boolean accept(N test) {
//			final boolean accept = min == null || cmp.compare(min, test.getKey()) < 0 ;
//			if (accept) {
//				min = test.getKey() ;
//			}
//			return accept ;
//		}
//		
//	}
//	
//	protected static final class UniqueDescKeyFilter<K, V, N extends Node<N> & Map.Entry<K, V>> implements FilterTotalOrder<N> {
//		
//		private static final long serialVersionUID = 6559250511146745086L ;
//		private final Comparator<? super K> cmp ;
//		private K max = null ;
//		
//		protected UniqueDescKeyFilter(Comparator<? super K> cmp) {
//			super() ;
//			this.cmp = cmp ;
//		}
//		
//		@Override
//		public boolean acceptBetween(N o1, N o2, boolean inclusive) {
//			return inclusive 
//			? max == null || o1 == null || cmp.compare(o1.getKey(), max) <= 0
//			: max == null || o1 == null || cmp.compare(o1.getKey(), max) < 0 ;
//		}
//		
//		@Override
//		public boolean accept(N test) {
//			final boolean accept = max == null || cmp.compare(test.getKey(), max) < 0 ;
//			if (accept) {
//				max = test.getKey() ;
//			}
//			return accept ;
//		}
//		
//	}
//
//	@Override
//	public void clear() {
//		super.clear() ;
//	}
//
//	@Override
//	public boolean contains(K key, V val) {
//		// we should be passing false instead of duplicateKeys for TreapMultiMaps, as they do NOT have duplicates for key/value pairs. HOWEVER the filter implementation can sensibly ignore this parameter and use the correct strategy.
//		return _contains(createFilter(key, val), permitsDuplicateKeys) ;
//	}
//
//	@Override
//	public boolean contains(K key) {
//		return _contains(createFilter(key), permitsDuplicateKeys) ;
//	}
//
//	@Override
//	public int count(K key, V val) {
//		// we should be passing false instead of duplicateKeys for TreapMultiMaps, as they do NOT have duplicates for key/value pairs. HOWEVER the filter implementation can sensibly ignore this parameter and use the correct strategy.
//		return _count(createFilter(key, val), permitsDuplicateKeys) ;
//	}
//	
//	@Override
//	public int count(K key) {
//		return _count(createFilter(key), permitsDuplicateKeys) ;
//	}
//
//	// simple getters
//	
//	@Override
//	public V first(K key) {
//		final N n = _first(createFilter(key), permitsDuplicateKeys) ;
//		return n == null ? null : n.getValue() ;
//	}
//	
//	@Override
//	public V last(K key) {
//		final N n = _last(createFilter(key), permitsDuplicateKeys) ;
//		return n == null ? null : n.getValue() ;
//	}
//
//	@Override
//	public Entry<K, V> first(FilterTotalOrder<? super Entry<K, V>> filter) {
//		return _first(filter, permitsDuplicateKeys) ;
//	}
//	
//	@Override
//	public Entry<K, V> last(FilterTotalOrder<? super Entry<K, V>> filter) {
//		return _last(filter, permitsDuplicateKeys) ;
//	}
//	
//	@Override
//	public Pair<V, V> boundaries(K find) {
//		return _boundaries(createComparable(find), permitsDuplicateKeys, AbstractTreapMap.<K, V, N>valueProjection()) ;
//	}
//	
//	@Override
//	public Pair<Entry<K, V>, Entry<K, V>> boundaryEntries(K find) {
//		return _boundaries(createComparable(find), permitsDuplicateKeys, AbstractTreapMap.<K, V, N>entryProjection()) ;
//	}
//	
//	@Override
//	public V ceil(K find) {
//		final Entry<K, V> ceil = ceilEntry(find) ;
//		return ceil == null ? null : ceil.getValue() ;
//	}
//	
//	@Override
//	public Entry<K, V> ceilEntry(K find) {
//		return _ceil(createComparable(find), permitsDuplicateKeys) ;
//	}
//	
//	@Override
//	public V floor(K find) {
//		final Entry<K, V> floor = floorEntry(find) ;
//		return floor == null ? null : floor.getValue() ;
//	}
//	
//	@Override
//	public Entry<K, V> floorEntry(K find) {
//		return _floor(createComparable(find), permitsDuplicateKeys) ;
//	}
//	
//	@Override
//	public V greater(K find) {
//		final Entry<K, V> greater = greaterEntry(find) ;
//		return greater == null ? null : greater.getValue() ;
//	}
//	
//	@Override
//	public Entry<K, V> greaterEntry(K find) {
//		return _greater(createComparable(find), permitsDuplicateKeys) ;
//	}
//	
//	@Override
//	public V lesser(K find) {
//		final Entry<K, V> lesser = lesserEntry(find) ;
//		return lesser == null ? null : lesser.getValue() ;
//	}
//	
//	@Override
//	public Entry<K, V> lesserEntry(K find) {
//		return _lesser(createComparable(find), permitsDuplicateKeys) ;
//	}
//	
//	
//	// simple iterable getters
//	
//	
//	@Override
//	public Iterable<V> values() {
//		return values(true) ;
//	}
//
//	@Override
//	public Iterable<V> values(final boolean asc) {
//		return iterable(asc, AbstractTreapMap.<K, V, N>valueProjection()) ;
//	}
//	
//	@Override
//	public Iterable<Entry<K, V>> entries(final K key) {
//		return entries(key, true) ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> entries(final K key, final boolean asc) {
//		return iterable(createFilter(key), asc, AbstractTreapMap.<K, V, N>entryProjection()) ;
//	}
//
//	
//	
//	@Override
//	public Iterable<V> values(final K key) {
//		return values(key, true) ;
//	}
//	@Override
//	public Iterable<V> values(final K key, final boolean asc) {
//		return iterable(createFilter(key), asc, AbstractTreapMap.<K, V, N>valueProjection()) ;
//	}
//	
//	@Override
//	public List<V> list(K key) {
//		return list(key, true) ;
//	}
//	@Override
//	public List<V> list(K key, boolean asc) {
//		return Collections.toList(values(key, asc)) ;
//	}
//
//	
//	
//	@Override
//	public Iterable<V> values(final FilterTotalOrder<? super K> filter, final boolean asc) {
//		return valuesByEntry(Filters.<K, V>toEntryFilter(filter), asc) ;
//	}
//	
//	@Override
//	public Iterable<V> valuesByEntry(final FilterTotalOrder<? super Entry<K, V>> filter, final boolean asc) {
//		return iterable(filter, asc, AbstractTreapMap.<K, V, N>valueProjection()) ;
//	}
//		
//	@Override
//	public T copy(FilterTotalOrder<? super K> filter) {
//		final Subtree<N> result = _copiedsubtree(new FilterWrapper<K, V, N>(filter), permitsDuplicateKeys) ;
//		return makeTree(result.head, result.size) ;
//	}
//	
//	@Override
//	public T copyByEntry(FilterTotalOrder<? super Entry<K, V>> filter) {
//		final Subtree<N> result = _copiedsubtree(filter, permitsDuplicateKeys) ;
//		return makeTree(result.head, result.size) ;
//	}
//
//	
//	
//	@Override
//	public int remove(K key, V val) {
//		return _remove(createFilter(key, val), permitsDuplicateKeys).numberRemoved ;
//	}
//
//	@Override
//	public int remove(K key) {
//		return _remove(createFilter(key), permitsDuplicateKeys).numberRemoved ;
//	}
//
//	@Override
//	public T remove(FilterTotalOrder<? super K> filter) {
//		final Removed<N> result = _remove(new FilterWrapper<K, V, N>(filter), permitsDuplicateKeys) ;
//		return makeTree(result.removed, result.numberRemoved) ;
//	}
//	
//	@Override
//	public T removeByEntry(FilterTotalOrder<? super Entry<K, V>> filter) {
//		final Removed<N> result = _remove(filter, permitsDuplicateKeys) ;
//		return makeTree(result.removed, result.numberRemoved) ;
//	}
//	
//	protected final <F> Iterable<F> iterable(final FilterTotalOrder<? super N> filter, final boolean asc, final Function<N, F> projection) {
//		return new AbstractIterable<F>() {
//			@Override
//			public Iterator<F> iterator() {
//				return _all(filter, asc, projection, permitsDuplicateKeys) ;
//			}
//		} ;
//	}
//	
//	protected final <F> Iterable<F> iterable(final boolean asc, final Function<N, F> projection) {
//		return new AbstractIterable<F>() {
//			@Override
//			public Iterator<F> iterator() {
//				return _all(asc, projection) ;
//			}
//		} ;
//	}
//
//	abstract class AbstractEntrySet implements OrderedMapEntrySet<K, V> {
//		
//		private static final long serialVersionUID = 90635567083464781L ;
//
//		@Override
//		public Iterable<Entry<K, V>> all(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			return AbstractTreapMap.this.iterable(filter, asc, AbstractTreapMap.<K, V, N>entryProjection()) ;
//		}
//
//		@Override
//		public OrderedReadOnlySet<Entry<K, V>> filterCopy(FilterTotalOrder<? super Entry<K, V>> filter) {
//			return AbstractTreapMap.this.copyByEntry(filter).entries() ;
//		}
//
//		@Override
//		public Entry<K, V> first(Entry<K, V> key) {
//			return AbstractTreapMap.this._first(createFilter(key.getKey(), key.getValue()), permitsDuplicateKeys) ;
//		}
//
//		@Override
//		public Entry<K, V> last(Entry<K, V> key) {
//			return AbstractTreapMap.this._last(createFilter(key.getKey(), key.getValue()), permitsDuplicateKeys) ;
//		}
//
//		@Override
//		public Entry<K, V> first(FilterTotalOrder<? super Entry<K, V>> filter) {
//			return AbstractTreapMap.this._first(filter, permitsDuplicateKeys) ;
//		}
//
//		@Override
//		public Entry<K, V> last(FilterTotalOrder<? super Entry<K, V>> filter) {
//			return AbstractTreapMap.this._last(filter, permitsDuplicateKeys) ;
//		}
//
//		@Override
//		public List<Entry<K, V>> list(Entry<K, V> value, boolean asc) {
//			return Collections.toList(all(value, asc)) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> all(Entry<K, V> value, boolean asc) {
//			return AbstractTreapMap.this.iterable(createFilter(value.getKey(), value.getValue()), asc, AbstractTreapMap.<K, V, N>entryProjection()) ;
//		}
//
//		@Override
//		public List<Entry<K, V>> list(Entry<K, V> value) {
//			return list(value, true) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> all(Entry<K, V> value) {
//			return all(value, true) ;
//		}
//
//		@Override
//		public void clear() {
//			AbstractTreapMap.this.clear() ;
//		}
//
//		@Override
//		public int remove(Entry<K, V> value) {
//			return AbstractTreapMap.this.remove(value.getKey(), value.getValue()) ;
//		}
//
//		@Override
//		public boolean contains(Entry<K, V> value) {
//			return AbstractTreapMap.this.contains(value.getKey(), value.getValue()) ;
//		}
//
//		@Override
//		public int count(FilterTotalOrder<? super Entry<K, V>> filter) {
//			return AbstractTreapMap.this._count(filter, permitsDuplicateKeys) ;
//		}
//
//		@Override
//		public int count(Entry<K, V> find) {
//			return AbstractTreapMap.this.count(find.getKey(), find.getValue()) ;
//		}
//
//		@Override
//		public int size() {
//			return AbstractTreapMap.this.size() ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> all() {
//			return all(true) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> all(final boolean asc) {
//			return new AbstractIterable<Entry<K,V>>() {
//				@Override
//				public Iterator<Entry<K, V>> iterator() {
//					return AbstractTreapMap.this._all(asc, AbstractTreapMap.<K, V, N>entryProjection()) ;
//				}
//			} ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> allByKey(FilterTotalOrder<? super K> filter, boolean asc) {
//			return AbstractTreapMap.this.iterable(new FilterWrapper<K, V, N>(filter), asc, AbstractTreapMap.<K, V, N>entryProjection()) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> unique() {
//			return unique(true) ;
//		}
//
//		@Override
//		public Iterator<Entry<K, V>> iterator() {
//			return AbstractTreapMap.this._all(true, AbstractTreapMap.<K, V, N>entryProjection()) ;
//		}
//
//		@Override
//		public boolean permitsDuplicates() {
//			return permitsDuplicateKeys ;
//		}
//
//		@Override
//		public OrderedMapEntrySet<K, V> remove(FilterTotalOrder<? super Entry<K, V>> filter) {
//			return AbstractTreapMap.this.removeByEntry(filter).entries() ;
//		}
//
//		@Override
//		public Boolean apply(Entry<K, V> v) {
//			return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
//		}
//
//		@Override
//		public ArbitrarySet<Entry<K, V>> copy() {
//			throw new UnsupportedOperationException() ;
//		}
//
//	}
//	
//	abstract class AbstractKeySet implements OrderedMapKeySet<K, V> {
//
//		private static final long serialVersionUID = -6071602455394719886L ;
//
//		@Override
//		public Pair<K, K> boundaries(K find) {
//			return _boundaries(createComparable(find), permitsDuplicateKeys, AbstractTreapMap.<K, V, N>keyProjection()) ;
//		}
//
//		@Override
//		public K ceil(K find) {
//			final Entry<K, V> ceil = AbstractTreapMap.this.ceilEntry(find) ;
//			return ceil == null ? null : ceil.getKey() ;
//		}
//
//		@Override
//		public K floor(K find) {
//			final Entry<K, V> floor = AbstractTreapMap.this.floorEntry(find) ;
//			return floor == null ? null : floor.getKey() ;
//		}
//
//		@Override
//		public K lesser(K find) {
//			final Entry<K, V> lesser = AbstractTreapMap.this.lesserEntry(find) ;
//			return lesser == null ? null : lesser.getKey() ;
//		}
//		
//		@Override
//		public K greater(K find) {
//			final Entry<K, V> greater = AbstractTreapMap.this.greaterEntry(find) ;
//			return greater == null ? null : greater.getKey() ;
//		}
//		
//		@Override
//		public Iterable<K> all(FilterTotalOrder<? super K> filter, boolean asc) {
//			return iterable(new FilterWrapper<K, V, N>(filter), asc, AbstractTreapMap.<K, V, N>keyProjection()) ;
//		}
//
//		@Override
//		public OrderedReadOnlySet<K> filterCopy(FilterTotalOrder<? super K> filter) {
//			return AbstractTreapMap.this.copy(filter).keys() ;
//		}
//
//		@Override
//		public K first(K key) {
//			final N n = AbstractTreapMap.this._first(createFilter(key), permitsDuplicateKeys) ;
//			return n == null ? null : n.getKey() ;
//		}
//
//		@Override
//		public K last(K key) {
//			final N n = AbstractTreapMap.this._last(createFilter(key), permitsDuplicateKeys) ;
//			return n == null ? null : n.getKey() ;
//		}
//
//		@Override
//		public K first(FilterTotalOrder<? super K> filter) {
//			final N n = AbstractTreapMap.this._first(new FilterWrapper<K, V, N>(filter), permitsDuplicateKeys) ;
//			return n == null ? null : n.getKey() ;
//		}
//
//		@Override
//		public K last(FilterTotalOrder<? super K> filter) {
//			final N n = AbstractTreapMap.this._last(new FilterWrapper<K, V, N>(filter), permitsDuplicateKeys) ;
//			return n == null ? null : n.getKey() ;
//		}
//
//		@Override
//		public List<K> list(K value, boolean asc) {
//			return Collections.toList(all(value, asc)) ;
//		}
//
//		@Override
//		public Iterable<K> all(K key, boolean asc) {
//			return AbstractTreapMap.this.iterable(createFilter(key), asc, AbstractTreapMap.<K, V, N>keyProjection()) ;
//		}
//
//		@Override
//		public List<K> list(K value) {
//			return list(value, true) ;
//		}
//
//		@Override
//		public Iterable<K> all(K value) {
//			return all(value, true) ;
//		}
//
//		@Override
//		public void clear() {
//			AbstractTreapMap.this.clear() ;
//		}
//
//		@Override
//		public int remove(K value) {
//			return AbstractTreapMap.this.remove(value) ;
//		}
//
//		@Override
//		public boolean contains(K value) {
//			return AbstractTreapMap.this.contains(value) ;
//		}
//
//		@Override
//		public int count(FilterTotalOrder<? super K> filter) {
//			return AbstractTreapMap.this._count(new FilterWrapper<K, V, N>(filter), permitsDuplicateKeys) ;
//		}
//
//		@Override
//		public int count(K find) {
//			return AbstractTreapMap.this._count(createComparable(find), permitsDuplicateKeys) ;
//		}
//
//		@Override
//		public int size() {
//			return AbstractTreapMap.this.size() ;
//		}
//
//		@Override
//		public Iterable<K> all() {
//			return all(true) ;
//		}
//
//		@Override
//		public Iterable<K> all(final boolean asc) {
//			return new AbstractIterable<K>() {
//				@Override
//				public Iterator<K> iterator() {
//					return AbstractTreapMap.this._all(asc, AbstractTreapMap.<K, V, N>keyProjection()) ;
//				}
//			} ;
//		}
//
//		@Override
//		public Iterable<K> allByEntry(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			return AbstractTreapMap.this.iterable(filter, asc, AbstractTreapMap.<K, V, N>keyProjection()) ;
//		}
//
//		@Override
//		public Iterable<K> unique() {
//			return unique(true) ;
//		}
//
//		@Override
//		public Iterator<K> iterator() {
//			return AbstractTreapMap.this._all(true, AbstractTreapMap.<K, V, N>keyProjection()) ;
//		}
//
//		@Override
//		public boolean permitsDuplicates() {
//			return permitsDuplicateKeys ;
//		}
//
//		@Override
//		public OrderedMapKeySet<K, V> remove(FilterTotalOrder<? super K> filter) {
//			return AbstractTreapMap.this.remove(filter).keys() ;
//		}
//	
//		@Override
//		public Boolean apply(K v) {
//			return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
//		}
//		
//		@Override
//		public ArbitrarySet<K> copy() {
//			throw new UnsupportedOperationException() ;
//		}
//
//	}
//	
//	@Override
//	public boolean permitsDuplicateKeys() {
//		return permitsDuplicateKeys ;
//	}
//
//	@Override
//	public OrderedMapEntrySet<K, V> entries() {
//		if (entrySet == null)
//			entrySet = createEntrySet() ;
//		return entrySet ;
//	}
//	@Override
//	public OrderedMapKeySet<K, V> keys() {
//		if (keySet == null)
//			keySet = createKeySet() ;
//		return keySet ;
//	}
//
//	public T copy() {
//		return makeTree(_copy(), size()) ;
//	}
//	
//}
