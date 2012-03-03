package org.jjoost.text.pattern;

class BuildInfiniteRepeat<S> extends BuildRegex<S> {

	private final BuildRegex<S> expr;
	
	public BuildInfiniteRepeat(BuildRegex<S> expr) {
		super(expr.scheme);
		this.expr = expr;
	}

	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {
		Node<S> marker = scheme.marker();
		Node<S> expr = this.expr.toNodeGraph(new NodeRef<S>(marker), IdCapture.empty());
		expr.makeLoop(marker);
		if (!end.isEmpty() && !tail.ref.isEmpty()) {
			if (tail.ref.inLoop()) {
				tail.ref = tail.ref.unrollLoop();
			}
			tail.ref = tail.ref.capture(end);
		}
		return expr.mergeLoopWithTail(tail.ref);
	}
	
	public String toString() {
		return expr.toString() + "*";
	}
	
	BuildInfiniteExclusiveLimitingRepeat<S> limit(int extendCount) {
		return new BuildInfiniteExclusiveLimitingRepeat<S>(expr, extendCount);
	}
	
}
