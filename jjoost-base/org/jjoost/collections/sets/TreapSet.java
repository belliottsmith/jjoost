package org.jjoost.collections.sets;
//package org.jjoost.collections.sets;
//
//import java.util.Comparator;
//import java.util.Random;
//
//import org.jjoost.util.FilterTotalOrder;
//
//public class TreapSet<V> extends AbstractTreapSet<V, TreapSet.MyNode<V>> implements ScalarSet<V> {
//
//	private static final long serialVersionUID = 3555083550263290558L ;
//
//	protected static final class MyNode<V> extends AbstractTreapSet.Node<MyNode<V>> implements AbstractTreapSet.ValueRepository<V> {
//
//		private static final long serialVersionUID = 6144575752917085465L ;
//		private final Comparator<? super V> cmp ;
//		private V value ;
//		
//		MyNode(int weight, Comparator<? super V> cmp, V value) {
//			super(weight) ;
//			this.cmp = cmp ;
//			this.value = value ;
//		}
//
//		MyNode(MyNode<V> from) {
//			super(from) ;
//			this.cmp = from.cmp ;
//			this.value = from.value ;
//		}
//		
//		@Override
//		protected MyNode<V> copy() {
//			return new MyNode<V>(this) ;
//		}
//
//		@Override
//		public V getValue() {
//			return value ;
//		}
//
//		@Override
//		public int compareTo(MyNode<V> that) {
//			return cmp.compare(this.value, that.value) ;
//		}
//		
//		public String toString() {
//			return value == null ? "null" : value.toString() ;
//		}
//		
//	}
//	
//	private static class MyFilter<V> implements Comparable<MyNode<V>>, FilterTotalOrder<MyNode<V>> {
//		private static final long serialVersionUID = -1223078040429797947L ;
//		private final V value ;
//		private final Comparator<? super V> cmp ;
//		MyFilter(V value, Comparator<? super V> cmp) {
//			super() ;
//			this.value = value ;
//			this.cmp = cmp ;
//		}
//		@Override
//		public int compareTo(MyNode<V> that) {
//			return cmp.compare(value, that.value) ;
//		}
//		@Override
//		public boolean acceptBetween(MyNode<V> o1, MyNode<V> o2, boolean inclusive) {
//			return inclusive 
//			? (o1 == null || cmp.compare(o1.getValue(), value) <= 0) && (o2 == null || cmp.compare(value, o2.getValue()) <= 0)
//			: (o1 == null || cmp.compare(o1.getValue(), value) <  0) && (o2 == null || cmp.compare(value, o2.getValue()) <  0) ;
//		}
//		@Override
//		public boolean accept(MyNode<V> test) {
//			return cmp.compare(value, test.getValue()) == 0 ;
//		}
//	}
//
//	private final Random rnd = new Random() ;
//	private final Comparator<? super V> cmp ;
//	
//	public TreapSet(Comparator<? super V> cmp) {
//		super(false) ;
//		this.cmp = cmp ;
//	}
//	private TreapSet(Comparator<? super V> cmp, MyNode<V> head, int size) {
//		super(false, head, size) ;
//		this.cmp = cmp ;
//	}
//
//	public static <V extends Comparable<V>> TreapSet<V> getComparableSet(Class<V> valueType) {
//		return new TreapSet<V>(AbstractTreapSet.<V>getComparator()) ;
//	}
//	
//	public static <V extends Comparable<V>> TreapSet<V> getComparableSet() {
//		return new TreapSet<V>(AbstractTreapSet.<V>getComparator()) ;
//	}
//	
//	@Override
//	protected FilterTotalOrder<MyNode<V>> createFilter(V value) {
//		return new MyFilter<V>(value, cmp) ;
//	}
//
//	@Override
//	protected Comparable<MyNode<V>> createComparable(V key) {
//		return new MyFilter<V>(key, cmp) ;
//	}
//	
//	@Override
//	protected TreapSet<V> makeTree(MyNode<V> head, int size) {
//		return new TreapSet<V>(cmp, head, size) ;
//	}
//
//	@Override
//	public V put(V val) {
//		final MyNode<V> old = _put(new MyNode<V>(rnd.nextInt(), cmp, val)) ;
//		return old == null ? null : old.value ;
//	}
//	@Override
//	public V putIfAbsent(V val) {
//		final MyNode<V> existing = _putIfAbsent(new MyNode<V>(rnd.nextInt(), cmp, val)) ;
//		return existing == null ? null : existing.value ;
//	}
//
//	@Override
//	public final V get(V key) {
//		return first(key) ;
//	}
//
//	public void intersect(TreapSet<V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void subtract(TreapSet<V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_intersect(with) ;
//	}
//	public void union(TreapSet<V> with) {
//		if (!with.cmp.equals(cmp))
//			throw new IllegalArgumentException("Comparators for the two maps must be the same") ;
//		_subtract(with) ;
//	}
//	
//	@Override
//	public Iterable<V> unique() {
//		return all() ;
//	}
//	@Override
//	public Iterable<V> unique(boolean asc) {
//		return all(asc) ;
//	}
//	
//	@Override
//	public boolean permitsDuplicates() {
//		return false ;
//	}
//	
//	@Override
//	public Iterable<V> unique(FilterTotalOrder<? super V> filter, boolean asc) {
//		return all(filter, asc) ;
//	}
//
//	@Override
//	public TreapSet<V> copy() {
//		return makeTree(_copy(), size()) ;
//	}
//
//}
