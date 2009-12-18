package org.jjoost.collections;

public interface MultiSet<V> extends ArbitrarySet<V> {

	/**
	 * put the provided value into the set; this is always successful as duplicates are permitted
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	@Override public V put(V val) ;
	public void put(V val, int numberOfTimes) ;
	
	@Override public MultiSet<V> copy() ;
	
}
