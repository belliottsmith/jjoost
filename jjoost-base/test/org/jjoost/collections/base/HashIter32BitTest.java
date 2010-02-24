package org.jjoost.collections.base;

import java.util.Arrays;

import junit.framework.TestCase;

public class HashIter32BitTest extends TestCase {

	public void testNoResize() {
		for (int i = 2 ; i != 20 ; i++) {
			HashIter32Bit iter = new HashIter32Bit(i > 4 ? 4 : i, i) ;
			boolean[] visited = new boolean[1 << i] ;
			int lc = -1 ;
			do {
				if (lc >= 0)
					assertTrue(iter.haveVisitedAlready(lc)) ;
				assertFalse(visited[iter.current()]) ;
				assertFalse(iter.haveVisitedAlready(iter.current())) ;
				visited[iter.current()] = true ;
				lc = iter.current() ;
			} while (iter.next()) ;
			for (int j = 0 ; j != visited.length ; j++)
				assertTrue(Integer.toString(j), visited[j]) ;
		}
	}
	
	public void testResizing() {
		HashIter32Bit iter = new HashIter32Bit(4, 10) ;
		int[] visited = new int[1 << 20] ;
		int[] visited2 = new int[1 << 20] ;
		Arrays.fill(visited, -1) ;
		int i = 0 ;
		int nextResize = 10 ;
		int size = 10 ;
		int sizeIncrementor = 1 ; 
		do {
			if (++i == nextResize) {
				if (size + sizeIncrementor == 20) {
					iter.resize(size = 20) ;
					sizeIncrementor = -5 ;
					nextResize <<= 2 ;
				} else if (sizeIncrementor > 0) {
					iter.resize(size += sizeIncrementor++) ;
					nextResize <<= 2 ;
				} else {
					iter.resize(size += sizeIncrementor--) ;
					nextResize <<= 2 ;
				}
			}
			int c = iter.current() ;
			assertEquals(i + " (" + c + ") vs " + visited[c] + " (" + visited2[c] + ") @ " + size, visited[c] != -1, iter.haveVisitedAlready(c)) ;
			for (int j = 0 ; j != 1 << (20 - size) ; j++) {					
				visited[c | (j << size)] = i ;
				visited2[c | (j << size)] = c ;
			}
			if (!iter.next())
				break ;
		} while(true) ;
		int c = 0 ;
		for (int j = 0 ; j != visited.length ; j++) {
			if (visited[j] == -1) {
				c++ ;
			}
		}
		System.out.println(c) ;
		for (int j = 0 ; j != visited.length ; j++)
			assertTrue(Integer.toString(j), visited[j] != -1) ;		
	}
	
}
