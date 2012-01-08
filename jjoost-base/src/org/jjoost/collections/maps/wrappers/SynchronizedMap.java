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

import org.jjoost.collections.Map;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitarySet;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;

public class SynchronizedMap<K, V> extends SynchronizedArbitraryMap<K, V, Map<K, V>> implements Map<K, V> {
	
	private static final long serialVersionUID = 2692454383540344975L;
	public SynchronizedMap(Map<K, V> delegate) {
		super(delegate);
	}
	
	private Set<K> keySet;
	private Set<Entry<K, V>> entrySet;
	@Override public synchronized Set<K> keys() {
		if (keySet == null)
			keySet = wrap(delegate.keys());
		return keySet;
	}
	@Override public synchronized Set<Entry<K, V>> entries() {
		if (entrySet == null)
			entrySet = wrap(delegate.entries());
		return entrySet;
	}
	
	@Override public synchronized V apply(K v) {
		return delegate.apply(v);
	}
	@Override public synchronized V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
		return delegate.ensureAndGet(key, putIfNotPresent);
	}
	@Override public synchronized V ensureAndGet(K key,
			Function<? super K, ? extends V> putIfNotPresent) {
		return delegate.ensureAndGet(key, putIfNotPresent);
	}
	@Override public synchronized V get(K key) {
		return delegate.get(key);
	}
	@Override public synchronized int size() {
		return delegate.size();
	}

	@Override public synchronized V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
		return delegate.putIfAbsent(key, putIfNotPresent);
	}

	@Override
	public Map<K, V> copy() {
		return new SynchronizedMap<K, V>(delegate.copy());
	}
	@Override public synchronized UnitarySet<V> values(K key) {
		return wrap(delegate.values(key));
	}
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return delegate.replace(key, oldValue, newValue);
	}
	@Override
	public V replace(K key, V val) {
		return delegate.replace(key, val);
	}

}
