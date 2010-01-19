package org.jjoost.collections.base ;

import java.util.Arrays ;

import org.jjoost.util.Function ;

@SuppressWarnings("unchecked")
final class HashNodeVisitSeq<N extends HashNode<N>, NCmp> extends HashNodeVisits<N, NCmp> {
	
	private N[] nodes = (N[]) new Object[4] ;
	private int count = 0 ;
	private int i = 0 ;
	
	HashNodeVisitSeq(int hash) {
		super(hash) ;
	}

	public void visit(N n) {
		if (n.hash != hash)
			throw new IllegalStateException() ;
		if (count == nodes.length)
			nodes = Arrays.copyOf(nodes, count << 1) ;
		nodes[count++] = n ;
	}
	
	public void removeLast() {
		count-- ;
	}
	
	public void reset(int hash) {
		count = 0 ;
		this.hash = hash ;
	}
	
	public int hash() {
		return hash ;
	}
	
	public boolean isEmpty() {
		return count == 0 ;
	}
	
	public N last() {
		return nodes[count-1] ;
	}
	
	public void revisit() {
		i = 0 ;
	}
	
	public boolean haveVisitedAlready(N n, Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		if (i == count)
			return false ;
		final int lasti = i ;
		final NCmp c = nodeEqualityProj.apply(n) ;
		while (i != count && 
				!(nodeEquality.prefixMatch(c, nodes[i]) 
				&& nodeEquality.suffixMatch(c, nodes[i]))) {
			i++ ;
		}
		if (i == count) {
			count = lasti ;
			return false ;
		} else {
			i++ ;
			return true ;
		}
	}
	
}
