package org.jjoost.collections;

import org.jjoost.collections.base.HashStoreType;
import org.jjoost.collections.sets.concurrent.LockFreeCountingMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeInlineMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedCountingMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedInlineMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedNestedMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedScalarHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeNestedMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeScalarHashSet;
import org.jjoost.collections.sets.serial.SerialCountingMultiHashSet;
import org.jjoost.collections.sets.serial.SerialInlineMultiHashSet;
import org.jjoost.collections.sets.serial.SerialLinkedCountingMultiHashSet;
import org.jjoost.collections.sets.serial.SerialLinkedInlineMultiHashSet;
import org.jjoost.collections.sets.serial.SerialLinkedNestedMultiHashSet;
import org.jjoost.collections.sets.serial.SerialLinkedHashSet;
import org.jjoost.collections.sets.serial.SerialNestedMultiHashSet;
import org.jjoost.collections.sets.serial.SerialHashSet;
import org.jjoost.collections.sets.wrappers.SynchronizedMultiSet;
import org.jjoost.collections.sets.wrappers.SynchronizedSet;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;

/**
 * 
 * @author b.elliottsmith
 *
 */
public final class SetMaker {
	
	private SetMaker() {}

	public static <V> HashSetMaker<V> hash() {
		return new HashSetMaker<V>() ;
	}
	
	public static abstract class AbstractSetMaker<V> {
		
		public abstract Set<V> newSet() ;
		public abstract MultiSet<V> newMultiSet(MultiSetNesting<V> nesting) ;
		protected abstract AbstractSetMaker<V> copy() ;
		
		public MultiSet<V> newMultiSet() {
			return newMultiSet(MultiSetNesting.<V>inline()) ;
		}
		public Factory<Set<V>> newSetFactory() {
			return new ScalarSetFactory<V>(this) ;
		}
		public Factory<MultiSet<V>> newMultiSetFactory(MultiSetNesting<V> nesting) {
			return new MultiSetFactory<V>(this, nesting) ;
		}
	}
	
	public static class HashSetMaker<V> extends AbstractSetMaker<V> {
		private Rehasher rehasher = null ;
		private Equality<? super V> eq = Equalities.object() ;
		private HashStoreType type = HashStoreType.serial() ;
		private int initialCapacity = 16 ;
		private float loadFactor = 0.75f ;
		public HashSetMaker() { }
		private HashSetMaker(Rehasher rehasher,
				Equality<? super V> eq, HashStoreType type,
				int initialCapacity, float loadFactor) {
			super();
			this.rehasher = rehasher;
			this.eq = eq ;
			this.type = type;
			this.initialCapacity = initialCapacity;
			this.loadFactor = loadFactor;
		}
		public HashSetMaker<V> rehasher(Rehasher rehasher) { this.rehasher = rehasher ; return this ; }
		public HashSetMaker<V> equality(Equality<? super V> eq) { this.eq = eq ; return this ; }
		public HashSetMaker<V> type(HashStoreType type) { this.type = type ; return this ; }
		public HashSetMaker<V> initialCapacity(int initialCapacity) { this.initialCapacity = initialCapacity ; return this ; }
		public HashSetMaker<V> loadFactor(float loadFactor) { this.loadFactor = loadFactor ; return this ; }
		public Set<V> newSet() {
			switch(type.type()) {
			case SERIAL:
				return new SerialHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq) ;
			case SYNCHRONIZED:
				return new SynchronizedSet<V>(
					new SerialHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq)) ;
			case LINKED_SERIAL:
				return new SerialLinkedHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq) ;
			case LINKED_SYNCHRONIZED:
				return new SynchronizedSet<V>(
					new SerialLinkedHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq)) ;
			case LOCK_FREE:
				return new LockFreeScalarHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq) ;
			case LINKED_LOCK_FREE:
				return new LockFreeLinkedScalarHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq) ;
			default:
				throw new IllegalArgumentException(type.toString()) ;
			}			
		}
		public MultiSet<V> newMultiSet(MultiSetNesting<V> nesting) {
			switch (nesting.type()) {
			case INLINE:
				switch(type.type()) {
				case SERIAL:
					return new SerialInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialInlineMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq)) ;
				case LINKED_SERIAL:
					return new SerialLinkedInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case LINKED_SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialLinkedInlineMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq)) ;
				case LOCK_FREE:
					return new LockFreeInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				default:
					throw new IllegalArgumentException(type.toString()) ;
				}			
			case NESTED:
				switch(type.type()) {
				case SERIAL:
					return new SerialNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialNestedMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq)) ;
				case LINKED_SERIAL:
					return new SerialLinkedNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case LINKED_SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialLinkedNestedMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq)) ;
				case LOCK_FREE:
					return new LockFreeNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				default:
					throw new IllegalArgumentException(type.toString()) ;
				}			
			case COUNTING:
				switch(type.type()) {
				case SERIAL:
					return new SerialCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialCountingMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq)) ;
				case LINKED_SERIAL:
					return new SerialLinkedCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case LINKED_SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialLinkedCountingMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq)) ;
				case LOCK_FREE:
					return new LockFreeCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq) ;
				default:
					throw new IllegalArgumentException(type.toString()) ;
				}			
			}
			throw new IllegalArgumentException() ;
		}
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
		public HashSetMaker<V> copy() {
			return new HashSetMaker<V>(rehasher, eq,
					type, initialCapacity, loadFactor) ;
		}
	}

	private static final class MultiSetFactory<V> implements Factory<MultiSet<V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final AbstractSetMaker<V> maker ;
		private final MultiSetNesting<V> type ;
		public MultiSetFactory(AbstractSetMaker<V> maker, MultiSetNesting<V> type) {
			this.maker = maker.copy() ;
			this.type = type ;
		}
		@Override
		public MultiSet<V> create() {
			return maker.newMultiSet(type) ;
		}
	}
	
	private static final class ScalarSetFactory<V> implements Factory<Set<V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final AbstractSetMaker<V> maker ;
		public ScalarSetFactory(AbstractSetMaker<V> maker) {
			this.maker = maker.copy() ;
		}
		@Override
		public Set<V> create() {
			return maker.newSet() ;
		}
	}

}
