package org.jjoost.text.pattern;

class BuildFiniteRepeat<S> extends BuildRegex<S> {

	// <= 0 indicates infinite
	private final int count;
	private final BuildRegex<S> expr;
	
	public BuildFiniteRepeat(int repeats, BuildRegex<S> expr) {
		super(expr.scheme);
		this.count = repeats;		
		this.expr = expr;
	}

	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {		
		Node<S> repeat = tail.ref;
		for (int i = 0 ; i < count ; i++) {
			repeat = this.expr.toNodeGraph(new NodeRef<S>(repeat), end);
		}
		return repeat;
	}
	
}
