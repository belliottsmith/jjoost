package org.jjoost.text.pattern;

class NodeSplit<S> {
	
	final Node<S> disjointLeft;
	final Node<S> intersectLeft;
	final Node<S> intersectRight;
	final Node<S> disjointRight;
	
	NodeSplit(Node<S> disjointLeft, Node<S> intersectLeft,
			Node<S> intersectRight, Node<S> disjointRight) {
		this.disjointLeft = disjointLeft;
		this.intersectLeft = intersectLeft;
		this.intersectRight = intersectRight;
		this.disjointRight = disjointRight;
	}

}
