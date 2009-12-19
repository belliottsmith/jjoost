package org.jjoost.collections.base ;

import org.jjoost.util.Function ;

abstract class HashNodeVisits<N extends HashNode<N>, NCmp> {
	
	int hash ;
	public HashNodeVisits(int hash) {
		super() ;
		this.hash = hash ;
	}

	abstract void visit(N n) ;

	abstract void removeLast() ;

	abstract void reset(int hash) ;

	abstract boolean isEmpty() ;

	abstract N last() ;

	abstract void revisit() ;

	abstract boolean haveVisitedAlready(N n, Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) ;

}