package org.jjoost.collections.sets.serial;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore.SerialLinkedHashNode ;
import org.jjoost.collections.sets.base.CountingListHashSet ;
import org.jjoost.util.Counter ;
import org.jjoost.util.Counters ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialLinkedCountingListHashSet<V> extends CountingListHashSet<V, SerialLinkedCountingListHashSet.SerialLinkedCountingListHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialLinkedCountingListHashSet() {
		this(16, 0.75f) ;
	}
	public SerialLinkedCountingListHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialLinkedCountingListHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(Counters.newCounter(), keyHasher, rehasher, 
			SerialLinkedCountingListHashSet.<V>serialLinkedNodeFactory(), 
			new ValueEquality<V>(keyEquality), 
			new SerialLinkedHashStore<SerialLinkedCountingListHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

	// this implementation has no concurrency guarantees
	public static final class SerialLinkedCountingListHashSetNode<V> extends SerialLinkedHashNode<SerialLinkedCountingListHashSetNode<V>> implements INode<V, SerialLinkedCountingListHashSetNode<V>> {
		private static final long serialVersionUID = -5766263745864028747L;
		public SerialLinkedCountingListHashSetNode(int hash, V value, int count) {
			super(hash);
			this.value = value;
			this.count = count ;
		}
		private final V value ;
		private int count ; 
		@Override public V getValue() { 
			return value ; 
		}
		@Override public SerialLinkedCountingListHashSetNode<V> copy() { 
			return new SerialLinkedCountingListHashSetNode<V>(hash, value, count) ; 
		}
		@Override public int destroy() {
			int r = count ;
			count = -1 ;
			return r ; 
		}
		@Override public int get() {
			return count ;
		}
		@Override
		public boolean add(int i) {
			count += i ; 
			return true ;
		}
		@Override 
		public boolean valid() { 
			return count > 0 ; 
		}
		@Override
		public Counter newInstance() {
			throw new UnsupportedOperationException() ;
		}
		@Override
		public Iterator<V> iter(final Iterator<Iterator<V>> superIter) {
			return new Iterator<V>() {
				int c = 0 ;
				boolean last = false ;
				boolean next = false ;
				@Override
				public boolean hasNext() {
					return next = (count > c) ;
				}
				@Override
				public V next() {
					if (!next)
						throw new NoSuchElementException() ;
					c++ ;
					last = true ;
					return value ;
				}
				@Override
				public void remove() {
					if (!last)
						throw new NoSuchElementException() ;
					count -= 1 ;
					if (count <= 0) {
						count = -1 ;
						superIter.remove() ;
					}
				}
			} ;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final SerialLinkedCountingListHashSetNodeFactory SERIAL_SCALAR_HASH_NODE_FACTORY = new SerialLinkedCountingListHashSetNodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <V> SerialLinkedCountingListHashSetNodeFactory<V> serialLinkedNodeFactory() {
		return SERIAL_SCALAR_HASH_NODE_FACTORY ;
	}
	public static final class SerialLinkedCountingListHashSetNodeFactory<V> implements HashNodeFactory<V, SerialLinkedCountingListHashSetNode<V>> {
		@Override
		public final SerialLinkedCountingListHashSetNode<V> makeNode(final int hash, final V value) {
			return new SerialLinkedCountingListHashSetNode<V>(hash, value, 0) ;
		}
	}

	protected static final class ValueEquality<V> extends CountingListHashSet.ValueEquality<V, SerialLinkedCountingListHashSetNode<V>> {
		public ValueEquality(Equality<? super V> valEq) {
			super(valEq) ;
		}
		@Override
		public boolean prefixMatch(V cmp, SerialLinkedCountingListHashSetNode<V> n) {
			return valEq.equates(cmp, n.value) ;
		}
		@Override
		public boolean suffixMatch(V cmp, SerialLinkedCountingListHashSetNode<V> n) {
			return true ;
		}		
	}
	
}
