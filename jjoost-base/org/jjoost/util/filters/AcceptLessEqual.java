package org.jjoost.util.filters ;

/**
 * A partial order filter that accepts everything less than or equal to the provided value, as determined by the <code>Comparator</code>
 * provided to its methods by utilising classes.
 * <p>
 * This class implements both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
 * methods utilise the provided comparator.
 */
public class AcceptLessEqual<E extends Comparable<? super E>> extends PartialOrderAcceptLessEqual<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

    /**
	 * Constructs a new partial order filter that accepts everything less than oe equal to the provided value, as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparator
	 * 
	 * @param than
	 *            inclusive upper limit of acceptable values
	 */
	public AcceptLessEqual(E than) {
		super(than) ;
	}

	public boolean accept(E test) {
		return test.compareTo(than) <= 0 ;
	}

    /**
	 * Constructs a new partial order filter that accepts everything less than or equal to the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param than
	 *            inclusive upper limit of acceptable values
	 * @return a filter that accepts everything less than or equal to the provided value
	 */
	public static <E extends Comparable<? super E>> AcceptLessEqual<E> get(E than) {
		return new AcceptLessEqual<E>(than) ;
	}

	public String toString() {
		return "is less than or equal to " + than ;
	}

}
