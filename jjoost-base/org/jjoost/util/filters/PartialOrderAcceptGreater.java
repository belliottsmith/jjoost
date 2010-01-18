package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * A partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
 * provided to its methods by utilising classes
 */
public class PartialOrderAcceptGreater<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	final E than ;

    /**
	 * Constructs a new partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param than
	 *            exclusive lower limit of acceptable values
	 */
	public PartialOrderAcceptGreater(E than) {
		super() ;
		this.than = than ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(than, test) > 0 ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return ub == null || cmp.compare(than, ub) > 0 ;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		final int offset = lbInclusive ? 1 : 0 ;
		return lb == null || cmp.compare(lb, than) < offset ;
	}
	
    /**
	 * Returns a partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param than
	 *            exclusive lower limit of acceptable values
	 * @return a partial order filter that accepts everything greater than the provided value
	 */
	public static <E> PartialOrderAcceptGreater<E> get(E than) {
		return new PartialOrderAcceptGreater<E>(than) ;
	}

	public String toString() {
		return "is less than " + than ;
	}

}
