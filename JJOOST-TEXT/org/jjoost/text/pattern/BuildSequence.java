package org.jjoost.text.pattern;

class BuildSequence<S> extends BuildRegex<S> {

	private final BuildRegex<S>[] regexs ;
	
	public BuildSequence(@SuppressWarnings("unchecked") BuildRegex<S> ... regexs) {
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

//	@Override
//	int match(String input, int offset, Captured capture) {
//		for (int i = 0 ; offset < input.length() && i != regexs.length ; i++) {
//			final int match = regexs[i].match(input, offset, capture);
//			if (match < 0) {
//				return -1;		
//			}
//			offset += match;
//		}
//		return offset;
//	}

}
