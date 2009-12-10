package org.jjoost.util;

import java.io.Serializable;

/**
 * A function from domain to range 
 * 
 * @author Benedict Elliott Smith
 * @param <X>
 * @param <Y>
 */
public interface Function<X, Y> extends Serializable {
    
	/** 
	 * @param v something of type domain
	 * @return the result of the function (something of type range)
	 */
    public Y apply(X v) ;

}
