package org.jjoost.collections.maps;
//package org.jjoost.collections.maps;
//
//import java.util.Comparator;
//import java.util.Map;
//import java.util.Random;
//import java.util.Map.Entry;
//
//import org.jjoost.util.Factory;
//import org.jjoost.util.FilterTotalOrder;
//import org.jjoost.util.Function;
//import org.jjoost.util.Objects;
//import org.jjoost.util.tuples.Pair;
//
//public class TreapMap<K, V> extends AbstractTreapMap<K, V, TreapMap.MyNode<K, V>, TreapMap<K, V>> implements ScalarMap<K, V> {
//
//	private static final long serialVersionUID = -2718090863654308780L ;
//	
//	protected static final class MyNode<K, V> extends AbstractTreapMap.Node<MyNode<K, V>> implements Map.Entry<K, V> {
//
//		private static final long serialVersionUID = -7164429200980806078L ;
//		private final Comparator<? super K> cmp ;
//		private final K key ;
//		private V value ;
//		
//		MyNode(int weight, Comparator<? super K> cmp, K key, V value) {
//			super(weight) ;
//			this.cmp = cmp ;
//			this.key = key ;
//			this.value = value ;
//		}
//
//		MyNode(MyNode<K, V> from) {
//			super(from) ;
//			this.cmp = from.cmp ;
//			this.key = from.key ;
//			this.value = from.value ;
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
//			return cmp.compare(this.key, that.key) ;
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
//			return inclusive 
//			? (o1 == null || cmp.compare(o1.getKey(), key) <= 0) && (o2 == null || cmp.compare(key, o2.getKey()) <= 0)
//			: (o1 == null || cmp.compare(o1.getKey(), key) <  0) && (o2 == null || cmp.compare(key, o2.getKey()) <  0) ;
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
//			// cannot safely enforce inclusivity as we force true for certain operations internally to the treap
//			return inclusive
//			? (o1 == null || cmp.compare(o1.getKey(), key) <= 0) && (o2 == null || cmp.compare(key, o2.getKey()) <= 0)
//			: (o1 == null || cmp.compare(o1.getKey(), key) <  0) && (o2 == null || cmp.compare(key, o2.getKey()) <  0) ;
//		}
//		@Override
//		public boolean accept(MyNode<K, V> test) {
//			return cmp.compare(key, test.getKey()) == 0 && Objects.equalQuick(value, test.getValue()) ;
//		}
//	}
//	
//	private final Random rnd = new Random() ;
//	private final Comparator<? super K> cmp ;
//	
//	public TreapMap(Comparator<? super K> cmp) {
//		super(false) ;
//		this.cmp = cmp ;
//	}
//	private TreapMap(Comparator<? super K> cmp, MyNode<K, V> head, int size) {
//		super(false, head, size) ;
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
//	protected TreapMap<K, V> makeTree(MyNode<K, V> head, int size) {
//		return new TreapMap<K, V>(cmp, head, size) ;
//	}
//
//	@Override
//	@SuppressWarnings("serial")
//	public V putIfAbsent(final K key, final Function<? super K, ? extends V> putIfNotPresent) {
//		final MyNode<K, V> n = _putIfAbsent(new MyKeyFilter<K, V>(key, cmp), new Factory<MyNode<K, V>>() {
//			@Override
//			public MyNode<K, V> create() {
//				return new MyNode<K, V>(rnd.nextInt(), cmp, key, putIfNotPresent.apply(key)) ;
//			}
//		}) ;
//		return n == null ? null : n.value ;
//	}
//	@Override
//	@SuppressWarnings("serial")
//	public V ensureAndGet(final K key, final Factory<? extends V> putIfNotPresent) {
//		return _ensureAndGet(new MyKeyFilter<K, V>(key, cmp), new Factory<MyNode<K, V>>() {
//			@Override
//			public MyNode<K, V> create() {
//				return new MyNode<K, V>(rnd.nextInt(), cmp, key, putIfNotPresent.create()) ;
//			}
//		}).value ;
//	}
//	@Override
//	@SuppressWarnings("serial")
//	public V ensureAndGet(final K key, final Function<? super K, ? extends V> putIfNotPresent) {
//		return _ensureAndGet(new MyKeyFilter<K, V>(key, cmp), new Factory<MyNode<K, V>>() {
//			@Override
//			public MyNode<K, V> create() {
//				return new MyNode<K, V>(rnd.nextInt(), cmp, key, putIfNotPresent.apply(key)) ;
//			}
//		}).value ;
//	}
//	@Override
//	public V put(K key, V val) {
//		final MyNode<K, V> old = _put(new MyNode<K, V>(rnd.nextInt(), cmp, key, val)) ;
//		return old == null ? null : old.value ;
//	}
//	@Override
//	public V putIfAbsent(K key, V val) {
//		final MyNode<K, V> existing = _putIfAbsent(new MyNode<K, V>(rnd.nextInt(), cmp, key, val)) ;
//		return existing == null ? null : existing.value ;
//	}
//
//	public static <K extends Comparable<K>, V> TreapMap<K, V> getComparableMap(Class<K> keyType) {
//		return new TreapMap<K, V>(AbstractTreapMap.<K>getComparator()) ;
//	}
//	
//	public static <K extends Comparable<K>, V> TreapMap<K, V> getComparableMap() {
//		return new TreapMap<K, V>(AbstractTreapMap.<K>getComparator()) ;
//	}
//	
//	@Override
//	public final V get(K key) {
//		return first(key) ;
//	}
//
//	public void intersect(TreapMap<K, V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void subtract(TreapMap<K, V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void union(TreapMap<K, V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_subtract(with) ;
//	}
//	
//	class EntrySet extends AbstractEntrySet {
//
//		private static final long serialVersionUID = 4561491260213320330L ;
//
//		@Override
//		public Iterable<Entry<K, V>> uniqueByKey(FilterTotalOrder<? super K> filter, boolean asc) {
//			return allByKey(filter, asc) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> unique(boolean asc) {
//			return all(asc) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> unique(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			return all(filter, asc) ;
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
//		@Override
//		public Entry<K, V> put(Entry<K, V> val) {
//			throw new UnsupportedOperationException() ;
//		}
//		
//	}
//	
//	class KeySet extends AbstractKeySet {
//
//		private static final long serialVersionUID = 8343502508535077846L ;
//
//		@Override
//		public Iterable<K> unique(boolean asc) {
//			return all(asc) ;
//		}
//
//		@Override
//		public Iterable<K> unique(FilterTotalOrder<? super K> filter, boolean asc) {
//			return all(filter, asc) ;
//		}
//
//		@Override
//		public Iterable<K> uniqueByEntry(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			return allByEntry(filter, asc) ;
//		}
//
//		@Override
//		public K put(K val) {
//			throw new UnsupportedOperationException() ;			
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
//		return this.entries() ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> lastOfEachKey() {
//		return this.entries() ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> firstOfEachKey(FilterTotalOrder<? super Entry<K, V>> filter) {
//		return this.entries().all(filter, true) ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> lastOfEachKey(FilterTotalOrder<? super Entry<K, V>> filter) {
//		return this.entries().all(filter, true) ;
//	}
//	@Override
//	public V apply(K v) {
//		return get(v) ;
//	}
//
//	@Override
//	public ArbitraryMap<V, K> inverse() {
//		// TODO :
//		throw new UnsupportedOperationException() ;
//	}
//
//}
