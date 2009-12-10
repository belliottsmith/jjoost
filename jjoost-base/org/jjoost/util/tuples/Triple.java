package org.jjoost.util.tuples;

import java.io.Serializable;

import org.jjoost.util.Objects;

public class Triple<A, B, C> implements Serializable {

	private static final long serialVersionUID = -467576690690262462L ;
	
	public final A first;
    public final B second;
	public final C third;

    public Triple(A first, B second, C third) {
        this.first = first ;
        this.second = second ;
        this.third = third ;
    }

    @SuppressWarnings("unchecked")
	public boolean equals(Object that) {
    	return that instanceof Triple && equals((Triple<?, ?, ?>) that) ;
    }
    
    public boolean equals(Triple<?, ?, ?> that) {
    	return Objects.equalQuick(this.first, that.first) && Objects.equalQuick(this.second, that.second) && Objects.equalQuick(this.third, that.third) ;
    }
    
}
