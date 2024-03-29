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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jjoost.util.Equality;
import org.jjoost.util.Filter;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;

public abstract class AbstractSerialHashStore<N extends AbstractSerialHashStore.SerialHashNode<N>, S extends AbstractSerialHashStore<N, S>> implements HashStore<N, S> {

	private static final long serialVersionUID = 5818748848600569496L;
	private static final FlagNode DELETED_FLAG = new FlagNode("DELETED");

	public static abstract class SerialHashNode<N extends SerialHashNode<N>> extends HashNode<N> {
		private static final long serialVersionUID = 2035712133283347382L;
		protected N next;
		public SerialHashNode(int hash) {
			super(hash);
		}
		@SuppressWarnings("unchecked")
		void flagDeleted() {
			next = (N) DELETED_FLAG;
		}
	}
	
	protected N[] table;
	protected int totalNodeCount;
	protected int uniquePrefixCount;
	protected int loadLimit;
	protected final float loadFactor;
	
	protected void inserted(N n) {
	}
	protected void reinserted(N n) {
	}
	protected void removed(N n) {
	}
	
	@SuppressWarnings("unchecked")
	public AbstractSerialHashStore(int size, float loadFactor) {
        int capacity = 8;
        while (capacity < size)
        	capacity <<= 1;
        this.totalNodeCount = 0;
        this.table = (N[]) new SerialHashNode[capacity];
        this.loadLimit = (int) (capacity * loadFactor);
		this.loadFactor = loadFactor;
	}
	
	protected AbstractSerialHashStore(float loadFactor, N[] table, int totalNodeCount, int uniquePrefixCount) {
		this.totalNodeCount = totalNodeCount;
		this.uniquePrefixCount = uniquePrefixCount;
		this.table = table;
		this.loadLimit = (int) (table.length * loadFactor);
		this.loadFactor = loadFactor;
	}
	
	// **************************************************
	// PUBLIC METHODS
	// **************************************************
	
	@Override
	public int capacity() {
		return table.length;
	}
	
	@Override
    public int totalCount() {
    	return totalNodeCount;
    }
    
	@Override
	public int uniquePrefixCount() {
		return uniquePrefixCount;
	}
	
	@Override
	public boolean isEmpty() {
		return totalNodeCount == 0;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public int clear() {
		final int r = totalNodeCount;
		totalNodeCount = 0;
		uniquePrefixCount = 0;
		table = (N[]) new SerialHashNode[table.length];
		return r;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f) {
		final Iterator<V> r = new ClearedIterator<V>(table, f);
		totalNodeCount = 0;
		uniquePrefixCount = 0;
		table = (N[]) new SerialHashNode[table.length];
		return r;
	}
	
	@Override
	public String toString() {
		return "{" + Iters.toString(all(null, null, Functions.<N>toString(true)), ", ") + "}";
	}

	// **************************************************
	// public PUT METHODS
	// **************************************************
	
	@Override
	public <NCmp, V> V put(PutAction action, NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		assert (action != PutAction.ENSUREANDGET);
		grow();
		
		final boolean mayReplace = eq.isUnique();
		final int hash = put.hash;
		final int bucket = put.hash & (table.length - 1);
		final int reverse = Integer.reverse(hash);
		boolean partial = false;
		
   		N p = null;
    	N n = table[bucket];
    	N toReplace = null;
   		while (n != null) {
   			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
   				if (partial) break;
   				else partial = true;
   			}
   			if (partial) {
   				if (mayReplace && eq.suffixMatch(find, n)) {
   	   				toReplace = n;
   	   				break;
   				}   				
   			}
			if (HashNode.insertBefore(reverse, n)) 
    			break;
   			p = n;
   			n = n.next;
   		}
   		
   		if (toReplace == null) {
   			if (action == PutAction.REPLACE) {
   				return null;
   			}
   		} else {
   			if (action == PutAction.IFABSENT) {
   				return ret.apply(toReplace);
   			}
   		}
   		
   		if (p == null) { 
   			table[bucket] = put;
   		} else {
   			p.next = put;
		}
   		
		if (toReplace == null) {
			totalNodeCount++;
			put.next = n;
		} else {
			removed(toReplace);
			put.next = toReplace.next;
			// set the next pointer of the removed node to the node that replaces it, so that iterators see consistent state
			toReplace.next = put;
		}
    	if (!partial) {
    		uniquePrefixCount++;
    	}

		inserted(put);
		if (toReplace == null) {
			return null;
		}
		return ret.apply(toReplace);
	}

