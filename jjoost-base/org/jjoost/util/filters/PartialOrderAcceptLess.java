package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

public class PartialOrderAcceptLess<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	protected final E than ;

	public PartialOrderAcceptLess(E than) {
		super() ;
		this.than = than ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(test, than) < 0 ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return lb == null || cmp.compare(lb, than) < 0 ;
	}

	public static <E> PartialOrderAcceptLess<E> get(E than) {
		return new PartialOrderAcceptLess<E>(than) ;
	}

	public String toString() {
		return "is less than " + than ;
	}

}
