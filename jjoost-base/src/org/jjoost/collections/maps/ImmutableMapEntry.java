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

package org.jjoost.collections.maps;

import java.util.Map;
import java.util.Map.Entry;

import org.jjoost.util.Objects;

public final class ImmutableMapEntry<K, V> implements Map.Entry<K, V> {

	private final K key;
	private final V value;
	
	public ImmutableMapEntry(K key, V value) {
		super();
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that) {
		return that instanceof Entry && equals((Entry<?, ?>) that);
	}

	public boolean equals(Entry<?, ?> that) {
		return Objects.equalQuick(this.key, that.getKey()) && Objects.equalQuick(this.value, that.getValue());
	}
	
	public String toString() {
		return "{" + key + " -> " + value + "}";
	}
	
}
