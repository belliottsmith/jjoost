package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.MultiMap ;
import org.jjoost.collections.ScalarSet ;

public class BiMapManyToMany<K, V> extends AbstractBiMap<K, V, MultiMap<K, V>, MultiMap<V, K>> implements MultiMap<K, V> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMapManyToMany<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, MultiMap<V, K>, MultiMap<K, V>> partner() {
		return partner ;
	}

	public BiMapManyToMany(MultiMap<K, V> forwards, MultiMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMapManyToMany<V, K>(back, this) ;
	}

	private BiMapManyToMany(MultiMap<K, V> forwards, BiMapManyToMany<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public MultiMap<K, V> copy() {
		final MultiMap<K, V> fwds = map.copy() ;
		final MultiMap<V, K> back = partner().map.copy() ;		
		return new BiMapManyToMany<K, V>(fwds, back) ;
	}

	public MultiMap<V, K> inverse() {
		return partner ;
	}
	
	@Override
	public ScalarSet<Entry<K, V>> entries() {
		return map.entries() ;
	}

	@Override
	public MultiSet<K> keys() {
		return map.keys() ;
	}

	@Override
	public Iterable<V> apply(K key) {
		return map.values(key) ;
	}

}
