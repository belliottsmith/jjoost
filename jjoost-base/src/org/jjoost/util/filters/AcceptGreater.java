package org.jjoost.util.filters ;

/**
 * A partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
 * provided to its methods by utilising classes.
 * <p>
 * The class implements both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
 * methods utilise the provided comparator
 */
public class AcceptGreater<E extends Comparable<? super E>> extends PartialOrderAcceptGreater<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	/**
	 * Constructs a new partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param than
	 *            exclusive upper limit of acceptable values
	 */
	public AcceptGreater(E than) {
		super(than) ;
	}

	public boolean accept(E test) {
		return test.compareTo(than) > 0 ;
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
	 *            exclusive lower limit of acceptable values
	 * @return a filter that accepts everything greater than the provided value
	 */
	public static <E extends Comparable<? super E>> AcceptGreater<E> get(E than) {
		return new AcceptGreater<E>(than) ;
	}

	public String toString() {
		return "is greater than " + than ;
	}

}
