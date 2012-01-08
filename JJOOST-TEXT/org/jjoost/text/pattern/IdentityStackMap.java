package org.jjoost.text.pattern;

// this is not an arbitrary stack - it is specifically designed to follow a function call stack
// i.e., any items x1 and x2 where x1 was pushed before x2 should be popped in reverse order, i.e. with x2 popped before x1   
public final class IdentityStackMap<K, V> extends StackMap<K, V> {

	public final int hash(Object key) {
		return rehash(System.identityHashCode(key));
	}

	@Override
	protected boolean equals(Object a, Object b) {
		return a == b;
	}
    
}
