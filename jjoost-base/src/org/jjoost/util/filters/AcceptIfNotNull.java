package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

/**
 * A filter accepting everything except null
 * 
 * @author b.elliottsmith
 */
public class AcceptIfNotNull<E> implements Filter<E> {

	private static final long serialVersionUID = -6305241848003882379L ;

	public boolean accept(E test) {
		return test != null ;
	}

	public String toString() {
		return "is not null" ;
	}
	
    /**
     * Returns a filter accepting everything except null
     * 
     * @return a filter accepting everything except null
     */
	public static <E> AcceptIfNotNull<E> get() {
		return new AcceptIfNotNull<E>() ;
	}

}
