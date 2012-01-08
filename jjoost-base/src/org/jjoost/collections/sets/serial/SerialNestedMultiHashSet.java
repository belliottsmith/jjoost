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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SerialHashStore.SerialHashNode;
import org.jjoost.collections.sets.base.NestedMultiHashSet;
import org.jjoost.util.Counters;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialNestedMultiHashSet<V> extends NestedMultiHashSet<V, SerialNestedMultiHashSet.Node<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialNestedMultiHashSet() {
		this(16, 0.75f);
	}
	public SerialNestedMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object());
	}
	
	public SerialNestedMultiHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality);
	}
	
	public SerialNestedMultiHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality);
	}
	
	public SerialNestedMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(Counters.newCounter(), rehasher, 
			new ValueEquality<V, Node<V>>(keyEquality), 
			SerialNestedMultiHashSet.<V>factory(), 
			new SerialHashStore<Node<V>>(minimumInitialCapacity, loadFactor));
	}

	// this implementation makes absolutely no concurrency guarantees
	@SuppressWarnings("unchecked")
	public static final class Node<V> extends SerialHashNode<Node<V>> implements NestedMultiHashSet.INode<V, Node<V>> {
		private static final long serialVersionUID = -5766263745864028747L;

		private V[] values = (V[]) new Object[4];
		private int count = 0;
		
		protected Node(int hash, V value) {
			super(hash);
			values[0] = value;
		}
		protected Node(int hash, V[] values, int count) {
			super(hash);
			this.values = values;
			this.count = count;
		}
		@Override public Node<V> copy() { 
			return new Node<V>(hash, values.clone(), count);
		}
		@Override
		public int count() {
			return count;
		}
		@Override
		public boolean put(V value) {
			if (count <= 0)
				return false;
			if (count == values.length)
				values = Arrays.copyOf(values, values.length << 1);
			values[count++] = value;
			return true;
		}
		@Override 
		public boolean valid() { 
			return count > 0;
		}
		@Override
		public boolean put(V v, int c) {
			if (count <= 0)
				return false;
			while (count + c >= values.length)
				values = Arrays.copyOf(values, values.length << 1);
			for (int i = 0 ; i != c ; i++)
				values[count+i] = v;
			count += c;
			return true;
		}
		
		@Override public int remove(int target) {
			final int newc = count - target;
			if (newc <= 0) {
				final int oldc = count;
				count = 0;
				return oldc;
			}
			count = newc;
			return target;
		}
		
		@Override
		public List<V> removeAndReturn(int target) {
			final int oldc = count;
			final int newc = count - target;
			if (newc <= 0) {
				count = 0;
				return Arrays.asList(Arrays.copyOf(values, oldc));
			}
			count = newc;
			return Arrays.asList(Arrays.copyOfRange(values, oldc, newc));
		}
		
		@Override
		public V getValue() {
			return values[0];
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
			final SerialNestedMultiHashSet<V> set = (SerialNestedMultiHashSet<V>) arg;
			return new Iterator<V>() {
				int last = -1;
				int next = 0;
				@Override
				public boolean hasNext() {
					return next < count;
				}
				@Override
				public V next() {
					if (next >= count)
						throw new NoSuchElementException();
					last = next;
					next += 1;
					return values[last];
				}
				@Override
				public void remove() {
					if (last == -1)
						throw new NoSuchElementException();
					if (count < 2) {
						if (count == 1) {
							set.totalCount.add(-1);
							count = -1;
							set.removeNode(Node.this);
						}
					} else {
						final int mi = count - 1;
						for (int i = last ; i < mi ; i++)
							values[i] = values[i+1];
						last = -1;
						count--;
						set.totalCount.add(-1);
					}
				}				
			};
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private static final NodeFactory NODE_FACTORY = new NodeFactory();
	@SuppressWarnings("unchecked")
	private static <V> NodeFactory<V> factory() {
		return NODE_FACTORY;
	}
	public static final class NodeFactory<V> implements HashNodeFactory<V, Node<V>> {
		private static final long serialVersionUID = -1182339303025241191L;
		@Override
		public final Node<V> makeNode(final int hash, final V value) {
			return new Node<V>(hash, value);
		}
	}

}
