package org.jjoost.collections.maps;
//package org.jjoost.collections.maps;
//
//import java.util.Comparator;
//import java.util.Map;
//import java.util.Random;
//import java.util.Map.Entry;
//
//import org.jjoost.util.FilterTotalOrder;
//import org.jjoost.util.Filters;
//import org.jjoost.util.tuples.Pair;
//
//public class TreapMultiMap<K, V> extends AbstractTreapMap<K, V, TreapMultiMap.MyNode<K, V>, TreapMultiMap<K, V>> implements MultiMap<K, V> {
//
//	private static final long serialVersionUID = -3482453238241673377L ;
//	
//	protected static final class MyNode<K, V> extends AbstractTreapMap.Node<MyNode<K, V>> implements Map.Entry<K, V> {
//
//		private static final long serialVersionUID = -4919426666247600817L ;
//		private final Comparator<? super K> kcmp ;
//		private final Comparator<? super V> vcmp ;
//		private final K key ;
//		private V value ;
//		
//		MyNode(int weight, Comparator<? super K> kcmp, Comparator<? super V> vcmp, K key, V value) {
//			super(weight) ;
//			this.kcmp = kcmp ;
//			this.vcmp = vcmp ;
//			this.key = key ;
//			this.value = value ;
//		}
//
//		MyNode(MyNode<K, V> from) {
//			super(from) ;
//			this.kcmp = from.kcmp ;
//			this.vcmp = from.vcmp ;
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
//			final int c = vcmp.compare(value, this.value) ;
//			if (c != 0)
//				throw new IllegalArgumentException("In an OrderedMultiMap, setValue() may only be called on a value that is equal to the existing value as per the provided comparator; the value provided in this case (" + value + ") is " + (c < 0 ? "less than" : "greater than") + " the existing value (" + this.value + "), so the operation must fail in order to guarantee the new value would not break the ordering of the tree. Please remove and re-insert the key/value pair to accomplish this operation in this case.") ;
//			final V old = this.value ;
//			this.value = value ;
//			return old ;
//		}
//
//		@Override
//		public int compareTo(MyNode<K, V> that) {
//			final int c = kcmp.compare(this.key, that.key) ;
//			return c == 0 ? vcmp.compare(this.value, that.value) : c ;
//		}
//
//		public String toString() {
//			return "{" + key + " -> " + value + "}" ;
//		}
//		
//	}
//	
//	private static class MyKeyFilter<K, V> implements Comparable<MyNode<K, V>>, FilterTotalOrder<MyNode<K, V>> {
//		private static final long serialVersionUID = -1778731884905163059L ;
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
//			// { inclusive == true }  due to TreapMultiMap key only filter (duplicateKeys = true)
//			return (o1 == null || cmp.compare(o1.getKey(), key) <= 0) && (o2 == null || cmp.compare(key, o2.getKey()) <= 0)  ;
//		}
//		@Override
//		public boolean accept(MyNode<K, V> test) {
//			return cmp.compare(key, test.getKey()) == 0 ;
//		}
//	}
//
//	private static class MyKeyValueFilter<K, V> implements Comparable<MyNode<K, V>>, FilterTotalOrder<MyNode<K, V>>  {
//		private static final long serialVersionUID = -7470528275329868511L ;
//		private final K key ;
//		private final V value ;
//		private final Comparator<? super K> kcmp ;
//		private final Comparator<? super V> vcmp ;
//		MyKeyValueFilter(K key, V value, Comparator<? super K> kcmp, Comparator<? super V> vcmp) {
//			super() ;
//			this.key = key ;
//			this.value = value ;
//			this.kcmp = kcmp ;
//			this.vcmp = vcmp ;
//		}
//		@Override
//		public int compareTo(MyNode<K, V> that) {
//			final int c = kcmp.compare(key, that.key) ;
//			return c == 0 ? vcmp.compare(value, that.value) : c ;
//		}
//		@Override
//		public boolean acceptBetween(MyNode<K, V> o1, MyNode<K, V> o2, boolean inclusive) {
//			// { inclusive == false }  due to TreapMultiMap (key, value) only filter
//			final int klb = o1 == null ? -1 : kcmp.compare(o1.getKey(), key) ;
//			final int kub = o2 == null ? -1 : kcmp.compare(key, o2.getKey()) ;
//			return (klb < 0 || (klb == 0 && vcmp.compare(o1.getValue(), value) < 0))
//				&& (kub < 0 || (kub == 0 && vcmp.compare(value, o2.getValue()) < 0)) ;
//		}
//		@Override
//		public boolean accept(MyNode<K, V> test) {
//			return kcmp.compare(key, test.getKey()) == 0 
//			&& vcmp.compare(value, test.getValue()) == 0 ;
//		} 
//	}
//	
//	private final Random rnd = new Random() ;
//	private final Comparator<? super K> kcmp ;
//	private final Comparator<? super V> vcmp ;
//	
//	public TreapMultiMap(Comparator<? super K> kcmp, Comparator<? super V> vcmp) {
//		super(true) ;
//		this.kcmp = kcmp ;
//		this.vcmp = vcmp ;
//	}
//	private TreapMultiMap(Comparator<? super K> kcmp, Comparator<? super V> vcmp, MyNode<K, V> head, int size) {
//		super(true, head, size) ;
//		this.kcmp = kcmp ;
//		this.vcmp = vcmp ;
//	}
//
//	@Override
//	protected FilterTotalOrder<MyNode<K, V>> createFilter(K key, V value) {
//		return new MyKeyValueFilter<K, V>(key, value, kcmp, vcmp) ;
//	}
//
//	@Override
//	protected FilterTotalOrder<MyNode<K, V>> createFilter(K key) {
//		return new MyKeyFilter<K, V>(key, kcmp) ;
//	}
//	
//	@Override
//	protected Comparable<MyNode<K, V>> createComparable(K key) {
//		return new MyKeyFilter<K, V>(key, kcmp) ;
//	}
//	
//	@Override
//	protected TreapMultiMap<K, V> makeTree(MyNode<K, V> head, int size) {
//		return new TreapMultiMap<K, V>(kcmp, vcmp, head, size) ;
//	}
//
//	@SuppressWarnings("unchecked")
//	protected Iterable<K> filterKeys(FilterTotalOrder<? super MyNode<K, V>> filter, final boolean asc, final boolean uniqueKeysOnly) {
//		if (uniqueKeysOnly) {
//			FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//			filters[0] = asc ? new UniqueAscKeyFilter<K, V, MyNode<K, V>>(kcmp) : new UniqueDescKeyFilter<K, V, MyNode<K, V>>(kcmp) ;
//			filters[1] = filter ;
//			filter = Filters.and(filters) ;
//		}
//		return iterable(filter, asc, AbstractTreapMap.<K, V, MyNode<K, V>>keyProjection()) ;
//	}
//	
//	@Override
//	public V put(K key, V val) {
//		final MyNode<K, V> old = _put(new MyNode<K, V>(rnd.nextInt(), kcmp, vcmp, key, val)) ;
//		return old == null ? null : old.value ;
//	}
//	@Override
//	public V putIfAbsent(K key, V val) {
//		final MyNode<K, V> existing = _putIfAbsent(new MyNode<K, V>(rnd.nextInt(), kcmp, vcmp, key, val)) ;
//		return existing == null ? null : existing.value ;
//	}
//
//	public static <K extends Comparable<K>, V extends Comparable<V>> TreapMultiMap<K, V> getComparableMap(Class<K> keyType, Class<V> valueType) {
//		return new TreapMultiMap<K, V>(AbstractTreapMap.<K>getComparator(), AbstractTreapMap.<V>getComparator()) ;
//	}
//	
//	public static <K extends Comparable<K>, V extends Comparable<V>> TreapMultiMap<K, V> getComparableMap() {
//		return new TreapMultiMap<K, V>(AbstractTreapMap.<K>getComparator(), AbstractTreapMap.<V>getComparator()) ;
//	}
//
//
//	@Override
//	public boolean contains(K key, V val) {
//		// we pass false instead of duplicateKeys for TreapMultiMaps, as they do NOT have duplicates for key/value pairs.
//		return _contains(createFilter(key, val), false) ;
//	}
//
//	@Override
//	public int count(K key, V val) {
//		// we pass false instead of duplicateKeys for TreapMultiMaps, as they do NOT have duplicates for key/value pairs.
//		return _count(createFilter(key, val), false) ;
//	}
//	
//
//	public void intersect(TreapMultiMap<K, V> with) {
//		if (!with.kcmp.equals(kcmp) || !with.vcmp.equals(vcmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void subtract(TreapMultiMap<K, V> with) {
//		if (!with.kcmp.equals(kcmp) || !with.vcmp.equals(vcmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void union(TreapMultiMap<K, V> with) {
//		if (!with.kcmp.equals(kcmp) || !with.vcmp.equals(vcmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_subtract(with) ;
//	}
//
//	class EntrySet extends AbstractEntrySet {
//
//		private static final long serialVersionUID = 6323123201890852271L ;
//
//		@Override
//		public Iterable<Entry<K, V>> uniqueByKey(FilterTotalOrder<? super K> filter, boolean asc) {
//			return allByKey(filter, asc) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> unique(boolean asc) {
//			return TreapMultiMap.this.iterable(asc, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//		}
//
//		@Override
//		public Iterable<Entry<K, V>> unique(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			return TreapMultiMap.this.iterable(filter, asc, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//		}
//		
//		@Override
//		public Pair<Entry<K, V>, Entry<K, V>> boundaries(Entry<K, V> find) {
//			return TreapMultiMap.this._boundaries(new MyKeyValueFilter<K, V>(find.getKey(), find.getValue(), kcmp, vcmp), true, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//		}
//
//		@Override
//		public Entry<K, V> ceil(Entry<K, V> find) {
//			return TreapMultiMap.this._ceil(new MyKeyValueFilter<K, V>(find.getKey(), find.getValue(), kcmp, vcmp), true) ;
//		}
//
//		@Override
//		public Entry<K, V> floor(Entry<K, V> find) {
//			return TreapMultiMap.this._floor(new MyKeyValueFilter<K, V>(find.getKey(), find.getValue(), kcmp, vcmp), true) ;
//		}
//
//		@Override
//		public Entry<K, V> lesser(Entry<K, V> find) {
//			return TreapMultiMap.this._lesser(new MyKeyValueFilter<K, V>(find.getKey(), find.getValue(), kcmp, vcmp), true) ;
//		}
//		
//		@Override
//		public Entry<K, V> greater(Entry<K, V> find) {
//			return TreapMultiMap.this._greater(new MyKeyValueFilter<K, V>(find.getKey(), find.getValue(), kcmp, vcmp), true) ;
//		}
//
//		@Override
//		public Entry<K, V> put(Entry<K, V> val) {
//			return TreapMultiMap.this._put(new MyNode<K, V>(rnd.nextInt(), kcmp, vcmp, val.getKey(), val.getValue())) ;
//		}
//		
//	}
//	
//	class KeySet extends AbstractKeySet {
//
//		private static final long serialVersionUID = -2820733903763119702L ;
//
//		@Override
//		public Iterable<K> unique(boolean asc) {
//			return TreapMultiMap.this.iterable(asc ? new UniqueAscKeyFilter<K, V, MyNode<K, V>>(kcmp) : new UniqueDescKeyFilter<K, V, MyNode<K, V>>(kcmp), asc, AbstractTreapMap.<K, V, MyNode<K, V>>keyProjection()) ;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public Iterable<K> unique(FilterTotalOrder<? super K> filter, boolean asc) {
//			FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//			filters[0] = new FilterWrapper<K, V, MyNode<K, V>>(filter) ;
//			filters[1] = asc ? new UniqueAscKeyFilter<K, V, MyNode<K, V>>(kcmp) : new UniqueDescKeyFilter<K, V, MyNode<K, V>>(kcmp) ;
//			return TreapMultiMap.this.iterable(Filters.and(filters), asc, AbstractTreapMap.<K, V, MyNode<K, V>>keyProjection()) ;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public Iterable<K> uniqueByEntry(FilterTotalOrder<? super Entry<K, V>> filter, boolean asc) {
//			FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//			filters[0] = filter ;
//			filters[1] = asc ? new UniqueAscKeyFilter<K, V, MyNode<K, V>>(kcmp) : new UniqueDescKeyFilter<K, V, MyNode<K, V>>(kcmp) ;
//			return TreapMultiMap.this.iterable(Filters.and(filters), asc, AbstractTreapMap.<K, V, MyNode<K, V>>keyProjection()) ;
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
//		return iterable(new UniqueAscKeyFilter<K, V, MyNode<K, V>>(kcmp), true, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//	@Override
//	public Iterable<Entry<K, V>> lastOfEachKey() {
//		return iterable(new UniqueDescKeyFilter<K, V, MyNode<K, V>>(kcmp), false, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterable<Entry<K, V>> firstOfEachKey(FilterTotalOrder<? super Entry<K, V>> filter) {
//		FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//		filters[0] = filter ;
//		filters[1] = new UniqueAscKeyFilter<K, V, MyNode<K, V>>(kcmp) ;
//		return iterable(Filters.and(filters), true, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterable<Entry<K, V>> lastOfEachKey(FilterTotalOrder<? super Entry<K, V>> filter) {
//		FilterTotalOrder<? super MyNode<K, V>>[] filters = new FilterTotalOrder[2] ;
//		filters[0] = filter ;
//		filters[1] = new UniqueDescKeyFilter<K, V, MyNode<K, V>>(kcmp) ;
//		return iterable(Filters.and(filters), false, AbstractTreapMap.<K, V, MyNode<K, V>>entryProjection()) ;
//	}
//
//	@Override
//	public TreapMultiMap<V, K> inverse() {
//		final TreapMultiMap<V, K> r = new TreapMultiMap<V, K>(vcmp, kcmp) ;
//		for (Map.Entry<K, V> entry : entries()) {
//			r.put(entry.getValue(), entry.getKey()) ;
//		}
//		return r ;
//	}
//
//	@Override
//	public Iterable<V> apply(K v) {
//		return values(v) ;
//	}
//
//}
