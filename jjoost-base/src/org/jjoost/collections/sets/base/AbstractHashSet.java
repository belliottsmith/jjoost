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
import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.base.HashNode;
import org.jjoost.collections.base.HashNodeEquality;
import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.HashStore;
import org.jjoost.collections.base.HashStore.PutAction;
//import org.jjoost.collections.base.LockFreeHashStore.LockFreeHashNode;
//import org.jjoost.collections.base.LockFreeLinkedHashStore.LockFreeLinkedHashNode;
import org.jjoost.collections.base.AbstractSerialHashStore.SerialHashNode;
import org.jjoost.collections.base.SerialLinkedHashStore.SerialLinkedHashNode;
import org.jjoost.util.Equality;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Objects;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

@SuppressWarnings("deprecation")
public abstract class AbstractHashSet<V, N extends HashNode<N> & Value<V>, S extends HashStore<N, S>> extends AbstractSet<V> implements AnySet<V> {

	private static final long serialVersionUID = 3187373892419456381L;
	
	protected final S store;
	protected final Rehasher rehasher;
	protected final HashNodeFactory<V, N> nodeFactory;
	protected final ValueEquality<V> valEq;
	
	protected AbstractHashSet(Rehasher rehasher, ValueEquality<V> equality, HashNodeFactory<V, N> nodeFactory, S table) {
		this.store = table;
		this.rehasher = rehasher;
		this.valEq = equality;
		this.nodeFactory = nodeFactory;
	}
	
	public int capacity() {
		return store.capacity();
	}
	
	protected final int hash(V key) {
		return rehasher.rehash(valEq.valEq.hash(key));
	}
	
	protected final Function<Value<V>, V> valProj() {
		return Functions.<V>getValueContentsProjection();
	}
	
	protected final Function<N, N> nodeProj() {
		return Functions.<N>identity();
	}
	
	@Override
	public V putIfAbsent(V val) {
		return store.put(PutAction.IFABSENT, hash(val), val, valEq, nodeFactory, valProj());
	}
	
	@Override
	public int remove(V val) {
		return store.remove(hash(val), Integer.MAX_VALUE, val, valEq);
	}
	@Override
	public Iterable<V> removeAndReturn(V val) {
		return store.removeAndReturn(hash(val), Integer.MAX_VALUE, val, valEq, valProj());
	}
	
	@Override
	public int remove(V val, int atMost) {
		return store.remove(hash(val), atMost, val, valEq);
	}
	@Override
	public Iterable<V> removeAndReturn(V val, int atMost) {
		return store.removeAndReturn(hash(val), atMost, val, valEq, valProj());
	}
	
	@Override
	public Iterator<V> clearAndReturn() {
		return store.clearAndReturn(Functions.<V>getValueContentsProjection());
	}

	@Override
	public int putAll(Iterable<V> vals) {
		int c = 0;
		for (V val : vals) {
			if (store.put(PutAction.PUT, val, nodeFactory.makeNode(hash(val), val), valEq, nodeProj()) == null)
				c++;
		}
		return c;
	}

	@Override
	public V removeAndReturnFirst(V value) {
		return store.removeAndReturnFirst(hash(value), Integer.MAX_VALUE, value, valEq, valProj());
	}

	@Override
	public V removeAndReturnFirst(V value, int atMost) {
		return store.removeAndReturnFirst(hash(value), atMost, value, valEq, valProj());
	}
	
