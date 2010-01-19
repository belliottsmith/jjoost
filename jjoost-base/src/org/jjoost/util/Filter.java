package org.jjoost.util ;

import java.io.Serializable;

/**
 * Defines a simple filter over objects
 * 
 * @author b.elliottsmith
 */
public interface Filter<E> extends Serializable {

    /**
	 * Returns <code>true</code> if the <code>Filter</code> accepts the parameter and <code>false</code> otherwise
	 * 
	 * @param test
	 *            value <code>to</code> test
	 * @return <code>true</code> if the filter accepts <code>test</code>; <code>false</code> otherwise
	 */
    public boolean accept(E test) ;

}
