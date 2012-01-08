package org.jjoost.database;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.jjoost.collections.Map;
import org.jjoost.collections.maps.serial.SerialHashMap;
import org.jjoost.util.Classes;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;

public abstract class FetchRowLookup<BaseKeyType, InputKeyType, OutputKeyType, InputValueType, OutputValueType, M, T extends FetchRowLookup<BaseKeyType, InputKeyType, OutputKeyType, InputValueType, OutputValueType, M, T>> extends FetchDataKeyLookup<BaseKeyType, InputKeyType, OutputKeyType, OutputValueType, M, T> {

    protected boolean unGrouped = false;
    protected boolean failOnKeyReuse = true;
    protected Function<? super InputValueType[], ? extends OutputValueType> mapValues;
    protected Class<? extends InputValueType>[] valueTypes;

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
    
    public static final class Type0<K, IV, OV> extends FetchRowLookup<K, Object, Object, IV, OV, Object, Type0<K, IV, OV>> {
    	
    	private Type0(Function<? super IV[], ? extends OV> map) {
    		super(map);
    	}

        @SuppressWarnings("unchecked")
    	public <NIV> Type0<K, NIV, NIV[]> select(Class<? extends NIV> ... clazzes) {
        	final Type0<K, NIV, NIV[]> r = (Type0<K, NIV, NIV[]>) this;
        	r.valueTypes = clazzes;
        	r.mapValues = Functions.identity();
        	return r;
        }
        
	    @SuppressWarnings("unchecked")
		public <NV> Type0<K, IV, NV> mapValues(Function<? super IV[], ? extends NV> f) {
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
    
    public static final class Type1<K, IK, OK, IV, OV> extends FetchRowLookup<K, IK, OK, IV, OV, Map<OK, OV>, Type1<K, IK, OK, IV, OV>> {
    	private Equality<? super OK> keyEq;
		private Type1(boolean labeled, Function<? super IV[], ? extends OV> map) {
			super(map);
		}
		private Type1(FetchRowLookup<?, ?, ?, IV, OV, ?, ?> copy) {
			super(copy);
		}
        @SuppressWarnings("unchecked")
    	public <NIV> Type1<K, IK, OK, NIV, NIV[]> select(Class<? extends NIV> ... clazzes) {
        	final Type1<K, IK, OK, NIV, NIV[]> r = (Type1<K, IK, OK, NIV, NIV[]>) this;
        	r.valueTypes = clazzes;
        	r.mapValues = Functions.identity();
        	return r;
        }
	    @SuppressWarnings("unchecked")
		public <NV> Type1<K, IK, OK, IV, NV> mapValues(Function<? super IV[], ? extends NV> f) {
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
	    public Type1<K, IK, OK, IV, OV> withKeyEquality(Equality<? super OK> equality) {
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
			final Map<OK, V2> r = new SerialHashMap<OK, V2>(keyEq);
			return new ScalarMapWrapper<OK, V2, Map<OK, V2>>(keyEq, r);
		}
    }
    
    public static final class Type2<K, IK, OK, IV, OV> extends FetchRowLookup<K, IK, OK, IV, OV, LinkedHashMap<OK, OV>, Type2<K, IK, OK, IV, OV>> {
    	private Type2(boolean labeled, Function<? super IV[], ? extends OV> map) {
    		super(map);
    	}
    	private Type2(FetchRowLookup<?, ?, ?, IV, OV, ?, ?> copy) {
    		super(copy);
    	}
        @SuppressWarnings("unchecked")
    	public <NIV> Type2<K, IK, OK, NIV, NIV[]> select(Class<? extends NIV> ... clazzes) {
        	final Type2<K, IK, OK, NIV, NIV[]> r = (Type2<K, IK, OK, NIV, NIV[]>) this;
        	r.valueTypes = clazzes;
        	r.mapValues = Functions.identity();
        	return r;
        }
	    @SuppressWarnings("unchecked")
		public <NV> Type2<K, IK, OK, IV, NV> mapValues(Function<? super IV[], NV> f) {
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
    
	private final class RowLookup implements Func<M> {
    	
    	@Override
    	public M run(ResultSet rs) throws SQLException {
    		final Class<InputValueType> valueType = Classes.findCommonAncestor(valueTypes);
    		final KeyFetcher<OutputKeyType> keyFetcher = keyFetcher(rs);
    		final MapWrapper<OutputKeyType, OutputValueType, M> out = resultMap();
    		final GetFromResultSet<? extends InputValueType>[] columns = Fetch.<InputValueType>getItemGetters(timeZone, 1, valueTypes).run(rs) ;
    		while (rs.next()) {
    			final OutputKeyType key = keyFetcher.fetch(rs);
				@SuppressWarnings("unchecked")
				final InputValueType[] row = (InputValueType[]) Array.newInstance(valueType, columns.length);
				for (int i = 0 ; i != columns.length ; i++) {
					row[i] = columns[i].get(rs) ;
				}
				if (out.put(key, mapValues.apply(row)) != null && failOnKeyReuse) {    				
					throw new IllegalStateException("Key " + key + " was found in multiple sequences in the result set, which by default is not permitted. Either correct your grouping or select permittingKeyOverwriting(true)");
				}
    		}
    		return out.get();
    	}
    }
    
	protected Func<M> get() throws SQLException {
		return new RowLookup();
	}
	
	protected FetchRowLookup(Function<? super InputValueType[], ? extends OutputValueType> map) {
		this.mapValues = map;
	}

	protected FetchRowLookup(FetchRowLookup<?, ?, ?, InputValueType, OutputValueType, ?, ?> copy) {
		super(copy);
		this.valueTypes = copy.valueTypes;
		this.mapValues = copy.mapValues;
		this.failOnKeyReuse = copy.failOnKeyReuse;
		this.unGrouped = copy.unGrouped;
	}

	static Type0<?, Object, Object[]> build() {
		return new Type0<Object, Object, Object[]>(Functions.<Object[]>identity());
	}

}
