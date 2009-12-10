package org.jjoost.collections.sets;
//package org.jjoost.collections.sets;
//
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//
//import org.jjoost.collections.Collections;
//import org.jjoost.collections.abstr.AbstractMutableTreap;
//import org.jjoost.util.FilterTotalOrder;
//import org.jjoost.util.Function;
//import org.jjoost.util.tuples.Pair;
//
//public abstract class AbstractTreapSet<V, N extends AbstractMutableTreap.Node<N> & AbstractTreapSet.ValueRepository<V>> extends AbstractMutableTreap<N> implements OrderedSet<V> {
//
//	private static final long serialVersionUID = -2730749706430096608L ;
//
//	public static interface ValueRepository<V> {
//		V getValue() ;
//	}
//	
//	protected abstract Comparable<N> createComparable(V key) ;
//	protected abstract FilterTotalOrder<N> createFilter(V key) ;
//	protected abstract AbstractTreapSet<V, N> makeTree(N head, int size) ;
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
//	private final boolean duplicateKeys ;
//	
//	protected AbstractTreapSet(final boolean permitsDuplicateKeys) {
//		super() ;
//		this.duplicateKeys = permitsDuplicateKeys ;
//	}
//	protected AbstractTreapSet(final boolean permitsDuplicateKeys, N head, int size) {
//		super(head, size) ;
//		this.duplicateKeys = permitsDuplicateKeys ;
//	}
//
//	@SuppressWarnings("unchecked")
//	private static final Function VALUE_PROJECTION = new Function<ValueRepository<Object>, Object>() {
//		private static final long serialVersionUID = 6140723772513448903L ;
//		@Override
//		public Object apply(ValueRepository<Object> v) {
//			return v.getValue() ;
//		}
//	} ;
//	
//	@SuppressWarnings("unchecked")
//	protected static final <V, N extends AbstractMutableTreap.Node<N> & AbstractTreapSet.ValueRepository<V>> Function<N, V> valueProjection() {
//		return (Function<N, V>) VALUE_PROJECTION ;
//	}
//
//	protected static final class FilterWrapper<V, N extends Node<N> & AbstractTreapSet.ValueRepository<V>> implements FilterTotalOrder<N> {
//		private static final long serialVersionUID = 2875326660732397597L ;
//		private final FilterTotalOrder<? super V> wrapped ;
//		FilterWrapper(FilterTotalOrder<? super V> wrapped) {
//			super() ;
//			this.wrapped = wrapped ;
//		}
//		@Override
//		public boolean acceptBetween(N o1, N o2, boolean inclusive) {
//			return wrapped.acceptBetween(o1 == null ? null : o1.getValue(), o2 == null ? null : o2.getValue(), inclusive) ;
//		}
//		@Override
//		public boolean accept(N test) {
//			return wrapped.accept(test.getValue()) ;
//		}
//	}
//	
//	protected static final class UniqueAscKeyFilter<V, N extends Node<N> & AbstractTreapSet.ValueRepository<V>> implements FilterTotalOrder<N> {
//
//		private static final long serialVersionUID = 6559250511146745086L ;
//		private final Comparator<? super V> cmp ;
//		private V min = null ;
//
//		protected UniqueAscKeyFilter(Comparator<? super V> cmp) {
//			super() ;
//			this.cmp = cmp ;
//		}
//
//		@Override
//		public boolean acceptBetween(N o1, N o2, boolean inclusive) {
//			return inclusive 
//			? min == null || o2 == null || cmp.compare(min, o2.getValue()) <= 0
//			: min == null || o2 == null || cmp.compare(min, o2.getValue()) < 0 ;
//		}
//
//		@Override
//		public boolean accept(N test) {
//			final boolean accept = min == null || cmp.compare(min, test.getValue()) < 0 ;
//			if (accept) {
//				min = test.getValue() ;
//			}
//			return accept ;
//		}
//		
//	}
//	
//	protected static final class UniqueDescKeyFilter<V, N extends Node<N> & AbstractTreapSet.ValueRepository<V>> implements FilterTotalOrder<N> {
//		
//		private static final long serialVersionUID = 6559250511146745086L ;
//		private final Comparator<? super V> cmp ;
//		private V max = null ;
//		
//		protected UniqueDescKeyFilter(Comparator<? super V> cmp) {
//			super() ;
//			this.cmp = cmp ;
//		}
//		
//		@Override
//		public boolean acceptBetween(N o1, N o2, boolean inclusive) {
//			return inclusive 
//			? max == null || o1 == null || cmp.compare(o1.getValue(), max) <= 0
//			: max == null || o1 == null || cmp.compare(o1.getValue(), max) < 0 ;
//		}
//		
//		@Override
//		public boolean accept(N test) {
//			final boolean accept = max == null || cmp.compare(test.getValue(), max) < 0 ;
//			if (accept) {
//				max = test.getValue() ;
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
//	public boolean contains(V val) {
//		// we should be passing false instead of duplicateKeys for TreapMultiMaps, as they do NOT have duplicates for key/value pairs. HOWEVER the filter implementation can sensibly ignore this parameter and use the correct strategy.
//		return _contains(createFilter(val), duplicateKeys) ;
//	}
//
//	// simple getters
//	
//	@Override
//	public V first(V key) {
//		final N n = _first(createFilter(key), duplicateKeys) ;
//		return n == null ? null : n.getValue() ;
//	}
//	
//	@Override
//	public V last(V key) {
//		final N n = _last(createFilter(key), duplicateKeys) ;
//		return n == null ? null : n.getValue() ;
//	}
//
//	@Override
//	public V first(FilterTotalOrder<? super V> filter) {
//		final N n =_first(new FilterWrapper<V, N>(filter), duplicateKeys) ;
//		return n == null ? null : n.getValue() ;
//	}
//	
//	@Override
//	public V last(FilterTotalOrder<? super V> filter) {
//		final N n =_last(new FilterWrapper<V, N>(filter), duplicateKeys) ;
//		return n == null ? null : n.getValue() ;
//	}
//	
//	@Override
//	public Pair<V, V> boundaries(V find) {
//		return _boundaries(createComparable(find), duplicateKeys, AbstractTreapSet.<V, N>valueProjection()) ;
//	}
//	
//	@Override
//	public V ceil(V find) {
//		final N ceil = _ceil(createComparable(find), duplicateKeys) ;
//		return ceil == null ? null : ceil.getValue() ;
//	}
//	
//	@Override
//	public V floor(V find) {
//		final N floor = _floor(createComparable(find), duplicateKeys) ;
//		return floor == null ? null : floor.getValue() ;
//	}
//	
//	@Override
//	public V lesser(V find) {
//		final N lesser = _lesser(createComparable(find), duplicateKeys) ;
//		return lesser == null ? null : lesser.getValue() ;
//	}
//	
//	@Override
//	public V greater(V find) {
//		final N greater = _greater(createComparable(find), duplicateKeys) ;
//		return greater == null ? null : greater.getValue() ;
//	}
//	
//
//
//	// simple iterable getters
//	
//	
//	@Override
//	public Iterable<V> all() {
//		return all(true) ;
//	}
//
//	@Override
//	public Iterable<V> all(final boolean asc) {
//		return iterable(asc, AbstractTreapSet.<V, N>valueProjection()) ;
//	}
//	
//
//
//	
//	
//	@Override
//	public Iterable<V> all(final V key) {
//		return all(key, true) ;
//	}
//	@Override
//	public Iterable<V> all(final V key, final boolean asc) {
//		return iterable(createFilter(key), asc, AbstractTreapSet.<V, N>valueProjection()) ;
//	}
//	
//	@Override
//	public List<V> list(V key) {
//		return list(key, true) ;
//	}
//	@Override
//	public List<V> list(V key, boolean asc) {
//		return Collections.toList(all(key, asc)) ;
//	}
//
//	
//	
//	@Override
//	public Iterable<V> all(final FilterTotalOrder<? super V> filter, final boolean asc) {
//		return iterable(new FilterWrapper<V, N>(filter), asc, AbstractTreapSet.<V, N>valueProjection()) ;
//	}
//	
//	@Override
//	public OrderedSet<V> filterCopy(FilterTotalOrder<? super V> filter) {
//		final Subtree<N> result = _copiedsubtree(new FilterWrapper<V, N>(filter), duplicateKeys) ;
//		return makeTree(result.head, result.size) ;
//	}
//	
//
//
//	
//	@Override
//	public int remove(V val) {
//		return _remove(createFilter(val), duplicateKeys).numberRemoved ;
//	}
//
//	@Override
//	public OrderedSet<V> remove(FilterTotalOrder<? super V> filter) {
//		final Removed<N> result = _remove(new FilterWrapper<V, N>(filter), duplicateKeys) ;
//		return makeTree(result.removed, result.numberRemoved) ;
//	}
//	
//	
//	
//	
//	protected final <F> Iterable<F> iterable(final FilterTotalOrder<? super N> filter, final boolean asc, final Function<N, F> projection) {
//		return new Iterable<F>() {
//			@Override
//			public Iterator<F> iterator() {
//				return _all(filter, asc, projection, duplicateKeys) ;
//			}
//		} ;
//	}
//	
//	protected final <F> Iterable<F> iterable(final boolean asc, final Function<N, F> projection) {
//		return new Iterable<F>() {
//			@Override
//			public Iterator<F> iterator() {
//				return _all(asc, projection) ;
//			}
//		} ;
//	}
//	
//	@Override
//	public Iterator<V> iterator() {
//		return _all(true, AbstractTreapSet.<V, N>valueProjection()) ;
//	}
//	
//	@Override
//	public int count(V value) {
//		return _count(createComparable(value), duplicateKeys) ;
//	}
//
//	@Override
//	public int count(FilterTotalOrder<? super V> filter) {
//		return _count(new FilterWrapper<V, N>(filter), duplicateKeys) ;
//	}
//	@Override
//	public Boolean apply(V v) {
//		return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
//	}
//	
//}
