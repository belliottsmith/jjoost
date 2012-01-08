package org.jjoost.text.pattern;

class BuildInfiniteExclusiveExtendingRepeat<S> extends BuildRegex<S> {

	// <= 0 indicates infinite
	private final BuildRegex<S> expr;
	
	public BuildInfiniteExclusiveExtendingRepeat(BuildRegex<S> expr) {
		super(expr.scheme);
		this.expr = expr;
	}

	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {
		final Node<S> marker = scheme.marker();
		final Node<S> expr = this.expr.toNodeGraph(new NodeRef<S>(marker), end);
		expr.makeLoop(marker);
		if (!end.isEmpty() && !tail.ref.isEmpty()) {
			if (tail.ref.inLoop()) {
				tail.ref = tail.ref.unrollLoop();
			}
			tail.ref = tail.ref.capture(end);
		}
		return expr.mergeExclusiveExtendingLoopWithTail(tail.ref);
	}
	
}
