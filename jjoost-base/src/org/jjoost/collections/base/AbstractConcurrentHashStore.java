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

package org.jjoost.collections.base;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;

import org.jjoost.util.Functions;
import org.jjoost.util.Iters;
import org.jjoost.util.Rehasher;
import org.jjoost.util.Rehashers;
import org.jjoost.util.concurrent.waiting.UnfairWaitQueue;
import org.jjoost.util.concurrent.waiting.WaitHandle;
import org.jjoost.util.concurrent.waiting.WaitQueue;

@SuppressWarnings("unchecked")
public abstract class AbstractConcurrentHashStore<
	N extends AbstractConcurrentHashStore.ConcurrentHashNode<N>, 
	T extends AbstractConcurrentHashStore.Table> implements HashStore<N> {

	private static final long serialVersionUID = -1578733824843315344L;
	
	public enum Counting {
		OFF, SAMPLED, PRECISE
	}

	protected final float loadFactor;
	protected final Counter totalCounter;
	protected final Counter uniquePrefixCounter;
	protected final Counter growthCounter;
	private T tablePtr;
	
	public AbstractConcurrentHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
        int capacity = 8;
        while (capacity < initialCapacity)
        	capacity <<= 1;
        setTable(newRegularTable((N[]) new ConcurrentHashNode[capacity], (int) (capacity * loadFactor)));
        this.loadFactor = loadFactor;
        if (totalCounting == null || uniquePrefixCounting == null)
        	throw new IllegalArgumentException();
        switch (uniquePrefixCounting) {
        case OFF: uniquePrefixCounter = DONT_COUNT ; break;
        case SAMPLED: uniquePrefixCounter = new SampledCounter() ; break;
        case PRECISE: uniquePrefixCounter = new PreciseCounter() ; break;
        default: throw new IllegalArgumentException();
        }
        switch (totalCounting) {
        case OFF: totalCounter = DONT_COUNT ; break;
        case SAMPLED: totalCounter = new SampledCounter() ; break;
        case PRECISE: totalCounter = new PreciseCounter() ; break;
        default: throw new IllegalArgumentException();
        }
        if (uniquePrefixCounting == Counting.OFF) {
        	growthCounter = totalCounter;
        } else { 
        	growthCounter = uniquePrefixCounter;
        }
	}
	
	protected void inserted(N node) { }
	protected void removed(N node) { }

	protected abstract T newRegularTable(ConcurrentHashNode<N>[] table, int capacity);
	protected abstract T newResizingTable(T table, int newCapacity);
	protected abstract T newBlockingTable();

	@Override
	public String toString() {
		return "{" + Iters.toString(all(null, null, Functions.<N>toString(true)), ", ") + "}";
	}

	@Override
	public int clear() {
		return Iters.count(clearAndReturn(Functions.identity()));
	}

	@Override
	public boolean isEmpty() {
		if (totalCounter.on())
			return totalCounter.getSafe() == 0;
		if (uniquePrefixCounter.on())
			return uniquePrefixCounter.getSafe() == 0;
		return getTableFresh().isEmpty();
	}

	@Override
	public final int capacity() {
		return getTableFresh().length();
	}
	
	@Override
	public final int totalCount() {
		return totalCounter.getSafe();
	}

	@Override
	public final int uniquePrefixCount() {
		return uniquePrefixCounter.getSafe();
	}

	@Override
	public final void resize(int size) {
        int capacity = 8;
        while (capacity < size)
        	capacity <<= 1;
        while (true) {
			final T table = getTableFresh();
        	if (table.length() == capacity)
        		return;
        	if (table instanceof RegularTable) {
				final T tmp = newBlockingTable();
				if (casTable(table, tmp)) {
					final T resizingTable = newResizingTable((T) table, capacity);
					setTable(resizingTable);
					((BlockingTable<T>)tmp).wake(resizingTable);
					((ResizingTable<T>)resizingTable).rehash(0, false);
				}
        	} else if (table instanceof BlockingTable) {
        	} else {
        		((ResizingTable<T>) table).waitOnTableResize();
        	}
        }
	}

	@Override
	public final void shrink() {
        final int totalCount = uniquePrefixCounter.getSafe();
        int capacity = 8;
        while ((capacity * loadFactor) < totalCount)
        	capacity <<= 1;
        if (capacity < getTableFresh().length())
        	resize(capacity);
	}

	protected final void grow() {		
		while (growthCounter.getUnsafe() > getTableStale().maxCapacity()) {
			final Table table = getTableFresh();
			if (growthCounter.getSafe() <= table.maxCapacity())
				return;
			if (table instanceof RegularTable) {
				final T tmp = newBlockingTable();
				if (casTable(table, tmp)) {
					final T growingTable = newResizingTable((T) table, table.length() << 1);
					setTable(growingTable);
					((BlockingTable<T>)tmp).wake(growingTable);
					((ResizingTable<T>)growingTable).rehash(0, false);
				}
			} else if (table instanceof BlockingTable) {
				((BlockingTable<T>) table).waitForNext();
			} else {
				((ResizingTable<T>) table).waitOnTableResize();
			}
		}
	}
	
	// *****************************************
	// NODE DECLARATION
	// *****************************************
	
	public abstract static class ConcurrentHashNode<N extends ConcurrentHashNode<N>> extends HashNode<N> {
		private static final long serialVersionUID = -6236082606699747110L;
		private static final long nextPtrOffset;
		private N nextPtr;
		public ConcurrentHashNode(int hash) {
			super(hash);
		}
		public abstract N copy();
		final boolean startRehashing(ConcurrentHashNode<?> expect) {
			return Unsafe.INST.compareAndSwapObject(this, nextPtrOffset, expect, BLOCKING_REHASH_IN_PROGRESS_FLAG);
		}
		final boolean startTwoStepDelete(ConcurrentHashNode<?> expect) {
			return Unsafe.INST.compareAndSwapObject(this, nextPtrOffset, expect, BLOCKING_DELETE_IN_PROGRESS_FLAG);
		}
		final void completeTwoStepDelete() {
			Unsafe.INST.putObjectVolatile(this, nextPtrOffset, DELETED_FLAG);
		}
		final boolean performOneStepSafeDelete(ConcurrentHashNode<?> expect) {
			return Unsafe.INST.compareAndSwapObject(this, nextPtrOffset, expect, DELETED_FLAG);
		}
		final void performOneStepForcedLazyDelete() {
			Unsafe.INST.putOrderedObject(this, nextPtrOffset, DELETED_FLAG);
		}
		final boolean casNext(ConcurrentHashNode<?> expect, ConcurrentHashNode<?> upd) {
			return Unsafe.INST.compareAndSwapObject(this, nextPtrOffset, expect, upd);
		}
		final ConcurrentHashNode<?> getNextStale() {
			return nextPtr;
		}
		final ConcurrentHashNode<?> getNextFresh() {
			return (ConcurrentHashNode<?>) Unsafe.INST.getObjectVolatile(this, nextPtrOffset);
		}
		final void lazySetNext(ConcurrentHashNode<?> upd) {
			Unsafe.INST.putOrderedObject(this, nextPtrOffset, upd);
		}
		final void volatileSetNext(ConcurrentHashNode<?> upd) {
			Unsafe.INST.putObjectVolatile(this, nextPtrOffset, upd);
		}
		static {
			try {
				final Field field = ConcurrentHashNode.class.getDeclaredField("nextPtr");
				nextPtrOffset = Unsafe.INST.objectFieldOffset(field);
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e);
			}
		}
	}
	
	// TODO: override serialization methods to throw an exception if they encounter a FlagNode; table can only be serialized when in a stable state
	static final class FlagNode extends ConcurrentHashNode<FlagNode> {
		private static final long serialVersionUID = -8235849034699744602L;
		public final String type;
		public FlagNode(String type) {
			super(-1);
			this.type = type;
		}
		public String toString() {
			return type;
		}
		public FlagNode copy() {
			throw new UnsupportedOperationException();
		}
	}
	
	protected static final FlagNode REHASHED_FLAG = new FlagNode("REHASHED");
	protected static final FlagNode DELETED_FLAG = new FlagNode("DELETED");
	protected static final FlagNode BLOCKING_REHASH_IN_PROGRESS_FLAG = new FlagNode("REHASHING");
	protected static final FlagNode BLOCKING_DELETE_IN_PROGRESS_FLAG = new FlagNode("DELETING");
	
	// *****************************************
	// UNDERLYING TABLE DEFINITIONS
	// *****************************************
	
	protected static interface Table {
		public int length();
		public int maxCapacity();
		public boolean isEmpty();
	}

	protected abstract static class RegularTable implements Table {
		protected final ConcurrentHashNode[] table;
		protected final int mask;
		protected final int capacity;
		public RegularTable(ConcurrentHashNode[] table, int capacity) {
			this.table = table;
			this.mask = table.length - 1;
			this.capacity = capacity;
		}
		public final int maxCapacity() {
			return capacity;
		}
		public final int length() {
			return table.length;
		}
		@Override
		public final boolean isEmpty() {
			for (int i = 0 ; i != table.length ; i++) {
				final ConcurrentHashNode head = table[i];
				if (head != null)
					return false;
			}
			return true;
		}
	}	
	
	protected static class BlockingTable<T extends Table> implements Table {
		private final WaitQueue waiting = new UnfairWaitQueue();
		protected volatile T next = null;
		protected final void waitForNext() {
			final WaitHandle wait = waiting.register();
			if (next != null)
				return;
			wait.waitForeverNoInterrupt();
		}
		public final void wake(T next) {
			this.next = next;
			waiting.wakeAll();
		}
		public final int maxCapacity() {
			waitForNext();
			return next.maxCapacity();
		}
		public final int length() {
			waitForNext();
			return next.length();
		}
		@Override
		public final boolean isEmpty() {
			waitForNext();
			return next.isEmpty();
		}
	}
	
	protected abstract static class ResizingTable<T extends Table> implements Table {
		
		private final AbstractConcurrentHashStore<?, T> store;
		protected final ConcurrentHashNode[] oldTable;
		protected final ConcurrentHashNode[] newTable;
		protected final int oldTableMask;
		protected final int newTableMask;
		protected final WaitQueue waitingForCompletion = new UnfairWaitQueue();
		protected final int capacity;
		protected int remainingSegments;
		
		public ResizingTable(AbstractConcurrentHashStore<?, T> store, RegularTable table, int newLength, int segments) {
			this.store = store;
			this.oldTable = table.table;
			this.oldTableMask = oldTable.length - 1;
			this.newTable = new ConcurrentHashNode[newLength];
			this.newTableMask = newTable.length - 1;
			this.capacity = (int)(newTable.length * store.loadFactor);
			Unsafe.INST.putIntVolatile(this, remainingSegmentsOffset, segments);
		}
		
		protected final void waitOnTableResize() {
			if (store.getTableFresh() != this)
				return;
			final WaitHandle wait = waitingForCompletion.register();
			if (store.getTableFresh() != this)
				return;
			wait.waitForeverNoInterrupt();
		}
		
		@Override
		public final int maxCapacity() {
			return capacity;
		}
		@Override
		public final int length() {
			return newTable.length;
		}
		@Override
		public final boolean isEmpty() {
			return false;
		}

		protected abstract void rehash(int from, boolean needThisIndex);
		
		protected final void finishSegment() {
			int remaining = remainingSegments;
			while (true) {
				if (Unsafe.INST.compareAndSwapInt(this, remainingSegmentsOffset, remaining, remaining - 1))
					break;
				remaining = Unsafe.INST.getIntVolatile(this, remainingSegmentsOffset);
			}
			if (remaining == 1) {
				store.casTable(this, store.newRegularTable(newTable, capacity));
				waitingForCompletion.wakeAll();
			}
		}
		
		private static final long remainingSegmentsOffset;
		static {
			try {
				final Field field = ResizingTable.class.getDeclaredField("remainingSegments");
				remainingSegmentsOffset = Unsafe.INST.objectFieldOffset(field);
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		
	}

	// *************************************
	// COUNTER DECLARATIONS
	// *************************************
	
	protected static interface Counter extends Serializable {
		public int getSafe();
		public int getUnsafe();
		public void increment(int hash);
		public void decrement(int hash);
		public boolean on();
		public Counter newInstance(int count);
	}
	protected static final class PreciseCounter implements Counter {
		private static final long serialVersionUID = -2830009566783179121L;
		private int count = 0;
		private static final long countOffset;
		public int getSafe() {
			return Unsafe.INST.getIntVolatile(this, countOffset);
		}
		public int getUnsafe() {
			return count;
		}
		public void increment(int hash) {
			{	final int count = this.count;
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count + 1))
					return ;	}
			while (true) {
				final int count = Unsafe.INST.getIntVolatile(this, countOffset);
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count + 1))
					return;
			}
		}
		public void decrement(int hash) {
			{	final int count = this.count;
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count - 1))
					return ;	 }
			while (true) {
				final int count = Unsafe.INST.getIntVolatile(this, countOffset);
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count - 1))
					return;
			}
		}
		public boolean on() { return true ; }
		static {
			try {
				final Field field = PreciseCounter.class.getDeclaredField("count");
				countOffset = Unsafe.INST.objectFieldOffset(field);
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		@Override
		public Counter newInstance(int count) {
			final PreciseCounter counter = new PreciseCounter();
			counter.count = count;
			return counter;
		}
	}
	protected static final class SampledCounter implements Counter {
		private static final long serialVersionUID = -6437345273821290811L;
		private int count = 0;
		private static final long countOffset;
		public final int getSafe() {
			return Unsafe.INST.getIntVolatile(this, countOffset) << 4;
		}
		public final int getUnsafe() {
			return count << 4;
		}
		public void increment(int hash) {
			if ((hash + System.nanoTime() & 31) != 0)
				return;
			{
				final int count = this.count;
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count + 1))
					return;
			}
			while (true) {
				final int count = Unsafe.INST.getIntVolatile(this, countOffset);
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count + 1))
					return;
			}
		}
		public void decrement(int hash) {
			if ((hash + System.nanoTime() & 31) != 0)
				return;
			{
				final int count = this.count;
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count - 1))
					return;
			}
			while (true) {
				final int count = Unsafe.INST.getIntVolatile(this, countOffset);
				if (Unsafe.INST.compareAndSwapInt(this, countOffset, count, count - 1))
					return;
			}
		}
		public boolean on() { return true ; }
		@Override
		public Counter newInstance(int count) {
			final SampledCounter counter = new SampledCounter();
			counter.count = Math.max(count >> 4, 1);
			return counter;
		}

		static {
			try {
				final Field field = SampledCounter.class.getDeclaredField("count");
				countOffset = Unsafe.INST.objectFieldOffset(field);
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e);
			}
		}
	}
	
	protected static final DontCount DONT_COUNT = new DontCount();
	protected static final class DontCount implements Counter {
		private static final long serialVersionUID = 1633916421321597636L;
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
	
	private static final long tablePtrOffset;
    private static final long nodeArrayIndexBaseOffset = Unsafe.INST.arrayBaseOffset(ConcurrentHashNode[].class);
    private static final long nodeArrayIndexScale = Unsafe.INST.arrayIndexScale(ConcurrentHashNode[].class);
	static {
		try {
			final Field field = AbstractConcurrentHashStore.class.getDeclaredField("tablePtr");
			tablePtrOffset = Unsafe.INST.objectFieldOffset(field);
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
	}
	
	final boolean casTable(Table expect, T update) {
		return Unsafe.INST.compareAndSwapObject(this, tablePtrOffset, expect, update);
	}

	final void setTable(T update) {
		Unsafe.INST.putObjectVolatile(this, tablePtrOffset, update);
	}
	
	final T getTableFresh() {
		return (T) Unsafe.INST.getObjectVolatile(this, tablePtrOffset);
	}
	
	final T getTableStale() {
		return tablePtr;
	}
	
	static final boolean casNodeArrayDirect(final ConcurrentHashNode[] arr, final long i, final ConcurrentHashNode expect, final ConcurrentHashNode upd) {
		return Unsafe.INST.compareAndSwapObject(arr, i, expect, upd);
	}	
	static final boolean casNodeArray(final ConcurrentHashNode[] arr, final int i, final ConcurrentHashNode expect, final ConcurrentHashNode upd) {
		return Unsafe.INST.compareAndSwapObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), expect, upd);
	}	
	static final void lazySetNodeArray(final ConcurrentHashNode[] arr, final int i, final ConcurrentHashNode upd) {
		Unsafe.INST.putOrderedObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd);
	}
	static final void volatileSetNodeArray(final ConcurrentHashNode[] arr, final int i, final ConcurrentHashNode upd) {
		Unsafe.INST.putObjectVolatile(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd);
	}
	static final long directNodeArrayIndex(final int i) {
		return nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i);
	}
	static final ConcurrentHashNode getNodeVolatileDirect(final ConcurrentHashNode[] arr, final long i) {
		return (ConcurrentHashNode) Unsafe.INST.getObjectVolatile(arr, i);
	}
	static final ConcurrentHashNode getNodeVolatile(final ConcurrentHashNode[] arr, final int i) {
		return (ConcurrentHashNode) Unsafe.INST.getObjectVolatile(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i));
	}

	public static Rehasher defaultRehasher() {
		return Rehashers.jdkHashmapRehasher();
	}

}
