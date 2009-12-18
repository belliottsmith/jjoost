package org.jjoost.collections.base;

import java.io.Serializable ;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.LockSupport;

import org.jjoost.collections.lists.UniformList ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Function ;
import org.jjoost.util.Functions ;
import org.jjoost.util.Iters ;
import org.jjoost.util.concurrent.ThreadQueue ;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class LockFreeHashStore<N extends LockFreeHashStore.Node<N>> implements HashStore<N> {

	private static final long serialVersionUID = -1578733824843315344L ;
	
	public enum Counting {
		OFF, SAMPLED, PRECISE
	}

	private static final Unsafe unsafe = getUnsafe();
	
	private final WaitingOnDelete<N> waitingOnDelete = new WaitingOnDelete<N>(null, null) ;
	private final float loadFactor ;
	protected final Counter totalCounter ;
	protected final Counter uniquePrefixCounter ;
	private Table<N> tablePtr ;
	
	@SuppressWarnings("unchecked")
	public LockFreeHashStore(int initialCapacity, float loadFactor, Counting totalCounting, Counting uniquePrefixCounting) {
        int capacity = 1 ;
        while (capacity < initialCapacity)
        	capacity <<= 1 ;
        setTable(new RegularTable<N>((N[]) new Node[capacity], (int) (capacity * loadFactor))) ;
        this.loadFactor = loadFactor ;
        if (totalCounting == null || uniquePrefixCounting == null)
        	throw new IllegalArgumentException() ;
        switch (totalCounting) {
        case OFF: totalCounter = DONT_COUNT ; break ;
        case SAMPLED: totalCounter = new SampledCounter() ; break ;
        case PRECISE: totalCounter = new PreciseCounter() ; break ;
        default: throw new IllegalArgumentException() ;
        }
        switch (uniquePrefixCounting) {
        case OFF: uniquePrefixCounter = DONT_COUNT ; break ;
        case SAMPLED: uniquePrefixCounter = new SampledCounter() ; break ;
        case PRECISE: uniquePrefixCounter = new PreciseCounter() ; break ;
        default: throw new IllegalArgumentException() ;
        }
	}

	protected void inserted(N node) { }
	protected void removed(N node) { }


		
	// **********************************************************
	// METHODS FOR INSERTION
	// **********************************************************	
	
	
	
	@Override
	public <NCmp, V> V put(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		grow() ;
		
		final boolean replace = !eq.isUnique() ;		
		final int hash = put.hash ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
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
					node = getTableUnsafe().writerGetSafe(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return null ;
					} 
					node = prev.getNextSafe() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
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
							totalCounter.increment(hash) ;
							return null ;
						}
						// if we fail, backtrack to prev2.next, or to the list head if prev2 is null
						if (prev2 == null) {
							partial = false ;
							prev2 = prev = null ;						
							node = getTableUnsafe().writerGetSafe(hash) ;
						} else {
							node = prev2.getNextSafe() ;
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
					final N next = node.getNextUnsafe() ;
					if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG){
						if (next == REHASHING_FLAG) {
							// bucket is being rehashed, so start again
							partial = false ;
							prev2 = prev = null ;
							node = getTableFresh().writerGetSafe(hash) ;
						} else {
							// node is being deleted, so wait for it to complete and grab prev.next
							waitOnDelete(node) ;
							if (prev == null) {
								partial = false ;
								prev2 = prev = null ;
								node = getTableUnsafe().writerGetSafe(hash) ;
							} else {
								node = prev.getNextUnsafe() ;
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
						node = getTableUnsafe().writerGetSafe(hash) ;						
						continue ;
					} else {
						if (prev.casNext(next, put)) {
							inserted(put) ;
							removed(node) ;
							return ret.apply(node) ;
						}
						// we failed, so backtrack one
						node = prev.getNextUnsafe() ;
						partial = prevPartial ; ;
						continue ;
					}
					
				}
				
				prev2 = prev ;
				prev = node ;
				node = node.getNextUnsafe() ;
			}
		}
	}

	
	@Override
	public <NCmp, V> V putIfAbsent(NCmp find, N put, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		grow() ;
		
		final int hash = put.hash ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
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
					node = getTableUnsafe().writerGetSafe(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return null ;
					} 
					node = prev.getNextSafe() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
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
							totalCounter.increment(hash) ;
							return null ;
						}
						// if we fail, backtrack to prev2.next, or to the list head if prev2 is null
						if (prev2 == null) {
							partial = false ;
							prev2 = prev = null ;						
							node = getTableUnsafe().writerGetSafe(hash) ;
						} else {
							node = prev2.getNextSafe() ;
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
				node = node.getNextUnsafe() ;
			}
		}
	}
	
	@Override
	public <NCmp, V> V putIfAbsent(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) {
		grow() ;
		
		N put = null ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
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
					node = getTableUnsafe().writerGetSafe(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return null ;
					} 
					node = prev.getNextSafe() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
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
							return null ;
						}
						// if we fail, backtrack to prev2.next, or to the list head if prev2 is null
						if (prev2 == null) {
							partial = false ;
							prev2 = prev = null ;						
							node = getTableUnsafe().writerGetSafe(hash) ;
						} else {
							node = prev2.getNextSafe() ;
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
				node = node.getNextUnsafe() ;
			}
		}
	}
	
	
	@Override
	public <NCmp, V> V ensureAndGet(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, HashNodeFactory<? super NCmp, N> factory, Function<? super N, ? extends V> ret) {
		grow() ;
		
		N put = null ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
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
					node = getTableUnsafe().writerGetSafe(hash) ;
					
				} else {
					
					// otherwise prev was the last item in the list, so attempt to cas tail pointer from null to our new node
					if (prev.casNext(null, put)) {
						inserted(put) ;
						totalCounter.increment(hash) ;
						uniquePrefixCounter.increment(hash) ;
						return ret.apply(put) ;
					} 
					node = prev.getNextSafe() ;
				}
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
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
							node = getTableUnsafe().writerGetSafe(hash) ;
						} else {
							node = prev2.getNextSafe() ;
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
				node = node.getNextUnsafe() ;
			}
		}
	}

	
	
	
	// **********************************************************
	// METHODS FOR REMOVAL
	// **********************************************************	
	
	
	
	
	@Override
	public <NCmp> int remove(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return 0 ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}
		
		final boolean expectOnlyOne = eq.isUnique() ;
		int c = 0 ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		boolean partial = false ;
		boolean keptNeighbours = false ;
		
		while (true) {
			
			if (node == null) {
				
				if (!keptNeighbours && c > 0)
					uniquePrefixCounter.decrement(hash) ;
				return c ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
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
					N next = node.getNextUnsafe() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG){
							if (next == REHASHING_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								partial = false ;
								prev2 = prev = null ;
								node = getTableFresh().writerGetSafe(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									partial = false ;
									node = getTableUnsafe().writerGetSafe(hash) ;
								} else {
									node = prev.getNextUnsafe() ;
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
									node = getTableUnsafe().writerGetSafe(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									partial = prevPartial ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextSafe() ;
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
							next = node.getNextSafe() ;
						}
					}

				} else {
					
					if (partial)
						keptNeighbours = true ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextUnsafe() ;
					
				}
			}
				
		}
	}
	
	@Override
	public <NCmp, V> V removeAndReturnFirst(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return null ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}
		
		final boolean expectOnlyOne = eq.isUnique() ;
		boolean doneFirst = false ;
		V r = null ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		boolean partial = false ;
		boolean keptNeighbours = false ;
		int c = 0 ;
		
		while (true) {
			
			if (node == null) {
				
				if (!keptNeighbours & doneFirst)
					uniquePrefixCounter.decrement(hash) ;
				return r ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
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
					N next = node.getNextUnsafe() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG){
							if (next == REHASHING_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								partial = false ;
								prev2 = prev = null ;
								node = getTableFresh().writerGetSafe(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									partial = false ;
									node = getTableUnsafe().writerGetSafe(hash) ;
								} else {
									node = prev.getNextUnsafe() ;
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
									node = getTableUnsafe().writerGetSafe(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									partial = prevPartial ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextSafe() ;
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
							next = node.getNextSafe() ;
						}						
					}
					
				} else {
					
					if (partial)
						keptNeighbours = true ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextUnsafe() ;
					
				}
			}
			
		}
	}
	

	@Override
	public <NCmp, V> Iterable<V> removeAndReturn(int hash, int removeAtMost, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		if (removeAtMost < 1) {
			if (removeAtMost == 0)
				return Collections.emptyList() ;
			throw new IllegalArgumentException("Cannot remove less than zero elements") ;
		}
		
		int c = 0 ;
		final List<V> r = eq.isUnique() | removeAtMost == 1 ? null : new ArrayList<V>(4) ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		boolean partial = false ;
		boolean keptNeighbours = false ;
		
		while (true) {
			
			if (node == null) {
				
				if (!keptNeighbours && r.size() > 0)
					uniquePrefixCounter.decrement(hash) ;
				return r ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					partial = false ;
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						partial = false ;
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
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
					N next = node.getNextUnsafe() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG){
							if (next == REHASHING_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								partial = false ;
								prev2 = prev = null ;
								node = getTableFresh().writerGetSafe(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									partial = false ;
									node = getTableUnsafe().writerGetSafe(hash) ;
								} else {
									node = prev.getNextUnsafe() ;
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
									node = getTableUnsafe().writerGetSafe(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									partial = prevPartial ;
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextSafe() ;
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
							next = node.getNextSafe() ;
							
						}
						
					}
					
				} else {
					
					if (partial)
						keptNeighbours = true ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextUnsafe() ;
					
				}
			}
			
		}
	}	
	
	@Override
	public <NCmp> boolean removeNode(Function<? super N, ? extends NCmp> nodePrefixEqFunc, HashNodeEquality<? super NCmp, ? super N> nodePrefixEq, N n) {

		final int hash = n.hash ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		
		while (true) {
			
			if (node == null) {
				
				return false ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
					// this bucket is being rehashed, so simply start from the head again (this will block until this bucket has been grown)
					prev2 = prev = null ;
					node = getTableFresh().writerGetSafe(hash) ;				
				} else {
					// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the list head
					waitOnDelete(prev) ;
					if (prev2 == null) {
						prev2 = prev = null ;						
						node = getTableUnsafe().writerGetSafe(hash) ; // note: could improve by performing a get *un*safe, as we have passed a memory barrier already with the waitOnDelete
					} else {
						node = prev2.getNextSafe() ;
						prev = prev2 ;
						prev2 = null ;
					}
				}
				
			} else {

				if (node == n) {

					// this node is a complete match AND we are replacing complete matches, so swap node for put					
					N next = node.getNextUnsafe() ;
					while (true) {
						
						if (next == REHASHING_FLAG | next == DELETING_FLAG | next == DELETED_FLAG){
							if (next == REHASHING_FLAG) {
								// bucket is being rehashed, so start from the beginning of the outer loop
								prev2 = prev = null ;
								node = getTableFresh().writerGetSafe(hash) ;
							} else {
								// node is being deleted, so wait for it to complete, grab prev.next and return to outer loop
								waitOnDelete(node) ;
								if (prev == null) {
									node = getTableUnsafe().writerGetSafe(hash) ;
								} else {
									node = prev.getNextUnsafe() ;
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
									node = getTableUnsafe().writerGetSafe(hash) ;
								}
							} else {
								success = prev.casNext(node, next) ;
								if (!success) {
									// failed to set prev's next to node, so step back to prev.next and break to outer loop
									node.volatileSetNext(next) ;
									waitingOnDelete.wake(node) ;
									node = prev.getNextSafe() ;
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
							next = node.getNextSafe() ;
						}						
					}
					
				} else {
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextUnsafe() ;
					
				}
			}
			
		}

	}

	
	
	// **********************************************************
	// METHODS TO QUERY
	// **********************************************************	
	
	
	
	@Override
	public <NCmp> boolean contains(final int hash, final NCmp find, final HashNodeEquality<? super NCmp, ? super N> eq) {
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				return false ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				if (node == REHASHING_FLAG) {
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
						node = prev2.getNextSafe() ;
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
				node = node.getNextUnsafe() ;
			}
		}
	}

	@Override
	public <NCmp> int count(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq) {
		int c = 0 ;
		boolean countedLast = false ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				return c ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
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
						node = prev2.getNextSafe() ;
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
				node = node.getNextUnsafe() ;
			}
		}
	}

	@Override
	public <NCmp, V> V first(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {		
				
				return null ;		
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
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
						node = prev2.getNextSafe() ;
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
				node = node.getNextUnsafe() ;
			}
		}
	}

	@Override
	public <NCmp, V> List<V> findNow(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> eq, Function<? super N, ? extends V> ret) {
		final List<V> r = new ArrayList<V>(6) ;
		N prev = null, prev2 = null ;
		N node = getTableUnsafe().writerGetUnsafe(hash) ;
		boolean partial = false ;
		while (true) {
			
			if (node == null) {
				
				return r ;
				
			} else if (node == REHASHING_FLAG | node == DELETING_FLAG | node == DELETED_FLAG) {
				
				if (node == REHASHING_FLAG) {
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
						node = prev2.getNextSafe() ;
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
				node = node.getNextUnsafe() ;
				
			}
		}
	}

	@Override
	public <NCmp, NCmp2, V> Iterator<V> find(int hash, NCmp find, HashNodeEquality<? super NCmp, ? super N> findEq,
			Function<? super N, ? extends NCmp2> nodeEqualityProj, HashNodeEquality<? super NCmp2, ? super N> nodeEq,
			Function<? super N, ? extends V> ret) {
		return new Search<NCmp, NCmp2, V>(hash, find, findEq, nodeEqualityProj, nodeEq, ret) ;
	}

	@Override
	public <NCmp, V> Iterator<V> all(Function<? super N, ? extends NCmp> nodeEqualityProj,
			HashNodeEquality<? super NCmp, ? super N> nodeEquality, Function<? super N, ? extends V> ret) {
		return new LazyAllIterator<NCmp, V>(nodeEqualityProj, nodeEquality, ret) ;
	}

	@Override
	public <NCmp, NCmp2, V> Iterator<V> unique(Function<? super N, ? extends NCmp> uniquenessEqualityProj,
			Equality<? super NCmp> uniquenessEquality, Function<? super N, ? extends NCmp2> nodeEqualityProj,
			HashNodeEquality<? super NCmp2, ? super N> nodeEquality, Function<? super N, ? extends V> ret) {
		// TODO Auto-generated method stub
		return null ;
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
					final Table<N> newTable = new RegularTable<N>((N[]) new Node[table.capacity()], table.capacity()) ;
					if (casTable(tmp, newTable)) {
						tmp.wake(newTable) ;
						if (table instanceof RegularTable) {
							return new DestroyingIterator(((RegularTable) table).table, ret) ;
						} else {
							GrowingTable gt = (GrowingTable) table ;
							gt.waiting.wakeAll() ;
							gt.grow(0, false, false, true) ;
							return new DestroyingIterator(gt.newTable, ret) ;
						}
					}					
				} 
			}
		}
	}

	@Override
	public HashStore<N> copy() {
		// TODO Auto-generated method stub
		return null ;
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
	public int totalCount() {
		return totalCounter.getSafe() ;
	}

	@Override
	public int uniquePrefixCount() {
		return uniquePrefixCounter.getSafe() ;
	}

	@Override
	public void resize(int size) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void shrink() {
		throw new UnsupportedOperationException() ;
	}

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
	private void grow() {		
		while (totalCounter.getUnsafe() > getTableUnsafe().capacity()) {
			final Table<N> table = getTableFresh() ;
			if (totalCounter.getSafe() <= table.capacity())
				return ;
			if (table instanceof RegularTable) {
				final BlockingTable<N> tmp = new BlockingTable<N>() ;
				if (casTable(table, tmp)) {
					final GrowingTable growingTable = new GrowingTable((RegularTable<N>) table) ;
					setTable(growingTable) ;
					tmp.wake(growingTable) ;
					growingTable.grow(0, false, true, false) ;
				}
			} else if (table instanceof BlockingTable) {
				((BlockingTable) table).waitForNext() ;
			} else {
				((GrowingTable) table).waitUntilGrown() ;
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
			N next = prevNode.getNextUnsafe() ;
			while (true) {				
				if (next == REHASHING_FLAG | next == DELETED_FLAG | next == DELETING_FLAG) {
					if (next == REHASHING_FLAG) {
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
				next = prevNode.getNextSafe() ;
			}
			prevNode = null ;
			prevPred = null ;
		}
		
	}
	
	protected final class Search<NCmp, NCmp2, V> extends GeneralIterator<NCmp2, V> {

		final NCmp find ;
		final HashNodeEquality<? super NCmp, ? super N> findEquality ;
		final HashNodeSeq<N, NCmp2> visited ;
		
		public Search(int hash, NCmp find, 
				HashNodeEquality<? super NCmp, ? super N> findEquality, 
				Function<? super N, ? extends NCmp2> nodeEqualityProj, 
				HashNodeEquality<? super NCmp2, ? super N> nodeEquality, 
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret) ;
			this.find = find ;
			this.findEquality = findEquality ;
			this.visited = new HashNodeSeq<N, NCmp2>(hash, nodeEqualityProj, nodeEquality) ;
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
				node.getNextUnsafe() ;
			}
			while (node != null) {
				
				if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG) {
					
					if (node == REHASHING_FLAG) {
						
						// this bucket is being rehashed, so we need to start from the head again, however we also have to ensure we skip over nodes we have already visited
						// as a result this inner loop performs the same work as the outer loop except with a membership check for visitedness...
						prev2 = prev = null ;
						node = getTableFresh().readerGetFresh(hash) ;
						partial = false ;
						this.visited.revisit() ;
						
						while (node != null) {
							
							if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG) {
								
								if (node == REHASHING_FLAG) {
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
										node = prev2.getNextSafe() ;
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
									if (!this.visited.haveVisitedAlready(node))
										break ;
								}
								
							}
							
							prev2 = prev ;
							prev = node ;
							node = node.getNextUnsafe() ;
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
								node = prev.getNextSafe() ;
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
							node = prev2.getNextSafe() ;
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
					node = node.getNextUnsafe() ;
				}
			}
			
			this.nextPred = prev ;
			this.nextNode = node ;
		}
	
	}
	
	protected final class LazyAllIterator<NCmp, V> extends GeneralIterator<NCmp, V> {

		Table<N> tableCache = getTableUnsafe() ;
		final HashIter32Bit indexIter ;
		final HashNodeSeqs<N, NCmp> bucketVisited ;
		HashNodeSeq<N, NCmp> curVisited ;
		
		public LazyAllIterator(Function<? super N, ? extends NCmp> nodeEqualityProj, 
				HashNodeEquality<? super NCmp, ? super N> nodeEquality, 
				Function<? super N, ? extends V> ret) {
			super(nodeEqualityProj, nodeEquality, ret) ;
			tableCache = getTableUnsafe() ;
			final int bits = Integer.bitCount(tableCache.capacity() - 1) ;
			indexIter = new HashIter32Bit(bits > 4 ? 4 : 1, bits) ;
			bucketVisited = new HashNodeSeqs<N, NCmp>(nodeEqualityProj, nodeEquality) ; 
			moveNext(null, null) ;
		}
		
		@Override
		public V next() {
			if (nextNode == null)
				throw new NoSuchElementException() ;
			prevPred = nextPred ;
			prevNode = nextNode ;
			curVisited.visit(prevNode) ;
			moveNext(nextNode, nextPred) ;
			return ret.apply(prevNode) ;
		}
		
		private void moveNext(N node, N prev) {
			
			N prev2 = prev ;
			prev = node ;
			if (node == null) {
				node = tableCache.readerGetFresh(indexIter.current()) ;
			} else {
				node.getNextUnsafe() ;
			}
			
			while (true) {
				
				if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG) {
					
					if (node == REHASHING_FLAG) {
						
						// this bucket is being rehashed, so we need to start from the head again, however we also have to ensure we skip over nodes we have already visited
						// as a result this inner loop performs the same work as the outer loop except with a membership check for visitedness...
						tableCache = getTableFresh() ;
						indexIter.resize(Integer.bitCount(tableCache.capacity() - 1)) ;
						prev2 = prev = null ;
						node = tableCache.readerGetFresh(indexIter.current()) ;
						bucketVisited.revisitAll() ;
						curVisited = null ;
						
					} else {
						
						// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the most recent non-deleted visited node
						waitOnDelete(prev) ;
						if (prev2 == null) {
							// cannot backtrack to prev2, so find the last active visited node; if all previously visited nodes have been deleted then backtrack to start of list
							if (!indexIter.safe()) {
								
								prev2 = prev = null ;
								node = tableCache.readerGetFresh(indexIter.current()) ;
								curVisited = null ;
								
							} else {
								
								prev2 = prev = node = null ;
								while (true) {
									while (!curVisited.isEmpty()) {
										prev = curVisited.last() ;
										node = prev.getNextSafe() ;
										if (node == DELETING_FLAG)
											waitOnDelete(node) ;
										if (node != DELETED_FLAG)
											break ;
										curVisited.removeLast() ;
										node = null ;
									}
									if (!curVisited.isEmpty())
										break ;								
									if (bucketVisited.isEmpty()) {
										prev = null ;
										node = tableCache.readerGetFresh(indexIter.current()) ;
										break ;
									}								
									curVisited = bucketVisited.pop() ;
								}
								
							}							
							
						} else {
							
							node = prev2.getNextSafe() ;
							prev = prev2 ;
							prev2 = null ;
							
						}
						
					}
					
				} else if (node == null) {
					
					if (!indexIter.next())
						break ;
					node = tableCache.readerGetStale(indexIter.current()) ;
					bucketVisited.clear() ;
					curVisited = null ;
					
				} else {
					
					if (curVisited == null || curVisited.hash != node.hash)
						curVisited = bucketVisited.get(node) ;
					if (!(indexIter.haveVisitedAlready(node.hash) || curVisited.haveVisitedAlready(node)))
						break ;
					
					prev2 = prev ;
					prev = node ;
					node = node.getNextUnsafe() ;
					
				}
			}
			
			this.nextPred = prev ;
			this.nextNode = node ;
		}
		
	}
	
	
	protected final class EagerAllIterator {
		
		final HashIter32Bit indexIter ;
		Table<N> tableCache = getTableUnsafe() ;
		Iterator<N> current ;
		
		public EagerAllIterator() {
			tableCache = getTableUnsafe() ;
			final int bits = Integer.bitCount(tableCache.capacity() - 1) ;
			indexIter = new HashIter32Bit(bits > 4 ? 4 : 1, bits) ;
		}
		
		public N next() {
			if (current == null | !current.hasNext()) {
				
				final List<N> list = new ArrayList<N>() ;
				N prev2 = null, prev = null ;
				N node = null ;
				
				while (true) {
					
					if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG) {
						
						if (node == REHASHING_FLAG) {
							
							// this bucket is being rehashed, so we need to start from the head again. since we visit an entire bucket in one go
							// we do not need to save previously visited nodes; the indexIter ensures we do not look at any hash that occured in 
							// a previously visited bucket
							list.clear() ;
							tableCache = getTableFresh() ;
							indexIter.resize(Integer.bitCount(tableCache.capacity() - 1)) ;
							prev2 = prev = null ;
							node = tableCache.readerGetFresh(indexIter.current()) ;
							
						} else {
							
							// prev has been or is being deleted, so wait for deletion to complete and then backtrack either to prev2 or to the most recent non-deleted visited node
							waitOnDelete(prev) ;
							if (prev2 == null) {
								
								prev2 = prev = null ;
								node = tableCache.readerGetFresh(indexIter.current()) ;

							} else {
								
								node = prev2.getNextSafe() ;
								prev = prev2 ;
								prev2 = null ;
								
							}
							
						}
						
					} else if (node == null) {
						
						if (list.size() != 0) {
							current = list.iterator() ;
							return current.next() ;
						}
						if (!indexIter.next()) {
							return null ;
						}						
						node = tableCache.readerGetStale(indexIter.current()) ;
						
					} else {
						
						if (!indexIter.haveVisitedAlready(node.hash)) {
							list.add(node) ;
						}
						
						prev2 = prev ;
						prev = node ;
						node = node.getNextUnsafe() ;						
					}
				}
			} else {
				
				return current.next() ;
				
			}
		}
		
	}
	
	protected final class DestroyingIterator<V> implements Iterator<V> {
		
		final Function<? super N, ? extends V> ret ;
		final N[] table ;
		int i = 0 ;
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
				
				if (node == REHASHING_FLAG | node == DELETED_FLAG | node == DELETING_FLAG) {
					
					throw new IllegalStateException() ;
					
				} else if (node == null) {
					
					if (list.size() != 0) {
						current = list.iterator() ;
					}
					if (i == table.length) {
						current = null ;
					}
					i += 1 ;
					node = table[i] ;
					while (!casNodeArray(table, i, node, REHASHING_FLAG)) {
						node = table[i] ;
					}
					
				} else {
					
					final N next = node.getNextSafe() ;
					if (next == DELETING_FLAG) {
						waitOnDelete(node) ;							
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
			return ret.apply(current.next()) ;
		}

		@Override
		public void remove() {
			// do nothing - already deleted
		}
		
	}
	
	
	
	
	
	// *****************************************
	// NODE DECLARATION
	// *****************************************
	
	
	
	
	public abstract static class Node<N extends Node<N>> extends HashNode<N> {
		private static final long serialVersionUID = -6236082606699747110L ;
		private N nextPtr ;
		public Node(int hash) {
			super(hash) ;
		}
		public abstract N copy() ; 
		final boolean startRehashing(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, REHASHING_FLAG) ;
		}
		final boolean startDelete(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, DELETING_FLAG) ;
		}
		final boolean oneStepDelete(N expect) {
			return unsafe.compareAndSwapObject(this, nextPtrOffset, expect, DELETED_FLAG) ;
		}
		final void finishDelete() {
			unsafe.putObjectVolatile(this, nextPtrOffset, DELETED_FLAG) ;
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
		final void volatileSetNext(N upd) {
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
	
	@SuppressWarnings("unchecked")
	// TODO: override serialization methods to ensure they are deserialized into the global REHASHING_FLAG etc.
	private static final class FlagNode extends Node {
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
	private static final FlagNode DELETED_FLAG = new FlagNode("DELETED") ;
	private static final FlagNode DELETING_FLAG = new FlagNode("DELETING") ;
	
	
	
	
	// *****************************************
	// UNDERLYING TABLE DEFINITIONS
	// *****************************************

	
	
	
	private static interface Table<N extends Node<N>> {
		public N writerGetSafe(int hash) ;
		public N writerGetUnsafe(int hash) ;
		public N readerGetFresh(int hash) ;
		public N readerGetStale(int hash) ;
		public boolean compareAndSet(int hash, N expect, N update) ;
		public int capacity() ;
		public boolean isEmpty() ;
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
		public final N writerGetSafe(int hash) {
			return getNodeVolatile(table, hash & mask) ;
		}
		public final N writerGetUnsafe(int hash) {
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
		public final int capacity() {
			return capacity ;
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
	private static final class BlockingTable<N extends Node<N>> implements Table<N> {
		private final ThreadQueue waiting = new ThreadQueue(null) ;
		private volatile Table<N> next = null ;
		void waitForNext() {
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
		public int capacity() {
			waitForNext() ;
			return next.capacity() ;
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
		public N readerGetFresh(int hash) {
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
		public N readerGetStale(int hash) {
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
				grow(oldTableIndex, true, false, false) ;
				return getNodeVolatile(newTable, hash & newTableMask) ;
			}
			return getNodeVolatile(newTable, hash & newTableMask) ;
		}
		public N writerGetUnsafe(int hash) {
			final int oldTableIndex = hash & oldTableMask ;
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			if ((migrated[migratedIndex] & migratedBit) == 0) {
				grow(oldTableIndex, true, false, false) ;
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
			final int migratedIndex = oldTableIndex >> 5 ;
			final int migratedBit = 1 << (oldTableIndex & 31) ;
			if ((migrated[migratedIndex] & migratedBit) == 0) {
				return false ;
			} else {
				return casNodeArray(newTable, hash & newTableMask, expect, update) ;
			}
		}
		public void grow(int from, boolean needThisIndex, boolean initiator, boolean tryAll) {
			if (!completion.startContributing())
				return ;
			for (int i = from ; i != oldTable.length ; i++)
				if (!rehash(i, !(needThisIndex & (from == i))) & !tryAll)
					break ;
			if (completion.finishContributing(initiator)) {
				// perform a CAS rather than a set because a clear() could have already removed this table before grow completion
				LockFreeHashStore.this.casTable(this, new RegularTable<N>(newTable, capacity)) ;
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
		@Override
		public boolean isEmpty() {
			return false ;
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
					final WaitingOnDelete<N> prev = next ;
					next = next.next ;
					prev.wake() ;
				} else {
					next = next.next ;
				}
			}
		}
		protected void remove() { super.remove() ; } 
	}

	
	
	
	// *************************************
	// COUNTER DECLARATIONS
	// *************************************
	
	
	
	
	private static interface Counter extends Serializable {
		public int getSafe() ;
		public int getUnsafe() ;
		public void increment(int hash) ;
		public void decrement(int hash) ;
		public boolean on() ;
	}
	private static final class PreciseCounter implements Counter {
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
	}
	private static final class SampledCounter implements Counter {
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
			if ((hash + System.nanoTime() & 15) != 0)
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
			if ((hash + System.nanoTime() & 15) != 0)
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
		static {
			try {
				final Field field = SampledCounter.class.getDeclaredField("count") ;
				countOffset = unsafe.objectFieldOffset(field) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	private static final DontCount DONT_COUNT = new DontCount() ;
	private static final class DontCount implements Counter {
		private static final long serialVersionUID = 1633916421321597636L ;
		public final int getSafe() { return Integer.MIN_VALUE ; }
		public final int getUnsafe() { return Integer.MIN_VALUE ; }
		public void increment(int hash) { }
		public void decrement(int hash) { }
		public boolean on() { return false ; }
	}
	
	
	
	
	// *************************************
	// "UNSAFE" OPERATIONS
	// *************************************
	
	
	
	
	private static final long tablePtrOffset ;
    private static final long nodeArrayIndexBaseOffset = unsafe.arrayBaseOffset(Node[].class);
    private static final long nodeArrayIndexScale = unsafe.arrayIndexScale(Node[].class);
    private static final long intArrayIndexBaseOffset = unsafe.arrayBaseOffset(int[].class);
    private static final long intArrayIndexScale = unsafe.arrayIndexScale(int[].class);
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
	
	private static final <N extends Node<N>> boolean casNodeArrayDirect(final N[] arr, final long i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, i, expect, upd) ;
	}	
	private static final <N extends Node<N>> boolean casNodeArray(final N[] arr, final int i, final N expect, final N upd) {
		return unsafe.compareAndSwapObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), expect, upd) ;
	}	
	private static final <N extends Node<N>> void lazySetNodeArray(final N[] arr, final int i, final N upd) {
		unsafe.putOrderedObject(arr, nodeArrayIndexBaseOffset + (nodeArrayIndexScale * i), upd) ;
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
