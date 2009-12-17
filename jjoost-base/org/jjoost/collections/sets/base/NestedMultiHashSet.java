package org.jjoost.collections.sets.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.base.HashNode ;
import org.jjoost.collections.base.HashNodeEquality ;
import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.collections.iters.EmptyIterator ;
import org.jjoost.util.Counter;
import org.jjoost.util.Equality;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Hasher;
import org.jjoost.util.Iters ;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class NestedMultiHashSet<V, N extends HashNode<N> & NestedMultiHashSet.INode<V, N>> implements MultiSet<V> {

	private static final long serialVersionUID = 3187373892419456381L;
	
	protected final HashStore<N> table ;
	protected final Hasher<? super V> valHasher ;
	protected final Rehasher rehasher ;
	protected final HashNodeFactory<V, N> nodeFactory ;
	protected final ValueEquality<V, N> valEq ;
	protected final Counter totalCount ;
	
	protected NestedMultiHashSet(Counter counter, Hasher<? super V> valHasher, Rehasher rehasher, ValueEquality<V, N> equality, HashNodeFactory<V, N> nodeFactory, HashStore<N> table) {
		this.table = table ;
		this.totalCount = counter ;
		this.valHasher = valHasher ;
		this.rehasher = rehasher ;
		this.nodeFactory = nodeFactory ;
		this.valEq = equality ;
	}

	protected Function<N, N> identity() {
		return Functions.<N>identity() ;
	}
	
	protected Function<Value<V>, V> valProj() {
		return Functions.<V>getAbstractValueContentsProjection() ;
	}
	
	final int hash(V key) {
		return rehasher.hash(valHasher.hash(key)) ;
	}
	
	protected static <V, N extends HashNode<N> & INode<V, N>> boolean removeNode(NestedMultiHashSet<V, N> set, N node) {
		return set.table.removeNode(set.valProj(), set.valEq, node) ;
	}
	
	@Override
	public boolean permitsDuplicates() {
		return true ;
	}

	@Override
	public V put(V val) {
		final int hash = hash(val) ;
		while (true) {
			final N existing = table.ensureAndGet(hash, val, valEq, nodeFactory, identity()) ;
			if (existing.put(val))
				break ;
		}
		totalCount.add(1) ;
		return null ;
	}
	
	@Override
	public int putAll(Iterable<V> vals) {
		int c = 0 ;
		for (V val : vals)
			if (put(val) == null)
				c++ ;
		return c ;
	}
	
	@Override
	public V putIfAbsent(V val) {
		final int hash = hash(val) ;
		while (true) {
			final N existing = table.putIfAbsent(hash, val, valEq, nodeFactory, identity()) ;
			if (existing == null) {
				totalCount.add(1) ;
				return null ;
			}
			return existing.getValue() ;
		}
	}
	
	@Override
	public int remove(V val) {
		final int hash = hash(val) ;
		while (true) {
			final N r = table.removeAndReturnFirst(hash, val, valEq, identity()) ;
			if (r == null)
				return 0 ;
			final int removed = r.destroy() ;
			if (removed >= 0) {
				if (removed != 0)
					totalCount.add(-removed) ;
				return removed ;
			}
		}		
	}
	
	@Override
	public V removeAndReturnFirst(V val) {
		final int hash = hash(val) ;
		while (true) {
			final N r = table.removeAndReturnFirst(hash, val, valEq, identity()) ;
			if (r == null)
				return null ;
			final V v = r.getValue() ; 
			final int removed = r.destroy() ;
			if (removed >= 0) {
				if (removed != 0)
					totalCount.add(-removed) ;
				return v ;
			}
		}
	}

	@Override
	public Iterable<V> removeAndReturn(V val) {
		final int hash = hash(val) ;
		final List<Iterable<V>> iters = new ArrayList<Iterable<V>>() ;
		for (INode<V, N> n : table.removeAndReturn(hash, val, valEq, identity())) {
			final List<V> r = n.destroyAndReturn() ;
			final int c = r.size() ;
			if (c >= 0) {
				if (c != 0)
					totalCount.add(-c) ;
				iters.add(r) ;
			}
		}
		switch (iters.size()) {
		case 0:
			return java.util.Collections.emptyList() ;
		case 1:
			return iters.get(0) ;
		default:
			return Iters.concat(iters) ;
		}
	}
	@Override
	public boolean contains(V val) {
		final int hash = hash(val) ;
		while (true) {
			final N r = table.first(hash, val, valEq, identity()) ;
			if (r == null)
				return false ;
			if (r.valid())
				return true ;
		}		
	}
	@Override
	public int count(V val) {
		final int hash = hash(val) ;
		while (true) {
			final N r = table.first(hash, val, valEq, identity()) ;
			if (r == null)
				return 0 ;
			final int c = r.count() ;
			if (c >= 0)
				return c ;
		}		
	}
	@Override
	public void shrink() {
		table.shrink() ;
	}
	@Override
	public V first(V val) {
		return table.first(hash(val), val, valEq, valProj()) ;
	}
	@Override
	public List<V> list(V val) {
		return Iters.toList(all(val)) ;
	}
	@Override
	public int totalCount() {
		return totalCount.get() ;
	}
	@Override
	public int uniqueCount() {
		return table.totalCount() ;
	}
	@Override
	public boolean isEmpty() {
		return table.isEmpty() ;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Iterable<V> all() {
		return Functions.apply(Functions.<V>getAbstractValueContentsProjection(), (Iterable<Value<V>>) table);
	}
	@Override
	public Iterable<V> all(final V val) {
		final int hash = hash(val) ;
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				final N n = table.first(hash, val, valEq, identity()) ;
				if (n == null)
					return EmptyIterator.<V>get() ;
				return n.iterator(NestedMultiHashSet.this) ;
			}
		} ;
	}

	@Override
	public MultiSet<V> copy() {
		// TODO implement
		// no way to guarantee totalCount is valid - must copy the table and then calculate a fresh total
		throw new UnsupportedOperationException() ;
	}

	@Override
	public Iterable<V> unique() {
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				final NodeContentsIterator<V, N> f = new NodeContentsIterator<V, N>() ;
				final Iterator<Iterator<V>> iters = table.unique(valProj(), valEq.getEquality(), valProj(), valEq, f) ;
				f.superIter = iters ;
				return Iters.concat(iters) ;
			}
		} ;
	}

	@Override
	public int clear() {
		return table.clear() ;
	}

	@Override
	public Iterator<V> clearAndReturn() {
		return Iters.concat(table.clearAndReturn(NestedMultiHashSet.<V, N>destroyer())) ;
	}

	@Override
	public Iterator<V> iterator() {
		final NodeContentsIterator<V, N> f = new NodeContentsIterator<V, N>() ;
		final Iterator<Iterator<V>> iters = table.all(valProj(), valEq, f) ;
		f.superIter = iters ;
		return Iters.concat(iters) ;
	}

	@Override
	public Boolean apply(V v) {
		return contains(v) ;
	}
	
	// **********************************
	// INTERFACES
	// **********************************
	
	protected static interface INode<V, N extends HashNode<N> & INode<V, N>> extends Value<V> {
		public boolean put(V v) ;
		public boolean valid() ;
		public int count() ;
		public int destroy() ;
		public List<V> destroyAndReturn() ;
		public Iterator<V> iterator(Iterator<Iterator<V>> superIter) ;		
		public Iterator<V> iterator(NestedMultiHashSet<V, N> set) ;		
	}

	protected static final class ValueEquality<V, N extends HashNode<N> & INode<V, N>> implements HashNodeEquality<V, N> {
		protected final Equality<? super V> valEq ;
		public ValueEquality(Equality<? super V> valEq) {
			this.valEq = valEq ;
		}
		public final Equality<? super V> getEquality() {
			return valEq ;
		}
		public final boolean isUnique() {
			return true ;
		}
		@Override
		public boolean prefixMatch(V cmp, N n) {
			return valEq.equates(cmp, n.getValue()) ;
		}
		@Override
		public boolean suffixMatch(V cmp, N n) {
			return true ;
		}
	}
	
