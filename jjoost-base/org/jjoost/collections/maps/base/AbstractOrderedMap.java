package org.jjoost.collections.maps.base;

import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map.Entry ;

import org.jjoost.collections.ArbitraryMap ;
import org.jjoost.collections.OrderedMap ;
import org.jjoost.collections.OrderedSet ;
import org.jjoost.collections.base.OrderedStore ;
import org.jjoost.collections.iters.AbstractIterable ;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Filters ;
import org.jjoost.util.Function ;
import org.jjoost.util.Functions ;
import org.jjoost.util.Iters ;
import org.jjoost.util.tuples.Pair ;

public abstract class AbstractOrderedMap<K, V, N extends Entry<K, V>, S extends OrderedStore<N, S>, M extends OrderedMap<K, V>> implements OrderedMap<K, V> {

	private static final long serialVersionUID = 8356596119889029488L ;

	private final S store ;
	private final boolean keyCmpIsTotalOrder ;
	private final Comparator<? super K> keyCmp ;
	private final boolean entryCmpIsTotalOrder ;
	private final Comparator<Entry<K, V>> entryCmp ;
	private final Comparator<Entry<K, V>> entryInsertCmp ;
	private final OrderedMapNodeFactory<K, V, N> nodeFactory ;
	
	public AbstractOrderedMap(S store, boolean keyCmpIsTotalOrder, Comparator<? super K> keyCmp, boolean entryCmpIsTotalOrder,
			Comparator<Entry<K, V>> entryCmp, Comparator<Entry<K, V>> entryInsertCmp, OrderedMapNodeFactory<K, V, N> nodeFactory) {
		super() ;
		this.store = store ;
		this.keyCmpIsTotalOrder = keyCmpIsTotalOrder ;
		this.keyCmp = keyCmp ;
		this.entryCmpIsTotalOrder = entryCmpIsTotalOrder ;
		this.entryCmp = entryCmp ;
		this.entryInsertCmp = entryInsertCmp ;
		this.nodeFactory = nodeFactory ;
	}

	protected abstract M create(S store, Comparator<? super K> keyComparator, Comparator<Entry<K, V>> entryComparator, OrderedMapNodeFactory<K, V, N> nodeFactory) ;
	
	protected M create(S store) {
		return create(store, keyCmp, entryCmp, nodeFactory) ;
	}
	
	protected final Function<Entry<K, V>, K> keyProj() {
		return Functions.<K, Entry<K, V>>getMapEntryKeyProjection() ;
	}
	
	protected final Function<Entry<K, V>, V> valProj() {
		return Functions.<V, Entry<K, V>>getMapEntryValueProjection() ;
	}
	
	protected final Function<Entry<K, V>, Entry<K, V>> entryProj() {
		return Functions.<Entry<K, V>>identity() ;
	}
	
