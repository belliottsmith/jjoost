package org.jjoost.collections.base;

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
		super(size, loadFactor);
	}

	public SerialLinkedHashStore(float loadFactor, N[] table, int size) {
		super(loadFactor, table, size);
	}

	private final N head = newHead() ;
	
	@Override
	protected void inserted(N n) {
		N tail = head.linkPrev ;
		n.linkPrev = tail ;
		n.linkNext = head ;
		tail.linkNext = n ;
		head.linkPrev = n ;
	}

	@Override
	protected void removed(N n) {
		final N next = n.linkNext ;
		final N prev = n.linkPrev ;
		next.linkPrev = prev ;
		prev.linkNext = next ; 
	}

	// TODO : is there an efficient way to copy this without temporarily doubling table memory utilisation? 
	// we could instead iterate over the array twice, first time inserting in opposite order into buckets and on second pass reversing this...
	@Override
	public <NCmp> HashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		throw new UnsupportedOperationException() ;
//		final N[] table = (N[]) new SerialLinkedHashNode[this.table.length] ;
//		final N[] tails = (N[]) new SerialLinkedHashNode[this.table.length] ;
//		final Iterator<N> ns = all(null, null, Functions.<N>identity()) ;
//		N listTail = newHead() ;
//		while (ns.hasNext()) {
//			final N n = ns.next().copy() ;
//			final int hash = n.hash & (table.length - 1) ;
//			final N tail = tails[hash] ;
//			if (tail == null) {
//				table[hash] = tails[hash] = n ;
//			} else {
//				tails[hash] = tail.next = n ;
//			}
//			n.linkPrev = listTail ;
//			listTail = listTail.linkNext = n ;
//		}
//		for (int i = 0 ; i != table.length ; i++) {
//			N orig = table[i] ;
//			if (orig != null) {
//				N copy = orig.copy() ;
//				table[i] = copy ;
//				orig = orig.linkNext ;
//				while (orig != null) {
//					copy.linkNext = copy = orig.copy() ;
//					orig = orig.linkNext ;
//				}
//			}
//		}
//		return new SerialHashStore<N>(loadFactor, table, totalNodeCount) ;
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
			prev = next ;
			next = next.linkNext ;
			return ret.apply(prev) ;
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
