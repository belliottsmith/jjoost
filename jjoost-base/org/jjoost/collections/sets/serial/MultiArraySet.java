package org.jjoost.collections.sets.serial;

import java.util.ArrayList ;
import java.util.Iterator;
import java.util.List ;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.Set;
import org.jjoost.collections.sets.base.AbstractArraySet ;
import org.jjoost.collections.sets.base.AbstractUniqueSetAdapter;
import org.jjoost.util.Equality ;
import org.jjoost.util.Filters ;
import org.jjoost.util.Iters ;

public class MultiArraySet<V> extends AbstractArraySet<V> implements MultiSet<V> {

	private static final long serialVersionUID = 7815149592942049121L ;

	public MultiArraySet(int initialCapacity) {
		super(initialCapacity) ;
	}
	
	public MultiArraySet(int initialCapacity, Equality<? super V> valEq) {
		super(initialCapacity, valEq) ;
	}

	protected MultiArraySet(V[] vals, int count, Equality<? super V> valEq) {
		super(vals, count, valEq) ;
	}

	@Override
	public MultiSet<V> copy() {
		return new MultiArraySet<V>(vals.clone(), count, valEq) ;
	}

	@Override
	public boolean add(V v) {
		put(v) ;
		return true ;
	}
	
	@Override
	public V put(V v) {
		ensureIndex(count) ;
		vals[count++] = v ;
		return null ;
	}

	@Override
	public void put(V v, int c) {
		ensureIndex(count + c) ;
		for (int i = 0 ; i != c ; i++)
			vals[count+i] = v ;
		count += c ;
	}
	
	@Override
	public int putAll(Iterable<V> vs) {
		final int oldc = count ;
		for (V v : vs) {
			ensureIndex(count) ;
			vals[count++] = v ;
		}
		return count - oldc ;
	}

	@Override
	public Iterable<V> all(V v) {
		return list(v) ;
	}

	@Override
	public List<V> list(V v) {
		List<V> r = new ArrayList<V>() ;
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				r.add(v) ;
		return r ;
	}

	@Override
	public boolean permitsDuplicates() {
		return true ;
	}

	@Override
	public Set<V> unique() {
		return new UniqueSet() ;
	}

	@Override
	public int uniqueCount() {
		return Iters.count(unique()) ;
	}
	
	private final class UniqueSet extends AbstractUniqueSetAdapter<V> {
		private static final long serialVersionUID = -7222841189073398910L;
		@Override
		protected AnySet<V> set() {
			return MultiArraySet.this ;
		}
		@Override
		public Iterator<V> iterator() {
			return Filters.apply(Filters.unique(valEq), MultiArraySet.this.iterator()) ;
		}		
	}
	
}
