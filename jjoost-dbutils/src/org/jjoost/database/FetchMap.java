package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.NavigableMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.Map;
import org.jjoost.collections.MultiMap;
import org.jjoost.collections.maps.serial.SerialHashMap;
import org.jjoost.collections.maps.serial.SerialInlineMultiHashMap;

public abstract class FetchMap<K, V, M, T extends FetchMap<K, V, M, T>> extends Fetch<M, T> {

	protected static <K, V> Func<NavigableMap<K, V>> fNavigableMap(TimeZone tz, Class<? extends K> domain, Class<? extends V> range) throws SQLException {
		return new FillJDKMap<K, V, NavigableMap<K, V>>(getResultSetItemGetter(domain, tz, 0), getResultSetItemGetter(range, tz, 1), new TreeMap<K, V>()) ;
	}
	protected static <K, V> Func<LinkedHashMap<K, V>> fLinkedMap(TimeZone tz, Class<? extends K> domain, Class<? extends V> range) throws SQLException {
		return new FillJDKMap<K, V, LinkedHashMap<K, V>>(getResultSetItemGetter(domain, tz, 0), getResultSetItemGetter(range, tz, 1), new LinkedHashMap<K, V>()) ;
	}
	protected static <K, V> Func<Map<K, V>> fScalarMap(TimeZone tz, Class<? extends K> domain, Class<? extends V> range) throws SQLException {
		return new FillMap<K, V, Map<K, V>>(getResultSetItemGetter(domain, tz, 0), getResultSetItemGetter(range, tz, 1), new SerialHashMap<K, V>()) ;
	}
	protected static <K, V> Func<MultiMap<K, V>> fMultiMap(TimeZone tz, Class<? extends K> domain, Class<? extends V> range) throws SQLException {
		return new FillMap<K, V, MultiMap<K, V>>(getResultSetItemGetter(domain, tz, 0), getResultSetItemGetter(range, tz, 1), new SerialInlineMultiHashMap<K, V>()) ;
	}
//	protected static <K extends Comparable<K>, V> Func<TreapMap<K, V>> fOrderedScalarMap(TimeZone tz, Class<K> domain, Class<? extends V> range) throws SQLException {
//		return new FillMap<K, V, TreapMap<K, V>>(getResultSetItemGetter(domain, tz, 0), getResultSetItemGetter(range, tz, 1), TreapMap.<K, V>getComparableMap(domain)) ;
//	}
//	protected static <K extends Comparable<K>, V extends Comparable<V>> Func<TreapMultiMap<K, V>> fOrderedMultiMap(TimeZone tz, Class<K> domain, Class<V> range) throws SQLException {
//		return new FillMap<K, V, TreapMultiMap<K, V>>(getResultSetItemGetter(domain, tz, 0), getResultSetItemGetter(range, tz, 1), TreapMultiMap.<K, V>getComparableMap(domain, range)) ;
//	}
	private static final class FillJDKMap<K, V, M extends java.util.Map<K, V>> implements Func<M> {
		final GetFromResultSet<K> domain ;
		final GetFromResultSet<V> range ;
		final M map ;
		public FillJDKMap(GetFromResultSet<K> domain, GetFromResultSet<V> range, M map) {
			this.domain = domain;
			this.range = range;
			this.map = map;
		}
		@Override
		public M run(ResultSet rs) throws SQLException {
			while (rs.next()) {
				map.put(domain.get(rs), range.get(rs)) ;
			}    			
			return map ;
		}
	}
	private static final class FillMap<K, V, M extends AnyMap<K, V>> implements Func<M> {
		final GetFromResultSet<K> domain ;
		final GetFromResultSet<V> range ;
		final M map ;
		public FillMap(GetFromResultSet<K> domain, GetFromResultSet<V> range, M map) {
			this.domain = domain;
			this.range = range;
			this.map = map;
		}
		@Override
		public M run(ResultSet rs) throws SQLException {
    		while (rs.next()) {
    			map.put(domain.get(rs), range.get(rs)) ;
    		}    			
	    	return map ;
		}
	}

	protected FetchMap() { }
	protected FetchMap(FetchMap<?, ?, ?, ?> copy) { super(copy); }
	
	@Override
	protected Func<M> get() throws SQLException {
		throw new IllegalStateException("Must specify the type of map to return");
	}

}
