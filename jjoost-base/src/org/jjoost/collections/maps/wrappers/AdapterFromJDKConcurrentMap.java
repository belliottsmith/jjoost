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

package org.jjoost.collections.maps.wrappers;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.jjoost.collections.Map;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.util.Function;
import org.jjoost.util.Iters;

// TODO : this class' methods currently assume keys/values are all non null, which is an invalid assumption
public class AdapterFromJDKConcurrentMap<K, V> extends AdapterFromJDKMap<K, V> implements Map<K, V> {
	
	private static final long serialVersionUID = -5498331996410891451L;
	final ConcurrentMap<K, V> map;
	
	public AdapterFromJDKConcurrentMap(ConcurrentMap<K, V> map) {
		super(map);
		this.map = map;
	}
	
	// TODO: make atomic using locks
	@Override
	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {		
		final V v = map.get(key);
		if (v == null)
			map.put(key, putIfNotPresent.apply(key));
		return v;
	}
	
	@Override
	public V putIfAbsent(K key, V val) {
		return map.putIfAbsent(key, val);
	}
	
	@Override
	public int size() {
		return map.size();
	}
	@Override
	public V apply(K v) {
		return map.get(v);
	}
	@Override
	public int clear() {
		final int size = map.size();
		map.clear();
		return size;
	}
	
	@Override
	public int remove(K key, V val) {
		if (map.remove(key, val)) {
			return 1;
		}
		return 0;
	}
	@Override
	public int remove(K key) {
		final Object val = map.get(key);
		if (map.containsKey(key) && map.remove(key) == val) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		if (map.remove(key, val)) {
			return new UniformList<Entry<K, V>>(new ImmutableMapEntry<K, V>(key, null), 1);
		}
		return Iters.emptyIterable();
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		final V val = map.get(key);
		if (map.containsKey(key) && map.remove(key) == val) {
			return new UniformList<Entry<K, V>>(new ImmutableMapEntry<K, V>(key, val), 1);
		}
		return Iters.emptyIterable();
	}
	
}
