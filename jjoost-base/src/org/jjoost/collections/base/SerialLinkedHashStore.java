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

package org.jjoost.collections.base;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jjoost.util.Function;

@SuppressWarnings("unchecked")
public class SerialLinkedHashStore<N extends SerialLinkedHashStore.SerialLinkedHashNode<N>> extends SerialHashStore<N> {

	private static final long serialVersionUID = -6706178526455624676L;

	public static abstract class SerialLinkedHashNode<N extends SerialLinkedHashNode<N>> extends SerialHashNode<N> {
		private static final long serialVersionUID = 2035712133283347382L;
		protected N linkNext;
		protected N linkPrev;
		public SerialLinkedHashNode(int hash) {
			super(hash);
		}
	}
	
	private static final <N extends SerialLinkedHashNode<N>> N newHead() {
		return (N) new Head<N>();
	}
	private static final class Head<N extends SerialLinkedHashNode<N>> extends SerialLinkedHashNode<N> {
		private static final long serialVersionUID = 5809977718436984909L;
		@Override public N copy() {
			throw new UnsupportedOperationException();
		}
		private Head() {
			super(0);
			linkNext = (N) this;
			linkPrev = (N) this;
		}
	}
	
	public SerialLinkedHashStore(int size, float loadFactor) {
		super(loadFactor, (N[]) new SerialLinkedHashNode[capacity(size)], 0, 0);
		head = newHead();
	}
	
	private static int capacity(int size) {
        int capacity = 8;
        while (capacity < size)
        	capacity <<= 1;
        return capacity;
	}

	private SerialLinkedHashStore(float loadFactor, N[] table, int totalNodeCount, int uniquePrefixCount, N head) {
		super(loadFactor, table, totalNodeCount, uniquePrefixCount);
		this.head = head;
	}

	private final N head;
	
	@Override
	protected void inserted(N n) {
		super.inserted(n);
		N tail = head.linkPrev;
		n.linkPrev = tail;
		n.linkNext = head;
		tail.linkNext = n;
		head.linkPrev = n;
	}

	@Override
	protected void reinserted(N n) {
		super.reinserted(n);
		final N head = this.head;
		N linkNext = n.linkNext;
		if (linkNext != head) {
			N linkPrev = n.linkPrev;
			linkPrev.linkNext = linkNext;
			linkNext.linkPrev = linkPrev;
			N tail = head.linkPrev;
			n.linkPrev = tail;
			n.linkNext = head;
			tail.linkNext = n;
			head.linkPrev = n;
		}
	}
	
	@Override
	protected void removed(N n) {
		super.removed(n);
		final N next = n.linkNext;
		final N prev = n.linkPrev;
		next.linkPrev = prev;
		prev.linkNext = next;
		n.linkNext = null;
		n.linkPrev = next;
	}

	@Override
	public <NCmp> SerialHashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		final N[] tbl = (N[]) new SerialLinkedHashNode[this.table.length];
		final int totalNodeCount = this.totalNodeCount;
		final int uniquePrefixCount = this.uniquePrefixCount;
		N newHead, newTail = newHead();
		newHead = newTail;
		N cur = head.linkNext;
		while (cur != head) {
			final N copy = cur.copy();
			copy.linkPrev = newTail;
			newTail = newTail.linkNext = copy;
			cur = cur.linkNext;
		}
		newTail.linkNext = newHead;
		newTail = newHead.linkNext;
		final int mask = (tbl.length - 1);
		while (newTail != newHead) {
			final int bucket = newTail.hash & mask;
			cur = tbl[bucket];
			if (cur == null) {
				tbl[bucket] = newTail;
			} else {
				int rev = Integer.reverse(newTail.hash);
				while (cur.next != null && Integer.reverse(cur.next.hash) < rev)
					cur = cur.next;
				newTail.next = cur.next;
				cur.next = newTail;
			}
			newTail = newTail.linkNext;
		}
		return new SerialLinkedHashStore<N>(loadFactor, tbl, totalNodeCount, uniquePrefixCount, newHead);
	}

	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> ret) {
		return new LinkIterator<NCmp, V>(nodePrefixEqFunc, nodePrefixEq, ret);
	}
	
	private final class LinkIterator<NCmp, V> implements Iterator<V> {

		private final Function<? super N, ? extends NCmp> nodePrefixEqFunc;
		private final HashNodeEquality<? super NCmp, ? super N> nodePrefixEq;
		private final Function<? super N, ? extends V> ret;
		private N prev = null;
		private N next = head.linkNext;
		
		public LinkIterator(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> f) {
			this.nodePrefixEqFunc = nodePrefixEqFunc;
			this.nodePrefixEq = nodePrefixEq;
			this.ret = f;
		}

		@Override
		public boolean hasNext() {
			if (next == null) {
				N prev = this.prev;
				if (prev.linkNext == null) {
					prev = prev.linkPrev;
					while (prev.linkNext == null)
						prev = prev.linkPrev;
					next = prev;
				} else next = prev.linkNext;
			}
			return next != head;
		}

		@Override
		public V next() {
			if (!hasNext())
				throw new NoSuchElementException();
			prev = next;
			next = null;
			return ret.apply(prev);
		}

		@Override
		public void remove() {
			if (prev.linkNext != null)
				removeNode(nodePrefixEqFunc, nodePrefixEq, prev);
		}
		
	}
	
	public N oldest() {
		final N head = this.head;
		final N next = head.next;
		if (next != head) {
			return next;
		}
		return null;
	}

}
