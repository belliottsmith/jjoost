package org.jjoost.text.pattern;

import java.io.Serializable;

import org.jjoost.util.Function;

public interface NodeMap<S> extends Iterable<Node<S>>, Serializable {
	
	public NodeMap<S> replace(Function<Node<S>, Node<S>> replace, int offset);
	public NodeMapEntry<S> lookup(S s);
	public NodeMapEntry<S> lookupSpecial(SpecialTransition s);
	public NodeMapSplit<S> split(NodeMap<S> that, int offset);
	public NodeScheme<S> scheme();
	public Iterable<NodeMapEntry<S>> entries();
	public boolean isEmpty();
	public void replaceInSitu(Node<S> replace, Node<S> with);
	public NodeMap<S> addCapture(IdCapture capture);	
	
}
