package org.jjoost.text.pattern;

class BuildSelect<S> extends BuildRegex<S> {

	final BuildRegex<S>[] exprs ;
	public BuildSelect(@SuppressWarnings("unchecked") BuildRegex<S> ... exprs) {
		super(exprs[0].scheme);
		this.exprs = exprs;
	}
	
	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {
		Node<S> r = exprs[0].toNodeGraph(tail.copy(), end);
		for (int i = 1 ; i < exprs.length ; i++) {
			r = r.mergeAlternatePath(exprs[i].toNodeGraph(tail.copy(), end), true).get();
		}
		return r;
	}

}
