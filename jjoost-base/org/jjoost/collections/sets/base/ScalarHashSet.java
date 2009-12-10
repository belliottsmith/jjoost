package org.jjoost.collections.sets.base;

import org.jjoost.collections.ScalarSet;
import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.HashTable ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Hasher;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class ScalarHashSet<V, N extends Value<V>> extends AbstractHashSet<V, N> implements ScalarSet<V> {

	private static final long serialVersionUID = -6385620376018172675L;

	public ScalarHashSet(Hasher<? super V> valHasher, Rehasher rehasher, Equality<? super V> equality, HashNodeFactory<V, N> nodeFactory, HashTable<N> table) {
		super(valHasher, rehasher, nodeFactory, new ValueEquality<V>(equality), table) ;
	}
	
	private ScalarHashSet(Hasher<? super V> valHasher, Rehasher rehasher, HashNodeFactory<V, N> nodeFactory, AbstractHashSet.ValueEquality<V> equality, HashTable<N> table) {
		super(valHasher, rehasher, nodeFactory, equality, table) ;
	}
	
	@Override
	public V put(V val) {
		return table.put(val, nodeFactory.makeNode(hash(val), val), valEq, valProj()) ;
	}
	
	@Override
	public V get(V key) {
		return first(key) ;
	}

	@Override
	public ScalarSet<V> copy() {
		return new ScalarHashSet<V, N>(valHasher, rehasher, nodeFactory, valEq, table.copy()) ;
	}

	@Override
	public boolean permitsDuplicates() {
		return false ;
	}

	@Override
	public Iterable<V> unique() {
		return all() ;
	}

	@Override
	public int size() {
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
