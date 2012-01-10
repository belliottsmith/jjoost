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

package org.jjoost.collections.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.sets.serial.MultiArraySet;
import org.jjoost.collections.sets.serial.SerialHashSet;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Filter;
import org.jjoost.util.Filters;
import org.jjoost.util.Function;
import org.jjoost.util.filters.MappedFilter;

public interface HashStore<N, S extends HashStore<N, S>> extends Serializable {

	public static enum Locality {
		ADJACENT, SAME_BUCKET, GLOBAL
	}
	
    public int totalCount();
    public int uniquePrefixCount();
	public boolean isEmpty();
	public int clear();
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f);
	public <F> S copy(Function<? super N, ? extends F> nodeEqualityProj, HashNodeEquality<? super F, ? super N> nodeEquality);
	public int capacity();
	
	public static enum PutAction {
		PUT, IFABSENT, REPLACE, ENSUREANDGET
	}
	
	// ENSUREANDGET is not a valid action for this version of put; only for the factory version
	public <F, V> V put(PutAction action, F find, N put, HashNodeEquality<? super F, ? super N> eq, Function<? super N, ? extends V> ret);
	public <F, V> V put(PutAction action, int hash, F put, HashNodeEquality<? super F, ? super N> eq, HashNodeFactory<? super F, N> factory, Function<? super N, ? extends V> ret);
	
	public <F> boolean removeNode(Function<? super N, ? extends F> nodePrefixEqFunc, HashNodeEquality<? super F, ? super N> nodePrefixEq, N n);
	public <F> int remove(int hash, int removeAtMost, F find, HashNodeEquality<? super F, ? super N> eq);
	public <F, V> V removeAndReturnFirst(int hash, int removeAtMost, F find, HashNodeEquality<? super F, ? super N> eq, Function<? super N, ? extends V> ret);
	public <F, V> Iterable<V> removeAndReturn(int hash, int removeAtMost, F find, HashNodeEquality<? super F, ? super N> eq, Function<? super N, ? extends V> ret);

	public <F> int count(int hash, F find, HashNodeEquality<? super F, ? super N> eq, int countUpTo);
	public <F, V> V first(int hash, F find, HashNodeEquality<? super F, ? super N> eq, Function<? super N, ? extends V> ret);
	
	public <F, V> List<V> findNow(
			int hash, F find, 
			HashNodeEquality<? super F, ? super N> findEq, 
			Function<? super N, ? extends V> ret);
	public <F, F2, V> Iterator<V> find(
			int hash, F find, 
			HashNodeEquality<? super F, ? super N> findEq, 
			Function<? super N, ? extends F2> nodeEqualityProj, 
			HashNodeEquality<? super F2, ? super N> nodeEq, 
			Function<? super N, ? extends V> ret);
	
	public <F, V> Iterator<V> all(
			Function<? super N, ? extends F> nodeEqualityProj, 
			HashNodeEquality<? super F, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret);
	
	public <F, F2, V> Iterator<V> unique(
			Function<? super N, ? extends F> uniquenessEqualityProj, 
			Equality<? super F> uniquenessEquality, 
			Locality duplicateLocality,
			Function<? super N, ? extends F2> nodeEqualityProj, 
			HashNodeEquality<? super F2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret);
	
	
	// helper classes for implementing unique() method
	
	static final class Helper {

		public static <N extends HashNode<N>, F> Filter<N> forUniqueness(
				Function<? super N, ? extends F> uniquenessEqualityProj,
				Equality<? super F> uniquenessEquality, 
				Locality duplicateLocality
		) {
			switch (duplicateLocality) {
			case ADJACENT: 
				return Filters.mapped(uniquenessEqualityProj, Filters.uniqueSeq(uniquenessEquality));
			case SAME_BUCKET: 
				return new UniqueLocalSetFilter<N, F>(uniquenessEquality, uniquenessEqualityProj);
			case GLOBAL: 
				return Filters.mapped(uniquenessEqualityProj, Filters.unique(new SerialHashSet<F>(uniquenessEquality)));
			default:
				throw new IllegalStateException();
			}			
		}
		
		public static boolean cmp(int revA, int revB) {
			return (revA < revB) ^ ((revB > 0) != (revA > 0));
		}

		public static boolean revThenCmp(int revA, int b) {
			return cmp(revA, Integer.reverse(b));
		}

	}
	
	static final class UniqueLocalSetFilter<N extends HashNode<N>, V> extends MappedFilter<N, V> {
		private static final long serialVersionUID = 6287653437378935003L;
		private final AnySet<V> set;
		private int previousHash = -1;
		public UniqueLocalSetFilter(Equality<? super V> eq, Function<? super N, ? extends V> f) {
			this(new MultiArraySet<V>(4, eq), f);
		}
		private UniqueLocalSetFilter(AnySet<V> set, Function<? super N, ? extends V> f) {
			super(f, Filters.unique(set));
			this.set = set;
		}
		@Override
		public boolean accept(N n) {
			if (previousHash != n.hash)
				set.clear();
			return super.accept(n);
		}
	}
	
	static final class UniqueSequenceFilterFactory<N, V> implements Factory<Filter<N>> {
		private static final long serialVersionUID = 6287653437378935003L;
		private final Filter<N> filter;
		public UniqueSequenceFilterFactory(Equality<? super V> eq, Function<? super N, ? extends V> f) {
			this.filter = Filters.mapped(f, Filters.uniqueSeq(eq));
		}
		@Override
		public Filter<N> create() {
			return filter;
		}
	}
	
	static final class UniqueLocalSetFilterFactory<N, V> implements Factory<Filter<N>> {
		private static final long serialVersionUID = 6287653437378935003L;
		private final MultiArraySet<V> set;
		private final Filter<N> filter;
		public UniqueLocalSetFilterFactory(Equality<? super V> eq, Function<? super N, ? extends V> f) {
			this.set = new MultiArraySet<V>(4, eq);
			this.filter = Filters.mapped(f, Filters.unique(set));
		}
		@Override
		public Filter<N> create() {
			set.clear();
			return filter;
		}
	}
	
	static final class UniqueGlobalSetFilterFactory<N, V> implements Factory<Filter<N>> {
		private static final long serialVersionUID = 6287653437378935003L;
		private final Filter<N> filter;
		public UniqueGlobalSetFilterFactory(Equality<? super V> eq, Function<? super N, ? extends V> f) {
			final SerialHashSet<V> set = new SerialHashSet<V>(eq);
			this.filter = Filters.mapped(f, Filters.unique(set));
		}
		@Override
		public Filter<N> create() {
			return filter;
		}
	}
	
}
