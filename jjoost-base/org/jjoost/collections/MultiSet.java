package org.jjoost.collections;

public interface MultiSet<V> extends ArbitrarySet<V> {

	/**
	 * put the provided value into the set; this is always successful as duplicates are permitted
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V put(V val) ;
	
	public MultiSet<V> copy() ;
	
}
