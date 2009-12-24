package org.jjoost.collections;

import org.jjoost.util.Function;

/**
 * <p> This interface declares that a set supports precisely one occurence of each value
 * 
 * <p>Note that an <code>Iterator</code> returned by concurrent implementors of this class is permitted
 * to return values more times than they actually ever occurred <b>if a valid sequence of deletes and inserts happens</b>
 * to cause the <code>Iterator</code> to see the values multiple times. See the javadoc of the implementing classes
 * to determine their behaviour in this case.
 * 
 * @author b.elliottsmith
 */
public interface Set<V> extends AnySet<V>, Function<V, Boolean> {

	/**
	 * Insert the parameter into the set, removing and returning any value equal to the
	 * parameter that was already present, as determined by the set's definition of equality.
	 * 
	 * @param value to insert
	 * @return value that was evicted from the set as a result of the action
	 */
	@Override public V put(V val) ;
	
	public V get(V key) ;

	/**
	 * A convenience method returning the size of the set; this is equivalent to
	 * <code>totalCount()</code> or <code>uniqueCount()</code> 
	 * @return size of the set
	 */
	public int size() ;
	
	/**
	 * Returns a copy of the set. This method may or may not return a set of the same class as the one it was
	 * called on, however return a <code>Set</code>
	 */
	@Override public Set<V> copy() ;
	
}
