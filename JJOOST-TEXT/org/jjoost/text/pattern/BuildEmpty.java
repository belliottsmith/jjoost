package org.jjoost.text.pattern;

class BuildEmpty<S> extends BuildRegex<S> {

	public BuildEmpty() {
		super(null);
	}
	
	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {
		return tail.ref;
	}
	
}
