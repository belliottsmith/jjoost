package org.jjoost.text.pattern;

public interface NodeMapBuilder<S> {

	NodeMapBuilder<S> bind(Group<S> s, Node<S> n, IdSet routes, IdCapture capture);
	NodeMap<S> done();
	
}
