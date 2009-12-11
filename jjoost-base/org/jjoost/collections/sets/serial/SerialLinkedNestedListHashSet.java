package org.jjoost.collections.sets.serial;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore.SerialLinkedHashNode ;
import org.jjoost.collections.sets.base.NestedListHashSet ;
import org.jjoost.util.Counters ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialLinkedNestedListHashSet<V> extends NestedListHashSet<V, SerialLinkedNestedListHashSet.Node<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialLinkedNestedListHashSet() {
		this(16, 0.75f) ;
	}
	public SerialLinkedNestedListHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialLinkedNestedListHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(Counters.newCounter(), keyHasher, rehasher, 
			new ValueEquality<V, Node<V>>(keyEquality), 
			SerialLinkedNestedListHashSet.<V>factory(), 
			new SerialLinkedHashStore<Node<V>>(minimumInitialCapacity, loadFactor)) ;
	}

	// this implementation makes absolutely no concurrency guarantees
	@SuppressWarnings("unchecked")
	public static final class Node<V> extends SerialLinkedHashNode<Node<V>> implements INode<V, Node<V>> {
		private static final long serialVersionUID = -5766263745864028747L;

		private V[] values = (V[]) new Object[4] ;
		private int count = 1 ;
		
		protected Node(int hash, V value) {
			super(hash);
			values[0] = value ;
		}
		protected Node(int hash, V[] values, int count) {
			super(hash);
			this.values = values ;
			this.count = count ;
		}
		@Override public Node<V> copy() { 
			return new Node<V>(hash, values.clone(), count) ; 
		}
		@Override
		public int count() {
			return count ;
		}
		@Override public int destroy() {
			final int c = count ;
			count = -1 ;
			return c ;
		}
		@Override
		public List<V> destroyAndReturn() {
			final int c = count ;
			count = -1 ;
			return Arrays.asList(values).subList(0, c) ;
		}
		@Override
		public V getValue() {
			return values[0] ;
		}
		@Override
		public Iterator<V> iterator(final Iterator<Iterator<V>> superIter) {
			return new Iterator<V>() {
				int last = -1 ;
				int next = 0 ;
				@Override
				public boolean hasNext() {
					return next < count ;
				}
				@Override
				public V next() {
					if (next >= count)
						throw new NoSuchElementException() ;
					last = next ;
					next += 1 ;
					return values[last] ;
				}
				@Override
				public void remove() {
					if (last == -1)
						throw new NoSuchElementException() ;
					if (count < 2) {
						count = -1 ;
						superIter.remove() ;
					} else {
						final int mi = count - 1 ;
						for (int i = last ; i < mi ; i++)
							values[i] = values[i+1] ;
						last = -1 ;
						count-- ;
					}
				}				
			};
		}
		@Override
		public Iterator<V> iterator(final NestedListHashSet<V, Node<V>> set) {
			return new Iterator<V>() {
				int last = -1 ;
				int next = 0 ;
				@Override
				public boolean hasNext() {
					return next < count ;
				}
				@Override
				public V next() {
					if (next >= count)
						throw new NoSuchElementException() ;
					last = next ;
					next += 1 ;
					return values[last] ;
				}
				@Override
				public void remove() {
					if (last == -1)
						throw new NoSuchElementException() ;
					if (count < 2) {
						count = -1 ;
						removeNode(set, Node.this) ;
					} else {
						final int mi = count - 1 ;
						for (int i = last ; i < mi ; i++)
							values[i] = values[i+1] ;
						last = -1 ;
						count-- ;
					}
				}				
			};
		}
		@Override
		public boolean put(V value) {
			if (count <= 0)
				return false ;
			if (count == values.length)
				values = Arrays.copyOf(values, values.length << 1) ;
			values[count++] = value ;
			return true ;
		}
		@Override 
		public boolean valid() { 
			return count > 0 ; 
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final NodeFactory NODE_FACTORY = new NodeFactory() ;
	@SuppressWarnings("unchecked")
	private static <V> NodeFactory<V> factory() {
		return NODE_FACTORY ;
	}
	public static final class NodeFactory<V> implements HashNodeFactory<V, Node<V>> {
		@Override
		public final Node<V> makeNode(final int hash, final V value) {
			return new Node<V>(hash, value) ;
		}
	}

}
