package org.jjoost.collections.base;

import org.jjoost.util.Function ;

class HashNodeVisitComplete<N extends HashNode<N>, NCmp> extends HashNodeVisits<N, NCmp> {

	public HashNodeVisitComplete(int hash) {
		super(hash) ;
	}

	@Override
	public boolean haveVisitedAlready(N n, Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		return true ;
	}

	@Override
	public boolean isEmpty() {
		return false ;
	}

	@Override
	public N last() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void removeLast() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void reset(int hash) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void revisit() {

	}

	@Override
	public void visit(N n) {
		throw new UnsupportedOperationException() ;
	}
	
}
