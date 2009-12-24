package org.jjoost.util ;

import java.io.Serializable;

/**
 * Defines a simple filter over objects
 * 
 * @author b.elliottsmith
 * @param <E>
 */
public interface Filter<E> extends Serializable {

    /**
     * Returns true if the filter accepts the parameter and false otherwise
     * 
     * @param test
     * @return true if the filter accepts test; false otherwise
     */
    public boolean accept(E test) ;

}