	@Override
	public <NCmp, V> V put(PutAction action, final int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) {
		grow();
		
		final boolean mayReplace = eq.isUnique();
		final int bucket = hash & (table.length - 1);
		final int reverse = Integer.reverse(hash);
		boolean partial = false;
		
   		N p = null;
    	N n = table[bucket];
    	N toReplace = null;
   		while (n != null) {
   			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
   				if (partial) break;
   				else partial = true;
   			}
   			if (partial) {
   				if (mayReplace && eq.suffixMatch(find, n)) {
   	   				toReplace = n;
   	   				break;
   				}   				
   			}
			if (HashNode.insertBefore(reverse, n)) 
    			break;
   			p = n;
   			n = n.next;
   		}
   		
   		N r, put;
		switch (action) {
		case IFABSENT:
			if (toReplace != null) {
				return ret.apply(toReplace);
			}
			put = factory.makeNode(hash, find);
			r = null;
			break;
		case ENSUREANDGET:
			if (toReplace != null) {
				return ret.apply(toReplace);
			}
			put = factory.makeNode(hash, find);
			r = put;
			break;
		case REPLACE:
   			if (toReplace == null) {
   				return null;
   			}
		case PUT:
			r = toReplace;
			put = factory.makeNode(hash, find);
			break;
		default:
			throw new IllegalStateException();
		}
   		
   		if (p == null) { 
   			table[bucket] = put;
   		} else {
   			p.next = put;
		}
   		
		if (toReplace == null) {
			totalNodeCount++;
			put.next = n;
		} else {
			removed(toReplace);
			put.next = toReplace.next;
			// set the next pointer of the removed node to the node that replaces it, so that iterators see consistent state
			toReplace.next = put;
		}
    	if (!partial) {
    		uniquePrefixCount++;
    	}

