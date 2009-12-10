//package org.jjoost.collections.maps.base;
//
//import java.util.Comparator ;
//import java.util.Map.Entry ;
//
//import org.jjoost.collections.ArbitraryMap ;
//import org.jjoost.collections.OrderedScalarMap ;
//import org.jjoost.collections.base.OrderedStore ;
//import org.jjoost.util.Factory ;
//import org.jjoost.util.Function ;
//
//public class ScalarOrderedMap<K, V, N extends Entry<K, V>, S extends OrderedStore<N, S>> extends AbstractOrderedMap<K, V, N, S, ScalarOrderedMap<K, V, N, S>> implements OrderedScalarMap<K, V> {
//
//	@Override
//	protected ScalarOrderedMap<K, V, N, S> create(S store, Comparator<? super K> keyComparator, Comparator<Entry<K, V>> entryComparator,
//			OrderedMapNodeFactory<K, V, N> nodeFactory) {
//		// TODO Auto-generated method stub
//		return null ;
//	}
//
//	@Override
//	public org.jjoost.collections.OrderedScalarMap.OrderedScalarMapEntrySet<K, V> entries() {
//		// TODO Auto-generated method stub
//		return null ;
//	}
//
//	@Override
//	public org.jjoost.collections.OrderedScalarMap.OrderedScalarMapKeySet<K, V> keys() {
//		// TODO Auto-generated method stub
//		return null ;
//	}
//
//	@Override
//	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
//		return null ;
//	}
//
//	@Override
//	public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) {
//		// TODO Auto-generated method stub
//		return null ;
//	}
//
//	@Override
//	public V get(K key) {
//		// TODO Auto-generated method stub
//		return null ;
//	}
//
//	@Override
//	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
//		// TODO Auto-generated method stub
//		return null ;
//	}
//
//	@Override
//	public int size() {
//		// TODO Auto-generated method stub
//		return 0 ;
//	}
//
//	@Override
//	public ArbitraryMap<V, K> inverse() {
//		// TODO Auto-generated method stub
//		return null ;
//	}
//
//	@Override
//	public V apply(K v) {
//		return get(v) ;
//	}
//
//}
