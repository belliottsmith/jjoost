package org.jjoost.collections.base;

import java.util.Collections;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException;

import org.jjoost.collections.base.AbstractConcurrentHashStore.ConcurrentHashNode;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.util.Equality ;
import org.jjoost.util.Function ;
import org.jjoost.util.concurrent.waiting.UnfairParkingWaitQueue;
import org.jjoost.util.concurrent.waiting.WaitHandle;
import org.jjoost.util.concurrent.waiting.WaitQueue;

@SuppressWarnings("unchecked")
public class HashLockHashStore<N extends ConcurrentHashNode<N>> extends AbstractConcurrentHashStore<N, HashLockHashStore.Table<N>> {

	private static final long serialVersionUID = -369208509152951474L;

	private static ThreadLocal<LockNode> lockCache = new ThreadLocal<LockNode>() ;
	protected static final int REHASH_SEGMENT_SIZE = 256 ;
	protected static final int REHASH_SEGMENT_SHIFT = Integer.bitCount(REHASH_SEGMENT_SIZE - 1) ;

	public HashLockHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
		super(initialCapacity, loadFactor, totalCounting, uniquePrefixCounting) ;
	}

	static interface Table<N extends ConcurrentHashNode<N>> extends AbstractConcurrentHashStore.Table {
		ConcurrentHashNode lock(int hash) ;
		<NCmp> ConcurrentHashNode deleteOrLock(int hash, HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) ;
		ConcurrentHashNode insertOrLock(int hash, N node) ;
		void unlock(int hash, ConcurrentHashNode lock) ;
		ConcurrentHashNode read(int hash) ;
	}
	
	@Override
	protected Table<N> newBlockingTable() {
		return new BlockingTable<N>() ;
	}

	@Override
	protected Table<N> newRegularTable(ConcurrentHashNode[] table, int capacity) {
		return new RegularTable(table, capacity) ;
	}

	@Override
	protected Table<N> newResizingTable(Table<N> table, int newLength) {
		if (newLength == table.length() << 1)
			return new GrowingTable(this, (RegularTable) table, newLength) ;
		throw new IllegalArgumentException() ;
	}


	// **********************************************************
	// METHODS FOR INSERTION
	// **********************************************************	
	
	@Override
	public <NCmp, V> V put(
			NCmp find, N put, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		final int hash = put.hash ;
		final boolean replace = eq.isUnique() ;		

		Table<N> table = getTableStale() ;		
		ConcurrentHashNode lock = table.insertOrLock(hash, put) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).insertOrLock(hash, put) ;
		if (!(lock instanceof LockNode))
			return lock == null ? null : ret.apply((N) lock) ;
		
		try {
			
			boolean partial = false ;
			final int reverse = Integer.reverse(hash) ;
			
			ConcurrentHashNode p = lock ;
			ConcurrentHashNode n = lock.getNextStale() ;
			ConcurrentHashNode replaced = null ;
	   		while (n != null) {
	   			if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
	   				if (partial) break ;
	   				else partial = true ;
	   			}
	   			if (partial) {
	   				if (replace && eq.suffixMatch(find, (N) n)) {
	   	   				replaced = n ;
	   	   				break ;
	   				}   				
	   			} else if (HashNode.insertBefore(reverse, n)) {
	   				break ;
	   			}
	   			p = n ;
	   			n = n.getNextStale() ;
	   		}
	   		
			if (replaced == null) {
				put.lazySetNext(n) ;
				totalCounter.increment(hash) ;
			} else {
				removed((N) replaced) ;
				put.lazySetNext(replaced.getNextStale()) ;
				// set the next pointer of the removed node to the node that replaces it, so that iterators see consistent state
				replaced.lazySetNext(put) ;
			}
	   		p.lazySetNext(put) ;
	   		
	    	if (!partial)
	    		uniquePrefixCounter.increment(hash) ;

			inserted(put) ;
			if (replaced == null)
				return null ;
			return ret.apply((N) replaced) ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}
	
	@Override
	public <NCmp, V> V putIfAbsent(
			NCmp find, N put, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		final int hash = put.hash ;
		Table<N> table = getTableStale() ;		
		ConcurrentHashNode lock = table.insertOrLock(hash, put) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).insertOrLock(hash, put) ;
		if (!(lock instanceof LockNode))
			return lock == null ? null : ret.apply((N) lock) ;
		
		try {
			
			boolean partial = false ;
			final int reverse = Integer.reverse(hash) ;
			
			ConcurrentHashNode p = lock ;
			ConcurrentHashNode n = lock.getNextStale() ;
	   		while (n != null) {
	   			if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
	   				if (partial) break ;
	   				else partial = true ;
	   			}
	   			if (partial) {
	   				if (eq.suffixMatch(find, (N) n))
	   					return ret.apply((N) n) ;
	   			} else if (HashNode.insertBefore(reverse, n)) {
	   				break ;
	   			}
	   			p = n ;
	   			n = n.getNextStale() ;
	   		}
	   		
	   		put.lazySetNext(n) ;
	   		p.lazySetNext(put) ;
	   		
	    	if (!partial)
	    		uniquePrefixCounter.increment(hash) ;

			inserted(put) ;
			return null ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}
	
	@Override
	public <NCmp, V> V putIfAbsent(
			final int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			HashNodeFactory<? super NCmp, N> factory, 
			Function<? super N, ? extends V> ret,
			boolean returnNewIfCreated) {
		grow() ;
		
		Table<N> table = getTableStale() ; 
		ConcurrentHashNode lock = table.lock(hash) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).lock(hash) ;		
		try {
			
			boolean partial = false ;
			final int reverse = Integer.reverse(hash) ;
			
			ConcurrentHashNode p = lock ;
			ConcurrentHashNode n = lock.getNextStale() ;
	   		while (n != null) {
	   			if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
	   				if (partial) break ;
	   				else partial = true ;
	   			}
	   			if (partial) {
	   				if (eq.suffixMatch(find, (N) n))
	   					return ret.apply((N) n) ;
	   			} else if (HashNode.insertBefore(reverse, n)) {
	   				break ;
	   			}
	   			p = n ;
	   			n = n.getNextStale() ;
	   		}
	   		
	   		N put = factory.makeNode(hash, find) ;
	   		p.lazySetNext(put) ;
	   		put.lazySetNext(n) ;
	   		
	    	if (!partial)
	    		uniquePrefixCounter.increment(hash) ;

			inserted(put) ;
			
			if (returnNewIfCreated)
				return ret.apply(put) ;
			return null ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}
	
	// **********************************************************
	// METHODS FOR REMOVAL
	// **********************************************************	
	
	@Override
	public <NCmp> int remove(
			int hash, int removeAtMost, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return 0 ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}
		
		Table<N> table = getTableStale() ;		
		ConcurrentHashNode lock = table.deleteOrLock(hash, eq, find) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).deleteOrLock(hash, eq, find) ;
		if (!(lock instanceof LockNode))
			return lock == null ? 0 : 1 ;

		try {
			
			final boolean eqIsUniq = eq.isUnique() ;
	    	final int reverse = Integer.reverse(hash) ;
			boolean partial = false ;
			ConcurrentHashNode p = lock ;
			ConcurrentHashNode n = lock.getNextStale() ; 
	    	int r = 0 ;
	    	boolean keptNeighbours = false ;
	    	while (n != null) {
				if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
					if (partial) break ;
					else partial = true ;
				}
	    		if (partial && eq.suffixMatch(find, (N) n)) {
	    			// removing n
	    			r++ ;
	    			final ConcurrentHashNode next = n.getNextStale() ;
	    			p.lazySetNext(next) ;
	    			removed((N) n) ;
					n = next ;
	    			totalCounter.decrement(hash) ;
	    			if (eqIsUniq | (r == removeAtMost)) {
	    				if (!keptNeighbours)
	    					keptNeighbours = n != null 
	    						&& n.hash == hash 
	    						&& eq.prefixMatch(find, (N) n) ;
	    				break ;
	    			}
	    		} else if (partial) {
	    			keptNeighbours = true ;
	    			if (eqIsUniq)
	    				break ;
	    		} else if (HashNode.insertBefore(reverse, n)){
	    			break ;
	    		} else {
	    			p = n ;
	    			n = p.getNextStale() ;
	    		}
	    	}
	    	
	    	if (!keptNeighbours && r != 0)
	    		uniquePrefixCounter.decrement(hash) ;
			return r ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}
	
	@Override
	public <NCmp, V> V removeAndReturnFirst(
			int hash, int removeAtMost, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return null ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}

		Table<N> table = getTableStale() ;		
		ConcurrentHashNode lock = table.deleteOrLock(hash, eq, find) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).deleteOrLock(hash, eq, find) ;
		if (!(lock instanceof LockNode))
			return lock == null ? null : ret.apply((N) lock) ;
		
		try {
			
			final boolean eqIsUniq = eq.isUnique() ;
			boolean partial = false ;
	    	final int reverse = Integer.reverse(hash) ;
	    	ConcurrentHashNode p = lock ;
	    	ConcurrentHashNode n = lock.getNextStale() ; 
			boolean keptNeighbours = false ;
			ConcurrentHashNode removed = null ;
			int c = 0 ;
			while (n != null) {
				if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
					if (partial) break ;
					else partial = true ;
				}
				if (partial && eq.suffixMatch(find, (N) n)) {
					c++ ;
					final ConcurrentHashNode next = n.getNextStale() ;
					p.lazySetNext(next) ;
					if (removed == null)
						removed = n ;
					totalCounter.decrement(hash) ;
					removed((N) n) ;
					n = next ;
					if (eqIsUniq | (c == removeAtMost)) {
	    				if (!keptNeighbours)
	    					keptNeighbours = n != null 
	    						&& n.hash == hash 
	    						&& eq.prefixMatch(find, (N) n) ;
	    				break ;
					}
				} else if (partial) {
					keptNeighbours = true ;
					if (eqIsUniq)
						break ;
	    		} else if (HashNode.insertBefore(reverse, n)){
	    			break ;
	    		} else {
					p = n ;
					n = p.getNextStale() ;
				}
			}
			
			if (removed != null) {
				if (!keptNeighbours)
					uniquePrefixCounter.decrement(hash) ;
				return ret.apply((N) removed) ;
			}
			
			return null ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}
	
	@Override
	public <NCmp, V> Iterable<V> removeAndReturn(
			int hash, int removeAtMost, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return null ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}

		Table<N> table = getTableStale() ;
		ConcurrentHashNode lock = table.deleteOrLock(hash, eq, find) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).deleteOrLock(hash, eq, find) ;
		if (!(lock instanceof LockNode))
			return lock == null ? Collections.<V>emptyList() : new UniformList<V>(ret.apply((N) lock), 1) ;
			
		try {
			
			final boolean eqIsUniq = eq.isUnique() ;
			boolean partial = false ;
	    	final int reverse = Integer.reverse(hash) ;
	    	ConcurrentHashNode p = lock ;
	    	ConcurrentHashNode n = lock.getNextStale() ; 
			boolean keptNeighbours = false ;
			ConcurrentHashNode removedHead = null, removedTail = null ;
			int c = 0 ;
			while (n != null) {
				if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
					if (partial) break ;
					else partial = true ;
				}
				if (partial && eq.suffixMatch(find, (N) n)) {
					c++ ;
					final ConcurrentHashNode next = n.getNextStale() ;
					p.lazySetNext(next) ;
					if (removedHead == null) {
						removedHead = removedTail = n.copy() ;
					} else {
						removedTail.lazySetNext(n.copy()) ;
						removedTail = removedTail.getNextStale() ;
					}
					totalCounter.decrement(hash) ;
					removed((N) n) ;				
					n = next ;
					if (eqIsUniq | (c == removeAtMost)) {
	    				if (!keptNeighbours)
	    					keptNeighbours = n != null 
	    						&& n.hash == hash 
	    						&& eq.prefixMatch(find, (N) n) ;
	    				break ;
					}
				} else if (partial) {
					keptNeighbours = true ;
					if (eqIsUniq)
						break ;
	    		} else if (HashNode.insertBefore(reverse, n)){
	    			break ;
				} else {
					p = n ;
					n = p.getNextStale() ;
				}
			}
			
			if (!keptNeighbours && removedHead != null)
				uniquePrefixCounter.decrement(hash) ;
			
			return new SimpleNodeIterable<N, V>((N) removedHead, ret) ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}	
	
	@Override
	public <NCmp> boolean removeNode(
			Function<? super N, ? extends NCmp> nodePrefixEqFunc, 
			HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {
		throw new UnsupportedOperationException() ;
	}

	// **********************************************************
	// METHODS TO QUERY
	// **********************************************************	
	
	@Override
	public <NCmp> int count(int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq,
			int countUpTo) {
		if (countUpTo < 1)
			return 0 ;
		final boolean stopAtOne = eq.isUnique() | countUpTo == 1 ;
		boolean partial = false ;
		int count = 0 ;
		ConcurrentHashNode n = getTableStale().read(hash) ;
		while (n == REHASHED_FLAG)
			n = getTableFresh().read(hash) ;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
				if (partial) return count ;
				else partial = true ;
			}
			if (partial && eq.suffixMatch(find, (N) n)) {
				if (stopAtOne)
					return 1 ;
				count += 1 ;
				if (countUpTo == count)
					return count ;
			}
			n = n.getNextStale() ;
		}
		return count ;
	}

	@Override
	public <NCmp, V> V first(int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		final int reverse = Integer.reverse(hash) ;
		boolean partial = false ;
		ConcurrentHashNode n = getTableStale().read(hash) ;
		while (n == REHASHED_FLAG)
			n = getTableFresh().read(hash) ;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, (N) n))) {
				if (partial) return null ;
				else partial = true ;
			}
			if (partial && eq.suffixMatch(find, (N) n))
				return ret.apply((N) n) ;
			if (HashNode.insertBefore(reverse, n))
				break ;
			n = n.getNextStale() ;
		}
		return null ;
	}

	@Override
	public <NCmp, V> List<V> findNow(int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public <NCmp, NCmp2, V> Iterator<V> find(
			int hash, 
			NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> findEq,
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEq,
			Function<? super N, ? extends V> ret) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public <NCmp, V> Iterator<V> all(
			Function<? super N, ? extends NCmp> nodeEqualityProj,
			HashNodeEquality<? super NCmp, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj,
			Equality<? super NCmp> uniquenessEquality, 
			Locality duplicateLocality, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj,
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> ret) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public <NCmp> HashStore<N> copy(
			Function<? super N, ? extends NCmp> nodeEqualityProj,
			HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		throw new UnsupportedOperationException() ;
	}

	protected static final class SimpleNodeIterable<N extends ConcurrentHashNode<N>, V> implements Iterable<V> {
		private final N head ;
		private final Function<? super N, ? extends V> f ;
		public SimpleNodeIterable(N head, Function<? super N, ? extends V> f) {
			this.head = head ;
			this.f = f;
		}
		@Override
		public Iterator<V> iterator() {
			return new NodeIterator<N, V>(head, f) ;
		}		
	}
	
	protected static final class NodeIterator<N extends ConcurrentHashNode<N>, V> implements Iterator<V> {
		private ConcurrentHashNode cur ;
		private final Function<? super N, ? extends V> f ;
		public NodeIterator(N head, Function<? super N, ? extends V> f) {
			this.cur = head ;
			this.f = f;
		}
		@Override
		public boolean hasNext() {
			return cur != null ;
		}
		@Override
		public V next() {
			if (cur == null)
				throw new NoSuchElementException() ;
			final V r = f.apply((N) cur) ;
			cur = cur.getNextStale() ;
			return r ;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	private static final class LockNode<N extends ConcurrentHashNode<N>> extends ConcurrentHashNode<N> {
		private static final long serialVersionUID = 1L;
		final WaitQueue waiting = new UnfairParkingWaitQueue() ;
		public LockNode(int hash) {
			super(hash);
		}
		@Override
		public N copy() {
			throw new UnsupportedOperationException() ;
		}
		WaitHandle waitOn() {
			return waiting.register() ;
		}
		void wakeAll() {
			waiting.wakeAll() ;
		}
	}

	private final class RegularTable extends AbstractConcurrentHashStore.RegularTable implements Table<N> {

		public RegularTable(ConcurrentHashNode[] table, int capacity) {
			super(table, capacity);
		}
		
		@Override
		public final ConcurrentHashNode lock(int hash) {
			final int index = hash & mask ;
			final long directIndex = directNodeArrayIndex(index) ;
			final LockNode lock = lockNode() ;
			return lock(directIndex, lock) ;
		}
		
		private final ConcurrentHashNode lock(long directIndex, ConcurrentHashNode lock) {
			while (true) {
				final ConcurrentHashNode head = getNodeVolatileDirect(table, directIndex) ;
				if (head instanceof LockNode) {
					waitOnLock(table, head, directIndex) ;
					continue ;
				}
				if (head == REHASHED_FLAG)
					return (N) REHASHED_FLAG ;
				lock.lazySetNext(head) ;
				if (casNodeArrayDirect(table, directIndex, head, lock))
					return lock ;
			}
		}
		
		@Override
		public final <NCmp> ConcurrentHashNode deleteOrLock(int hash,
				HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) {
			final int index = hash & mask ;
			final long directIndex = directNodeArrayIndex(index) ;
			ConcurrentHashNode head ;
			while (true) {
				head = getNodeVolatileDirect(table, directIndex) ;
				if (head == null)
					return null ;
				if (head == REHASHED_FLAG)
					return (N) REHASHED_FLAG ;
				if (head instanceof LockNode) {
					waitOnLock(table, head, directIndex) ;
					continue ;
				}
				break ;
			}
			final ConcurrentHashNode lock = lockNode() ;
			lock.lazySetNext(head) ;
			if (casNodeArrayDirect(table, directIndex, head, lock))
				return lock ;
			return lock(directIndex, lock) ;
		}

		@Override
		public final ConcurrentHashNode insertOrLock(int hash, ConcurrentHashNode node) {
			final int index = hash & mask ;
			final long directIndex = directNodeArrayIndex(index) ;			
			ConcurrentHashNode lock = null ;
			ConcurrentHashNode head = table[index] ;
			while (true) {
				if (head == null) {
					if (casNodeArrayDirect(table, directIndex, null, node))
						return null ;
					head = getNodeVolatileDirect(table, directIndex) ;
					continue ;
				}
				if (head == REHASHED_FLAG)
					return (N) REHASHED_FLAG ;
				if (head instanceof LockNode) {
					waitOnLock(table, head, directIndex) ;
					head = getNodeVolatileDirect(table, directIndex) ;
					continue ;
				}
				if (lock == null)
					lock = lockNode() ;
				lock.lazySetNext(head) ;
				if (casNodeArrayDirect(table, directIndex, head, lock))
					return lock ;
				head = getNodeVolatileDirect(table, directIndex) ;
			}
		}
		
		@Override
		public final ConcurrentHashNode read(int hash) {
			final int i = hash & mask ;
			ConcurrentHashNode r = getNodeVolatile(table, i) ;
			while (r instanceof LockNode) {
				final ConcurrentHashNode l = r ;
				r = r.getNextStale() ;
				if (r == null) {
					final ConcurrentHashNode h = getNodeVolatile(table, i) ;
					if (l == h)
						break ;
					r = h ;
				} else if ((r.hash & mask) == i) {
					break ;
				} else {
					r = getNodeVolatile(table, i) ;
				}
			}
			return r ;
		}

		@Override
		public final void unlock(int hash, ConcurrentHashNode lock) {
			volatileSetNodeArray(table, hash & mask, lock.getNextStale()) ;
			((LockNode) lock).wakeAll() ;
		}

	}
	
	private static final class BlockingTable<N extends ConcurrentHashNode<N>> extends AbstractConcurrentHashStore.BlockingTable<Table<N>> implements Table<N> {

		@Override
		public ConcurrentHashNode lock(int hash) {
			waitForNext() ;
			return next.lock(hash) ;
		}

		@Override
		public ConcurrentHashNode insertOrLock(int hash, N node) {
			waitForNext() ;
			return next.insertOrLock(hash, node) ;
		}
		
		@Override
		public <NCmp> ConcurrentHashNode deleteOrLock(int hash,
				HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) {
			waitForNext() ;
			return next.deleteOrLock(hash, eq, find) ;
		}

		@Override
		public ConcurrentHashNode read(int hash) {
			waitForNext() ;
			return next.read(hash) ;
		}

		@Override
		public void unlock(int hash, ConcurrentHashNode lock) {
			next.unlock(hash, lock) ;
		}
		
	}
	
	private final class GrowingTable extends ResizingTable {
		public GrowingTable(AbstractConcurrentHashStore<N, Table<N>> store,
				RegularTable table, int newLength) {
			super(store, table, newLength);
		}
		@Override
		protected final void doBucket(ConcurrentHashNode lock, int oldTableIndex) {
			ConcurrentHashNode cur = lock.getNextStale() ;
			if (cur == null)
				return ;
			while (true) {
				final ConcurrentHashNode chainHead = cur ;
				final int chainIndex = chainHead.hash & newTableMask ;
				cur = cur.getNextStale() ;
				while (cur != null && (cur.hash & newTableMask) == chainIndex) {
					cur = cur.getNextStale() ;
				}
				if (cur == null) {
					lazySetNodeArray(newTable, chainIndex, chainHead) ;
					return ;
				} else {
					lazySetNodeArray(newTable, chainIndex, copyChain(chainHead, cur)) ;
				}
			}
		}
	}	
	
	private abstract class ResizingTable extends AbstractConcurrentHashStore.ResizingTable<Table<N>> implements Table<N> {

		public ResizingTable(AbstractConcurrentHashStore<N, Table<N>> store,
				RegularTable table, int newLength) {
			super(store, table, newLength, Math.max(1, table.table.length >>> REHASH_SEGMENT_SHIFT)) ;
		}

		protected abstract void doBucket(ConcurrentHashNode lock, int oldTableIndex) ;
		
		public final void rehash(int from, boolean needThisIndex) {
			final int needIndex = needThisIndex ? from : -1 ;
			from = from & ~(REHASH_SEGMENT_SIZE - 1) ;
			LockNode lock = null ;
			int c = 0 ;
			while (c < oldTableMask) {
				lock = startSegment(lock, from) ;
				if (lock == null) {
					if (needIndex < 0 || oldTable[needIndex] == REHASHED_FLAG)
						return ;
				} else {
					final int maxi = Math.min(oldTable.length, from + REHASH_SEGMENT_SIZE) ;
					for (int i = from ; i != maxi ; i++) {
						if (i == from || startBucket(lock, i)) {
							doBucket(lock, i) ;
							lazySetNodeArray(oldTable, i, REHASHED_FLAG) ;
						}
					}
					lock.wakeAll() ;
					finishSegment() ;
				}
				c = c + REHASH_SEGMENT_SIZE ;
				from = (from + REHASH_SEGMENT_SIZE) & oldTableMask ;
			}
			if (needIndex >= 0 && oldTable[needIndex] != REHASHED_FLAG)
				waitOnTableResize() ;
		}
		
		protected final LockNode startSegment(LockNode lock, int oldTableIndex) {
			ConcurrentHashNode cur ;
			final long directOldTableIndex = directNodeArrayIndex(oldTableIndex) ;
			while (true) {
				cur = getNodeVolatileDirect(oldTable, directOldTableIndex) ;
				if (cur instanceof LockNode) {
					waitOnLock(oldTable, cur, directOldTableIndex) ;
					continue ;
				}
				if (cur == REHASHED_FLAG)
					return null ;
				if (lock == null)
					lock = lockNode() ;
				lock.lazySetNext(cur) ;
				if (casNodeArrayDirect(oldTable, directOldTableIndex, cur, lock))
					return lock ;
			}
		}
		
		
		// returns a boolean indicating if work needs to be done
		protected final boolean startBucket(ConcurrentHashNode lock, int oldTableIndex) {
			ConcurrentHashNode cur ;
			final long directOldTableIndex = directNodeArrayIndex(oldTableIndex) ;
			while (true) {
				cur = getNodeVolatileDirect(oldTable, directOldTableIndex) ;
				if (cur == null) {
					if (casNodeArrayDirect(oldTable, directOldTableIndex, null, REHASHED_FLAG))
						return false ;
					continue ;
				}
				if (cur instanceof LockNode) {
					waitOnLock(oldTable, cur, directOldTableIndex) ;
					continue ;
				}
				if (cur == REHASHED_FLAG)
					throw new IllegalStateException() ;
				lock.lazySetNext(cur) ;
				if (casNodeArrayDirect(oldTable, directOldTableIndex, cur, lock))
					return true ;
			}
		}
		
		@Override
		public final ConcurrentHashNode lock(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			if (oldTable[oldTableIndex] != REHASHED_FLAG)
				rehash(oldTableIndex, true) ;			
			return lockInternal(directNodeArrayIndex(hash & newTableMask)) ;
		}

		private final ConcurrentHashNode lockInternal(long directNewTableIndex) {
			final ConcurrentHashNode lock = lockNode() ;
			while (true) {
				final ConcurrentHashNode head = getNodeVolatileDirect(newTable, directNewTableIndex) ;
				if (head instanceof LockNode) {
					waitOnLock(newTable, head, directNewTableIndex) ;
					continue ;
				}
				if (head == REHASHED_FLAG)
					return (N) REHASHED_FLAG ;
				lock.lazySetNext(head) ;
				if (casNodeArrayDirect(newTable, directNewTableIndex, head, lock))
					return lock ;
			}
		}
		
		@Override
		public final ConcurrentHashNode insertOrLock(int hash, N node) {
			final int oldTableIndex = hash & oldTableMask ;
			if (oldTable[oldTableIndex] != REHASHED_FLAG)
				rehash(oldTableIndex, true) ;			
			final int newTableIndex = hash & newTableMask ;
			final long newTableIndexDirect = directNodeArrayIndex(hash & newTableMask) ;
			if (newTable[newTableIndex] == null && casNodeArrayDirect(newTable, newTableIndexDirect, null, node))
				return null ;
			return lockInternal(newTableIndexDirect) ;
		}

		@Override
		public final <NCmp> ConcurrentHashNode deleteOrLock(int hash,
				HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) {
			final int oldTableIndex = hash & oldTableMask ;
			if (oldTable[oldTableIndex] != REHASHED_FLAG)
				rehash(oldTableIndex, true) ;			
			final long newTableIndexDirect = directNodeArrayIndex(hash & newTableMask) ;
			return lockInternal(newTableIndexDirect) ;
		}
		
		@Override
		public final ConcurrentHashNode read(int hash) {
			ConcurrentHashNode[] table = oldTable ;
			int mask = oldTableMask ;
			boolean vol = false ;
			int index = hash & mask ;
			ConcurrentHashNode r = table[index] ;
			while (true) {
				while (r instanceof LockNode) {
					final ConcurrentHashNode l = r ;
					r = r.getNextStale() ;
					if (r == null) {
						final ConcurrentHashNode h = getNodeVolatile(table, index) ;
						if (l == h)
							break ;
						r = h ;
					} else if ((r.hash & mask) == index) {
						break ;
					} else {
						r = getNodeVolatile(table, index) ;
					}
				}
				if (r == REHASHED_FLAG) {
					if (table == oldTable) {
						table = newTable ;
						mask = newTableMask ;
						index = hash & mask ;
					} else {
						return (N) REHASHED_FLAG ;
					}
				} else if (vol) {
					return r ;
				}
				r = getNodeVolatile(table, index) ;
				vol = true ;
			}
		}

		@Override
		public final void unlock(int hash, ConcurrentHashNode lock) {
			volatileSetNodeArray(newTable, hash & newTableMask, lock.getNextStale()) ;
			((LockNode)lock).wakeAll() ;
		}
		
	}

	abstract class AbstractIterator<NCmp, V> extends AbstractHashNodeIterator<N, NCmp, V> {

		public AbstractIterator(
				Function<? super N, ? extends NCmp> nodeEqualityProj,
				HashNodeEquality<? super NCmp, ? super N> nodeEquality,
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret);
		}
		
		@Override
		protected void delete(N[] nodes, int node) {
			removeNode(nodeEqualityProj, nodeEquality, nodes[node]) ;
		}
		
	}
	
	final LockNode lockNode() {
		LockNode lock = lockCache.get() ;
		if (lock == null) {
			lock = new LockNode(0) ;
			lockCache.set(lock) ;
		}
		return lock ;
	}
	
	private void waitOnLock(ConcurrentHashNode[] table, ConcurrentHashNode lock, long directIndex) {
		while (true) {
			final WaitHandle wait = ((LockNode) lock).waitOn() ;
			if (getNodeVolatileDirect(table, directIndex) != lock)
				return ;
			wait.awaitUninterruptibly() ;
			if (getNodeVolatileDirect(table, directIndex) != lock)
				return ;
		}
	}
	
	private static ConcurrentHashNode copyChain(ConcurrentHashNode head, ConcurrentHashNode tail) {
		ConcurrentHashNode r = head.copy() ;
		ConcurrentHashNode t = r ;
		head = head.getNextStale() ;
		while (head != tail) {
			ConcurrentHashNode next = head.copy() ;
			t.lazySetNext(next) ;
			t = next ;
			head = head.getNextStale() ;
		}
		return r ;
	}

}
