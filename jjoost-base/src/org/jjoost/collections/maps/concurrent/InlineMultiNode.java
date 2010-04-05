package org.jjoost.collections.maps.concurrent;

import java.util.Map.Entry;

import org.jjoost.collections.base.AbstractConcurrentHashStore.ConcurrentHashNode;
import org.jjoost.collections.maps.base.HashMapNodeFactory;
import org.jjoost.collections.maps.base.InlineMultiHashMap;
import org.jjoost.util.Equality;

final class InlineMultiNode<K, V> extends ConcurrentHashNode<InlineMultiNode<K, V>> implements Entry<K, V> {
	
	private static final long serialVersionUID = -5766263745864028747L;
	
	public InlineMultiNode(int hash, K key, V value) {
		super(hash);
		this.key = key;
		this.value = value;
	}
	private final K key ;
	private V value ;		
	@Override public K getKey() { return key ; }
	@Override public V getValue() { return value ; }
	@Override public V setValue(V value) { throw new UnsupportedOperationException() ; }
	@Override public InlineMultiNode<K, V> copy() { return new InlineMultiNode<K, V>(hash, key, value) ; }
	@Override public String toString() { return "{" + key + " -> " + value + "}" ; }

	@SuppressWarnings("unchecked")
	private static final NodeFactory FACTORY = new NodeFactory() ;
	
	@SuppressWarnings("unchecked")
	public static <K, V> NodeFactory<K, V> factory() {
		return FACTORY ;
	}
	
	protected static final class NodeFactory<K, V> implements HashMapNodeFactory<K, V, InlineMultiNode<K, V>> {
		@Override
		public final InlineMultiNode<K, V> makeNode(final int hash, final K key, final V value) {
			return new InlineMultiNode<K, V>(hash, key, value) ;
		}
	}
	
	protected static final class KeyEquality<K, V> extends InlineMultiHashMap.KeyEquality<K, V, InlineMultiNode<K, V>> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean prefixMatch(K cmp, InlineMultiNode<K, V> n) {
			return keyEq.equates(cmp, n.key) ;
		}
	}

	protected static final class NodeEquality<K, V> extends InlineMultiHashMap.NodeEquality<K, V, InlineMultiNode<K, V>> {
		private static final long serialVersionUID = -8668943955126687051L ;
		public NodeEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean prefixMatch(Entry<K, V> cmp, InlineMultiNode<K, V> n) {
			return keyEq.equates(cmp.getKey(), n.key) ;
		}
		@Override
		public boolean suffixMatch(Entry<K, V> cmp, InlineMultiNode<K, V> n) {
			return valEq.equates(cmp.getValue(), n.value) ;
		}
	}

}

