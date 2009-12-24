package org.jjoost.util;

import java.io.Serializable;

/**
 * This interface declares a method which defines equality been objects of type <code>E</code>
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public interface Equality<E> extends Serializable {
	
	/**
	 * Returns a boolean indicating if the two parameters are considered equal
	 * by this <code>Equality</code>
	 * 
	 * @param a
	 * @param b
	 * @return true if this equality equates the two arguments 
	 */
    public boolean equates(E a, E b) ;

}
