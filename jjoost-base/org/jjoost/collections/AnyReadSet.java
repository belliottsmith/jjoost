package org.jjoost.collections;

import java.io.Serializable;
import java.util.List;

import org.jjoost.util.Function;

/**
 * <p> This is the common interface for all Jjoost sets. 
 * <p> The methods declared here make no assumptions about the number of occurences of a given value 
 * and declare no actions that may modify the set, so is the most general form of set. No
 * concrete class should implement this interface directly.
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
public interface AnyReadSet<V> extends Iterable<V>, Function<V, Boolean>, Serializable {

	/**
	 * The first value stored in the set that is equal to the provided value, as determined by
	 * any provided <code>Equality</code> or <code>Comparator</code>. Returns null if no
	 * matching item is stored in the set.
	 * 
	 * @param find
	 * 
	 * @return first matching value
	 */
	public V first(V value) ;
	
	/**
	 * Returns an <code>Iterable</code> of all values contained in the set which are equal 
	 * to the parameter, as determined by the set's definition of equality. <p>Changes to the 
	 * set that happen prior to retrieving an <code>Iterator</code> from the <code>Iterable</code>
	 * should be reflected in the resulting <code>Iterator</code>. Changes to the
	 * set once an <code>Iterator</code> has been obtained may or may not be reflected in the 
	 * <code>Iterator</code> at the discretion of the implementing class. <p>In a concurrent 
	 * set it is acceptable for values to occur extra times if they are deleted and re-inserted
	 * in between method calls on the <code>Iterator</code>.
	 * 
	 * @param find
	 * 
	 * @return matching values
	 */
	public Iterable<V> all(V value) ;
	
	/**
	 * Returns a <code>List</code> of all values contained in the set which are equal
	 * to the parameter, as determined by the set's definition of equality. This list
	 * should be constructed "eagerly" and as such should be a consistent snapshot of 
	 * the values valid at some point between the method being called and it returning.
	 * 
	 * @param find
	 * 
	 * @return matching values
	 */
	public List<V> list(V value) ;
	
	/**
	 * Returns a boolean indicating if the parameter occurs in the set at least once.
	 * 
	 * @param find
	 * 
	 * @return true, if present
	 */
	public boolean contains(V value) ;
	
	/**
	 * Returns an integer representing the number of occurrences of the value in the set
	 * 
	 * @param find
	 * 
	 * @return number of occurrences
	 */
	public int count(V value) ;
	
	/**
	 * Returns an <code>Iterable</code> of only unique values in the set, as determined by
	 * the set's definition of equality. This method is guaranteed not to return duplicates,
	 * even  
	 * 
	 * @return unique values
	 */
	public Iterable<V> unique() ;
	
	/**
	 * Returns the total number of values (including duplicates) in the set
	 * 
	 * @return total number of values in the set
	 */
	public int totalCount() ;
	
	/**
	 * Returns the number of unique values in the set
	 * 
	 * @return number of unique values in the set
	 */
	public int uniqueCount() ;	
	
	/**
	 * Indicates if the set is empty
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty() ;
	
	/**
	 * Indicates if the set permits a value to occur more than once
	 * 
	 * @return true, if successful
	 */
	public boolean permitsDuplicates() ;
	
}
