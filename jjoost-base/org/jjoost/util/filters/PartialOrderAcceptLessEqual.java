package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

public class PartialOrderAcceptLessEqual<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	protected final E than ;

	public PartialOrderAcceptLessEqual(E than) {
		super() ;
		this.than = than ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(test, than) <= 0 ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		final int offset = lbInclusive ? 1 : 0 ;
		return lb == null || cmp.compare(lb, than) < offset ;
	}

	public static <E> PartialOrderAcceptLessEqual<E> get(E than) {
		return new PartialOrderAcceptLessEqual<E>(than) ;
	}

	public String toString() {
		return "is less than " + than ;
	}

}
