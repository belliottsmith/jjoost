package org.jjoost.util;

import java.io.Serializable;

/**
 * This interface declares a method which defines equality been objects of type <code>E</code>
 * 
 * @author b.elliottsmith
 */
public interface Equality<E> extends Serializable {
	
	/**
	 * Returns a <code>boolean</code> indicating if the two parameters are considered equal by this <code>Equality</code>
	 * 
	 * @param a
	 *            an <code>Object</code> of type <code>E</code>
	 * @param b
	 *            another <code>Object</code> of type <code>E</code>
	 * @return <code>true</code> if this equality equates the two arguments
	 */
    public boolean equates(E a, E b) ;
    
    /**
	 * Returns the hash value of the object as defined by this <code>Equality</code>. For all objects this <code>Equality</code> can be
	 * applied to, it should be the case that <code>equates(a, b)</code> ==> <code>hash(a) == hash(b)</code>
	 * 
	 * @param o
	 *            an <code>Object</code> of type E
	 * @return the hash of the parameter
	 */
    public int hash(E o) ;

}
