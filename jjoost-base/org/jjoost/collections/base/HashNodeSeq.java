package org.jjoost.collections.base ;

import java.util.Arrays ;

import org.jjoost.util.Function ;

@SuppressWarnings("unchecked")
final class HashNodeSeq<N extends HashNode<N>, NCmp> {
	
	final Function<? super N, ? extends NCmp> nodeEqualityProj ;
	final HashNodeEquality<? super NCmp, ? super N> nodeEquality ;
	private N[] nodes = (N[]) new Object[4] ;
	private int count = 0 ;
	private int i = 0 ;
	int hash ;
	
	HashNodeSeq(int hash, Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		this.hash = hash ;
		this.nodeEqualityProj = nodeEqualityProj ;
		this.nodeEquality = nodeEquality ;
	}

	void visit(N n) {
		if (n.hash != hash)
			throw new IllegalStateException() ;
		if (count == nodes.length)
			nodes = Arrays.copyOf(nodes, count << 1) ;
		nodes[count++] = n ;
	}
	
	void removeLast() {
		count-- ;
	}
	
	void reset(int hash) {
		count = 0 ;
		this.hash = hash ;
	}
	
	public int hash() {
		return hash ;
	}
	
	boolean isEmpty() {
		return count == 0 ;
	}
	
	N last() {
		return nodes[count-1] ;
	}
	
	boolean revisit() {
		if (i == 0)
			return false ;
		i = 0 ;
		return true ;
	}
	
	boolean haveVisitedAlready(N n) {
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
