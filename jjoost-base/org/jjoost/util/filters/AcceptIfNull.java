package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

/**
 * A filter accepting only values that are null
 * 
 * @author b.elliottsmith
 */
public class AcceptIfNull<E> implements Filter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	public boolean accept(E test) {
		return test == null ;
	}

	public String toString() {
		return "is null" ;
	}
	
    /**
     * Returns a filter accepting only values that are null
     * 
     * @return a filter accepting only values that are null
     */
	public static <E> AcceptIfNull<E> get() {
		return new AcceptIfNull<E>() ;
	}

}
