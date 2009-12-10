package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

public class PartialOrderAcceptBetween<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	protected final E lb ;
	protected final E ub ;
	protected final int lbOffsetIfUbInclusive ;
	protected final int ubOffsetIfLbInclusive ;

	public PartialOrderAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive) {
		super() ;
		this.lb = lb ;
		this.ub = ub ;
		lbOffsetIfUbInclusive = lbInclusive ? 1 : 0 ;
		ubOffsetIfLbInclusive = ubInclusive ? 1 : 0 ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return (lb == null || cmp.compare(test, lb) > lbOffsetIfUbInclusive)
				&& (ub == null || cmp.compare(test, ub) < ubOffsetIfLbInclusive) ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return (this.lb == null || ub == null || cmp.compare(this.lb, ub) < (lbInclusive ? lbOffsetIfUbInclusive : 0))
				&& (this.ub == null || lb == null || cmp.compare(lb, this.ub) < (ubInclusive ? ubOffsetIfLbInclusive : 0)) ;
	}

	public static <E> PartialOrderAcceptBetween<E> get(E lb, E ub) {
		return get(lb, true, ub, false) ;
	}

	public static <E> PartialOrderAcceptBetween<E> get(E lb, boolean lbInclusive, E ub, boolean ubInclusive) {
		return new PartialOrderAcceptBetween<E>(lb, lbInclusive, ub, ubInclusive) ;
	}

	public String toString() {
		return "is between " + lb + " and " + ub ;
	}

}
