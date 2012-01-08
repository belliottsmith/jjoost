package org.jjoost.text.pattern;

public final class NodeMapSplit<S> {

	public static final class NodeIntersect<S> {
		public final Group<S> symbols;
		public final Node<S> left;
		public final Node<S> right;
		public final IdCapture leftCapture;
		public final IdCapture rightCapture;
		public NodeIntersect(Group<S> symbols, Node<S> left, IdCapture leftCapture, Node<S> right, IdCapture rightCapture) {
			this.symbols = symbols;
			this.left = left;
			this.right = right;
			this.leftCapture = leftCapture;
			this.rightCapture = rightCapture;
		}
	}
	
	public final NodeMap<S> left;
	public final NodeMap<S> right;
	public final NodeIntersect<S>[] intersect;
	
	public NodeMapSplit(NodeMap<S> left, NodeMap<S> right,
			NodeIntersect<S>[] intersect) {
		this.left = left;
		this.right = right;
		this.intersect = intersect;
	}
	
}
