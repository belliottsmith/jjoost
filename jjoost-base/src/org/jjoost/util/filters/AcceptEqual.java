package org.jjoost.util.filters ;

import java.util.Comparator ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;

/**
 * A filter accepting only values equal to the one provided, using the provided definition of equality. The filter implements both
 * <code>Filter</code> and <code>FilterPartialOrder</code>.
 */
public class AcceptEqual<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	final E than ;
	final Equality<? super E> equality ;

    /**
	 * Constructs a new filter accepting only values equal to the one provided, using the provided definition of equality. The filter implements both
	 * <code>Filter</code> and <code>FilterPartialOrder</code>.
	 * 
	 * @param than
	 *            value to accept
	 * @param equality
	 *            the definition of equality
	 */
	public AcceptEqual(E than, Equality<? super E> equality) {
		this.than = than ;
		this.equality = equality ;
	}

	public boolean accept(E test) {
		return equality.equates(test, than) ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(test, than) == 0 ;
	}

	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return (lb == null || (cmp.compare(lb, than) < (lbInclusive ? 1 : 0))) && (ub == null || (cmp.compare(than, ub) < (ubInclusive ? 1 : 0))) ;
	}

	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return lb == null || ub == null || (cmp.compare(lb, than) < (lbInclusive ? 0 : 1)) || (cmp.compare(than, ub) < (ubInclusive ? 0 : 1)) ;
	}
	
    /**
	 * Constructs a new filter accepting only values equal to the one provided, using default object equality. The filter implements both
	 * <code>Filter</code> and <code>FilterPartialOrder</code>.
	 * 
	 * @param than
	 *            value to accept
	 */
	public static <E> AcceptEqual<E> get(E than) {
		return new AcceptEqual<E>(than, Equalities.object()) ;
	}

    /**
	 * Constructs a new filter accepting only values equal to the one provided, using the provided definition of equality. The filter implements both
	 * <code>Filter</code> and <code>FilterPartialOrder</code>.
	 * 
	 * @param than
	 *            value to accept
	 * @param equality
	 *            the definition of equality
	 */
	public static <E> AcceptEqual<E> get(E than, Equality<? super E> equality) {
		return new AcceptEqual<E>(than, equality) ;
	}
	
	public String toString() {
		return "equals " + than ;
	}

}
