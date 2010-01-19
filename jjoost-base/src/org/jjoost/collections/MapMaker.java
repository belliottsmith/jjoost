package org.jjoost.collections;

import org.jjoost.collections.base.HashStoreType;
import org.jjoost.collections.maps.concurrent.LockFreeInlineListHashMap;
import org.jjoost.collections.maps.concurrent.LockFreeInlineMultiHashMap;
import org.jjoost.collections.maps.concurrent.LockFreeLinkedInlineListHashMap;
import org.jjoost.collections.maps.concurrent.LockFreeLinkedInlineMultiHashMap;
import org.jjoost.collections.maps.concurrent.LockFreeLinkedHashMap;
import org.jjoost.collections.maps.concurrent.LockFreeHashMap;
import org.jjoost.collections.maps.nested.NestedSetListMap;
import org.jjoost.collections.maps.nested.NestedSetMultiMap;
import org.jjoost.collections.maps.serial.SerialInlineListHashMap;
import org.jjoost.collections.maps.serial.SerialInlineMultiHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineListHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineMultiHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedHashMap;
import org.jjoost.collections.maps.serial.SerialHashMap;
import org.jjoost.collections.maps.wrappers.DefaultFactoryMap ;
import org.jjoost.collections.maps.wrappers.DefaultFunctionMap ;
import org.jjoost.collections.maps.wrappers.SynchronizedListMap;
import org.jjoost.collections.maps.wrappers.SynchronizedMultiMap;
import org.jjoost.collections.maps.wrappers.SynchronizedMap;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;

/**
 * This abstract class defines methods which can be used to construct a <code>Map<code>,
 * <code>MultiMap<code> or <code>ListMap<code>. Also defined are various concrete implementations
 * of <code>MapMaker</code>, such as a <code>HashMapMaker</code> which define specific options
 * available for that kind of map.
 * 
 * @author b.elliottsmith
 */
public abstract class MapMaker<K, V> {

	/**
	 * Construct and return a new <code>Map</code>
	 * 
	 * @return a new <code>Map</code>
	 */
	public abstract Map<K, V> newMap() ;
	
	/**
	 * Return a new <code>ListMap</code> with <code>INLINE</code> nesting
	 * 
	 * @return a new <code>ListMap</code>
	 */
	public ListMap<K, V> newListMap() {
		return newListMap(ListMapNesting.<V>inline()) ;
	}
	
	/**
	 * Construct and return a new <code>ListMap</code> with the provided nesting settings
	 * 
	 * @param nesting
	 *            nesting
	 * @return a new <code>ListMap</code> with the provided nesting settings
	 */
	public abstract ListMap<K, V> newListMap(ListMapNesting<V> nesting) ;
	
	/**
	 * Return a new <code>MultiMap</code> with <code>INLINE</code> nesting
	 * 
	 * @return a new <code>MultiMap</code> with <code>INLINE</code> nesting
	 */
	public MultiMap<K, V> newMultiMap() {
		return newMultiMap(MultiMapNesting.<V>inline()) ;			
	}
	
	/**
	 * Construct and return a new <code>MultiMap</code> with the provided nesting settings
	 * 
	 * @param nesting
	 *            nesting
	 * @return a new <code>MultiMap</code> with the provided nesting settings
	 */
	public abstract MultiMap<K, V> newMultiMap(MultiMapNesting<V> nesting) ;

	/**
	 * Return a new <code>MapMaker</code> with the same properties as this one
	 * 
	 * @return a copy of this <code>MapMaker</code>
	 */
	protected abstract MapMaker<K, V> copy() ;
	
	/**
	 * Return a new <code>Factory</code> whose create() method returns the result of <code>this.newMap()</code>. Changes to this
	 * <code>MapMaker</code> after the construction of this factory will not affect the result of its <code>create()</code> method.
	 * 
	 * @return a factory of type <code>Map</code>
	 */
	public Factory<Map<K, V>> newMapFactory() {
		return new ScalarMapFactory<K, V>(this) ;
	}
	/**
	 * Return a new <code>Factory</code> whose create() method returns the result of <code>this.newListMap(nesting)</code>. Changes to this
	 * <code>MapMaker</code> after the construction of this factory will not affect the result of its <code>create()</code> method.
	 * 
	 * @param nesting
	 *            nesting
	 * @return a factory of type <code>ListMap</code>
	 */
	public Factory<ListMap<K, V>> newListMapFactory(ListMapNesting<V> nesting) {
		return new ListMapFactory<K, V>(this, nesting) ;
	}
	/**
	 * Return a new <code>Factory</code> whose <code>create()</code> method returns the result of <code>this.newMultiMap(nesting)</code>.
	 * Changes to this <code>MapMaker</code> after the construction of this factory will not affect the result of its <code>create()</code>
	 * method.
	 * 
	 * @param nesting
	 *            nesting
	 * @return a factory of type <code>MultiMap</code>
	 */
	public Factory<MultiMap<K, V>> newMultiMapFactory(MultiMapNesting<V> nesting) {
		return new MultiMapFactory<K, V>(this, nesting) ;
	}
		
