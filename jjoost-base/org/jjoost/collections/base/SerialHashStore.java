package org.jjoost.collections.base;

import java.io.Serializable;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jjoost.collections.ScalarSet ;
import org.jjoost.collections.sets.serial.SerialScalarHashSet ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Hashers ;
import org.jjoost.util.Iters ;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;

public class SerialHashStore<N extends SerialHashStore.SerialHashNode<N>> implements HashStore<N> {

	private static final long serialVersionUID = 5818748848600569496L ;

	public static abstract class SerialHashNode<N> implements Serializable {
		private static final long serialVersionUID = 2035712133283347382L;
		public final int hash ;
		protected N next ;
		public SerialHashNode(int hash) {
			this.hash = hash ;
		}
		public abstract N copy() ;
		public final int hash() { return hash ; }
	}
	
	protected N[] table ;
	protected int totalNodeCount ;
	protected int uniquePrefixCount ;
	protected int mask ;
	protected int loadLimit ;
	protected final float loadFactor ;
	protected transient int modCount = 0 ;	
	
	protected void inserted(N n) {		
	}
	protected void removed(N n) {		
	}
	
	@SuppressWarnings("unchecked")
	public SerialHashStore(int size, float loadFactor) {
        int capacity = 1 ;
        while (capacity < size)
        	capacity <<= 1 ;
        this.totalNodeCount = 0 ;
        this.table = (N[]) new SerialHashNode[capacity] ;
        this.mask = capacity - 1 ;
        this.loadLimit = (int) (capacity * loadFactor) ;
		this.loadFactor = loadFactor ;
	}
	
	protected SerialHashStore(float loadFactor, N[] table, int size) {
		this.totalNodeCount = 0 ;
		this.table = table ;
		this.mask = table.length - 1 ;
		this.loadLimit = (int) (table.length * loadFactor) ;
		this.loadFactor = loadFactor ;
	}
	
