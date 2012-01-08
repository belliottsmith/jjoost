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

import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.LockSupport;

import org.jjoost.util.Function;
import org.jjoost.util.Functions;

@Deprecated
public class LockFreeLinkedHashStore<N extends LockFreeLinkedHashStore.LockFreeLinkedHashNode<N>> extends LockFreeHashStore<N> {

	private static final long serialVersionUID = 5364765360666482653L;

	public static abstract class LockFreeLinkedHashNode<N extends LockFreeLinkedHashNode<N>> extends LockFreeHashNode<N> {
		private static final long serialVersionUID = 2035712133283347382L;
		N linkNextPtr;
		N linkPrevPtr;
		public LockFreeLinkedHashNode(int hash) {
			super(hash);
		}
		private static final long linkNextPtrOffset;
		private static final long linkPrevPtrOffset;
		final boolean startLinkDelete(N expect) {
			return unsafe.compareAndSwapObject(this, linkNextPtrOffset, expect, DELETING_LINK_FLAG);
		}
		final void finishLinkDelete() {
			unsafe.putObjectVolatile(this, linkNextPtrOffset, DELETED_LINK_FLAG);
		}
		final boolean casLinkNext(N expect, N upd) {
			return unsafe.compareAndSwapObject(this, linkNextPtrOffset, expect, upd);
		}
		final void lazySetLinkNext(N upd) {
			unsafe.putOrderedObject(this, linkNextPtrOffset, upd);
		}
		final void lazySetLinkPrev(N upd) {
			unsafe.putOrderedObject(this, linkPrevPtrOffset, upd);
		}
		final void volatileSetLinkPrev(N upd) {
			unsafe.putObjectVolatile(this, linkPrevPtrOffset, upd);
		}
		@SuppressWarnings("unchecked")
		final N getLinkNextFresh() {
			return (N) unsafe.getObjectVolatile(this, linkNextPtrOffset);
		}
		@SuppressWarnings("unchecked")
		final N getLinkPrevFresh() {
			return (N) unsafe.getObjectVolatile(this, linkPrevPtrOffset);
		}
		final N getLinkNextStale() {
			return linkNextPtr;
		}
		final N getLinkPrevStale() {
			return linkPrevPtr;
		}
		static {
			try {
				Field field;
				field = LockFreeLinkedHashNode.class.getDeclaredField("linkNextPtr");
				linkNextPtrOffset = unsafe.objectFieldOffset(field);
				field = LockFreeLinkedHashNode.class.getDeclaredField("linkPrevPtr");
				linkPrevPtrOffset = unsafe.objectFieldOffset(field);
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final <N extends LockFreeLinkedHashNode<N>> N newHead() {
		return (N) new Head<N>();
	}
	private static final class Head<N extends LockFreeLinkedHashNode<N>> extends LockFreeLinkedHashNode<N> {
		private static final long serialVersionUID = 5809977718436984909L;
		@Override public N copy() {
			throw new UnsupportedOperationException();
		}
		private Head() {
			super(0);
			setupHead(this);
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static final void setupHead(LockFreeLinkedHashNode head) {
		head.lazySetLinkNext(head);
		head.lazySetLinkPrev(head);
	}

	@SuppressWarnings("rawtypes")
	private static final class FlagNode extends LockFreeLinkedHashNode {
		private static final long serialVersionUID = -8235849034699744602L;
		public final String type;
		public FlagNode(String type) {
			super(-1);
			this.type = type;
		}
		public String toString() {
			return type;
		}
		public FlagNode copy() {
			throw new UnsupportedOperationException();
		}
	}
	
	private static final FlagNode DELETING_LINK_FLAG = new FlagNode("DELETING");
	private static final FlagNode DELETED_LINK_FLAG = new FlagNode("DELETED");

	// TODO : can probably merge waiting
	protected final WaitingOnNode<N> waitingOnLink = new WaitingOnNode<N>(null, null);
	
	public LockFreeLinkedHashStore(float loadFactor, Counter totalCounter, Counter uniqCounter, boolean useUniqCounterForGrowth, N[] table) {
		super(loadFactor, totalCounter, uniqCounter, useUniqCounterForGrowth, table);
	}

	public LockFreeLinkedHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
		super(initialCapacity, loadFactor, totalCounting, uniquePrefixCounting);
	}

	private final N head = newHead();
	
	@Override
	// will be called precisely once per node
	protected void inserted(N n) {
		n.lazySetLinkNext(head);
		N tail = head.getLinkPrevStale();
		while (true) {
			if (tail.casLinkNext(head, n)) {
				n.lazySetLinkPrev(tail);
				head.volatileSetLinkPrev(n);
				waitingOnLink.wake(n);
				return;
			}
			tail = head.getLinkPrevFresh();
		}
	}

	@Override
	// will be called at most once per node
	protected void removed(N n) {
		N next = n.getLinkNextStale();
		while (true) {
			N prev = n.getLinkPrevStale();
			if (prev == null) {
				// node has not yet been inserted
				waitOnInsert(n);
				continue;
			}
			if (n.startLinkDelete(next)) {
				while (!prev.casLinkNext(n, next)) {
					waitOnDelete(prev);
					prev = n.getLinkPrevStale();
				}
				next.lazySetLinkPrev(prev);
				n.lazySetLinkPrev(next);
				n.finishLinkDelete();
				waitingOnLink.wake(n);
				return;
			}
		}
	}
 
	private void waitOnDelete(final N node) {
		if (node.getLinkNextFresh() != DELETING_LINK_FLAG)
			return;
		WaitingOnNode<N> waiting = new WaitingOnNode<N>(Thread.currentThread(), node);
		waitingOnLink.insert(waiting);
		while (node.getLinkNextFresh() == DELETING_LINK_FLAG)
			LockSupport.park();
		waiting.remove();
	}
	
	private void waitOnInsert(final N node) {
		if (node.getLinkPrevFresh() != null)
			return;
		WaitingOnNode<N> waiting = new WaitingOnNode<N>(Thread.currentThread(), node);
		waitingOnLink.insert(waiting);
		while (node.getLinkPrevFresh() == null)
			LockSupport.park();
		waiting.remove();
	}
	
	// we could iterate over the array twice, first time inserting in opposite order into buckets and on second pass reversing this...
	@SuppressWarnings("unchecked")
	@Override
	public <NCmp> HashStore<N> copy(
			Function<? super N, ? extends NCmp> nodeEqualityProj,		
			HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		final Iterator<N> iter = new LinkIterator<NCmp, N>(nodeEqualityProj, nodeEquality, Functions.<N>identity());
		final LockFreeLinkedHashStore<N> copy = new LockFreeLinkedHashStore<N>(
				loadFactor, 
				totalCounter.newInstance(0), 
				uniquePrefixCounter.newInstance(0), 
				uniquePrefixCounter == growthCounter, 
				(N[]) new LockFreeLinkedHashNode[capacity()]);
		while (iter.hasNext()) {
			final N next = iter.next().copy();
			copy.put(false, nodeEqualityProj.apply(next), next, nodeEquality, Functions.identity());
		}
		return copy;
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
		private N next = head;
		public LinkIterator(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> f) {
			this.nodePrefixEqFunc = nodePrefixEqFunc;
			this.nodePrefixEq = nodePrefixEq;
			this.ret = f;
			moveNext();
		}

		@Override
		public boolean hasNext() {
			return next != head;
		}

		@Override
		public V next() {
			if (next == head)
				throw new NoSuchElementException();
			prev = next;
			moveNext();
			return ret.apply(prev);
		}
		
		private void moveNext() {
			final N prev = this.next;
			N next = prev.getLinkNextStale();
			if (next == DELETING_LINK_FLAG | next == DELETED_LINK_FLAG) {
				waitOnDelete(prev);
				next = prev.getLinkPrevStale();
			}
			N nextNext = next.getLinkNextStale();
			while (nextNext == DELETING_LINK_FLAG | nextNext == DELETED_LINK_FLAG) {
				waitOnDelete(next);
				next = next.getLinkPrevStale();
				nextNext = next.getLinkNextStale();
			}
			this.next = next;
		}

		@Override
		public void remove() {
			if (prev == null)
				throw new NoSuchElementException();
			removeNode(nodePrefixEqFunc, nodePrefixEq, prev);
			prev = null;
		}
		
	}

}
