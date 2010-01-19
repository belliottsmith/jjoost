package org.jjoost.util;

import java.io.Serializable;

/**
 * The interface declares a method defining a function from domain <code>X</code> to range <code>Y</code>
 * 
 * @author b.elliottsmith
 */
public interface Function<X, Y> extends Serializable {
    
	/** 
	 * Returns the result of applying the function to the parameter
	 * 
	 * @param v something of type domain
	 * @return the result of the function (something of type range)
	 */
    public Y apply(X v) ;

}
