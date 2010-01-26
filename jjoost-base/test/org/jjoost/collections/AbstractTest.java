package org.jjoost.collections;

import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.jjoost.util.Iters ;

import junit.framework.TestCase ;

public abstract class AbstractTest extends TestCase {

	protected void checkListContents(List<?> expect, List<?> actual, boolean expectSameOrder) {
		assertEquals(expect.size(), actual.size()) ;
		if (expectSameOrder) {
			for (int i = 0 ; i != expect.size() ; i++)
				assertSame(expect.get(0), actual.get(0)) ;
		} else {
			for (Object o : expect)
				assertEquals(Iters.count(o, expect), Iters.count(o, actual)) ;
		}			
	}
	
	protected void checkIterableContents(Iterable<?> expect, Iterable<?> actual, boolean expectSameOrder) {
		checkIteratorContents(expect.iterator(), actual.iterator(), expectSameOrder) ;
	}
	
	protected void checkIteratorContents(Iterator<?> expect, Iterator<?> actual, boolean expectSameOrder) {
		if (expectSameOrder) {
			while (expect.hasNext() && actual.hasNext())
				assertEquals(expect.next(), actual.next()) ;
			assertEquals(expect.hasNext(), actual.hasNext()) ;
			checkFinishedIterator(actual) ;
		} else {
			checkListContents(Iters.toList(expect), Iters.toList(actual), false) ;
		}
	}

	protected void checkFinishedIterator(Iterator<?> i) {
		try {
			assertFalse(i.hasNext()) ;
			i.next() ;
			assertTrue(false) ;
		} catch (NoSuchElementException e) {			
		}
	}
	
}
