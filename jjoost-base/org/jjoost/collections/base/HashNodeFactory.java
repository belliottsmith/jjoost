package org.jjoost.collections.base;

public interface HashNodeFactory<NCmp, N> {

	public N makeNode(int hash, NCmp from) ;
	
}
