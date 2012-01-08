package org.jjoost.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.Set;
import org.jjoost.collections.sets.serial.SerialLinkedHashSet;

public class Classes {

	@SuppressWarnings("unchecked")
	public static <V> Class<V> findCommonAncestor(Class<? extends V> ... clazzes) {
		final Set<Class<?>>[] ancestors = new Set[clazzes.length];
		for (int i = 0 ; i != ancestors.length ; i++) {
			ancestors[i] = ancestors(clazzes[i]);
		}
		for (int i = ancestors.length - 2; i >= 0 ; i--) {
			ancestors[i].retain(ancestors[i + 1]);
		}
		return (Class<V>) ancestors[0].iterator().next();
	}
	
	public static <V> Set<Class<?>> findCommonAncestors(Class<? extends V> ... clazzes) {
		@SuppressWarnings("unchecked")
		final Set<Class<?>>[] ancestors = new SerialLinkedHashSet[clazzes.length];
		for (int i = 0 ; i != ancestors.length ; i++) {
			ancestors[i] = ancestors(clazzes[i]);
		}
		for (int i = ancestors.length - 2; i >= 0 ; i--) {
			ancestors[i].retain(ancestors[i + 1]);
		}
		final Set<Class<?>> r = ancestors[0];
		for (int i = 1 ; i < r.size() ; i++) {

			final Iterator<Class<?>> iter = r.iterator();
			Class<?> head = iter.next();
			for (int j = 1 ; j != i ; j++) {
				head = iter.next();
			}
			while (iter.hasNext()) {
				if (iter.next().isAssignableFrom(head)) {
					iter.remove();
				}
			}
		}
		
		return r;
	}
	
	// TODO : not sure this closely emulates javac behaviour when multiple possible ancestor types. test!
	public static Set<Class<?>> ancestors(Class<?> clazz) {
		// perform breadth-first explore of ancestry, re-adding objects each time we visit to remove those that are re-declared closer to the root
		// (as we want to consider them last)
		final Set<Class<?>> out = new SerialLinkedHashSet<Class<?>>();
		List<Class<?>> next = new ArrayList<Class<?>>();
		List<Class<?>> prev = new ArrayList<Class<?>>();
		prev.add(clazz);
		while (!prev.isEmpty()) {
			for (Class<?> c : prev) {				
				out.add(c);
				final Class<?> sp = c.getSuperclass();
				if (sp != Object.class && sp != null) {
					next.add(sp);
				}
				for (Class<?> i : c.getInterfaces()) {
					next.add(i);
				}
			}
			List<Class<?>> t = prev;
			prev = next;
			next = t;
			next.clear();
		}		
		out.add(Object.class);
		return out;
	}
	
}
