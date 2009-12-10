package org.jjoost.util.tuples;

import java.io.Serializable;

import org.jjoost.util.Objects;

public class Pair<A, B> implements Serializable {

	private static final long serialVersionUID = 4885537229275773557L ;
	
	public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first ;
        this.second = second ;
    }
    
    public String toString() {
    	return "{" + first + ", " + second + "}" ;
    }
    
    public int hashCode() {
    	return (first == null ? 0 : first.hashCode()) + (second == null ? 0 : second.hashCode()) ;
    }
    
    @SuppressWarnings("unchecked")
	public boolean equals(Object that) {
    	return that instanceof Pair && equals((Pair<?, ?>) that) ;
    }
    
    public boolean equals(Pair<?, ?> that) {
    	return Objects.equalQuick(this.first, that.first) && Objects.equalQuick(this.second, that.second) ;
    }
    
    public static <A, B> Pair<A, B> get(A first, B second) {
    	return new Pair<A, B>(first, second) ;
    }

}
