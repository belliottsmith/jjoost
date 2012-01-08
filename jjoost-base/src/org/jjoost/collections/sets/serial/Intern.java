package org.jjoost.collections.sets.serial;

import org.jjoost.util.Function;

public class Intern<V> implements Function<V, V> {

	private static final long serialVersionUID = -2042387823021980500L;
	
	final SerialLinkedHashSet<V> cache; 
	final int maxSize;
	public Intern(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize must be greater than zero");
		}
		this.maxSize = maxSize;
		this.cache = new SerialLinkedHashSet<V>();
	}
	
	public V apply(V in) {
		final V out = cache.putIfAbsent(in);
		if (out == null) {			
			if (cache.size() > maxSize) {
				cache.removeOldest();
			}
			return in;
		}
		return out;
	}

}
