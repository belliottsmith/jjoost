package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.ListMap ;
import org.jjoost.collections.ListSet ;
import org.jjoost.collections.MultiMap ;

public class BiMapListToMany<K, V> extends AbstractBiMap<K, V, ListMap<K, V>, MultiMap<V, K>> implements ListMap<K, V> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMapManyToList<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, MultiMap<V, K>, ListMap<K, V>> partner() {
		return partner ;
	}

	public BiMapListToMany(ListMap<K, V> forwards, MultiMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMapManyToList<V, K>(back, this) ;
	}

	private BiMapListToMany(ListMap<K, V> forwards, BiMapManyToList<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public ListMap<K, V> copy() {
		final ListMap<K, V> fwds = map.copy() ;
		final MultiMap<V, K> back = partner().map.copy() ;		
		return new BiMapListToMany<K, V>(fwds, back) ;
	}

	public MultiMap<V, K> inverse() {
		return partner ;
	}
	
	@Override
	public ListSet<Entry<K, V>> entries() {
		return map.entries() ;
	}

	@Override
	public ListSet<K> keys() {
		return map.keys() ;
	}

	@Override
	public Iterable<V> apply(K key) {
		return values(key) ;
	}

}
