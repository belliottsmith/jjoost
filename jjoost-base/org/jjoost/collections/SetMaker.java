package org.jjoost.collections;

import org.jjoost.collections.base.HashStoreCounting ;
import org.jjoost.collections.base.HashStoreType ;
import org.jjoost.collections.sets.serial.SerialCountingListHashSet ;
import org.jjoost.collections.sets.serial.SerialInlineListHashSet ;
import org.jjoost.collections.sets.serial.SerialNestedListHashSet ;
import org.jjoost.collections.sets.serial.SerialScalarHashSet ;
import org.jjoost.collections.sets.wrappers.SynchronizedListSet ;
import org.jjoost.collections.sets.wrappers.SynchronizedScalarSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;

public class SetMaker {

	public static <V> HashSetMaker<V> hash() {
		return new HashSetMaker<V>() ;
	}
	
	public static abstract class AbstractSetMaker<V> {
		
		public abstract ScalarSet<V> newScalarSet() ;
		public abstract ListSet<V> newListSet(ListSetNesting<V> nesting) ;
		protected abstract AbstractSetMaker<V> copy() ;
		
		public ListSet<V> newListSet() {
			return newListSet(ListSetNesting.<V>inline()) ;
		}
		public Factory<ScalarSet<V>> newScalarSetFactory() {
			return new ScalarSetFactory<V>(this) ;
		}
		public Factory<ListSet<V>> newListSetFactory(ListSetNesting<V> nesting) {
			return new ListSetFactory<V>(this, nesting) ;
		}
	}
	
	public static class HashSetMaker<V> extends AbstractSetMaker<V> {
		private Hasher<? super V> hasher = Hashers.object() ;
		private Rehasher rehasher = null ;
		private Equality<? super V> eq = Equalities.object() ;
		private HashStoreType type = HashStoreType.serial() ;
		private int initialCapacity = 16 ;
		private float loadFactor = 0.75f ;
		private HashStoreCounting counting ;
		public HashSetMaker() { }
		private HashSetMaker(Hasher<? super V> hasher, Rehasher rehasher,
				Equality<? super V> eq, HashStoreType type,
				int initialCapacity, float loadFactor,
				HashStoreCounting counting) {
			super();
			this.hasher = hasher ;
			this.rehasher = rehasher;
			this.eq = eq ;
			this.type = type;
			this.initialCapacity = initialCapacity;
			this.loadFactor = loadFactor;
			this.counting = counting;
		}
		public HashSetMaker<V> hasher(Hasher<? super V> hasher) { this.hasher = hasher ; return this ; }
		public HashSetMaker<V> rehasher(Rehasher rehasher) { this.rehasher = rehasher ; return this ; }
		public HashSetMaker<V> equality(Equality<? super V> eq) { this.eq = eq ; return this ; }
		public HashSetMaker<V> type(HashStoreType type) { this.type = type ; return this ; }
		public HashSetMaker<V> initialCapacity(int initialCapacity) { this.initialCapacity = initialCapacity ; return this ; }
		public HashSetMaker<V> loadFactor(float loadFactor) { this.loadFactor = loadFactor ; return this ; }
		public HashSetMaker<V> counting(HashStoreCounting counting) { this.counting = counting ; return this ; }
		public ScalarSet<V> newScalarSet() {
			switch(type.type()) {
			case SERIAL:
			case SYNCHRONIZED:				
				ScalarSet<V> r = new SerialScalarHashSet<V>(
						initialCapacity, 
						loadFactor, 
						hasher, 
						rehasher(), 
						eq) ;
				if (type.type() == HashStoreType.Type.SYNCHRONIZED)
					r = new SynchronizedScalarSet<V>(r) ;
				return r ;
			case NON_BLOCKING:
//			case PARTITIONED_BLOCKING:
//			case PARTITIONED_NON_BLOCKING:
			}			
			throw new UnsupportedOperationException() ;
		}
		public ListSet<V> newListSet(ListSetNesting<V> nesting) {
			switch (nesting.type()) {
			case ListSetNesting.Type.INLINE:
				switch(type.type()) {
				case SERIAL:
				case SYNCHRONIZED:
					ListSet<V> r = new SerialInlineListHashSet<V>(
							initialCapacity, 
							loadFactor, 
							hasher, 
							rehasher(), 
							eq) ;
					if (type.type() == HashStoreType.Type.SYNCHRONIZED)
						r = new SynchronizedListSet<V>(r) ;
					return r ;
				case NON_BLOCKING:
//				case PARTITIONED_BLOCKING:
//				case PARTITIONED_NON_BLOCKING:
				}			
				throw new UnsupportedOperationException() ;
			case ListSetNesting.Type.NESTED:
				switch(type.type()) {
				case SERIAL:
				case SYNCHRONIZED:
					ListSet<V> r = new SerialNestedListHashSet<V>(
							initialCapacity, 
							loadFactor,
							hasher, 
							rehasher(), 
							eq) ;
					if (type.type() == HashStoreType.Type.SYNCHRONIZED)
						r = new SynchronizedListSet<V>(r) ;
					return r ;
				case NON_BLOCKING:
//				case PARTITIONED_BLOCKING:
//				case PARTITIONED_NON_BLOCKING:
					throw new UnsupportedOperationException() ;
				}			
			case ListSetNesting.Type.COUNTING:
				switch(type.type()) {
				case SERIAL:
				case SYNCHRONIZED:
					ListSet<V> r = new SerialCountingListHashSet<V>(
							initialCapacity, 
							loadFactor,
							hasher, 
							rehasher(), 
							eq) ;
					if (type.type() == HashStoreType.Type.SYNCHRONIZED)
						r = new SynchronizedListSet<V>(r) ;
					return r ;
				case NON_BLOCKING:
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
			case NON_BLOCKING:
			default:
				return Rehashers.jdkHashmapRehasher() ;
			}
		}
		public HashSetMaker<V> copy() {
			return new HashSetMaker<V>(hasher, rehasher, eq,
					type, initialCapacity, loadFactor, counting) ;
		}
	}

	private static final class ListSetFactory<V> implements Factory<ListSet<V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final AbstractSetMaker<V> maker ;
		private final ListSetNesting<V> type ;
		public ListSetFactory(AbstractSetMaker<V> maker, ListSetNesting<V> type) {
			this.maker = maker.copy() ;
			this.type = type ;
		}
		@Override
		public ListSet<V> create() {
			return maker.newListSet(type) ;
		}
	}
	
	private static final class ScalarSetFactory<V> implements Factory<ScalarSet<V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final AbstractSetMaker<V> maker ;
		public ScalarSetFactory(AbstractSetMaker<V> maker) {
			this.maker = maker.copy() ;
		}
		@Override
		public ScalarSet<V> create() {
			return maker.newScalarSet() ;
		}
	}

}
