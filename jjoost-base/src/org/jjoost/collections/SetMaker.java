/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.collections;

import org.jjoost.collections.base.HashStoreType;
import org.jjoost.collections.sets.concurrent.LockFreeCountingMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeInlineMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedCountingMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedInlineMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedNestedMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeLinkedHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeNestedMultiHashSet;
import org.jjoost.collections.sets.concurrent.LockFreeHashSet;
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
public abstract class SetMaker<V> {
	
	private SetMaker() {}

	/**
	 * Construct and return a new <code>Set</code>
	 * 
	 * @return a new <code>Set</code>
	 */
	public abstract Set<V> newSet();
	
	/**
	 * Construct and return a new <code>MultiSet</code> with <code>INLINE</code> nesting
	 * 
	 * @return a new <code>MultiSet</code> with <code>INLINE</code> nesting
	 */
	public MultiSet<V> newMultiSet() {
		return newMultiSet(MultiSetNesting.<V>inline());
	}
	
	/**
	 * Construct and return a new <code>MultiSet</code> with the provided nesting settings
	 * 
	 * @param nesting
	 *            nesting
	 * @return a new <code>MultiSet</code> with the provided nesting settings
	 */
	public abstract MultiSet<V> newMultiSet(MultiSetNesting<V> nesting);
	
	/**
	 * Return a new <code>SetMaker</code> with the same properties as this one
	 * 
	 * @return a copy of this <code>SetMaker</code>
	 */
	protected abstract SetMaker<V> copy();
	
	/**
	 * Return a new <code>Factory</code> whose <code>create()</code> method returns the result of <code>this.newSet()</code>. Changes to
	 * this <code>SetMaker</code> after the construction of this factory will not affect the result of calls to the factory's
	 * <code>create()</code> method.
	 * 
	 * @return a factory of type <code>Set</code>
	 */
	public Factory<Set<V>> newSetFactory() {
		return new ScalarSetFactory<V>(this);
	}
	
	/**
	 * Return a new <code>Factory</code> whose <code>create()</code> method returns the result of <code>this.newMultiSet(nesting)</code>.
	 * Changes to this <code>SetMaker</code> after the construction of this factory will not affect the result of calls to the factory's
	 * <code>create()</code> method.
	 * 
	 * @return a factory of type <code>MultiSet</code>
	 */
	public Factory<MultiSet<V>> newMultiSetFactory(MultiSetNesting<V> nesting) {
		return new MultiSetFactory<V>(this, nesting);
	}
	
	/**
	 * Returns a <code>new HashSetMaker</code> for building hash sets
	 * @return <code>HashSetMaker</code>
	 */
	public static <V> HashSetMaker<V> hash() {
		return new HashSetMaker<V>();
	}
	
