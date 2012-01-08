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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.Map;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitarySet;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;

public class DefaultFunctionMap<K, V> implements Map<K, V> {

	private static final long serialVersionUID = 7778573411318310241L;
	private final Map<K, V> delegate;
	private final Function<? super K, ? extends V> defaultFunction;

	public DefaultFunctionMap(Map<K, V> delegate,
			Function<? super K, ? extends V> defaultFunction) {
		super();
		this.delegate = delegate;
		this.defaultFunction = defaultFunction;
	}

	public V apply(K v) {
		return delegate.ensureAndGet(v, defaultFunction);
	}

	public int clear() {
		return delegate.clear();
	}

	public Iterator<Entry<K, V>> clearAndReturn() {
		return delegate.clearAndReturn();
	}

	public boolean contains(K key, V val) {
		return delegate.contains(key, val);
	}

	public boolean contains(K key) {
		return delegate.contains(key);
	}

	public Map<K, V> copy() {
		return delegate.copy();
	}

	public int count(K key, V val) {
		return delegate.count(key, val);
	}

	public int count(K key) {
		return delegate.count(key);
	}

	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
		return delegate.ensureAndGet(key, putIfNotPresent);
	}

	public V ensureAndGet(K key,
			Function<? super K, ? extends V> putIfNotPresent) {
		return delegate.ensureAndGet(key, putIfNotPresent);
	}

	public Set<Entry<K, V>> entries() {
		return delegate.entries();
	}

	public Iterable<Entry<K, V>> entries(K key) {
		return delegate.entries(key);
	}

	public V get(K key) {
		return delegate.ensureAndGet(key, defaultFunction);
	}

	public AnyMap<V, K> inverse() {
		return delegate.inverse();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Set<K> keys() {
		return delegate.keys();
	}

	public List<V> list(K key) {
		return delegate.list(key);
	}

	public boolean permitsDuplicateKeys() {
		return delegate.permitsDuplicateKeys();
	}

	public boolean add(K key, V val) {
		return delegate.add(key, val);
	}
	
	public V put(K key, V val) {
		return delegate.put(key, val);
	}

	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
		return delegate.putIfAbsent(key, putIfNotPresent);
	}

	public V putIfAbsent(K key, V val) {
		return delegate.putIfAbsent(key, val);
	}

	public int remove(K key, V val) {
		return delegate.remove(key, val);
	}

	public int remove(K key) {
		return delegate.remove(key);
	}

	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		return delegate.removeAndReturn(key, val);
	}

	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		return delegate.removeAndReturn(key);
	}

	public V removeAndReturnFirst(K key) {
		return delegate.removeAndReturnFirst(key);
	}

	public void shrink() {
		delegate.shrink();
	}

	public int size() {
		return delegate.size();
	}

	public int totalCount() {
		return delegate.totalCount();
	}

	public int uniqueKeyCount() {
		return delegate.uniqueKeyCount();
	}

	public AnySet<V> values() {
		return delegate.values();
	}

	public UnitarySet<V> values(K key) {
		return delegate.values(key);
	}

	@Override
	public V first(K key) {
		return delegate.ensureAndGet(key, defaultFunction);
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
