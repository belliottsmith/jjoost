package org.jjoost.collections.base;


import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;

import org.jjoost.util.concurrent.ThreadQueue ;

import sun.misc.Unsafe;

// TODO : improvements to the thread queue?
// TODO : experiment with segments again (reduces concurrency overhead for cas-ing count (currently marking >=20% CPU time in benchmark!), and possible reduces extra memory necessary during grows)
// TODO : consider optimisations to the rehash() method
// TODO : iterators + other standard methods
@SuppressWarnings("restriction")
public class NonBlockingHashStore<N extends NonBlockingHashStore.Node<N>> implements HashStore<N> {

	private static final Unsafe unsafe = getUnsafe();
	
	private final WaitingOnDelete<N> waitingOnDelete = new WaitingOnDelete<N>(null, null) ;
	private final float loadFactor ;
	private final Count count = new PreciseCount() ;
	private Table<N> tablePtr ;
	
	@SuppressWarnings("unchecked")
	public NonBlockingHashStore(int initialCapacity, int segments, float loadFactor) {
        int capacity = 1 ;
        while (capacity < initialCapacity)
        	capacity <<= 1 ;
        setTable(new RegularTable<N>((N[]) new Node[capacity], (int) (capacity * loadFactor))) ;
        this.loadFactor = loadFactor ;
	}

//	private NodePos<N> find(final int hash, final K key, boolean doGetTableSafely) {
//		N prev2, prev, node ;
//		prev2 = prev = null ;
//		node = (doGetTableSafely ? getTableSafe() : getTableUnsafe()).writerGetSafe(hash) ;
//		while (node != null) {
//			if (node == DELETED_FLAG | node == DELETING_FLAG) {
//				waitOnDelete(prev) ;
//				if (prev2 == null) {
//					prev = null ;
//					node = getTableUnsafe().writerGetSafe(hash) ;
//				} else {
//					node = prev2.getNextSafe() ;
//					prev = prev2 ;
//					prev2 = null ;
//				}
//				continue ;
//			} else if (node == REHASHING_FLAG) {
//				prev2 = prev = null ;
//				node = getTableSafe().writerGetUnsafe(hash) ;
//				continue ;
//			} else if (node.key.equals(key)) {
//				break ;
//			}
//			prev2 = prev ;
//			prev = node ;
//			node = node.getNextUnsafe() ;
//		}
//		if (node == null)
//			return null ;
//		return new NodePos<K, V>(prev, node) ;
//	}
//	
//	private boolean put(final K key, final V val, final int hash) {
//		ensureLoadFactor() ;
//		final N newnode = new N(key, val, hash) ;
//		
//		N prev = null ;
//		N node = getTableUnsafe().writerGetUnsafe(hash) ;
//		boolean r = true ;
//		while (true) {
//			if (node == null) {
//				
//				if (prev == null) {
//					// prev is null, so list is entirely empty; attempt to add at head
//					if (getTableUnsafe().compareAndSet(hash, null, newnode)) {
//						count.increment(hash) ;
//						return r ;
//					}
//					node = getTableUnsafe().writerGetSafe(hash) ;
//				} else {
//					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
//					if (prev.casNext(null, newnode)) {
//						count.increment(hash) ;
//						return r ;
//					} 
//					node = prev.getNextSafe() ;
//				}
//				
//			} else if (node == REHASHING_FLAG) {
//				
//				prev = null ;
//				node = getTableSafe().writerGetUnsafe(hash) ;
//				
//			} else if (node == DELETING_FLAG | node == DELETED_FLAG) {
//				
//				// { prev != null }
//				waitOnDelete(prev) ;
//				prev = null ;
//				node = getTableUnsafe().writerGetSafe(hash) ;
//				
//			} else if (key.equals(node.key)) {
//				
//				final N next = node.getNextUnsafe() ;
//				r = delete(hash, new NodePos<K, V>(prev, node)) ;
//				node = next ; // node has to have been deleted; prev may not be the real prev but if not we'll deal with that problem if/when we need to perform a put on it				
//				
//			} else {
//				
//				prev = node ;
//				node = node.getNextUnsafe() ;
//				
//			}
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	private boolean delete(int hash, NodePos<K, V> pos) {
//		boolean doGetNextSafely = false ;
//		while (pos != null) {
//			final N next = doGetNextSafely ? pos.node.getNextSafe() : pos.node.getNextUnsafe() ;
//			if (next == DELETED_FLAG | next == DELETING_FLAG) {
//				return false ;
//			}
//			if (next == REHASHING_FLAG) {
//				pos = find(hash, pos.node.key, true) ;
//				continue ;
//			}
//			if (!pos.node.casNext(next, DELETING_FLAG)) {
//				doGetNextSafely = true ;
//				continue ;
//			}
//			if (pos.prev == null) {
//				if (getTableUnsafe().compareAndSet(hash, pos.node, next)) {
//					pos.node.volatileSetDown(DELETED_FLAG) ;
//					count.decrement(hash) ;
//					waitingOnDelete.wake(pos.node) ;
//					return true ;
//				} else {
//					pos.node.volatileSetDown(next) ;
//					waitingOnDelete.wake(pos.node) ;
//				}
//			} else {
//				if (pos.prev.casNext(pos.node, next)) {
//					pos.node.volatileSetDown(DELETED_FLAG) ;
//					count.decrement(hash) ;
//					waitingOnDelete.wake(pos.node) ;
//					return true ;
//				} else {
//					pos.node.volatileSetDown(next) ;
//					waitingOnDelete.wake(pos.node) ;
//				}
//			}
//			pos = find(hash, pos.node.key, false) ;
//			doGetNextSafely = false ;
//		}
//		return false ;
//	}
//	
//	private boolean delete(final K key, final int hash) {
//		return delete(hash, find(hash, key, false)) ; 
//	}		
//	
//	private V get(K key, final int hash, boolean expect) {
//		Table<K, V> table ;
//		int candidates = 0 ;
//		int rehashes = 0 ;
//		int deletes = 0 ;
//		N node = (table = getTableUnsafe()).readerGetSafe(hash), prev = null, prev2 = null ;
//		while (node != null) {
//			if (node == REHASHING_FLAG) {
//				prev2 = prev = null ;
//				node = (table = getTableSafe()).readerGetUnsafe(hash) ;
//				rehashes++ ;
//			} else if (node == DELETED_FLAG | node == DELETING_FLAG) {
//				waitOnDelete(prev) ;
//				if (prev2 == null) {
//					prev = null ;
//					node = (table = getTableUnsafe()).readerGetSafe(hash) ;
//				} else {
//					node = prev2.getNextSafe() ;
//					prev = prev2 ;
//					prev2 = null ;
//				}
//				deletes++ ;
//			} else if (key.equals(node.key)) {
//				return node.value ;
//			} else {
//				prev2 = prev ;
//				prev = node ;
//				node = node.getNextUnsafe() ;
//			}
//			candidates++ ;
//		}
//		if (expect) {
//			System.out.println("Failed to find record " + key + " out of " + candidates + " candidates (" + deletes + " delete loops, " + rehashes + " rehash loops) in " + table + "; result of getSafe(): " + getSafe(key, hash)) ;
//		}
//		return null ;
//	}
//
//	private V getSafe(K key, final int hash) {
//		Table<K, V> table ;
//		int candidates = 0 ;
//		N node = (table = getTableSafe()).readerGetSafe(hash), prev = null, prev2 = null ;		
//		while (node != null) {
//			if (node == REHASHING_FLAG) {
//				prev2 = prev = null ;
//				node = (table = getTableSafe()).readerGetSafe(hash) ;
//			} else if (node == DELETED_FLAG | node == DELETING_FLAG) {
//				waitOnDelete(prev) ;
//				if (prev2 == null) {
//					prev = null ;
//					node = (table = getTableSafe()).readerGetSafe(hash) ;
//				} else {
//					node = prev2.getNextSafe() ;
//					prev = prev2 ;
//					prev2 = null ;
//				}
//			} else if (key.equals(node.key)) {
//				System.out.println("getSafe(): used " + table) ;
//				return node.value ;
//			} else {
//				prev2 = prev ;
//				prev = node ;
//				node = node.getNextSafe() ;
//			}
//			candidates++ ;
//		}
//		System.out.println("getSafe(): used " + table) ;
//		return null ;
//	}
//	
	
	private void waitOnDelete(final N node) {
		if (node.getNextSafe() != DELETING_FLAG)
			return ;
		WaitingOnDelete<N> waiting = new WaitingOnDelete<N>(Thread.currentThread(), node) ;
		waitingOnDelete.insert(waiting) ;
		while (node.getNextSafe() == DELETING_FLAG)
			LockSupport.park() ;
		waiting.remove() ;
	}
	
	@SuppressWarnings("unchecked")
	private void ensureLoadFactor() {		
		while (count.getUnsafe() > getTableUnsafe().capacity()) {
			final Table<N> table = getTableSafe() ;
			if (count.getSafe() <= table.capacity())
				return ;
			if (table instanceof RegularTable) {
				final BlockingTable<N> tmp = new BlockingTable<N>() ;
				if (casTable(table, tmp)) {
					final GrowingTable growingTable = new GrowingTable((RegularTable<N>) table) ;
					setTable(growingTable) ;
					tmp.wake(growingTable) ;
					growingTable.grow(0, false, true) ;
				}
			} else if (table instanceof BlockingTable) {
			} else {
				((GrowingTable) table).waitUntilGrown() ;
			}
		}
	}

	// *****************************************
	// NODE DECLARATION
	// *****************************************
	
	protected abstract static class Node<N extends Node<N>> {
		private final int hash ;
		private N nextPtr ;
		public Node(int hash) {
			this.hash = hash ;
		}
		public abstract N copy() ; 
		final boolean startRehashing(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, REHASHING_FLAG) ;
		}
		final boolean startDeleting(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, DELETING_FLAG) ;
		}
		final boolean finishDeleting() {
			return false ;
		}
		final boolean casNext(N expect, N upd) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, upd) ;
		}
		final N getNextUnsafe() {
			return nextPtr ;
		}
		@SuppressWarnings("unchecked")
		final N getNextSafe() {
			return (N) unsafe.getObjectVolatile(this, nextPtrOffset) ;
		}
		final void lazySetNext(N upd) {
			unsafe.putOrderedObject(this, nextPtrOffset, upd) ;
		}
		final void volatileSetDown(N upd) {
			unsafe.putObjectVolatile(this, nextPtrOffset, upd) ;
		}
		private static final long nextPtrOffset ;
		static {
			try {
				final Field field = Node.class.getDeclaredField("nextPtr") ;
				nextPtrOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	// *****************************************
	// UNDERLYING TABLE DEFINITIONS
	// *****************************************

	private static interface Table<N extends Node<N>> {
		public N writerGetSafe(int hash) ;
		public N writerGetUnsafe(int hash) ;
		public N readerGetSafe(int hash) ;
		public N readerGetUnsafe(int hash) ;
		public boolean compareAndSet(int hash, N expect, N update) ;
		public int capacity() ;
	}
	
	private static final class RegularTable<N extends Node<N>> implements Table<N> {
		private final N[] table ;
		private final int mask ;
		private final int capacity ;
		public RegularTable(N[] table, int capacity) {
			this.table = table ;
			this.mask = table.length - 1 ;
			this.capacity = capacity ;
		}
		@SuppressWarnings("unchecked")
		public final N writerGetSafe(int hash) {
			return getNodeVolatile(table, hash & mask) ;
		}
		public final N writerGetUnsafe(int hash) {
			return table[hash & mask] ;
		}
		@SuppressWarnings("unchecked")
		public final N readerGetSafe(int hash) {
			return getNodeVolatile(table, hash & mask) ;
		}
		public final N readerGetUnsafe(int hash) {
			return table[hash & mask] ;
		}
		public final boolean compareAndSet(int hash, N expect, N update) {
			return unsafe.compareAndSwapObject(table, nodeArrayIndexBaseOffset + nodeArrayIndexScale * (hash & mask), expect, update) ;
		}
		public final int capacity() {
			return capacity ;
		}
	}	
	
	@SuppressWarnings("unchecked")
	private static final class BlockingTable<N extends Node<N>> implements Table<N> {
		private final ThreadQueue waiting = new ThreadQueue(null) ;
		private volatile Table<N> next = null ;
		private void waitForNext() {
			waiting.insert(new ThreadQueue(Thread.currentThread())) ;
			while (next == null)
				LockSupport.park() ;
		}
		public N writerGetSafe(int hash) {
			waitForNext() ;
			return next.writerGetSafe(hash) ;
		}
		public N writerGetUnsafe(int hash) {
			waitForNext() ;
			return next.writerGetUnsafe(hash) ;
		}
		public N readerGetSafe(int hash) {
			waitForNext() ;
			return next.readerGetSafe(hash) ;
		}
		public N readerGetUnsafe(int hash) {
			waitForNext() ;
			return next.readerGetUnsafe(hash) ;
		}
		public boolean compareAndSet(int hash, N expect, N update) {
			waitForNext() ;
			return false ;
		}
		public int capacity() {
			waitForNext() ;
			return next.capacity() ;
		}
		public void wake(Table<N> next) {
			this.next = next ;
			waiting.wakeAll() ;
		}
	}
	
	private final class GrowingTable implements Table<N> {
		private final N[] oldTable ;
		private final N[] newTable ;
		private final int[] migrated ;
		private final int oldTableMask ;
		private final int newTableMask ;
		private final WaitingOnGrow waiting ;
		private final GrowCompletion completion = new GrowCompletion() ;
		private final int capacity ;
		@SuppressWarnings("unchecked")
		public GrowingTable(RegularTable<N> table) {
			this.oldTable = table.table ;
			this.oldTableMask = oldTable.length - 1 ;
			this.newTable = (N[]) new Node[oldTable.length << 1] ;
			this.newTableMask = newTable.length - 1 ;
			this.migrated = new int[(oldTable.length >> 5) + 1] ;
			this.waiting = new WaitingOnGrow(null, -1) ;
			this.capacity = (int)(newTable.length * loadFactor) ;
		}
		@SuppressWarnings("unchecked")
		public N readerGetSafe(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			N r ;
			if ((migrated[migratedIndex] & migratedBit) == 0) {
				r = getNodeVolatile(oldTable, oldTableIndex) ;
				if (r == REHASHING_FLAG) {
					wait(oldTableIndex) ;
					r = newTable[hash & newTableMask] ;
//					r = getNodeVolatile(newTable, hash & newTableMask) ;
				}
			} else {
				r = getNodeVolatile(newTable, hash & newTableMask) ;
			}
			return r ;
		}
		@SuppressWarnings("unchecked")
		public N readerGetUnsafe(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			N r ;
			if ((migrated[migratedIndex] & migratedBit) == 0) {
				r = oldTable[oldTableIndex] ;
				if (r == REHASHING_FLAG) {
					wait(oldTableIndex) ;
					r = newTable[hash & newTableMask] ;
				}
			} else {
				// just because migrated flag is 1, we cannot guarantee that we haven't seen partial data ahead of a volatile sync, so must perform a volatile read of the data here to be sure  
				r = getNodeVolatile(newTable, hash & newTableMask) ;
			}
			return r ;
		}
		public N writerGetSafe(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			if ((migrated[migratedIndex] & migratedBit) == 0) {
				grow(oldTableIndex, true, false) ;
				return getNodeVolatile(newTable, hash & newTableMask) ;
			}
			return getNodeVolatile(newTable, hash & newTableMask) ;
		}
		public N writerGetUnsafe(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			if ((migrated[migratedIndex] & migratedBit) == 0) {
				grow(oldTableIndex, true, false) ;
				return newTable[hash & newTableMask] ;
			}
			return getNodeVolatile(newTable, hash & newTableMask) ;
		}
		private void wait(int oldTableIndex) {
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			final WaitingOnGrow queue = new WaitingOnGrow(Thread.currentThread(), oldTableIndex) ;
			waiting.insert(queue) ;
			while ((getIntVolatile(migrated, migratedIndex) & migratedBit) == 0)
				LockSupport.park() ;
			queue.remove() ;
		}
		private void waitUntilGrown() {
			// small possibility somebody will get to here before the first grow() is called; this should only happen on small hash maps however
			if (NonBlockingHashStore.this.getTableSafe() != this)
				return ;
			final WaitingOnGrow queue = new WaitingOnGrow(Thread.currentThread(), -1) ;
			waiting.insert(queue) ;
			while (NonBlockingHashStore.this.getTableSafe() == this)
				LockSupport.park() ;
		}
		
		// compareAndSets don't need to be dealt with so strongly; if a thread is trying to update old and it is mid-grow, the cas will fail;
		// if it WAS trying to update old and it has been migrated, it will fail; so can assume that the value we obtain here is the one we were
		// looking for, as if not it will simply cause a retry/continue
		public boolean compareAndSet(int hash, N expect, N update) {
			final int oldTableIndex = hash & oldTableMask ;
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			if ((migrated[migratedIndex] & migratedBit) == 0) {
				return false ;
			} else {
				return casNodeArray(newTable, hash & newTableMask, expect, update) ;
			}
		}
		public void grow(int from, boolean needThisIndex, boolean initiator) {
			if (!completion.startContributing())
				return ;
			for (int i = from ; i != oldTable.length ; i++)
				if (!rehash(i, !(needThisIndex & (from == i))))
					break ;
			if (completion.finishContributing(initiator)) {
				NonBlockingHashStore.this.setTable(new RegularTable<N>(newTable, capacity)) ;
				waiting.wakeAll() ;
			}
		}
		@SuppressWarnings("unchecked")
		private boolean rehash(int oldTableIndex, boolean returnImmediatelyIfAlreadyRehashing) {
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			N cur ;
			{
				final long directOldTableIndex = directNodeArrayIndex(oldTableIndex) ;
				while (true) {
					cur = getNodeVolatileDirect(oldTable, directOldTableIndex) ;
					final boolean success = casNodeArrayDirect(oldTable, directOldTableIndex, cur, REHASHING_FLAG) ;
					if (cur == REHASHING_FLAG) {
						if (!returnImmediatelyIfAlreadyRehashing)
							wait(oldTableIndex) ;
						return false ;
					} 
					if (success)
						break ;
				}
			}
			final int extrabit = oldTable.length ;
			N tail1 = null ;
			N tail2 = null ;
			
			boolean doGetNextSafely = false ;
			while (cur != null) {
				final N next = (doGetNextSafely ? cur.getNextSafe() : cur.getNextUnsafe()) ;
				if (next == DELETING_FLAG) {
					// cur cannot be actually deleted as CAS operations to the head will fail, and we have set prev's next to RETRY_FLAG,
					// as such the delete will be aborted by the deleting thread at which point we can continue; however add ourselves to the
					// waiting queue so as to not spin wastefully
					waitOnDelete(cur) ;
					doGetNextSafely = false ;
					continue ;
				}
				if (cur.startRehashing(next)) {
					
					final int hash = cur.hash;
					if ((extrabit & hash) == 0) {
						// stays in old bucket
						// TODO : test if this is safe (not copying the nodes but just placing them in the new locations). should be given we safely suffix the list at all points with the rehashing flag...
						if (tail1 == null) {
							tail1 = cur ;
							lazySetNodeArray(newTable, oldTableIndex, tail1) ;
						} else {
							tail1.lazySetNext(cur) ;
							tail1 = cur ;
						}
					} else {
						// goes in new bucket
						if (tail2 == null) {
							tail2 = cur ;
							lazySetNodeArray(newTable, oldTableIndex | extrabit, tail2) ;
						} else {
							tail2.lazySetNext(cur) ;
							tail2 = cur ;
						}
					}
					
					cur = next ;
					doGetNextSafely = false ;
				} else {
					doGetNextSafely = true ;
				}
			}

			if (tail1 != null)
				tail1.lazySetNext(null) ;
			if (tail2 != null)
				tail2.lazySetNext(null) ;
//			if (tail1 != null) {
//				tail1.volatileSetDown(null) ;
//			} else if (tail2 != null) {
//				tail2.volatileSetDown(null) ;
//			} else {
//				volatileSetNodeArray(newTable, oldTableIndex, null) ;
//			}
			
			// flag as migrated
			final long directMigratedIndex = directIntArrayIndex(migratedIndex) ;
			int prevMigratedFlags = migrated[migratedIndex] ;
			if (!casIntArrayDirect(migrated, directMigratedIndex, prevMigratedFlags, prevMigratedFlags | migratedBit)) {
				while (true) {
					prevMigratedFlags = getIntVolatileDirect(migrated, directMigratedIndex) ;
					if (casIntArrayDirect(migrated, directMigratedIndex, prevMigratedFlags, prevMigratedFlags | migratedBit))
						break ;
				}
			}
			
			// wake up waiters
			waiting.wake(oldTableIndex) ;
			return true ;
		}
		@Override
		public int capacity() {
			return capacity ;
		}
	}
	
	private static final class GrowCompletion {
		// false indicates completion is finished
		private volatile int contributors = 1 ;
		private static final AtomicIntegerFieldUpdater<GrowCompletion> contributorsUpdater = AtomicIntegerFieldUpdater.newUpdater(GrowCompletion.class, "contributors") ;
		public boolean startContributing() {
			while (true) {
				final int contr = contributors ;
				if (contr == 0)
					return false ;
				if (contributorsUpdater.compareAndSet(this, contr, contr + 1))
					return true ;
			}
		}
		// true indicates this thread is the last thread to finish contributing to the grow, and as such should migrate the new table to a RegularTable object 
		public boolean finishContributing(boolean initiator) {
			final int delta = initiator ? 2 : 1 ;
			while (true) {
				final int contr = contributors ;
				final int next = contr - delta ;
				if (contributorsUpdater.compareAndSet(this, contr, next))
					return next == 0 ;
			}
		}
	}

	private static interface Count {
		public int getSafe() ;
		public int getUnsafe() ;
	}
	private static final class PreciseCount implements Count {
		private int count = 0 ;
		private static final long countOffset ;
		public int getSafe() {
			return unsafe.getIntVolatile(this, countOffset) ;
		}
		public int getUnsafe() {
			return count ;
		}
		public void increment(int hash) {
			{	final int count = this.count ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count + 1))
					return ;	}
			while (true) {
				final int count = unsafe.getIntVolatile(this, countOffset) ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count + 1))
					return ;
			}
		}
		public void decrement(int hash) {
			{	final int count = this.count ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count - 1))
					return ;	 }
			while (true) {
				final int count = unsafe.getIntVolatile(this, countOffset) ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count - 1))
					return ;
			}
		}
		static {
			try {
				final Field field = PreciseCount.class.getDeclaredField("count") ;
				countOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	private static final class SampleCount implements Count {
		private int count = 0 ;
		private static final long countOffset ;
		public final int getSafe() {
			return unsafe.getIntVolatile(this, countOffset) << 4 ;
		}
		public final int getUnsafe() {
			return count << 4 ;
		}
		public void increment(int hash) {
			if ((hash + System.currentTimeMillis() & 15) != 0)
				return ;
			{
				final int count = this.count ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count + 1))
					return ;
			}
			while (true) {
				final int count = unsafe.getIntVolatile(this, countOffset) ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count + 1))
					return ;
			}
		}
		public void decrement(int hash) {
			if ((hash + System.currentTimeMillis() & 15) != 0)
				return ;
			{
				final int count = this.count ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count - 1))
					return ;
			}
			while (true) {
				final int count = unsafe.getIntVolatile(this, countOffset) ;
				if (unsafe.compareAndSwapInt(this, countOffset, count, count - 1))
					return ;
			}
		}
		static {
			try {
				final Field field = SampleCount.class.getDeclaredField("count") ;
				countOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
		@Override
		public int getPrecise() {
			return count << 4 ;
		}
	}
	private static final class NoCount implements Count {
		public final int getSafe() { return 0 ; }
		public final int getUnsafe() { return 0 ; }
		public final int getPrecise() { return 0 ; }
		public void increment(int hash) { }
		public void decrement(int hash) { }
	}
	
    static int hash(int h) {
    	h ^= (h >>> 20) ^ (h >>> 12);
    	return h ^ (h >>> 7) ^ (h >>> 4);
    }
    
	private static final class WaitingOnGrow extends ThreadQueue<WaitingOnGrow> {
		private final int oldTableIndex ;
		public WaitingOnGrow(Thread thread, int oldTableIndex) {
			super(thread) ;
			this.oldTableIndex = oldTableIndex;
		}
		void wake(int tableIndex) {
			WaitingOnGrow next = this.next ;
			while (next != null) {
				if (tableIndex == next.oldTableIndex) {
					final WaitingOnGrow prev = next ;
					next = next.next ;
					prev.wake() ;
				} else {
					next = next.next ;
				}
			}
		}
		protected void remove() { super.remove() ; } 
	}
	
	private static final class WaitingOnDelete<N> extends ThreadQueue<WaitingOnDelete<N>> {
		private final N node ;
		public WaitingOnDelete(Thread thread, N node) {
			super(thread) ;
			this.node = node ;
		}
		void wake(N deleted) {
			WaitingOnDelete<N> next = this.next ;
			while (next != null) {
				if (deleted == next.node) {
					final WaitingOnDelete prev = next ;
					next = next.next ;
					prev.wake() ;
				} else {
					next = next.next ;
				}
			}
		}
		protected void remove() { super.remove() ; } 
	}
	
	private static final class NodePos<N> {
		final N prev ;
		final N node ;
		public NodePos(N prev, N node) {
			super();
			this.prev = prev;
			this.node = node;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final class FlagNode extends Node {
		public final String type ;
		public FlagNode(String type) {
			super(-1) ;
			this.type = type ;
		}
		public String toString() {
			return type ;
		}
	}
	
	public int size() {
		return count.getSafe() ;
	}

	private static final FlagNode REHASHING_FLAG = new FlagNode("REHASHING") ;	
	private static final FlagNode DELETED_FLAG = new FlagNode("DELETED") ;
	private static final FlagNode DELETING_FLAG = new FlagNode("DELETING") ;
	
	private static final long tablePtrOffset ;
    private static final long nodeArrayIndexBaseOffset = unsafe.arrayBaseOffset(Node[].class);
    private static final long nodeArrayIndexScale = unsafe.arrayIndexScale(Node[].class);
    private static final long intArrayIndexBaseOffset = unsafe.arrayBaseOffset(int[].class);
    private static final long intArrayIndexScale = unsafe.arrayIndexScale(int[].class);
	static {
		try {
			final Field field = NonBlockingHashStore.class.getDeclaredField("tablePtr") ;
			tablePtrOffset = unsafe.objectFieldOffset(field) ;
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e) ;
		}
	}
	
	private static Unsafe getUnsafe() {
		Unsafe unsafe = null ;
		try {
			Class<?> uc = Unsafe.class;
			Field[] fields = uc.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].getName().equals("theUnsafe")) {
					fields[i].setAccessible(true);
					unsafe = (Unsafe) fields[i].get(uc);
					break;
				}
			}
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e) ;
		}
		return unsafe;
	}
	
	private final boolean casTable(Table<N> expect, Table<N> update) {
		return unsafe.compareAndSwapObject(this, tablePtrOffset, expect, update) ;
	}

	private final void setTable(Table<N> update) {
		unsafe.putObjectVolatile(this, tablePtrOffset, update) ;
	}
	
	@SuppressWarnings("unchecked")
	private final Table<N> getTableSafe() {
		return (Table<N>) unsafe.getObjectVolatile(this, tablePtrOffset) ;
	}
	
	private final Table<N> getTableUnsafe() {
		return tablePtr ;
	}
	
	@SuppressWarnings("unchecked")
	private static final <N extends Node<N>> boolean casNodeArrayDirect(final N[] arr, final long i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, i, expect, upd) ;
	}	
	@SuppressWarnings("unchecked")
	private static final <N extends Node<N>> boolean casNodeArray(final N[] arr, final int i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), expect, upd) ;
	}	
	@SuppressWarnings("unchecked")
	private static final <N extends Node<N>> void lazySetNodeArray(final N[] arr, final int i, final N upd) {
		unsafe.putOrderedObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd) ;
	}
	@SuppressWarnings("unchecked")
	private static final <N extends Node<N>> void volatileSetNodeArray(final N[] arr, final int i, final N upd) {
		unsafe.putObjectVolatile(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd) ;
	}
	private static final long directIntArrayIndex(final int i) {
		return intArrayIndexBaseOffset + (intArrayIndexScale * i) ;
	}
	private static final long directNodeArrayIndex(final int i) {
		return nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i) ;
	}
	private static final boolean casIntArrayDirect(final int[] arr, final long i, final int expect, final int upd) {
		return unsafe.compareAndSwapInt(arr, i, expect, upd) ;
	}	
	private static final int getIntVolatile(final int[] arr, final int i) {
		return unsafe.getIntVolatile(arr, intArrayIndexBaseOffset + (intArrayIndexScale * i)) ;
	}
	private static final int getIntVolatileDirect(final int[] arr, final long i) {
		return unsafe.getIntVolatile(arr, i) ;
	}
	@SuppressWarnings("unchecked")
	private static final <N extends Node<N>> N getNodeVolatileDirect(final N[] arr, final long i) {
		return (N) unsafe.getObjectVolatile(arr, i) ;
	}
	@SuppressWarnings("unchecked")
	private static final <N extends Node<N>> N getNodeVolatile(final N[] arr, final int i) {
		return (N) unsafe.getObjectVolatile(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i)) ;
	}
	
}
