package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.jjoost.collections.Map;
import org.jjoost.collections.maps.serial.SerialHashMap;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;

public abstract class FetchMatrixLookup<BaseKeyType, InputKeyType, OutputKeyType, InputValueType, OutputValueType, M, T extends FetchMatrixLookup<BaseKeyType, InputKeyType, OutputKeyType, InputValueType, OutputValueType, M, T>> extends FetchDataKeyLookup<BaseKeyType, InputKeyType, OutputKeyType, OutputValueType, M, T> {

    protected boolean unGrouped = false;
    protected boolean failOnKeyReuse = true;
    protected Function<? super InputValueType, ? extends OutputValueType> mapValues;
    protected final boolean labeled;
    protected Class<?>[] valueTypes;

    @SuppressWarnings("unchecked")
	public T select(Class<?> ... clazzes) {
    	this.valueTypes = clazzes;
    	return (T) this;
    }
    
    @SuppressWarnings("unchecked")
	public T withGroupedInput(boolean on) {
    	this.unGrouped = !on;
    	return (T) this;
    }
    
    @SuppressWarnings("unchecked")
	public T permittingKeyOverwriting(boolean on) {
    	this.failOnKeyReuse = !on;
    	return (T) this;
    }
    
	@SuppressWarnings("unchecked")
	protected Func<Function<Object[], OutputValueType>> mapValues() {
		if (labeled) {
			return new Label();
		} else {
			return new Constant<Function<Object[], OutputValueType>>((Function<Object[], OutputValueType>) mapValues);
		}
	}
	
    public static final class Type0<K, IV, OV> extends FetchMatrixLookup<K, Object, Object, IV, OV, Object, Type0<K, IV, OV>> {
    	
    	private Type0(boolean labeled, Function<? super IV, ? extends OV> map) {
    		super(labeled, map);
    	}
    	
	    @SuppressWarnings("unchecked")
		public <NV> Type0<K, IV, NV> mapValues(Function<? super IV, ? extends NV> f) {
	    	final Type0<K, IV, NV> r = (Type0<K, IV, NV>) this;
	    	r.mapValues = f;
	    	return r;
	    }
	    
	    /**
	     * defaults to LinkedHashMap result when yielding singleton keys
	     * @param <NK>
	     * @param clazzes
	     * @return
	     */
	    public <NK> Type2<NK, NK, NK, IV, OV> partBy(Class<NK> clazz) {
	    	final Type2<NK, NK, NK, IV, OV> r = new Type2<NK, NK, NK, IV, OV>(this);
	    	r.keyType = clazz;	 
	    	r.mapKeys = Functions.identity();
	    	return r;
	    }
	    
	    /**
	     * defaults to ScalarMap result when yielding array keys
	     * @param <NK>
	     * @param clazzes
	     * @return
	     */
	    public <NK> Type1<NK, NK[], NK[], IV, OV> partBy(@SuppressWarnings("unchecked") Class<? extends NK> ... clazzes) {
	    	final Type1<NK, NK[], NK[], IV, OV> r = new Type1<NK, NK[], NK[], IV, OV>(this);
	    	r.keyTypes = clazzes;
	    	r.mapKeys = Functions.identity();
	    	r.keyEq = Equalities.objectArray();
	    	return r;
	    }

		@Override
		protected MapWrapper<Object, OV, Object> resultMap() {
			throw new IllegalStateException("You are executing an incompletely configured builder");
		}
		
		@Override
		protected <V2> MapWrapper<Object, V2, ?> temporaryMap() {
			throw new IllegalStateException("You are executing an incompletely configured builder");
		}
    }
    
    public static final class Type1<K, IK, OK, IV, OV> extends FetchMatrixLookup<K, IK, OK, IV, OV, Map<OK, OV>, Type1<K, IK, OK, IV, OV>> {
    	private Equality<? super OK> keyEq;
		private Type1(boolean labeled, Function<? super IV, ? extends OV> map) {
			super(labeled, map);
		}
		private Type1(FetchMatrixLookup<?, ?, ?, IV, OV, ?, ?> copy) {
			super(copy);
		}
	    @SuppressWarnings("unchecked")
		public <NV> Type1<K, IK, OK, IV, NV> mapValues(Function<? super IV, ? extends NV> f) {
	    	final Type1<K, IK, OK, IV, NV> r = (Type1<K, IK, OK, IV, NV>) this;
	    	r.mapValues = f;
	    	return r;
	    }
	    @SuppressWarnings("unchecked")
	    public <NK> Type1<K, IK, NK, IV, OV> mapKeys(Function<IK, NK> f) {
	    	final Type1<K, IK, NK, IV, OV> r = (Type1<K, IK, NK, IV, OV>) this;
	    	r.mapKeys = f;
	    	r.keyEq = Equalities.object();
	    	return r;
	    }
	    public Type1<K, IK, OK, IV, OV> withKeyEq(Equality<? super OK> equality) {
	    	this.keyEq = equality;
	    	return this;
	    }
	    
