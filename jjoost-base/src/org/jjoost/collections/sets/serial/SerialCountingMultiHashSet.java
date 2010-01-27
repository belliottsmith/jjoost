package org.jjoost.collections.sets.serial;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SerialHashStore.SerialHashNode;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.collections.sets.base.NestedMultiHashSet;
import org.jjoost.util.Counters;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialCountingMultiHashSet<V> extends NestedMultiHashSet<V, SerialCountingMultiHashSet.Node<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialCountingMultiHashSet() {
		this(16, 0.75f) ;
	}
	public SerialCountingMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialCountingMultiHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}
	
	public SerialCountingMultiHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality) ;
	}
	
	public SerialCountingMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(Counters.newCounter(), rehasher, 
			new NestedMultiHashSet.ValueEquality<V, SerialCountingMultiHashSet.Node<V>>(keyEquality), 
			SerialCountingMultiHashSet.<V>serialNodeFactory(), 
			new SerialHashStore<Node<V>>(minimumInitialCapacity, loadFactor)) ;
	}

	// this implementation has no concurrency guarantees
	public static final class Node<V> extends SerialHashNode<Node<V>> implements NestedMultiHashSet.INode<V, Node<V>> {
		
		private static final long serialVersionUID = -5766263745864028747L;
		
		public Node(int hash, V value, int count) {
			super(hash);
			this.value = value;
			this.count = count ;
		}
		
		private final V value ;
		private int count ; 
		
		@Override public V getValue() { 
			return value ; 
		}
		
		@Override public Node<V> copy() { 
			return new Node<V>(hash, value, count) ; 
		}
		
		@Override public int count() {
			return count ;
		}
		
		@Override public int remove(int i) {
			final int newc = count - i ;
			if (newc <= 0) {
				final int oldc = count ;
				count = 0 ;
				return oldc ;
			}
			count = newc ;
			return i ;
		}
		
		@Override
		public List<V> removeAndReturn(int target) {
			return new UniformList<V>(value, remove(target)) ;
		}

		@Override public boolean put(V val) {
			if (count < 1)
				return false ;
			count += 1 ; 
			return true ;
		}
		
		@Override public boolean put(V val, int c) {
			if (count < 1)
				return false ;
			count += c ; 
			return true ;
		}
		
		@Override 
		public boolean valid() { 
			return count > 0 ; 
		}
		
		@Override
		public Iterator<V> iterator(final Iterator<Iterator<V>> superIter) {
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
		
		@Override
		public Iterator<V> iterator(final NestedMultiHashSet<V, Node<V>> set) {
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
						removeNode(set, Node.this) ;
					}
				}
			} ;			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static final SerialCountingListHashSetNodeFactory SERIAL_SCALAR_HASH_NODE_FACTORY = new SerialCountingListHashSetNodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <V> SerialCountingListHashSetNodeFactory<V> serialNodeFactory() {
		return SERIAL_SCALAR_HASH_NODE_FACTORY ;
	}
	public static final class SerialCountingListHashSetNodeFactory<V> implements HashNodeFactory<V, Node<V>> {
		@Override
		public final Node<V> makeNode(final int hash, final V value) {
			return new Node<V>(hash, value, 0) ;
		}
	}

}