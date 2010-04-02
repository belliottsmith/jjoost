package org.jjoost.collections.base;

import java.util.Iterator ;
import java.util.List ;

import org.jjoost.util.Equality ;
import org.jjoost.util.Function ;

public class SynchronizedHashStore<N extends HashNode<N>> extends SynchronizedDelegator implements HashStore<N> {
	
	private static final long serialVersionUID = 5588736896279488162L ;
	
	private final HashStore<N> delegate ;
	public SynchronizedHashStore(HashStore<N> delegate) {
		super() ;
		this.delegate = delegate ;
	}
	
	public synchronized <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality, Function<? super N, ? extends V> ret) {
		return wrap(delegate.all(nodeEqualityProj, nodeEquality, ret)) ;
	}
	public synchronized int clear() {
		return delegate.clear() ;
	}
	public synchronized <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f) {
		return delegate.clearAndReturn(f) ;
	}
	public synchronized <NCmp> boolean contains(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		return delegate.contains(hash, find, eq) ;
	}
	public synchronized <NCmp> HashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		return new SynchronizedHashStore<N>(delegate.copy(nodeEqualityProj, nodeEquality)) ;
	}
	public synchronized <NCmp> int count(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		return delegate.count(hash, find, eq) ;
	}
	public synchronized <NCmp, NCmp2, V> Iterator<V> find(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq,
		Function<? super N, ? extends NCmp2> nodeEqualityProj, HashNodeEquality<? super NCmp2, ? super N> nodeEq,
		Function<? super N, ? extends V> ret) {
		return wrap(delegate.find(hash, find, findEq, nodeEqualityProj, nodeEq, ret)) ;
	}
	public synchronized <NCmp, V> List<V> findNow(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq,
		Function<? super N, ? extends V> ret) {
		return delegate.findNow(hash, find, findEq, ret) ;
	}
	public synchronized <NCmp, V> V first(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return delegate.first(hash, find, eq, ret) ;
	}
	public synchronized boolean isEmpty() {
		return delegate.isEmpty() ;
	}
	public synchronized <NCmp, V> V put(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return delegate.put(find, put, eq, ret) ;
	}
	public synchronized <NCmp, V> V putIfAbsent(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq,
		HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret, boolean returnNewIfCreated) {
		return delegate.putIfAbsent(hash, put, eq, factory, ret, returnNewIfCreated) ;
	}
	public synchronized <NCmp, V> V putIfAbsent(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return delegate.putIfAbsent(find, put, eq, ret) ;
	}
	public synchronized <NCmp> int remove(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		return delegate.remove(hash, removeAtMost, find, eq) ;
	}
	public synchronized <NCmp, V> Iterable<V> removeAndReturn(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq,
		Function<? super N, ? extends V> ret) {
		return delegate.removeAndReturn(hash, removeAtMost, find, eq, ret) ;
	}
	public synchronized <NCmp, V> V removeAndReturnFirst(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq,
		Function<? super N, ? extends V> ret) {
		return delegate.removeAndReturnFirst(hash, removeAtMost, find, eq, ret) ;
	}
	public synchronized <NCmp> boolean removeNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc,
		HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {
		return delegate.removeNode(nodePrefixEqFunc, nodePrefixEq, n) ;
	}
	public synchronized void resize(int size) {
		delegate.resize(size) ;
	}
	public synchronized void shrink() {
		delegate.shrink() ;
	}
	public synchronized int totalCount() {
		return delegate.totalCount() ;
	}
	public synchronized int capacity() {
		return delegate.capacity() ;
	}
	public synchronized <NCmp, NCmp2, V> Iterator<V> unique(
		Function<? super N, ? extends NCmp> uniquenessEqualityProj,
		Equality<? super NCmp> uniquenessEquality, 
		Locality duplicateLocality, 
		Function<? super N, ? extends NCmp2> nodeEqualityProj,
		HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
		Function<? super N, ? extends V> ret) {
			return wrap(delegate.unique(uniquenessEqualityProj, uniquenessEquality, 
					duplicateLocality, nodeEqualityProj, nodeEquality, ret)) ;
	}
	public synchronized int uniquePrefixCount() {
		return delegate.uniquePrefixCount() ;
	}

}
