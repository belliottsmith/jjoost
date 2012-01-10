/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.collections.sets.base;

import java.util.Iterator;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;

import org.jjoost.collections.base.HashNode;
import org.jjoost.collections.base.HashNodeEquality;
import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.HashStore;
import org.jjoost.collections.base.HashStore.Locality;
import org.jjoost.collections.base.HashStore.PutAction;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class InlineMultiHashSet<V, N extends HashNode<N> & Value<V>, S extends HashStore<N, S>> extends AbstractHashSet<V, N, S> implements MultiSet<V> {

	private static final long serialVersionUID = -6385620376018172675L;

	public InlineMultiHashSet(Rehasher rehasher, Equality<? super V> equality, HashNodeFactory<V, N> nodeFactory, S table) {
		super(rehasher, new ValueEquality<V>(equality), nodeFactory, table);
		this.putEq = new PutEquality<V>(equality);
	}
	
	private InlineMultiHashSet(Rehasher rehasher, AbstractHashSet.ValueEquality<V> equality, HashNodeFactory<V, N> nodeFactory, PutEquality<V> putEq, S table) {
		super(rehasher, equality, nodeFactory, table);
		this.putEq = putEq;
	}
	
	private final PutEquality<V> putEq;
	private UniqueSet unique;
	
	@Override
	public boolean add(V val) {
		put(val);
		return true;
	}
	
	@Override
	public V put(V val) {
		return store.put(PutAction.PUT, val, nodeFactory.makeNode(hash(val), val), putEq, valProj());
	}
	
	@Override
	public void put(V val, int count) {
		for (int i = 0 ; i != count ; i++) {
			store.put(PutAction.PUT, val, nodeFactory.makeNode(hash(val), val), putEq, valProj());
		}
	}
	
	@Override
	public MultiSet<V> copy() {
		return new InlineMultiHashSet<V, N, S>(rehasher, valEq, nodeFactory, putEq, store.copy(valProj(), valEq));
	}

	@Override
	public boolean permitsDuplicates() {
		return true;
	}
	@Override
	public int uniqueCount() {
		return store.uniquePrefixCount();
	}

	@Override
	public Set<V> unique() {
		if (unique == null)
			unique = new UniqueSet();
		return unique;
	}
	
	private final class UniqueSet extends AbstractUniqueSetAdapter<V> {
		
		private static final long serialVersionUID = -1106116714278629141L;

		@Override
		protected AnySet<V> set() {
			return InlineMultiHashSet.this;
		}

		@Override
		public Iterator<V> iterator() {
			return wrap(store.unique(valProj(), valEq.getValueEquality(), Locality.ADJACENT, valProj(), valEq, valProj()));
		}
		
	}

	private static final class ValueEquality<V> extends AbstractHashSet.ValueEquality<V> {
		public ValueEquality(Equality<? super V> valEq) {
			super(valEq);
		}
		@Override
		public boolean isUnique() {
			return false;
		}
	}

	private static final class PutEquality<V> implements HashNodeEquality<V, Value<V>> {
		final Equality<? super V> valEq;
		public PutEquality(Equality<? super V> valEq) { this.valEq = valEq ; }
		@Override 
		public boolean suffixMatch(V n1, Value<V> n2) { 
			return false;
		}
		@Override 
		public boolean prefixMatch(V v, Value<V> v2) { 
			return valEq.equates(v, v2.getValue());
		}
		@Override
		public boolean isUnique() {
			return false;
		}
	}

	@Override
	public Equality<? super V> equality() {
		return valEq.valEq;
	}
	
}
