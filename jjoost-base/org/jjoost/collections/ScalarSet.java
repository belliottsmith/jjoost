package org.jjoost.collections;

import org.jjoost.util.Function;

/**
 * A ScalarMap permits at most one value for each possible key, much like the java.util.HashMap
 * 
 * @author Benedict Elliott Smith
 *
 * @param <K>
 * @param <V>
 */
public interface ScalarSet<V> extends ArbitrarySet<V>, Function<V, Boolean> {

	public V put(V val) ;
	public V get(V key) ;
	public ScalarSet<V> copy() ;
	public int size() ;
	
}
