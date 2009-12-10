package org.jjoost.util ;

import java.io.Serializable;

/**
 * Defines a simple filter over objects
 * 
 * @author Benedict Elliott Smith
 * @param <E>
 */
public interface Filter<E> extends Serializable {

    /**
     * @param test
     * @return true if the filter accepts test; false otherwise
     */
    public boolean accept(E test) ;

}
