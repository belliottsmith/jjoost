package org.jjoost.collections.base;

import java.io.Serializable ;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList ;
import java.util.Arrays;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.LockSupport;

import org.jjoost.collections.lists.UniformList ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Factories;
import org.jjoost.util.Factory;
import org.jjoost.util.Filter;
import org.jjoost.util.Filters;
import org.jjoost.util.Function ;
import org.jjoost.util.Functions ;
import org.jjoost.util.Iters ;
import org.jjoost.util.Rehasher ;
import org.jjoost.util.Rehashers ;
import org.jjoost.util.concurrent.ThreadQueue ;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class LockFreeHashStore<N extends LockFreeHashStore.LockFreeHashNode<N>> implements HashStore<N> {

	@SuppressWarnings("unchecked")
	private static final Factory NO_FILTER = Factories.constant(Filters.acceptAll()) ;
	
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
	private Table<N> tablePtr ;
	
	@SuppressWarnings("unchecked")
	public LockFreeHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
        int capacity = 8 ;
        while (capacity < initialCapacity)
        	capacity <<= 1 ;
        setTable(new RegularTable<N>((N[]) new LockFreeHashNode[capacity], (int) (capacity * loadFactor))) ;
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

	protected LockFreeHashStore(float loadFactor, Counter totalCounter, Counter uniqCounter, boolean useUniqCounterForGrowth, N[] table) {
		this.loadFactor = loadFactor ;
		this.totalCounter = totalCounter ;
		this.uniquePrefixCounter = uniqCounter ;
		this.growthCounter = useUniqCounterForGrowth ? uniqCounter : totalCounter ;
		setTable(new RegularTable<N>(table, (int) loadFactor * table.length)) ;
	}
	
	protected void inserted(N node) { }
	protected void removed(N node) { }


		
	// **********************************************************
	// METHODS FOR INSERTION
	// **********************************************************	
	
	
	
	@Override
	public <NCmp, V> V put(
			NCmp find, N put, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		final boolean replace = eq.isUnique() ;		
		final int hash = put.hash ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				// we have not encountered any prior partial or complete node matches, so simply insert the node at the end of the list
				
				if (prev == null) {
					
					// prev is null, so list is entirely empty; attempt to add at head
					if (getTableUnsafe().compareAndSet(hash, null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return null ;
					}
					partial = false ;
					node = getTableUnsafe().writerGetFresh(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						if (!partial)
							uniquePrefixCounter.increment(hash) ;
						return null ;
					} 
					node = prev.getNextFresh() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				final boolean prevPartial = partial ;
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) {
						
						// at this point we have previously seen at least one partial match but have encountered no complete matches
						// so try to insert the new node here
						// { prev != null }
						put.lazySetNext(node) ;
						if (prev.casNext(node, put)) {
							inserted(put) ;
							totalCounter.increment(hash) ;
							return null ;
						}
						// if we fail, backtrack to prev2.next, or to the list head if prev2 is null
						if (prev2 == null) {
							partial = false ;
							prev2 = prev = null ;						
							node = getTableUnsafe().writerGetFresh(hash) ;
						} else {
							node = prev2.getNextFresh() ;
							prev = prev2 ;
							prev2 = null ;
							partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
						}
						continue ;
						
					} else {
						// we have not seen a partial match before this node, so simply set partial match to true						
						partial = true ;
					}
				}
				
				if ((partial & replace) && eq.suffixMatch(find, node)) {
					
					// this node is a complete match AND we are replacing complete matches, so swap node for put
					final N next = node.getNextStale() ;
					if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG | next == REHASHED_FLAG){
						if (next == REHASHING_FLAG | next == REHASHED_FLAG) {
							// bucket is being rehashed, so start again
							partial = false ;
							prev2 = prev = null ;
							node = getTableFresh().writerGetFresh(hash) ;
						} else {
							// node is being deleted, so wait for it to complete and grab prev.next
							waitOnDelete(node) ;
							if (prev == null) {
								partial = false ;
								prev2 = prev = null ;
								node = getTableUnsafe().writerGetFresh(hash) ;
							} else {
								node = prev.getNextStale() ;
								partial = prevPartial ; ;
							}							
						}
						continue ;
					}
					
					put.lazySetNext(next) ;
					if (prev == null) {
						if (getTableUnsafe().compareAndSet(hash, node, put)) {
							inserted(put) ;
							removed(node) ;
							return ret.apply(node) ;
						}
						// we failed, so start from head again
						partial = false ;
						prev2 = prev = null ;
						node = getTableUnsafe().writerGetFresh(hash) ;						
						continue ;
					} else {
						if (prev.casNext(node, put)) {
							inserted(put) ;
							removed(node) ;
							return ret.apply(node) ;
						}
						// we failed, so backtrack one
						node = prev.getNextStale() ;
						partial = prevPartial ; ;
						continue ;
					}
					
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
			}
		}
	}

	
	@Override
	public <NCmp, V> V putIfAbsent(
			NCmp find, N put, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		final int hash = put.hash ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			if (node == null) {
				
				// we have not encountered any prior partial or complete node matches, so simply insert the node at the end of the list
				
				if (prev == null) {
					
					// prev is null, so list is entirely empty; attempt to add at head
					if (getTableUnsafe().compareAndSet(hash, null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return null ;
					}
					partial = false ;
					node = getTableUnsafe().writerGetFresh(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						if (!partial)
							uniquePrefixCounter.increment(hash) ;
						return null ;
					} 
					node = prev.getNextFresh() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) {
						
						// at this point we have previously seen at least one partial match but have encountered no complete matches
						// so try to insert the new node here
						// { prev != null }
						put.lazySetNext(node) ;
						if (prev.casNext(node, put)) {
							inserted(put) ;
							totalCounter.increment(hash) ;
							return null ;
						}
						// if we fail, backtrack to prev2.next, or to the list head if prev2 is null
						if (prev2 == null) {
							partial = false ;
							prev2 = prev = null ;						
							node = getTableUnsafe().writerGetFresh(hash) ;
						} else {
							node = prev2.getNextFresh() ;
							prev = prev2 ;
							prev2 = null ;
							partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
						}
						continue ;
						
					} else {
						// we have not seen a partial match before this node, so simply set partial match to true						
						partial = true ;
					}
				}
				
				if (partial && eq.suffixMatch(find, node)) {
					// this node is a complete match so simply return it
					return ret.apply(node) ;
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
			}
		}
	}
	
	@Override
	public <NCmp, V> V putIfAbsent(
			int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			HashNodeFactory<? super NCmp, N> factory, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		N put = null ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			if (node == null) {
				
				// we have not encountered any prior partial or complete node matches, so simply insert the node at the end of the list
				if (put == null)
					put = factory.makeNode(hash, find) ;
				
				if (prev == null) {
										
					// prev is null, so list is entirely empty; attempt to add at head
					if (getTableUnsafe().compareAndSet(hash, null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return null ;
					}
					partial = false ;
					node = getTableUnsafe().writerGetFresh(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						if (!partial)
							uniquePrefixCounter.increment(hash) ;
						return null ;
					} 
					node = prev.getNextFresh() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) {
						
						if (put == null)
							put = factory.makeNode(hash, find) ;
						
						// at this point we have previously seen at least one partial match but have encountered no complete matches
						// so try to insert the new node here
						// { prev != null }
						put.lazySetNext(node) ;
						if (prev.casNext(node, put)) {
							inserted(put) ;
							totalCounter.increment(hash) ;
							return null ;
						}
						// if we fail, backtrack to prev2.next, or to the list head if prev2 is null
						if (prev2 == null) {
							partial = false ;
							prev2 = prev = null ;						
							node = getTableUnsafe().writerGetFresh(hash) ;
						} else {
							node = prev2.getNextFresh() ;
							prev = prev2 ;
							prev2 = null ;
							partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
						}
						continue ;
						
					} else {
						// we have not seen a partial match before this node, so simply set partial match to true						
						partial = true ;
					}
				}
				
				if (partial && eq.suffixMatch(find, node)) {
					// this node is a complete match so simply return it
					return ret.apply(node) ;
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
			}
		}
	}
	
	
	@Override
	public <NCmp, V> V ensureAndGet(
			int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			HashNodeFactory<? super NCmp, N> factory, 
			Function<? super N, ? extends V> ret) {
		grow() ;
		
		N put = null ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				// we have not encountered any prior partial or complete node matches, so simply insert the node at the end of the list
				if (put == null)
					put = factory.makeNode(hash, find) ;
				
				if (prev == null) {
					
					// prev is null, so list is entirely empty; attempt to add at head
					if (getTableUnsafe().compareAndSet(hash, null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return ret.apply(put) ;
					}
					partial = false ;
					node = getTableUnsafe().writerGetFresh(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						if (!partial)
							uniquePrefixCounter.increment(hash) ;
						return ret.apply(put) ;
					} 
					node = prev.getNextFresh() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) {						
						if (put == null)
							put = factory.makeNode(hash, find) ;
						
						// at this point we have previously seen at least one partial match but have encountered no complete matches
						// so try to insert the new node here
						// { prev != null }
						put.lazySetNext(node) ;
						if (prev.casNext(node, put)) {	
							totalCounter.increment(hash) ;
							return ret.apply(put) ;
						}
						// if we fail, backtrack to prev2.next, or to the list head if prev2 is null
						if (prev2 == null) {
							partial = false ;
							prev2 = prev = null ;						
							node = getTableUnsafe().writerGetFresh(hash) ;
						} else {
							node = prev2.getNextFresh() ;
							prev = prev2 ;
							prev2 = null ;
							partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
						}
						continue ;
						
					} else {
						// we have not seen a partial match before this node, so simply set partial match to true						
						partial = true ;
					}
				}
				
				if (partial && eq.suffixMatch(find, node)) {
					// this node is a complete match so simply return it
					return ret.apply(node) ;
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
			}
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
		
		final boolean expectOnlyOne = eq.isUnique() ;
		int c = 0 ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		boolean keptNeighbours = false ;
		
		while (true) {
			
			if (node == null) {
				
				if (!keptNeighbours && c > 0)
					uniquePrefixCounter.decrement(hash) ;
				return c ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				final boolean prevPartial = partial ;
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) {
						if (!keptNeighbours)
							uniquePrefixCounter.decrement(hash) ;
						return c ;
					} else {
						partial = true ;
					}
				}
				
				if (partial && eq.suffixMatch(find, node)) {
					
					// this node is a complete match AND we are replacing complete matches, so swap node for put					
					N next = node.getNextStale() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG | next == REHASHED_FLAG){
							if (next == REHASHING_FLAG | next == REHASHED_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								partial = false ;
								prev2 = prev = null ;
								node = getTableFresh().writerGetFresh(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									partial = false ;
									node = getTableUnsafe().writerGetFresh(hash) ;
								} else {
									node = prev.getNextStale() ;
									partial = prevPartial ; ;
								}
							}
							break ;
						}

						if (node.startDelete(next)) {
							
							boolean success ;
							if (prev == null) {
								success = getTableUnsafe().compareAndSet(hash, node, next) ;
								if (!success) {
									// failed to set head, so regrab head, undo what we've done, and return to outer loop
									partial = false ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = getTableUnsafe().writerGetFresh(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									partial = prevPartial ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextFresh() ;
								}
							}
							
							if (success) {
								node.finishDelete() ;
								waitingOnDelete.wake(node) ;
								removed(node) ;
								totalCounter.decrement(hash) ;
								if (expectOnlyOne | (++c == removeAtMost)) {
									if (!keptNeighbours && (next == null || hash != next.hash || !eq.prefixMatch(find, next)))
										uniquePrefixCounter.decrement(hash) ;
									return c ;
								}
								node = next ;
							}

							break ;
							
						} else {

							// failed to start delete so grab latest value of next and loop 
							next = node.getNextFresh() ;
						}
					}

				} else {
					
					if (partial)
						keptNeighbours = true ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextStale() ;
					
				}
			}
				
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
		
		final boolean expectOnlyOne = eq.isUnique() ;
		boolean doneFirst = false ;
		V r = null ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		boolean keptNeighbours = false ;
		int c = 0 ;
		
		while (true) {
			
			if (node == null) {
				
				if (!keptNeighbours & doneFirst)
					uniquePrefixCounter.decrement(hash) ;
				return r ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				final boolean prevPartial = partial ;
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) {
						if (!keptNeighbours)
							uniquePrefixCounter.decrement(hash) ;
						return r ;
					} else {
						partial = true ;
					}
				}
				
				if (partial && eq.suffixMatch(find, node)) {
					
					// this node is a complete match AND we are replacing complete matches, so swap node for put					
					N next = node.getNextStale() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG | next == REHASHED_FLAG){
							if (next == REHASHING_FLAG | next == REHASHED_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								partial = false ;
								prev2 = prev = null ;
								node = getTableFresh().writerGetFresh(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									partial = false ;
									node = getTableUnsafe().writerGetFresh(hash) ;
								} else {
									node = prev.getNextStale() ;
									partial = prevPartial ; ;
								}
							}
							break ;
						}
						
						if (node.startDelete(next)) {
							
							boolean success ;
							if (prev == null) {
								success = getTableUnsafe().compareAndSet(hash, node, next) ;
								if (!success) {
									// failed to set head, so regrab head, undo what we've done, and return to outer loop
									partial = false ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = getTableUnsafe().writerGetFresh(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									partial = prevPartial ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextFresh() ;
								}
							}
							
							if (success) {
								node.finishDelete() ;
								waitingOnDelete.wake(node) ;
								removed(node) ;
								totalCounter.decrement(hash) ;
								if (expectOnlyOne | (++c == removeAtMost)) {
									if (!keptNeighbours && (next == null || hash != next.hash || !eq.prefixMatch(find, next)))
										uniquePrefixCounter.decrement(hash) ;
									return ret.apply(node) ;
								}
								if (!doneFirst) {
									r = ret.apply(node) ;
									doneFirst = true ;
								}
								node = next ;
							}
							
							break ;
							
						} else {
							
							// failed to start delete so grab latest value of next and loop 
							next = node.getNextFresh() ;
						}						
					}
					
				} else {
					
					if (partial)
						keptNeighbours = true ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextStale() ;
					
				}
			}
			
		}
	}
	

	@Override
	public <NCmp, V> Iterable<V> removeAndReturn(
			int hash, int removeAtMost, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return Iters.emptyIterable() ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}
		
		int c = 0 ;
		final List<V> r = eq.isUnique() | removeAtMost == 1 ? null : new ArrayList<V>(4) ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		boolean keptNeighbours = false ;
		
		while (true) {
			
			if (node == null) {
				
				if (r == null || r.size() == 0)
					return Iters.emptyIterable() ;
				if (!keptNeighbours)
					uniquePrefixCounter.decrement(hash) ;
				return r ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				final boolean prevPartial = partial ;
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) {
						if (!keptNeighbours) // {: r.size() > 0 :}
							uniquePrefixCounter.decrement(hash) ;
						return r ;
					} else {
						partial = true ;
					}
				}
				
				if (partial && eq.suffixMatch(find, node)) {
					
					// this node is a complete match AND we are replacing complete matches, so swap node for put					
					N next = node.getNextStale() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG | next == REHASHED_FLAG){
							if (next == REHASHING_FLAG | next == REHASHED_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								partial = false ;
								prev2 = prev = null ;
								node = getTableFresh().writerGetFresh(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									partial = false ;
									node = getTableUnsafe().writerGetFresh(hash) ;
								} else {
									node = prev.getNextStale() ;
									partial = prevPartial ; ;
								}
							}
							break ;
						}
						
						if (node.startDelete(next)) {
							
							boolean success ;
							if (prev == null) {
								success = getTableUnsafe().compareAndSet(hash, node, next) ;
								if (!success) {
									// failed to set head, so regrab head, undo what we've done, and return to outer loop
									partial = false ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = getTableUnsafe().writerGetFresh(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									partial = prevPartial ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextFresh() ;
								}
							}
							
							if (success) {
								node.finishDelete() ;
								waitingOnDelete.wake(node) ;
								removed(node) ;
								totalCounter.decrement(hash) ;
								if (r == null | (++c == removeAtMost)) {
									if (!keptNeighbours && (next == null || hash != next.hash || !eq.prefixMatch(find, next)))
										uniquePrefixCounter.decrement(hash) ;
									return r == null ? new UniformList<V>(ret.apply(node), 1) : r ;
								}
								r.add(ret.apply(node)) ;
								node = next ;
							}
							
							break ;
							
						} else {
							
							// failed to start delete so grab latest value of next and loop 
							next = node.getNextFresh() ;
							
						}
						
					}
					
				} else {
					
					if (partial)
						keptNeighbours = true ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextStale() ;
					
				}
			}
			
		}
	}	
	
	@Override
	public <NCmp> boolean removeNode(
			Function<? super N, ? extends NCmp> nodePrefixEqFunc, 
			HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {

		final int hash = n.hash ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		
		while (true) {
			
			if (node == null) {
				
				return false ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					prev2 = prev = null ;
					node = getTableFresh().writerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
					}
				}
				
			} else {

				if (node == n) {

					// this node is a complete match AND we are replacing complete matches, so swap node for put					
					N next = node.getNextStale() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG | next == REHASHED_FLAG){
							if (next == REHASHING_FLAG | next == REHASHED_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								prev2 = prev = null ;
								node = getTableFresh().writerGetFresh(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									node = getTableUnsafe().writerGetFresh(hash) ;
								} else {
									node = prev.getNextStale() ;
								}
							}
							break ;
						}
						
						if (node.startDelete(next)) {
							
							boolean success ;
							if (prev == null) {
								success = getTableUnsafe().compareAndSet(hash, node, next) ;
								if (!success) {
									// failed to set head, so regrab head, undo what we've done, and return to outer loop
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = getTableUnsafe().writerGetFresh(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextFresh() ;
								}
							}
							
							if (success) {
								node.finishDelete() ;
								waitingOnDelete.wake(node) ;
								removed(node) ;
								totalCounter.decrement(hash) ;
								if ((prev == null || hash != prev.hash || !nodePrefixEq.prefixMatch(nodePrefixEqFunc.apply(n), next)) 
								 && (next == null || hash != next.hash || !nodePrefixEq.prefixMatch(nodePrefixEqFunc.apply(n), next)))
									uniquePrefixCounter.decrement(hash) ;
								return true ;
								
							}
							
							break ;

						} else {						
							
							// failed to start delete so grab latest value of next and loop 
							next = node.getNextFresh() ;
						}						
					}
					
				} else {
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextStale() ;
					
				}
			}
			
		}

	}

	
	
	// **********************************************************
	// METHODS TO QUERY
	// **********************************************************	
	
	
	
	@Override
	public <NCmp> boolean contains(final int hash, final NCmp find, 
			final HashNodeEquality<? super NCmp, ? super N> eq) {
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				return false ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().readerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().readerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
			} else {
				
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) return false ;
					else  partial = true ;
				}				
				if (partial && eq.suffixMatch(find, node)) {
					// this node is a complete match so simply return it
					return true ;
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
			}
		}
	}

	@Override
	public <NCmp> int count(int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq) {
		int c = 0 ;
		boolean countedLast = false ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				return c ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					c = 0 ;
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().readerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						c = 0 ;
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().readerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						if (countedLast)
							c-- ;
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) return c ;
					else  partial = true ;
				}				
				if (partial && eq.suffixMatch(find, node)) {
					// this node is a complete match so simply return it
					c++ ;
					countedLast = true ;
				} else {
					countedLast = false ;
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
			}
		}
	}

	@Override
	public <NCmp, V> V first(int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {		
				
				return null ;		
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().readerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().readerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) return null  ;
					else  partial = true ;
				}				
				if (partial && eq.suffixMatch(find, node)) {
					// this node is a complete match so simply return it
					return ret.apply(node) ;
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
			}
		}
	}

	@Override
	public <NCmp, V> List<V> findNow(int hash, NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> eq, 
			Function<? super N, ? extends V> ret) {
		final List<V> r = new ArrayList<V>(6) ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetStale(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				return r ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
				
				if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().readerGetFresh(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().readerGetFresh(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextFresh() ;
						prev = prev2 ;
						prev2 = null ;
						partial = prev.hash == hash && eq.prefixMatch(find, prev) ;
					}
				}
				
			} else {
				
				if (partial != (node.hash == hash && eq.prefixMatch(find, node))) {
					if (partial) return r  ;
					else  partial = true ;
				}				
				if (partial && eq.suffixMatch(find, node)) {
					// this node is a complete match so simply return it
					r.add(ret.apply(node)) ;
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextStale() ;
				
			}
		}
	}

	@Override
	public <NCmp, NCmp2, V> Iterator<V> find(
			int hash, 
			NCmp find, 
			HashNodeEquality<? super NCmp, ? super N> findEq,
			Function<? super N, ? extends NCmp2> nodeEqualityProj, 
			HashNodeEquality<? super NCmp2, ? super N> nodeEq,
			Function<? super N, ? extends V> ret) {
		return new Search<NCmp, NCmp2, V>(hash, find, findEq, nodeEqualityProj, nodeEq, ret) ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <NCmp, V> Iterator<V> all(
			Function<? super N, ? extends NCmp> nodeEqualityProj,
			HashNodeEquality<? super NCmp, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		return new EagerAllIterator<NCmp, V>(nodeEqualityProj, nodeEquality, NO_FILTER, ret) ;
	}

	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(
			Function<? super N, ? extends NCmp> uniquenessEqualityProj,
			Equality<? super NCmp> uniquenessEquality, 
			Locality duplicateLocality, 
			Function<? super N, ? extends NCmp2> nodeEqualityProj,
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
			Function<? super N, ? extends V> ret) {
		final Factory<Filter<N>> filterFactory ;
		filterFactory = HashStore.Helper.forUniqueness(uniquenessEqualityProj, uniquenessEquality, duplicateLocality) ;
		return new EagerAllIterator<NCmp2, V>(nodeEqualityProj, nodeEquality, filterFactory, ret) ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> ret) {
		while (true) {
			final Table<N> table = getTableFresh() ;
			if (table instanceof BlockingTable) {
				((BlockingTable<N>) table).waitForNext() ;
			} else {
				final BlockingTable<N> tmp = new BlockingTable<N>() ;
				if (casTable(table, tmp)) {
					final Table<N> newTable = new RegularTable<N>((N[]) new LockFreeHashNode[table.length()], (int) (loadFactor * table.length())) ;
					if (casTable(tmp, newTable)) {
						tmp.wake(newTable) ;
						if (table instanceof RegularTable) {
							return new DestroyingIterator(((RegularTable) table).table, ret) ;
						} else {
							ResizingTable gt = (ResizingTable) table ;
							gt.waiting.wakeAll() ;
							gt.rehash(0, false, false, true) ;
							return new DestroyingIterator(gt.newTable, ret) ;
						}
					}					
				} 
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <NCmp> HashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj, HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		final Iterator<N> iter = new EagerAllIterator<N, N>(null, null, NO_FILTER, Functions.<N>identity()) ;
		final LockFreeHashNode<N> head = (LockFreeHashNode<N>) new EmptyNode() ;
		N tail = (N) head ;
		boolean countUniq = uniquePrefixCounter.on() ;
		int tc = 0 ;
		int uc = 0 ;
		while (iter.hasNext()) {
			final N next = iter.next().copy() ;
			if (countUniq & tail != head && (tail.hash == next.hash && nodeEquality.prefixMatch(nodeEqualityProj.apply(next), tail)))
				uc++ ;
			tail = tail.nextPtr = next ;
			tc++ ;
		}
		int minCap = (int)(tc / loadFactor) ;
		int cap = 8 ;
		while (cap < minCap)
			cap <<= 1 ;
		final N[] table = (N[]) new LockFreeHashNode[cap] ;
		tail = head.nextPtr ;
		// due to using the EagerAllIterator to build our list we should have discrete batches of
		// nodes that all go into the same bucket already in the correct order, however multiple such buckets might need to 
		// go into any one target bucket. as such each distinct bucket batch will be inserted at the head of the target bucket
		// this maintains the ordering within a given key but permits us to avoid storing more than one bucket tail pointer
		N batchTail = null , batchHead = null ;
		int batchBucket = -1 ;
		final int mask = table.length - 1 ;
		if (tail != null) {
			batchHead = batchTail = tail ;
			batchBucket = tail.hash & mask ;
			tail = tail.nextPtr ;
			while (tail != null) {
				final N next = tail ;
				tail = tail.nextPtr ;
				final int nextBucket = next.hash & mask ;
				if (batchBucket != nextBucket) {
					batchTail.nextPtr = table[batchBucket] ;
					table[batchBucket] = batchHead ;
					batchHead = batchTail = next ;
					batchBucket = nextBucket ;
				} else {
					batchTail = batchTail.nextPtr = next ;
				}
			}
			batchTail.nextPtr = table[batchBucket] ;
			table[batchBucket] = batchHead ;
		}
		return new LockFreeHashStore<N>(loadFactor, totalCounter.newInstance(tc), uniquePrefixCounter.newInstance(uc), uniquePrefixCounter == growthCounter, table) ;
	}

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
	public int capacity() {
		return getTableFresh().length() ;
	}
	
	@Override
	public int totalCount() {
		return totalCounter.getSafe() ;
	}

	@Override
	public int uniquePrefixCount() {
		return uniquePrefixCounter.getSafe() ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void resize(int size) {
        int capacity = 8 ;
        while (capacity < size)
        	capacity <<= 1 ;
        while (true) {
			final Table<N> table = getTableFresh() ;
        	if (table.length() == capacity)
        		return ;
        	if (table instanceof RegularTable) {
				final BlockingTable<N> tmp = new BlockingTable<N>() ;
				if (casTable(table, tmp)) {
					final ResizingTable resizingTable ;
					if (capacity < table.length()) 
						resizingTable = new ShrinkingTable((RegularTable<N>) table, capacity) ;
					else if (capacity == table.length() << 1)
						resizingTable = new Growing2xTable((RegularTable<N>) table) ;
					else resizingTable = new Growing2ttpNxTable((RegularTable<N>) table, capacity) ;
					setTable(resizingTable) ;
					tmp.wake(resizingTable) ;
					resizingTable.rehash(0, false, true, false) ;
				}
        	} else if (table instanceof BlockingTable) {
        	} else {
        		((ResizingTable) table).waitUntilResized() ;
        	}
        }
	}

	@Override
	public void shrink() {
        final int totalCount = uniquePrefixCounter.getSafe() ;
        int capacity = 8 ;
        while ((capacity * loadFactor) < totalCount)
        	capacity <<= 1 ;
        if (capacity < getTableFresh().length())
        	resize(capacity) ;
	}

	private void waitOnDelete(final N node) {
		if (node.getNextFresh() != DELETING_FLAG)
			return ;
		WaitingOnNode<N> waiting = new WaitingOnNode<N>(Thread.currentThread(), node) ;
		waitingOnDelete.insert(waiting) ;
		while (node.getNextFresh() == DELETING_FLAG)
			LockSupport.park() ;
		waiting.remove() ;
	}
	
	@SuppressWarnings("unchecked")
	private void grow() {		
		while (growthCounter.getUnsafe() > getTableUnsafe().maxCapacity()) {
			final Table<N> table = getTableFresh() ;
			if (growthCounter.getSafe() <= table.maxCapacity())
				return ;
			if (table instanceof RegularTable) {
				final BlockingTable<N> tmp = new BlockingTable<N>() ;
				if (casTable(table, tmp)) {
					final Growing2xTable growingTable = new Growing2xTable((RegularTable<N>) table) ;
					setTable(growingTable) ;
					tmp.wake(growingTable) ;
					growingTable.rehash(0, false, true, false) ;
				}
			} else if (table instanceof BlockingTable) {
				((BlockingTable) table).waitForNext() ;
			} else {
				((ResizingTable) table).waitUntilResized() ;
			}
		}
	}
	

	


	// *****************************************
	// ITERATORS
	// *****************************************
	
	
	
	
	protected abstract class GeneralIterator<NCmp, V> implements Iterator<V> {

		final HashNodeEquality<? super NCmp, ? super N> nodeEquality ;
		final Function<? super N, ? extends NCmp> nodeEqualityProj ;
		final Function<? super N, ? extends V> ret ;
		N prevPred, prevNode, nextPred, nextNode ;

		public GeneralIterator(
				Function<? super N, ? extends NCmp> nodeEqualityProj,
				HashNodeEquality<? super NCmp, ? super N> nodeEquality,
				Function<? super N, ? extends V> ret) {
			this.nodeEquality = nodeEquality ;
			this.nodeEqualityProj = nodeEqualityProj ;
			this.ret = ret ;
		}

		@Override
		public boolean hasNext() {
			return nextNode != null ;
		}

		@Override
		public void remove() {
			if (prevNode == null)
				throw new NoSuchElementException() ;
			N next = prevNode.getNextStale() ;
			while (true) {				
				if (next == REHASHING_FLAG | next == DELETED_FLAG | next == DELETING_FLAG | next == REHASHED_FLAG) {
					if (next == REHASHING_FLAG | next == REHASHED_FLAG) {
						removeNode(nodeEqualityProj, nodeEquality, prevNode) ;
					}
					break ;
				}
				if (prevNode.startDelete(next)) {
					boolean success ;
					if (prevPred == null) {
						success = getTableUnsafe().compareAndSet(prevNode.hash, prevNode, next) ;
					} else {
						success = prevPred.casNext(prevNode, next) ;
					}					
					if (success) {
						prevNode.finishDelete() ;
						waitingOnDelete.wake(prevNode) ;
						removed(prevNode) ;
						totalCounter.decrement(prevNode.hash) ;
						if (uniquePrefixCounter.on()) {
							final NCmp cmp = nodeEqualityProj.apply(prevNode) ;
							if ((prevPred == null || prevNode.hash != prevPred.hash || !nodeEquality.prefixMatch(cmp, next)) 
							 && (next == null || prevNode.hash != next.hash || !nodeEquality.prefixMatch(cmp, next)))
								uniquePrefixCounter.decrement(prevNode.hash) ;
						}
					} else {
						prevNode.volatileSetNext(next) ;
						waitingOnDelete.wake(prevNode) ;
						removeNode(nodeEqualityProj, nodeEquality, prevNode) ;
					}
					break ;
				}
				next = prevNode.getNextFresh() ;
			}
			prevNode = null ;
			prevPred = null ;
		}
		
	}
	
	protected final class Search<NCmp, NCmp2, V> extends GeneralIterator<NCmp2, V> {

		final NCmp find ;
		final HashNodeEquality<? super NCmp, ? super N> findEquality ;
		final HashNodeVisits<N, NCmp2> visited ;
		
		public Search(int hash, NCmp find, 
				HashNodeEquality<? super NCmp, ? super N> findEquality, 
				Function<? super N, ? extends NCmp2> nodeEqualityProj, 
				HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret) ;
			this.find = find ;
			this.findEquality = findEquality ;
			this.visited = new HashNodeVisitSeq<N, NCmp2>(hash) ;
			moveNext(hash, null, null, true) ;
		}

		@Override
		public V next() {
			if (nextNode == null)
				throw new NoSuchElementException() ;
			prevPred = nextPred ;
			prevNode = nextNode ;
			visited.visit(prevNode) ;
			moveNext(prevNode.hash, prevNode, prevPred, false) ;
			return ret.apply(prevNode) ;
		}
		
		private void moveNext(final int hash, N node, N prev, boolean first) {
			final NCmp find = this.find ;
			boolean partial ;
			N prev2 = prev ;
			prev = node ;
			if (first) {
				partial = false ;
				node = getTableUnsafe().readerGetFresh(hash) ;
			} else {
				partial = true ;
				node = node.getNextStale() ;
			}
			while (node != null) {
				
				if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG | node == REHASHED_FLAG) {
					
					if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
						
						// this bucket is being rehashed, so we need to start from the head again, however we also have to ensure we skip over nodes we have already visited
						// as a result this inner loop performs the same work as the outer loop except with a membership check for visitedness...
						prev2 = prev = null ;
						node = getTableFresh().readerGetFresh(hash) ;
						partial = false ;
						this.visited.revisit() ;
						
						while (node != null) {
							
							if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG | node == REHASHED_FLAG) {
								
								if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
									// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
									prev2 = prev = null ;
									node = getTableFresh().readerGetFresh(hash) ;
								} else {
									// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
									waitOnDelete(prev) ;
									if (prev2 == null) {
										prev2 = prev = null ;						
										node = getTableUnsafe().readerGetFresh(hash) ;
									} else {
										node = prev2.getNextFresh() ;
										prev = prev2 ;
										prev2 = null ;
									}
								}
								
							} else {
								
								if (partial != (node.hash == hash && findEquality.prefixMatch(find, node))) {
									if (partial) {
										node = null ;
										break ;
									} else {
										partial = true ;
									}
								}
								
								if (partial && findEquality.suffixMatch(find, node)) {
									if (!this.visited.haveVisitedAlready(node, nodeEqualityProj, nodeEquality))
										break ;
								}
								
							}
							
							prev2 = prev ;
							prev = node ;
							node = node.getNextStale() ;
						}
						
						break ;
						
					} else {
						
						// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the most recent non-deleted visited node
						waitOnDelete(prev) ;
						if (prev2 == null) {
							// cannot backtrack to prev2, so find the last active visited node; if all previously visited nodes have been deleted then backtrack to start of list
							prev2 = prev = null ;
							while (!visited.isEmpty()) {
								prev = visited.last() ;
								node = prev.getNextFresh() ;
								if (node == DELETING_FLAG)
									waitOnDelete(node) ;
								if (node != DELETED_FLAG)
									break ;
								visited.removeLast() ;
							}
							if (visited.isEmpty()) {
								prev = null ;
								node = getTableUnsafe().readerGetFresh(hash) ;
							}
						} else {
							node = prev2.getNextFresh() ;
							prev = prev2 ;
							prev2 = null ;
						}
						
					}
					
				} else {

					if (partial != (node.hash == hash && findEquality.prefixMatch(find, node))) {
						if (partial) {
							node = null ;
							break ;
						} else {
							partial = true ;
						}
					}				
					if (partial && findEquality.suffixMatch(find, node))
						break ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextStale() ;
				}
			}
			
			this.nextPred = prev ;
			this.nextNode = node ;
		}
	
	}
	
	protected final class EagerAllIterator<NCmp, V> extends GeneralIterator<NCmp, V> {
		
		final HashIter32Bit indexIter ;
		Table<N> tableCache = getTableUnsafe() ;
		final List<N> list = new ArrayList<N>() ;
		final Factory<? extends Filter<? super N>>  filterFactory ;
		Iterator<N> current ;
		
		public EagerAllIterator(Function<? super N, ? extends NCmp> nodeEqualityProj, 
			HashNodeEquality<? super NCmp, ? super N> nodeEquality,
			Factory<? extends Filter<? super N>> filterFactory,
			Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret) ;
			tableCache = getTableUnsafe() ;
			this.filterFactory = filterFactory ;
			final int bits = Integer.bitCount(tableCache.length() - 1) ;
			indexIter = new HashIter32Bit(bits > 4 ? 4 : bits, bits) ;
			moveNext() ;
		}
		
		@Override
		public V next() {
			if (nextNode == null)
				throw new NoSuchElementException() ;
			prevPred = nextPred ;
			prevNode = nextNode ;
			moveNext() ;
			return ret.apply(prevNode) ;
		}
		
		private void moveNext() {
			if (current == null || !current.hasNext()) {
				
				final List<N> list = this.list ;
				Filter<? super N> filter = filterFactory.create() ;
				list.clear() ;
				N prev2 = null, prev = null ;
				N node ;
				if (current == null) 
					node = tableCache.writerGetStale(indexIter.current()) ;
				else node = null ;

				while (true) {
					
					if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG | node == REHASHED_FLAG | node == null) {
						
						if (node == DELETING_FLAG | node == DELETED_FLAG) {
							
							// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the most recent non-deleted visited node
							waitOnDelete(prev) ;
							if (prev2 == null) {
								
								prev2 = prev = null ;
								node = tableCache.writerGetFresh(indexIter.current()) ;

							} else {
								
								node = prev2.getNextFresh() ;
								prev = prev2 ;
								prev2 = null ;
								
							}
							
						} else if (node == null) {
							
							if (list.size() != 0) {
								current = list.iterator() ;
								this.nextPred = null ;
								this.nextNode = current.next() ;
								return ;
							}
							if (!indexIter.next()) {
								this.nextNode = null ;
								this.nextPred = null ;
								return ;
							}
							node = tableCache.writerGetStale(indexIter.current()) ;
							
						} else {
							
							// this bucket is being rehashed, so we need to start from the head again. since we visit an entire bucket in one go
							// we do not need to save previously visited nodes; the indexIter ensures we do not look at any hash that occured in 
							// a previously visited bucket
							list.clear() ;
							filter = filterFactory.create() ;
							tableCache = getTableFresh() ;
							indexIter.resize(Integer.bitCount(tableCache.length() - 1)) ;
							prev2 = prev = null ;
							node = tableCache.writerGetFresh(indexIter.current()) ;
							
						} 
						
					} else {
						
						// regular case: we have a non-null node to yield, so simply decide if we have seen it before or not...
						
						if (!indexIter.haveVisitedAlready(node.hash) && filter.accept(node)) {
							list.add(node) ;
						}
						
						prev2 = prev ;
						prev = node ;
						node = node.getNextStale() ;						
					}
				}
				
			} else {
				
				this.nextPred = this.nextNode ;
				this.nextNode = current.next() ;
				
			}
		}
		
	}
	
	protected final class DestroyingIterator<V> implements Iterator<V> {
		
		final Function<? super N, ? extends V> ret ;
		final N[] table ;
		int i = -1 ;
		Iterator<N> current ;
		
		public DestroyingIterator(N[] table, Function<? super N, ? extends V> ret) {
			this.table = table ;
			this.ret = ret ;
			nextIterator() ;
		}
		
		@SuppressWarnings("unchecked")
		private void nextIterator() {
			final List<N> list = new ArrayList<N>() ;
			N node = null ;
			
			while (true) {
				
				if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG | node == REHASHED_FLAG) {
					
					throw new IllegalStateException() ;
					
				} else if (node == null) {
					
					if (list.size() != 0) {
						
						current = list.iterator() ;
						break ;
						
					} else {
						
						i += 1 ;
						if (i >= table.length) {
							current = null ;
							break ;
						} else {
							node = table[i] ;
							while (!casNodeArray(table, i, node, REHASHED_FLAG)) {
								node = table[i] ;
							}
						}
						
					}
					
				} else {
					
					final N next = node.getNextFresh() ;
					if (next == DELETING_FLAG) {
						waitOnDelete(node) ;	
					} else if (next == DELETED_FLAG) {
						node = next ;
					} else if (node.oneStepDelete(next)) {
						list.add(node) ;
						node = next ;
					}
					
				}
			}
		}
		
		public boolean hasNext() {
			return current != null ;
		}
		
		public V next() {
			if (current == null)
				throw new NoSuchElementException() ;
			final V r = ret.apply(current.next()) ;
			if (!current.hasNext())
				nextIterator() ;
			return r ;
		}

		@Override
		public void remove() {
			// do nothing - already deleted
		}
		
	}
	
	
	
	
	// *****************************************
	// NODE DECLARATION
	// *****************************************
	
	
	
	public abstract static class LockFreeHashNode<N extends LockFreeHashNode<N>> extends HashNode<N> {
		private static final long serialVersionUID = -6236082606699747110L ;
		private N nextPtr ;
		public LockFreeHashNode(int hash) {
			super(hash) ;
		}
		public abstract N copy() ; 
		private final boolean startRehashing(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, REHASHING_FLAG) ;
		}
		private final boolean startDelete(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, DELETING_FLAG) ;
		}
		private final boolean oneStepDelete(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, DELETED_FLAG) ;
		}
		private final void finishDelete() {
			unsafe.putObjectVolatile(this, nextPtrOffset, DELETED_FLAG) ;
		}
		private final boolean casNext(N expect, N upd) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, upd) ;
		}
		private final N getNextStale() {
			return nextPtr ;
		}
		@SuppressWarnings("unchecked")
		private final N getNextFresh() {
			return (N) unsafe.getObjectVolatile(this, nextPtrOffset) ;
		}
		private final void lazySetNext(N upd) {
			unsafe.putOrderedObject(this, nextPtrOffset, upd) ;
		}
		private final void volatileSetNext(N upd) {
			unsafe.putObjectVolatile(this, nextPtrOffset, upd) ;
		}
		private static final long nextPtrOffset ;
		static {
			try {
				final Field field = LockFreeHashNode.class.getDeclaredField("nextPtr") ;
				nextPtrOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final class EmptyNode extends LockFreeHashNode {
		private static final long serialVersionUID = -8235849034699744602L ;
		public EmptyNode() {
			super(-1) ;
		}
		public EmptyNode copy() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	@SuppressWarnings("unchecked")
	// TODO: override serialization methods to throw an exception if they encounter a FlagNode; table can only be serialized when in a stable state
	private static final class FlagNode extends LockFreeHashNode {
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
	
	private static final FlagNode REHASHING_FLAG = new FlagNode("REHASHING") ;	
	private static final FlagNode REHASHED_FLAG = new FlagNode("REHASHING") ;	
	private static final FlagNode DELETED_FLAG = new FlagNode("DELETED") ;
	private static final FlagNode DELETING_FLAG = new FlagNode("DELETING") ;
	
	
	
	
	// *****************************************
	// UNDERLYING TABLE DEFINITIONS
	// *****************************************

	
	
	
	private static interface Table<N extends LockFreeHashNode<N>> {
		public N writerGetFresh(int hash) ;
		public N writerGetStale(int hash) ;
		public N readerGetFresh(int hash) ;
		public N readerGetStale(int hash) ;
		public boolean compareAndSet(int hash, N expect, N update) ;
		public int length() ;
		public int maxCapacity() ;
		public boolean isEmpty() ;
	}
	
	private static final class RegularTable<N extends LockFreeHashNode<N>> implements Table<N> {
		private final N[] table ;
		private final int mask ;
		private final int capacity ;
		public RegularTable(N[] table, int capacity) {
			this.table = table ;
			this.mask = table.length - 1 ;
			this.capacity = capacity ;
		}
		public final N writerGetFresh(int hash) {
			return getNodeVolatile(table, hash & mask) ;
		}
		public final N writerGetStale(int hash) {
			return table[hash & mask] ;
		}
		public final N readerGetFresh(int hash) {
			return getNodeVolatile(table, hash & mask) ;
		}
		public final N readerGetStale(int hash) {
			return table[hash & mask] ;
		}
		public final boolean compareAndSet(int hash, N expect, N update) {
			return unsafe.compareAndSwapObject(table, nodeArrayIndexBaseOffset + nodeArrayIndexScale * (hash & mask), expect, update) ;
		}
		public final int maxCapacity() {
			return capacity ;
		}
		public final int length() {
			return table.length ;
		}
		@Override
		public boolean isEmpty() {
			for (int i = 0 ; i != table.length ; i++) {
				final N head = table[i] ;
				if (head != null)
					return false ;
			}
			return true ;
		}
	}	
	
	@SuppressWarnings("unchecked")
	private static final class BlockingTable<N extends LockFreeHashNode<N>> implements Table<N> {
		private final ThreadQueue waiting = new ThreadQueue(null) ;
		private volatile Table<N> next = null ;
		void waitForNext() {
			waiting.insert(new ThreadQueue(Thread.currentThread())) ;
			while (next == null)
				LockSupport.park() ;
		}
		public N writerGetFresh(int hash) {
			waitForNext() ;
			return next.writerGetFresh(hash) ;
		}
		public N writerGetStale(int hash) {
			waitForNext() ;
			return next.writerGetStale(hash) ;
		}
		public N readerGetFresh(int hash) {
			waitForNext() ;
			return next.readerGetFresh(hash) ;
		}
		public N readerGetStale(int hash) {
			waitForNext() ;
			return next.readerGetStale(hash) ;
		}
		public boolean compareAndSet(int hash, N expect, N update) {
			waitForNext() ;
			return false ;
		}
		public int maxCapacity() {
			waitForNext() ;
			return next.maxCapacity() ;
		}
		public int length() {
			waitForNext() ;
			return next.length() ;
		}
		public void wake(Table<N> next) {
			this.next = next ;
			waiting.wakeAll() ;
		}
		@Override
		public boolean isEmpty() {
			waitForNext() ;
			return next.isEmpty() ;
		}
	}
	
	private abstract class ResizingTable implements Table<N> {
		final N[] oldTable ;
		final N[] newTable ;
		final int oldTableMask ;
		final int newTableMask ;
		final WaitingOnGrow waiting ;
		final GrowCompletion completion = new GrowCompletion() ;
		final int capacity ;
		@SuppressWarnings("unchecked")
		public ResizingTable(RegularTable<N> table, int newLength) {
			this.oldTable = table.table ;
			this.oldTableMask = oldTable.length - 1 ;
			this.newTable = (N[]) new LockFreeHashNode[newLength] ;
			this.newTableMask = newTable.length - 1 ;
			this.waiting = new WaitingOnGrow(null, -1) ;
			this.capacity = (int)(newTable.length * loadFactor) ;
		}
		public N readerGetFresh(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			N r = oldTable[oldTableIndex] ;
			if (r != REHASHING_FLAG & r != REHASHED_FLAG)
				r = getNodeVolatile(oldTable, oldTableIndex) ;
			if (r == REHASHING_FLAG | r == REHASHED_FLAG) {
				if (r == REHASHING_FLAG) {
					wait(oldTableIndex) ;
					return newTable[hash & newTableMask] ;
				} else {
					return getNodeVolatile(newTable, hash & newTableMask) ;
				}
			}
			return r ;
		}
		public N readerGetStale(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			final N r = oldTable[oldTableIndex] ;
			if (r == REHASHING_FLAG | r == REHASHED_FLAG) {
				if (r == REHASHING_FLAG)
					wait(oldTableIndex) ;
				return newTable[hash & newTableMask] ;
			}
			return r ;
		}
		public N writerGetFresh(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			if (oldTable[oldTableIndex] != REHASHED_FLAG) {
				rehash(oldTableIndex, true, false, false) ;
				return newTable[hash & newTableMask];
			}
			return getNodeVolatile(newTable, hash & newTableMask) ;
		}
		public N writerGetStale(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			if (oldTable[oldTableIndex] != REHASHED_FLAG)
				rehash(oldTableIndex, true, false, false) ;
			return newTable[hash & newTableMask];
		}
		void wait(int oldTableIndex) {
			final WaitingOnGrow queue = new WaitingOnGrow(Thread.currentThread(), oldTableIndex) ;
			waiting.insert(queue) ;
			while (getNodeVolatile(oldTable, oldTableIndex) != REHASHED_FLAG)
				LockSupport.park() ;
			queue.remove() ;
		}
		void waitUntilResized() {
			// small possibility somebody will get to here before the first grow() is called; this should only happen on small hash maps however
			if (LockFreeHashStore.this.getTableFresh() != this)
				return ;
			final WaitingOnGrow queue = new WaitingOnGrow(Thread.currentThread(), -1) ;
			waiting.insert(queue) ;
			while (LockFreeHashStore.this.getTableFresh() == this)
				LockSupport.park() ;
		}
		
		// compareAndSets don't need to be dealt with so strongly; if a thread is trying to update old and it is mid-grow, the cas will fail;
		// if it WAS trying to update old and it has been migrated, it will fail; so can assume that the value we obtain here is the one we were
		// looking for, as if not it will simply cause a retry/continue
		public boolean compareAndSet(int hash, N expect, N update) {
			final int oldTableIndex = hash & oldTableMask ;
			if (oldTable[oldTableIndex] == REHASHED_FLAG) {
				return casNodeArray(newTable, hash & newTableMask, expect, update) ;
			} else {
				return false ;
			}
		}
		void rehash(int from, boolean needThisIndex, boolean initiator, boolean tryAll) {
			if (!completion.startContributing())
				return ;
			boolean returnImmediatelyIfAlreadyHashing = !needThisIndex ;
			for (int i = from ; i != oldTable.length ; i++) {
				N head = startBucket(i, returnImmediatelyIfAlreadyHashing) ;
				if (head != null) {
					if (head == REHASHING_FLAG)
						head = null ;
					doBucket(head, i) ;
					finishBucket(i) ;
				} else if (!tryAll) {
					break ;
				}
				returnImmediatelyIfAlreadyHashing = true ;
			}				
			if (completion.finishContributing(initiator)) {
				// perform a CAS rather than a set because a clear() could have already removed this table before grow completion
				LockFreeHashStore.this.casTable(this, new RegularTable<N>(newTable, capacity)) ;
				waiting.wakeAll() ;
			}
		}
		@SuppressWarnings("unchecked")
		N startBucket(int oldTableIndex, boolean returnImmediatelyIfAlreadyRehashing) {
			N cur ;
			final long directOldTableIndex = directNodeArrayIndex(oldTableIndex) ;
			while (true) {
				cur = getNodeVolatileDirect(oldTable, directOldTableIndex) ;
				final boolean success = casNodeArrayDirect(oldTable, directOldTableIndex, cur, REHASHING_FLAG) ;
				if (cur == REHASHING_FLAG) {
					if (!returnImmediatelyIfAlreadyRehashing)
						wait(oldTableIndex) ;
					return null ;
				} 
				if (success) {
					if (cur == null)
						return (N) REHASHING_FLAG ; 
					return cur ;
				}
			}
		}
		@SuppressWarnings("unchecked")
		void finishBucket(int oldTableIndex) {
			lazySetNodeArray(oldTable, oldTableIndex, REHASHED_FLAG) ;
			// wake up waiters
			waiting.wake(oldTableIndex) ;
		}
		abstract void doBucket(N head, int oldTableIndex) ;
		@Override
		public int maxCapacity() {
			return capacity ;
		}
		@Override
		public int length() {
			return newTable.length ;
		}
		@Override
		public boolean isEmpty() {
			return false ;
		}
	}
	
	private final class Growing2xTable extends ResizingTable {
		public Growing2xTable(RegularTable<N> table) {
			super(table, table.table.length << 1) ;
		}
		@Override
		protected void doBucket(N cur, int oldTableIndex) {
			final int extrabit = oldTable.length ;
			N tail1 = null ;
			N tail2 = null ;
			
			boolean doGetNextSafely = false ;
			while (cur != null) {
				final N next = (doGetNextSafely ? cur.getNextFresh() : cur.getNextStale()) ;
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
						if (tail1 == null) {
							tail1 = cur.copy() ;
							lazySetNodeArray(newTable, oldTableIndex, tail1) ;
						} else {
							N t = cur.copy() ;
							tail1.lazySetNext(t) ;
							tail1 = t ;
						}
					} else {
						// goes in new bucket
						if (tail2 == null) {
							tail2 = cur.copy() ;
							lazySetNodeArray(newTable, oldTableIndex | extrabit, tail2) ;
						} else {
							N t = cur.copy() ;
							tail2.lazySetNext(t) ;
							tail2 = t ;
						}
					}
					
					cur = next ;
					doGetNextSafely = false ;
				} else {
					doGetNextSafely = true ;
				}
			}
		}
	}
	
	private final class Growing2ttpNxTable extends ResizingTable {
   		final int tailShift = Integer.bitCount(oldTableMask) ;
		@SuppressWarnings("unchecked")
   		final N[] tails = (N[]) new LockFreeHashNode[newTable.length >> tailShift] ;
		public Growing2ttpNxTable(RegularTable<N> table, int newLength) {
			super(table, newLength) ;
		}
		@Override
		protected void doBucket(N cur, int oldTableIndex) {
			final N[] tails = this.tails ;
			final int tailShift = this.tailShift ;
			Arrays.fill(tails, null) ;
			
			boolean doGetNextSafely = false ;
			while (cur != null) {
				final N next = (doGetNextSafely ? cur.getNextFresh() : cur.getNextStale()) ;
				if (next == DELETING_FLAG) {
					// cur cannot be actually deleted as CAS operations to the head will fail, and we have set prev's next to RETRY_FLAG,
					// as such the delete will be aborted by the deleting thread at which point we can continue; however add ourselves to the
					// waiting queue so as to not spin wastefully
					waitOnDelete(cur) ;
					doGetNextSafely = false ;
					continue ;
				}
				if (cur.startRehashing(next)) {
					
					final int newTableIndex = cur.hash & newTableMask ;
					final int tail = newTableIndex >> tailShift ;
					final N copy = cur.copy() ;
					if (tails[tail] == null) {
						lazySetNodeArray(newTable, newTableIndex, copy) ;
						tails[tail] = copy ;
					} else {
						tails[tail].lazySetNext(copy) ;
						tails[tail] = copy ;
					}
					
					cur = next ;
					doGetNextSafely = false ;
				} else {
					doGetNextSafely = true ;
				}
			}
		}
	}
	
	private final class ShrinkingTable extends ResizingTable {
		public ShrinkingTable(RegularTable<N> table, int newLength) {
			super(table, newLength) ;
		}
		@Override
		void doBucket(N head, int oldTableIndex) {
			final int newTableIndex = oldTableIndex & newTableMask ;
			final long newTableIndexDirect = directNodeArrayIndex(newTableIndex) ;
			N prev2 = null, prev = null, node = newTable[newTableIndex] ;
			while (true) {				
				if (node == null) {		
					
					if (prev == null) {						
						if (casNodeArrayDirect(newTable, newTableIndexDirect, null, head)) {
							return ;
						}
						node = getNodeVolatileDirect(newTable, newTableIndexDirect) ;
					} else {
						if (prev.casNext(null, head)) {
							return ;
						}
						node = prev.getNextFresh() ;
					}
					
				} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG | node == REHASHED_FLAG) {
					
					if (node == REHASHING_FLAG | node == REHASHED_FLAG) {
						throw new IllegalStateException() ;
					} else {
						// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
						waitOnDelete(prev) ;
						if (prev2 == null) {							
							prev = null ;						
							node = getNodeVolatileDirect(newTable, newTableIndexDirect) ;
						} else {
							node = prev2.getNextFresh() ;
							prev = prev2 ;
							prev2 = null ;
						}
					}
					
				} else {
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextStale() ;
					
				}
			}
		}
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
    private static final long nodeArrayIndexBaseOffset = unsafe.arrayBaseOffset(LockFreeHashNode[].class);
    private static final long nodeArrayIndexScale = unsafe.arrayIndexScale(LockFreeHashNode[].class);
	static {
		try {
			final Field field = LockFreeHashStore.class.getDeclaredField("tablePtr") ;
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
	private final Table<N> getTableFresh() {
		return (Table<N>) unsafe.getObjectVolatile(this, tablePtrOffset) ;
	}
	
	private final Table<N> getTableUnsafe() {
		return tablePtr ;
	}
	
	private static final <N extends LockFreeHashNode<N>> boolean casNodeArrayDirect(final N[] arr, final long i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, i, expect, upd) ;
	}	
	private static final <N extends LockFreeHashNode<N>> boolean casNodeArray(final N[] arr, final int i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), expect, upd) ;
	}	
	private static final <N extends LockFreeHashNode<N>> void lazySetNodeArray(final N[] arr, final int i, final N upd) {
		unsafe.putOrderedObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd) ;
	}
	private static final long directNodeArrayIndex(final int i) {
		return nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i) ;
	}
	@SuppressWarnings("unchecked")
	private static final <N extends LockFreeHashNode<N>> N getNodeVolatileDirect(final N[] arr, final long i) {
		return (N) unsafe.getObjectVolatile(arr, i) ;
	}
	@SuppressWarnings("unchecked")
	private static final <N extends LockFreeHashNode<N>> N getNodeVolatile(final N[] arr, final int i) {
		return (N) unsafe.getObjectVolatile(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i)) ;
	}

	public static Rehasher defaultRehasher() {
		return Rehashers.jdkHashmapRehasher() ;
	}

}
