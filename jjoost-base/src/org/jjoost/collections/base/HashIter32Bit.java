package org.jjoost.collections.base;

import java.util.Arrays;

public class HashIter32Bit {

	private final int lowBitsMask ;
	private int numTotalBits ;
	private int resizingTo ;
	private int highBitsVisited ;
	private int highBitsCounter ;
	private int highBitsIncrementor ;
	private int lowBitsCounter ;
	
	/**
	 * saves the cursor position we were at prior to shrinking the number of hash bits used.
	 * records are inserted at the beginning, and are deleted as soon as we reach the same
	 * lowBitsCounter we were at when we began the shrink, after setting the highBitsVisited 
	 * to the highBitsForEqualLowBits value of the deleted record; thus resizingFrom is:
	 *     - sorted in ascending order of lowBits
	 *         (because when we insert a record all entries must be by definition above the current lowBitsCounter value)
	 *     - sorted in descending order of highBitsForLowerLowBits
	 *         (because we can only insert these records in the case of a shrink, and so for a record to already be present
	 *         it must have been inserted by a shrink still in progress which when instigated must have had a smaller highBitsIncrementor
	 *         and, as such, for all lowBits that had completed prior to the shrink, the value of highBitsCounter + highBitsIncrementor 
	 *         must be have been less than those completed since the shrink began)
	 *     - TODO: prove that is sorted in ascending order of highBitsForHigherLowBits, which some of the logic requires
	 */
	private ResizingFrom[] resizingFrom = null ;
	
	private static final class ResizingFrom {
		private int highBitsForEqualLowBits ;
		private int highBitsForLowerLowBits ;
		private int highBitsForHigherLowBits ;
		private int lowBits ;
		public ResizingFrom(int highBitsForEqualLowBits,
				int highBitsForLowerLowBits, int highBitsForHigherLowBits, int lowBits) {
			this.highBitsForEqualLowBits = highBitsForEqualLowBits;
			this.highBitsForLowerLowBits = highBitsForLowerLowBits;
			this.highBitsForHigherLowBits = highBitsForHigherLowBits;
			this.lowBits = lowBits;
		}
	}
	
	private void pushResizingFrom() {
		final ResizingFrom rf ;
		if (lowBitsCounter == 0) {
			rf = new ResizingFrom(highBitsCounter, -1, highBitsCounter, 1) ;
		} else {
			final int lowerLowBitsInc = (1 << (32 - numTotalBits)) ;
			final int highBitsForLowerLowBits = (highBitsCounter & ~(lowerLowBitsInc - 1)) + lowerLowBitsInc ;
			rf = new ResizingFrom(highBitsVisited, highBitsForLowerLowBits, highBitsCounter, lowBitsCounter) ;
		}
		if (resizingFrom == null) {
			resizingFrom = new ResizingFrom[] { rf } ;
		} else {
			final ResizingFrom[] rfs = new ResizingFrom[resizingFrom.length + 1] ;
			System.arraycopy(resizingFrom, 0, rfs, 1, resizingFrom.length) ;
			resizingFrom[0] = rf ;
		}
	}
	
	public HashIter32Bit(int numLowerBits, int numTotalBits) {
		this.numTotalBits = numTotalBits ;		
		this.resizingTo = numTotalBits ;
		this.lowBitsMask = (1 << numLowerBits) - 1 ;
		this.highBitsIncrementor = 1 << (32 - numTotalBits) ;
	}
	
