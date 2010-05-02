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

import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.MultiMap ;
import org.jjoost.collections.Map ;
import org.jjoost.collections.Set ;

public class BiMapManyToOne<K, V> extends AbstractBiMap<K, V, MultiMap<K, V>, Map<V, K>> implements MultiMap<K, V> {

	private static final long serialVersionUID = -3696446893675439338L ;

	private final BiMapOneToMany<V, K> partner ;
	@Override protected final AbstractBiMap<V, K, Map<V, K>, MultiMap<K, V>> partner() {
		return partner ;
	}

	public BiMapManyToOne(MultiMap<K, V> forwards, Map<V, K> back) {
		super(forwards) ;
		this.partner = new BiMapOneToMany<V, K>(back, this) ;
	}

	private BiMapManyToOne(MultiMap<K, V> forwards, BiMapOneToMany<V, K> partner) {
		super(forwards) ;
		this.partner = partner ;
	}
	
	@Override
	public MultiMap<K, V> copy() {
		final MultiMap<K, V> fwds = map.copy() ;
		final Map<V, K> back = partner().map.copy() ;		
		return new BiMapManyToOne<K, V>(fwds, back) ;
	}

	public Map<V, K> inverse() {
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
