package org.jjoost.collections.maps;
//package org.jjoost.collections.maps;
//
//import java.util.Comparator;
//import java.util.Map;
//import java.util.Random;
//import java.util.Map.Entry;
//import java.util.concurrent.atomic.AtomicLong;
//
//import org.jjoost.collections.sets.LLHashSet;
//import org.jjoost.util.FilterTotalOrder;
//import org.jjoost.util.Filters;
//import org.jjoost.util.Objects;
//import org.jjoost.util.tuples.Pair;
//
///**
// * Guarantees the ordering of values with the same key (i.e. first one added will always be enumerated first); after this a much less useful constraint of all keys added within 
// * the same batch of 4 billion puts will be ordered correctly, but those that span the boundary of a reset of the counter will be swapped (so that those inserted after the boundary will occur before those 
// * inserted before the boundary).
// * @author Benedict Elliott Smith
// *
// * @param <K>
// * @param <V>
// */
//public class TreapBagMap<K, V> extends AbstractTreapMap<K, V, TreapBagMap.MyNode<K, V>, TreapBagMap<K, V>> implements ListMap<K, V> {
//
//	private static final long serialVersionUID = -8208341777726878706L ;
//	
//	protected static final class MyNode<K, V> extends AbstractTreapMap.Node<MyNode<K, V>> implements Map.Entry<K, V> {
//
//		private static final long serialVersionUID = -876120501640184722L ;
//		private final Comparator<? super K> cmp ;
//		private final K key ;
//		private V value ;
//		private final long id ;
//		
//		MyNode(int weight, Comparator<? super K> cmp, K key, V value, long id) {
//			super(weight) ;
//			this.cmp = cmp ;
//			this.key = key ;
//			this.value = value ;
//			this.id = id ;
//		}
//
//		MyNode(MyNode<K, V> from) {
//			super(from) ;
//			this.cmp = from.cmp ;
//			this.key = from.key ;
//			this.value = from.value ;
//			this.id = from.id ;
//		}
//		
//		@Override
//		protected MyNode<K, V> copy() {
//			return new MyNode<K, V>(this) ;
//		}
//
//		@Override
//		public K getKey() {
//			return key ;
//		}
//
//		@Override
//		public V getValue() {
//			return value ;
//		}
//
//		@Override
//		public V setValue(V value) {
//			final V old = this.value ;
//			this.value = value ;
//			return old ;
//		}
//
//		@Override
//		public int compareTo(MyNode<K, V> that) {
//			final int d = cmp.compare(this.key, that.key) ;
//			return d == 0 ? (int) (this.id - that.id) : d ; 
//		}
//		
//		public String toString() {
//			return "{" + key + " -> " + value + "}" ;
//		}
//		
//	}
//	
//	private static class MyKeyFilter<K, V> implements Comparable<MyNode<K, V>>, FilterTotalOrder<MyNode<K, V>> {
//		private static final long serialVersionUID = -1223078040429797947L ;
//		private final K key ;
//		private final Comparator<? super K> cmp ;
//		MyKeyFilter(K key, Comparator<? super K> cmp) {
//			super() ;
//			this.key = key ;
//			this.cmp = cmp ;
//		}
//		@Override
//		public int compareTo(MyNode<K, V> that) {
//			return cmp.compare(key, that.key) ;
//		}
//		@Override
//		public boolean acceptBetween(MyNode<K, V> o1, MyNode<K, V> o2, boolean inclusive) {
//			// { inclusive == true }  due to TreapBagMap (duplicateKeys = true)
//			return (o1 == null || cmp.compare(o1.getKey(), key) <= 0) && (o2 == null || cmp.compare(key, o2.getKey()) <= 0)  ;
//		}
//		@Override
//		public boolean accept(MyNode<K, V> test) {
//			return cmp.compare(key, test.getKey()) == 0 ;
//		}
//	}
//
//	private static class MyKeyValueFilter<K, V> implements FilterTotalOrder<MyNode<K, V>> {
//		private static final long serialVersionUID = -6761191289716757046L ;
//		private final K key ;
//		private final V value ;
//		private final Comparator<? super K> cmp ;
//		MyKeyValueFilter(K key, V value, Comparator<? super K> cmp) {
//			super() ;
//			this.key = key ;
//			this.value = value ;
//			this.cmp = cmp ;
//		}
//		@Override
//		public boolean acceptBetween(MyNode<K, V> o1, MyNode<K, V> o2, boolean inclusive) {
//			// { inclusive == true }  due to TreapBagMap (duplicateKeys = true)
//			return (o1 == null || cmp.compare(o1.getKey(), key) <= 0) && (o2 == null || cmp.compare(key, o2.getKey()) <= 0)  ;
//		}
//		@Override
//		public boolean accept(MyNode<K, V> test) {
//			return cmp.compare(test.getKey(), key) == 0 && Objects.equalQuick(value, test.getValue()) ;
//		}
//	}
//	
//	protected static final class UniqueEntryFilter<K, V, N extends Node<N> & Map.Entry<K, V>> implements FilterTotalOrder<N> {
//
//		private static final long serialVersionUID = 6559250511146745086L ;
//		private final Comparator<? super K> cmp ;
//		private final LLHashSet<V> seenForThisKey = new LLHashSet<V>() ;
//		private K lastKey = null ;
//
//		protected UniqueEntryFilter(Comparator<? super K> cmp) {
//			super() ;
//			this.cmp = cmp ;
//		}
//
//		@Override
//		public boolean acceptBetween(N o1, N o2, boolean inclusive) {
//			return true ;
//		}
//
//		@Override
//		public boolean accept(N test) {
//			final int c = cmp.compare(test.getKey(), lastKey) ;
//			if (c == 0) {
//				return seenForThisKey.putIfAbsent(test.getValue()) == null ;
//			} else {
//				seenForThisKey.clear() ;
//				seenForThisKey.put(test.getValue()) ;
//				lastKey = test.getKey() ;
//				return true ;
//			}
//		}
//		
//	}
//	
//	private final Random rnd = new Random() ;
//	private final Comparator<? super K> cmp ;
//	private final AtomicLong id = new AtomicLong(Long.MIN_VALUE) ;
//	
//	public TreapBagMap(Comparator<? super K> cmp) {
//		super(true) ;
//		this.cmp = cmp ;
//	}
//	private TreapBagMap(Comparator<? super K> cmp, MyNode<K, V> head, int size) {
//		super(true, head, size) ;
//		this.cmp = cmp ;
//	}
//
//	@Override
//	protected FilterTotalOrder<MyNode<K, V>> createFilter(K key, V value) {
//		return new MyKeyValueFilter<K, V>(key, value, cmp) ;
//	}
//
//	@Override
//	protected Comparable<MyNode<K, V>> createComparable(K key) {
//		return new MyKeyFilter<K, V>(key, cmp) ;
//	}
//	
//	@Override
//	protected FilterTotalOrder<MyNode<K, V>> createFilter(K key) {
//		return new MyKeyFilter<K, V>(key, cmp) ;
//	}
//
//	@Override
//	protected TreapBagMap<K, V> makeTree(MyNode<K, V> head, int size) {
//		return new TreapBagMap<K, V>(cmp, head, size) ;
//	}
//
//	@Override
//	public V put(K key, V val) {
//		_put(new MyNode<K, V>(rnd.nextInt(), cmp, key, val, id.incrementAndGet())) ;
//		return null ;
//	}
//
//	public static <K extends Comparable<K>, V> TreapBagMap<K, V> getComparableMap(Class<K> keyType) {
//		return new TreapBagMap<K, V>(AbstractTreapMap.<K>getComparator()) ;
//	}
//	
//	public static <K extends Comparable<K>, V> TreapBagMap<K, V> getComparableMap() {
//		return new TreapBagMap<K, V>(AbstractTreapMap.<K>getComparator()) ;
//	}
//	
//	public void intersect(TreapBagMap<K, V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void subtract(TreapBagMap<K, V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void union(TreapBagMap<K, V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_subtract(with) ;
//	}
//	
//	class EntrySet extends AbstractEntrySet {
//
//		private static final long serialVersionUID = -5164380156346074544L ;
//
//		@Override
//		public Iterable<Entry<K, V>> uniqueByKey(FilterTotalOrder<? super K> filter, boolean asc) {
//			return allByKey(filter, asc) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> unique(boolean asc) {
//			return TreapBagMap.this.iterable(new UniqueEntryFilter<K, V, MyNode<K, V>>(cmp), asc, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//		}
//
//		@Override
//		@SuppressWarnings("unchecked")
//		public Iterable<Entry<K, V>> unique(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			return TreapBagMap.this.iterable(Filters.and(filter, new UniqueEntryFilter<K, V, MyNode<K, V>>(cmp)), asc, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//		}
//		
//		@Override
//		public Pair<Entry<K, V>, Entry<K, V>> boundaries(Entry<K, V> find) {
//			throw new UnsupportedOperationException() ;
//		}
//
//		@Override
//		public Entry<K, V> ceil(Entry<K, V> find) {
//			throw new UnsupportedOperationException() ;
//		}
//
//		@Override
//		public Entry<K, V> floor(Entry<K, V> find) {
//			throw new UnsupportedOperationException() ;
//		}
//
//		@Override
//		public Entry<K, V> lesser(Entry<K, V> find) {
//			throw new UnsupportedOperationException() ;
//		}
//		
//		@Override
//		public Entry<K, V> greater(Entry<K, V> find) {
//			throw new UnsupportedOperationException() ;
//		}
//		
//	}
//	
//	class KeySet extends AbstractKeySet {
//
//		private static final long serialVersionUID = -837805457448706173L ;
//
//		@Override
//		public Iterable<K> unique(boolean asc) {
//			return TreapBagMap.this.iterable(asc ? new UniqueAscKeyFilter<K, V, MyNode<K, V>>(cmp) : new UniqueDescKeyFilter<K, V, MyNode<K, V>>(cmp), asc, AbstractTreapMap.<K, V, MyNode<K, V>>keyProjection()) ;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public Iterable<K> unique(FilterTotalOrder<? super K> filter, boolean asc) {
//			FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//			filters[0] = new FilterWrapper<K, V, MyNode<K, V>>(filter) ;
//			filters[1] = asc ? new UniqueAscKeyFilter<K, V, MyNode<K, V>>(cmp) : new UniqueDescKeyFilter<K, V, MyNode<K, V>>(cmp) ;
//			return TreapBagMap.this.iterable(Filters.and(filters), asc, AbstractTreapMap.<K, V, MyNode<K, V>>keyProjection()) ;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public Iterable<K> uniqueByEntry(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//			filters[0] = filter ;
//			filters[1] = asc ? new UniqueAscKeyFilter<K, V, MyNode<K, V>>(cmp) : new UniqueDescKeyFilter<K, V, MyNode<K, V>>(cmp) ;
//			return TreapBagMap.this.iterable(Filters.and(filters), asc, AbstractTreapMap.<K, V, MyNode<K, V>>keyProjection()) ;
//		}
//		
//	}
//
//	@Override
//	EntrySet createEntrySet() {
//		return new EntrySet();
//	}
//	@Override
//	KeySet createKeySet() {
//		return new KeySet() ;
//	}
//	
//	@Override
//	public Iterable<Entry<K, V>> firstOfEachKey() {
//		return iterable(new UniqueAscKeyFilter<K, V, MyNode<K, V>>(cmp), true, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> lastOfEachKey() {
//		return iterable(new UniqueDescKeyFilter<K, V, MyNode<K, V>>(cmp), false, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterable<Entry<K, V>> firstOfEachKey(FilterTotalOrder<? super Entry<K, V>> filter) {
//		FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//		filters[0] = filter ;
//		filters[1] = new UniqueAscKeyFilter<K, V, MyNode<K, V>>(cmp) ;
//		return iterable(Filters.and(filters), true, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterable<Entry<K, V>> lastOfEachKey(FilterTotalOrder<? super Entry<K, V>> filter) {
//		FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//		filters[0] = filter ;
//		filters[1] = new UniqueDescKeyFilter<K, V, MyNode<K, V>>(cmp) ;
//		return iterable(Filters.and(filters), false, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//
//	@Override
//	public ArbitraryMap<V, K> inverse() {
//		throw new UnsupportedOperationException() ;
//	}
//
//	@Override
//	public Iterable<V> apply(K v) {
//		return values(v) ;
//	}
//
//}
