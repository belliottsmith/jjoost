package org.jjoost.text.pattern;

class BuildSequence<S> extends BuildRegex<S> {

	private final BuildRegex<S>[] regexs ;
	
	public BuildSequence(BuildRegex<S> ... regexs) {
		super(regexs[0].scheme);
		this.regexs = regexs ;
	}

	@Override
	public Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end) {
		int i = regexs.length - 1;
		Node<S> result = tail.ref;
		while (i > -1) {
			result = regexs[i].toNodeGraph(new NodeRef<S>(result), end);
			end = IdCapture.empty();
			i--;
		}
		return result;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0 ; i != regexs.length ; i++) {
			sb.append(regexs[i]);
		}
		return sb.toString();
	}
	
}