	@Override
	public boolean contains(V value) {
		return store.count(hash(value), value, valEq, 1) > 0;
	}
	@Override
	public int count(V value) {
		return store.count(hash(value), value, valEq, Integer.MAX_VALUE);
	}
	@Override
	public V first(V value) {
		return store.first(hash(value), value, valEq, valProj());
	}
	@Override
	public List<V> list(V val) {
		return store.findNow(hash(val), val, valEq, valProj());
	}
	@Override
	public int totalCount() {
		return store.totalCount();
	}
	@Override
	public boolean isEmpty() {
		return store.isEmpty();
	}
	@Override
	public Iterable<V> all(final V val) {
		final int hash = hash(val);
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return store.find(hash, val, valEq, valProj(), valEq, valProj());
			}
		};
	}

	@Override
	public int clear() {
		return store.clear();
	}

	@Override
	public Iterator<V> iterator() {
		return store.all(valProj(), valEq, valProj());
	}

	@Override
	public Boolean apply(V v) {
		return contains(v);
	}
	
	@Override
	public Equality<? super V> equality() {
		return valEq.valEq;
	}
	
	public String toString() {
		return store.toString();
	}
	
 	// **********************************
	// EQUALITY
	// **********************************
	
	protected static abstract class ValueEquality<V> implements HashNodeEquality<V, Value<V>> {
		protected final Equality<? super V> valEq;
		public ValueEquality(Equality<? super V> valEq) { this.valEq = valEq ; }
		@Override 
		public boolean suffixMatch(V n1, Value<V> n2) { 
			return true;
		}
		@Override 
		public boolean prefixMatch(V v, Value<V> v2) { 
			return valEq.equates(v, v2.getValue());
		}
		public Equality<? super V> getValueEquality() {
			return valEq;
		}		
	}
	
	// **********************************
	// NODE IMPLEMENTATIONS
	// **********************************
	
	public static final class SerialHashSetNode<V> extends SerialHashNode<SerialHashSetNode<V>> implements Value<V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public SerialHashSetNode(int hash, V value) {
			super(hash);
			this.value = value;
		}
		private V value;
		@Override public V getValue() { return value ; }
		@Override public SerialHashSetNode<V> copy() { return new SerialHashSetNode<V>(hash, value) ; }
		@Override public String toString() { return Objects.toString(value) ; }
	}
	
	private static final SerialHashSetNodeFactory<?> SERIAL_FACTORY = new SerialHashSetNodeFactory<Object>();
	@SuppressWarnings("unchecked")
	public static <V> SerialHashSetNodeFactory<V> serialNodeFactory() {
		return (SerialHashSetNodeFactory<V>) SERIAL_FACTORY;
	}
	public static final class SerialHashSetNodeFactory<V> implements HashNodeFactory<V, SerialHashSetNode<V>> {
		private static final long serialVersionUID = -4613572380650059469L;
		@Override
		public final SerialHashSetNode<V> makeNode(final int hash, final V value) {
			return new SerialHashSetNode<V>(hash, value);
		}
	}
	
	public static final class SerialLinkedHashSetNode<V> extends SerialLinkedHashNode<SerialLinkedHashSetNode<V>> implements Value<V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public SerialLinkedHashSetNode(int hash, V value) {
			super(hash);
			this.value = value;
		}
		private V value;
		@Override public V getValue() { return value ; }
		@Override public SerialLinkedHashSetNode<V> copy() { return new SerialLinkedHashSetNode<V>(hash, value) ; }
		@Override public String toString() { return Objects.toString(value) ; }
	}
	
	@SuppressWarnings("rawtypes")
	private static final SerialLinkedHashSetNodeFactory SERIAL_LINKED_FACTORY = new SerialLinkedHashSetNodeFactory();
	@SuppressWarnings("unchecked")
	public static <V> SerialLinkedHashSetNodeFactory<V> serialLinkedNodeFactory() {
		return SERIAL_LINKED_FACTORY;
	}
	public static final class SerialLinkedHashSetNodeFactory<V> implements HashNodeFactory<V, SerialLinkedHashSetNode<V>> {
		private static final long serialVersionUID = 6689938098796653070L;
		@Override
		public final SerialLinkedHashSetNode<V> makeNode(final int hash, final V value) {
			return new SerialLinkedHashSetNode<V>(hash, value);
		}
	}
	
//	public static final class LockFreeHashSetNode<V> extends LockFreeHashNode<LockFreeHashSetNode<V>> implements Value<V> {
//		private static final long serialVersionUID = -5766263745864028747L;
//		public LockFreeHashSetNode(int hash, V value) {
//			super(hash);
//			this.value = value;
//		}
//		private V value;
//		@Override public V getValue() { return value ; }
//		@Override public LockFreeHashSetNode<V> copy() { return new LockFreeHashSetNode<V>(hash, value) ; }
//		@Override public String toString() { return Objects.toString(value) ; }
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private static final LockFreeHashSetNodeFactory LOCKFREE_FACTORY = new LockFreeHashSetNodeFactory();
//	@SuppressWarnings("unchecked")
//	public static <V> LockFreeHashSetNodeFactory<V> lockFreeNodeFactory() {
//		return LOCKFREE_FACTORY;
//	}
//	public static final class LockFreeHashSetNodeFactory<V> implements HashNodeFactory<V, LockFreeHashSetNode<V>> {
//		private static final long serialVersionUID = -1108755693852364279L;
//
//		@Override
//		public final LockFreeHashSetNode<V> makeNode(final int hash, final V value) {
//			return new LockFreeHashSetNode<V>(hash, value);
//		}
//	}
//	
//	public static final class LockFreeLinkedHashSetNode<V> extends LockFreeLinkedHashNode<LockFreeLinkedHashSetNode<V>> implements Value<V> {
//		private static final long serialVersionUID = -5766263745864028747L;
//		public LockFreeLinkedHashSetNode(int hash, V value) {
//			super(hash);
//			this.value = value;
//		}
//		private V value;
//		@Override public V getValue() { return value ; }
//		@Override public LockFreeLinkedHashSetNode<V> copy() { return new LockFreeLinkedHashSetNode<V>(hash, value) ; }
//		@Override public String toString() { return Objects.toString(value) ; }
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private static final LockFreeLinkedHashSetNodeFactory LOCKFREE_LINKED_FACTORY = new LockFreeLinkedHashSetNodeFactory();
//	@SuppressWarnings("unchecked")
//	public static <V> LockFreeLinkedHashSetNodeFactory<V> lockFreeLinkedNodeFactory() {
//		return LOCKFREE_LINKED_FACTORY;
//	}
//	public static final class LockFreeLinkedHashSetNodeFactory<V> implements HashNodeFactory<V, LockFreeLinkedHashSetNode<V>> {
//		private static final long serialVersionUID = 4736536599540508852L;
//		@Override
//		public final LockFreeLinkedHashSetNode<V> makeNode(final int hash, final V value) {
//			return new LockFreeLinkedHashSetNode<V>(hash, value);
//		}
//	}
	
}
