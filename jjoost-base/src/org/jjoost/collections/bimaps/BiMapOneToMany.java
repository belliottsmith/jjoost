/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.collections.bimaps;

import java.util.Map.Entry;

import org.jjoost.collections.MultiMap;
import org.jjoost.collections.Map;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitarySet;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;

public class BiMapOneToMany<K, V> extends AbstractBiMap<K, V, Map<K, V>, MultiMap<V, K>> implements Map<K, V> {

	private static final long serialVersionUID = -3696446893675439338L;

	private final BiMapManyToOne<V, K> partner;
	@Override protected final AbstractBiMap<V, K, MultiMap<V, K>, Map<K, V>> partner() {
		return partner;
	}

	public BiMapOneToMany(Map<K, V> forwards, MultiMap<V, K> back) {
		super(forwards);
		this.partner = new BiMapManyToOne<V, K>(back, this);
	}

	private BiMapOneToMany(Map<K, V> forwards, BiMapManyToOne<V, K> partner) {
		super(forwards);
		this.partner = partner;
	}
	
	@Override
	public Map<K, V> copy() {
		final Map<K, V> fwds = map.copy();
		final MultiMap<V, K> back = partner().map.copy();
		return new BiMapOneToMany<K, V>(fwds, back);
	}

	public MultiMap<V, K> inverse() {
		return partner;
	}
	
	@Override
	public Set<Entry<K, V>> entries() {
		return map.entries();
	}

	@Override
	public Set<K> keys() {
		return map.keys();
	}

	@Override
	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
		if (!map.contains(key)) {
			final V val = putIfNotPresent.create();
			if (partner.map.contains(val))
				return val;
			map.put(key, val);
			partner.map.put(val, key);
			return val;
		}
		return map.get(key);
	}

	@Override
	public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) {
		if (!map.contains(key)) {
			final V val = putIfNotPresent.apply(key);
			if (partner.map.contains(val))
				return val;
			map.put(key, val);
			partner.map.put(val, key);
			return val;
		}
		return map.get(key);
	}

	@Override
	public V get(K key) {
		return map.get(key);
	}

	@Override
	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
		if (!map.contains(key)) {
			final V val = putIfNotPresent.apply(key);
			if (partner.map.contains(val))
				return val;
			map.put(key, val);
			partner.map.put(val, key);
			return null;
		}
		return map.get(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public V apply(K v) {
		return map.apply(v);
	}
	
	@Override
	public UnitarySet<V> values(K key) {
		return map.values(key);
	}
	
}
