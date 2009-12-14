package org.jjoost.collections;

import org.jjoost.collections.base.HashStoreType ;
import org.jjoost.collections.maps.nested.ThreadSafeNestedSetListMap ;
import org.jjoost.collections.maps.nested.ThreadSafeNestedSetMultiMap ;
import org.jjoost.collections.maps.serial.SerialInlineListHashMap ;
import org.jjoost.collections.maps.serial.SerialInlineMultiHashMap ;
import org.jjoost.collections.maps.serial.SerialScalarHashMap ;
import org.jjoost.collections.maps.wrappers.SynchronizedListMap ;
import org.jjoost.collections.maps.wrappers.SynchronizedMultiMap ;
import org.jjoost.collections.maps.wrappers.SynchronizedScalarMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;

public class MapMaker {

	public static <K, V> HashMapMaker<K, V> hash() {
		return new HashMapMaker<K, V>() ;
	}
	
	public static abstract class AbstractMapMaker<K, V> {
		
		public abstract ScalarMap<K, V> newScalarMap() ;
		public abstract ListMap<K, V> newListMap(ListMapNesting<V> nesting) ;
		public abstract MultiMap<K, V> newMultiMap(MultiMapNesting<V> nesting) ;
		protected abstract AbstractMapMaker<K, V> copy() ;
		
		public ListMap<K, V> newListMap() {
			return newListMap(ListMapNesting.<V>inline()) ;
		}
		public MultiMap<K, V> newMultiMap() {
			return newMultiMap(MultiMapNesting.<V>inline()) ;			
		}
		public Factory<ScalarMap<K, V>> newScalarMapFactory() {
			return new ScalarMapFactory<K, V>(this) ;
		}
		public Factory<ListMap<K, V>> newListMapFactory(ListMapNesting<V> nesting) {
			return new ListMapFactory<K, V>(this, nesting) ;
		}
		public Factory<MultiMap<K, V>> newMultiMapFactory(MultiMapNesting<V> nesting) {
			return new MultiMapFactory<K, V>(this, nesting) ;
		}
	}
	
