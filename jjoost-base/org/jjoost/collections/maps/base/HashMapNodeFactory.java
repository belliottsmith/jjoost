package org.jjoost.collections.maps.base;

public interface HashMapNodeFactory<K, V, N> {

	public N node(int hash, K key, V value) ;

}