	// **************************************************
	// PUBLIC METHODS
	// **************************************************
	
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
		return Iters.toString(all(null, null, Functions.<N>toString())) ;
	}

	// **************************************************
	// public PUT METHODS
	// **************************************************
	
	/**
	 * internal method which will insert the provided node into the hash map, returning the node it replaced (if any)
	 * 
	 * @param put
	 * @return
	 */
	
	@Override
	public <NCmp, V> V put(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		grow() ;
		
		final boolean replace = !eq.isUnique() ;
		final int hash = put.hash & mask ;
    	N n = table[hash] ;
    	if (n == null) {
    		table[hash] = put ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			boolean partial = false ;
    			if (partial != (n.hash == put.hash && eq.prefixMatch(find, n))) {
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
    					table[hash] = put ;
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
		uniquePrefixCount++ ;
		totalNodeCount++ ;

		inserted(put) ;
		return null ;
	}

	/**
	 * internal method which will insert the provided node into the hash map if no equivalent node already exists, 
	 * returning null if the node was inserted and the pre-existing node otherwise.
	 * 
	 * @param put
	 * @return
	 */
	
	@Override
	public <NCmp, V> V putIfAbsent(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		grow() ;

		final int hash = put.hash & mask ;
    	N n = table[hash] ;
    	if (n == null) {
    		table[hash] = put ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			boolean partial = false ;
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
		uniquePrefixCount++ ;
		totalNodeCount++ ;

		inserted(put) ;
		return null ;
	}

	/**
	 * internal method which will look for a node matching the input and return it if found, 
	 * otherwise will insert the node returned by the factory method, returning null in this case
	 * 
	 * @param put
	 * @return
	 */
	
	@Override
	public <NCmp, V> V putIfAbsent(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) {
		grow() ;
		
		hash = hash & mask ;
    	N n = table[hash] ;
    	if (n == null) {
    		table[hash] = factory.makeNode(hash, put) ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			boolean partial = false ;
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
		uniquePrefixCount++ ;
		totalNodeCount++ ;

		return null ;
	}
	
	/**
	 * internal method which will look for a node matching the input and return it if found, 
	 * otherwise will insert the node returned by the factory method; will always return the node
	 * whether it had to be inserted or not
	 * 
	 * @param put
	 * @return
	 */
	
	@Override
	public <NCmp, V> V ensureAndGet(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) {
		grow() ;
		
		hash = hash & mask ;
    	N n = table[hash] ;
    	if (n == null) {
    		n = table[hash] = factory.makeNode(hash, put) ;
    	} else {
    		N p = null ;
    		while (n != null) {
    			boolean partial = false ;
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
		uniquePrefixCount++ ;
		totalNodeCount++ ;

		inserted(n) ;
		return ret.apply(n) ;
	}
	
	// **************************************************
	// public REMOVE METHODS
	// **************************************************
	
	@Override
	public <NCmp> int remove(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {

		final boolean eqIsUniq = eq.isUnique() ;
		boolean partial = false ;
    	hash = hash & mask ;
    	N p = null ;
    	N n = table[hash] ; 
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
    				table[hash] = next ;
    			} else {
    				p.next = next ;
    			}
				n.next = null ;
    			removed(n) ;
				n = next ;
    			totalNodeCount -= 1 ;
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
	public <NCmp> boolean removeExistingNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {
		final int hash = n.hash & mask ;
		N p = table[hash] ;
		if (p == n) {
			table[hash] = p.next ;
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
		return true ;
	}

	@Override
	public <NCmp, V> Iterable<V> removeAndReturn(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return removedNodeIterable(internalRemoveAndReturn(hash, c, eq), ret) ;
	}
	@Override
	public <NCmp, V> V removeAndReturnFirst(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		final N n = internalRemoveAndReturn(hash, c, eq) ;
		return n == null ? null : ret.apply(n) ;
	}
	private <NCmp> N internalRemoveAndReturn(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq) {
		
		final boolean eqIsUniq = eq.isUnique() ;
		boolean partial = false ;
		hash = hash & mask ;
		N p = null ;
		N n = table[hash] ; 
		boolean keptNeighbours = false ;
		N removedHead = null, removedTail = null ;		
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(c, n))) {
				if (partial) {
					if (!keptNeighbours)
						uniquePrefixCount -= 1 ;
					return removedHead ;
				} else partial = true ;
			}
			if (partial && eq.suffixMatch(c, n)) {
				final N next = n.next ;
				if (p == null) {
					table[hash] = next ;
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
    	N n = table[hash & mask] ;
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
		N n = table[hash & mask] ;
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
		N n = table[hash & mask] ;
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

	@Override
	public <NCmp, V> Iterator<V> find(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq, Function<? super N, ? extends NCmp> nodePrefixEqProj, Function<? super N, ? extends V> retProj) {		
		return new Search<NCmp, V>(hash, find, findEq, nodePrefixEqProj, retProj) ;
	}
	
	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqProj, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> retProj) {
		return new AllIterator<NCmp, V>(nodePrefixEq, nodePrefixEqProj, retProj) ;
	}
	
	@Override
	public <NCmp, V> Iterator<V> unique(Function<? super N, ? extends NCmp> proj, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Equality<? super NCmp> forceUniq, Function<? super N, ? extends V> ret) {
		return new UniqueIterator<NCmp, V>(proj, forceUniq, nodePrefixEq, ret) ;
	}
	
	// **************************************************
	// public ITERATOR CLASSES
	// **************************************************
	
	abstract class GeneralIterator<NCmp, V> implements Iterator<V> {
		
		final Function<? super N, ? extends NCmp> eqF ;
		final HashNodeEquality<? super NCmp, ? super N> eq ;
		int curHash , curModCount = modCount ;
		N curPrev , curNode , nextPrev , nextNode ;

		public GeneralIterator(HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends NCmp> eqF) {
			this.eq = eq ;
			this.eqF = eqF ;
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
				table[curHash] = curNode.next ;
				if (nextNode == null || !eq.prefixMatch(eqF.apply(curNode), nextNode))
					uniquePrefixCount -= 1 ;
			} else {
				curPrev.next = curNode.next ;
				final NCmp cmp = eqF.apply(curPrev) ;
				if (!eq.prefixMatch(cmp, curNode) && (nextNode == null || !eq.prefixMatch(cmp, nextNode)))
					uniquePrefixCount -= 1 ;
			}
			removed(curNode) ;
			curPrev = null ;
			curNode = null ;
			totalNodeCount -= 1 ;
			curModCount = modCount++ ;
		}

	}
	
	private class AllIterator<NCmp, V> extends GeneralIterator<NCmp, V> {
		
		final Function<? super N, ? extends V> ret ;
		int nextHash = - 1 ;
		
		AllIterator(HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends NCmp> eqF, Function<? super N, ? extends V> ret) {
			super(eq, eqF) ;
			this.ret = ret ;
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
	
	private class ClearedIterator<V> implements Iterator<V> {
		
		final N[] table ;
		final Function<? super N, ? extends V> f ;
		int nextHash = - 1 ;
		N nextNode ;
		
		ClearedIterator(N[] table, Function<? super N, ? extends V> f) {
			this.table = table ;
			this.f = f ;
			while (nextNode == null & nextHash != table.length - 1) {
				nextNode = table[++nextHash] ;
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
			while (nextNode == null & nextHash != table.length - 1) {
				nextNode = table[++nextHash] ;
			}
			return f.apply(r) ;
		}
		
	}

	private class UniqueIterator<NCmp, V> extends GeneralIterator<NCmp, V> {
		
		final ScalarSet<N> seen ;
		final Function<? super N, ? extends V> ret ;
		
		int nextHash = - 1 ;
		
		UniqueIterator(Function<? super N, ? extends NCmp> eqF, Equality<? super NCmp> uniqEq, HashNodeEquality<? super NCmp, ? super N> nodeEq, Function<? super N, ? extends V> ret) {
			super(nodeEq, eqF) ;
			this.ret = ret ;
			this.seen = new SerialScalarHashSet<N>(8, 2f, Hashers.object(), Rehashers.flipEveryHalfByte(), SerialHashStore.<NCmp, N>nodeEquality(eqF, uniqEq)) ;
			while (nextNode == null & nextHash != table.length - 1) {
				nextNode = table[++nextHash] ;
			}
		}
		
		public V next() {
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			if (nextNode == null)
				throw new NoSuchElementException() ;
			seen.put(nextNode) ;
			curNode = nextNode ;
			curHash = nextHash ;
			curPrev = nextPrev ;
			nextPrev = nextNode ;			
			nextNode = nextNode.next ;
			while (nextNode != null && seen.contains(nextNode)) {				
				nextNode = nextNode.next ;
				while (nextNode == null & nextHash != table.length - 1) {
					nextPrev = nextNode ;
					nextNode = table[++nextHash] ;
				}
			}
			if (nextNode == null || curNode.hash != nextNode.hash || !eq.prefixMatch(eqF.apply(curNode), nextNode))
				seen.clear() ;
			return ret.apply(curNode) ;
		}
		
	}
	
	class Search<NCmp, V> extends GeneralIterator<NCmp, V> {
		
		final NCmp find ;
		final Function<? super N, ? extends V> f ;
		
		Search(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq, Function<? super N, ? extends NCmp> nodeEqFunc, Function<? super N, ? extends V> f) {
			super(findEq, nodeEqFunc) ;
			this.find = find ;
			this.curHash = hash & mask ;
			this.f = f ;
			nextNode = table[curHash] ;
			boolean partial = false ;
			while (nextNode != null) {
				if (partial != (curNode.hash == nextNode.hash && findEq.prefixMatch(find, nextNode))) {
					if (partial) {
						nextNode = null ;
						nextPrev = null ;
						return ;
					} else partial = true ;
				}
				if (partial && findEq.suffixMatch(find, nextNode))
					break ;
			}
		}
		
		public V next() {
			if (curModCount != modCount)
				throw new ConcurrentModificationException() ;
			if (nextNode == null)
				throw new NoSuchElementException() ;
			curNode = nextNode ;
			curPrev = nextPrev ;
			if (eq.isUnique()) {
				nextPrev = nextNode = null ;
			} else {
				nextPrev = nextNode ;
				nextNode = nextNode.next ;
				while (nextNode != null && curNode.hash == nextNode.hash && eq.prefixMatch(find, nextNode) && !eq.suffixMatch(find, nextNode)) {
					nextPrev = nextNode ;
					nextNode = nextNode.next ;
				}
			}
			return f.apply(curNode) ;
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
			mask = table.length - 1 ;
			rehash(oldtable) ;			
		}
	}
	
	public void shrink() {
		int size = table.length ;
		while ((int)(size * loadFactor) > totalNodeCount)
			size >>= 1 ;
		size <<=1 ;
		if (size <= 1)
			size = 2 ;
		if (size < table.length)
			resize(size) ;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void resize(int size) {
        int capacity = 1 ;
        while (capacity < size)
        	capacity <<= 1 ;
        if (capacity != table.length) {
    		N[] oldtable = table ;
    		table = (N[]) new SerialHashNode[capacity] ;
    		loadLimit = (int) (table.length * loadFactor) ;
    		mask = table.length - 1 ;
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
    		final int extraBitCount = Integer.bitCount((oldTable.length - 1) - (table.length - 1)) ;
    		final int extraBitShift = Integer.bitCount(table.length - 1) ;
    		final int[] extraBitArray = new int[extraBitCount] ;
    		for (int i = 0 ; i != extraBitCount ; i++)
    			extraBitArray[i] = i << extraBitShift ;
        	for (int i = 0 ; i != oldTable.length ; i++) {
        		N tail = null ;
        		for (int j = 0 ; j != extraBitCount ; j++) {
            		N node = oldTable[i | extraBitArray[j]] ;
            		while (node != null) {
                		final N next = node.next ;
                		node.next = null ;
                		if (tail == null) {
                    		final int newIndex = node.hash & newTableMask ;
                			table[newIndex] = tail = node ;
                		} else {
                			tail = tail.next = node ;
                		}
            			node = next ;
            		}
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
	public HashStore<N> copy() {
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
		return new SerialHashStore<N>(loadFactor, table, totalNodeCount) ;
	}

	private static <N extends SerialHashNode<N>, V> Iterable<V> removedNodeIterable(N head, Function<? super N, ? extends V> f) {
		return new RemovedNodeIterable<N, V>(head, f) ;
	}
	private static final class RemovedNodeIterable<N extends SerialHashNode<N>, V> implements Iterable<V> {
		private final N head ;
		private final Function<? super N, ? extends V> f ;
		public RemovedNodeIterable(N head, Function<? super N, ? extends V> f) {
			this.head = head;
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

	private static final class NodeEquality<C, N extends SerialHashNode<N>> implements Equality<N> {
		private static final long serialVersionUID = 624143538048396654L ;
		private final Function<? super N, ? extends C> f ;
		private final Equality<? super C> eq ;
		public NodeEquality(Function<? super N, ? extends C> f, Equality<? super C> eq) {
			super() ;
			this.f = f ;
			this.eq = eq ;
		}
		@Override
		public boolean equates(N a, N b) {
			final C ac = f.apply(a) ;
			final C bc = f.apply(b) ;
			return eq.equates(ac, bc) ;
		}
	} ;
	
	private static <C, N extends SerialHashNode<N>> NodeEquality<C, N> nodeEquality(Function<? super N, ? extends C> f, Equality<? super C> eq) {
		return new NodeEquality<C, N>(f, eq) ;
	}
	
}
