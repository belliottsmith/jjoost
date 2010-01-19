package org.jjoost.util.filters ;

import org.jjoost.util.Equalities ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Filter ;

/**
 * A <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
 * being tested, using the provided definition of equality.
 */
public class AcceptUniqueSequence<V> implements Filter<V> {

	private static final long serialVersionUID = 4135610622081116945L ;
	private final Equality<? super V> eq ;

    /**
     * Construct a new <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
     * being tested, using regular object equality.
     */
	public AcceptUniqueSequence() {
		this(Equalities.object()) ;
	}

    /**
     * Construct a new <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
     * being tested, using the provided definition of equality.
     */
	public AcceptUniqueSequence(Equality<? super V> eq) {
		this.eq = eq ;
	}

	private V prev = null ;
	public boolean accept(V next) {
		final boolean r = !eq.equates(next, prev) ;
		prev = next ;
		return r ;
	}
	
	public String toString() {
		return "is not preceded by itself" ;
	}

    /**
     * Returns a <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
     * being tested, using regular object equality.
     * 
     * @return a filter rejecting any values equal to their predecessor
     */
	public static <V> AcceptUniqueSequence<V> get() {
		return new AcceptUniqueSequence<V>() ;
	}
	
	/**
	 * Returns a <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
	 * being tested, using the provided definition of equality.
	 * 
	 * @return a filter rejecting any values equal to their predecessor
	 */
	public static <V> AcceptUniqueSequence<V> get(Equality<? super V> eq) {
		return new AcceptUniqueSequence<V>(eq) ;
	}
	
}
