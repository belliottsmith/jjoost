package org.jjoost.database;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.jjoost.collections.Map;
import org.jjoost.collections.MultiMap;

public class FetchHashMap<K, V, M> extends FetchMap<K, V, M, FetchHashMap<K, V, M>> {

	protected Class<K> keyType;
	protected Class<V> valueType;
	public <NK, NV> FetchHashMap<NK, NV, ?> select(Class<NK> keyType, Class<NV> valueType) {
		@SuppressWarnings("unchecked")
		final FetchHashMap<NK, NV, ?> r = (FetchHashMap<NK, NV, ?>) this;
		r.keyType = keyType;
		r.valueType = valueType;
		return r;
	}
	
	public FetchHashMap<K, V, Map<K, V>> asScalarMap() {
		return new FetchMap<K, V>(this);
	}
	
	public FetchHashMap<K, V, MultiMap<K, V>> asMultiMap() {
		return new FetchMultiMap<K, V>(this);
	}
	
	public FetchHashMap<K, V, LinkedHashMap<K, V>> asLinkedJdkMap() {
		return new FetchJDKHashMap<K, V>(this);
	}
	
	public static class FetchMap<K, V> extends FetchHashMap<K, V, Map<K, V>> {
		private FetchMap(FetchHashMap<K, V, ?> copy) {
			super(copy);
		}
		protected Func<Map<K, V>> get() throws SQLException {
			return fScalarMap(timeZone, keyType, valueType);
		}
	}
	
	public static class FetchMultiMap<K, V> extends FetchHashMap<K, V, MultiMap<K, V>> {
		private FetchMultiMap(FetchHashMap<K, V, ?> copy) {
			super(copy);
		}
		protected Func<MultiMap<K, V>> get() throws SQLException {
			return fMultiMap(timeZone, keyType, valueType);
		}
	}
	
	public static class FetchJDKHashMap<K, V> extends FetchHashMap<K, V, LinkedHashMap<K, V>> {
		private FetchJDKHashMap(FetchHashMap<K, V, ?> copy) {
			super(copy);
		}
		protected Func<LinkedHashMap<K, V>> get() throws SQLException {
			return fLinkedMap(timeZone, keyType, valueType);
		}
	}
	
	protected FetchHashMap() {
		super();
	}

	protected FetchHashMap(FetchHashMap<K, V, ?> copy) {
		super(copy);
		this.keyType = copy.keyType;
		this.valueType = copy.valueType;
	}

	static FetchHashMap<?, ?, ?> build() {
		return new FetchHashMap<Object, Object, Object>();
	}
	
}
