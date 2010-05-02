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

	@Override
	public MultiSet<V> values(K key) {
		return map.values(key) ;
	}
	
}
