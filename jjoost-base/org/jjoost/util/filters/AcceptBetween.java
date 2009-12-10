package org.jjoost.util.filters ;

public class AcceptBetween<E extends Comparable<? super E>> extends PartialOrderAcceptBetween<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	public AcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive) {
		super(lb, lbInclusive, ub, ubInclusive) ;
	}

	@Override
	public boolean accept(E test) {
		return (lb == null || lb.compareTo(test) < lbOffsetIfUbInclusive)
			&& (ub == null || test.compareTo(ub) < ubOffsetIfLbInclusive) ;
	}

	public static <E extends Comparable<? super E>> AcceptBetween<E> get(E lb, E ub) {
		return get(lb, true, ub, false) ;
	}

	public static <E extends Comparable<? super E>> AcceptBetween<E> get(E lb, boolean lbInclusive, E ub, boolean ubInclusive) {
		return new AcceptBetween<E>(lb, lbInclusive, ub, ubInclusive) ;
	}

	public String toString() {
		return "is between " + lb + " and " + ub ;
	}

}