	/**
	 * Returns a <code>new HashMapMaker</code>
	 * @return <code>HashMapMaker</code>
	 */
	public static <K, V> HashMapMaker<K, V> hash() {
		return new HashMapMaker<K, V>() ;
	}
	
	/**
	 * This class provides a user friendly means of constructing
	 * a variety of hash maps. Almost all hash based map options
	 * are exposed by this class.
	 * 
	 * @author b.elliottsmith
	 */
	public static class HashMapMaker<K, V> extends MapMaker<K, V> {
		private Rehasher rehasher = null ;
		private Equality<? super K> keyEquality = Equalities.object() ;
		private Equality<? super V> valEquality = Equalities.object() ;
		private HashStoreType type = HashStoreType.serial() ;
		private int initialCapacity = 16 ;
		private float loadFactor = 0.75f ;
		private Function<K, V> factoryFunction ;
		private Factory<V> factory ;
		/**
		 * create a new HashMapMaker
		 */
		public HashMapMaker() { }
		private HashMapMaker(Rehasher rehasher,
				Equality<? super K> keyEquality,
				Equality<? super V> valEquality, HashStoreType type,
				int initialCapacity, float loadFactor,
				Function<K, V> factoryFunction,
				Factory<V> factory) {
			super();
			this.rehasher = rehasher;
			this.keyEquality = keyEquality;
			this.valEquality = valEquality;
			this.type = type;
			this.initialCapacity = initialCapacity;
			this.loadFactor = loadFactor;
			this.factoryFunction = factoryFunction;
			this.factory = factory;
		}
		/**
		 * Set the <code>Rehasher</code> used by maps constructed by this <code>MapMaker</code>. All hashes are passed through the rehasher
		 * before being used; it is the rehasher's job to prevent unfortunate inputs/hash functions causing the map to perform poorly.
		 * The default differs depending on the <code>HashStoreType</code>.
		 * 
		 * @param rehasher
		 *            rehasher
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> rehasher(Rehasher rehasher) { this.rehasher = rehasher ; return this ; }
		/**
		 * Set the key equality used by maps constructed by this <code>MapMaker</code>. The <code>Equality</code> defines both the hash and
		 * equality implementations to use instead of the default <code>Object.hashCode()</code> and <code>Object.equals()</code> methods.
		 * The default is <code>Equalities.object()</code> which delegates to these methods, however <code>Equalities.identity()</code>
		 * causes maps created by this <code>MapMaker</code> to behave like an <code>IdentityHashMap</code> (regarding key equality).
		 * 
		 * @param eq
		 *            the key <code>Equality</code>
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> keyEq(Equality<? super K> eq) { this.keyEquality = eq ; return this ; }
		/**
		 * Set the equality used for value matching. This is used less commonly than the key equality, however is still
		 * necessary. All methods that accept a value will use this <code>Equality</code> to determine equality of the value
		 * against those present in the map. This is particularly important for a <code>MultiMap</code> and <code>ListMap</code>
		 * with INLINE nesting, as it will be used to determine equality for the value portion of each entry, and
		 * calls to <code>values().unique()</code> are quite likely to construct a secondary hash map 
		 * using the hash defined by this <code>Equality</code>. If the nesting type is NESTED then this options is ignored, 
		 * as the nested set will have its own equality defined. 
		 * 
		 * @param eq
		 *            the value <code>Equality</code>
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> valEq(Equality<? super V> eq) { this.valEquality = eq ; return this ; }
		/**
		 * Set the type of hash store to back the map by; this will affect performance and concurrency characteristics, primarily,
		 * but should have no impact on the basic functionality.
		 * 
		 * @param type
		 *            the hash store type
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> type(HashStoreType type) { this.type = type ; return this ; }
		/**
		 * Specify the minimum initial capacity a map should have on construction
		 * 
		 * @param initialCapacity
		 *            the minimum initial capacity of the map constructed
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> initialCapacity(int initialCapacity) { this.initialCapacity = initialCapacity ; return this ; }
		/**
		 * Define the load factor all maps should be constructed with. This parameter is used to decide when to enlarge a hash structure, and will
		 * greatly affect both the size and speed of the map. The smaller this value (less than 1) it is, the more space the map will waste
		 * but the better it will cope with poor distribution of elements. A perfect hash would need a value of 1 to perform optimally, but
		 * since most hash functions are not perfect, a value below 1 is usually best. A value above 1 will begin to save space at the
		 * expense of extra overhead maintaining and querying the map. If the size of the map is expected to stay <b>relatively</b> static,
		 * with the occasional peaks and troughs, however, a high load factor may avoid expensive and unnecessary grow operations.
		 * 
		 * @param loadFactor
		 *            the load factory of the map constructed
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> loadFactor(float loadFactor) { this.loadFactor = loadFactor ; return this ; }
		/**
		 * This options is only available for the construction of a regular <code>Map</code>. If this property is set then any call to
		 * <code>get(k)</code> on the map where the key does not exist has the key inserted into the map with a value provided by the
		 * factory. The effect is that <code>ensureAndGet(k, factory)</code> is called instead of <code>get(k)</code>
		 * 
		 * @param factory
		 *            the factory to use to populate values against non-existent keys that are accessed via <code>get()</code>
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> defaultsTo(Factory<V> factory) { this.factory = factory ; this.factoryFunction = null ; return this ; }
		/**
		 * This options is only available for the construction of a regular <code>Map</code>. If this property is set
		 * then any call to <code>get(k)</code> on the map where the key does not exist has the key inserted into the
		 * map with a value provided by the function. The effect is that <code>ensureAndGet(k, function)</code> is called
		 * instead of <code>get(k)</code>
		 * 
		 * @param function
		 *            the function to use to populate values against non-existent keys that are accessed via <code>get()</code>
		 * @return <code>this</code>
		 */
		public HashMapMaker<K, V> defaultsTo(Function<K, V> function) { this.factoryFunction = function ; this.factory = null ; return this ; }
		public Map<K, V> newMap() {
			final Map<K, V> r ;
			switch(type.type()) {
			case SERIAL:
				r = new SerialHashMap<K, V>(
					initialCapacity, loadFactor,  
					rehasher(), keyEquality, valEquality) ;
				break ;
			case SYNCHRONIZED:
				r = new SynchronizedMap<K, V>(
					new SerialHashMap<K, V>(
						initialCapacity, loadFactor,  
						rehasher(), keyEquality, valEquality)) ;
				break ;
			case LINKED_SERIAL:
				r = new SerialLinkedHashMap<K, V>(
					initialCapacity, loadFactor, 
					rehasher(), keyEquality, valEquality) ;
				break ;
			case LINKED_SYNCHRONIZED:
				r = new SynchronizedMap<K, V>(
					new SerialLinkedHashMap<K, V>(
						initialCapacity, loadFactor,  
						rehasher(), keyEquality, valEquality)) ;
				break ;
			case LOCK_FREE:
				r = new LockFreeHashMap<K, V>(
					initialCapacity, loadFactor,  
					rehasher(), keyEquality, valEquality) ;
				break ;
			case LINKED_LOCK_FREE:
				r = new LockFreeLinkedHashMap<K, V>(
					initialCapacity, loadFactor,  
					rehasher(), keyEquality, valEquality) ;
				break ;
			default:
				throw new UnsupportedOperationException() ;
			}
			if (factory != null)
				return new DefaultFactoryMap<K, V>(r, factory) ;
			if (factoryFunction != null)
				return new DefaultFunctionMap<K, V>(r, factoryFunction) ;
			return r ;
		}
		public MultiMap<K, V> newMultiMap(MultiMapNesting<V> nesting) {
			if (factory != null || factoryFunction != null)
				throw new IllegalArgumentException("Default values cannot be used in MultiMap or ListMap") ;
			switch (nesting.type()) {
			case INLINE:
				switch(type.type()) {
				case SERIAL:
					return new SerialInlineMultiHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(), keyEquality, valEquality) ;
				case SYNCHRONIZED:
					return new SynchronizedMultiMap<K, V>(
						new SerialInlineMultiHashMap<K, V>(
							initialCapacity, loadFactor, 
							rehasher(), keyEquality, valEquality)) ;
				case LINKED_SERIAL:
					return new SerialLinkedInlineMultiHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(), keyEquality, valEquality) ;
				case LINKED_SYNCHRONIZED:
					return new SynchronizedMultiMap<K, V>(
						new SerialLinkedInlineMultiHashMap<K, V>(
							initialCapacity, loadFactor, 
							rehasher(), keyEquality, valEquality)) ;
				case LOCK_FREE:
					return new LockFreeInlineMultiHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(), keyEquality, valEquality) ;
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedInlineMultiHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(), keyEquality, valEquality) ;
				default:
					throw new UnsupportedOperationException() ;
				}			
			case NESTED:
				switch (type.type()) {
				case SERIAL: case LINKED_SERIAL: case LINKED_SYNCHRONIZED: case SYNCHRONIZED:
					// TODO : valueEquality used in this context is erroneous, as we do not 
					// in fact know what value equality is being used by the sets provided by the factory
					return new NestedSetMultiMap<K, V>(
							MapMaker.<K, Set<V>>hash()
								.initialCapacity(initialCapacity)
								.loadFactor(loadFactor)
								.keyEq(keyEquality)
								.rehasher(rehasher)
								.type(type)
								.newMap(), 
								valEquality, 
								nesting.factory()) ;
				default:
					// TODO : NestedSetMultiMap only really supports SERIAL and SYNCHRONIZED; need a new threadsafe version
					throw new UnsupportedOperationException("NestedSetMultiMap is not concurrency safe, and there is not yet an equivalent class providing this functionality. You could try an Inline multi map instead.") ;
				}
			default:
					throw new UnsupportedOperationException() ;
			}
		}
		public ListMap<K, V> newListMap(ListMapNesting<V> nesting) {
			if (factory != null || factoryFunction != null)
				throw new IllegalArgumentException("Default values cannot be used in MultiMap or ListMap") ;
			switch (nesting.type()) {
			case INLINE:
				switch(type.type()) {
				case SERIAL:
					return new SerialInlineListHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(),  keyEquality, valEquality) ;
				case SYNCHRONIZED:
					// TODO : SynchronizedHashStore class so that we need fewer wrapping classes
					return new SynchronizedListMap<K, V>(
						new SerialInlineListHashMap<K, V>(
							initialCapacity, loadFactor, 
							rehasher(),  keyEquality, valEquality)) ;
				case LINKED_SERIAL:
					return new SerialLinkedInlineListHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(),  keyEquality, valEquality) ;
				case LINKED_SYNCHRONIZED:
					return new SynchronizedListMap<K, V>(
						new SerialLinkedInlineListHashMap<K, V>(
							initialCapacity, loadFactor, 
							rehasher(),  keyEquality, valEquality)) ;
				case LOCK_FREE:
					return new LockFreeInlineListHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(),  keyEquality, valEquality) ;
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedInlineListHashMap<K, V>(
						initialCapacity, loadFactor, 
						rehasher(),  keyEquality, valEquality) ;
				default:
					throw new UnsupportedOperationException() ;
				}			
			case NESTED:
				switch (type.type()) {
				case SERIAL: case LINKED_SERIAL: case LINKED_SYNCHRONIZED: case SYNCHRONIZED:
					return new NestedSetListMap<K, V>(MapMaker.<K, MultiSet<V>>hash()
						.initialCapacity(initialCapacity)
						.loadFactor(loadFactor)
						.keyEq(keyEquality)
						.rehasher(rehasher)
						.type(type)
						.newMap(),
						valEquality,
						nesting.factory()) ;
				default:
					// TODO : NestedSetListMap only really supports SERIAL and SYNCHRONIZED; need a new threadsafe version
					throw new UnsupportedOperationException("NestedSetListMap is not concurrency safe, and there is not yet an equivalent class providing this functionality. You could try an Inline list map instead.") ;
				}
			default:
				throw new UnsupportedOperationException() ;
			}
		}
		/**
		 * @return the rehasher to use for the map we are constructing; if the rehasher field is not null, it should return the value of
		 *         this field, but otherwise should pick the default rehasher for the hash structure type being created
		 */
		protected Rehasher rehasher() {
			if (rehasher != null)
				return rehasher ;
			switch(type.type()) {
			case SERIAL:
			case SYNCHRONIZED:
			case LOCK_FREE:
			default:
				return Rehashers.jdkHashmapRehasher() ;
			}
		}
		public HashMapMaker<K, V> copy() {
			return new HashMapMaker<K, V>(rehasher, keyEquality,
					valEquality, type, initialCapacity, loadFactor,
					factoryFunction, factory) ;
		}		
	}

	private static final class ListMapFactory<K, V> implements Factory<ListMap<K, V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final MapMaker<K, V> maker ;
		private final ListMapNesting<V> type ;
		public ListMapFactory(MapMaker<K, V> maker, ListMapNesting<V> type) {
			this.maker = maker.copy() ;
			this.type = type ;
		}
		@Override
		public ListMap<K, V> create() {
			return maker.newListMap(type) ;
		}
	}
	
	private static final class ScalarMapFactory<K, V> implements Factory<Map<K, V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final MapMaker<K, V> maker ;
		public ScalarMapFactory(MapMaker<K, V> maker) {
			this.maker = maker.copy() ;
		}
		@Override
		public Map<K, V> create() {
			return maker.newMap() ;
		}
	}
	
	private static final class MultiMapFactory<K, V> implements Factory<MultiMap<K, V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final MapMaker<K, V> maker ;
		private final MultiMapNesting<V> type ;
		public MultiMapFactory(MapMaker<K, V> maker, MultiMapNesting<V> type) {
			this.maker = maker.copy() ;
			this.type = type ;
		}
		@Override
		public MultiMap<K, V> create() {
			return maker.newMultiMap(type) ;
		}
	}

}
