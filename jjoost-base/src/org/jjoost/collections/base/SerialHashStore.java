package org.jjoost.collections.base;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List ;
import java.util.NoSuchElementException;

import org.jjoost.collections.AnySet ;
import org.jjoost.collections.sets.serial.MultiArraySet ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters ;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;

public class SerialHashStore<N extends SerialHashStore.SerialHashNode<N>> implements HashStore<N> {

	private static final long serialVersionUID = 5818748848600569496L ;

	public static abstract class SerialHashNode<N extends SerialHashNode<N>> extends HashNode<N> {
		private static final long serialVersionUID = 2035712133283347382L;
		protected N next ;
		public SerialHashNode(int hash) {
			super(hash) ;
		}
	}
	
	protected N[] table ;
	protected int totalNodeCount ;
	protected int uniquePrefixCount ;
	protected int loadLimit ;
	protected final float loadFactor ;
	protected transient int modCount = 0 ;	
	
	protected void inserted(N n) {
		modCount++ ;
	}
	protected void removed(N n) {
		modCount++ ;
	}
	
	@SuppressWarnings("unchecked")
	public SerialHashStore(int size, float loadFactor) {
        int capacity = 8 ;
        while (capacity < size)
        	capacity <<= 1 ;
        this.totalNodeCount = 0 ;
        this.table = (N[]) new SerialHashNode[capacity] ;
        this.loadLimit = (int) (capacity * loadFactor) ;
		this.loadFactor = loadFactor ;
	}
	
	protected SerialHashStore(float loadFactor, N[] table, int totalNodeCount, int uniquePrefixCount) {
		this.totalNodeCount = totalNodeCount ;
		this.uniquePrefixCount = uniquePrefixCount ;		
		this.table = table ;
		this.loadLimit = (int) (table.length * loadFactor) ;
		this.loadFactor = loadFactor ;
	}
	
	// **************************************************
	// PUBLIC METHODS
	// **************************************************
	
	@Override
	public int capacity() {
		return table.length ;
	}
	
	@Override
    public int totalCount() {
    	return totalNodeCount ;
    }
    
	@Override
	public int uniquePrefixCount() {
		return uniquePrefixCount ;
	}
	
	@Override
	public boolean isEmpty() {
		return totalNodeCount == 0 ;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public int clear() {
		final int r = totalNodeCount ;
		totalNodeCount = 0 ;
		uniquePrefixCount = 0 ;
		table = (N[]) new SerialHashNode[table.length] ;
		return r ;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f) {
		final Iterator<V> r = new ClearedIterator<V>(table, f);
		totalNodeCount = 0 ;
		uniquePrefixCount = 0 ;
		table = (N[]) new SerialHashNode[table.length] ;
		return r ;
	}
	
	@Override
	public String toString() {
		return "{" + Iters.toString(all(null, null, Functions.<N>toString(true)), ", ") + "}" ;
	}

	// **************************************************
	// public PUT METHODS
	// **************************************************
	
	@Override
	public <NCmp, V> V put(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		grow() ;
		
		final boolean replace = eq.isUnique() ;
		final int hash = put.hash ;
		final int bucket = put.hash & (table.length - 1) ;		
		boolean partial = false ;
    	N n = table[bucket] ;
    	if (n == null) {
    		table[bucket] = put ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
    				if (partial) {
    					// inserting new node grouped with others with same prefix
    					p.next = put ;
    					put.next = n ;
    					totalNodeCount++ ;
    					inserted(put) ;
    					return null ;
    				} else partial = true ;
    			}   
    			if (partial && replace && eq.suffixMatch(find, n)) {
    				// replacing existing node
    				if (p == null) {
    					table[bucket] = put ;
    				} else {
    					p.next = put ;
    				}
					put.next = n.next ;
					removed(n) ;
					inserted(put) ;
    				return ret.apply(n) ;
    			}
    			p = n ;
    			n = n.next ;
    		}
			// inserting new node with no matching prefixes in table
    		p.next = put ;
    	}
    	if (!partial)
    		uniquePrefixCount++ ;
		totalNodeCount++ ;

