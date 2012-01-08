package org.jjoost.collections.base;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jjoost.util.Function;

abstract class AbstractHashNodeIterator<N, NCmp, V> implements Iterator<V> {
	
	protected final Function<? super N, ? extends NCmp> nodeEqualityProj;
	protected final HashNodeEquality<? super NCmp, ? super N> nodeEquality;
	protected final Function<? super N, ? extends V> ret;
	protected N[] nextNodes;
	protected int nextNodesCount;
	protected N[] reuse;
	private int nextNode = -1;
	private N[] prevNodes;
	private int prevNode;
	
	AbstractHashNodeIterator(
			Function<? super N, ? extends NCmp> nodeEqualityProj, 
			HashNodeEquality<? super NCmp, ? super N> nodeEquality,
			Function<? super N, ? extends V> ret) {
		this.nodeEqualityProj = nodeEqualityProj;
		this.nodeEquality = nodeEquality;
		this.ret = ret;
	}
	
	protected abstract void nextHash(N[] prevs, int prev);
	protected abstract boolean accept(N node);
	protected abstract boolean isDeleted(N[] prevs, int prev, N[] nodes, int node);
	protected abstract void delete(N[] nodes, int node);
	
	public boolean hasNext() {
		int node = this.nextNode;
		if (node == -1) {
			int prev = this.prevNode;
			N[] prevs = this.prevNodes;
			N[] nodes = this.nextNodes;
			if (prevs == nodes & (prevs != null)) {
				node = prev + 1;
			} else {
				node = 0;
			}
			while (nodes != null) {
				final int nodeCount = this.nextNodesCount;
				while (node < nodeCount 
						&& (isDeleted(prevs, prev, nodes, node)
							|| !accept(nodes[node])))
						node++;
				if (node < nodeCount)
					break;
				nextHash(prevs, prev);
				prevs = nodes;
				prev = node - 1;
				nodes = this.nextNodes;
				node = 0;
			}
			this.nextNode = node;
		}
		return nextNodes != null;
	}
	
	public V next() {
		if (nextNodes == null)
			throw new NoSuchElementException();
		if (prevNodes != nextNodes)
			reuse = prevNodes;
		prevNode = nextNode;
		prevNodes = nextNodes;
		nextNode = -1;
		return ret.apply(prevNodes[prevNode]);
	}
	
	public void remove() {
		if (prevNodes == null)
			throw new NoSuchElementException();
		delete(prevNodes, prevNode);
	}
	
}
