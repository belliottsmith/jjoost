package org.jjoost.collections.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List ;

import org.jjoost.collections.sets.serial.MultiArraySet;
import org.jjoost.collections.sets.serial.SerialHashSet;
import org.jjoost.util.Equality ;
import org.jjoost.util.Factory;
import org.jjoost.util.Filter;
import org.jjoost.util.Filters;
import org.jjoost.util.Function;

public interface HashStore<N extends HashNode<N>> extends Serializable {

	public static enum Locality {
		ADJACENT, SAME_BUCKET, GLOBAL
	}
	
    public int totalCount() ;
    public int uniquePrefixCount() ;
	public boolean isEmpty() ;
	public int clear() ;
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f) ;
	public <NCmp> HashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) ;
	public int capacity() ;
	public void shrink() ;
	public void resize(int size) ;
	
	public <NCmp, V> V put(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> V putIfAbsent(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> V putIfAbsent(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret, boolean returnNewIfCreated) ;
//	public <NCmp, V> V ensureAndGet(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) ;
	
	public <NCmp> boolean removeNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) ;
	public <NCmp> int remove(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) ;
	public <NCmp, V> V removeAndReturnFirst(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	public <NCmp, V> Iterable<V> removeAndReturn(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	
	public <NCmp> boolean contains(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) ;
	public <NCmp> int count(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) ;
	public <NCmp, V> V first(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) ;
	
	public <NCmp, V> List<V> findNow(
			int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> findEq, 
			Function<? super N, ? extends V> ret) ;
	public <NCmp, NCmp2, V> Iterator<V> find(
			int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> findEq, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEq, 
			Function<? super N, ? extends V> ret) ;
	
	public <NCmp, V> Iterator<V> all(
			Function<? super N, ? extends NCmp> nodeEqualityProj, 
			HashNodeEquality<? super NCmp, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) ;
	
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj, 
			Equality<? super NCmp> uniquenessEquality, 
			Locality duplicateLocality,
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) ;
	
	
	// helper classes for implementing unique() method
	
	static final class Helper {
		public static <N, NCmp> Factory<Filter<N>> forUniqueness(
				Function<? super N, ? extends NCmp> uniquenessEqualityProj,
				Equality<? super NCmp> uniquenessEquality, 
				Locality duplicateLocality
			) {
			switch (duplicateLocality) {
			case ADJACENT: 
				return new UniqueSequenceFilterFactory<N, NCmp>(uniquenessEquality, uniquenessEqualityProj) ; 
			case SAME_BUCKET: 
				return new UniqueLocalSetFilterFactory<N, NCmp>(uniquenessEquality, uniquenessEqualityProj) ; 
			case GLOBAL: 
				return new UniqueGlobalSetFilterFactory<N, NCmp>(uniquenessEquality, uniquenessEqualityProj) ; 
			default:
				throw new IllegalStateException() ;
			}			
		}
	}
	
	static final class UniqueSequenceFilterFactory<N, V> implements Factory<Filter<N>> {
		private static final long serialVersionUID = 6287653437378935003L;
		private final Filter<N> filter ;
		public UniqueSequenceFilterFactory(Equality<? super V> eq, Function<? super N, ? extends V> f) {
			this.filter = Filters.mapped(f, Filters.uniqueSeq(eq)) ;
		}
		@Override
		public Filter<N> create() {
			return filter ;
		}
	}
	
	static final class UniqueLocalSetFilterFactory<N, V> implements Factory<Filter<N>> {
		private static final long serialVersionUID = 6287653437378935003L;
		private final MultiArraySet<V> set ;
		private final Filter<N> filter ;
		public UniqueLocalSetFilterFactory(Equality<? super V> eq, Function<? super N, ? extends V> f) {
			this.set = new MultiArraySet<V>(4, eq) ;
			this.filter = Filters.mapped(f, Filters.unique(set)) ;
		}
		@Override
		public Filter<N> create() {
			set.clear() ;
			return filter ;
		}
	}
	
	static final class UniqueGlobalSetFilterFactory<N, V> implements Factory<Filter<N>> {
		private static final long serialVersionUID = 6287653437378935003L;
		private final Filter<N> filter ;
		public UniqueGlobalSetFilterFactory(Equality<? super V> eq, Function<? super N, ? extends V> f) {
			final SerialHashSet<V> set = new SerialHashSet<V>(eq) ;
			this.filter = Filters.mapped(f, Filters.unique(set)) ;
		}
		@Override
		public Filter<N> create() {
			return filter ;
		}
	}
	
}
