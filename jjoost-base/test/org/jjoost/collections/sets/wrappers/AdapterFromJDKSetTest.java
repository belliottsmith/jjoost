//package org.jjoost.collections.sets.wrappers;
//
//import org.jjoost.collections.Set ;
//import org.jjoost.collections.sets.base.GeneralSetTest ;
//
//public class AdapterFromJDKSetTest extends GeneralSetTest {
//
//	@Override
//	protected boolean add(String v) {
//		return set.add(v) ;
//	}
//
//	@Override
//	protected Set<String> getSet() {
//		return new AdapterFromJDKSet<String>(new java.util.HashSet<String>()) ;
//	}
//
//	@Override
//	protected String put(String v) {
//		return set.put(v) ;
//	}
//
//	public void testCopy_whenEmpty() {
//		// unsupported
//	}
//	
//	public void testCopy_whenNotEmpty() {
//		// unsupported
//	}
//	
//}
