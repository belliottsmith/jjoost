///**
// * Copyright (c) 2010 Benedict Elliott Smith
// * 
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// * 
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// * 
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// */
//
//package org.jjoost.collections.sets.concurrent;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.NoSuchElementException;
//
//import org.jjoost.collections.base.HashNodeFactory;
//import org.jjoost.collections.base.LockFreeLinkedHashStore;
//import org.jjoost.collections.base.SerialHashStore;
//import org.jjoost.collections.base.LockFreeHashStore.Counting;
//import org.jjoost.collections.base.LockFreeLinkedHashStore.LockFreeLinkedHashNode;
//import org.jjoost.collections.lists.UniformList;
//import org.jjoost.collections.sets.base.NestedMultiHashSet;
//import org.jjoost.util.Counters;
//import org.jjoost.util.Equalities;
//import org.jjoost.util.Equality;
//import org.jjoost.util.Rehasher;
//
//public class LockFreeLinkedCountingMultiHashSet<V> extends NestedMultiHashSet<V, LockFreeLinkedCountingMultiHashSet.Node<V>>{
//
//	private static final long serialVersionUID = 1051610520557989640L;
//
//	public LockFreeLinkedCountingMultiHashSet() {
//		this(16, 0.75f);
//	}
//	public LockFreeLinkedCountingMultiHashSet(int minimumInitialCapacity, float loadFactor) {
//		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object());
//	}
//	
//	public LockFreeLinkedCountingMultiHashSet(Equality<? super V> keyEquality) {
//		this(SerialHashStore.defaultRehasher(), keyEquality);
//	}
//	
//	public LockFreeLinkedCountingMultiHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
//		this(16, 0.75f, rehasher, keyEquality);
//	}
//	
//	public LockFreeLinkedCountingMultiHashSet( 
//			int minimumInitialCapacity, float loadFactor, 
//			Rehasher rehasher, Equality<? super V> keyEquality) 
//	{
//		super(Counters.newCounter(), rehasher, 
//			new NestedMultiHashSet.ValueEquality<V, LockFreeLinkedCountingMultiHashSet.Node<V>>(keyEquality), 
//			LockFreeLinkedCountingMultiHashSet.<V>factory(), 
//			new LockFreeLinkedHashStore<Node<V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.OFF));
//	}
//
//	// this implementation has no concurrency guarantees
//	public static final class Node<V> extends LockFreeLinkedHashNode<Node<V>> implements NestedMultiHashSet.INode<V, Node<V>> {
//		
//		private static final long serialVersionUID = -5766263745864028747L;
//		
//		public Node(int hash, V value, int count) {
//			super(hash);
//			this.value = value;
//			this.count = count;
//		}
//		
//		private final V value;
//		private int count;
//		
//		@Override public V getValue() { 
//			return value;
//		}
//		
//		@Override public Node<V> copy() { 
//			return new Node<V>(hash, value, count);
//		}
//		
//		@Override public int count() {
//			return count;
//		}
//		
//		@Override public int remove(int i) {
//			final int newc = count - i;
//			if (newc <= 0) {
//				final int oldc = count;
//				count = 0;
//				return oldc;
//			}
//			count = newc;
//			return i;
//		}
//		
//		@Override
//		public List<V> removeAndReturn(int target) {
//			return new UniformList<V>(value, remove(target));
//		}
//
//		@Override public boolean put(V val) {
//			if (count < 1)
//				return false;
//			count += 1;
//			return true;
//		}
//		
//		@Override public boolean put(V val, int c) {
//			if (count < 1)
//				return false;
//			count += c;
//			return true;
//		}
//		
//		@Override 
//		public boolean valid() { 
//			return count > 0;
//		}
//		
//		@Override
//		public boolean initialise() {
//			if (count != 0)
//				return false;
//			count = 1;
//			return true;
//		}
//		
//		@Override
//		public Iterator<V> iterator(final NestedMultiHashSet<V, Node<V>> arg) {
//			final LockFreeLinkedCountingMultiHashSet<V> set = (LockFreeLinkedCountingMultiHashSet<V>) arg;
//			return new Iterator<V>() {
//				int c = 0;
//				boolean last = false;
//				boolean next = false;
//				@Override
//				public boolean hasNext() {
//					return next = (count > c);
//				}
//				@Override
//				public V next() {
//					if (!next)
//						throw new NoSuchElementException();
//					c++;
//					last = true;
//					return value;
//				}
//				@Override
//				public void remove() {
//					if (!last)
//						throw new NoSuchElementException();
//					count -= 1;
//					if (count <= 0) {
//						count = -1;
//						set.removeNode(Node.this);
//					}
//				}
//			};
//		}
//		
//	}
//	
//	@SuppressWarnings("unchecked")
//	private static final NodeFactory FACTORY = new NodeFactory();
//	@SuppressWarnings("unchecked")
//	public static <V> NodeFactory<V> factory() {
//		return FACTORY;
//	}
//	public static final class NodeFactory<V> implements HashNodeFactory<V, Node<V>> {
//		@Override
//		public final Node<V> makeNode(final int hash, final V value) {
//			return new Node<V>(hash, value, 0);
//		}
//	}
//
//}
