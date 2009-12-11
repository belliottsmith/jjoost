package org.jjoost.collections.iters;

import org.jjoost.util.Function;

/**
 * Given an <code>Iterator</code> object and a function, yields an <code>Iterator</code> 
 * representing the result of applying that function to every element of the supplied <code>Iterator</code> 
 * 
 * @author b.elliottsmith
 *
 * @param <X>
 * @param <Y>
 */
public class MappedClosableIterator<X, Y> extends MappedIterator<X, Y> implements ClosableIterator<Y> {

    public MappedClosableIterator(ClosableIterator<? extends X> base, Function<? super X, ? extends Y> function) {
    	super(base, function) ;
    }

	public void close() {
		((ClosableIterator<? extends X>) base).close() ;
	}
	
}
