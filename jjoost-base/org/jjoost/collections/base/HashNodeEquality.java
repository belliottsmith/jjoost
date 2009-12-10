package org.jjoost.collections.base;

public interface HashNodeEquality<NCmp, N> {

	public boolean prefixMatch(NCmp cmp, N n) ;
	public boolean suffixMatch(NCmp cmp, N n) ;
	public boolean isUnique() ;
	
}