		@Override
		protected MapWrapper<OK, OV, Map<OK, OV>> resultMap() {
			final Map<OK, OV> r = new SerialHashMap<OK, OV>(keyEq);
			return new ScalarMapWrapper<OK, OV, Map<OK, OV>>(keyEq, r);
		}
		public Type2<K, IK, OK, IV, OV> asLinkedJdkMap() {
			final Type2<K, IK, OK, IV, OV> r = new Type2<K, IK, OK, IV, OV>(this);
			r.keyType = this.keyType;
			r.keyTypes = this.keyTypes;
			r.mapKeys = this.mapKeys;
			return r;
		}
		@Override
		protected <V2> MapWrapper<OK, V2, ?> temporaryMap() {
			final SerialHashMap<OK, V2> r = new SerialHashMap<OK, V2>(keyEq);
			return new ScalarMapWrapper<OK, V2, Map<OK, V2>>(keyEq, r);
		}
		
    }
    
    public static final class Type2<K, IK, OK, IV, OV> extends FetchMatrixLookup<K, IK, OK, IV, OV, LinkedHashMap<OK, OV>, Type2<K, IK, OK, IV, OV>> {
    	private Type2(boolean labeled, Function<? super IV, ? extends OV> map) {
    		super(labeled, map);
    	}
    	private Type2(FetchMatrixLookup<?, ?, ?, IV, OV, ?, ?> copy) {
    		super(copy);
    	}
	    @SuppressWarnings("unchecked")
		public <NV> Type2<K, IK, OK, IV, NV> mapValues(Function<? super IV, NV> f) {
	    	final Type2<K, IK, OK, IV, NV> r = (Type2<K, IK, OK, IV, NV>) this;
	    	r.mapValues = f;
	    	return r;
	    }
	    @SuppressWarnings("unchecked")
	    public <NK> Type2<K, IK, NK, IV, OV> mapKeys(Function<IK, NK> f) {
	    	final Type2<K, IK, NK, IV, OV> r = (Type2<K, IK, NK, IV, OV>) this;
	    	r.mapKeys = f;
	    	return r;
	    }
	    public Type1<K, IK, OK, IV, OV> asScalarMap() {
			final Type1<K, IK, OK, IV, OV> r = new Type1<K, IK, OK, IV, OV>(this);
			r.keyType = this.keyType;
			r.keyTypes = this.keyTypes;
			r.mapKeys = this.mapKeys;
	    	r.keyEq = Equalities.object();
			return r;
		}
		@Override
		protected MapWrapper<OK, OV, LinkedHashMap<OK, OV>> resultMap() {
			final LinkedHashMap<OK, OV> r = new LinkedHashMap<OK, OV>();
			return new JDKMapWrapper<OK, OV, LinkedHashMap<OK, OV>>(r);
		}
		@Override
		protected <V2> MapWrapper<OK, V2, LinkedHashMap<OK, V2>> temporaryMap() {
			final LinkedHashMap<OK, V2> r = new LinkedHashMap<OK, V2>();
			return new JDKMapWrapper<OK, V2, LinkedHashMap<OK, V2>>(r);
		}
    }
    
	private final class Grouped implements Func<M> {
    	
    	@Override
    	public M run(ResultSet rs) throws SQLException {
    		final KeyFetcher<OutputKeyType> keyFetcher = keyFetcher(rs);
    		final MapWrapper<OutputKeyType, OutputValueType, M> out = resultMap();
    		final BuildColumnFromResultSet[] columns = getColumnBuilders(keyFetcher.cols(), timeZone, valueTypes).run(rs) ;
    		final Function<Object[], OutputValueType> mapValues = mapValues().run(rs);
    		OutputKeyType lastKey = null ;
    		while (rs.next()) {
    			final OutputKeyType key = keyFetcher.fetch(rs);
    			if (lastKey == null) {
    				lastKey = key;
    			} else if (!out.equals(lastKey, key)) {
    				final Object[] mx = new Object[columns.length] ;
    				for (int i = 0 ; i != columns.length ; i++) {
    					mx[i] = columns[i].getResult() ;
    				}
    				if (out.put(lastKey, mapValues.apply(mx)) != null && failOnKeyReuse) {    				
    					throw new IllegalStateException("Key " + lastKey + " was found in multiple sequences in the result set, which by default is not permitted. Either correct your grouping or select permittingKeyOverwriting(true)");
    				}
        			lastKey = key ;
    			} else {
    				keyFetcher.unused();
    			}
    			for (BuildColumnFromResultSet column : columns) {
    				column.fetch(rs) ;
    			}
    		}
    		if (lastKey != null) {
        		final Object[] mx = new Object[columns.length] ;
        		for (int i = 0 ; i != columns.length ; i++) {
        			mx[i] = columns[i].getResult() ;
        		}
				if (out.put(lastKey, mapValues.apply(mx)) != null && failOnKeyReuse) {    				
					throw new IllegalStateException("Key " + lastKey + " was found in multiple sequences in the result set, which by default is not permitted. Either correct your grouping or select permittingKeyOverwriting(true)");
				}
    		}
    		return out.get();
    	}
    }
    
