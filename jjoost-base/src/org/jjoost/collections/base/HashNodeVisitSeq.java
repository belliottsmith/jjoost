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

package org.jjoost.collections.base ;

import java.util.Arrays ;

import org.jjoost.util.Function ;

@SuppressWarnings("unchecked")
final class HashNodeVisitSeq<N extends HashNode<N>, NCmp> extends HashNodeVisits<N, NCmp> {
	
	private N[] nodes = (N[]) new HashNode[4] ;
	private int count = 0 ;
	private int i = 0 ;
	
	HashNodeVisitSeq(int hash) {
		super(hash) ;
	}

	public void visit(N n) {
		if (n.hash != hash)
			throw new IllegalStateException() ;
		if (count == nodes.length)
			nodes = Arrays.copyOf(nodes, count << 1) ;
		nodes[count++] = n ;
	}
	
	public void removeLast() {
		count-- ;
	}
	
	public void reset(int hash) {
		count = 0 ;
		this.hash = hash ;
	}
	
	public int hash() {
		return hash ;
	}
	
	public boolean isEmpty() {
		return count == 0 ;
	}
	
	public N last() {
		return nodes[count-1] ;
	}
	
	public void revisit() {
		i = 0 ;
	}
	
	public boolean haveVisitedAlready(N n, Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		if (i == count)
			return false ;
		final int lasti = i ;
		final NCmp c = nodeEqualityProj.apply(n) ;
		while (i != count && 
				!(nodeEquality.prefixMatch(c, nodes[i]) 
				&& nodeEquality.suffixMatch(c, nodes[i]))) {
			i++ ;
		}
		if (i == count) {
			count = lasti ;
			return false ;
		} else {
			i++ ;
			return true ;
		}
	}
	
}
