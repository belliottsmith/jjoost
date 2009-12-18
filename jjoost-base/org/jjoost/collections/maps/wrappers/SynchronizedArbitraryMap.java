package org.jjoost.collections.maps.wrappers;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.ArbitraryMap;
import org.jjoost.collections.ArbitrarySet;
import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.ScalarSet;
import org.jjoost.collections.base.SynchronizedDelegator ;

public abstract class SynchronizedArbitraryMap<K, V, M extends ArbitraryMap<K, V>> extends SynchronizedDelegator implements ArbitraryMap<K, V> {
	
	private static final long serialVersionUID = -7183655836427941893L ;
	
	final M delegate ;
	
	public SynchronizedArbitraryMap(M delegate) {
		this.delegate = delegate;
	}
	
	@Override public synchronized int clear() {
		return delegate.clear();
	}
	@Override public synchronized Iterator<Entry<K, V>> clearAndReturn() {
		return wrap(delegate.clearAndReturn()) ;
	}
	@Override public synchronized boolean contains(K key, V val) {
		return delegate.contains(key, val);
	}
	@Override public synchronized boolean contains(K key) {
		return delegate.contains(key);
	}
	@Override public synchronized int count(K key, V val) {
		return delegate.count(key, val);
	}
	@Override public synchronized int count(K key) {
		return delegate.count(key);
	}
	@Override public synchronized Iterable<Entry<K, V>> entries(K key) {
		return wrap(delegate.entries(key)) ;
	}
	@Override public synchronized V first(K key) {
		return delegate.first(key);
	}
	@Override public synchronized ArbitraryMap<V, K> inverse() {
		return delegate.inverse();
	}
	@Override public synchronized boolean isEmpty() {
		return delegate.isEmpty();
	}
	@Override public synchronized List<V> list(K key) {
		return delegate.list(key);
	}
	@Override public synchronized boolean permitsDuplicateKeys() {
		return delegate.permitsDuplicateKeys();
	}
	@Override public synchronized V put(K key, V val) {
		return delegate.put(key, val);
	}
	@Override public synchronized V putIfAbsent(K key, V val) {
		return delegate.putIfAbsent(key, val);
	}
	@Override public synchronized int remove(K key, V val) {
		return delegate.remove(key, val);
	}
	@Override public synchronized int remove(K key) {
		return delegate.remove(key);
	}
	@Override public synchronized Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		return wrap(delegate.removeAndReturn(key, val)) ;
	}
	@Override public synchronized Iterable<Entry<K, V>> removeAndReturn(K key) {
		return wrap(delegate.removeAndReturn(key)) ;
	}
	@Override public synchronized V removeAndReturnFirst(K key) {
		return delegate.removeAndReturnFirst(key);
	}
	@Override public synchronized void shrink() {
		delegate.shrink();
	}
	@Override public synchronized int totalCount() {
		return delegate.totalCount();
	}
	@Override public synchronized int uniqueKeyCount() {
		return delegate.uniqueKeyCount();
	}
	@Override public synchronized Iterable<V> values() {
		return wrap(delegate.values()) ;
	}

	@SuppressWarnings("hiding")
	protected <V> ArbitrarySet<V> wrap(final ArbitrarySet<V> delegate) {
		return new ArbitrarySet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
			@Override public Iterable<V> all() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.all()) ;
				}
			}
			@Override public Iterable<V> all(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.all(value)) ;
				}
			}
			@Override public Boolean apply(V v) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.apply(v) ;
				}
			}
			@Override public int clear() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.clear() ;
				}
			}
			@Override public Iterator<V> clearAndReturn() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.clearAndReturn() ;
				}
			}
			@Override public boolean contains(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.contains(value) ;
				}
			}
			@Override public ScalarSet<V> copy() {
				throw new UnsupportedOperationException() ;
			}
			@Override public int count(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.count(value) ;
				}
			}
			@Override public V first(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.first(value) ;
				}
			}
			@Override public boolean isEmpty() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.isEmpty() ;
				}
			}
			@Override public Iterator<V> iterator() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.iterator()) ;
				}
			}
			@Override public List<V> list(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.list(value) ;
				}
			}
			@Override public boolean permitsDuplicates() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.permitsDuplicates() ;
				}
			}
			@Override public V put(V val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.put(val) ;
				}
			}
			@Override public int putAll(Iterable<V> val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.putAll(val) ;
				}
			}
			@Override public V putIfAbsent(V val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.putIfAbsent(val) ;
				}
			}
			@Override public int remove(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.remove(value) ;
				}
			}
			@Override public Iterable<V> removeAndReturn(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.removeAndReturn(value)) ;
				}
			}
			@Override public V removeAndReturnFirst(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturnFirst(value) ;
				}
			}
			@Override public void shrink() {
				synchronized(SynchronizedArbitraryMap.this) {
					delegate.shrink() ;
				}
			}
			@Override public int totalCount() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.totalCount() ;
				}
			}
			@Override public Iterable<V> unique() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.unique()) ;
				}
			}
			@Override public int uniqueCount() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.uniqueCount() ;
				}
			}
			@Override
			public int remove(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.remove(value, removeAtMost) ;
				}
			}
			@Override
			public Iterable<V> removeAndReturn(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturn(value, removeAtMost) ;
				}
			}
			@Override
			public V removeAndReturnFirst(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturnFirst(value, removeAtMost) ;
				}
			}
		} ;
	}
	
	@SuppressWarnings("hiding")
	protected <V> ScalarSet<V> wrap(final ScalarSet<V> delegate) {
		return new ScalarSet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
			@Override public Iterable<V> all() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.all()) ;
				}
			}
			@Override public Iterable<V> all(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.all(value)) ;
				}
			}
			@Override public Boolean apply(V v) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.apply(v) ;
				}
			}
			@Override public int clear() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.clear() ;
				}
			}
			@Override public Iterator<V> clearAndReturn() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.clearAndReturn() ;
				}
			}
			@Override public boolean contains(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.contains(value) ;
				}
			}
			@Override public ScalarSet<V> copy() {
				throw new UnsupportedOperationException() ;
			}
			@Override public int count(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.count(value) ;
				}
			}
			@Override public V first(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.first(value) ;
				}
			}
			@Override public boolean isEmpty() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.isEmpty() ;
				}
			}
			@Override public Iterator<V> iterator() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.iterator()) ;
				}
			}
			@Override public List<V> list(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.list(value) ;
				}
			}
			@Override public boolean permitsDuplicates() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.permitsDuplicates() ;
				}
			}
			@Override public V put(V val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.put(val) ;
				}
			}
			@Override public int putAll(Iterable<V> val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.putAll(val) ;
				}
			}
			@Override public V putIfAbsent(V val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.putIfAbsent(val) ;
				}
			}
			@Override public int remove(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.remove(value) ;
				}
			}
			@Override public Iterable<V> removeAndReturn(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.removeAndReturn(value)) ;
				}
			}
			@Override public V removeAndReturnFirst(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturnFirst(value) ;
				}
			}
			@Override public void shrink() {
				synchronized(SynchronizedArbitraryMap.this) {
					delegate.shrink() ;
				}
			}
			@Override public int totalCount() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.totalCount() ;
				}
			}
			@Override public Iterable<V> unique() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.unique()) ;
				}
			}
			@Override public int uniqueCount() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.uniqueCount() ;
				}
			}
			@Override
			public V get(V key) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.get(key) ;
				}
			}
			@Override public int size() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.size() ;
				}
			}
			@Override
			public int remove(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.remove(value, removeAtMost) ;
				}
			}
			@Override
			public Iterable<V> removeAndReturn(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturn(value, removeAtMost) ;
				}
			}
			@Override
			public V removeAndReturnFirst(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturnFirst(value, removeAtMost) ;
				}
			}
		} ;
	}
	
	@SuppressWarnings("hiding")
	protected <V> MultiSet<V> wrap(final MultiSet<V> delegate) {
		return new MultiSet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
			@Override public Iterable<V> all() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.all()) ;
				}
			}
			@Override public Iterable<V> all(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.all(value)) ;
				}
			}
			@Override public Boolean apply(V v) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.apply(v) ;
				}
			}
			@Override public int clear() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.clear() ;
				}
			}
			@Override public Iterator<V> clearAndReturn() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.clearAndReturn() ;
				}
			}
			@Override public boolean contains(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.contains(value) ;
				}
			}
			@Override public MultiSet<V> copy() {
				throw new UnsupportedOperationException() ;
			}
			@Override public int count(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.count(value) ;
				}
			}
			@Override public V first(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.first(value) ;
				}
			}
			@Override public boolean isEmpty() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.isEmpty() ;
				}
			}
			@Override public Iterator<V> iterator() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.iterator()) ;
				}
			}
			@Override public List<V> list(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.list(value) ;
				}
			}
			@Override public boolean permitsDuplicates() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.permitsDuplicates() ;
				}
			}
			@Override public V put(V val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.put(val) ;
				}
			}
			@Override public void put(V val, int count) {
				synchronized(SynchronizedArbitraryMap.this) {
					delegate.put(val, count) ;
				}
			}
			@Override public int putAll(Iterable<V> val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.putAll(val) ;
				}
			}
			@Override public V putIfAbsent(V val) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.putIfAbsent(val) ;
				}
			}
			@Override public int remove(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.remove(value) ;
				}
			}
			@Override public Iterable<V> removeAndReturn(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.removeAndReturn(value)) ;
				}
			}
			@Override public V removeAndReturnFirst(V value) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturnFirst(value) ;
				}
			}
			@Override public void shrink() {
				synchronized(SynchronizedArbitraryMap.this) {
					delegate.shrink() ;
				}
			}
			@Override public int totalCount() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.totalCount() ;
				}
			}
			@Override public Iterable<V> unique() {
				synchronized(SynchronizedArbitraryMap.this) {
					return wrap(delegate.unique()) ;
				}
			}
			@Override public int uniqueCount() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.uniqueCount() ;
				}
			}			
			@Override
			public int remove(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.remove(value, removeAtMost) ;
				}
			}
			@Override
			public Iterable<V> removeAndReturn(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturn(value, removeAtMost) ;
				}
			}
			@Override
			public V removeAndReturnFirst(V value, int removeAtMost) {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.removeAndReturnFirst(value, removeAtMost) ;
				}
			}
		} ;
	}
	
}
