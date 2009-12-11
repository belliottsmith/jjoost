package org.jjoost.util;

/**
 * A simple class to encapsulate some dynamic integer greater than or equal to zero
 * 
 * @author b.elliottsmith
 */
public interface Counter {

	public boolean add(int i) ;
	public int get() ;
	public Counter newInstance() ;
	
}
