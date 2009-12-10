package org.jjoost.util;

import java.io.Serializable;

/**
 * A simple class that can be used to define equality over objects, and hence can be used to define
 * custom equalities for sets, maps and the like
 * 
 * @author Benedict Elliott Smith
 *
 * @param <E>
 */
public interface Equality<E> extends Serializable {
	
	/**
	 * @param a
	 * @param b
	 * @return true if this equality equates the two arguments 
	 */
    public boolean equates(E a, E b) ;

}
