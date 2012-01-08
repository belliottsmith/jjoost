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

import org.jjoost.collections.Set;
import org.jjoost.collections.base.HashNode;
import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.HashStore;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class HashSet<V, N extends HashNode<N> & Value<V>> extends AbstractHashSet<V, N> implements Set<V> {

	private static final long serialVersionUID = -6385620376018172675L;

	public HashSet(Rehasher rehasher, Equality<? super V> equality, HashNodeFactory<V, N> nodeFactory, HashStore<N> table) {
		super(rehasher, new ValueEquality<V>(equality), nodeFactory, table);
	}
	
	private HashSet(Rehasher rehasher, AbstractHashSet.ValueEquality<V> equality, HashNodeFactory<V, N> nodeFactory, HashStore<N> table) {
		super(rehasher, equality, nodeFactory, table);
	}
	
	@Override
	public boolean add(V val) {
		return store.putIfAbsent(val, nodeFactory.makeNode(hash(val), val), valEq, nodeProj()) == null;
	}
	
	@Override
	public V put(V val) {
		return store.put(false, val, nodeFactory.makeNode(hash(val), val), valEq, valProj());
	}
	
	@Override
	public V get(V key) {
		return first(key);
	}

	@Override
	public HashSet<V, N> copy() {
		return new HashSet<V, N>(rehasher, valEq, nodeFactory, store.copy(valProj(), valEq));
	}

	@Override
	public boolean permitsDuplicates() {
		return false;
	}

	@Override
	public Set<V> unique() {
		return this;
	}

	@Override
	public int size() {
		return totalCount();
	}

	@Override
	public int uniqueCount() {
		return totalCount();
	}
	
	private static final class ValueEquality<V> extends AbstractHashSet.ValueEquality<V> {
		public ValueEquality(Equality<? super V> valEq) {
			super(valEq);
		}
		@Override
		public boolean isUnique() {
			return true;
		}
	}
	
}
