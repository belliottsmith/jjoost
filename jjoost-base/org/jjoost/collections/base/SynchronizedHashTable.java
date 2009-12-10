package org.jjoost.collections.base;
//package org.jjoost.collections.generic;
//
//import java.util.Iterator;
//import java.util.List;
//
//import org.jjoost.util.Function;
//
//public class SynchronizedHashTable<N, NCmp, NPrefix> implements HashTable<N, NCmp, NPrefix> {
//
//	private static final long serialVersionUID = -5741992211360499734L;
//
//	private final HashTable<N, NCmp, NPrefix> table ;
//	
//	public SynchronizedHashTable(HashTable<N, NCmp, NPrefix> table) {
//		this.table = table ;
//	}
//
//	@Override
//	public synchronized int clear() {
//		return table.clear();
//	}
//
//	@Override
//	public synchronized boolean contains(int hash, NCmp c) {
//		return table.contains(hash, c);
//	}
//
//	@Override
//	public synchronized boolean containsPrefixMatch(int hash, NPrefix c) {
//		return table.containsPrefixMatch(hash, c);
//	}
//
//	@Override
//	public synchronized int count(int hash, NCmp c) {
//		return table.count(hash, c);
//	}
//
//	@Override
//	public synchronized int countPrefixMatches(int hash, NPrefix c) {
//		return table.countPrefixMatches(hash, c);
//	}
//
//	@Override
//	public synchronized N ensureAndGet(int hash, NCmp put, Function<? super NCmp, N> factory) {
//		return table.ensureAndGet(hash, put, factory);
//	}
//
//	@Override
//	public synchronized Iterator<N> find(int hash, NCmp c) {
//		return table.find(hash, c);
//	}
//
//	@Override
//	public synchronized Iterator<N> findPrefixMatches(int hash, NPrefix c) {
//		return table.findPrefixMatches(hash, c);
//	}
//
//	@Override
//	public synchronized N first(int hash, NCmp c) {
//		return table.first(hash, c);
//	}
//
//	@Override
//	public synchronized N firstPrefixMatch(int hash, NPrefix c) {
//		return table.firstPrefixMatch(hash, c);
//	}
//
//	@Override
//	public synchronized boolean isEmpty() {
//		return table.isEmpty();
//	}
//
//	@Override
//	public synchronized Iterator<N> iterator() {
//		return table.iterator();
//	}
//
//	@Override
//	public synchronized <V> List<V> list(int hash, NCmp c, Function<? super N, V> f) {
//		return table.list(hash, c, f);
//	}
//
//	@Override
//	public synchronized <V> List<V> listPrefixMatches(int hash, NPrefix c, Function<? super N, V> f) {
//		return table.listPrefixMatches(hash, c, f);
//	}
//
//	@Override
//	public synchronized N put(N put) {
//		return table.put(put);
//	}
//
//	@Override
//	public synchronized N putIfAbsent(int hash, NCmp put, Function<? super NCmp, N> factory) {
//		return table.putIfAbsent(hash, put, factory);
//	}
//
//	@Override
//	public synchronized N putIfAbsent(N put) {
//		return table.putIfAbsent(put);
//	}
//
//	@Override
//	public synchronized int remove(int hash, NCmp c) {
//		return table.remove(hash, c);
//	}
//
//	@Override
//	public synchronized int removePrefixMatches(int hash, NPrefix c) {
//		return table.removePrefixMatches(hash, c);
//	}
//
//	@Override
//	public synchronized void resize(int size) {
//		table.resize(size);
//	}
//
//	public synchronized int totalNodeCount() {
//		return table.totalNodeCount();
//	}
//
//	public synchronized int uniquePrefixCount() {
//		return table.uniquePrefixCount();
//	}
//
//	@Override
//	public synchronized String toString() {
//		return table.toString();
//	}
//
//	public synchronized <V> Iterator<V> find(int hash, NCmp c, Function<? super N, V> f) {
//		return table.find(hash, c, f);
//	}
//
//	public synchronized <V> Iterator<V> findPrefixMatches(int hash, NPrefix c, Function<? super N, V> f) {
//		return table.findPrefixMatches(hash, c, f);
//	}
//
//	public synchronized <V> Iterator<V> iterator(Function<? super N, V> f) {
//		return table.iterator(f);
//	}
//
//	@Override
//	public synchronized HashTable<N, NCmp, NPrefix> copy() {
//		return new SynchronizedHashTable<N, NCmp, NPrefix>(table.copy()) ;
//	}
//	
//	public static <N> HashTableFactory<N> factory(final HashTableFactory<N> wrapped) {
//		return new HashTableFactory<N>() {
//			@Override
//			public <NCmp, NPrefix> HashTable<N, NCmp, NPrefix> make(int minimumInitialCapacity, float loadFactor, HashTableEquality<? super N, ? super NCmp, ? super NPrefix> equality) {
//				return new SynchronizedHashTable<N, NCmp, NPrefix>(wrapped.<NCmp, NPrefix>make(minimumInitialCapacity, loadFactor, equality)) ;
//			}
//		} ;
//	}
//
//	public synchronized void putDups(N put) {
//		table.putDups(put);
//	}
//
//	public synchronized <V> Iterator<V> iterateFirstNodeOfEachPrefix(Function<? super N, V> f) {
//		return table.iterateFirstNodeOfEachPrefix(f);
//	}
//
//	public synchronized <V> Iterator<V> iterateFirstOfEachEqualNodes(Function<? super N, V> f) {
//		return table.iterateFirstOfEachEqualNodes(f);
//	}
//
//	public synchronized void shrink() {
//		table.shrink();
//	}
//
//	public synchronized <V> Iterable<V> removeAndReturn(int hash, NCmp c, Function<? super N, V> f) {
//		return table.removeAndReturn(hash, c, f);
//	}
//
//	public synchronized <V> Iterable<V> removeAndReturnPrefixMatches(int hash, NPrefix c,
//			Function<? super N, V> f) {
//		return table.removeAndReturnPrefixMatches(hash, c, f);
//	}
//
//	public synchronized N removePrefixMatchesAndReturnFirst(int hash, NPrefix c) {
//		return table.removePrefixMatchesAndReturnFirst(hash, c);
//	}
//
//}
