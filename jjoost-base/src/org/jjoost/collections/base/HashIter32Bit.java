package org.jjoost.collections.base;

public class HashIter32Bit {

	private final int lowBitsMask ;
	private int numTotalBits ;
	private int resizingTo ;
	private int highBitsCounter ;
	private int lowBitsCounter ;	
	private int middleBitsCounter ;
	private int middleBitsIncrementor ;
	private boolean unsafe = false ;
	
	public HashIter32Bit(int numLowerBits, int numTotalBits) {
		this.numTotalBits = numTotalBits ;		
		this.resizingTo = numTotalBits ;
		this.lowBitsMask = (1 << numLowerBits) - 1 ;
	}
	
	public void resize(int numTotalBits) {
		if (numTotalBits > this.numTotalBits) {
			resizingTo = numTotalBits ;
			middleBitsIncrementor = 1 << (32 - (numTotalBits - this.numTotalBits)) ;
			middleBitsCounter &= middleBitsIncrementor ;
			unsafe = true ;
		} else if (numTotalBits < this.numTotalBits) {
			final int mask = ~((1 << (32 - numTotalBits)) - 1) ;
			this.highBitsCounter &= mask ;
			this.middleBitsIncrementor = 1 << (32 - (this.numTotalBits - numTotalBits)) ;
			// TODO : must modify middleBitsCounter and lowBitsCounter to make up for the truncation in highBitsCounter 
			this.resizingTo = numTotalBits ;
			this.numTotalBits = numTotalBits ;
			unsafe = true ;
		}
	}
	
	public boolean next() {
		middleBitsCounter = middleBitsCounter + middleBitsIncrementor ;
		if (middleBitsCounter == 0) {
			unsafe = false ;
			lowBitsCounter = (lowBitsCounter + 1) & lowBitsMask ;
			if (lowBitsCounter == 0) {
				highBitsCounter += 1 << (32 - numTotalBits) ;
				middleBitsIncrementor = 0 ;
				numTotalBits = resizingTo ;
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
			return highBitsCounter > highBits ;
		}
	}
	
	public boolean safe() {
		return unsafe ;
	}
	
	public int current() {
		return Integer.reverse(highBitsCounter + (middleBitsCounter >>> numTotalBits)) | lowBitsCounter ;
	}

}
