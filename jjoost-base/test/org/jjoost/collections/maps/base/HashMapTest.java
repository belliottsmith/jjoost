package org.jjoost.collections.maps.base;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AbstractHashStoreBasedScalarCollectionTest;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;

public abstract class HashMapTest extends AbstractHashStoreBasedScalarCollectionTest {

//	public UnitarySet<V> values(K key);
//	public Set<Entry<K, V>> entries();
//	public Set<K> keys();
//	public Set<V> values();
//	public int remove(K key, V val);
//	public Iterable<Entry<K, V>> removeAndReturn(K key, V val);
//	public AnyMap<V, K> inverse();
//	public boolean contains(K key, V val);
//	public int count(K key, V val);
//	public Iterable<Entry<K, V>> entries(K key);

	private final HashMap<String, String, ?> map = createMap();
	protected abstract HashMap<String, String, ?> createMap();
	private static final Function<String, String> function = new Function<String, String>() {
		private static final long serialVersionUID = 8731482102834376759L;
		@Override
		public String apply(String v) {
			return v;
		}
	};
	private static final class ValueFactory implements Factory<String> {
		private static final long serialVersionUID = 5487497026936333617L;
		private int cur = 0;
		@Override
		public String create() {
			return Integer.toString(++cur);
		}
	};

	public void testPutIfAbsentFactory_whenNotPresent() {
		checkEmpty();
		assertEquals(null, map.putIfAbsent("a", function));
		assertEquals(null, map.putIfAbsent("q", function));
		assertEquals(null, map.putIfAbsent(null, function));
		checkAndClear(3);
	}
	
