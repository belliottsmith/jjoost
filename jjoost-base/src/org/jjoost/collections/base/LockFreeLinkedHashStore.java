package org.jjoost.collections.base;

import java.lang.reflect.Field ;
import java.lang.reflect.UndeclaredThrowableException ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.concurrent.locks.LockSupport ;

import org.jjoost.util.Equality ;
import org.jjoost.util.Function ;

@SuppressWarnings("restriction")
public class LockFreeLinkedHashStore<N extends LockFreeLinkedHashStore.LockFreeLinkedHashNode<N>> extends LockFreeHashStore<N> {

	private static final long serialVersionUID = 5364765360666482653L ;

	public static abstract class LockFreeLinkedHashNode<N extends LockFreeLinkedHashNode<N>> extends LockFreeHashNode<N> {
		private static final long serialVersionUID = 2035712133283347382L;
		protected N linkNextPtr ;
		protected N linkPrevPtr ;
		public LockFreeLinkedHashNode(int hash) {
			super(hash) ;
		}
		private static final long linkNextPtrOffset ;
		private static final long linkPrevPtrOffset ;
		private final boolean startLinkDelete(N expect) {
			return unsafe.compareAndSwapObject(this, linkNextPtrOffset, expect, DELETING_FLAG) ;
		}
		private final void finishLinkDelete() {
			unsafe.putObjectVolatile(this, linkNextPtrOffset, DELETED_FLAG) ;
		}
		private final boolean casLinkNext(N expect, N upd) {
			return unsafe.compareAndSwapObject(this, linkNextPtrOffset, expect, upd) ;
		}
		private final void lazySetLinkNext(N upd) {
			unsafe.putOrderedObject(this, linkNextPtrOffset, upd) ;
		}
		private final void lazySetLinkPrev(N upd) {
			unsafe.putOrderedObject(this, linkPrevPtrOffset, upd) ;			
		}
		private final void volatileSetLinkPrev(N upd) {
			unsafe.putObjectVolatile(this, linkPrevPtrOffset, upd) ;			
		}
		@SuppressWarnings("unchecked")
		private final N getLinkNextFresh() {
			return (N) unsafe.getObjectVolatile(this, linkNextPtrOffset) ;
		}
		@SuppressWarnings("unchecked")
		private final N getLinkPrevFresh() {
			return (N) unsafe.getObjectVolatile(this, linkPrevPtrOffset) ;
		}
		private final N getLinkNextStale() {
			return linkNextPtr ;
		}
		private final N getLinkPrevStale() {
			return linkPrevPtr ;
		}
		static {
			try {
				Field field ;
				field = LockFreeLinkedHashNode.class.getDeclaredField("linkNextPtr") ;
				linkNextPtrOffset = unsafe.objectFieldOffset(field) ;
				field = LockFreeLinkedHashNode.class.getDeclaredField("linkPrevPtr") ;
				linkPrevPtrOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final <N extends LockFreeLinkedHashNode<N>> N newHead() {
		return (N) new Head<N>() ;
	}
	private static final class Head<N extends LockFreeLinkedHashNode<N>> extends LockFreeLinkedHashNode<N> {
		private static final long serialVersionUID = 5809977718436984909L;
		@Override public N copy() {
			throw new UnsupportedOperationException() ;
		}
		private Head() {
			super(0) ;
			setupHead(this) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final void setupHead(LockFreeLinkedHashNode head) {
		head.lazySetLinkNext(head) ;
		head.lazySetLinkPrev(head) ;
	}

	@SuppressWarnings("unchecked")
	private static final class FlagNode extends LockFreeLinkedHashNode {
		private static final long serialVersionUID = -8235849034699744602L ;
		public final String type ;
		public FlagNode(String type) {
			super(-1) ;
			this.type = type ;
		}
		public String toString() {
			return type ;
		}
		public FlagNode copy() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	private static final FlagNode DELETING_FLAG = new FlagNode("DELETING") ;
	private static final FlagNode DELETED_FLAG = new FlagNode("DELETED") ;

	protected final WaitingOnNode<N> waitingOnLinkInsert = new WaitingOnNode<N>(null, null) ;
	protected final WaitingOnNode<N> waitingOnLinkDelete = new WaitingOnNode<N>(null, null) ;
	
	public LockFreeLinkedHashStore(float loadFactor, Counter totalCounter, Counter uniqCounter, boolean useUniqCounterForGrowth, N[] table) {
		super(loadFactor, totalCounter, uniqCounter, useUniqCounterForGrowth, table) ;
	}

	public LockFreeLinkedHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
		super(initialCapacity, loadFactor, totalCounting, uniquePrefixCounting) ;
	}

	private final N head = newHead() ;
	
	@Override
	// will be called precisely once per node
	protected void inserted(N n) {
		N tail = head.getLinkPrevStale() ;
		while (true) {
			n.lazySetLinkNext(head) ;
			if (tail.casLinkNext(head, n)) {
				n.lazySetLinkPrev(tail) ;
				head.volatileSetLinkPrev(n) ;
				waitingOnLinkInsert.wake(n) ;
				return ;
			}
			tail = head.getLinkPrevFresh() ;		
		}
	}

	@Override
	// will be called at most once per node
	protected void removed(N n) {
		N next = n.getLinkNextStale() ;
		while (true) {
			N prev = n.getLinkPrevStale() ;
			if (prev == null) {
				// node has not yet been inserted
				waitOnInsert(n) ;
				continue ;
			}
			if (n.startLinkDelete(next)) {
				while (!prev.casLinkNext(n, next)) {
					waitOnDelete(prev) ;
					prev = n.getLinkPrevStale() ;
				}
				next.lazySetLinkPrev(prev) ;
				n.lazySetLinkPrev(next) ;
				n.finishLinkDelete() ;
				waitingOnLinkDelete.wake(n) ;
				return ;
			}
		}
	}

	private void waitOnDelete(final N node) {
		if (node.getLinkNextFresh() != DELETING_FLAG)
			return ;
		WaitingOnNode<N> waiting = new WaitingOnNode<N>(Thread.currentThread(), node) ;
		waitingOnLinkDelete.insert(waiting) ;
		while (node.getLinkNextFresh() == DELETING_FLAG)
			LockSupport.park() ;
		waiting.remove() ;
	}
	
	private void waitOnInsert(final N node) {
		if (node.getLinkPrevFresh() != null)
			return ;
		WaitingOnNode<N> waiting = new WaitingOnNode<N>(Thread.currentThread(), node) ;
		waitingOnLinkDelete.insert(waiting) ;
		while (node.getLinkPrevFresh() == null)
			LockSupport.park() ;
		waiting.remove() ;
	}
	
	// we could iterate over the array twice, first time inserting in opposite order into buckets and on second pass reversing this...
	@Override
	public <NCmp> HashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> ret) {
		return new LinkIterator<NCmp, V>(nodePrefixEqFunc, nodePrefixEq, ret) ;
	}
	
	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj, 
			Equality<? super NCmp> uniquenessEquality, 
			Locality duplicateLocality, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		throw new UnsupportedOperationException() ;
	}
	
	private final class LinkIterator<NCmp, V> implements Iterator<V> {

		private final Function<? super N, ? extends NCmp> nodePrefixEqFunc ;
		private final HashNodeEquality<? super NCmp, ? super N> nodePrefixEq ;
		private final Function<? super N, ? extends V> ret ;
		private N prev = null ;
		private N next = head ;
		public LinkIterator(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> f) {
			this.nodePrefixEqFunc = nodePrefixEqFunc ;
			this.nodePrefixEq = nodePrefixEq ;
			this.ret = f ;
			moveNext() ;
		}

		@Override
		public boolean hasNext() {
			return next != head ;
		}

		@Override
		public V next() {
			if (next == head)
				throw new NoSuchElementException() ;
			prev = next ;
			moveNext() ;
			return ret.apply(prev) ;
		}
		
		private void moveNext() {
			final N prev = this.next ;
			N next = prev.getLinkNextStale() ;
			if (next == DELETING_FLAG | next == DELETED_FLAG) {
				waitOnDelete(prev) ;
				next = prev.getLinkPrevStale() ;
			}
			N nextNext = next.getLinkNextStale() ;
			while (nextNext == DELETING_FLAG | nextNext == DELETED_FLAG) {
				waitOnDelete(next) ;
				next = next.getLinkPrevStale() ;
				nextNext = next.getLinkNextStale() ;
			}
			this.next = next ;
		}

		@Override
		public void remove() {
			if (prev == null)
				throw new NoSuchElementException() ;
			removeNode(nodePrefixEqFunc, nodePrefixEq, prev) ;
			prev = null ;
		}
		
	}

}
