package org.jjoost.util.filters ;

/**
 * A partial order filter that accepts everything greater than or equal to the provided value, as determined by the <code>Comparator</code>
 * provided to its methods by utilising classes.
 * <p>
 * The class implements both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
 * methods utilise the provided comparator
 */
public class AcceptGreaterEqual<E extends Comparable<? super E>> extends PartialOrderAcceptGreaterEqual<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	/**
	 * Constructs a partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes.
	 * 
	 * @param than
	 *            inclusive lower limit of acceptable values
	 */
	public AcceptGreaterEqual(E than) {
		super(than) ;
	}

	public boolean accept(E test) {
		return test.compareTo(than) >= 0 ;
	}

	/**
	 * Returns a partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param than
	 *            inclusive lower limit of acceptable values
	 * @return a filter that accepts everything greater than or equal to the provided value
	 */
	public static <E extends Comparable<? super E>> AcceptGreaterEqual<E> get(E than) {
		return new AcceptGreaterEqual<E>(than) ;
	}

	public String toString() {
		return "is greater or equal to " + than ;
	}

}
