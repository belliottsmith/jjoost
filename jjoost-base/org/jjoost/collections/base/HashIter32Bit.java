package org.jjoost.collections.base;

public class HashIter32Bit {

	private final int endHighBits ;
	private final int lowBitsMask ;
	private final int[] completionCaps ;
	private int currentHighBits ;
	private int nextHighBits ;
	private int currentLowBits ;
	private int numTotalBits ;
	private int incrementor ;
	private int unsafeUntil = 0 ;
	private int unsafeFrom = Integer.MAX_VALUE ;
	
	public HashIter32Bit(int numLowerBits, int numTotalBits) {
		this.completionCaps = new int[1 << numLowerBits] ;
		java.util.Arrays.fill(completionCaps, -1) ;
		this.numTotalBits = numTotalBits ;
		this.lowBitsMask = (1 << numLowerBits) - 1 ;
		this.endHighBits = 1 << (32 - numLowerBits) ;
		this.incrementor = 1 << (32 - numTotalBits) ;
		this.nextHighBits = this.incrementor ;
	}
	
	public void resize(int numTotalBits) {
		final int newIncrementor = 1 << (32 - numTotalBits) ;
		if (numTotalBits > this.numTotalBits) {
			if (unsafeFrom > currentHighBits)
				unsafeFrom = currentHighBits ;
			final int unsafeTil = nextHighBits ;
			if (unsafeUntil < unsafeTil)
				unsafeUntil = unsafeTil ;
		} else if (numTotalBits < this.numTotalBits) {
			final int mask = ~(newIncrementor - 1) ;
			currentHighBits &= mask ;
			if (unsafeUntil != 0) {
				// need to round unsafeFrom and unsafeUntil to the nearest multiple of the new (smaller) HighBits incrementor
				unsafeFrom &= mask ;
				if ((unsafeUntil & mask) != 0)
					unsafeUntil = (unsafeUntil & mask) + newIncrementor ;
			}
		}
		this.incrementor = newIncrementor ;
		this.nextHighBits = currentHighBits + newIncrementor ;
		this.numTotalBits = numTotalBits ;
	}
	
	public boolean next() {
		if (nextHighBits < completionCaps[currentLowBits])
			throw new IllegalStateException() ;
		completionCaps[currentLowBits] = nextHighBits ;
		while (true) {
			if (unsafeUntil != 0) {
				if (nextHighBits == unsafeUntil) {
					currentHighBits = unsafeFrom ;
					nextHighBits = currentHighBits + incrementor ;
					unsafeUntil = 0 ;
					unsafeFrom = Integer.MAX_VALUE ;
				} else {
					currentHighBits = nextHighBits ;
					nextHighBits += incrementor ;
					if (nextHighBits > completionCaps[currentLowBits])
						return true ;
				}
			} else if (currentLowBits == lowBitsMask) {
				currentHighBits = nextHighBits ;
				if (currentHighBits == endHighBits)
					return false ;
				nextHighBits += incrementor ;
				currentLowBits = 0 ;				
				if (nextHighBits > completionCaps[currentLowBits])
					return true ; 
			} else {
				currentLowBits += 1 ;
				if (nextHighBits > completionCaps[currentLowBits])
					return true ;
			}
		}
	}
	
	public boolean haveVisitedAlready(int hash) {
		return completionCaps[hash & lowBitsMask] > Integer.reverse(hash & ~lowBitsMask) ;
	}
	
	public boolean safe() {
		return unsafeUntil == 0 ;
	}
	
	public int current() {
		return Integer.reverse(currentHighBits) | currentLowBits ;
	}
	
	public static void main(String[] args) {
		final HashIter32Bit iter = new HashIter32Bit(4, 8) ;
		boolean[] visited = new boolean[256] ;
		boolean cont = true ;
		int c = 0 ;
		try {
			while (cont) {
				System.out.println(String.format("%32s  %4s %10s %b", Integer.toBinaryString(iter.currentHighBits), Integer.toBinaryString(iter.currentLowBits), Integer.toBinaryString(iter.current()), iter.safe())) ;
				if (iter.haveVisitedAlready(iter.current()))
					System.out.println("visited") ;				
				visited[iter.current()] = true ;
				c++ ;
				switch(c) {
				case 29:				
					iter.resize(7) ;
					System.out.println("resize to 7") ;
					break ;
				case 31:
					iter.resize(10) ;
					System.out.println("resize to 10") ;
					break ;
				case 32:
					iter.resize(8) ;
					System.out.println("resize to 8") ;
					break ;
				default :
					cont = iter.next() ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out) ;
		}
		System.out.println(c) ;
		for (int i = 0 ; i != visited.length ; i++) {
			if (!visited[i]) {
				System.out.println(String.format("%10s", Integer.toBinaryString(i))) ;
			}
		}
	}
	
}
