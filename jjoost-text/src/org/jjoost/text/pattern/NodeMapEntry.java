package org.jjoost.text.pattern;

import java.io.Serializable;

public interface NodeMapEntry<S> extends Serializable {

	public Group<S> sym();
	public Node<S> next();
	public IdCapture capture();
	public IdSet routes();

}
