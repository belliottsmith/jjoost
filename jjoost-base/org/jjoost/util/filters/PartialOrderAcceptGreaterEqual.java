package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

public class PartialOrderAcceptGreaterEqual<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	protected final E than ;

	public PartialOrderAcceptGreaterEqual(E than) {
		super() ;
		this.than = than ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(test, than) >= 0 ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		final int offset = ubInclusive ? 1 : 0 ;
		return ub == null || cmp.compare(ub, than) > offset ;
	}

	public static <E> PartialOrderAcceptGreaterEqual<E> get(E than) {
		return new PartialOrderAcceptGreaterEqual<E>(than) ;
	}

	public String toString() {
		return "is less than " + than ;
	}

}
