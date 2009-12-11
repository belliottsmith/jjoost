package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Objects ;

/**
 * a simple filter that accepts only objects that equal that provided in its
 * constructor
 * 
 * @author b.elliottsmith
 * @param <E>
 */

public class AcceptEqual<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	protected final E than ;

	public AcceptEqual(E than) {
		this.than = than ;
	}

	public boolean accept(E test) {
		return Objects.equalQuick(test, than) ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(test, than) == 0 ;
	}

	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		final int lbOffset = lbInclusive ? 1 : 0 ;
		final int ubOffset = ubInclusive ? 1 : 0 ;
		return (lb == null || (cmp.compare(lb, than) < lbOffset)) && (ub == null || (cmp.compare(than, ub) < ubOffset)) ;
	}

	public static <E> AcceptEqual<E> get(E than) {
		return new AcceptEqual<E>(than) ;
	}

	public String toString() {
		return "equals " + than ;
	}

}
