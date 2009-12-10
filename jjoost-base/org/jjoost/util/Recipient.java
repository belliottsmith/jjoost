package org.jjoost.util;

import java.io.Serializable;

public interface Recipient<V> extends Serializable {
    
	/** 
	 * @param v something of type domain
	 * @return the result of the function (something of type range)
	 * @throws InterruptedException 
	 */
    public void receive(V v) throws InterruptedException ;

    @SuppressWarnings("unchecked")
	public static final Recipient NOOP = 
		new Recipient() {
			private static final long serialVersionUID = 5801405085506068892L ;
			public void receive(Object v) { } 
		} ;
		
}
