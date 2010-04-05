package org.jjoost.collections.base;

import java.util.Collections;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.LockSupport;

import org.jjoost.collections.base.AbstractConcurrentHashStore.ConcurrentHashNode;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.util.Equality ;
import org.jjoost.util.Function ;

public class HashLockHashStore<N extends ConcurrentHashNode<N>> extends AbstractConcurrentHashStore<N, HashLockHashStore.Table<N>> {

	private static final long serialVersionUID = -369208509152951474L;

	private final WaitingOnNode<N> waitingOnLock = new WaitingOnNode<N>(null, null) ;
	private LockCache lockCache = new LockCache(new Thread[32], new LockNode[32]) ;
	

	public HashLockHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
		super(initialCapacity, loadFactor, totalCounting, uniquePrefixCounting) ;
	}

	static interface Table<N extends ConcurrentHashNode<N>> extends AbstractConcurrentHashStore.Table<N> {
		N lock(int hash) ;
		<NCmp> N deleteOrLock(int hash, HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) ;
		N insertOrLock(int hash, N node) ;
		void unlock(int hash, N lock) ;
		N read(int hash) ;
	}
	
	@Override
	protected Table<N> newBlockingTable() {
		return new BlockingTable<N>() ;
	}

	@Override
	protected Table<N> newRegularTable(N[] table, int capacity) {
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
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <NCmp, V> V put(
			NCmp find, N put, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		final int hash = put.hash ;
		final boolean replace = eq.isUnique() ;		

		Table<N> table = getTableStale() ;		
		N lock = table.insertOrLock(hash, put) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).insertOrLock(hash, put) ;
		if (!(lock instanceof LockNode))
			return lock == null ? null : ret.apply(lock) ;
		
		try {
			
			boolean partial = false ;
			final int reverse = Integer.reverse(hash) ;
			
	   		N p = lock ;
	    	N n = lock.getNextStale() ;
	    	N replaced = null ;
	   		while (n != null) {
	   			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
	   				if (partial) break ;
	   				else partial = true ;
	   			}
	   			if (partial) {
	   				if (replace && eq.suffixMatch(find, n)) {
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
				removed(replaced) ;
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
			return ret.apply(replaced) ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <NCmp, V> V putIfAbsent(
			NCmp find, N put, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		final int hash = put.hash ;
		Table<N> table = getTableStale() ;		
		N lock = table.insertOrLock(hash, put) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).insertOrLock(hash, put) ;
		if (!(lock instanceof LockNode))
			return lock == null ? null : ret.apply(lock) ;
		
		try {
			
			boolean partial = false ;
			final int reverse = Integer.reverse(hash) ;
			
	   		N p = lock ;
	    	N n = lock.getNextStale() ;
	   		while (n != null) {
	   			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
	   				if (partial) break ;
	   				else partial = true ;
	   			}
	   			if (partial) {
	   				if (eq.suffixMatch(find, n))
	   					return ret.apply(n) ;
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
		N lock = table.lock(hash) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).lock(hash) ;		
		try {
			
			boolean partial = false ;
			final int reverse = Integer.reverse(hash) ;
			
	   		N p = lock ;
	    	N n = lock.getNextStale() ;
	   		while (n != null) {
	   			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
	   				if (partial) break ;
	   				else partial = true ;
	   			}
	   			if (partial) {
	   				if (eq.suffixMatch(find, n))
	   					return ret.apply(n) ;
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
	
	@SuppressWarnings("unchecked")
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
		N lock = table.deleteOrLock(hash, eq, find) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).deleteOrLock(hash, eq, find) ;
		if (!(lock instanceof LockNode))
			return lock == null ? 0 : 1 ;

		try {
			
			final boolean eqIsUniq = eq.isUnique() ;
	    	final int reverse = Integer.reverse(hash) ;
			boolean partial = false ;
	    	N p = lock ;
	    	N n = lock.getNextStale() ; 
	    	int r = 0 ;
	    	boolean keptNeighbours = false ;
	    	while (n != null) {
				if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
					if (partial) break ;
					else partial = true ;
				}
	    		if (partial && eq.suffixMatch(find, n)) {
	    			// removing n
	    			r++ ;
	    			final N next = n.getNextStale() ;
	    			p.lazySetNext(next) ;
	    			removed(n) ;
					n = next ;
	    			totalCounter.decrement(hash) ;
	    			if (eqIsUniq | (r == removeAtMost)) {
	    				if (!keptNeighbours)
	    					keptNeighbours = n != null 
	    						&& n.hash == hash 
	    						&& eq.prefixMatch(find, n) ;
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
	
	@SuppressWarnings("unchecked")
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
		N lock = table.deleteOrLock(hash, eq, find) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).deleteOrLock(hash, eq, find) ;
		if (!(lock instanceof LockNode))
			return lock == null ? null : ret.apply(lock) ;
		
		try {
			
			final boolean eqIsUniq = eq.isUnique() ;
			boolean partial = false ;
	    	final int reverse = Integer.reverse(hash) ;
			N p = lock ;
			N n = lock.getNextStale() ; 
			boolean keptNeighbours = false ;
			N removed = null ;
			int c = 0 ;
			while (n != null) {
				if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
					if (partial) break ;
					else partial = true ;
				}
				if (partial && eq.suffixMatch(find, n)) {
					c++ ;
					final N next = n.getNextStale() ;
					p.lazySetNext(next) ;
					if (removed == null)
						removed = n ;
					totalCounter.decrement(hash) ;
					removed(n) ;
					n = next ;
					if (eqIsUniq | (c == removeAtMost)) {
	    				if (!keptNeighbours)
	    					keptNeighbours = n != null 
	    						&& n.hash == hash 
	    						&& eq.prefixMatch(find, n) ;
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
				return ret.apply(removed) ;
			}
			
			return null ;
			
		} finally {
			table.unlock(hash, lock) ;
		}
		
	}
	
	@SuppressWarnings("unchecked")
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
		N lock = table.deleteOrLock(hash, eq, find) ;
		while (lock == REHASHED_FLAG)
			lock = (table = getTableFresh()).deleteOrLock(hash, eq, find) ;
		if (!(lock instanceof LockNode))
			return lock == null ? Collections.<V>emptyList() : new UniformList<V>(ret.apply(lock), 1) ;
			
		try {
			
			final boolean eqIsUniq = eq.isUnique() ;
			boolean partial = false ;
	    	final int reverse = Integer.reverse(hash) ;
			N p = lock ;
			N n = lock.getNextStale() ; 
			boolean keptNeighbours = false ;
			N removedHead = null, removedTail = null ;
			int c = 0 ;
			while (n != null) {
				if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
					if (partial) break ;
					else partial = true ;
				}
				if (partial && eq.suffixMatch(find, n)) {
					c++ ;
					final N next = n.getNextStale() ;
					p.lazySetNext(next) ;
					if (removedHead == null) {
						removedHead = removedTail = n.copy() ;
					} else {
						removedTail.lazySetNext(n.copy()) ;
						removedTail = removedTail.getNextStale() ;
					}
					totalCounter.decrement(hash) ;
					removed(n) ;				
					n = next ;
					if (eqIsUniq | (c == removeAtMost)) {
	    				if (!keptNeighbours)
	    					keptNeighbours = n != null 
	    						&& n.hash == hash 
	    						&& eq.prefixMatch(find, n) ;
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
			
			return new SimpleNodeIterable<N, V>(removedHead, ret) ;
			
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
		N n = getTableStale().read(hash) ;
		while (n == REHASHED_FLAG)
			n = getTableFresh().read(hash) ;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
				if (partial) return count ;
				else partial = true ;
			}
			if (partial && eq.suffixMatch(find, n)) {
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
		N n = getTableStale().read(hash) ;
		while (n == REHASHED_FLAG)
			n = getTableFresh().read(hash) ;
		while (n != null) {
			if (partial != (n.hash == hash && eq.prefixMatch(find, n))) {
				if (partial) return null ;
				else partial = true ;
			}
			if (partial && eq.suffixMatch(find, n))
				return ret.apply(n) ;
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
		private N cur ;
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
			final V r = f.apply(cur) ;
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
		public LockNode(int hash) {
			super(hash);
		}
		@Override
		public N copy() {
			throw new UnsupportedOperationException() ;
		}
	}

	private final class RegularTable extends AbstractConcurrentHashStore.RegularTable<N> implements Table<N> {

		public RegularTable(N[] table, int capacity) {
			super(table, capacity);
		}
		
		@Override
		public final N lock(int hash) {
			final int index = hash & mask ;
			final long directIndex = directNodeArrayIndex(index) ;
			final N lock = lockNode() ;
			return lock(directIndex, lock) ;
		}
		
		@SuppressWarnings("unchecked")
		private final N lock(long directIndex, N lock) {
			while (true) {
				final N head = getNodeVolatileDirect(table, directIndex) ;
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
		
		@SuppressWarnings("unchecked")
		@Override
		public final <NCmp> N deleteOrLock(int hash,
				HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) {
			final int index = hash & mask ;
			final long directIndex = directNodeArrayIndex(index) ;
			N head ;
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
			final N lock = lockNode() ;
			lock.lazySetNext(head) ;
			if (casNodeArrayDirect(table, directIndex, head, lock))
				return lock ;
			return lock(directIndex, lock) ;
		}

		@SuppressWarnings("unchecked")
		@Override
		public final N insertOrLock(int hash, N node) {
			final int index = hash & mask ;
			final long directIndex = directNodeArrayIndex(index) ;			
			N lock = null ;
			while (true) {
				final N head = getNodeVolatileDirect(table, directIndex) ;
				if (head == null) {
					if (casNodeArrayDirect(table, directIndex, null, node))
						return null ;
					continue ;
				}
				if (head == REHASHED_FLAG)
					return (N) REHASHED_FLAG ;
				if (head instanceof LockNode) {
					waitOnLock(table, head, directIndex) ;
					continue ;
				}
				if (lock == null)
					lock = lockNode() ;
				lock.lazySetNext(head) ;
				if (casNodeArrayDirect(table, directIndex, head, lock))
					return lock ;
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final N read(int hash) {
			final int i = hash & mask ;
			final N r = getNodeVolatile(table, i) ;
			if (r instanceof LockNode)
				return r.getNextStale() ;
			return r ;
		}

		@Override
		public final void unlock(int hash, N lock) {
			volatileSetNodeArray(table, hash & mask, lock.getNextStale()) ;
			waitingOnLock.wake(lock) ;
		}

	}
	
	private static final class BlockingTable<N extends ConcurrentHashNode<N>> extends AbstractConcurrentHashStore.BlockingTable<N, Table<N>> implements Table<N> {

		@Override
		public N lock(int hash) {
			waitForNext() ;
			return next.lock(hash) ;
		}

		@Override
		public N insertOrLock(int hash, N node) {
			waitForNext() ;
			return next.insertOrLock(hash, node) ;
		}
		
		@Override
		public <NCmp> N deleteOrLock(int hash,
				HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) {
			waitForNext() ;
			return next.deleteOrLock(hash, eq, find) ;
		}

		@Override
		public N read(int hash) {
			waitForNext() ;
			return next.read(hash) ;
		}

		@Override
		public void unlock(int hash, N lock) {
			next.unlock(hash, lock) ;
		}
		
	}
	
	private final class GrowingTable extends ResizingTable {
		public GrowingTable(AbstractConcurrentHashStore<N, Table<N>> store,
				RegularTable table, int newLength) {
			super(store, table, newLength);
		}
		@Override
		protected final void doBucket(N lock, int oldTableIndex) {
			N cur = lock.getNextStale() ;
			while (true) {
				final N chainHead = cur ;
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
	
	private abstract class ResizingTable extends AbstractConcurrentHashStore.ResizingTable<N, Table<N>> implements Table<N> {

		public ResizingTable(AbstractConcurrentHashStore<N, Table<N>> store,
				RegularTable table, int newLength) {
			super(store, table, newLength);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected final N startBucket(int oldTableIndex,
				boolean returnImmediatelyIfAlreadyRehashing) {
			N cur ;
			final long directOldTableIndex = directNodeArrayIndex(oldTableIndex) ;
			N lock = null ;
			while (true) {
				cur = getNodeVolatileDirect(oldTable, directOldTableIndex) ;
				if (cur == null) {
					if (casNodeArrayDirect(oldTable, directOldTableIndex, null, REHASHED_FLAG))
						return (N) REHASHED_FLAG ;
					continue ;
				}
				if (cur instanceof LockNode) {
					waitOnLock(oldTable, cur, directOldTableIndex) ;
					continue ;
				}
				if (cur == REHASHED_FLAG) {
					if (!returnImmediatelyIfAlreadyRehashing)
						waitOnIndexResize(oldTableIndex) ;
					return null ;
				}
				if (lock == null)
					lock = lockNode() ;
				lock.lazySetNext(cur) ;
				if (casNodeArrayDirect(oldTable, directOldTableIndex, cur, lock))
					return lock ;
			}
		}
		
		@Override
		protected final void finishedBucket(N lock, int oldTableIndex) {
			waitingOnLock.wake(lock) ;
		}

		@SuppressWarnings("unchecked")
		@Override
		public final N lock(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			if (oldTable[oldTableIndex] != REHASHED_FLAG)
				super.rehash(oldTableIndex, true, false, true) ;			
			final int newTableIndex = hash & newTableMask ;
			final long directNewTableIndex = directNodeArrayIndex(newTableIndex) ;
			final N lock = lockNode() ;
			while (true) {
				final N head = getNodeVolatileDirect(newTable, directNewTableIndex) ;
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
		public final N insertOrLock(int hash, N node) {
			return lock(hash) ;
		}

		@Override
		public final <NCmp> N deleteOrLock(int hash,
				HashNodeEquality<? super NCmp, ? super N> eq, NCmp find) {
			return lock(hash) ;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final N read(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			N oldTableHead = oldTable[oldTableIndex] ;
			if (oldTableHead != REHASHED_FLAG)
				oldTableHead = getNodeVolatile(oldTable, oldTableIndex) ;
			N head ;
			if (oldTableHead == REHASHED_FLAG) {
				final int newTableIndex = hash & newTableMask ;
				head = newTable[newTableIndex] ;
			} else {
				head = oldTableHead ;
			}
			if (head instanceof LockNode)
				head = head.getNextStale() ;
			return head ;
		}

		@Override
		public final void unlock(int hash, N lock) {
			volatileSetNodeArray(newTable, hash & newTableMask, lock.getNextStale()) ;
			waitingOnLock.wake(lock) ;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	final N lockNode() {
		final Thread me = Thread.currentThread() ;
		final int hash = me.hashCode() ;
		while (true) {			
			final LockCache cache = lockCache ;
			final Thread[] threads = cache.threads ;
			final int mask = threads.length - 1 ;
			int index = hash & mask ;
			while (true) {
				final Thread t = threads[index] ;
				if (t == me)
					return (N) cache.locks[index] ;
				if (t == null) {
					addLock(me) ;
					if (threads[index] == me)
						return (N) cache.locks[index] ;
					break ;
				}
				index = (index + 1) & mask;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	final synchronized void addLock(final Thread me) {
		LockCache cache = lockCache ;
		if (cache.count == cache.threads.length >> 2) {
			final LockCache repl = new LockCache(new Thread[cache.threads.length << 1], new LockNode[cache.threads.length << 1]) ;
			final int mask = repl.threads.length - 1 ;
			for (int i = 0 ; i != cache.threads.length ; i++) {
				final Thread t = cache.threads[i] ;
				if (t != null) {
					int index = t.hashCode() & mask ;
					while (true) {
						if (repl.threads[index] == null) {
							repl.threads[index] = t ;
							repl.locks[index] = cache.locks[i] ;
							break ;
						}
						index = (index + 1) & mask ;
					}
				}
			}
			lockCache = repl ;
			cache = repl ;
		}
		final int hash = me.hashCode() ;
		final Thread[] threads = cache.threads ;
		final int mask = threads.length - 1 ;
		int index = hash & mask ;
		while (true) {
			if (threads[index] == null) {
				threads[index] = me ;
				cache.locks[index] = new LockNode(0) ;
				cache.count += 1 ;
				break ;
			}
			index = (index + 1) & mask ;
		}
	}
	
	private void waitOnLock(N[] table, N lock, long directIndex) {
		WaitingOnNode<N> queue = new WaitingOnNode<N>(Thread.currentThread(), lock) ;
		waitingOnLock.insert(queue) ;
		while (getNodeVolatileDirect(table, directIndex) == lock)
			LockSupport.park() ;
		queue.remove() ;
	}
	
	private static final class LockCache {
		int count ;
		final Thread[] threads ;
		@SuppressWarnings("unchecked")
		final LockNode[] locks ;
		@SuppressWarnings("unchecked")
		public LockCache(Thread[] threads, LockNode[] locks) {
			this.threads = threads;
			this.locks = locks;
		}
	}
	
	private static <N extends ConcurrentHashNode<N>> N copyChain(N head, N tail) {
		N r = head.copy() ;
		N t = r ;
		head = head.getNextStale() ;
		while (head != tail) {
			N next = head.copy() ;
			t.lazySetNext(next) ;
			t = next ;
			head = head.getNextStale() ;
		}
		return r ;
	}
	
}
