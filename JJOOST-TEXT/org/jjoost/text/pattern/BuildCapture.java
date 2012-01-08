package org.jjoost.text.pattern;

public class BuildCapture<S> extends BuildRegex<S> {

	final int label;
	final BuildRegex<S> capture;
	public BuildCapture(int label, BuildRegex<S> capture) {
		super(capture.scheme);
		this.capture = capture;
		this.label = label;
	}
	
	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {
		IdCapture nextEnd;
		if (!tail.ref.isEmpty()) {
			tail.ref = tail.ref.capture(end.end(label, false));
			nextEnd = IdCapture.empty();
		} else {
			nextEnd = end.end(label, true);
		}
		Node<S> expr = capture.toNodeGraph(tail.copy(), nextEnd);
		if (expr.inLoop()) {
			expr = expr.unrollLoop();
		}
		expr = expr.capture(IdCapture.empty().start(label));
		return expr;
	}
//	
//	@Override
//	public Node<S> toNodeGraph(Node<S> tail, IdCapture end, IdentityHashMap<Node<S>, Node<S>> tails) {
//		IdCapture nextEnd;
//		if (!tail.isEmpty()) {
//			tail = tail.capture(end.end(label, false));
//			if (tails != null) {
//				tails.put(tail, tail);
//			}
//			nextEnd = IdCapture.empty();
//		} else {
//			nextEnd = end.end(label, true);
//		}
//		Node<S> expr = capture.toNodeGraph(tail, nextEnd, tails);
//		if (expr.inLoop()) {
//			expr = expr.unrollLoop();
//		}
//		expr = expr.capture(IdCapture.empty().start(label));
//		return expr;
//	}
//	

}
