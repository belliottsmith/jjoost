package org.jjoost.util;

/**
 * A simple interface encapsulating some dynamic integer greater than or equal to zero
 * 
 * @author b.elliottsmith
 */
public interface Counter {
	
	/**
	 * returns true if adding the provided integer does not reduce the value to below zero; if true
	 * then the value was added to the <code>Counter</code>
	 * 
	 * @param i to add
	 * @return true, if successful
	 */
	public boolean add(int i) ;
	
	/**
	 * Gets the current value of the <code>Counter</code>
	 * @return current value
	 */
	public int get() ;
	
	/**
	 * returns a new <code>Counter</code> of the same type as this one, with a value of zero
	 * @return new <code>Counter</code>
	 */
	public Counter newInstance() ;
	
}
