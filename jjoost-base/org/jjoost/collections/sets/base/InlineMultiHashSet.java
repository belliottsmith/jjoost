package org.jjoost.collections.sets.base;

import java.util.Iterator;

import org.jjoost.collections.MultiSet;

import org.jjoost.collections.base.HashNode ;
import org.jjoost.collections.base.HashNodeEquality ;
import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.collections.iters.AbstractIterable ;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class InlineMultiHashSet<V, N extends HashNode<N> & Value<V>> extends AbstractHashSet<V, N> implements MultiSet<V> {

	private static final long serialVersionUID = -6385620376018172675L;

	public InlineMultiHashSet(Hasher<? super V> valHasher, Rehasher rehasher, Equality<? super V> equality, HashNodeFactory<V, N> nodeFactory, HashStore<N> table) {
		super(valHasher, rehasher, nodeFactory, new ValueEquality<V>(equality), table) ;
		this.putEq = new PutEquality<V>(equality) ;
	}
	
	private InlineMultiHashSet(Hasher<? super V> valHasher, Rehasher rehasher, HashNodeFactory<V, N> nodeFactory, AbstractHashSet.ValueEquality<V> equality, PutEquality<V> putEq, HashStore<N> table) {
		super(valHasher, rehasher, nodeFactory, equality, table) ;
		this.putEq = putEq ;
	}
	
	private final PutEquality<V> putEq ;
	
	@Override
	public V put(V val) {
		return store.put(val, nodeFactory.makeNode(hash(val), val), putEq, valProj()) ;
	}
	
	@Override
	public MultiSet<V> copy() {
		return new InlineMultiHashSet<V, N>(valHasher, rehasher, nodeFactory, valEq, putEq, store.copy()) ;
	}

	@Override
	public boolean permitsDuplicates() {
		return true ;
	}

	@Override
	public Iterable<V> unique() {
		return new AbstractIterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return store.unique(valProj(), valEq.getValueEquality(), valProj(), valEq, valProj()) ;
			}
		} ;
	}

	private static final class ValueEquality<V> extends AbstractHashSet.ValueEquality<V> {
		public ValueEquality(Equality<? super V> valEq) {
			super(valEq) ;
		}
		@Override
		public boolean isUnique() {
			return false ;
		}
	}

	private static final class PutEquality<V> implements HashNodeEquality<V, Value<V>> {
		final Equality<? super V> valEq ;
		public PutEquality(Equality<? super V> valEq) { this.valEq = valEq ; }
		@Override 
		public boolean suffixMatch(V n1, Value<V> n2) { 
			return false ;
		}
		@Override 
		public boolean prefixMatch(V v, Value<V> v2) { 
			return valEq.equates(v, v2.getValue()) ; 
		}
		@Override
		public boolean isUnique() {
			return false ;
		}
	}
	
}
