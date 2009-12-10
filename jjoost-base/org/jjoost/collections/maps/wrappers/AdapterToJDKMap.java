package org.jjoost.collections.maps.wrappers;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Set ;

import org.jjoost.collections.ScalarMap ;
import org.jjoost.collections.sets.wrappers.AdapterToJDKSet ;
import org.jjoost.util.Iters ;
import org.jjoost.util.Objects ;

public class AdapterToJDKMap<K, V> implements Map<K, V> {
	
	private final Class<K> keyClazz ;
	private final Class<V> valueClazz ;
	private final ScalarMap<K, V> map ;
	private Set<K> keySet ;
	private Set<Entry<K, V>> entrySet ;
	private Collection<V> valueCollection ;
	
	public AdapterToJDKMap(Class<K> keyClazz, Class<V> valueClazz, ScalarMap<K, V> map) {
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
	public Set<java.util.Map.Entry<K, V>> entrySet() {
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
	public Set<K> keySet() {
		if (keySet == null)
			keySet = new AdapterToJDKSet<K>(keyClazz, map.keys()) ;
		return keySet ;
	}
	@Override
	public V put(K key, V value) {
		return map.put(key, value) ;
	}
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
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
