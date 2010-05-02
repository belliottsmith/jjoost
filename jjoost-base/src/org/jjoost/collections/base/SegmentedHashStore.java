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

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.jjoost.util.Equality ;
import org.jjoost.util.Factory ;
import org.jjoost.util.Function ;
import org.jjoost.util.Iters ;

public class SegmentedHashStore<N extends HashNode<N>> implements HashStore<N> {
	
	private static final long serialVersionUID = -5186207371319394054L ;
	
	private final HashStore<N>[] segments ;
	private final int segmentShift ;
	
	public SegmentedHashStore(int segments, Factory<HashStore<N>> factory) {
		this(makeSegments(segments, factory)) ; 
	}
	public SegmentedHashStore(HashStore<N>[] segments) {
		if (Integer.bitCount(segments.length) != 1)
			throw new IllegalArgumentException("Number of segments provided must be a power of 2") ;
		this.segments = segments ;
		this.segmentShift = 32 - Integer.bitCount((segments.length - 1)) ;
	}

	@SuppressWarnings("unchecked")
	private static <N extends HashNode<N>> HashStore<N>[] makeSegments(int segments, Factory<HashStore<N>> factory) {
		final HashStore<N>[] r = new HashStore[segments] ;
		for (int i = 0 ; i != segments ; i++)
			r[i] = factory.create() ;
		return r ;
	}
	
	private final HashStore<N> segmentFor(int hash) {
		return segments[hash >>> segmentShift] ;
	}
	@SuppressWarnings("unchecked")
	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodePrefixEqFunc,
			HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, Function<? super N, ? extends V> ret) {
		Iterator<V>[] iters = new Iterator[segments.length] ;
		for (int i = 0 ; i != segments.length ; i++)
			iters[i] = segments[i].all(nodePrefixEqFunc, nodePrefixEq, ret) ;
		return Iters.concat(Arrays.asList(iters).iterator()) ;
	}

	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj, 
			Equality<? super NCmp> uniquenessEquality, 
			Locality duplicateLocality, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		throw new UnsupportedOperationException() ;
//		Iterator<V>[] iters = new Iterator[segments.length] ;
//		for (int i = 0 ; i != segments.length ; i++)
//			iters[i] = segments[i].unique(uniquenessEqualityProj, uniquenessEquality, nodeEqualityProj, nodeEquality, ret) ;
//		return Iters.concat(Arrays.asList(iters).iterator()) ;
	}
	@Override
	public int clear() {
		int c = 0 ;
		for (HashStore<N> segment : segments)
			c += segment.clear() ;
		return c ;
	}
	@SuppressWarnings("unchecked")
	@Override
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f) {
		Iterator<V>[] iters = new Iterator[segments.length] ;
		for (int i = 0 ; i != segments.length ; i++)
			iters[i] = segments[i].clearAndReturn(f) ;
		return Iters.concat(Arrays.asList(iters).iterator()) ;
	}
	@Override
	public <NCmp> HashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		throw new UnsupportedOperationException() ;
	}
	@Override
	public <NCmp> int count(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, int countUpTo) {
		return segmentFor(hash).count(hash, find, eq, countUpTo) ;
	}
	@Override
	public <NCmp, NCmp2, V> Iterator<V> find(
			int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> findEq, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEq, 
			Function<? super N, ? extends V> ret) {
		return segmentFor(hash).find(hash, find, findEq, nodeEqualityProj, nodeEq, ret) ;
	}
	@Override
	public <NCmp, V> V first(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return segmentFor(hash).first(hash, find, eq, ret) ;
	}
	@Override
	public boolean isEmpty() {
		for (HashStore<N> segment : segments)
			if (!segment.isEmpty())
				return false ;
		return true ;
	}
	@Override
	public <NCmp, V> V put(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return segmentFor(put.hash).put(find, put, eq, ret) ;
	}
	@Override
	public <NCmp, V> V putIfAbsent(int hash, NCmp put, HashNodeEquality<? super NCmp, ? super N> eq,
			HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret, boolean returnNewIfCreated) {
		return segmentFor(hash).putIfAbsent(hash, put, eq, factory, ret, returnNewIfCreated) ;
	}
	@Override
	public <NCmp, V> V putIfAbsent(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		return segmentFor(put.hash).putIfAbsent(find, put, eq, ret) ;
	}
	@Override
	public <NCmp> int remove(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		return segmentFor(hash).remove(hash, removeAtMost, find, eq) ;
	}
	@Override
	public <NCmp, V> Iterable<V> removeAndReturn(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq,
			Function<? super N, ? extends V> ret) {
		return segmentFor(hash).removeAndReturn(hash, removeAtMost, find, eq, ret) ;
	}
	@Override
	public <NCmp, V> V removeAndReturnFirst(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq,
			Function<? super N, ? extends V> ret) {
		return segmentFor(hash).removeAndReturnFirst(hash, removeAtMost, find, eq, ret) ;
	}
	@Override
	public <NCmp> boolean removeNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc,
			HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {
		return segmentFor(n.hash).removeNode(nodePrefixEqFunc, nodePrefixEq, n) ;
	}
	@Override
	public void resize(int size) {
		throw new UnsupportedOperationException() ;
	}
	@Override
	public void shrink() {
		for (HashStore<N> segment : segments)
			segment.shrink() ;
	}
	@Override
	public int totalCount() {
		int tc = 0 ;
		for (HashStore<N> segment : segments)
			tc+= segment.totalCount() ;
		return tc ;
	}
	@Override
	public int uniquePrefixCount() {
		int c = 0 ;
		for (HashStore<N> segment : segments)
			c+= segment.uniquePrefixCount() ;
		return c ;
	}
	@Override
	public int capacity() {
		int c = 0 ;
		for (HashStore<N> segment : segments)
			c+= segment.capacity() ;
		return c ;
	}
	@Override
	public <NCmp, V> List<V> findNow(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq,
			Function<? super N, ? extends V> ret) {
		return segmentFor(hash).findNow(hash, find, findEq, ret) ;
	}
	
}
