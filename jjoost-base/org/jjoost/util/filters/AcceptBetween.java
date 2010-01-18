package org.jjoost.util.filters ;

/**
 * A filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
 * Each bound can be specified as inclusive or exclusive
 */
public class AcceptBetween<E extends Comparable<? super E>> extends PartialOrderAcceptBetween<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

    /**
     * Constructs a new filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
     * Each bound can be specified as inclusive or exclusive
     * 
     * @param lb
     *            lower limit of acceptable values
     * @param lbIsInclusive
     *            <code>true</code> if lb should be inclusive, <code>false</code> if exclusive
     * @param ub
     *            exclusive upper limit of acceptable values
     * @param ubIsInclusive
     *            <code>true</code> if <code>ub</code> should be inclusive, <code>false</code> if exclusive
     */
	public AcceptBetween(E lb, boolean lbIsInclusive, E ub, boolean ubIsInclusive) {
		super(lb, lbIsInclusive, ub, ubIsInclusive) ;
	}

	@Override
	public boolean accept(E test) {
		return (lb == null || lb.compareTo(test) < lbOffsetIfUbInclusive)
			&& (ub == null || test.compareTo(ub) < ubOffsetIfLbInclusive) ;
	}

    /**
	 * Returns a partial order filter that accepts everything greater than or equal to the provided lower bound (first argument) and
	 * everything strictly less than the provided upper bound (second argument), as determined by the <code>Comparator</code> provided to
	 * its methods by utilising classes.
	 * 
	 * @param lb
	 *            inclusive lower limit of acceptable values
	 * @param ub
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything in the range <code>[lb...ub)</code>
	 */
	public static <E extends Comparable<? super E>> AcceptBetween<E> get(E lb, E ub) {
		return get(lb, true, ub, false) ;
	}

    /**
     * Returns a filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
     * Each bound can be specified as inclusive or exclusive
     * 
     * @param lb
     *            lower limit of acceptable values
     * @param lbIsInclusive
     *            <code>true</code> if lb should be inclusive, <code>false</code> if exclusive
     * @param ub
     *            exclusive upper limit of acceptable values
     * @param ubIsInclusive
     *            <code>true</code> if <code>ub</code> should be inclusive, <code>false</code> if exclusive
     * @return a filter that accepts everything in the range <code>[lb...ub)</code>
     */
	public static <E extends Comparable<? super E>> AcceptBetween<E> get(E lb, boolean lbIsInclusive, E ub, boolean ubIsInclusive) {
		return new AcceptBetween<E>(lb, lbIsInclusive, ub, ubIsInclusive) ;
	}

	public String toString() {
		return "is between " + lb + " and " + ub ;
	}

}
