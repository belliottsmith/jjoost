package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.AnyMap ;
import org.jjoost.collections.AnySet ;

public class BiMap<K, V> extends AbstractBiMap<K, V, AnyMap<K, V>, AnyMap<V, K>> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMap<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, AnyMap<V, K>, AnyMap<K, V>> partner() {
		return partner ;
	}

	public BiMap(AnyMap<K, V> forwards, AnyMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMap<V, K>(back, this) ;
	}

	private BiMap(AnyMap<K, V> forwards, BiMap<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public AnyMap<K, V> copy() {
		final AnyMap<K, V> fwds = map.copy() ;
		final AnyMap<V, K> back = partner().map.copy() ;		
		return new BiMap<K, V>(fwds, back) ;
	}

	@Override
	public AnySet<Entry<K, V>> entries() {
		return map.entries() ;
	}

	@Override
	public AnySet<K> keys() {
		return map.keys() ;
	}
	
	@Override
	public AnySet<V> values(K key) {
		return map.values(key) ;
	}

}
