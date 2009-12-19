package org.jjoost.collections.sets.concurrent;

import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.LockFreeHashStore.Counting ;
import org.jjoost.collections.base.LockFreeHashStore.LockFreeHashNode ;
import org.jjoost.collections.lists.UniformList ;
import org.jjoost.collections.sets.base.NestedMultiHashSet ;
import org.jjoost.util.Counters ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class LockFreeCountingMultiHashSet<V> extends NestedMultiHashSet<V, LockFreeCountingMultiHashSet.Node<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeCountingMultiHashSet() {
		this(16, 0.75f) ;
	}
	public LockFreeCountingMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public LockFreeCountingMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(Counters.newThreadSafeCounter(), keyHasher, rehasher, 
			new NestedMultiHashSet.ValueEquality<V, LockFreeCountingMultiHashSet.Node<V>>(keyEquality), 
			LockFreeCountingMultiHashSet.<V>factory(), 
			new LockFreeHashStore<Node<V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.OFF)) ;
	}

	// this implementation has no concurrency guarantees
	public static final class Node<V> extends LockFreeHashNode<Node<V>> implements INode<V, Node<V>> {
		
		private static final long serialVersionUID = -5766263745864028747L;
		
		public Node(int hash, V value, int count) {
			super(hash);
			this.value = value;
			this.count = count ;
		}
		
		private final V value ;
		private int count ; 
		
		@Override 
		public V getValue() { 
			return value ; 
		}
		
		@Override 
		public synchronized Node<V> copy() { 
			return new Node<V>(hash, value, count) ; 
		}
		
		@Override 
		public synchronized int count() {
			return count ;
		}
		
		@Override 
		public synchronized int remove(int i) {
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
		public synchronized List<V> removeAndReturn(int target) {
			return new UniformList<V>(value, remove(target)) ;
		}

		@Override 
		public synchronized boolean put(V val) {
			if (count < 1)
				return false ;
			count += 1 ; 
			return true ;
		}
		
		@Override 
		public boolean put(V val, int c) {
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
	private static final NodeFactory FACTORY = new NodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <V> NodeFactory<V> factory() {
		return FACTORY ;
	}
	public static final class NodeFactory<V> implements HashNodeFactory<V, Node<V>> {
		@Override
		public final Node<V> makeNode(final int hash, final V value) {
			return new Node<V>(hash, value, 0) ;
		}
	}

}
