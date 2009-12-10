package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

public class PartialOrderAcceptGreater<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	protected final E than ;

	public PartialOrderAcceptGreater(E than) {
		super() ;
		this.than = than ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(test, than) > 0 ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return ub == null || cmp.compare(ub, than) > 0 ;
	}

	public static <E> PartialOrderAcceptGreater<E> get(E than) {
		return new PartialOrderAcceptGreater<E>(than) ;
	}

	public String toString() {
		return "is less than " + than ;
	}

}
