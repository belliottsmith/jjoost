package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.ListMap ;
import org.jjoost.collections.MultiSet ;

public class BiMapListToList<K, V> extends AbstractBiMap<K, V, ListMap<K, V>, ListMap<V, K>> implements ListMap<K, V> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMapListToList<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, ListMap<V, K>, ListMap<K, V>> partner() {
		return partner ;
	}

	public BiMapListToList(ListMap<K, V> forwards, ListMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMapListToList<V, K>(back, this) ;
	}

	private BiMapListToList(ListMap<K, V> forwards, BiMapListToList<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public ListMap<K, V> copy() {
		final ListMap<K, V> fwds = map.copy() ;
		final ListMap<V, K> back = partner().map.copy() ;		
		return new BiMapListToList<K, V>(fwds, back) ;
	}

	public ListMap<V, K> inverse() {
		return partner ;
	}
	
	@Override
	public MultiSet<Entry<K, V>> entries() {
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