	public void resize(int newTotalBits) {
		final int newHighBitsIncrementor = 1 << (32 - newTotalBits) ;
		// quite neatly we can use the highBitsVisited here to obtain the newHighBitsCounter in all possible resizing scenarios, despite their effects being quite different
		final int newHighBitsCounter = highBitsVisited & ~(newHighBitsIncrementor - 1) ;
		if (newTotalBits > this.numTotalBits) {
			resizingTo = newTotalBits ;
			highBitsIncrementor = newHighBitsIncrementor ;
			highBitsCounter = newHighBitsCounter ;
		} else if (newTotalBits < this.numTotalBits) {
			if (lowBitsCounter == 0) {
				// insert a resizing from record IFF we are losing information about how far we have traversed for future records
				if ((resizingFrom == null || resizingFrom[0].lowBits > 1) && (highBitsCounter & ~(newHighBitsIncrementor - 1)) < highBitsCounter)
					pushResizingFrom() ;
			} else {
				pushResizingFrom() ;
				lowBitsCounter = 0 ;
				highBitsVisited = newHighBitsCounter ;
			}
			numTotalBits = newTotalBits ;
			resizingTo = newTotalBits ;
			highBitsIncrementor = newHighBitsIncrementor ;
			highBitsCounter = newHighBitsCounter ;
		}
	}
	
	public boolean next() {
		final int highBitsMask = (1 << (32 - numTotalBits)) - 1 ;
		highBitsCounter = highBitsCounter + highBitsIncrementor ;
		if ((highBitsCounter & highBitsMask) == 0) {
			lowBitsCounter = (lowBitsCounter + 1) & lowBitsMask ;
			if (lowBitsCounter == 0) {				
				highBitsIncrementor = 1 << (32 - resizingTo) ;
				numTotalBits = resizingTo ;
				if (highBitsCounter == 1 << (32 - Integer.bitCount(lowBitsMask)))
					return false ;
			} else {
				highBitsCounter -= highBitsMask + 1;
			}
			highBitsVisited = highBitsCounter ;
			ResizingFrom rf ;
			if (resizingFrom != null && (rf = resizingFrom[0]).lowBits == lowBitsCounter) {
				if (highBitsVisited < rf.highBitsForEqualLowBits)
					highBitsVisited = rf.highBitsForEqualLowBits ;				
				if (rf.highBitsForHigherLowBits > highBitsCounter && (resizingFrom.length == 1 || resizingFrom[1].lowBits == rf.lowBits + 1)) {
					rf.lowBits++ ;
					rf.highBitsForLowerLowBits = -1 ;
					rf.highBitsForEqualLowBits = rf.highBitsForHigherLowBits ;
				} else {
					if (resizingFrom.length == 1) resizingFrom = null ;
					else resizingFrom = Arrays.copyOfRange(resizingFrom, 1, resizingFrom.length) ;
				}
			}
		} else {
			highBitsVisited = highBitsCounter ;
		}
		return true ;
	}
	
	public boolean visit(int hash) {
		final int highBits = Integer.reverse(hash & ~lowBitsMask) ;
		final int lowBits = hash & lowBitsMask ;
		if (lowBits == lowBitsCounter) {
			if (highBitsVisited > highBits)
				return false ;
			highBitsVisited = highBits ;
			return true ;
		} else {
			throw new IllegalArgumentException(String.format("The provided hash should not have occured at index %d of a " +
					"hash array of size %d, so this method is being used incorrectly, or there is a problem with the hash table", 
					current(), 1 << numTotalBits)) ;
		}
	}
	
	public boolean haveVisitedAlready(int hash) {
		final int highBits = Integer.reverse(hash & ~lowBitsMask) ;
		final int lowBits = hash & lowBitsMask ;
		if (lowBits < lowBitsCounter) {
			return highBitsCounter + (1 << (32 - numTotalBits)) > highBits ;
		} else if (lowBits == lowBitsCounter) {
			return highBitsVisited > highBits ;
		} else {
			if (highBitsCounter > highBits)
				return true ;
			if (resizingFrom != null) { 
				for (ResizingFrom rf : resizingFrom) {
					if (lowBits < rf.lowBits)
						return rf.highBitsForLowerLowBits > highBits ;
					else if (lowBits == rf.lowBits)
						return rf.highBitsForEqualLowBits > highBits ;
					else if (rf.highBitsForHigherLowBits > highBits)
						return true ;
				}
			}
			return false ;
		}
	}

	public int current() {
		return Integer.reverse(highBitsCounter) | lowBitsCounter ;
	}

}