	public static class HashMapMaker<K, V> extends AbstractMapMaker<K, V> {
		private Hasher<? super K> keyHasher = Hashers.object() ;
		private Rehasher rehasher = null ;
		private Equality<? super K> keyEquality = Equalities.object() ;
		private Equality<? super V> valEquality = Equalities.object() ;
		private HashStoreType type = HashStoreType.serial() ;
		private int initialCapacity = 16 ;
		private float loadFactor = 0.75f ;
		private Function<K, V> factoryFunction ;
		private Factory<V> factory ;
		public HashMapMaker() { }
		private HashMapMaker(Hasher<? super K> keyHasher, Rehasher rehasher,
				Equality<? super K> keyEquality,
				Equality<? super V> valEquality, HashStoreType type,
				int initialCapacity, float loadFactor,
				Function<K, V> factoryFunction,
				Factory<V> factory) {
			super();
			this.keyHasher = keyHasher;
			this.rehasher = rehasher;
			this.keyEquality = keyEquality;
			this.valEquality = valEquality;
			this.type = type;
			this.initialCapacity = initialCapacity;
			this.loadFactor = loadFactor;
			this.factoryFunction = factoryFunction;
			this.factory = factory;
		}
		public HashMapMaker<K, V> hasher(Hasher<? super K> hasher) { this.keyHasher = hasher ; return this ; }
		public HashMapMaker<K, V> rehasher(Rehasher rehasher) { this.rehasher = rehasher ; return this ; }
		public HashMapMaker<K, V> keyEq(Equality<? super K> eq) { this.keyEquality = eq ; return this ; }
		public HashMapMaker<K, V> valEq(Equality<? super V> eq) { this.valEquality = eq ; return this ; }
		public HashMapMaker<K, V> type(HashStoreType type) { this.type = type ; return this ; }
		public HashMapMaker<K, V> initialCapacity(int initialCapacity) { this.initialCapacity = initialCapacity ; return this ; }
		public HashMapMaker<K, V> loadFactor(float loadFactor) { this.loadFactor = loadFactor ; return this ; }
		public HashMapMaker<K, V> defaultsTo(Factory<V> factory) { this.factory = factory ; this.factoryFunction = null ; return this ; }
		public HashMapMaker<K, V> defaultsTo(Function<K, V> function) { this.factoryFunction = function ; this.factory = null ; return this ; }
		public ScalarMap<K, V> newScalarMap() {
			switch(type.type()) {
			case SERIAL:
			case SYNCHRONIZED:
				ScalarMap<K, V> r = new SerialScalarHashMap<K, V>(
						initialCapacity, 
						loadFactor, 
						keyHasher, 
						rehasher(), 
						keyEquality,
						valEquality) ;
				if (type.type() == HashStoreType.Type.SYNCHRONIZED)
					r = new SynchronizedScalarMap<K, V>(r) ;
				return r ;
			case LOCK_FREE:
//			case PARTITIONED_BLOCKING:
//			case PARTITIONED_NON_BLOCKING:
				throw new UnsupportedOperationException() ;
			}			
			throw new IllegalArgumentException() ;
		}
		public MultiMap<K, V> newMultiMap(MultiMapNesting<V> nesting) {
			if (factory != null || factoryFunction != null)
				throw new IllegalArgumentException("Default values cannot be used in MultiMap or ListMap") ;
			switch (nesting.type()) {
			case MultiMapNesting.Type.INLINE:
				switch(type.type()) {
				case SERIAL:
				case SYNCHRONIZED:
					MultiMap<K, V> r = new SerialInlineMultiHashMap<K, V>(
							initialCapacity, 
							loadFactor, 
							keyHasher, 
							rehasher(), 
							keyEquality, 
							valEquality) ;
					if (type.type() == HashStoreType.Type.SYNCHRONIZED)
						r = new SynchronizedMultiMap<K, V>(r) ;
					return r ;
				case LOCK_FREE:
//				case PARTITIONED_BLOCKING:
//				case PARTITIONED_NON_BLOCKING:
					throw new UnsupportedOperationException() ;
				}			
			case MultiMapNesting.Type.NESTED:
				switch(type.type()) {
				case SERIAL:
				case SYNCHRONIZED:
					MultiMap<K, V> r = new ThreadSafeNestedSetMultiMap<K, V>(
							MapMaker.<K, ScalarSet<V>>hash()
								.initialCapacity(initialCapacity)
								.loadFactor(loadFactor)
								.hasher(keyHasher)
								.keyEq(keyEquality)
								.rehasher(rehasher)
								.newScalarMap(), nesting.factory()) ;
					if (type.type() == HashStoreType.Type.SYNCHRONIZED)
						r = new SynchronizedMultiMap<K, V>(r) ;
					return r ;
				case LOCK_FREE:
//				case PARTITIONED_BLOCKING:
//				case PARTITIONED_NON_BLOCKING:
					throw new UnsupportedOperationException() ;
				}			
			}
			throw new IllegalArgumentException() ;
		}
		public ListMap<K, V> newListMap(ListMapNesting<V> nesting) {
			if (factory != null || factoryFunction != null)
				throw new IllegalArgumentException("Default values cannot be used in MultiMap or ListMap") ;
			switch (nesting.type()) {
			case ListMapNesting.Type.INLINE:
				switch(type.type()) {
				case SERIAL:
				case SYNCHRONIZED:
					ListMap<K, V> r = new SerialInlineListHashMap<K, V>(
							initialCapacity, 
							loadFactor, 
							keyHasher, 
							rehasher(), 
							keyEquality, 
							valEquality) ;
					if (type.type() == HashStoreType.Type.SYNCHRONIZED)
						r = new SynchronizedListMap<K, V>(r) ;
					return r ;
				case LOCK_FREE:
//				case PARTITIONED_BLOCKING:
//				case PARTITIONED_NON_BLOCKING:
					throw new UnsupportedOperationException() ;
				}			
			case ListMapNesting.Type.NESTED:
				switch(type.type()) {
				case SERIAL:
				case SYNCHRONIZED:
					ListMap<K, V> r = new ThreadSafeNestedSetListMap<K, V>(
							MapMaker.<K, MultiSet<V>>hash()
								.initialCapacity(initialCapacity)
								.loadFactor(loadFactor)
								.hasher(keyHasher)
								.keyEq(keyEquality)
								.rehasher(rehasher)
								.newScalarMap(), nesting.factory()) ;
					if (type.type() == HashStoreType.Type.SYNCHRONIZED)
						r = new SynchronizedListMap<K, V>(r) ;
					return r ;
				case LOCK_FREE:
//				case PARTITIONED_BLOCKING:
//				case PARTITIONED_NON_BLOCKING:
					throw new UnsupportedOperationException() ;
				}			
			}
			throw new IllegalArgumentException() ; 
		}
		protected Rehasher rehasher() {
			if (rehasher != null)
				return rehasher ;
			switch(type.type()) {
//			case PARTITIONED_BLOCKING:
//			case PARTITIONED_NON_BLOCKING:
//				return Rehashers.jdkConcurrentHashmapRehasher() ;
			case SERIAL:
			case SYNCHRONIZED:
			case LOCK_FREE:
			default:
				return Rehashers.jdkHashmapRehasher() ;
			}
		}
		public HashMapMaker<K, V> copy() {
			return new HashMapMaker<K, V>(keyHasher, rehasher, keyEquality,
					valEquality, type, initialCapacity, loadFactor,
					factoryFunction, factory) ;
		}		
	}

	private static final class ListMapFactory<K, V> implements Factory<ListMap<K, V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final AbstractMapMaker<K, V> maker ;
		private final ListMapNesting<V> type ;
		public ListMapFactory(AbstractMapMaker<K, V> maker, ListMapNesting<V> type) {
			this.maker = maker.copy() ;
			this.type = type ;
		}
		@Override
		public ListMap<K, V> create() {
			return maker.newListMap(type) ;
		}
	}
	
	private static final class ScalarMapFactory<K, V> implements Factory<ScalarMap<K, V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final AbstractMapMaker<K, V> maker ;
		public ScalarMapFactory(AbstractMapMaker<K, V> maker) {
			this.maker = maker.copy() ;
		}
		@Override
		public ScalarMap<K, V> create() {
			return maker.newScalarMap() ;
		}
	}
	
	private static final class MultiMapFactory<K, V> implements Factory<MultiMap<K, V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final AbstractMapMaker<K, V> maker ;
		private final MultiMapNesting<V> type ;
		public MultiMapFactory(AbstractMapMaker<K, V> maker, MultiMapNesting<V> type) {
			this.maker = maker.copy() ;
			this.type = type ;
		}
		@Override
		public MultiMap<K, V> create() {
			return maker.newMultiMap(type) ;
		}
	}

}