    private final class Ungrouped implements Func<M> {

    	@Override
    	public M run(ResultSet rs) throws SQLException {
    		final MapWrapper<OutputKeyType, BuildColumnFromResultSet[], ?> stage = temporaryMap();
    		final KeyFetcher<OutputKeyType> keyFetcher = keyFetcher(rs);
    		final BuildColumnFromResultSet[] template = getColumnBuilders(keyFetcher.cols(), timeZone, valueTypes).run(rs);
    		final Function<Object[], OutputValueType> mapValues = mapValues().run(rs);
    		while (rs.next()) {
    			final OutputKeyType key = keyFetcher.fetch(rs);
    			BuildColumnFromResultSet[] columns = stage.get(key);
    			if (columns == null) {
    				columns = new BuildColumnFromResultSet[template.length];
    				for (int i = 0 ; i != template.length ; i++) {
    					(columns[i] = template[i].newInstance()).fetch(rs);
    				}
    				stage.put(key, columns);
    			} else {
    				keyFetcher.unused();
        			for (BuildColumnFromResultSet column : columns) {
        				column.fetch(rs) ;
        			}
    			}
    		}
    		final MapWrapper<OutputKeyType, OutputValueType, M> out = resultMap();
    		final Iterator<Entry<OutputKeyType, BuildColumnFromResultSet[]>> iter = stage.entries().iterator();
    		while (iter.hasNext()) {
    			final Entry<OutputKeyType, BuildColumnFromResultSet[]> next = iter.next();
    			final Object[] mx = new Object[template.length];
    			final BuildColumnFromResultSet[] columns = next.getValue();
    			for (int i = 0 ; i != mx.length ; i++) {
    				mx[i] = columns[i].getResult();
    			}
    			out.put(next.getKey(), mapValues.apply(mx));
    			iter.remove();
    		}
    		return out.get();
    	}
    }
    
	protected Func<M> get() throws SQLException {
		if (unGrouped) {
			return new Ungrouped();
		} else {
			return new Grouped();
		}
	}
	
	protected FetchMatrixLookup(boolean labeled, Function<? super InputValueType, ? extends OutputValueType> map) {
		this.mapValues = map;
		this.labeled = labeled;
	}

	protected FetchMatrixLookup(FetchMatrixLookup<?, ?, ?, InputValueType, OutputValueType, ?, ?> copy) {
		super(copy);
		this.valueTypes = copy.valueTypes;
		this.mapValues = copy.mapValues;
		this.failOnKeyReuse = copy.failOnKeyReuse;
		this.unGrouped = copy.unGrouped;
		this.labeled = copy.labeled;
	}

	static Type0<?, Object[], Object[]> plain() {
		return new Type0<Object, Object[], Object[]>(false, Functions.<Object[]>identity());
	}

	static Type0<?, LinkedHashMap<String, Object>, LinkedHashMap<String, Object>> labeled() {
		return new Type0<Object, LinkedHashMap<String, Object>, LinkedHashMap<String, Object>>(true, Functions.<LinkedHashMap<String, Object>>identity());
	}
	
	private final class Label implements Func<Function<Object[], OutputValueType>> {
    	final Func<String[]> names = getColumnNames(keyType != null ? 1 : keyTypes.length);
		@SuppressWarnings({ "unchecked", "serial" })
		@Override
		public Function<Object[], OutputValueType> run(ResultSet rs) throws SQLException {
			final String[] names = this.names.run(rs);
			return Functions.composition(mapValues, (Function<Object[], InputValueType>) 
				new Function<Object[], LinkedHashMap<String, Object>>() {
				@Override
				public LinkedHashMap<String, Object> apply(Object[] v) {
					final LinkedHashMap<String, Object> r = new LinkedHashMap<String, Object>();
					for (int i = 0 ; i != v.length ; i++) {
						r.put(names[i], v[i]);
					}
					return r;
				}
			});
		}
	}
	
}
