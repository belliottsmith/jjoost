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

package org.jjoost.collections.sets.serial;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jjoost.collections.base.AbstractSerialHashStore.SerialHashNode;
import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.collections.sets.base.NestedMultiHashSet;
import org.jjoost.util.Counters;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialCountingMultiHashSet<V> extends NestedMultiHashSet<V, SerialCountingMultiHashSet.Node<V>> {

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialCountingMultiHashSet() {
		this(16, 0.75f);
	}
	public SerialCountingMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object());
	}
	
	public SerialCountingMultiHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality);
	}
	
	public SerialCountingMultiHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality);
	}
	
	public SerialCountingMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(Counters.newCounter(), rehasher, 
			new NestedMultiHashSet.ValueEquality<V, SerialCountingMultiHashSet.Node<V>>(keyEquality), 
			SerialCountingMultiHashSet.<V>serialNodeFactory(), 
			new SerialHashStore<Node<V>>(minimumInitialCapacity, loadFactor));
	}

	// this implementation has no concurrency guarantees
	public static final class Node<V> extends SerialHashNode<Node<V>> implements NestedMultiHashSet.INode<V, Node<V>> {
		
		private static final long serialVersionUID = -5766263745864028747L;
		
		public Node(int hash, V value, int count) {
			super(hash);
			this.value = value;
			this.count = count;
		}
		
		private final V value;
		private int count;
		
		@Override public V getValue() { 
			return value;
		}
		
		@Override public Node<V> copy() { 
			return new Node<V>(hash, value, count);
		}
		
		@Override public int count() {
			return count;
		}
		
		@Override public int remove(int i) {
			final int newc = count - i;
			if (newc <= 0) {
				final int oldc = count;
				count = -1;
				return oldc;
			}
			count = newc;
			return i;
		}
		
		@Override
		public List<V> removeAndReturn(int target) {
			return new UniformList<V>(value, remove(target));
		}

		@Override public boolean put(V val) {
			if (count < 0 || count == Integer.MAX_VALUE)
				return false;
			count += 1;
			return true;
		}
		
		@Override public boolean put(V val, int c) {
			if (count < 0)
				return false;
			count = Math.min(count + c, Integer.MAX_VALUE);
			return true;
		}
		
		@Override 
		public boolean valid() { 
			return count > 0;
		}
		
		@Override
		public boolean initialise() {
			if (count != 0)
				return false;
			count = 1;
			return true;
		}
		
		@Override
		public Iterator<V> iterator(final NestedMultiHashSet<V, Node<V>> arg) {
			final SerialCountingMultiHashSet<V> set = (SerialCountingMultiHashSet<V>) arg;
			return new Iterator<V>() {
				int c = 0;
				boolean last = false;
				boolean next = false;
				@Override
				public boolean hasNext() {
					return next = (count > c);
				}
				@Override
				public V next() {
					if (!next)
						throw new NoSuchElementException();
					c++;
					last = true;
					return value;
				}
				@Override
				public void remove() {
					if (!last)
						throw new NoSuchElementException();
					count -= 1;
					c -= 1;
					set.totalCount.add(-1);
					if (count <= 0) {
						count = -1;
						set.removeNode(Node.this);
					}
				}
			};
		}

	}
	
	@SuppressWarnings("rawtypes")
	private static final SerialCountingListHashSetNodeFactory SERIAL_SCALAR_HASH_NODE_FACTORY = new SerialCountingListHashSetNodeFactory();
	@SuppressWarnings("unchecked")
	public static <V> SerialCountingListHashSetNodeFactory<V> serialNodeFactory() {
		return SERIAL_SCALAR_HASH_NODE_FACTORY;
	}
	public static final class SerialCountingListHashSetNodeFactory<V> implements HashNodeFactory<V, Node<V>> {
		private static final long serialVersionUID = -3308673571792344499L;
		@Override
		public final Node<V> makeNode(final int hash, final V value) {
			return new Node<V>(hash, value, 0);
		}
	}

}