	/**
	 * This class provides a user friendly means of constructing
	 * a variety of hash sets. Almost all hash based set options
	 * are exposed by this class.
	 * 
	 * @author b.elliottsmith
	 */
	public static class HashSetMaker<V> extends SetMaker<V> {
		private Rehasher rehasher = null;
		private Equality<? super V> eq = Equalities.object();
		private HashStoreType type = HashStoreType.serial();
		private int initialCapacity = 16;
		private float loadFactor = 0.75f;
		/**
		 * create a new HashSetMaker
		 */
		public HashSetMaker() { }
		private HashSetMaker(Rehasher rehasher,
				Equality<? super V> eq, HashStoreType type,
				int initialCapacity, float loadFactor) {
			super();
			this.rehasher = rehasher;
			this.eq = eq;
			this.type = type;
			this.initialCapacity = initialCapacity;
			this.loadFactor = loadFactor;
		}
		/**
		 * Set the <code>Rehasher</code> used by sets constructed by this <code>SetMaker</code>. All hashes are passed through the rehasher
		 * before being used; it is the rehasher's job to prevent unfortunate inputs/hash functions causing the set to perform poorly.
		 * The default differs depending on the <code>HashStoreType</code>.
		 * 
		 * @param rehasher
		 *            the Rehasher
		 * @return <code>this</code>
		 */
		public HashSetMaker<V> rehasher(Rehasher rehasher) { this.rehasher = rehasher ; return this ; }
		/**
		 * Set the definition of equality used by sets constructed by this <code>SetMaker</code>. The <code>Equality</code> defines both the
		 * hash and equality implementations to use instead of the default <code>Object.hashCode()</code> and <code>Object.equals()</code>
		 * methods. The default is <code>Equalities.object()</code> which delegates to these methods, however
		 * <code>Equalities.identity()</code> causes sets created by this <code>SetMaker</code> to behave like an
		 * <code>IdentityHashMap</code> (regarding key equality).
		 * 
		 * @param eq
		 *            the key <code>Equality</code>
		 * @return <code>this</code>
		 */
		public HashSetMaker<V> equality(Equality<? super V> eq) { this.eq = eq ; return this ; }
		/**
		 * Set the type of hash structure to back the set by; this will affect performance and concurrency characteristics, primarily,
		 * but should have no impact on the basic functionality.
		 * 
		 * @param type
		 *            the hash store type
		 * @return <code>this</code>
		 */
		public HashSetMaker<V> type(HashStoreType type) { this.type = type ; return this ; }
		/**
		 * Specify the minimum initial capacity a set should have on construction
		 * 
		 * @param initialCapacity
		 *            the minimum initial capacity of the set constructed
		 * @return <code>this</code>
		 */
		public HashSetMaker<V> initialCapacity(int initialCapacity) { this.initialCapacity = initialCapacity ; return this ; }
		/**
		 * Define the load factor all sets should be constructed with. This parameter is used to decide when to enlarge a hash structure, and will
		 * affect both the size and speed of the map. The smaller this value (less than 1) it is, the more space the map will waste
		 * but the better it will cope with poor distribution of elements. A perfect hash would need a value of 1 to perform optimally, but
		 * since most hash functions are not perfect, a value below 1 is usually best. A value above 1 will begin to save space at the
		 * expense of extra overhead maintaining and querying the map. If the size of the map is expected to stay <b>relatively</b> static,
		 * with the occasional peaks and troughs, however, a high load factor may avoid expensive and unnecessary grow operations.
		 * 
		 * @param loadFactor
		 *            the load factory of the map constructed
		 * @return <code>this</code>
		 */
		public HashSetMaker<V> loadFactor(float loadFactor) { this.loadFactor = loadFactor ; return this ; }
		public Set<V> newSet() {
			switch(type.type()) {
			case SERIAL:
				return new SerialHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq);
			case SYNCHRONIZED:
				return new SynchronizedSet<V>(
					new SerialHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq));
			case LINKED_SERIAL:
				return new SerialLinkedHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq);
			case LINKED_SYNCHRONIZED:
				return new SynchronizedSet<V>(
					new SerialLinkedHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq));
			case LOCK_FREE:
				return new LockFreeHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq);
			case LINKED_LOCK_FREE:
				return new LockFreeLinkedHashSet<V>(
					initialCapacity, loadFactor, 
					rehasher(), eq);
			default:
				throw new IllegalArgumentException(type.toString());
			}			
		}
		public MultiSet<V> newMultiSet(MultiSetNesting<V> nesting) {
			switch (nesting.type()) {
			case INLINE:
				switch(type.type()) {
				case SERIAL:
					return new SerialInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialInlineMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq));
				case LINKED_SERIAL:
					return new SerialLinkedInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case LINKED_SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialLinkedInlineMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq));
				case LOCK_FREE:
					return new LockFreeInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedInlineMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				default:
					throw new IllegalArgumentException(type.toString());
				}			
			case NESTED:
				switch(type.type()) {
				case SERIAL:
					return new SerialNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialNestedMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq));
				case LINKED_SERIAL:
					return new SerialLinkedNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case LINKED_SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialLinkedNestedMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq));
				case LOCK_FREE:
					return new LockFreeNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedNestedMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				default:
					throw new IllegalArgumentException(type.toString());
				}			
			case COUNTING:
				switch(type.type()) {
				case SERIAL:
					return new SerialCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialCountingMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq));
				case LINKED_SERIAL:
					return new SerialLinkedCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case LINKED_SYNCHRONIZED:
					return new SynchronizedMultiSet<V>(
						new SerialLinkedCountingMultiHashSet<V>(
							initialCapacity, loadFactor, 
							rehasher(), eq));
				case LOCK_FREE:
					return new LockFreeCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				case LINKED_LOCK_FREE:
					return new LockFreeLinkedCountingMultiHashSet<V>(
						initialCapacity, loadFactor, 
						rehasher(), eq);
				default:
					throw new IllegalArgumentException(type.toString());
				}			
			}
			throw new IllegalArgumentException();
		}
		/**
		 * @return the rehasher to use for the set we are constructing; if the rehasher field is not null, it should return the value of
		 *         this field, but otherwise should pick the default rehasher for the hash structure type being created
		 */
		protected Rehasher rehasher() {
			if (rehasher != null)
				return rehasher;
			switch(type.type()) {
			case SERIAL:
			case SYNCHRONIZED:
			case LOCK_FREE:
			default:
				return Rehashers.jdkHashmapRehasher();
			}
		}
		public HashSetMaker<V> copy() {
			return new HashSetMaker<V>(rehasher, eq,
					type, initialCapacity, loadFactor);
		}
	}

	private static final class MultiSetFactory<V> implements Factory<MultiSet<V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final SetMaker<V> maker;
		private final MultiSetNesting<V> type;
		public MultiSetFactory(SetMaker<V> maker, MultiSetNesting<V> type) {
			this.maker = maker.copy();
			this.type = type;
		}
		@Override
		public MultiSet<V> create() {
			return maker.newMultiSet(type);
		}
	}
	
	private static final class ScalarSetFactory<V> implements Factory<Set<V>> {
		private static final long serialVersionUID = 475702452749567764L;
		private final SetMaker<V> maker;
		public ScalarSetFactory(SetMaker<V> maker) {
			this.maker = maker.copy();
		}
		@Override
		public Set<V> create() {
			return maker.newSet();
		}
	}

}
