package org.jjoost.text.pattern;

import java.io.Serializable;
import java.util.Map;

public interface NodeMap<S> extends Iterable<Node<S>>, Serializable {
	
	public NodeMap<S> replace(Map<Node<S>, Node<S>> replace, int offset);
	public Node<S> lookup(S s);
	public boolean maps(Node<S> s);
	public NodeMapSplit<S> split(NodeMap<S> that, int offset);
	public NodeScheme<S> scheme();
	public Iterable<NodeMapEntry<S>> entries();
	public boolean isEmpty();
	public void replaceInSitu(Node<S> replace, Node<S> with);
	public IdCapture capture(S s);
	public IdSet routes(S s);
	public NodeMap<S> addCapture(IdCapture capture);
	
}
