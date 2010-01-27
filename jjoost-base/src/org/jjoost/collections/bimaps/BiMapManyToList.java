package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.ListMap ;
import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.MultiMap ;
import org.jjoost.collections.Set ;

public class BiMapManyToList<K, V> extends AbstractBiMap<K, V, MultiMap<K, V>, ListMap<V, K>> implements MultiMap<K, V> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMapListToMany<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, ListMap<V, K>, MultiMap<K, V>> partner() {
		return partner ;
	}

	public BiMapManyToList(MultiMap<K, V> forwards, ListMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMapListToMany<V, K>(back, this) ;
	}

	private BiMapManyToList(MultiMap<K, V> forwards, BiMapListToMany<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public MultiMap<K, V> copy() {
		final MultiMap<K, V> fwds = map.copy() ;
		final ListMap<V, K> back = partner().map.copy() ;		
		return new BiMapManyToList<K, V>(fwds, back) ;
	}

	public ListMap<V, K> inverse() {
		return partner ;
	}
	
	@Override
	public Set<Entry<K, V>> entries() {
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

	@Override
	public Set<V> values(K key) {
		return map.values(key) ;
	}
	
}