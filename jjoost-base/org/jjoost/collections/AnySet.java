package org.jjoost.collections;

import java.util.Iterator;

/**
 * <p> This is the common interface for all modifiable Jjoost sets. The methods declared here make no assumptions 
 * about the number of occurences of a given value.
 * 
 * <p>WARNING: Note that in a MultiSet the <code>iterator()</code> method will
 * return an <code>Iterator</code> that <i>enumerates <b>every occurence</b> of every value</i>. If you want
 * to get unique occurences of values, call the <code>unique()</code> method.
 * 
 * <p>Also note that an <code>Iterator</code> returned by concurrent implementors of this class is permitted
 * to return values more times than they actually ever occurred <b>if a valid sequence of deletes and inserts happens</b>
 * to cause the <code>Iterator</code> to see the values multiple times. See the javadoc of the implementing classes
 * to determine their behaviour in this case.
 * 
 * @author b.elliottsmith
 */
public interface AnySet<V> extends AnyReadSet<V> {

	/**
	 * Insert the value into the set, returning any value that was evicted as a result
	 * or null if none (note that if the value to insert is null, the null returned 
	 * cannot be used to determine if any action was taken on the set). In a <code>Set</code>
	 * any pre-existing value that is equal to the parameter as determined by the set's
	 * definition of equality will be removed from the set and returned. In a <code>MultiSet</code>
	 * the return value will always be <code>null</code>
	 * 
	 * @param value to insert
	 * 
	 * @return value that was evicted from the set as a result of the action
	 */
	public V put(V val) ;	
	
	/**
	 * Performs the equivalent of a <code>put()</code> operation for every value provided,
	 * returning an int representing the total number of values that did not displace existing
	 * values. In a <code>MultiSet</code> this will always be equal to the number of values provided, however
	 * in a <code>Set</code> it may be fewer.
	 * 
	 * @param values to insert
	 * 
	 * @return number that did not displace existing values
	 */
	public int putAll(Iterable<V> vals) ;	
	
	/**
	 * Inserts the value and returns <code>null</code> if an equal value does not already occur in the set; 
	 * otherwise returns the first such value encountered
	 * 
	 * @param value to insert
	 * 
	 * @return existing value, or null if none
	 */
	public V putIfAbsent(V val) ;
	
	/**
	 * Removes <b>all occurrences</b> of values equal to the parameter from the set,
	 * as determined by the set's definition of equality and returns the number of values
	 * that were removed from the set. Equivalent to <code>remove(value, Integer.MAX_VALUE)</code>. 
	 * 
	 * @param value remove equal to
	 * 
	 * @return number removed
	 */
	public int remove(V value) ;
	
	/**
	 * Removes at most the prescribed number of values equal to the parameter
	 * from the set; which values are removed is implementation specific, and may be arbitrary.
	 * Returns the number of values actually removed as a result of the action.
	 * 
	 * @param remove remove equal to
	 * @param removeAtMost remove at most
	 * 
	 * @return the number removed
	 */
	public int remove(V value, int removeAtMost) ;	
	
	/**
	 * Removes all occurrences of values equal to the parameter and returns an <code>Iterable</code>
	 * of the values removed. Equivalent to <code>removeAndReturn(value, Integer.MAX_VALUE)</code>.
	 * 
	 * @param value remove equal to
	 * 
	 * @return values removed
	 */
	public Iterable<V> removeAndReturn(V value) ;	
	
	/**
	 * Removes at most the prescribed number of values equal to the parameter
	 * from the set and returns them to the user; which values are removed is implementation specific, 
	 * and may be arbitrary.
	 * 
	 * @param value the value
	 * @param removeAtMost remove at most
	 * 
	 * @return values removed
	 */
	public Iterable<V> removeAndReturn(V value, int removeAtMost) ;	
	
	/**
	 * Removes <b>all occurrences</b> of values equal to the parameter from the set,
	 * as determined by the set's definition of equality, and returns the first value encountered
	 * or null if none. Equivalent to <code>removeAndReturnFirst(value, Integer.MAX_VALUE)</code>.
	 * 
	 * @param value remove
	 * 
	 * @return first value removed
	 */
	public V removeAndReturnFirst(V value) ;	
	
	/**
	 * Removes at most the prescribed number of values equal to the parameter from the 
	 * set and returns the first one encountered to the user, or null if none; 
	 * which values are removed is implementation specific, and may be arbitrary.
	 * 
	 * @param value remove
	 * @param removeAtMost remove at most
	 * 
	 * @return first value removed
	 */
	public V removeAndReturnFirst(V value, int removeAtMost) ;
	
	/**
	 * Removes all values from the set and return the number of values removed. In a concurrent set this
	 * number should be a atomically determined; i.e. even if the set is no longer clear by the time this method
	 * returns, the number of values it reports to have removed should be the actual number removed.
	 * 
	 * @return number removed
	 */
	public int clear() ;	
	
	/**
	 * Removes all values from the set and returns an <code>Iterator</code> over them. This <code>Iterator</code>
	 * must be exactly equal to the set of values removed from the set by the action, however this set of values
	 * may not necessarily be determined, or the <code>clear()</code> completed, until the <code>Iterator</code> 
	 * has been exhausted.
	 * 
	 * @return values removed
	 */
	public Iterator<V> clearAndReturn() ;
	
	/**
	 * Returns an <code>AnySet</code> (usually a <code>Set</code>) representing only the unique values 
	 * present in this set; if this set is already unique this method should return the set itself. 
	 * Changes to each set should be reflected in the other, however put() operations on the unique()
	 * set will not be supported if the underlying set is not itself unique.
	 * 
	 * @return unique values
	 */
	public AnySet<V> unique() ;
	
	/**
	 * This method attempts to minimise the resource utilisation of the set. It may be a no-op.
	 */
	public void shrink() ;	
	
	/**
	 * Returns a copy of the set. This method may or may not return a set of the same class as the one it was
	 * called on, however must be of the same basic interface (either <code>Set</code> or <code>MultiSet</code>).
	 */
	public AnySet<V> copy() ;
	
}
