package org.jjoost.collections.sets.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.base.HashNodeEquality ;
import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.collections.base.HashStore.HashNode ;
import org.jjoost.collections.iters.AbstractIterable ;
import org.jjoost.collections.iters.RepeatIterable ;
import org.jjoost.collections.iters.RepeatIterator ;
import org.jjoost.util.Counter;
import org.jjoost.util.Equality;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Hasher;
import org.jjoost.util.Iters ;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class CountingMultiHashSet<V, N extends HashNode<N> & CountingMultiHashSet.INode<V, N>> implements MultiSet<V> {

	private static final long serialVersionUID = 3187373892419456381L;
	
	protected final HashStore<N> table ;
	protected final Hasher<? super V> valHasher ;
	protected final Rehasher rehasher ;
	protected final HashNodeFactory<V, N> nodeFactory ;
	protected final ValueEquality<V, N> valEq ;
	protected final Counter totalCount ;
	
//	protected static final <V, N extends INode<V, N>> boolean removeExistingNode(N node, CountingListHashSet<V, N> set) {
//		return set.table.removeExistingNode(set.valProj(), set.valEq, node) ;
//	}
//	
	protected Function<N, N> identity() {
		return Functions.<N>identity() ;
	}
	
	protected Function<Value<V>, V> valProj() {
		return Functions.<V>getAbstractValueContentsProjection() ;
	}
	
	protected CountingMultiHashSet(Counter counter, Hasher<? super V> valHasher, Rehasher rehasher, HashNodeFactory<V, N> nodeFactory, ValueEquality<V, N> equality, HashStore<N> table) {
		this.table = table ;
		this.totalCount = counter ;
		this.valHasher = valHasher ;
		this.rehasher = rehasher ;
		this.nodeFactory = nodeFactory ;
		this.valEq = equality ;
	}

	final int hash(V key) {
		return rehasher.hash(valHasher.hash(key)) ;
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
			if (existing.add(1))
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
			if (existing.valid())
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
			final int removed = r.destroy() ;
			if (removed >= 0) {
				if (removed != 0)
					totalCount.add(-removed) ;
				return r.getValue() ;
			}
		}
	}

	@Override
	public Iterable<V> removeAndReturn(V val) {
		final int hash = hash(val) ;
		final List<Iterable<V>> iters = new ArrayList<Iterable<V>>() ;
		for (N n : table.removeAndReturn(hash, val, valEq, identity())) {
			final int r = n.destroy() ;
			if (r >= 0) {
				if (r != 0)
					totalCount.add(-r) ;
				iters.add(new RepeatIterable<V>(n.getValue(), r)) ;
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
			final int c = r.get() ;
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

	@Override
	public Iterable<V> all() {
		return new AbstractIterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return table.all(valProj(), valEq, valProj());
			}
		} ;
	}
	
	@Override
	public Iterable<V> all(final V val) {
		final int hash = hash(val) ;
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return table.find(hash, val, valEq, valProj(), valProj()) ;
			}
		} ;
	}

	@Override
	public MultiSet<V> copy() {
		// no way to guarantee totalCount is valid - must copy the table and then calculate a fresh total
		// TODO
		throw new UnsupportedOperationException() ;
	}

	@Override
	public Iterable<V> unique() {
		return new AbstractIterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return table.unique(valProj(), valEq, valEq.getEquality(), valProj()) ;
			}
		} ;
	}

	@Override
	public int clear() {
		return table.clear() ;
	}

	@Override
	public Iterator<V> clearAndReturn() {
		return Iters.concat(table.clearAndReturn(destroyingKeyRepeater())) ;
	}

	@Override
	public Iterator<V> iterator() {
		final KeyRepeater<V, N> repeater = new KeyRepeater<V, N>() ;
		final Iterator<Iterator<V>> iters = table.all(valProj(), valEq, repeater) ;
		repeater.superIter = iters ;
		return Iters.concat(iters) ;
	}

	@Override
	public Boolean apply(V v) {
		return contains(v) ;
	}
	
	// **********************************
	// NODE INTERFACE
	// **********************************
	
	protected static interface INode<V, N extends INode<V, N>> extends Counter, Value<V> {
		public boolean valid() ;
		/**
		 * disables node for further use (in parallel environments) and returns the guaranteed to be final count this node contained
		 * @return
		 */		
		public int destroy() ;
		public Iterator<V> iter(Iterator<Iterator<V>> nodeIter) ;
	}
	
	protected static abstract class ValueEquality<V, N extends INode<V, N>> implements HashNodeEquality<V, N> {
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
	}
	
	// **********************************
	// UTILITY CLASSES
	// **********************************
	
	private static final class KeyRepeater<V, N extends INode<V, N>> implements Function<N, Iterator<V>> {
		private static final long serialVersionUID = -965724235732791909L;
		private Iterator<Iterator<V>> superIter ;
		@Override
		public Iterator<V> apply(N n) {
			return n.iter(superIter) ;
		}
	}

	@SuppressWarnings("unchecked")
	private static final DestroyingKeyRepeater DESTROYING_KEY_REPEATER = new DestroyingKeyRepeater() ;
	@SuppressWarnings("unchecked")
	private final DestroyingKeyRepeater<V, N> destroyingKeyRepeater() {
		return DESTROYING_KEY_REPEATER ;
	}
	private static final class DestroyingKeyRepeater<V, N extends INode<V, N>> implements Function<N, Iterator<V>> {
		private static final long serialVersionUID = -965724235732791909L;
		@Override
		public Iterator<V> apply(N n) {
			final int c = n.destroy() ;
			return new RepeatIterator<V>(n.getValue(), c > 0 ? c : 0) ;
		}
	}

}
