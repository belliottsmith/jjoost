package org.jjoost.text.pattern;

public class NodeRef<S> {

	public Node<S> ref;
	public NodeRef(Node<S> ref) {
		this.ref = ref;
	}
	public NodeRef<S> copy() {
		return new NodeRef<S>(ref);
	}
	
}
