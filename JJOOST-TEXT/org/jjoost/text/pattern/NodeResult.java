package org.jjoost.text.pattern;

public class NodeResult<S> {

	final Node<S> node;
	final IdCapture end;
	
	public NodeResult(Node<S> node) {
		this(node, IdCapture.empty());
	}
	public NodeResult(Node<S> node, IdCapture end) {
		super();
		this.node = node;
		this.end = end;
	}
	
}
