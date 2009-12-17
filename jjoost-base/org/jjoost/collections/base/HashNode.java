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
}

