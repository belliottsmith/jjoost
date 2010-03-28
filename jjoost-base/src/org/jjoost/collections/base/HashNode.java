package org.jjoost.collections.base ;

import java.io.Serializable ;

public abstract class HashNode<N extends HashNode<N>> implements Serializable {
	
	private static final long serialVersionUID = 2035712133283347382L;
	public final int hash ;
	public HashNode(int hash) {
		this.hash = hash ;
	}
	public abstract N copy() ;
	public final int hash() { return hash ; }
	
	public static boolean insertBefore(int reverseHash, HashNode<?> node) {
		final int reverseNodeHash = Integer.reverse(node.hash) ; 
		return (reverseHash < reverseNodeHash) ^ ((reverseNodeHash > 0) != (reverseHash > 0));
	}

	public static boolean insertBefore(HashNode<?> a, HashNode<?> b) {
		final int ra = Integer.reverse(a.hash) ; 
		final int rb = Integer.reverse(b.hash) ; 
		return (ra < rb) ^ ((ra > 0) != (rb > 0));
	}
	
}

