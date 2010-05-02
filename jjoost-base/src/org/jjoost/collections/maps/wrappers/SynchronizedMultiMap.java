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

import java.util.Map.Entry ;

import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.MultiMap;
import org.jjoost.collections.Set ;

public class SynchronizedMultiMap<K, V> extends SynchronizedArbitraryMap<K, V, MultiMap<K, V>> implements MultiMap<K, V> {
	
	private static final long serialVersionUID = 2692454383540344975L;
	public SynchronizedMultiMap(MultiMap<K, V> delegate) {
		super(delegate) ;
	}
	
	private MultiSet<K> keySet ;
	private Set<Entry<K, V>> entrySet ;
	@Override public synchronized MultiSet<K> keys() {
		if (keySet == null)
			keySet = wrap(delegate.keys()) ;
		return keySet ;
	}
	@Override public synchronized Set<Entry<K, V>> entries() {
		if (entrySet == null)
			entrySet = wrap(delegate.entries()) ;
		return entrySet ;
	}
	
	@Override public synchronized Iterable<V> apply(K v) {
		return wrap(delegate.apply(v)) ;
	}
	@Override public synchronized MultiMap<K, V> copy() {
		return new SynchronizedMultiMap<K, V>(delegate.copy()) ;
	}
	@Override public synchronized Set<V> values(K key) {
		return wrap(delegate.values(key)) ;
	}

}
