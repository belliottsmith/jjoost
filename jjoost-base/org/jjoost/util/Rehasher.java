package org.jjoost.util;

import java.io.Serializable;

/**
 * A simple interface defining custom hash functions over objects
 * 
 * @author Benedict Elliott Smith
 */
public interface Rehasher extends Serializable {
	
	/**
	 * @param o
	 * @return the hash of <code>o</code>
	 */
    public int hash(int hash) ;

}
