package org.jjoost.text.pattern;

class BuildOption<S> extends BuildRegex<S> {

	private final BuildRegex<S> expr;
	public BuildOption(BuildRegex<S> expr) {
		super(expr.scheme);
		this.expr = expr;
	}

	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {		
		final Node<S> e = expr.toNodeGraph(tail, end);
		return e.mergeAlternatePath(tail.ref.capture(end), false).get();
	}
	
	public String toString() {
		return expr.toString() + "?";
	}
	
}