	public void testPutIfAbsentFactory_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String q1 = "q";
		final String q2 = new String("q");
		map.putIfAbsent(a1, function);
		map.putIfAbsent(q1, function);
		assertSame(a1, map.putIfAbsent(a2, function));
		assertSame(q1, map.putIfAbsent(q2, function));
		assertSame(a1, get(a2));
		assertSame(q1, get(q2));
		checkAndClear(2);
	}
	
	public void testEnsureAndGetFunction_whenNotPresent() {
		checkEmpty();
		assertEquals("a", map.ensureAndGet("a", function));
		assertEquals("q", map.ensureAndGet("q", function));
		assertEquals(null, map.ensureAndGet(null, function));
		checkAndClear(3);
	}
	
	public void testEnsureAndGetFunction_whenPresent() {
		checkEmpty();
		final String a1 = "a";
		final String a2 = new String("a");
		final String q1 = "q";
		final String q2 = new String("q");
		map.ensureAndGet(a1, function);
		map.ensureAndGet(q1, function);
		assertSame(a1, map.ensureAndGet(a2, function));
		assertSame(q1, map.ensureAndGet(q2, function));
		assertSame(a1, get(a2));
		assertSame(q1, get(q2));
		checkAndClear(2);
	}
	
	public void testEnsureAndGetFactory_whenNotPresent() {
		checkEmpty();
		final Factory<String> factory = new ValueFactory();
		assertEquals("1", map.ensureAndGet("a", factory));
		assertEquals("2", map.ensureAndGet("q", factory));
		assertEquals("3", map.ensureAndGet(null, factory));
		checkAndClear(3);
	}
	
	public void testEnsureAndGetFactory_whenPresent() {
		checkEmpty();
		final Factory<String> factory = new ValueFactory();
		final String a1 = "a";
		final String a2 = new String("a");
		final String q1 = "q";
		final String q2 = new String("q");
		map.ensureAndGet(a1, factory);
		map.ensureAndGet(q1, factory);
		assertEquals("1", map.ensureAndGet(a2, factory));
		assertEquals("2", map.ensureAndGet(q2, factory));
		assertEquals("1", get(a2));
		assertEquals("2", get(q2));
		checkAndClear(2);
	}

	public void testRemovePair_whenNotPresent() {
		checkEmpty();
		assertEquals(0, map.remove("a", "a"));
		map.put("q", "q");
		assertEquals(0, map.remove("a", "a"));
		map.put("a", "q");
		assertEquals(0, map.remove("a", "a"));
		checkAndClear(2);
	}
	
	public void testRemovePair_whenPresent() {
		checkEmpty();
		map.put("a", "a");
		map.put("q", "q");
		assertEquals(1, map.remove("a", "a"));
		assertEquals(1, map.remove("q", "q"));
		map.put("a", "a");
		map.put("q", "q");
		assertEquals(1, map.remove("q", "q"));
		assertEquals(1, map.remove("a", "a"));
		checkAndClear(0);
	}
	
	@SuppressWarnings("unchecked")
	public void testRemoveAndReturnPair_whenNotPresent() {
		checkEmpty();
		checkIterableContents(Arrays.<ImmutableMapEntry<String, String>>asList(), map.removeAndReturn("a", "a"), true);
		map.put("q", "q");
		checkIterableContents(Arrays.<ImmutableMapEntry<String, String>>asList(), map.removeAndReturn("a", "a"), true);
		map.put("a", "q");
		checkIterableContents(Arrays.<ImmutableMapEntry<String, String>>asList(), map.removeAndReturn("a", "a"), true);
		checkAndClear(2);
	}
	
	@SuppressWarnings("unchecked")
	public void testRemoveAndReturnPair_whenPresent() {
		checkEmpty();
		map.put("a", "a");
		map.put("q", "q");
		checkIterableContents(Arrays.asList(new ImmutableMapEntry<String, String>("a", "a")), map.removeAndReturn("a", "a"), true);
		checkIterableContents(Arrays.asList(new ImmutableMapEntry<String, String>("q", "q")), map.removeAndReturn("q", "q"), true);
		map.put("a", "a");
		map.put("q", "q");
		checkIterableContents(Arrays.asList(new ImmutableMapEntry<String, String>("q", "q")), map.removeAndReturn("q", "q"), true);
		checkIterableContents(Arrays.asList(new ImmutableMapEntry<String, String>("a", "a")), map.removeAndReturn("a", "a"), true);
		checkAndClear(0);
	}
	
	public void testContainsPair() {
		checkEmpty();
		assertEquals(false, map.contains("a", "a"));
		assertEquals(false, map.contains("q", "q"));
		map.put("a", "q");
		assertEquals(true, map.contains("a", "q"));
		assertEquals(false, map.contains("a", "a"));
		assertEquals(false, map.contains("q", "q"));
		map.put("q", "a") ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(true, map.contains("a", "q"));
		assertEquals(true, map.contains("q", "a"));
		assertEquals(false, map.contains("a", "a"));
		assertEquals(false, map.contains("q", "q"));
		checkAndClear(2);
	}
	
	public void testCountPair() {
		checkEmpty();
		assertEquals(0, map.count("a", "a"));
		assertEquals(0, map.count("q", "q"));
		map.put("a", "q");
		assertEquals(1, map.count("a", "q"));
		assertEquals(0, map.count("a", "a"));
		assertEquals(0, map.count("q", "q"));
		map.put("q", "a") ; // add should not alter the state of the set, as an equal value is already present
		assertEquals(1, map.count("a", "q"));
		assertEquals(1, map.count("q", "a"));
		assertEquals(0, map.count("a", "a"));
		assertEquals(0, map.count("q", "q"));
		checkAndClear(2);
	}
	
	@Override
	protected boolean add(String v) {
		return map.add(v, v);
	}

	@Override
	protected int capacity() {
		return map.capacity();
	}

	@Override
	protected int clear() {
		return map.clear();
	}

	@Override
	protected Iterator<String> clearAndReturn() {
		return Functions.apply(map.clearAndReturn(), Functions.<String, Entry<String, String>>getMapEntryValueProjection());
	}

	@Override
	protected boolean contains(String v) {
		return map.contains(v);
	}

	@Override
	protected int count(String v) {
		return map.count(v);
	}

	@Override
	protected String first(String v) {
		return map.first(v);
	}

	@Override
	protected String get(String v) {
		return map.get(v);
	}

	@Override
	protected boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	protected Iterable<String> iterate(String v) {
		return map.values(v);
	}

	@Override
	protected Iterator<String> iterator() {
		return map.values().iterator();
	}

	@Override
	protected List<String> list(String v) {
		return map.list(v);
	}

	@Override
	protected String put(String v) {
		return map.put(v, v);
	}

	@Override
	protected String putIfAbsent(String v) {
		return map.putIfAbsent(v, v);
	}

	@Override
	protected int remove(String v) {
		return map.remove(v);
	}

	@Override
	protected Iterable<String> removeAndReturn(String v) {
		return Functions.apply(map.removeAndReturn(v), Functions.<String, Entry<String, String>>getMapEntryValueProjection());
	}

	@Override
	protected String removeAndReturnFirst(String v) {
		return map.removeAndReturnFirst(v);
	}

	@Override
	protected void resize(int i) {
		map.resize(i);
	}

	@Override
	protected void shrink() {
		map.shrink();
	}

	@Override
	protected int size() {
		return map.size();
	}

	@Override
	protected int totalCount() {
		return map.totalCount();
	}

	@Override
	protected int uniqueCount() {
		return map.uniqueKeyCount();
	}
	
}
