package org.jjoost.text.pattern;

import java.io.Serializable;

public interface MatchAction<I, E> extends Serializable {

	public E matched(I input, Captured captured);
	
}
