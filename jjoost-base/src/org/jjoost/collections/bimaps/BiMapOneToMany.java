package org.jjoost.collections.bimaps;

import java.util.Map.Entry ;

import org.jjoost.collections.MultiMap ;
import org.jjoost.collections.Map ;
import org.jjoost.collections.Set ;
import org.jjoost.collections.UnitarySet;
import org.jjoost.util.Factory ;
import org.jjoost.util.Function ;

public class BiMapOneToMany<K, V> extends AbstractBiMap<K, V, Map<K, V>, MultiMap<V, K>> implements Map<K, V> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMapManyToOne<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, MultiMap<V, K>, Map<K, V>> partner() {
		return partner ;
	}

	public BiMapOneToMany(Map<K, V> forwards, MultiMap<V, K> back) {
		super(forwards) ;
		this.partner = new BiMapManyToOne<V, K>(back, this) ;
	}

	private BiMapOneToMany(Map<K, V> forwards, BiMapManyToOne<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public Map<K, V> copy() {
		final Map<K, V> fwds = map.copy() ;
		final MultiMap<V, K> back = partner().map.copy() ;		
		return new BiMapOneToMany<K, V>(fwds, back) ;
	}

	public MultiMap<V, K> inverse() {
		return partner ;
	}
	
	@Override
	public Set<Entry<K, V>> entries() {
		return map.entries() ;
	}

	@Override
	public Set<K> keys() {
		return map.keys() ;
	}

	@Override
	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
		if (!map.contains(key)) {
			final V val = putIfNotPresent.create() ;
			if (partner.map.contains(val))
				return val ;
			map.put(key, val) ;
			partner.map.put(val, key) ;
			return val ;
		}
		return map.get(key) ;
	}

	@Override
	public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) {
		if (!map.contains(key)) {
			final V val = putIfNotPresent.apply(key) ;
			if (partner.map.contains(val))
				return val ;
			map.put(key, val) ;
			partner.map.put(val, key) ;
			return val ;
		}
		return map.get(key) ;
	}

	@Override
	public V get(K key) {
		return map.get(key) ;
	}

	@Override
	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
		if (!map.contains(key)) {
			final V val = putIfNotPresent.apply(key) ;
			if (partner.map.contains(val))
				return val ;
			map.put(key, val) ;
			partner.map.put(val, key) ;
			return null ;
		}
		return map.get(key) ;
	}

	@Override
	public int size() {
		return map.size() ;
	}

	@Override
	public V apply(K v) {
		return map.apply(v) ;
	}
	
	@Override
	public UnitarySet<V> values(K key) {
		return map.values(key) ;
	}
	
}