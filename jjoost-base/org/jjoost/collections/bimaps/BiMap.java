package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.ArbitraryMap ;
import org.jjoost.collections.ArbitrarySet ;

public class BiMap<K, V> extends AbstractBiMap<K, V, ArbitraryMap<K, V>, ArbitraryMap<V, K>> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMap<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, ArbitraryMap<V, K>, ArbitraryMap<K, V>> partner() {
		return partner ;
	}

	public BiMap(ArbitraryMap<K, V> forwards, ArbitraryMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMap<V, K>(back, this) ;
	}

	private BiMap(ArbitraryMap<K, V> forwards, BiMap<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public ArbitraryMap<K, V> copy() {
		final ArbitraryMap<K, V> fwds = map.copy() ;
		final ArbitraryMap<V, K> back = partner().map.copy() ;		
		return new BiMap<K, V>(fwds, back) ;
	}

	@Override
	public ArbitrarySet<Entry<K, V>> entries() {
		return map.entries() ;
	}

	@Override
	public ArbitrarySet<K> keys() {
		return map.keys() ;
	}

}
