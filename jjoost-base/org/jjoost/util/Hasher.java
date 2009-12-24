package org.jjoost.util;

import java.io.Serializable;

/**
 * This interface defines a hash function over objects of type <code>E</code>
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public interface Hasher<E> extends Serializable {
	
	/**
	 * Returns the hash of the provided object, as determined by this <code>Hasher</code> and <i>not</i> necessarily the object's <code>hashCode()</code> 
	 * 
	 * @param o
	 * @return the hash of <code>o</code>
	 */
    public int hash(E o) ;

}
