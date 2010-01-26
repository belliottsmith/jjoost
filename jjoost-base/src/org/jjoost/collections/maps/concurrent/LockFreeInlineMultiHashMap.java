package org.jjoost.collections.maps.concurrent;

import java.util.Map.Entry ;

import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.LockFreeHashStore.Counting ;
import org.jjoost.collections.base.LockFreeHashStore.LockFreeHashNode ;
import org.jjoost.collections.maps.base.HashMapNodeFactory ;
import org.jjoost.collections.maps.base.InlineMultiHashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class LockFreeInlineMultiHashMap<K, V> extends InlineMultiHashMap<K, V, LockFreeInlineMultiHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeInlineMultiHashMap() {
		this(16, 0.75f) ;
	}	
	public LockFreeInlineMultiHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public LockFreeInlineMultiHashMap(Equality<? super K> keyEquality) {
		this(LockFreeHashStore.defaultRehasher(), keyEquality) ;
	}	
	public LockFreeInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public LockFreeInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}
	
	public LockFreeInlineMultiHashMap( 
			int minimumInitialCapacity, float loadFactor,
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new KeyEquality<K, V>(keyEquality), new NodeEquality<K, V>(keyEquality, valEquality),
			LockFreeInlineMultiHashMap.<K, V>factory(), 
			new LockFreeHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.PRECISE)) ;
	}

	protected static final class Node<K, V> extends LockFreeHashNode<Node<K, V>> implements Entry<K, V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public Node(int hash, K key, V value) {
			super(hash);
			this.key = key;
			this.value = value;
		}
		private final K key ;
		private V value ;		
		@Override public K getKey() { return key ; }
		@Override public V getValue() { return value ; }
		@Override public V setValue(V value) { throw new UnsupportedOperationException() ; }
		@Override public Node<K, V> copy() { return new Node<K, V>(hash, key, value) ; }
	}
	
	@SuppressWarnings("unchecked")
	private static final NodeFactory FACTORY = new NodeFactory() ;
	
	@SuppressWarnings("unchecked")
	public static <K, V> NodeFactory<K, V> factory() {
		return FACTORY ;
	}
	
	protected static final class NodeFactory<K, V> implements HashMapNodeFactory<K, V, Node<K, V>> {
		@Override
		public final Node<K, V> makeNode(final int hash, final K key, final V value) {
			return new Node<K, V>(hash, key, value) ;
		}
	}
	
	protected static final class KeyEquality<K, V> extends InlineMultiHashMap.KeyEquality<K, V, Node<K, V>> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean prefixMatch(K cmp, Node<K, V> n) {
			return keyEq.equates(cmp, n.key) ;
		}
	}

	protected static final class NodeEquality<K, V> extends InlineMultiHashMap.NodeEquality<K, V, Node<K, V>> {
		private static final long serialVersionUID = -8668943955126687051L ;

		public NodeEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean prefixMatch(Entry<K, V> cmp, Node<K, V> n) {
			return keyEq.equates(cmp.getKey(), n.key) ;
		}
		@Override
		public boolean suffixMatch(Entry<K, V> cmp, Node<K, V> n) {
			return valEq.equates(cmp.getValue(), n.value) ;
		}
	}
	
}