		inserted(put);
		if (r == null) {
			return null;
		}
		return ret.apply(r);
	}
	
	// **************************************************
	// public REMOVE METHODS
	// **************************************************
	
	@Override
	public <NCmp> int remove(final int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return 0;
			throw new IllegalArgumentException("Cannot remove less than zero elements");
		}
		final boolean eqIsUniq = eq.isUnique();
    	final int bucket = hash & (table.length - 1);
    	final int reverse = Integer.reverse(hash);
		boolean partial = false;
    	N p = null;
    	N n = table[bucket];
    	int r = 0;
    	boolean keptNeighbours = false;
    	while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
				if (partial) break;
				else partial = true;
			}
    		if (partial && eq.suffixMatch(find, n)) {
    			// removing n
    			r++;
    			final N next = n.next;
    			if (p == null) {
    				table[bucket] = next;
    			} else {
    				p.next = next;
    			}
    			n.flagDeleted();
    			removed(n);
				n = next;
    			totalNodeCount -= 1;
    			if (eqIsUniq | (r == removeAtMost)) {
    				if (!keptNeighbours)
    					keptNeighbours = n != null 
    						&& n.hash == hash 
    						&& eq.prefixMatch(find, n);
    				break;
    			}
    		} else if (partial) {
    			keptNeighbours = true;
    			if (eqIsUniq)
    				break;
    		} else if (HashNode.insertBefore(reverse, n)) {
    			break;
    		} else {
    			p = n;
    			n = p.next;
    		}
    	}
    	
    	if (!keptNeighbours && r != 0)
    		uniquePrefixCount -= 1;
		return r;
	}

	@Override
	public <NCmp> boolean removeNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {
		final int bucket = n.hash & (table.length - 1);
		N p = table[bucket];
		if (p == n) {
			table[bucket] = p.next;
			if (p.next == null || !nodePrefixEq.prefixMatch(nodePrefixEqFunc.apply(n), p.next))
				uniquePrefixCount -= 1;
		} else {
			while (p != null && p.next != n)
				p = p.next;
			if (p == null)
				return false;
			p.next = n.next;
			final NCmp cmp = nodePrefixEqFunc.apply(n);
			if (!nodePrefixEq.prefixMatch(cmp, p) && (p.next == null || !nodePrefixEq.prefixMatch(cmp, p.next)))
				uniquePrefixCount -= 1;
		}
		n.flagDeleted();
		removed(n);
		totalNodeCount -= 1;
		return true;
	}

	@Override
	public <NCmp, V> V removeAndReturnFirst(final int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return null;
			throw new IllegalArgumentException("Cannot remove less than zero elements");
		}
		
		final boolean eqIsUniq = eq.isUnique();
		boolean partial = false;
		final int bucket = hash & (table.length - 1);
    	final int reverse = Integer.reverse(hash);
		N p = null;
		N n = table[bucket];
		boolean keptNeighbours = false;
		N removed = null;
		int c = 0;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
				if (partial) break;
				else partial = true;
			}
			if (partial && eq.suffixMatch(find, n)) {
				c++;
				final N next = n.next;
				if (p == null) {
					table[bucket] = next;
				} else {
					p.next = next;
				}
				if (removed == null)
					removed = n;
				n.flagDeleted();
				totalNodeCount -= 1;
				removed(n);
				n = next;
				if (eqIsUniq | (c == removeAtMost)) {
    				if (!keptNeighbours)
    					keptNeighbours = n != null 
    						&& n.hash == hash 
    						&& eq.prefixMatch(find, n);
    				break;
				}
			} else if (partial) {
				keptNeighbours = true;
				if (eqIsUniq)
					break;
    		} else if (HashNode.insertBefore(reverse, n)) {
    			break;
    		} else {
				p = n;
				n = p.next;
			}
		}
		
		if (removed != null) {
			if (!keptNeighbours)
				uniquePrefixCount -= 1;
			return ret.apply(removed);
		}
		
		return null;
	}
	
	public <NCmp, V> Iterable<V> removeAndReturn(final int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return Collections.emptyList();
			throw new IllegalArgumentException("Cannot remove less than zero elements");
		}
		
		final boolean eqIsUniq = eq.isUnique();
		boolean partial = false;
		final int bucket = hash & (table.length - 1);
    	final int reverse = Integer.reverse(hash);
		N p = null;
		N n = table[bucket];
		boolean keptNeighbours = false;
		N removedHead = null, removedTail = null;
		int c = 0;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
				if (partial) break;
				else partial = true;
			}
			if (partial && eq.suffixMatch(find, n)) {
				c++;
				final N next = n.next;
				if (p == null) {
					table[bucket] = next;
				} else {
					p.next = next;
				}
				if (removedHead == null) {
					removedHead = removedTail = n.copy();
				} else {
					removedTail = removedTail.next = n.copy();
				}
				n.flagDeleted();
				totalNodeCount -= 1;
				removed(n);
				n = next;
				if (eqIsUniq | (c == removeAtMost)) {
    				if (!keptNeighbours)
    					keptNeighbours = n != null 
    						&& n.hash == hash 
    						&& eq.prefixMatch(find, n);
    				break;
				}
			} else if (partial) {
				keptNeighbours = true;
				if (eqIsUniq)
					break;
    		} else if (HashNode.insertBefore(reverse, n)) {
    			break;
			} else {
				p = n;
				n = p.next;
			}
		}
		
		if (!keptNeighbours && removedHead != null)
			uniquePrefixCount -= 1;
		
		return removedNodeIterable(removedHead, ret);
	}
	
	// **************************************************
	// public MEMBERSHIP METHODS
	// **************************************************
	
	@Override
	public <NCmp> int count(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq, int countUpTo) {
		if (countUpTo < 1)
			return 0;
		final boolean stopAtOne = eq.isUnique() | countUpTo == 1;
		boolean partial = false;
		int count = 0;
		N n = table[hash & (table.length - 1)];
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(c, n))) {
				if (partial) return count;
				else partial = true;
			}
			if (partial && eq.suffixMatch(c, n)) {
				if (stopAtOne)
					return 1;
				count += 1;
				if (countUpTo == count)
					return count;
			}
			n = n.next;
		}
		return count;
	}
	
	// **************************************************
	// public RETRIEVAL METHODS
	// **************************************************
	
	@Override
	public <NCmp, V> V first(int hash, NCmp c, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		final int reverse = Integer.reverse(hash);
		boolean partial = false;
		N n = table[hash & (table.length - 1)];
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(c, n))) {
				if (partial) return null;
				else partial = true;
			}
			if (partial && eq.suffixMatch(c, n))
				return ret.apply(n);
			if (HashNode.insertBefore(reverse, n))
				break;
			n = n.next;
		}
		return null;
	}

	@Override
	public <NCmp, V> List<V> findNow(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq,
			Function<? super N, ? extends V> ret) {
		return Iters.toList(find(hash, find, findEq, null, null, ret));
	}
	
	@Override
	public <NCmp, NCmp2, V> Iterator<V> find(
			int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> findEq, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEq, 
			Function<? super N, ? extends V> ret) {		
		return new SearchIterator<NCmp, NCmp2, V>(hash, find, findEq, nodeEqualityProj, nodeEq, ret);
	}
	
	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqProj, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> retProj) {
		return new AllIterator<NCmp, V>(nodePrefixEqProj, nodePrefixEq, retProj);
	}
	
	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj, 
			Equality<? super NCmp> uniquenessEquality, 
			Locality duplicateLocality, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		final Filter<N> filter = HashStore.Helper.forUniqueness(uniquenessEqualityProj, uniquenessEquality, duplicateLocality);
		return new UniqueIterator<NCmp, NCmp2, V>(filter, nodeEqualityProj, nodeEquality, ret);
	}
	
	@SuppressWarnings("rawtypes")
	private static final class FlagNode extends SerialHashNode {
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

	// **************************************************
	// public ITERATOR CLASSES
	// **************************************************
	
	abstract class AbstractIterator<NCmp, V> extends AbstractHashNodeIterator<N, NCmp, V> {

		public AbstractIterator(
				Function<? super N, ? extends NCmp> nodeEqualityProj,
				HashNodeEquality<? super NCmp, ? super N> nodeEquality,
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret);
		}
		
		@Override
		protected void delete(N[] nodes, int node) {
			int prev = node;
			while (prev != 0) {
				prev -= 1;
				if (nodes[prev].next == nodes[node]) {
					nodes[prev].next = nodes[node].next;
					nodes[node].flagDeleted();
					removed(nodes[node]);
					return;
				}
			}
			removeNode(nodeEqualityProj, nodeEquality, nodes[node]);
		}
		
		@Override
		protected boolean isDeleted(N[] prevs, int prev, N[] nodes, int node) {
			return nodes[node].next == DELETED_FLAG;
		}
		
	}
	
	final class SearchIterator<NCmp, NCmp2, V> extends AbstractIterator<NCmp2, V> {
		
		@SuppressWarnings("unchecked")
		SearchIterator(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq, 
				Function<? super N, ? extends NCmp2> nodeEqualityProj,
				HashNodeEquality<? super NCmp2, ? super N> nodeEquality,
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret);
			N[] matches = (N[]) new SerialHashNode[4];
			int count = 0;
			N n = table[hash & (table.length - 1)];
			boolean partial = false;
			while (n != null) {
				if (partial != (hash == n.hash && findEq.prefixMatch(find, n))) {
					if (partial) break;
					else partial = true;
				}
				if (partial && findEq.suffixMatch(find, n)) {
					if (count == matches.length)
						matches = Arrays.copyOf(matches, matches.length << 1);
					matches[count++] = n;
				}
				n = n.next;
			}
			if (count == 0) {
				this.nextNodes = null;
				this.nextNodesCount = 0;
			} else {
				this.nextNodes = matches;
				this.nextNodesCount = count;
			}
		}

		@Override
		protected boolean accept(N node) {
			return true;
		}

		@Override
		protected void nextHash(N[] prevs, int prev) {
			nextNodes = null;
		}
		
	}
	
	class AllIterator<NCmp, V> extends AbstractIterator<NCmp, V> {

		final HashIter32Bit iter;
		AllIterator(Function<? super N, ? extends NCmp> nodeEqualityProj,
				HashNodeEquality<? super NCmp, ? super N> nodeEquality,
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret);
			
			final int numTotalBits = Integer.bitCount(table.length - 1);
			if (numTotalBits >= 8) iter = new HashIter32Bit(8, numTotalBits);
			else iter = new HashIter32Bit(numTotalBits, numTotalBits);
			nextHash(null, -1);
		}
		
		@Override
		protected boolean accept(N node) {
			return true;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void nextHash(N[] prevs, int prev) {
			boolean resized = false;
			if (iter.size() != table.length) {
				resized = true;
				iter.resize(Integer.bitCount(table.length - 1));
			}
			N n = null;
			if (prevs == null) {
				// should only be executed on first call
				n = table[iter.current()];
			} else {
				
				while (n == null & prev >= 0) {
					if (prevs[prev].next != DELETED_FLAG) {
						n = prevs[prev];
						break;
					}
					prev--;
				}
				
				if (n == null || (resized && n != null && !iter.correctBucket(n.hash)))
					n = table[iter.current()];
				else if (n != null) 
					n = n.next;
			}
			
			int hash = 0;
			while (true) {
				while (n != null) {
					if (iter.visit(n.hash)) {
						hash = n.hash;
						break;
					}
					n = n.next;
				}
				if (n != null)
					break;
				if (!iter.next())
					break;
				n = table[iter.current()];
			}
			
			if (n == null) {
				reuse = nextNodes = null;
				return;
			}
			N[] nodes;
			if (reuse != null) nodes = reuse;
			else nodes = (N[]) new SerialHashNode[2];
			int c = 0;
			while (n != null && n.hash == hash) {
				if (c == nodes.length)
					nodes = Arrays.copyOf(nodes, nodes.length << 1);
				nodes[c++] = n;
				n = n.next;
			}
			this.nextNodes = nodes;
			this.nextNodesCount = c;
		}
		
	}
	
	final class UniqueIterator<NCmp, NCmp2, V> extends AllIterator<NCmp2, V> {
		
		final Filter<N> filter;
		
		UniqueIterator(
				Filter<N> filter, 
				Function<? super N, ? extends NCmp2> nodeEqualityProj, 
				HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret);
			this.filter = filter;
		}
		
		@Override
		protected final boolean accept(N node) {
			return filter.accept(node);
		}
		
	}
	
	private class ClearedIterator<V> implements Iterator<V> {
		
		final N[] table;
		final Function<? super N, ? extends V> f;
		int nextBucket = - 1;
		N nextNode;
		
		ClearedIterator(N[] table, Function<? super N, ? extends V> f) {
			this.table = table;
			this.f = f;
			while (nextNode == null & nextBucket != table.length - 1) {
				nextNode = table[++nextBucket];
			}
		}
		
		public boolean hasNext() {
			return nextNode != null;
		}
		
		public void remove() {
			// no op - already removed
		}

		public V next() {
			if (nextNode == null)
				throw new NoSuchElementException();
			N r = nextNode;
			nextNode = nextNode.next;
			while (nextNode == null & (nextBucket != table.length - 1)) {
				nextNode = table[++nextBucket];
			}
			return f.apply(r);
		}
		
	}

	protected static final class NodeIterable<N extends SerialHashNode<N>, V> implements Iterable<V> {
		private final N head;
		private final Function<? super N, ? extends V> f;
		public NodeIterable(N head, Function<? super N, ? extends V> f) {
			this.head = head;
			this.f = f;
		}
		@Override
		public Iterator<V> iterator() {
			return new NodeIterator<N, V>(head, f);
		}		
	}
	
	protected static final class NodeIterator<N extends SerialHashNode<N>, V> implements Iterator<V> {
		private N cur;
		private final Function<? super N, ? extends V> f;
		public NodeIterator(N head, Function<? super N, ? extends V> f) {
			this.cur = head;
			this.f = f;
		}
		@Override
		public boolean hasNext() {
			return cur != null;
		}
		@Override
		public V next() {
			if (cur == null)
				throw new NoSuchElementException();
			final V r = f.apply(cur);
			cur = cur.next;
			return r;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	// **************************************************
	// PRIVATE METHODS
	// **************************************************

	@SuppressWarnings("unchecked")
	private void grow() {
		if (uniquePrefixCount >= loadLimit) {
			N[] oldtable = table;
			table = (N[]) new SerialHashNode[table.length << 1];
			loadLimit = (int) (table.length * loadFactor);
			rehash(oldtable);
		}
	}
	
//	public void shrink() {
//		int size = table.length;
//		while ((int)(size * loadFactor) > uniquePrefixCount)
//			size >>= 1;
//		size <<= 1;
//		if (size <= 1)
//			size = 2;
//		if (size < table.length)
//			resize(size);
//	}
//	@SuppressWarnings("unchecked")
//	@Override
//	public void resize(int size) {
//        int capacity = 8;
//        while (capacity < size)
//        	capacity <<= 1;
//        if (capacity != table.length) {
//    		N[] oldtable = table;
//    		table = (N[]) new SerialHashNode[capacity];
//    		loadLimit = (int) (table.length * loadFactor);
//    		rehash(oldtable);
//        }
//	}
	
	/**
	 * This method makes use of the fact that both tables are a power of 2 in size. It maintains the ordering of nodes within groups where matcher.partialMatch() == 1; grows fully maintain ordering of nodes
	 *  
	 * @param oldTable
	 */
    @SuppressWarnings("unchecked")
	private void rehash(N[] oldTable) {
    	// @@ TEST
    	final N[] table = this.table;
    	if (oldTable.length > table.length) {
    		final int newTableMask = table.length - 1;
    		for (int i = 0 ; i != oldTable.length ; i++) {
    			final int newTableIndex = i & newTableMask;
    			N newNode = oldTable[i];
    			N oldNode = table[newTableIndex];
    			final N newHead;
    			N newTail;
    			if (oldNode == null) {
    				if (newNode == null)
    					continue;
    				newHead = newTail = newNode;
    				newNode = newNode.next;
    			} else if (newNode != null && HashNode.insertBefore(newNode, oldNode)) {
    				newHead = newTail = newNode;
    				newNode = newNode.next;
    			} else {
    				newHead = newTail = oldNode;
    				oldNode = oldNode.next;
    			}
    			while (oldNode != null & newNode != null) {
    				if (HashNode.insertBefore(oldNode, newNode)) {
    					newTail = newTail.next = oldNode;
    					oldNode = oldNode.next;
    				} else {
    					newTail = newTail.next = newNode;
    					newNode = newNode.next;
    				}
    			}
    			if (oldNode != null)
    				newTail.next = oldNode;
    			if (newNode != null)
    				newTail.next = newNode;
    			table[newTableIndex] = newHead;
    		}
    	} else if (table.length == oldTable.length << 1) { 
    		int newIndexBit = oldTable.length;
        	for (int i = 0 ; i != oldTable.length ; i++) {
        		N tail1 = null, tail2 = null;
        		N node = oldTable[i];
        		while (node != null) {
            		final N next = node.next;
            		node.next = null;
            		if ((node.hash & newIndexBit) == 0) {
            			if (tail1 == null) {
            				tail1 = table[i] = node;
            			} else {
            				tail1 = tail1.next = node;
            			}
            		} else {
            			if (tail2 == null) {
            				tail2 = table[i | newIndexBit] = node;
            			} else {
            				tail2 = tail2.next = node;
            			}
            		}
        			node = next;
        		}
        	}
    	} else {
    		final int tailShift = Integer.bitCount(oldTable.length - 1);
    		final int newTableMask = table.length - 1;
    		final N[] tails = (N[]) new SerialHashNode[table.length >> tailShift];
    		boolean flushNext = false;
        	for (int i = 0 ; i != oldTable.length ; i++) {
        		if (flushNext) 
        			Arrays.fill(tails, null);
        		N node = oldTable[i];
        		flushNext = node != null;
        		while (node != null) {
            		final N next = node.next;
            		node.next = null;
            		final int newIndex = node.hash & newTableMask;
            		final int tail = newIndex >> tailShift;
            		if (tails[tail] == null) {
            			table[newIndex] = tails[tail] = node;
            		} else {
            			tails[tail] = tails[tail].next = node;
            		}
        			node = next;
        		}
        	}
    	}
    }

	private static <N extends SerialHashNode<N>, V> Iterable<V> removedNodeIterable(N head, Function<? super N, ? extends V> f) {
		return new NodeIterable<N, V>(head, f);
	}
	public static Rehasher defaultRehasher() {
		return Rehashers.jdkHashmapRehasher();
	}
	
}
