package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * S partial order filter that accepts everything greater than or equal to the provided value, as determined by the <code>Comparator</code>
 * provided to its methods by utilising classes
 */
public class PartialOrderAcceptGreaterEqual<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	final E than ;

    /**
	 * Constructs a new partial order filter that accepts everything greater than or equal to the provided value, as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param than
	 *            inclusive lower limit of acceptable values
	 */
	public PartialOrderAcceptGreaterEqual(E than) {
		super() ;
		this.than = than ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(than, test) >= 0 ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		final int offset = ubInclusive ? -1 : 0 ;
		return ub == null || cmp.compare(than, ub) > offset ;
	}
	
	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return lb == null || cmp.compare(lb, than) < 0 ;
	}
	
    /**
	 * Returns a partial order filter that accepts everything greater than or equal to the provided value, as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param than
	 *            inclusive lower limit of acceptable values
	 * @return a partial order filter that accepts everything greater than or equal to the provided value
	 */
    public static <E> PartialOrderAcceptGreaterEqual<E> get(E than) {
		return new PartialOrderAcceptGreaterEqual<E>(than) ;
	}

	public String toString() {
		return "is less than " + than ;
	}

}