	@Override
	public Pair<V, V> boundaries(K find) {
		return store.boundaries(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, Filters.<Entry<K, V>>acceptAll(), valProj()) ;
	}
	@Override
	public Pair<Entry<K, V>, Entry<K, V>> boundaryEntries(K find) {
		return store.boundaries(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, Filters.<Entry<K, V>>acceptAll(), entryProj()) ;
	}
	@Override
	public V ceil(K find) {
		return store.ceil(find, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj()) ;
	}
	@Override
	public Entry<K, V> ceilEntry(K find) {
		return store.ceil(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public int remove(FilterPartialOrder<K> filter) {
		return store.remove(filter, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
	}

	@Override
	public M removeAndReturn(FilterPartialOrder<K> filter) {
		return create(store.removeAndReturn(filter, keyProj(), keyCmp, keyCmpIsTotalOrder)) ;
	}

	@Override
	public M removeByEntryAndReturn(FilterPartialOrder<Entry<K, V>> filter) {
		return create(store.removeAndReturn(filter, entryProj(), entryCmp, entryCmpIsTotalOrder)) ;
	}

	@Override
	public Iterable<Entry<K, V>> entries(final K key, final boolean asc) {
		return new AbstractIterable<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return store.<K, Entry<K, V>>all(asc, key, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;			
			}			
		} ;
	}

	@Override
	public Entry<K, V> first(FilterPartialOrder<Entry<K, V>> filter) {
		return store.first(filter, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public Iterable<Entry<K, V>> firstOfEachKey() {
		if (keyCmpIsTotalOrder) 
			return entries() ;
		return new AbstractIterable<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return store.<K, Entry<K, V>>all(true, Filters.<K>uniqueAsc(), keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;
			}			
		} ;
	}

	@Override
	public Iterable<Entry<K, V>> lastOfEachKey() {
		if (keyCmpIsTotalOrder) 
			return entries().all(false) ;
		return new AbstractIterable<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return store.<K, Entry<K, V>>all(false, Filters.<K>uniqueDesc(), 
						keyProj(), keyCmp, keyCmpIsTotalOrder, 
						entryProj()
				) ;
			}			
		} ;
	}

	@Override
	public V floor(K find) {
		return store.<K, V>floor(find, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj()) ;
	}

	@Override
	public Entry<K, V> floorEntry(K find) {
		return store.<K, Entry<K, V>>floor(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public V greater(K find) {
		return store.<K, V>greater(find, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj()) ;
	}

	@Override
	public Entry<K, V> greaterEntry(K find) {
		return store.<K, Entry<K, V>>greater(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public Entry<K, V> last(FilterPartialOrder<Entry<K, V>> filter) {
		return store.last(filter, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public V last(K key) {
		return store.<K, V>last(key, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj()) ;
	}

	@Override
	public V lesser(K find) {
		return store.<K, V>lesser(find, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj()) ;
	}

	@Override
	public Entry<K, V> lesserEntry(K find) {
		return store.<K, Entry<K, V>>lesser(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public Iterable<V> values(final boolean asc) {
		return new AbstractIterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return store.iterator(asc, valProj());
			}
		} ;
	}

	@Override
	public Iterable<V> values(final K key, final boolean asc) {
		return new AbstractIterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return store.all(asc, key, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj());
			}
		} ;
	}

	@Override
	public int clear() {
		return store.clear() ;
	}

	@Override
	public Iterator<Entry<K, V>> clearAndReturn() {
		return store.clearAndReturn(entryProj()) ;
	}

	@Override
	public V put(K key, V val) {
		return store.put(nodeFactory.make(key, val), valProj()) ;
	}

	@Override
	public V putIfAbsent(K key, V val) {
		return store.putIfAbsent(nodeFactory.make(key, val), valProj()) ;
	}

	@Override
	public int remove(K key, V val) {
		return store.<Entry<K, V>>remove(new ImmutableMapEntry<K, V>(key, val), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
	}

	@Override
	public int remove(K key) {
		return store.<K>remove(key, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
	}

	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		return store.<Entry<K, V>, Entry<K, V>>removeAndReturn(new ImmutableMapEntry<K, V>(key, val), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		return store.<K, Entry<K, V>>removeAndReturn(key, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;
	}

	@Override
	public V removeAndReturnFirst(K key) {
		return store.<K, V>removeAndReturnFirst(key, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj()) ;
	}

	@Override
	public void shrink() {
	}

	@Override
	public boolean contains(K key, V val) {
		return store.<Entry<K, V>>contains(new ImmutableMapEntry<K, V>(key, val), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
	}

	@Override
	public boolean contains(K key) {
		return store.<K>contains(key, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
	}

	@Override
	public int count(K key, V val) {
		return store.<Entry<K, V>>count(new ImmutableMapEntry<K, V>(key, val), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
	}

	@Override
	public int count(K key) {
		return store.<K>count(key, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
	}

	@Override
	public Iterable<Entry<K, V>> entries(K key) {
		return entries(key, true) ;
	}

	@Override
	public V first(K key) {
		return store.<K, V>first(key, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj()) ;
	}

	@Override
	public boolean isEmpty() {
		return store.isEmpty() ;
	}

	@Override
	public List<V> list(K key) {
		return Iters.toList(values(key)) ;
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return !keyCmpIsTotalOrder ;
	}

	@Override
	public int totalCount() {
		return store.count() ;
	}

	@Override
	public int uniqueKeyCount() {
		if (keyCmpIsTotalOrder)
			return store.count() ;
		return store.count(Filters.<K>uniqueAsc(), keyProj(), keyCmp, false) ;
	}

	@Override
	public Iterable<V> values() {
		return values(true) ;
	}

	@Override
	public Iterable<V> values(K key) {
		return values(key, true) ;
	}

	@Override
	public OrderedMap<K, V> copy() {
		return create(store.<K>copy(Filters.<K>acceptAll(), keyProj(), keyCmp, keyCmpIsTotalOrder)) ;
	}

	@Override
	public M filterCopyByKey(FilterPartialOrder<K> filter) {
		return create(store.<K>copy(filter, keyProj(), keyCmp, keyCmpIsTotalOrder)) ;
	}

	@Override
	public M filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter) {
		return create(store.<Entry<K, V>>copy(filter, entryProj(), entryCmp, entryCmpIsTotalOrder)) ;
	}


//	@Override
//	public Iterable<Entry<K, V>> firstOfEachKey(final FilterPartialOrder<Entry<K, V>> filter) {
//		if (keyCmpIsTotalOrder)
//			return entries().all(filter, true) ;
//		return new AbstractIterable<Entry<K, V>>() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public Iterator<Entry<K, V>> iterator() {
//				return store.<Entry<K, V>, Entry<K, V>>
//				all(true, 
//						Filters.and(filter, Filters.forceComparator(Filters.<K>uniqueAsc(), keyCmp, keyProj())), 
//						entryProj(), entryCmp, entryCmpIsTotalOrder, 
//						entryProj()
//				) ;
//			}			
//		} ;
//	}
//
//	@Override
//	public Iterable<Entry<K, V>> lastOfEachKey(final FilterPartialOrder<Entry<K, V>> filter) {
//		if (keyCmpIsTotalOrder)
//			return entries().all(filter, false) ;
//		return new AbstractIterable<Entry<K, V>>() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public Iterator<Entry<K, V>> iterator() {
//				return store.<Entry<K, V>, Entry<K, V>>
//				all(false, 
//						Filters.and(filter, Filters.forceComparator(Filters.<K>uniqueDesc(), keyCmp, keyProj())), 
//						entryProj(), entryCmp, entryCmpIsTotalOrder, 
//						entryProj()
//				) ;
//			}			
//		} ;
//	}
//	@Override
//	public Iterable<V> filterValuesByKey(final FilterPartialOrder<K> filter, final boolean asc) {
//		return new AbstractIterable<V>() {
//			@Override
//			public Iterator<V> iterator() {
//				return store.all(asc, filter, keyProj(), keyCmp, keyCmpIsTotalOrder, valProj());
//			}
//		} ;
//	}
//
//	@Override
//	public Iterable<V> filterValuesByEntry(final FilterPartialOrder<Entry<K, V>> filter, final boolean asc) {
//		return new AbstractIterable<V>() {
//			@Override
//			public Iterator<V> iterator() {
//				return store.all(asc, filter, entryProj(), entryCmp, entryCmpIsTotalOrder, valProj());
//			}
//		} ;
//	}
//
//	@Override
//	public Iterable<Entry<K, V>> unique(final FilterPartialOrder<Entry<K, V>> filter, final boolean asc) {
//		if (entryCmpIsTotalOrder)
//			return all(filter, asc) ;			
//		return new AbstractIterable<Entry<K, V>>() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public Iterator<Entry<K, V>> iterator() {
//				return store.<Entry<K, V>, Entry<K, V>>
//				all(asc, 
//						Filters.and(filter, asc ? Filters.<Entry<K, V>>uniqueAsc() : Filters.<Entry<K, V>>uniqueDesc()), 
//						entryProj(), entryCmp, entryCmpIsTotalOrder, 
//						entryProj()
//				) ;
//			}			
//		} ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> all(final FilterPartialOrder<Entry<K, V>> filter, final boolean asc) {
//		return new AbstractIterable<Entry<K, V>>() {
//			@Override
//			public Iterator<Entry<K, V>> iterator() {
//				return store.<Entry<K, V>, Entry<K, V>>all(asc, filter, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;				
//			}
//		} ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> allByKey(final FilterPartialOrder<K> filter, final boolean asc) {
//		return new AbstractIterable<Entry<K, V>>() {
//			@Override
//			public Iterator<Entry<K, V>> iterator() {
//				return store.all(asc, filter, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj()) ;
//			}
//		} ;
//	}
//
//	@Override
//	public Iterable<Entry<K, V>> uniqueByKey(final FilterPartialOrder<K> filter, final boolean asc) {
//		if (entryCmpIsTotalOrder)
//			return allByKey(filter, asc) ;
//		return new AbstractIterable<Entry<K, V>>() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public Iterator<Entry<K, V>> iterator() {
//				return store.<Entry<K, V>, Entry<K, V>>
//				all(asc, 
//						Filters.and(Filters.forceComparator(filter, keyCmp, keyProj()), 
//								asc ? Filters.<Entry<K, V>>uniqueAsc() : Filters.<Entry<K, V>>uniqueDesc()
//						), 
//						entryProj(), entryCmp, entryCmpIsTotalOrder, 
//						entryProj()
//				) ;
//			}			
//		} ;
//	}
//
//	@Override
//	public Iterable<K> allByEntry(final FilterPartialOrder<Entry<K, V>> filter, final boolean asc) {
//		return new AbstractIterable<K>() {
//			@Override
//			public Iterator<K> iterator() {
//				return store.all(asc, filter, entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
//			}
//		} ;
//	}
//
//	@Override
//	public Iterable<K> uniqueByEntry(final FilterPartialOrder<Entry<K, V>> filter, final boolean asc) {
//		if (keyCmpIsTotalOrder)
//			return allByEntry(filter, asc) ;
//		return new AbstractIterable<K>() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public Iterator<K> iterator() {
//				return store.<Entry<K, V>, K>
//				all(asc, 
//						Filters.and(filter, Filters.forceComparator(
//								asc ? Filters.<K>uniqueAsc() : Filters.<K>uniqueDesc(), 
//							keyCmp, keyProj())), 
//						entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()
//				) ;
//			}			
//		} ;
//	}
//
//	@Override
//	public Iterable<K> unique(final FilterPartialOrder<K> filter, final boolean asc) {
//		if (keyCmpIsTotalOrder)
//			return all(filter, asc) ;			
//		return new AbstractIterable<K>() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public Iterator<K> iterator() {
//				return store.<K, K>
//				all(asc, 
//						Filters.and(filter, asc ? Filters.<K>uniqueAsc() : Filters.<K>uniqueDesc()), 
//						keyProj(), keyCmp, keyCmpIsTotalOrder, 
//						keyProj()
//				) ;
//			}			
//		} ;
//	}
//
//	@Override
//	public Iterable<K> all(final FilterPartialOrder<K> filter, final boolean asc) {
//		return new AbstractIterable<K>() {
//			@Override
//			public Iterator<K> iterator() {
//				return store.<K, K>all(asc, filter, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;				
//			}
//		} ;
//	}
//	

	protected abstract class AbstractFilteredKeySet implements OrderedSet<K> {

		private static final long serialVersionUID = -4052628932082605271L ;
		
		protected final FilterPartialOrder<Entry<K, V>> filter ;
		public AbstractFilteredKeySet(FilterPartialOrder<Entry<K, V>> filter) {
			super() ;
			this.filter = filter ;
		}

		protected final FilterPartialOrder<Entry<K, V>> filterWithKeyLookup(K key) {
			return Filters.and(Filters.<K, Entry<K, V>>forceComparator(Filters.isEqualTo(key), keyCmp, keyProj()), filter) ;
		}
		
		protected final FilterPartialOrder<Entry<K, V>> filterWithKeyFilter(FilterPartialOrder<K> filter2) {
			return Filters.and(filter, Filters.forceComparator(filter2, keyCmp, keyProj())) ;
		}
		
		@Override
		public Iterable<K> all(final boolean asc) {
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.all(asc, filter, entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
				}
			} ;
		}

		@Override
		public Iterable<K> all(final K value, final boolean asc) {
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.all(asc, filterWithKeyLookup(value), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
				}
			} ;
		}

		@Override
		public Iterable<K> all() {
			return all(true) ;
		}

		@Override
		public Iterable<K> all(K value) {
			return all(value, true) ;
		}

		@Override
		public boolean isEmpty() {
			return store.contains(filter, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public boolean permitsDuplicates() {
			return !keyCmpIsTotalOrder ;
		}

		@Override
		public int totalCount() {
			return store.count(filter, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public int uniqueCount() {
			return store.count(filterWithKeyFilter(Filters.<K>uniqueAsc()), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterable<K> unique(final boolean asc) {
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.all(asc, filterWithKeyFilter(Filters.<K>uniqueAsc()), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
				}
			} ;
		}

		@Override
		public Iterable<K> unique() {
			return unique(true) ;
		}

		@Override
		public Pair<K, K> boundaries(K find) {
			return store.boundaries(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, keyProj()) ;
		}

		@Override
		public int count(FilterPartialOrder<K> filter) {
			return store.count(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public K ceil(K find) {
			return store.ceil(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, keyProj()) ;
		}

		@Override
		public K floor(K find) {
			return store.floor(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, keyProj()) ;
		}

		@Override
		public K greater(K find) {
			return store.greater(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, keyProj()) ;
		}

		@Override
		public K lesser(K find) {
			return store.lesser(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, keyProj()) ;
		}

		@Override
		public K first() {
			return store.first(filter, entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public K first(K find) {
			return store.first(filterWithKeyLookup(find), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public K first(FilterPartialOrder<K> filter) {
			return store.first(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public K last() {
			return store.last(filter, entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public K last(FilterPartialOrder<K> filter) {
			return store.last(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public K last(K find) {
			return store.last(filterWithKeyLookup(find), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public int clear() {
			return store.remove(filter, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterator<K> clearAndReturn() {
			return store.removeAndReturn(filter, entryProj(), entryCmp, entryCmpIsTotalOrder).iterator(true, keyProj()) ;
		}

		@Override
		public K put(K val) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public int putAll(Iterable<K> val) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public K putIfAbsent(K val) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public int remove(K value) {
			return store.remove(filterWithKeyLookup(value), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterable<K> removeAndReturn(K value) {
			final S s = store.removeAndReturn(filterWithKeyLookup(value), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return s.iterator(true, keyProj()) ;
				}
				
			} ;
		}

		@Override
		public K removeAndReturnFirst(K value) {
			return store.removeAndReturnFirst(filterWithKeyLookup(value), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public Iterator<K> iterator() {
			return store.all(true, filter, entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public OrderedSet<K> filterCopy(FilterPartialOrder<K> filter) {
			return create(store.copy(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder)).keys() ;
		}

		@Override
		public OrderedSet<K> copy() {
			return create(store.copy(filter, entryProj(), entryCmp, entryCmpIsTotalOrder)).keys() ;
		}

		@Override
		public int remove(FilterPartialOrder<K> filter) {
			return store.remove(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public OrderedSet<K> removeAndReturn(FilterPartialOrder<K> filter) {
			return create(store.removeAndReturn(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder)).keys() ;
		}

		@Override
		public K removeAndReturnFirst(FilterPartialOrder<K> filter) {
			return store.removeAndReturnFirst(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder, keyProj()) ;
		}

		@Override
		public boolean contains(K value) {
			return store.contains(filterWithKeyLookup(value), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public int count(K value) {
			return store.count(filterWithKeyLookup(value), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public List<K> list(K value) {
			return Iters.toList(all(value)) ;
		}

		@Override
		public Boolean apply(K v) {
			return contains(v) ;
		}
		
		@Override
		public void shrink() {
		}

	}
	
	protected abstract class AbstractFilteredMap implements OrderedMap<K, V> {

		private static final long serialVersionUID = 5724108304692745490L ;
		protected final FilterPartialOrder<Entry<K, V>> filter ;
		public AbstractFilteredMap(FilterPartialOrder<Entry<K, V>> filter) {
			this.filter = filter ;
		}

		protected final FilterPartialOrder<Entry<K, V>> filterWithKeyLookup(K key) {
			return Filters.and(Filters.<K, Entry<K, V>>forceComparator(Filters.isEqualTo(key), keyCmp, keyProj()), filter) ;
		}
		
		protected final FilterPartialOrder<Entry<K, V>> filterWithEntryLookup(K key, V val) {
			final ImmutableMapEntry<K, V> entry = new ImmutableMapEntry<K, V>(key, val) ;
			return Filters.and(Filters.<Entry<K, V>>isEqualTo(entry), filter) ;
		}
		
		protected final FilterPartialOrder<Entry<K, V>> filterWithKeyFilter(FilterPartialOrder<K> filter2) {
			return Filters.and(filter, Filters.forceComparator(filter2, keyCmp, keyProj())) ;
		}

		@Override
		public OrderedMap<K, V> copy() {
			return create(store.copy(filter, entryProj(), entryCmp, entryCmpIsTotalOrder)) ;
		}

		@Override
		public OrderedMap<K, V> filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter) {
			return create(store.copy(Filters.and(this.filter, filter), entryProj(), entryCmp, entryCmpIsTotalOrder)) ;
		}

		@Override
		public OrderedMap<K, V> filterCopyByKey(FilterPartialOrder<K> filter) {
			return create(store.copy(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder)) ;
		}

		@Override
		public Entry<K, V> removeAndReturnFirst(FilterPartialOrder<K> filter) {
			return store.removeAndReturnFirst(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public int remove(FilterPartialOrder<K> filter) {
			return store.remove(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public OrderedMap<K, V> removeAndReturn(FilterPartialOrder<K> filter) {
			return create(store.removeAndReturn(filterWithKeyFilter(filter), entryProj(), entryCmp, entryCmpIsTotalOrder)) ;
		}

		@Override
		public OrderedMap<K, V> removeByEntryAndReturn(FilterPartialOrder<Entry<K, V>> filter) {
			return AbstractOrderedMap.this.removeByEntryAndReturn(Filters.and(filter, this.filter)) ;
		}

		@Override
		public int clear() {
			return store.remove(filter, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterator<Entry<K, V>> clearAndReturn() {
			return AbstractOrderedMap.this.removeByEntryAndReturn(this.filter).entries().iterator() ;
		}

		@Override
		public ArbitraryMap<V, K> inverse() {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public V put(K key, V val) {
			N n = nodeFactory.make(key, val) ;
			if (!filter.accept(n, entryCmp))
				throw new IllegalArgumentException(String.format("(%s->%s) does not pass the filter %s", key, val, filter)) ;
			return store.put(n, valProj()) ;
		}

		@Override
		public V putIfAbsent(K key, V val) {
			N n = nodeFactory.make(key, val) ;			
			if (!filter.accept(n, entryCmp))
				throw new IllegalArgumentException(String.format("(%s->%s) does not pass the filter %s", key, val, filter)) ;
			return store.putIfAbsent(n, valProj()) ;
		}

		@Override
		public int remove(K key, V val) {
			return store.remove(filterWithEntryLookup(key, val), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public int remove(K key) {
			return store.remove(filterWithKeyLookup(key), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
			return removeByEntryAndReturn(filterWithEntryLookup(key, val)).entries() ;
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(K key) {
			return removeByEntryAndReturn(filterWithKeyLookup(key)).entries() ;
		}

		@Override
		public V removeAndReturnFirst(K key) {
			return store.removeAndReturnFirst(filterWithKeyLookup(key), entryProj(), entryCmp, entryCmpIsTotalOrder, valProj()) ;
		}

		@Override
		public Pair<V, V> boundaries(K find) {
			return store.boundaries(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, valProj()) ;
		}

		@Override
		public Entry<K, V> first(FilterPartialOrder<Entry<K, V>> filter) {
			return store.first(Filters.and(this.filter, filter), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Pair<Entry<K, V>, Entry<K, V>> boundaryEntries(K find) {
			return store.boundaries(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, entryProj()) ;
		}

		@Override
		public V ceil(K find) {
			return store.ceil(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, valProj()) ;
		}

		@Override
		public Entry<K, V> ceilEntry(K find) {
			return store.ceil(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, entryProj()) ;
		}

		@Override
		public V floor(K find) {			
			return store.floor(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, valProj()) ;
		}

		@Override
		public Entry<K, V> floorEntry(K find) {
			return store.floor(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, entryProj()) ;
		}

		@Override
		public V greater(K find) {
			return store.greater(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, valProj()) ;
		}

		@Override
		public Entry<K, V> greaterEntry(K find) {
			return store.greater(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, entryProj()) ;
		}

		@Override
		public V lesser(K find) {
			return store.lesser(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, valProj()) ;
		}

		@Override
		public Entry<K, V> lesserEntry(K find) {
			return store.lesser(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, filter, entryProj()) ;
		}

		@Override
		public V first(K find) {
			return store.first(filterWithKeyLookup(find), entryProj(), entryCmp, entryCmpIsTotalOrder, valProj());
		}

		@Override
		public Entry<K, V> last(FilterPartialOrder<Entry<K, V>> filter) {
			return store.last(Filters.and(this.filter, filter), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj());
		}

		@Override
		public V last(K find) {
			return store.last(filterWithKeyLookup(find), entryProj(), entryCmp, entryCmpIsTotalOrder, valProj());
		}

		@Override
		public Iterable<Entry<K, V>> firstOfEachKey() {			
			if (keyCmpIsTotalOrder) 
				return entries() ;
			return new AbstractIterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.all(true, filterWithKeyFilter(Filters.<K>uniqueAsc()), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
				}
			} ;
		}

		@Override
		public Iterable<Entry<K, V>> lastOfEachKey() {
			if (keyCmpIsTotalOrder) 
				return entries().all(false) ;
			return new AbstractIterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.all(false, filterWithKeyFilter(Filters.<K>uniqueDesc()), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
				}
			} ;
		}

		@Override
		public Iterable<V> values(final boolean asc) {
			return new AbstractIterable<V>() {
				@Override
				public Iterator<V> iterator() {
					return store.all(asc, filter, entryProj(), entryCmp, entryCmpIsTotalOrder, valProj()) ;
				}
			} ;
		}

		@Override
		public Iterable<V> values(final K key, final boolean asc) {
			return new AbstractIterable<V>() {
				@Override
				public Iterator<V> iterator() {
					return store.all(asc, filterWithKeyLookup(key), entryProj(), entryCmp, entryCmpIsTotalOrder, valProj()) ;
				}
			} ;
		}

		@Override
		public boolean contains(K key, V val) {
			return store.contains(filterWithEntryLookup(key, val), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public boolean contains(K key) {
			return store.contains(filterWithKeyLookup(key), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public int count(K key, V val) {
			return store.count(filterWithEntryLookup(key, val), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterable<V> values() {
			return values(true) ;
		}

		@Override
		public Iterable<V> values(K key) {
			return values(key, true) ;
		}
		
		@Override
		public int count(K key) {
			return store.count(filterWithKeyLookup(key), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterable<Entry<K, V>> entries(final K find, final boolean asc) {
			return new AbstractIterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.all(asc, filterWithKeyLookup(find), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
				}				
			} ;
		}

		@Override
		public Iterable<Entry<K, V>> entries(K key) {
			return entries(key, true) ;
		}

		@Override
		public List<V> list(K key) {
			return Iters.toList(values(key)) ;
		}

		@Override
		public boolean permitsDuplicateKeys() {
			return AbstractOrderedMap.this.permitsDuplicateKeys() ;
		}

		@Override
		public boolean isEmpty() {
			return AbstractOrderedMap.this.isEmpty() ;
		}

		@Override
		public int totalCount() {
			return store.count(filter, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public int uniqueKeyCount() {
			return store.count(Filters.and(filter, Filters.<Entry<K, V>>uniqueAsc()), entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public void shrink() {
		}
		
	}

	protected abstract class AbstractEntrySet implements OrderedMapEntrySet<K, V> {

		private static final long serialVersionUID = -16290453333104178L ;

		@Override
		public int remove(FilterPartialOrder<Entry<K, V>> filter) {
			return store.remove(filter, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}
		
		@Override
		public Entry<K, V> removeAndReturnFirst(FilterPartialOrder<Entry<K, V>> filter) {
			return store.removeAndReturnFirst(filter, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> first() {
			return store.first(Filters.<Entry<K, V>>acceptAll(), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> last() {
			return store.last(Filters.<Entry<K, V>>acceptAll(), entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public OrderedMapEntrySet<K, V> removeAndReturn(FilterPartialOrder<Entry<K, V>> filter) {
			return AbstractOrderedMap.this.removeByEntryAndReturn(filter).entries() ;
		}
		
		@Override
		public Iterable<Entry<K, V>> unique(final boolean asc) {
			if (entryCmpIsTotalOrder)
				return all(asc) ;			
			return new AbstractIterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.<Entry<K, V>, Entry<K, V>>
					all(asc, asc ? Filters.<Entry<K, V>>uniqueAsc() : Filters.<Entry<K, V>>uniqueDesc(), 
						entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()
					) ;
				}			
			} ;
		}

		@Override
		public Iterable<Entry<K, V>> unique() {
			return unique(true) ;
		}

		@Override
		public int clear() {
			return AbstractOrderedMap.this.clear() ;
		}

		@Override
		public Iterator<Entry<K, V>> clearAndReturn() {
			return AbstractOrderedMap.this.clearAndReturn() ;
		}

		@Override
		public org.jjoost.collections.OrderedMap.OrderedMapEntrySet<K, V> copy() {
			return AbstractOrderedMap.this.copy().entries() ;
		}

		@Override
		public Entry<K, V> put(Entry<K, V> val) {
			return store.<Entry<K, V>, Entry<K, V>>put(val, entryProj(), entryInsertCmp, nodeFactory, entryProj()) ;
		}

		@Override
		public int putAll(Iterable<Entry<K, V>> vals) {
			int c = 0 ;
			for (Entry<K, V> val : vals)
				if (put(val) == null)
					c++ ;
			return c ;
		}

		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> val) {
			return store.<Entry<K, V>, Entry<K, V>>putIfAbsent(val, entryProj(), entryInsertCmp, nodeFactory, entryProj()) ;
		}

		@Override
		public int remove(Entry<K, V> val) {
			return store.<Entry<K, V>>remove(val, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> val) {
			return store.<Entry<K, V>, Entry<K, V>>removeAndReturn(val, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> val) {
			return store.<Entry<K, V>, Entry<K, V>>removeAndReturnFirst(val, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public void shrink() {
		}

		@Override
		public Iterable<Entry<K, V>> all(final boolean asc) {
			return new AbstractIterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.<Entry<K, V>>iterator(asc, entryProj()) ;				
				}
			} ;
		}

		@Override
		public Iterable<Entry<K, V>> all() {
			return all(true) ;
		}

		@Override
		public Iterable<Entry<K, V>> all(final Entry<K, V> value) {
			return all(value, true) ;
		}

		@Override
		public Iterable<Entry<K, V>> all(final Entry<K, V> value, final boolean asc) {
			return new AbstractIterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.<Entry<K, V>, Entry<K, V>>all(asc, value, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;				
				}
			} ;
		}

		@Override
		public Pair<Entry<K, V>, Entry<K, V>> boundaries(Entry<K, V> find) {
			return store.boundaries(find, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, Filters.<Entry<K, V>>acceptAll(), entryProj()) ;
		}

		@Override
		public Entry<K, V> ceil(Entry<K, V> find) {
			return store.ceil(find, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public int count(FilterPartialOrder<Entry<K, V>> filter) {
			return store.count(filter, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public org.jjoost.collections.OrderedMap.OrderedMapEntrySet<K, V> filterCopy(FilterPartialOrder<Entry<K, V>> filter) {
			return create(store.copy(filter, entryProj(), entryCmp, entryCmpIsTotalOrder)).entries() ;
		}

		@Override
		public Entry<K, V> first(FilterPartialOrder<Entry<K, V>> filter) {
			return store.first(filter, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> floor(Entry<K, V> find) {
			return store.floor(find, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> greater(Entry<K, V> find) {
			return store.greater(find, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> last(Entry<K, V> find) {
			return store.last(find, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> last(FilterPartialOrder<Entry<K, V>> filter) {
			return store.last(filter, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public Entry<K, V> lesser(Entry<K, V> find) {
			return store.lesser(find, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public boolean permitsDuplicates() {
			return !entryCmpIsTotalOrder ;
		}

		@Override
		public boolean contains(Entry<K, V> value) {
			return store.contains(value, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public int count(Entry<K, V> value) {
			return store.count(value, entryProj(), entryCmp, entryCmpIsTotalOrder) ;
		}

		@Override
		public Entry<K, V> first(Entry<K, V> value) {
			return store.first(value, entryProj(), entryCmp, entryCmpIsTotalOrder, entryProj()) ;
		}

		@Override
		public boolean isEmpty() {
			return store.isEmpty() ;
		}

		@Override
		public List<Entry<K, V>> list(Entry<K, V> value) {
			return Iters.toList(all(value, true)) ;
		}

		@Override
		public int totalCount() {
			return store.count() ;
		}

		@Override
		public int uniqueCount() {
			if (entryCmpIsTotalOrder)
				return store.count() ;
			throw new UnsupportedOperationException() ;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return store.iterator(true, entryProj()) ;
		}

		@Override
		public Boolean apply(Entry<K, V> v) {
			return contains(v) ;
		}		
		
	}

	protected abstract class AbstractKeySet implements OrderedSet<K> {
		
		private static final long serialVersionUID = -16290453333104178L ;
		
		@Override
		public int remove(FilterPartialOrder<K> filter) {
			return store.remove(filter, keyProj(), keyCmp, keyCmpIsTotalOrder) ; 
		}
		
		@Override
		public OrderedSet<K> removeAndReturn(FilterPartialOrder<K> filter) {
			return create(store.removeAndReturn(filter, keyProj(), keyCmp, keyCmpIsTotalOrder)).keys() ; 
		}
		
		@Override
		public int uniqueCount() {
			if (keyCmpIsTotalOrder)
				return store.count() ;
			return AbstractOrderedMap.this.uniqueKeyCount() ;
		}
		
		@Override
		public Iterable<K> unique(final boolean asc) {
			if (keyCmpIsTotalOrder)
				return all(asc) ;			
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.<K, K>
					all(asc, asc ? Filters.<K>uniqueAsc() : Filters.<K>uniqueDesc(), 
						keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()
					) ;
				}			
			} ;
		}

		@Override
		public Iterable<K> unique() {
			return unique(true) ;
		}
		
		@Override
		public int clear() {
			return AbstractOrderedMap.this.clear() ;
		}
		
		@Override
		public Iterator<K> clearAndReturn() {
			return store.clearAndReturn(keyProj()) ; 
		}
		
		@Override
		public OrderedSet<K> copy() {
			return AbstractOrderedMap.this.copy().keys() ;
		}
		
		@Override
		public K put(K val) {
			throw new UnsupportedOperationException() ;
		}
		
		@Override
		public int putAll(Iterable<K> vals) {
			int c = 0 ;
			for (K val : vals)
				if (put(val) == null)
					c++ ;
			return c ;
		}
		
		@Override
		public K putIfAbsent(K val) {
			throw new UnsupportedOperationException() ;
		}
		
		@Override
		public int remove(K val) {
			return store.<K>remove(val, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
		}
		
		@Override
		public Iterable<K> removeAndReturn(K val) {
			return store.<K, K>removeAndReturn(val, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public K removeAndReturnFirst(K val) {
			return store.<K, K>removeAndReturnFirst(val, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public void shrink() {
		}
		
		@Override
		public Iterable<K> all(final boolean asc) {
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.<K>iterator(asc, keyProj()) ;				
				}
			} ;
		}
		
		@Override
		public Iterable<K> all() {
			return all(true) ;
		}
		
		@Override
		public Iterable<K> all(final K value) {
			return all(value, true) ;
		}
		
		@Override
		public Iterable<K> all(final K value, final boolean asc) {
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.<K, K>all(asc, value, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;				
				}
			} ;
		}
		
		@Override
		public Pair<K, K> boundaries(K find) {
			return store.boundaries(find, keyProj(), keyCmp, keyCmpIsTotalOrder, entryProj(), entryCmp, entryCmpIsTotalOrder, Filters.<Entry<K, V>>acceptAll(), keyProj()) ;
		}
		
		@Override
		public K ceil(K find) {
			return store.ceil(find, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public int count(FilterPartialOrder<K> filter) {
			return store.count(filter, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
		}
		
		@Override
		public OrderedSet<K> filterCopy(FilterPartialOrder<K> filter) {
			return create(store.copy(filter, keyProj(), keyCmp, keyCmpIsTotalOrder)).keys() ;
		}
		
		@Override
		public K first(FilterPartialOrder<K> filter) {
			return store.first(filter, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public K floor(K find) {
			return store.floor(find, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public K greater(K find) {
			return store.greater(find, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public K last(K find) {
			return store.last(find, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public K last(FilterPartialOrder<K> filter) {
			return store.last(filter, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public K lesser(K find) {
			return store.lesser(find, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return !keyCmpIsTotalOrder ;
		}
		
		@Override
		public boolean contains(K value) {
			return store.contains(value, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
		}
		
		@Override
		public int count(K value) {
			return store.count(value, keyProj(), keyCmp, keyCmpIsTotalOrder) ;
		}
		
		@Override
		public K first(K value) {
			return store.first(value, keyProj(), keyCmp, keyCmpIsTotalOrder, keyProj()) ;
		}
		
		@Override
		public boolean isEmpty() {
			return store.isEmpty() ;
		}
		
		@Override
		public List<K> list(K value) {
			return Iters.toList(all(value)) ;
		}
		
		@Override
		public int totalCount() {
			return store.count() ;
		}
		
		@Override
		public Iterator<K> iterator() {
			return store.iterator(true, keyProj()) ;
		}
		
		@Override
		public Boolean apply(K v) {
			return contains(v) ;
		}		
		
	}
	
}
