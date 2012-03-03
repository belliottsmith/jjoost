package org.jjoost.text.pattern;

import java.io.Serializable;

public interface NodeMapEntry<S> extends Serializable {

	public Node<S> node();
	public IdCapture capture();
	public IdSet routes();

}
