package org.jjoost.util.tuples;

import java.io.Serializable;

import org.jjoost.util.Objects;

public class FourTuple<A, B, C, D> implements Serializable {

	private static final long serialVersionUID = 4149233645272599162L ;
	
	public final A first;
    public final B second;
	public final C third;
	public final D fourth ;
	
    public FourTuple(A first, B second, C third, D fourth) {
        this.first = first ;
        this.second = second ;
        this.third = third ;
        this.fourth = fourth ;
    }
    
    @SuppressWarnings("unchecked")
	public boolean equals(Object that) {
    	return that instanceof FourTuple && equals((FourTuple<?, ?, ?, ?>) that) ;
    }
    
    public boolean equals(FourTuple<?, ?, ?, ?> that) {
    	return Objects.equalQuick(this.first, that.first) && Objects.equalQuick(this.second, that.second) && Objects.equalQuick(this.third, that.third) && Objects.equalQuick(this.fourth, that.fourth) ;
    }
    
}
