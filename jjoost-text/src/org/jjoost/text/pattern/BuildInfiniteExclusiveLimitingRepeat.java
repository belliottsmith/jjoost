package org.jjoost.text.pattern;

class BuildInfiniteExclusiveLimitingRepeat<S> extends BuildRegex<S> {

	private final BuildRegex<S> expr;
	private final int extendCount;
	
	public BuildInfiniteExclusiveLimitingRepeat(BuildRegex<S> expr, int extendCount) {
		super(expr.scheme);
		this.expr = expr;
		this.extendCount = extendCount;
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
		if (extendCount <= 0) { 
			return expr.mergeExclusiveLimitingLoopWithTail(tail.ref);
		} else {
			return expr.mergeExclusiveLimitingLoopWithTail(tail.ref, extendCount);
		}
	}
	
}
