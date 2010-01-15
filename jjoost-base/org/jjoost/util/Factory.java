package org.jjoost.util;

import java.io.Serializable;

/**
 * This interface defines an arbitrary object factory that requires no arguments.
 *  
 * @author b.elliottsmith
 */
public interface Factory<E> extends Serializable {

	/**
	 * Returns an object of type <code>E</code>. This method should be thread-safe. 
	 * @return an object of type E
	 */
	E create() ;
	
}
