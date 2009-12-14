package org.jjoost.collections.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List ;

import org.jjoost.util.Equality ;
import org.jjoost.util.Function;

public interface HashStore<N extends HashStore.HashNode<N>> extends Serializable {

	public static abstract class HashNode<N extends HashNode<N>> implements Serializable {
		private static final long serialVersionUID = 2035712133283347382L;
		public final int hash ;
		public HashNode(int hash) {
			this.hash = hash ;
		}
		public abstract N copy() ;
		public final int hash() { return hash ; }
	}
	
    public int totalCount() ;
    public int uniquePrefixCount() ;
	public boolean isEmpty() ;
	public int clear() ;
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f) ;
	public HashStore<N> copy() ;
	public void shrink() ;
	public void resize(int size) ;
	
	public <NCmp, V> V put(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> V putIfAbsent(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> V putIfAbsent(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> V ensureAndGet(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) ;
	
	public <NCmp> boolean removeNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) ;
	public <NCmp> int remove(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) ;
	public <NCmp, V> V removeAndReturnFirst(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> Iterable<V> removeAndReturn(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	
	public <NCmp> boolean contains(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) ;
	public <NCmp> int count(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) ;
	public <NCmp, V> V first(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	
	public <NCmp, V> List<V> findNow(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> Iterator<V> find(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq, Function<? super N, ? extends NCmp> nodePrefixEqFunc, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> Iterator<V> unique(Function<? super N, ? extends NCmp> eqF, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Equality<? super NCmp> forceUniq, Function<? super N, ? extends V> ret) ;
	
	
}
