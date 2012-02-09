package org.jjoost.text.pattern;

public final class NodeMapSplit<S> {

	public static final class NodeIntersect<S> {
		public final Group<S>[] symbols;
		public final Node<S> left;
		public final Node<S> right;
		public final IdCapture leftCapture;
		public final IdCapture rightCapture;
		public final IdSet leftRoutes;
		public final IdSet rightRoutes;
		public NodeIntersect(Group<S> symbols, Node<S> left, IdSet leftRoutes, IdCapture leftCapture, Node<S> right, IdSet rightRoutes, IdCapture rightCapture) {
			this(new Group[] { symbols}, left, leftRoutes, leftCapture, right, rightRoutes, rightCapture);
		}
		public NodeIntersect(Group<S>[] symbols, Node<S> left, IdSet leftRoutes, IdCapture leftCapture, Node<S> right, IdSet rightRoutes, IdCapture rightCapture) {
			this.symbols = symbols;
			this.left = left;
			this.right = right;
			this.leftRoutes = leftRoutes;
			this.leftCapture = leftCapture;
			this.rightCapture = rightCapture;
			this.rightRoutes = rightRoutes;
		}
		
		public NodeIntersect<S> merge(NodeIntersect<S> that) {
			if (this.left == that.left && this.right == that.right && this.leftCapture.equals(that.leftCapture) && this.rightCapture.equals(that.rightCapture) && this.leftRoutes.equals(that.leftRoutes) && this.rightRoutes.equals(that.rightRoutes)) {
				final Group<S>[] r = new Group[this.symbols.length + that.symbols.length];
				System.arraycopy(this.symbols, 0, r, 0, this.symbols.length);
				System.arraycopy(that.symbols, 0, r, this.symbols.length, that.symbols.length);
				return new NodeIntersect<S>(r, this.left, this.leftRoutes, this.leftCapture, this.right, this.rightRoutes, this.rightCapture);
			}
			return null;
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
