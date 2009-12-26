package org.jjoost.collections;

/**
 * <p> This interface declares that a set supports multiple occurrences of each value
 * 
 * <p>Note that the <code>iterator()</code> method will return an <code>Iterator</code> that 
 * <i>enumerates <b>every occurence</b> of every value</i>. If you want
 * to get unique occurences of values, call the <code>unique()</code> method.
 * 
 * <p>Also note that an <code>Iterator</code> returned by concurrent implementors of this class is permitted
 * to return values more times than they actually ever occurred <b>if a valid sequence of deletes and inserts happens</b>
 * to cause the <code>Iterator</code> to see the values multiple times. See the javadoc of the implementing classes
 * to determine their behaviour in this case.
 * 
 * @author b.elliottsmith
 */
public interface MultiSet<V> extends AnySet<V> {

	/**
	 * Insert the value into the set. This method will always succeed and the
	 * return value will always be null.
	 * 
	 * @param val the val
	 * 
	 * @return null
	 */
	@Override public V put(V val) ;

	/**
	 * Insert the value into the set the specified number of times.
	 * 
	 * @param val the val
	 * @param numberOfTimes the number of times
	 */
	public void put(V val, int numberOfTimes) ;
	
	
	/**
	 * Returns a copy of the set. This method may or may not return a set of the same class as the one it was
	 * called on, however must return a <code>MultiSet</code>
	 */
	@Override public MultiSet<V> copy() ;

	/**
	 * Returns a <code>Set</code> representing only the unique values present in this set. 
	 * Changes to each set should be reflected in the other, however put() operations on the unique()
	 * set are not supported.
	 * 
	 * @return unique values
	 */
	public Set<V> unique() ;

}
