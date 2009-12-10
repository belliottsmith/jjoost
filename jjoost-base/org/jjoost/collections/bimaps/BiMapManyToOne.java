package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.ListSet ;
import org.jjoost.collections.MultiMap ;
import org.jjoost.collections.ScalarMap ;
import org.jjoost.collections.ScalarSet ;

public class BiMapManyToOne<K, V> extends AbstractBiMap<K, V, MultiMap<K, V>, ScalarMap<V, K>> implements MultiMap<K, V> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMapOneToMany<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, ScalarMap<V, K>, MultiMap<K, V>> partner() {
		return partner ;
	}

	public BiMapManyToOne(MultiMap<K, V> forwards, ScalarMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMapOneToMany<V, K>(back, this) ;
	}

	private BiMapManyToOne(MultiMap<K, V> forwards, BiMapOneToMany<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public MultiMap<K, V> copy() {
		final MultiMap<K, V> fwds = map.copy() ;
		final ScalarMap<V, K> back = partner().map.copy() ;		
		return new BiMapManyToOne<K, V>(fwds, back) ;
	}

	public ScalarMap<V, K> inverse() {
		return partner ;
	}
	
	@Override
	public ScalarSet<Entry<K, V>> entries() {
		return map.entries() ;
	}

	@Override
	public ListSet<K> keys() {
		return map.keys() ;
	}

	@Override
	public Iterable<V> apply(K key) {
		return map.values(key) ;
	}

}
