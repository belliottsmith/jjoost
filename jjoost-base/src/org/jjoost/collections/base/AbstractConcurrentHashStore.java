package org.jjoost.collections.base;


import java.io.Serializable ;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.LockSupport;

import org.jjoost.util.Functions ;
import org.jjoost.util.Iters ;
import org.jjoost.util.Rehasher ;
import org.jjoost.util.Rehashers ;
import org.jjoost.util.concurrent.ThreadQueue ;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public abstract class AbstractConcurrentHashStore<
	N extends AbstractConcurrentHashStore.ConcurrentHashNode<N>, 
	T extends AbstractConcurrentHashStore.Table<N>> implements HashStore<N> {

	private static final long serialVersionUID = -1578733824843315344L ;
	
	public enum Counting {
		OFF, SAMPLED, PRECISE
	}

	protected static final Unsafe unsafe = getUnsafe();
	
	protected final WaitingOnNode<N> waitingOnDelete = new WaitingOnNode<N>(null, null) ;
	protected final float loadFactor ;
	protected final Counter totalCounter ;
	protected final Counter uniquePrefixCounter ;
	protected final Counter growthCounter ;	
	private T tablePtr ;
	
	@SuppressWarnings("unchecked")
	public AbstractConcurrentHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
        int capacity = 8 ;
        while (capacity < initialCapacity)
        	capacity <<= 1 ;
        setTable(newRegularTable((N[]) new ConcurrentHashNode[capacity], (int) (capacity * loadFactor))) ;
        this.loadFactor = loadFactor ;
        if (totalCounting == null || uniquePrefixCounting == null)
        	throw new IllegalArgumentException() ;
        switch (uniquePrefixCounting) {
        case OFF: uniquePrefixCounter = DONT_COUNT ; break ;
        case SAMPLED: uniquePrefixCounter = new SampledCounter() ; break ;
        case PRECISE: uniquePrefixCounter = new PreciseCounter() ; break ;
        default: throw new IllegalArgumentException() ;
        }
        switch (totalCounting) {
        case OFF: totalCounter = DONT_COUNT ; break ;
        case SAMPLED: totalCounter = new SampledCounter() ; break ;
        case PRECISE: totalCounter = new PreciseCounter() ; break ;
        default: throw new IllegalArgumentException() ;
        }
        if (uniquePrefixCounting == Counting.OFF) {
        	growthCounter = totalCounter ;
        } else { 
        	growthCounter = uniquePrefixCounter ;
        }
	}
	
	protected void inserted(N node) { }
	protected void removed(N node) { }

	protected abstract T newRegularTable(N[] table, int capacity) ;
	protected abstract T newResizingTable(T table, int newCapacity) ;
	protected abstract T newBlockingTable() ;

	@Override
	public String toString() {
		return "{" + Iters.toString(all(null, null, Functions.<N>toString(true)), ", ") + "}" ;
	}

	@Override
	public int clear() {
		return Iters.count(clearAndReturn(Functions.identity())) ;
	}

	@Override
	public boolean isEmpty() {
		if (totalCounter.on())
			return totalCounter.getSafe() == 0 ;
		if (uniquePrefixCounter.on())
			return uniquePrefixCounter.getSafe() == 0 ;
		return getTableFresh().isEmpty() ;
	}

	@Override
	public final int capacity() {
		return getTableFresh().length() ;
	}
	
	@Override
	public final int totalCount() {
		return totalCounter.getSafe() ;
	}

	@Override
	public final int uniquePrefixCount() {
		return uniquePrefixCounter.getSafe() ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void resize(int size) {
        int capacity = 8 ;
        while (capacity < size)
        	capacity <<= 1 ;
        while (true) {
			final T table = getTableFresh() ;
        	if (table.length() == capacity)
        		return ;
        	if (table instanceof RegularTable) {
				final T tmp = newBlockingTable() ;
				if (casTable(table, tmp)) {
					final T resizingTable = newResizingTable((T) table, capacity) ;
					setTable(resizingTable) ;
					((BlockingTable<N, T>)tmp).wake(resizingTable) ;
					((ResizingTable<N, T>)resizingTable).rehash(0, false, true, false) ;
				}
        	} else if (table instanceof BlockingTable) {
        	} else {
        		((ResizingTable<N, T>) table).waitOnTableResize() ;
        	}
        }
	}

	@Override
	public final void shrink() {
        final int totalCount = uniquePrefixCounter.getSafe() ;
        int capacity = 8 ;
        while ((capacity * loadFactor) < totalCount)
        	capacity <<= 1 ;
        if (capacity < getTableFresh().length())
        	resize(capacity) ;
	}

	@SuppressWarnings("unchecked")
	protected final void grow() {		
		while (growthCounter.getUnsafe() > getTableStale().maxCapacity()) {
			final Table<N> table = getTableFresh() ;
			if (growthCounter.getSafe() <= table.maxCapacity())
				return ;
			if (table instanceof RegularTable) {
				final T tmp = newBlockingTable() ;
				if (casTable(table, tmp)) {
					final T growingTable = newResizingTable((T) table, table.length() << 1) ;
					setTable(growingTable) ;
					((BlockingTable<N, T>)tmp).wake(growingTable) ;
					((ResizingTable<N, T>)growingTable).rehash(0, false, true, false) ;
				}
			} else if (table instanceof BlockingTable) {
				((BlockingTable<N, T>) table).waitForNext() ;
			} else {
				((ResizingTable<N, T>) table).waitOnTableResize() ;
			}
		}
	}
	
	// *****************************************
	// NODE DECLARATION
	// *****************************************
	
	public abstract static class ConcurrentHashNode<N extends ConcurrentHashNode<N>> extends HashNode<N> {
		private static final long serialVersionUID = -6236082606699747110L ;
		private static final long nextPtrOffset ;
		private N nextPtr ;
		public ConcurrentHashNode(int hash) {
			super(hash) ;
		}
		public abstract N copy() ; 
		final boolean startRehashing(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, BLOCKING_REHASH_IN_PROGRESS_FLAG) ;
		}
		final boolean startTwoStepDelete(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, BLOCKING_DELETE_IN_PROGRESS_FLAG) ;
		}
		final void completeTwoStepDelete() {
			unsafe.putObjectVolatile(this, nextPtrOffset, DELETED_FLAG) ;
		}
		final boolean performOneStepSafeDelete(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, DELETED_FLAG) ;
		}
		final void performOneStepForcedLazyDelete() {
			unsafe.putOrderedObject(this, nextPtrOffset, DELETED_FLAG) ;
		}
		final boolean casNext(N expect, N upd) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, upd) ;
		}
		final N getNextStale() {
			return nextPtr ;
		}
		@SuppressWarnings("unchecked")
		final N getNextFresh() {
			return (N) unsafe.getObjectVolatile(this, nextPtrOffset) ;
		}
		final void lazySetNext(N upd) {
			unsafe.putOrderedObject(this, nextPtrOffset, upd) ;
		}
		final void volatileSetNext(N upd) {
			unsafe.putObjectVolatile(this, nextPtrOffset, upd) ;
		}
		static {
			try {
				final Field field = ConcurrentHashNode.class.getDeclaredField("nextPtr") ;
				nextPtrOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	// TODO: override serialization methods to throw an exception if they encounter a FlagNode; table can only be serialized when in a stable state
	static final class FlagNode extends ConcurrentHashNode {
		private static final long serialVersionUID = -8235849034699744602L ;
		public final String type ;
		public FlagNode(String type) {
			super(-1) ;
			this.type = type ;
		}
		public String toString() {
			return type ;
		}
		public FlagNode copy() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	protected static final FlagNode REHASHED_FLAG = new FlagNode("REHASHING") ;	
	protected static final FlagNode DELETED_FLAG = new FlagNode("DELETED") ;
	protected static final FlagNode BLOCKING_REHASH_IN_PROGRESS_FLAG = new FlagNode("REHASHING") ;	
	protected static final FlagNode BLOCKING_DELETE_IN_PROGRESS_FLAG = new FlagNode("DELETING") ;
	
	// *****************************************
	// UNDERLYING TABLE DEFINITIONS
	// *****************************************
	
	protected static interface Table<N extends ConcurrentHashNode<N>> {
		public int length() ;
		public int maxCapacity() ;
		public boolean isEmpty() ;
	}

	protected abstract static class RegularTable<N extends ConcurrentHashNode<N>> implements Table<N> {
		protected final N[] table ;
		protected final int mask ;
		protected final int capacity ;
		public RegularTable(N[] table, int capacity) {
			this.table = table ;
			this.mask = table.length - 1 ;
			this.capacity = capacity ;
		}
		public final int maxCapacity() {
			return capacity ;
		}
		public final int length() {
			return table.length ;
		}
		@Override
		public final boolean isEmpty() {
			for (int i = 0 ; i != table.length ; i++) {
				final N head = table[i] ;
				if (head != null)
					return false ;
			}
			return true ;
		}
	}	
	
	@SuppressWarnings("unchecked")
	protected static class BlockingTable<N extends ConcurrentHashNode<N>, T extends Table<N>> implements Table<N> {
		private final ThreadQueue waiting = new ThreadQueue(null) ;
		protected volatile T next = null ;
		protected final void waitForNext() {
			waiting.insert(new ThreadQueue(Thread.currentThread())) ;
			while (next == null)
				LockSupport.park() ;
		}
		public final void wake(T next) {
			this.next = next ;
			waiting.wakeAll() ;
		}
		public final int maxCapacity() {
			waitForNext() ;
			return next.maxCapacity() ;
		}
		public final int length() {
			waitForNext() ;
			return next.length() ;
		}
		@Override
		public final boolean isEmpty() {
			waitForNext() ;
			return next.isEmpty() ;
		}
	}
	
	protected abstract static class ResizingTable<N extends ConcurrentHashNode<N>, T extends Table<N>> implements Table<N> {
		private final AbstractConcurrentHashStore<N, T> store ;
		protected final N[] oldTable ;
		protected final N[] newTable ;
		protected final int oldTableMask ;
		protected final int newTableMask ;
		protected final WaitingOnGrow waiting ;
		protected final GrowCompletion completion = new GrowCompletion() ;
		protected final int capacity ;
		@SuppressWarnings("unchecked")
		public ResizingTable(AbstractConcurrentHashStore<N, T> store, RegularTable<N> table, int newLength) {
			this.store = store ;
			this.oldTable = table.table ;
			this.oldTableMask = oldTable.length - 1 ;
			this.newTable = (N[]) new ConcurrentHashNode[newLength] ;
			this.newTableMask = newTable.length - 1 ;
			this.waiting = new WaitingOnGrow(null, -1) ;
			this.capacity = (int)(newTable.length * store.loadFactor) ;
		}
		
		protected final void waitOnIndexResize(int oldTableIndex) {
			final WaitingOnGrow queue = new WaitingOnGrow(Thread.currentThread(), oldTableIndex) ;
			waiting.insert(queue) ;
			while (getNodeVolatile(oldTable, oldTableIndex) != REHASHED_FLAG)
				LockSupport.park() ;
			queue.remove() ;
		}
		
		protected final void waitOnTableResize() {
			// small possibility somebody will get to here before the first grow() is called; this should only happen on small hash maps however
			if (store.getTableFresh() != this)
				return ;
			final WaitingOnGrow queue = new WaitingOnGrow(Thread.currentThread(), -1) ;
			waiting.insert(queue) ;
			while (store.getTableFresh() == this)
				LockSupport.park() ;
		}
		
		@Override
		public final int maxCapacity() {
			return capacity ;
		}
		@Override
		public final int length() {
			return newTable.length ;
		}
		@Override
		public final boolean isEmpty() {
			return false ;
		}

		@SuppressWarnings("unchecked")
		public final void rehash(int from, boolean needThisIndex, boolean initiator, boolean tryAll) {
			if (!completion.startContributing())
				return ;
			boolean returnImmediatelyIfAlreadyHashing = !needThisIndex ;
			for (int i = from ; i != oldTable.length ; i++) {
				N head = startBucket(i, returnImmediatelyIfAlreadyHashing) ;
				if (head != null) {
					if (head != REHASHED_FLAG) {
						doBucket(head, i) ;						
						lazySetNodeArray(oldTable, i, REHASHED_FLAG) ;
						finishedBucket(head, i) ;
					}
					// wake up waiters
					waiting.wake(i) ;
				} else if (!tryAll) {
					break ;
				}
				returnImmediatelyIfAlreadyHashing = true ;
			}				
			if (completion.finishContributing(initiator)) {
				// perform a CAS rather than a set because a clear() could have already removed this table before grow completion
				store.casTable(this, store.newRegularTable(newTable, capacity)) ;
				waiting.wakeAll() ;
			}
		}
		
		protected abstract N startBucket(int oldTableIndex, boolean returnImmediatelyIfAlreadyRehashing) ;
		protected abstract void doBucket(N head, int oldTableIndex) ;
		protected void finishedBucket(N head, int oldTableIndex) {}
		
	}

	
	
	// ********************************************
	// THREAD WAITING UTILITIES
	// ********************************************
	
	
	
	
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

	protected static final class WaitingOnGrow extends ThreadQueue<WaitingOnGrow> {
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
	}
	
	protected static final class WaitingOnNode<N> extends ThreadQueue<WaitingOnNode<N>> {
		private final N node ;
		public WaitingOnNode(Thread thread, N node) {
			super(thread) ;
			this.node = node ;
		}
		void wake(N deleted) {
			WaitingOnNode<N> next = this.next ;
			while (next != null) {
				if (deleted == next.node) {
					final WaitingOnNode<N> prev = next ;
					next = next.next ;
					prev.wake() ;
				} else {
					next = next.next ;
				}
			}
		}		 
	}

	// *************************************
	// COUNTER DECLARATIONS
	// *************************************
	
	protected static interface Counter extends Serializable {
		public int getSafe() ;
		public int getUnsafe() ;
		public void increment(int hash) ;
		public void decrement(int hash) ;
		public boolean on() ;
		public Counter newInstance(int count) ;
	}
	protected static final class PreciseCounter implements Counter {
		private static final long serialVersionUID = -2830009566783179121L ;
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
		public boolean on() { return true ; }
		static {
			try {
				final Field field = PreciseCounter.class.getDeclaredField("count") ;
				countOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
		@Override
		public Counter newInstance(int count) {
			final PreciseCounter counter = new PreciseCounter() ;
			counter.count = count ;
			return counter ;
		}
	}
	protected static final class SampledCounter implements Counter {
		private static final long serialVersionUID = -6437345273821290811L ;
		private int count = 0 ;
		private static final long countOffset ;
		public final int getSafe() {
			return unsafe.getIntVolatile(this, countOffset) << 4 ;
		}
		public final int getUnsafe() {
			return count << 4 ;
		}
		public void increment(int hash) {
			if ((hash + System.nanoTime() & 31) != 0)
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
			if ((hash + System.nanoTime() & 31) != 0)
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
		public boolean on() { return true ; }
		@Override
		public Counter newInstance(int count) {
			final SampledCounter counter = new SampledCounter() ;
			counter.count = Math.max(count >> 4, 1) ;
			return counter ;
		}

		static {
			try {
				final Field field = SampledCounter.class.getDeclaredField("count") ;
				countOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	protected static final DontCount DONT_COUNT = new DontCount() ;
	protected static final class DontCount implements Counter {
		private static final long serialVersionUID = 1633916421321597636L ;
		public final int getSafe() { return Integer.MIN_VALUE ; }
		public final int getUnsafe() { return Integer.MIN_VALUE ; }
		public void increment(int hash) { }
		public void decrement(int hash) { }
		public boolean on() { return false ; }
		public Counter newInstance(int count) { return this; }
	}
	
	// *************************************
	// "UNSAFE" OPERATIONS
	// *************************************
	
	private static final long tablePtrOffset ;
    private static final long nodeArrayIndexBaseOffset = unsafe.arrayBaseOffset(ConcurrentHashNode[].class);
    private static final long nodeArrayIndexScale = unsafe.arrayIndexScale(ConcurrentHashNode[].class);
	static {
		try {
			final Field field = AbstractConcurrentHashStore.class.getDeclaredField("tablePtr") ;
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
	
	final boolean casTable(Table<N> expect, T update) {
		return unsafe.compareAndSwapObject(this, tablePtrOffset, expect, update) ;
	}

	final void setTable(T update) {
		unsafe.putObjectVolatile(this, tablePtrOffset, update) ;
	}
	
	@SuppressWarnings("unchecked")
	final T getTableFresh() {
		return (T) unsafe.getObjectVolatile(this, tablePtrOffset) ;
	}
	
	final T getTableStale() {
		return tablePtr ;
	}
	
	static final <N extends ConcurrentHashNode<N>> boolean casNodeArrayDirect(final N[] arr, final long i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, i, expect, upd) ;
	}	
	static final <N extends ConcurrentHashNode<N>> boolean casNodeArray(final N[] arr, final int i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), expect, upd) ;
	}	
	static final <N extends ConcurrentHashNode<N>> void lazySetNodeArray(final N[] arr, final int i, final N upd) {
		unsafe.putOrderedObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd) ;
	}
	static final <N extends ConcurrentHashNode<N>> void volatileSetNodeArray(final N[] arr, final int i, final N upd) {
		unsafe.putObjectVolatile(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd) ;
	}
	static final long directNodeArrayIndex(final int i) {
		return nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i) ;
	}
	@SuppressWarnings("unchecked")
	static final <N extends ConcurrentHashNode<N>> N getNodeVolatileDirect(final N[] arr, final long i) {
		return (N) unsafe.getObjectVolatile(arr, i) ;
	}
	@SuppressWarnings("unchecked")
	static final <N extends ConcurrentHashNode<N>> N getNodeVolatile(final N[] arr, final int i) {
		return (N) unsafe.getObjectVolatile(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i)) ;
	}

	public static Rehasher defaultRehasher() {
		return Rehashers.jdkHashmapRehasher() ;
	}

}
