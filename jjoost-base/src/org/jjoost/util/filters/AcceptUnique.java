package org.jjoost.util.filters ;

import org.jjoost.collections.Set ;
import org.jjoost.collections.sets.serial.SerialHashSet ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Filter ;

/**
 * A <code>Filter</code> which returns <code>true</code> if and only if it has never seen the value being tested before, 
 * using the provided set to maintain the previously visited set of values. 
 * 
 * @author b.elliottsmith
 */
public class AcceptUnique<V> implements Filter<V> {

	private static final long serialVersionUID = 4135610622081116945L ;
	private final Set<V> seen ;

	/**
	 * Construct a new <code>AcceptUnique</code> using default propreties (hash set using Object.equals() and Object.hashCode())
	 */
	public AcceptUnique() {
		this(new SerialHashSet<V>(8, 0.75f)) ;
	}
	
	/**
	 * Construct a new <code>AcceptUnique</code> using the provided set to maintain the previously visited set of values 
	 */
	public AcceptUnique(Set<V> set) {
		this.seen = set ;
	}

	public boolean accept(V next) {
		return seen.add(next) ;
	}
	
	public String toString() {
		return "is first occurence of" ;
	}

    /**
	 * Returns a <code>Filter</code> which returns <code>true</code> if and only if it has never seen the value being tested before, using regular
	 * object equality. It maintains a set of all visited values and therefore can be expensive with respect to memory utilisation.
	 * 
	 * @return a filter accepting only unique values
	 */
	public static <V> AcceptUnique<V> get() {
		return new AcceptUnique<V>() ;
	}
	
    /**
	 * Returns a <code>Filter</code> which returns <code>true</code> if and only if it has never seen the value being tested before, using the provided
	 * equality. It maintains a set of all visited values and therefore can be expensive with respect to memory utilisation.
	 * 
	 * @param eq the equality determining uniqueness
	 * @return a filter accepting only unique values
	 */
	public static <V> AcceptUnique<V> get(Equality<? super V> eq) {
		return get(new SerialHashSet<V>(eq)) ;
	}
	
    /**
	 * Returns a <code>Filter</code> which returns <code>true</code> if and only if it has never seen the value being tested before, using the provided
	 * set to maintain all visited values, and hence the set's definition of equality. This can be expensive with respect to memory utilisation.
	 * 
	 * @param set the set to store visited values in
	 * @return a filter accepting only unique values
	 */
	public static <V> AcceptUnique<V> get(Set<V> set) {
		return new AcceptUnique<V>(set) ;
	}
	
}
