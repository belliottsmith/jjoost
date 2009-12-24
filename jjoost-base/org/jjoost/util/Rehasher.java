package org.jjoost.util;

import java.io.Serializable;

/**
 * A simple interface defining custom <i>re-hashing</i> functions
 * 
 * @author b.elliottsmith
 */
public interface Rehasher extends Serializable {
	
	/**
	 * @param a hash
	 * @return the re-hash of the provided hash
	 */
    public int hash(int hash) ;

}
