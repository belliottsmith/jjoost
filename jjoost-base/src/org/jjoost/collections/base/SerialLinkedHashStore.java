package org.jjoost.collections.base;

import java.util.ConcurrentModificationException ;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jjoost.util.Equality ;
import org.jjoost.util.Function;

@SuppressWarnings("unchecked")
public class SerialLinkedHashStore<N extends SerialLinkedHashStore.SerialLinkedHashNode<N>> extends SerialHashStore<N> {

	private static final long serialVersionUID = -6706178526455624676L;

	public static abstract class SerialLinkedHashNode<N extends SerialLinkedHashNode<N>> extends SerialHashNode<N> {
		private static final long serialVersionUID = 2035712133283347382L;
		protected N linkNext ;
		protected N linkPrev ;
		public SerialLinkedHashNode(int hash) {
			super(hash) ;
		}
	}
	
	private static final <N extends SerialLinkedHashNode<N>> N newHead() {
		return (N) new Head<N>() ;
	}
	private static final class Head<N extends SerialLinkedHashNode<N>> extends SerialLinkedHashNode<N> {
		private static final long serialVersionUID = 5809977718436984909L;
		@Override public N copy() {
			throw new UnsupportedOperationException() ;
		}
		private Head() {
			super(0) ;
			linkNext = (N) this ;
			linkPrev = (N) this ;
		}
	}
	
	public SerialLinkedHashStore(int size, float loadFactor) {
		super(loadFactor, (N[]) new SerialLinkedHashNode[capacity(size)], 0, 0) ;
		head = newHead() ; 
	}
	
	private static int capacity(int size) {
        int capacity = 8 ;
        while (capacity < size)
        	capacity <<= 1 ;
        return capacity ;
	}

	private SerialLinkedHashStore(float loadFactor, N[] table, int totalNodeCount, int uniquePrefixCount, N head) {
		super(loadFactor, table, totalNodeCount, uniquePrefixCount) ;
		this.head = head ;
	}

	private final N head ;
	
	@Override
	protected void inserted(N n) {
		super.inserted(n) ;
		N tail = head.linkPrev ;
		n.linkPrev = tail ;
		n.linkNext = head ;
		tail.linkNext = n ;
		head.linkPrev = n ;		
	}

	@Override
	protected void removed(N n) {
		super.removed(n) ;
		final N next = n.linkNext ;
		final N prev = n.linkPrev ;
		next.linkPrev = prev ;
		prev.linkNext = next ; 
	}

	@Override
	public <NCmp> SerialHashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		int modCount = this.modCount ;
		final N[] tbl = (N[]) new SerialLinkedHashNode[this.table.length] ;
		final int totalNodeCount = this.totalNodeCount ;
		final int uniquePrefixCount = this.uniquePrefixCount ;
		N newHead, newTail = newHead() ;		
		newHead = newTail ;
		N cur = head.linkNext ;
		while (cur != head) {
			final N copy = cur.copy() ;
			copy.linkPrev = newTail ;
			newTail = newTail.linkNext = copy ;
			cur = cur.linkNext ;
		}
		newTail.linkNext = newHead ;
		if (modCount != this.modCount)
			throw new ConcurrentModificationException() ;
		newTail = newHead.linkNext ;
		final int mask = (tbl.length - 1) ;
		while (newTail != newHead) {
			final int bucket = newTail.hash & mask ;
			cur = tbl[bucket] ;
			if (cur == null) {
				tbl[bucket] = newTail ;
			} else {
				while (cur.next != null)
					cur = cur.next ;
				cur.next = newTail ;
			}
			newTail = newTail.linkNext ;
		}
		return new SerialLinkedHashStore<N>(loadFactor, tbl, totalNodeCount, uniquePrefixCount, newHead) ;
	}

	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> ret) {
		return new LinkIterator<NCmp, V>(nodePrefixEqFunc, nodePrefixEq, ret) ;
	}
	
	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj, 
			Equality<? super NCmp> uniquenessEquality, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		// TODO : implement - must apply uniqueness filter inside the LinkIterator in order to be able to apply the ret function afterwards...
		throw new UnsupportedOperationException() ;
	}
	
	private final class LinkIterator<NCmp, V> implements Iterator<V> {

		private final Function<? super N, ? extends NCmp> nodePrefixEqFunc ;
		private final HashNodeEquality<? super NCmp, ? super N> nodePrefixEq ;
		private final Function<? super N, ? extends V> ret ;
		private N prev = null ;
		private N next = head.linkNext ;
		private int curModCount = modCount ;
		
		public LinkIterator(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> f) {
			this.nodePrefixEqFunc = nodePrefixEqFunc ;
			this.nodePrefixEq = nodePrefixEq ;
			this.ret = f ;
		}

		@Override
		public boolean hasNext() {
			return next != head ;
		}

		@Override
		public V next() {
			if (next == head)
				throw new NoSuchElementException() ;
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			prev = next ;
			next = next.linkNext ;
			return ret.apply(prev) ;
		}

		@Override
		public void remove() {
			if (prev == null)
				throw new NoSuchElementException() ;
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			removeNode(nodePrefixEqFunc, nodePrefixEq, prev) ;
			curModCount = modCount ;
			prev = null ;
		}
		
	}

}
