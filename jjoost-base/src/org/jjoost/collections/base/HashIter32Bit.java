package org.jjoost.collections.base;

import java.util.Arrays;

public class HashIter32Bit {

	private final int lowBitsMask ;
	private int numTotalBits ;
	private int resizingTo ;
	private int highBitsCounter ;
	private int middleBitsCounter ;
	private int lowBitsCounter ;
	private int middleBitsIncrementor ;
	private ResizingFrom[] resizingFrom = null ;
	
	private static final class ResizingFrom {
		private final int highBitsForEqualLowBits ;
		private final int highBitsForLowerLowBits ;
		private final int lowBits ;
		public ResizingFrom(int highBitsForEqualLowBits,
				int highBitsForLowerLowBits, int lowBits) {
			this.highBitsForEqualLowBits = highBitsForEqualLowBits;
			this.highBitsForLowerLowBits = highBitsForLowerLowBits;
			this.lowBits = lowBits;
		}
	}
	
	private void pushResizingFrom() {
		if (resizingFrom == null) {
			resizingFrom = new ResizingFrom[] { getResizingFrom() } ;
		} else {
			resizingFrom = Arrays.copyOf(resizingFrom, resizingFrom.length + 1) ;
			resizingFrom[resizingFrom.length - 1] = getResizingFrom() ;
		}
	}
	
	private void cleanResizingFrom() {
		int removed = 0 ;
		for (int i = 0 ; i != resizingFrom.length ; i++) {
			if (highBitsCounter >= resizingFrom[i].highBitsForEqualLowBits) {
				resizingFrom[i] = null ;
				removed++ ;
			}
		}
		if (removed == resizingFrom.length) {
			resizingFrom = null ;
		} else if (removed != 0) {
			ResizingFrom[] copy = new ResizingFrom[resizingFrom.length - removed] ;
			int j = 0 ;
			for (ResizingFrom sf : copy) {
				if (sf != null) {
					copy[j++] = sf ;
				}
			}
			resizingFrom = copy ;
		}
	}
	
	private ResizingFrom getResizingFrom() {
		return new ResizingFrom(highBitsCounter + (middleBitsCounter >>> numTotalBits), highBitsCounter, lowBitsCounter) ;
	}
	
	public HashIter32Bit(int numLowerBits, int numTotalBits) {
		this.numTotalBits = numTotalBits ;		
		this.resizingTo = numTotalBits ;
		this.lowBitsMask = (1 << numLowerBits) - 1 ;
	}
	
	public void resize(int numTotalBits) {
		if (numTotalBits > this.numTotalBits) {
			resizingTo = numTotalBits ;
			middleBitsIncrementor = 1 << (32 - (numTotalBits - this.numTotalBits)) ;
			if ((middleBitsCounter & middleBitsIncrementor) != middleBitsCounter)
				pushResizingFrom() ;
			middleBitsCounter &= middleBitsIncrementor ;
		} else if (numTotalBits < this.numTotalBits) {
			final int newHighBitsCounter = highBitsCounter & ~((1 << (32 - numTotalBits)) - 1) ;
			if ((lowBitsCounter != 0) || (middleBitsCounter != 0) || highBitsCounter != newHighBitsCounter)
				pushResizingFrom() ;
			this.numTotalBits = numTotalBits ;
			resizingTo = numTotalBits ;
			middleBitsIncrementor = 0 ;
			middleBitsCounter = 0 ;
			highBitsCounter = newHighBitsCounter ;
			lowBitsCounter = 0 ;
		}
	}
	
	public boolean next() {
		middleBitsCounter = middleBitsCounter + middleBitsIncrementor ;
		if (middleBitsCounter == 0) {
			lowBitsCounter = (lowBitsCounter + 1) & lowBitsMask ;
			if (lowBitsCounter == 0) {
				highBitsCounter += 1 << (32 - numTotalBits) ;
				middleBitsIncrementor = 0 ;
				numTotalBits = resizingTo ;
				if (resizingFrom != null)
					cleanResizingFrom() ;
				if (highBitsCounter == 1 << (32 - Integer.bitCount(lowBitsMask)))
					return false ;
			}
		}
		return true ;
	}
	
	public boolean haveVisitedAlready(int hash) {
		final int highBits = Integer.reverse(hash & ~lowBitsMask) ;
		final int lowBits = hash & lowBitsMask ;
		if (lowBits == lowBitsCounter) {
			return highBitsCounter + (middleBitsCounter >>> numTotalBits) > highBits ;
		} else if (lowBits < lowBitsCounter) {
			return highBitsCounter + (1 << (32 - numTotalBits)) > highBits ;
		} else {
			if (resizingFrom != null) {
				for (ResizingFrom rf : resizingFrom) {
					if (lowBits < rf.lowBits) {
						return rf.highBitsForEqualLowBits > highBits ;
					} else if (lowBits == rf.lowBits) {
						return rf.highBitsForLowerLowBits > highBits ;
					}
				}
			}
			return highBitsCounter > highBits ;
		}
	}
	
	public int current() {
		return Integer.reverse(highBitsCounter + (middleBitsCounter >>> numTotalBits)) | lowBitsCounter ;
	}

}
