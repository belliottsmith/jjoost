package org.jjoost.database;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.jjoost.util.Classes;
import org.jjoost.util.Equality;
import org.jjoost.util.Function;
import org.jjoost.util.Objects;

// TODO make this superclass of FetchPartedMatrixLookup, FetchPartedColumnLookup and FetchRowLookup
public abstract class FetchDataKeyLookup<BaseKeyType, RealKeyType, KeyOutput, ValueOutput, M, T extends FetchDataKeyLookup<BaseKeyType, RealKeyType, KeyOutput, ValueOutput, M, T>> extends Fetch<M, T> {

    protected Class<BaseKeyType> keyType;
    protected Class<? extends BaseKeyType>[] keyTypes;
    protected Function<? super RealKeyType, ? extends KeyOutput> mapKeys;
    
    @SuppressWarnings("unchecked")
	protected KeyFetcher<KeyOutput> keyFetcher(ResultSet rs) throws SQLException {
    	if (keyType != null) {
    		final Function<? super BaseKeyType, ? extends KeyOutput> mapKeys = (Function<? super BaseKeyType, ? extends KeyOutput>) this.mapKeys;
    		final GetFromResultSet<? extends BaseKeyType> key = getResultSetItemGetter(keyType, timeZone, 0);
    		return new KeyFetcher<KeyOutput>() {
				@Override
				public KeyOutput fetch(ResultSet v) throws SQLException {
					return mapKeys.apply(key.get(v));
				}
				@Override
				public void unused() { }
				@Override
				public int cols() {
					return 1;
				}
    		};
    	} else {
    		final Function<? super BaseKeyType[], ? extends KeyOutput> mapKeys = (Function<? super BaseKeyType[], ? extends KeyOutput>) this.mapKeys;
    		final GetFromResultSet<? extends BaseKeyType>[] key = Fetch.<BaseKeyType>getItemGetters(timeZone, 0, keyTypes).run(rs);
    		final Class<BaseKeyType> keyType = Classes.findCommonAncestor(keyTypes);
    		return new KeyFetcher<KeyOutput>() {
    			boolean reuse = false;
    			BaseKeyType[] last;
				@Override
				public KeyOutput fetch(ResultSet v) throws SQLException {
					final BaseKeyType[] trg;
					if (reuse) {
						trg = last;
						reuse = false;
					} else {
						last = trg = (BaseKeyType[]) Array.newInstance(keyType, key.length);
					}
					for (int i = 0 ; i != trg.length ; i++) {
						trg[i] = key[i].get(v);
					}
					return mapKeys.apply(trg);
				}
				@Override
				public void unused() {
					reuse = true;
				}
				@Override
				public int cols() {
					return key.length;
				}
    		};
    	}
    }
    protected static interface KeyFetcher<K> {
    	K fetch(ResultSet rs) throws SQLException;
    	void unused();
    	int cols();
    }
    protected abstract MapWrapper<KeyOutput, ValueOutput, M> resultMap();
    protected abstract <V2> MapWrapper<KeyOutput, V2, ?> temporaryMap();
    protected static interface MapWrapper<K, V, M> {
    	V put(K key, V value);
    	V get(K key);
    	Iterable<Entry<K, V>> entries();
    	M get();
    	boolean equals(K a, K b);
    }
    
    protected static final class ScalarMapWrapper<K, V, M extends org.jjoost.collections.Map<K, V>> implements MapWrapper<K, V, M> {
    	
    	final Equality<? super K> equality;
    	final M map;
    	
		protected ScalarMapWrapper(Equality<? super K> keyEq, M map) {
			this.map = map;
			this.equality = keyEq;			
		}

		@Override
		public final V put(K key, V value) {
			return map.put(key, value);
		}

		@Override
		public final V get(K key) {
			return map.get(key);
		}

		@Override
		public final Iterable<Entry<K, V>> entries() {
			return map.entries();
		}

		@Override
		public final M get() {
			return map;
		}

		@Override
		public final boolean equals(K a, K b) {
			return equality.equates(a, b);
		}
    }

    protected static final class JDKMapWrapper<K, V, M extends Map<K, V>> implements MapWrapper<K, V, M> {
    	
    	final M map;
    	
    	protected JDKMapWrapper(M map) {
    		this.map = map;
    	}
    	
    	@Override
    	public final V put(K key, V value) {
    		return map.put(key, value);
    	}
    	
    	@Override
    	public final V get(K key) {
    		return map.get(key);
    	}
    	
    	@Override
    	public final Iterable<Entry<K, V>> entries() {
    		return map.entrySet();
    	}
    	
    	@Override
    	public final M get() {
    		return map;
    	}
    	
    	@Override
    	public final boolean equals(K a, K b) {
    		return Objects.equalQuick(a, b);
    	}
    	
    }
    
	protected FetchDataKeyLookup(FetchDataKeyLookup<?, ?, ?, ?, ?, ?> copy) {
		super(copy);
	}

	protected FetchDataKeyLookup() {
		super();
	}
	
}