//	protected static final class NodeEquality<V, N extends INode<V, N>> implements HashNodeEquality<N, N>, Equality<N> {
//		private static final long serialVersionUID = 3918906145341080830L ;
//		protected final Equality<? super V> valEq ;
//		public NodeEquality(Equality<? super V> valEq) {
//			this.valEq = valEq ;
//		}
//		public final boolean isUnique() {
//			return true ;
//		}
//		@Override
//		public boolean prefixMatch(N n1, N n2) {
//			return valEq.equates(n1.getValue(), n2.getValue()) ;
//		}
//		@Override
//		public boolean suffixMatch(N cmp, N n) {
//			return true ;
//		}
//		@Override
//		public boolean equates(N a, N b) {
//			return prefixMatch(a, b) ;
//		}
//	}
//	
	// *******************************
	// UTILITY CLASSES
	// *******************************
	
	private static final <V, N extends HashNode<N> & INode<V, N>> Destroyer<V, N> destroyer() {
		return new Destroyer<V, N>() ;
	}
	private static final class Destroyer<V, N extends HashNode<N> & INode<V, N>> implements Function<N, Iterator<V>> {
		private static final long serialVersionUID = -965724235732791909L;
		@Override
		public Iterator<V> apply(N n) {
			return n.destroyAndReturn().iterator() ;
		}
	}

	private static final class NodeContentsIterator<V, N extends HashNode<N> & INode<V, N>> implements Function<N, Iterator<V>> {
		private static final long serialVersionUID = -965724235732791909L;
		Iterator<Iterator<V>> superIter ;
		@Override
		public Iterator<V> apply(N n) {
			return n.iterator(superIter) ;
		}
	}

}