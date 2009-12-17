package org.jjoost.collections.base ;

import org.jjoost.util.Function ;

@SuppressWarnings("unchecked")
final class HashNodeSeqs<N extends HashNode<N>, NCmp> {
	
	private static final class Chain<N extends HashNode<N>, NCmp> {
		final HashNodeSeq<N, NCmp> visited ;
		NCmp key ;
		Chain<N, NCmp> next ;
		Chain<N, NCmp> prev ;
		public Chain(int hash, NCmp key, Chain<N, NCmp> next, Chain<N, NCmp> prev,
				Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
			this.key = key ;
			this.next = next ;
			this.prev = prev ;
			this.visited = new HashNodeSeq<N, NCmp>(hash, nodeEqualityProj, nodeEquality) ; 
		}
	}
	
	final Function<? super N, ? extends NCmp> nodeEqualityProj ;
	final HashNodeEquality<? super NCmp, ? super N> nodeEquality ;
	private Chain<N, NCmp>[] table = new Chain[8] ;
	private int c = 0 ;
	private Chain<N, NCmp> last ;
	private Chain<N, NCmp> reuse ;
	
	public HashNodeSeqs(Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		this.nodeEqualityProj = nodeEqualityProj ;
		this.nodeEquality = nodeEquality ;
	}
	
	HashNodeSeq<N, NCmp> get(N n) {
		
		final int index = Integer.reverse(n.hash) & (table.length - 1) ;
		
		Chain<N, NCmp> chain = table[index] ;
		while (chain != null) {
			if (chain.visited.hash == n.hash && nodeEquality.prefixMatch(chain.key, n))
				return chain.visited ;
			chain = chain.next ;
		}
		table[index] = last = chain = alloc(n, table[index], last) ;
		
		if (++c == table.length)
			grow() ;
		
		return chain.visited ;
		
	}
	
	HashNodeSeq<N, NCmp> pop() {
		
		if (last == null)
			return null ;
		table[last.visited.hash & (table.length - 1)] = last.next ;
		last = last.prev ;
		return last.visited ;
		
	}
	
	void grow() {
		
		final Chain<N, NCmp>[] oldTable = table ;
		final Chain<N, NCmp>[] newTable = new Chain[oldTable.length << 1] ;
		
		for (int i = 0 ; i != oldTable.length ; i++) {			
			Chain<N, NCmp> node = oldTable[i] ;
			while (node != null) {
				
				final Chain<N, NCmp> next = node.next ;
				final int newIndex = Integer.reverse(node.visited.hash) & (newTable.length - 1) ;
				node.next = newTable[newIndex] ;
				newTable[newIndex] = node ;
				node = next ;
				
			}
		}
		
	}
	
	boolean isEmpty() {
		return last == null ;
	}
	
	void clear() {
		if (c != 0) {
			java.util.Arrays.fill(table, null) ;
			reuse = last ;
			c = 0 ;
		}
	}
	
	Chain<N, NCmp> alloc(N n, Chain<N, NCmp> next, Chain<N, NCmp> prev) {
		final NCmp key = nodeEqualityProj.apply(n) ;
		if (reuse == null)
			return new Chain<N, NCmp>(n.hash, key, 
					next, prev, nodeEqualityProj, nodeEquality) ;
		Chain<N, NCmp> r = reuse ;
		reuse = r.prev ;
		r.next = next ;
		r.prev = prev ;
		r.key = key ;
		r.visited.reset(n.hash) ;
		return r ;
	}
	
	void revisitAll() {
		Chain<N, NCmp> n = last ;
		while (n != null) {
			n.visited.revisit() ;
			n = n.prev ;
		}
	}
	
}
