package org.jjoost.util.tuples;

/**
 * A simple interface representing a three-tuple (triple)
 * 
 * @author b.elliottsmith
 */
public interface Triple<A, B, C> extends Pair<A, B>{

	/**
	 * @return the third value of the triple
	 */
	public C third() ;
	
}
