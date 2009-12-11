package org.jjoost.util;

import java.io.Serializable;

/**
 * A simple interface defining custom hash functions over objects
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public interface Hasher<E> extends Serializable {
	
	/**
	 * @param o
	 * @return the hash of <code>o</code>
	 */
    public int hash(E o) ;

}
