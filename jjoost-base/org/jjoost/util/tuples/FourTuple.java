package org.jjoost.util.tuples;

/**
 * A simple interface representing a four-tuple
 * 
 * @author b.elliottsmith
 */
public interface FourTuple<A, B, C, D> extends Triple<A, B, C> {

	/**
	 * @return the fourth value of the tuple
	 */
	D fourth() ;
	
}
