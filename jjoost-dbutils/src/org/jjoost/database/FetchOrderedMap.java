//package org.jjoost.database;
//
//import java.sql.SQLException;
//import java.util.NavigableMap;
//
//public class FetchOrderedMap<K extends Comparable<K>, V extends Comparable<V>, M> extends FetchMap<K, V, M, FetchOrderedMap<K, V, M>> {
//
//	protected Class<K> keyType;
//	protected Class<V> valueType;
//	public <NK extends Comparable<NK>, NV extends Comparable<NV>> FetchOrderedMap<NK, NV, ?> select(Class<NK> keyType, Class<NV> valueType) {
//		@SuppressWarnings("unchecked")
//		final FetchOrderedMap<NK, NV, ?> r = (FetchOrderedMap<NK, NV, ?>) this;
//		r.keyType = keyType;
//		r.valueType = valueType;
//		return r;
//	}
//	
//	public static class Scalar<K extends Comparable<K>, V extends Comparable<V>> extends FetchOrderedMap<K, V, TreapMap<K, V>> {
//		private Scalar(FetchOrderedMap<K, V, ?> copy) {
//			super(copy);
//		}
//		protected Func<TreapMap<K, V>> get() throws SQLException {
//			return fOrderedScalarMap(timeZone, keyType, valueType);
//		}
//	}
//	
//	public static class Multi<K extends Comparable<K>, V extends Comparable<V>> extends FetchOrderedMap<K, V, TreapMultiMap<K, V>> {
//		private Multi(FetchOrderedMap<K, V, ?> copy) {
//			super(copy);
//		}
//		protected Func<TreapMultiMap<K, V>> get() throws SQLException {
//			return fOrderedMultiMap(timeZone, keyType, valueType);
//		}
//	}
//	
//	public static class JDK<K extends Comparable<K>, V extends Comparable<V>> extends FetchOrderedMap<K, V, NavigableMap<K, V>> {
//		private JDK(FetchOrderedMap<K, V, ?> copy) {
//			super(copy);
//		}
//		protected Func<NavigableMap<K, V>> get() throws SQLException {
//			return fNavigableMap(timeZone, keyType, valueType);
//		}
//	}
//	
//	protected FetchOrderedMap() {
//		super();
//	}
//
//	protected FetchOrderedMap(FetchOrderedMap<K, V, ?> copy) {
//		super(copy);
//		this.keyType = copy.keyType;
//		this.valueType = copy.valueType;
//	}
//
//	static FetchOrderedMap<?, ?, ?> build() {
//		return new FetchOrderedMap<Integer, Integer, Object>();
//	}
//	
//}