		inserted(put) ;
		return null ;
	}

	@Override
	public <NCmp, V> V putIfAbsent(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		grow() ;

		final int hash = put.hash ;
		final int bucket = put.hash & (table.length - 1) ;		
		boolean partial = false ;
    	N n = table[bucket] ;
    	if (n == null) {
    		table[bucket] = put ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
    				if (partial) {
    					// inserting new node grouped with others with same prefix
    					p.next = put ;
    					put.next = n ;
    					totalNodeCount++ ;
    					inserted(put) ;
    					return null ;
    				} else partial = true ;
    			}    			
    			if (partial && eq.suffixMatch(find, n)) {
    				// already present so not inserting
    				return ret.apply(n) ;
    			}
    			p = n ;
    			n = n.next ;
    		}
			// inserting new node with no matching prefixes in table
    		p.next = put ;
    	}
    	if (!partial)
    		uniquePrefixCount++ ;
		totalNodeCount++ ;

		inserted(put) ;
		return null ;
	}

	@Override
	public <NCmp, V> V putIfAbsent(final int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) {
		grow() ;
		
		final int bucket = hash & (table.length - 1) ;
		boolean partial = false ;
    	N n = table[bucket] ;
    	if (n == null) {
    		table[bucket] = factory.makeNode(hash, put) ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			if (partial != (n.hash == hash && eq.prefixMatch(put, n))) {
    				if (partial) {
    					// inserting new node grouped with others with same prefix
    					p = p.next = factory.makeNode(hash, put) ;
    					p.next = n ;
    					inserted(p) ;
    					return null ;
    				} else partial = true ;
    			}    			
    			if (partial && eq.suffixMatch(put, n)) {
    				return ret.apply(n) ;
    			}
    			p = n ;
    			n = n.next ;
    		}
			// inserting new node with no matching prefix in table
    		p.next = factory.makeNode(hash, put) ;
    		inserted(p.next) ;
    	}
    	if (!partial)
    		uniquePrefixCount++ ;
		totalNodeCount++ ;

		return null ;
	}
	
	@Override
	public <NCmp, V> V ensureAndGet(final int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) {
		grow() ;
		
		boolean partial = false ;
		final int bucket = hash & (table.length - 1) ;
    	N n = table[bucket] ;
    	if (n == null) {
    		n = table[bucket] = factory.makeNode(hash, put) ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			if (partial != (n.hash == hash && eq.prefixMatch(put, n))) {
    				if (partial) {
    					// inserting new node grouped with others with same prefix
    					p = p.next = factory.makeNode(hash, put) ;
    					p.next = n ;
    		    		inserted(p) ;
    					return ret.apply(p) ;
    				} else partial = true ;
    			}    			
    			if (partial && eq.suffixMatch(put, n)) {
					// already present so not inserting
    				return ret.apply(n) ;
    			}
    			p = n ;
    			n = n.next ;
    		}
    		n = p.next = factory.makeNode(hash, put) ;
    	}
    	if (!partial)
    		uniquePrefixCount++ ;
		totalNodeCount++ ;

		inserted(n) ;
		return ret.apply(n) ;
	}
	
	// **************************************************
	// public REMOVE METHODS
	// **************************************************
	
	@Override
	public <NCmp> int remove(final int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return 0 ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}
		final boolean eqIsUniq = eq.isUnique() ;
    	final int bucket = hash & (table.length - 1) ;
		boolean partial = false ;
    	N p = null ;
    	N n = table[bucket] ; 
    	int r = 0 ;
    	boolean keptNeighbours = false ;
    	while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
				if (partial) {
					if (!keptNeighbours)
						uniquePrefixCount -= 1 ;
					return r ;
				} else partial = true ;
			}
    		if (partial && eq.suffixMatch(find, n)) {
    			r++ ;
    			final N next = n.next ;
    			if (p == null) {
    				table[bucket] = next ;
    			} else {
    				p.next = next ;
    			}
				n.next = null ;
    			removed(n) ;
				n = next ;
    			totalNodeCount -= 1 ;
    			if (eqIsUniq | (r == removeAtMost)) {
    				if (!keptNeighbours)
    					keptNeighbours = n != null 
    						&& n.hash == hash 
    						&& eq.prefixMatch(find, n) ;
    				break ;
    			}
    		} else if (partial) {
    			keptNeighbours = true ;
    			if (eqIsUniq)
    				break ;
    		} else {
    			p = n ;
    			n = p.next ;
    		}
    	}
    	
    	if (!keptNeighbours && r != 0)
    		uniquePrefixCount -= 1 ;
		return r ;
	}

	@Override
	public <NCmp> boolean removeNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {
		final int bucket = n.hash & (table.length - 1) ;
		N p = table[bucket] ;
		if (p == n) {
			table[bucket] = p.next ;
			if (p.next == null || !nodePrefixEq.prefixMatch(nodePrefixEqFunc.apply(n), p.next))
				uniquePrefixCount -= 1 ;
		} else {
			while (p != null && p.next != n)
				p = p.next ;
			if (p == null)
				return false ;
			p.next = n.next ;
			final NCmp cmp = nodePrefixEqFunc.apply(n) ;
			if (!nodePrefixEq.prefixMatch(cmp, p) && (p.next == null || !nodePrefixEq.prefixMatch(cmp, p.next)))
				uniquePrefixCount -= 1 ;
		}
		removed(n) ;
		totalNodeCount -= 1 ;
		return true ;
	}

	@Override
	public <NCmp, V> Iterable<V> removeAndReturn(int hash, int removeAtMost, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return removedNodeIterable(internalRemoveAndReturn(hash, removeAtMost, c, eq), ret) ;
	}
	@Override
	public <NCmp, V> V removeAndReturnFirst(int hash, int removeAtMost, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		final N n = internalRemoveAndReturn(hash, removeAtMost, c, eq) ;
		return n == null ? null : ret.apply(n) ;
	}
	private <NCmp> N internalRemoveAndReturn(final int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return null ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}
		
		final boolean eqIsUniq = eq.isUnique() ;
		boolean partial = false ;
		final int bucket = hash & (table.length - 1) ;
		N p = null ;
		N n = table[bucket] ; 
		boolean keptNeighbours = false ;
		N removedHead = null, removedTail = null ;
		int c = 0 ;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
				if (partial) {
					if (!keptNeighbours)
						uniquePrefixCount -= 1 ;
					return removedHead ;
				} else partial = true ;
			}
			if (partial && eq.suffixMatch(find, n)) {
				c++ ;
				final N next = n.next ;
				if (p == null) {
					table[bucket] = next ;
				} else {
					p.next = next ;
				}
				if (removedHead == null) {
					removedHead = removedTail = n ;
				} else {
					removedTail = removedTail.next = n ;
				}
				n.next = null ;
				totalNodeCount -= 1 ;
				removed(n) ;				
				n = next ;
				if (eqIsUniq | (c == removeAtMost)) {
    				if (!keptNeighbours)
    					keptNeighbours = n != null 
    						&& n.hash == hash 
    						&& eq.prefixMatch(find, n) ;
    				break ;
				}
			} else if (partial) {
				keptNeighbours = true ;
				if (eqIsUniq)
					break ;
			} else {
				p = n ;
				n = p.next ;
			}
		}
		
		if (!keptNeighbours && removedHead != null)
			uniquePrefixCount -= 1 ;
		
		return removedHead ;
	}
	
	// **************************************************
	// public MEMBERSHIP METHODS
	// **************************************************
	
	@Override
	public <NCmp> boolean contains(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq) {
		boolean partial = false ;
    	N n = table[hash & (table.length - 1)] ;
    	while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(c, n))) {
				if (partial) return false ;
				else partial = true ;
			}
    		if (partial && eq.suffixMatch(c, n))
    			return true ;
    		n = n.next ;
    	}
    	return false ;
	}
	
	@Override
	public <NCmp> int count(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq) {
		final boolean eqIsUniq = eq.isUnique() ;
		boolean partial = false ;
		int count = 0 ;
		N n = table[hash & (table.length - 1)] ;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(c, n))) {
				if (partial) return count ;
				else partial = true ;
			}
			if (partial && eq.suffixMatch(c, n)) {
				if (eqIsUniq)
					return 1 ;
				count++ ;
			}
			n = n.next ;
		}
		return count ;
	}
	
	// **************************************************
	// public RETRIEVAL METHODS
	// **************************************************
	
	@Override
	public <NCmp, V> V first(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		boolean partial = false ;
		N n = table[hash & (table.length - 1)] ;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(c, n))) {
				if (partial) return null ;
				else partial = true ;
			}
			if (partial && eq.suffixMatch(c, n))
				return ret.apply(n) ;
			n = n.next ;
		}
		return null ;
	}

	// TODO : for efficiency, don't delegate to Iters.toList() 
	@Override
	public <NCmp, V> List<V> findNow(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq,
			Function<? super N, ? extends V> ret) {
		return Iters.toList(find(hash, find, findEq, null, null, ret)) ;
	}
	
	@Override
	public <NCmp, NCmp2, V> Iterator<V> find(
			int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> findEq, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEq, 
			Function<? super N, ? extends V> ret) {		
		return new Search<NCmp, NCmp2, V>(hash, find, findEq, nodeEqualityProj, nodeEq, ret) ;
	}
	
	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqProj, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> retProj) {
		return new AllIterator<NCmp, V>(nodePrefixEqProj, nodePrefixEq, retProj) ;
	}
	
	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj, 
			Equality<? super NCmp> uniquenessEquality, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		return new UniqueIterator<NCmp, NCmp2, V>(uniquenessEqualityProj, uniquenessEquality, nodeEqualityProj, nodeEquality, ret) ;
	}
	
	// **************************************************
	// public ITERATOR CLASSES
	// **************************************************
	
	abstract class GeneralIterator<NCmp, V> implements Iterator<V> {
		
		final Function<? super N, ? extends NCmp> nodeEqualityProj ;
		final HashNodeEquality<? super NCmp, ? super N> nodeEquality ;
		final Function<? super N, ? extends V> ret ;
		int curHash, curModCount = modCount ;
		N curPrev , curNode , nextPrev , nextNode ;

		public GeneralIterator(
				Function<? super N, ? extends NCmp> nodeEqualityProj, 
				HashNodeEquality<? super NCmp, ? super N> nodeEquality,
				Function<? super N, ? extends V> ret) {
			this.nodeEqualityProj = nodeEqualityProj ;
			this.nodeEquality = nodeEquality ;
			this.ret = ret ;
		}

		public boolean hasNext() { 
			return nextNode != null ; 
		}

		public void remove() {
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			if (curNode == null)
				throw new NoSuchElementException("Nothing to remove!") ;
			if (curPrev == null) {
				table[curHash & (table.length - 1)] = curNode.next ;
				if (nextNode == null || !nodeEquality.prefixMatch(nodeEqualityProj.apply(curNode), nextNode))
					uniquePrefixCount -= 1 ;
			} else {
				curPrev.next = curNode.next ;
				final NCmp cmp = nodeEqualityProj.apply(curPrev) ;
				if (!nodeEquality.prefixMatch(cmp, curNode) && (nextNode == null || !nodeEquality.prefixMatch(cmp, nextNode)))
					uniquePrefixCount -= 1 ;
			}
			removed(curNode) ;
			nextPrev = curPrev ;
			curPrev = null ;
			curNode = null ;
			totalNodeCount -= 1 ;
			curModCount = ++modCount ;
		}

	}
	
	private class AllIterator<NCmp, V> extends GeneralIterator<NCmp, V> {
		
		int nextHash = - 1 ;
		
		AllIterator(Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality, Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret) ;
			while (nextNode == null & nextHash != table.length - 1) {
				nextNode = table[++nextHash] ;
			}
		}

		public V next() {
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			if (nextNode == null)
				throw new NoSuchElementException() ;
			curNode = nextNode ;
			curHash = nextHash ;
			curPrev = nextPrev ;
			nextPrev = nextNode ;
			nextNode = nextNode.next ;
			while (nextNode == null & nextHash != table.length - 1) {
				nextPrev = nextNode ;
				nextNode = table[++nextHash] ;
			}
			return ret.apply(curNode) ;
		}
		
	}
	
	private class UniqueIterator<NCmp, NCmp2, V> extends GeneralIterator<NCmp2, V> {
		
		final Function<? super N, ? extends NCmp> uniquenessEqualityProj ;
		final AnySet<NCmp> seen ;
		
		int nextHash = - 1 ;
		
		UniqueIterator(
				Function<? super N, ? extends NCmp> uniquenessEqualityProj, 
				Equality<? super NCmp> uniquenessEquality, 
				Function<? super N, ? extends NCmp2> nodeEqualityProj, 
				HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret) ;
			this.seen = new MultiArraySet<NCmp>(4, uniquenessEquality) ;
			this.uniquenessEqualityProj = uniquenessEqualityProj ;
			while (nextNode == null & nextHash != table.length - 1) {
				nextNode = table[++nextHash] ;
			}
		}
		
		public V next() {
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			if (nextNode == null)
				throw new NoSuchElementException() ;
			seen.put(uniquenessEqualityProj.apply(nextNode)) ;
			curNode = nextNode ;
			curHash = nextHash ;
			curPrev = nextPrev ;
			nextPrev = nextNode ;			
			nextNode = nextNode.next ;
			while (nextNode != null && seen.contains(uniquenessEqualityProj.apply(nextNode))) {				
				nextNode = nextNode.next ;
				while (nextNode == null & nextHash != table.length - 1) {
					nextPrev = nextNode ;
					nextNode = table[++nextHash] ;
				}
			}
			if (nextNode == null || curNode.hash != nextNode.hash 
					|| !nodeEquality.prefixMatch(nodeEqualityProj.apply(curNode), nextNode))
				seen.clear() ;
			return ret.apply(curNode) ;
		}
		
	}
	
	class Search<NCmp, NCmp2, V> extends GeneralIterator<NCmp2, V> {
				
		final NCmp find ;
		final HashNodeEquality<? super NCmp, ? super N> findEquality ;
		
		Search(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq, 
				Function<? super N, ? extends NCmp2> nodeEqualityProj,
				HashNodeEquality<? super NCmp2, ? super N> nodeEquality,
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret) ;
			this.find = find ;
			this.curHash = hash ;
			this.findEquality = findEq ;		
			nextNode = table[curHash & (table.length - 1)] ;
			boolean partial = false ;
			while (nextNode != null) {
				if (partial != (hash == nextNode.hash && findEq.prefixMatch(find, nextNode))) {
					if (partial) {
						nextNode = null ;
						nextPrev = null ;
						return ;
					} else partial = true ;
				}
				if (partial && findEq.suffixMatch(find, nextNode))
					break ;
				nextPrev = nextNode ;
				nextNode = nextNode.next ;
			}
		}
		
		public V next() {
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			if (nextNode == null)
				throw new NoSuchElementException() ;
			curNode = nextNode ;
			curPrev = nextPrev ;
			if (findEquality.isUnique()) {
				nextPrev = nextNode = null ;
			} else {
				nextPrev = nextNode ;
				nextNode = nextNode.next ;
				while (nextNode != null) {
					if (curNode.hash != nextNode.hash || !findEquality.prefixMatch(find, nextNode)) {
						nextNode = null ;
						break ;
					} 
					if (findEquality.suffixMatch(find, nextNode))
						break ;
					nextPrev = nextNode ;
					nextNode = nextNode.next ;
				}
			}
			return ret.apply(curNode) ;
		}
		
	}
	
	private class ClearedIterator<V> implements Iterator<V> {
		
		final N[] table ;
		final Function<? super N, ? extends V> f ;
		int nextBucket = - 1 ;
		N nextNode ;
		
		ClearedIterator(N[] table, Function<? super N, ? extends V> f) {
			this.table = table ;
			this.f = f ;
			while (nextNode == null & nextBucket != table.length - 1) {
				nextNode = table[++nextBucket] ;
			}
		}
		
		public boolean hasNext() {
			return nextNode != null ;
		}
		
		public void remove() {
			// no op - already removed for goodness sake!
		}

		public V next() {
			if (nextNode == null)
				throw new NoSuchElementException() ;
			N r = nextNode ;
			nextNode = nextNode.next ;
			while (nextNode == null & (nextBucket != table.length - 1)) {
				nextNode = table[++nextBucket] ;
			}
			return f.apply(r) ;
		}
		
	}

	// **************************************************
	// PRIVATE METHODS
	// **************************************************

	@SuppressWarnings("unchecked")
	private void grow() {
		if (totalNodeCount >= loadLimit) {
			N[] oldtable = table ;
			table = (N[]) new SerialHashNode[table.length << 1] ;
			loadLimit = (int) (table.length * loadFactor) ;
			rehash(oldtable) ;			
		}
	}
	
	public void shrink() {
		int size = table.length ;
		while ((int)(size * loadFactor) > totalNodeCount)
			size >>= 1 ;
		size <<= 1 ;
		if (size <= 1)
			size = 2 ;
		if (size < table.length)
			resize(size) ;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void resize(int size) {
        int capacity = 8 ;
        while (capacity < size)
        	capacity <<= 1 ;
        if (capacity != table.length) {
    		N[] oldtable = table ;
    		table = (N[]) new SerialHashNode[capacity] ;
    		loadLimit = (int) (table.length * loadFactor) ;
    		rehash(oldtable) ;
        }
	}
	
	/**
	 * This method makes use of the fact that both tables are a power of 2 in size. It maintains the ordering of nodes within groups where matcher.partialMatch() == 1; grows fully maintain ordering of nodes
	 *  
	 * @param oldTable
	 */
    @SuppressWarnings("unchecked")
	private void rehash(N[] oldTable) {
    	// @@ TEST
    	final N[] table = this.table ;
    	if (oldTable.length > table.length) {
    		final int newTableMask = table.length - 1 ;
    		for (int i = 0 ; i != oldTable.length ; i++) {
    			final int newTableIndex = i & newTableMask ;
    			final N newHead = oldTable[i] ;
    			N existingTail = table[newTableIndex] ;
    			if (existingTail == null) {
    				table[newTableIndex] = newHead ;
    			} else if (newHead != null) {
    				while (existingTail.next != null)
    					existingTail = existingTail.next ;
    				existingTail.next = newHead ;
    			}
    		}
    	} else if (table.length == oldTable.length << 1) { 
    		int newIndexBit = oldTable.length ;
        	for (int i = 0 ; i != oldTable.length ; i++) {
        		N tail1 = null, tail2 = null;
        		N node = oldTable[i] ;
        		while (node != null) {
            		final N next = node.next ;            		
            		node.next = null ;
            		if ((node.hash & newIndexBit) == 0) {
            			if (tail1 == null) {
            				tail1 = table[i] = node ;
            			} else {
            				tail1 = tail1.next = node ;
            			}
            		} else {
            			if (tail2 == null) {
            				tail2 = table[i | newIndexBit] = node ;
            			} else {
            				tail2 = tail2.next = node ;
            			}
            		}
        			node = next ;
        		}
        	}
    	} else {
    		final int tailShift = Integer.bitCount(oldTable.length - 1) ;
    		final int newTableMask = table.length - 1 ;
    		final N[] tails = (N[]) new SerialHashNode[table.length >> tailShift] ;
    		boolean flushNext = false ; 
        	for (int i = 0 ; i != oldTable.length ; i++) {
        		if (flushNext) 
        			Arrays.fill(tails, null) ;
        		N node = oldTable[i] ;
        		flushNext = node != null ;
        		while (node != null) {
            		final N next = node.next ;
            		node.next = null ;
            		final int newIndex = node.hash & newTableMask ;
            		final int tail = newIndex >> tailShift ;
            		if (tails[tail] == null) {
            			table[newIndex] = tails[tail] = node ;
            		} else {
            			tails[tail] = tails[tail].next = node ;
            		}
        			node = next ;
        		}
        	}
    	}
    }

	@Override
	public <NCmp> HashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		final N[] table = this.table.clone() ;
		for (int i = 0 ; i != table.length ; i++) {
			N orig = table[i] ;
			if (orig != null) {
				N copy = orig.copy() ;
				table[i] = copy ;
				orig = orig.next ;
				while (orig != null) {
					copy.next = copy = orig.copy() ;
					orig = orig.next ;
				}
			}
		}
		return new SerialHashStore<N>(loadFactor, table, totalNodeCount, uniquePrefixCount) ;
	}

	private static <N extends SerialHashNode<N>, V> Iterable<V> removedNodeIterable(N head, Function<? super N, ? extends V> f) {
		return new RemovedNodeIterable<N, V>(head, f) ;
	}
	private static final class RemovedNodeIterable<N extends SerialHashNode<N>, V> implements Iterable<V> {
		private final N head ;
		private final Function<? super N, ? extends V> f ;
		public RemovedNodeIterable(N head, Function<? super N, ? extends V> f) {
			this.head = head ;
			this.f = f;
		}
		@Override
		public Iterator<V> iterator() {
			return new NodeIterator<N, V>(head, f) ;
		}		
	}
	private static final class NodeIterator<N extends SerialHashNode<N>, V> implements Iterator<V> {
		private N cur ;
		private final Function<? super N, ? extends V> f ;
		public NodeIterator(N head, Function<? super N, ? extends V> f) {
			this.cur = head ;
			this.f = f;
		}
		@Override
		public boolean hasNext() {
			return cur != null ;
		}
		@Override
		public V next() {
			if (cur == null)
				throw new NoSuchElementException() ;
			final V r = f.apply(cur) ;
			cur = cur.next ;
			return r ;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException() ;
		}
	}

	public static Rehasher defaultRehasher() {
		return Rehashers.jdkHashmapRehasher() ;
	}

}
