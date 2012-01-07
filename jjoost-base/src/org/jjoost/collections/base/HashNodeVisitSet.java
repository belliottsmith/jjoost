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

import org.jjoost.util.Function;

@SuppressWarnings("unchecked")
final class HashNodeVisitSet<N extends HashNode<N>, NCmp> {
	
	private static final class Chain<N extends HashNode<N>, NCmp> {
		HashNodeVisits<N, NCmp> visited;
		NCmp key;
		Chain<N, NCmp> next;
		public Chain(int hash, NCmp key, Chain<N, NCmp> next) {
			this.key = key;
			this.next = next;
			this.visited = new HashNodeVisitSeq<N, NCmp>(hash);
		}
		public Chain(int hash, NCmp key, Chain<N, NCmp> next, HashNodeVisitSeq<N, NCmp> reuse) {
			this.key = key;
			this.next = next;
			this.visited = reuse;
			reuse.reset(hash);
		}
	}
	
	final Function<? super N, ? extends NCmp> nodeEqualityProj;
	final HashNodeEquality<? super NCmp, ? super N> nodeEquality;
	private Chain<N, NCmp>[] table = new Chain[8];
	private int c = 0;
	private Chain<N, NCmp> cur;
	private HashNodeVisitSeq<N, NCmp> reuse;
	
	public HashNodeVisitSet(Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		this.nodeEqualityProj = nodeEqualityProj;
		this.nodeEquality = nodeEquality;
	}
	
	HashNodeVisits<N, NCmp> get(N n) {
		
		final int index = Integer.reverse(n.hash) & (table.length - 1);
		
		Chain<N, NCmp> chain = table[index];
		while (chain != null) {
			if (chain.visited.hash == n.hash && nodeEquality.prefixMatch(chain.key, n))
				return chain.visited;
			chain = chain.next;
		}
//		table[index] = last = chain = alloc(n, table[index], last);
		table[index] = cur = chain = alloc(n, table[index]);
		
		if (++c == table.length)
			grow();
		
		return chain.visited;
		
	}
	
	void finishLast() {
		if (cur != null && (cur.visited instanceof HashNodeVisitSeq)) {
			reuse = (HashNodeVisitSeq<N, NCmp>) cur.visited;
			cur.visited = new HashNodeVisitComplete<N, NCmp>(reuse.hash);
			cur = null;
		}
	}
	
	void leaveLast() {
		cur = null;
	}
	
	void grow() {
		
		final Chain<N, NCmp>[] oldTable = table;
		final Chain<N, NCmp>[] newTable = new Chain[oldTable.length << 1];
		
		for (int i = 0 ; i != oldTable.length ; i++) {			
			Chain<N, NCmp> node = oldTable[i];
			while (node != null) {
				
				final Chain<N, NCmp> next = node.next;
				final int newIndex = Integer.reverse(node.visited.hash) & (newTable.length - 1);
				node.next = newTable[newIndex];
				newTable[newIndex] = node;
				node = next;
				
			}
		}
		
	}

	void clear() {
		if (c != 0) {
			java.util.Arrays.fill(table, null);
			c = 0;
			cur = null;
			if (cur.visited instanceof HashNodeVisitSeq) {
				reuse = (HashNodeVisitSeq<N, NCmp>) cur.visited;
			}
		}
	}
	
	Chain<N, NCmp> alloc(N n, Chain<N, NCmp> next) {
		final NCmp key = nodeEqualityProj.apply(n);
		if (reuse == null)
			return new Chain<N, NCmp>(n.hash, key, next);
		final Chain<N, NCmp> r = new Chain<N, NCmp>(n.hash, key, next, reuse);
		reuse = null;
		return r;
	}
	
}
