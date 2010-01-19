package org.jjoost.util.tuples;

import java.io.Serializable;

/**
 * A simple interface representing a pair of values
 * 
 * @author b.elliottsmith
 */
public interface Pair<A, B> extends Serializable {

	/**
	 * @return the first value of the pair
	 */
	public A first() ;
	
	/**
	 * @return the second value of the pair
	 */
	public B second() ;
	
}
