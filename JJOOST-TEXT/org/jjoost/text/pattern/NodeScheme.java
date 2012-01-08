package org.jjoost.text.pattern;

public abstract class NodeScheme<S> {

	public abstract NodeMap<S> merge(@SuppressWarnings("unchecked") NodeMap<S> ... merge);
	public abstract NodeMapBuilder<S> build();
	public abstract NodeMap<S> emptyMap();
	
	public Node<S> terminal() {
		return new Node<S>(emptyMap(), IdSet.unitary(), IdSet.unitary(), false);
	}

	public Node<S> marker() {
		return new Node<S>(emptyMap());
	}
	
	public Node<S> empty() {
		return new Node<S>(emptyMap(), IdSet.empty(), IdSet.empty(), false);
	}
	
}
