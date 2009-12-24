package org.jjoost.collections.sets.base;

import org.jjoost.collections.Set;
import org.jjoost.collections.base.HashNode ;
import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Hasher;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class ScalarHashSet<V, N extends HashNode<N> & Value<V>> extends AbstractHashSet<V, N> implements Set<V> {

	private static final long serialVersionUID = -6385620376018172675L;

	public ScalarHashSet(Hasher<? super V> valHasher, Rehasher rehasher, Equality<? super V> equality, HashNodeFactory<V, N> nodeFactory, HashStore<N> table) {
		super(valHasher, rehasher, new ValueEquality<V>(equality), nodeFactory, table) ;
	}
	
	private ScalarHashSet(Hasher<? super V> valHasher, Rehasher rehasher, AbstractHashSet.ValueEquality<V> equality, HashNodeFactory<V, N> nodeFactory, HashStore<N> table) {
		super(valHasher, rehasher, equality, nodeFactory, table) ;
	}
	
	@Override
	public V put(V val) {
		return store.put(val, nodeFactory.makeNode(hash(val), val), valEq, valProj()) ;
	}
	
	@Override
	public V get(V key) {
		return first(key) ;
	}

	@Override
	public Set<V> copy() {
		return new ScalarHashSet<V, N>(valHasher, rehasher, valEq, nodeFactory, store.copy(valProj(), valEq)) ;
	}

	@Override
	public boolean permitsDuplicates() {
		return false ;
	}

	@Override
	public Iterable<V> unique() {
		return this ;
	}

	@Override
	public int size() {
		return totalCount() ;
	}

	@Override
	public int uniqueCount() {
		return totalCount() ;
	}
	
	private static final class ValueEquality<V> extends AbstractHashSet.ValueEquality<V> {
		public ValueEquality(Equality<? super V> valEq) {
			super(valEq) ;
		}
		@Override
		public boolean isUnique() {
			return true ;
		}
	}
	
}
