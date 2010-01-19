package org.jjoost.collections.iters;

import java.util.Iterator;

public interface ClosableIterator<E> extends Iterator<E> {

	public void close() ;
	
}
