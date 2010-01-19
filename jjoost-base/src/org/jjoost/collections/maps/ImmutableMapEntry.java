package org.jjoost.collections.maps;

import java.util.Map;
import java.util.Map.Entry;

import org.jjoost.util.Objects;

public final class ImmutableMapEntry<K, V> implements Map.Entry<K, V> {

	private final K key ;
	private final V value ;
	
	public ImmutableMapEntry(K key, V value) {
		super() ;
		this.key = key ;
		this.value = value ;
	}

	public K getKey() {
		return key ;
	}

	public V getValue() {
		return value ;
	}

	public V setValue(V value) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public int hashCode() {
		return key.hashCode() ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that) {
		return that instanceof Entry && equals((Entry<?, ?>) that) ;
	}

	public boolean equals(Entry<?, ?> that) {
		return Objects.equalQuick(this.key, that.getKey()) && Objects.equalQuick(this.value, that.getValue()) ;
	}
	
}
