package org.jjoost.collections.maps.wrappers;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitarySet;
import org.jjoost.collections.base.SynchronizedDelegator ;
import org.jjoost.util.Equality;

public abstract class SynchronizedArbitraryMap<K, V, M extends AnyMap<K, V>> extends SynchronizedDelegator implements AnyMap<K, V> {
	
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
	@Override public synchronized AnyMap<V, K> inverse() {
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
	@Override public synchronized AnySet<V> values() {
		return wrap(delegate.values()) ;
	}

	@SuppressWarnings("hiding")
	protected <V> AnySet<V> wrap(final AnySet<V> delegate) {
		return new AnySet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public Set<V> copy() {
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
			@Override public AnySet<V> unique() {
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
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.equality() ;
				}
			}
		} ;
	}
	
	@SuppressWarnings("hiding")
	protected <V> UnitarySet<V> wrap(final UnitarySet<V> delegate) {
		return new UnitarySet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public UnitarySet<V> copy() {
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
			@Override public UnitarySet<V> unique() {
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
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.equality() ;
				}
			}
			@Override
			public V get() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.get() ;
				}
			}
		} ;
	}
	
	@SuppressWarnings("hiding")
	protected <V> Set<V> wrap(final Set<V> delegate) {
		return new Set<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public Set<V> copy() {
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
			@Override public Set<V> unique() {
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
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.equality() ;
				}
			}
		} ;
	}
	
	@SuppressWarnings("hiding")
	protected <V> MultiSet<V> wrap(final MultiSet<V> delegate) {
		return new MultiSet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public Set<V> unique() {
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
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedArbitraryMap.this) {
					return delegate.equality() ;
				}
			}
		} ;
	}
	
}
