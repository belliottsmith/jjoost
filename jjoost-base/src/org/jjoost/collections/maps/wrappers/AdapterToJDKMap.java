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

import java.util.Collection ;
import java.util.Iterator ;

import org.jjoost.collections.Map ;
import org.jjoost.collections.sets.wrappers.AdapterToJDKSet ;
import org.jjoost.util.Iters ;
import org.jjoost.util.Objects ;

public class AdapterToJDKMap<K, V> implements java.util.Map<K, V> {
	
	private final Class<K> keyClazz ;
	private final Class<V> valueClazz ;
	private final Map<K, V> map ;
	private java.util.Set<K> keySet ;
	private java.util.Set<Entry<K, V>> entrySet ;
	private Collection<V> valueCollection ;
	
	public AdapterToJDKMap(Class<K> keyClazz, Class<V> valueClazz, Map<K, V> map) {
		super() ;
		this.keyClazz = keyClazz ;
		this.valueClazz = valueClazz ;
		this.map = map ;
	}
	
	@Override
	public void clear() {
		map.clear() ;
	}
	@Override
	public boolean containsKey(Object key) {
		return map.contains(keyClazz.cast(key)) ;
	}
	@Override
	public boolean containsValue(Object value) {
		return Iters.contains(valueClazz.cast(value), map.values()) ;
	}
	@SuppressWarnings("unchecked")
	@Override
	public java.util.Set<java.util.Map.Entry<K, V>> entrySet() {
		if (entrySet == null)
			entrySet = new AdapterToJDKSet<Entry<K, V>>((Class<Entry<K, V>>) (Class<?>) Entry.class, map.entries()) ;
		return entrySet ;
	}
	@Override
	public V get(Object key) {
		return map.get(keyClazz.cast(key)) ;
	}
	@Override
	public boolean isEmpty() {
		return map.isEmpty() ;
	}
	@Override
	public java.util.Set<K> keySet() {
		if (keySet == null)
			keySet = new AdapterToJDKSet<K>(keyClazz, map.keys()) ;
		return keySet ;
	}
	@Override
	public V put(K key, V value) {
		return map.put(key, value) ;
	}
	@Override
	public void putAll(java.util.Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> e : m.entrySet())
			map.put(e.getKey(), e.getValue()) ;
	}
	@Override
	public V remove(Object key) {
		return map.removeAndReturnFirst(keyClazz.cast(key)) ;
	}
	@Override
	public int size() {
		return map.size() ;
	}
	@Override
	public Collection<V> values() {
		if (valueCollection == null)
			valueCollection = new ValueCollection() ;
		return valueCollection ;
	}
	
	private final class ValueCollection implements Collection<V> {

		@Override
		public boolean add(V e) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public void clear() {
			map.clear() ;
		}

		@Override
		public boolean contains(Object o) {
			return containsValue(o) ;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			boolean r = true ;
			final Iterator<?> iter = c.iterator() ;
			while (r && iter.hasNext())
				r = contains(iter.next() );
			return r ;
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty() ;
		}

		@Override
		public Iterator<V> iterator() {
			return map.values().iterator() ;
		}

		@Override
		public boolean remove(Object o) {
			boolean r = false ;
			final Iterator<V> iter = values().iterator() ;
			while (iter.hasNext()) {
				if (Objects.equalQuick(iter.next(), o)) {
					iter.remove() ;
					r = true ;
				}
			}
			return r ;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean r = false ;
			final Iterator<V> iter = values().iterator() ;
			while (iter.hasNext()) {
				if (c.contains(iter.next())) {
					iter.remove() ;
					r = true ;
				}
			}
			return r ;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			boolean r = false ;
			final Iterator<V> iter = values().iterator() ;
			while (iter.hasNext()) {
				if (!c.contains(iter.next())) {
					iter.remove() ;
					r = true ;
				}
			}
			return r ;
		}

		@Override
		public int size() {
			return map.size() ;
		}

		@Override
		public Object[] toArray() {
			return Iters.toArray(this) ;
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return Iters.toArray(this, a) ;
		}
		
	}

}
