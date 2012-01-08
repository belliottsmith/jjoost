package org.jjoost.database;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class FetchObjects<E> extends Fetch<E[], FetchObjects<E>> {

	private Class<E> type;
	private Constructor<E> constructor;
	public <NE> FetchObjects<NE> select(Class<NE> type) {
		@SuppressWarnings("unchecked")
		final FetchObjects<NE> r = (FetchObjects<NE>) this;
		r.type = type;
		r.constructor = null;
		return r;
	}
	public <NE> FetchObjects<NE> select(Constructor<NE> constructor) {
		@SuppressWarnings("unchecked")
		final FetchObjects<NE> r = (FetchObjects<NE>) this;
		r.constructor = constructor;
		r.type = null;
		return r;
	}
	
	public Class<E> getColumnType() {
		return type;
	}

    private static <E> Func<E[]> fObject(TimeZone tz, Constructor<E> c) throws SQLException {
    	return new AnyObject<E>(new Constant<Constructor<E>>(c), getItemGetters(tz, 1, c.getParameterTypes())) ;
    }
    private static <E> Func<E[]> fObject(TimeZone tz, Class<E> c) throws SQLException {
    	final Func<Constructor<E>> constructor = new ConstructorGuesser<E>(c) ;
    	final Func<GetFromResultSet<?>[]> args = new ConstructorArgGetters(tz, constructor) ;
    	return new AnyObject<E>(constructor, args) ;
    }
    private static final class Constant<E> implements Func<E> {
    	final E self ;
		private Constant(E self) {
			this.self = self;
		}
		@Override
		public E run(ResultSet rs) throws SQLException {
			return self ;
		}    	
    }
    private static final class ConstructorArgGetters implements Func<GetFromResultSet<?>[]> {
    	final Func<? extends Constructor<?>> constructor ;
    	final TimeZone tz ;
		private ConstructorArgGetters(TimeZone tz, Func<? extends Constructor<?>> constructor) {
			this.tz = tz ;
			this.constructor = constructor;
		}
		@Override
		public GetFromResultSet<?>[] run(ResultSet rs) throws SQLException {
			return getItemGetters(tz, 0, true, constructor.run(rs).getParameterTypes()).run(rs) ;
		}
    }
    private static final class ConstructorGuesser<E> implements Func<Constructor<E>> {
    	final Class<E> clazz ;
    	private ConstructorGuesser(Class<E> clazz) {
    		this.clazz = clazz ;
    	}
    	@SuppressWarnings("unchecked")
		@Override
    	public Constructor<E> run(ResultSet rs) throws SQLException {
    		final ResultSetMetaData meta = rs.getMetaData() ;
    		final Constructor<E>[] constructors = (Constructor<E>[]) clazz.getConstructors() ;
    		constr: for (int i = 0 ; i != constructors.length ; i++) {
    			final Constructor<E> constructor = constructors[i] ;
    			final Class<?>[] args = constructor.getParameterTypes() ;
    			if (args.length != meta.getColumnCount()) {
    				continue ;
    			}
    			for (int j = 0 ; j != args.length ; j++) {
    				if (!compatible(args[j], meta.getColumnType(1 + j))) {
    					continue constr ;
    				}
    			}
    			return constructor ;
    		}
    		throw new IllegalArgumentException("Could not find a constructor in " + clazz.getCanonicalName() + " matching the return types of the provided query") ;
    	}    	
    }
    private static final class AnyObject<E> implements Func<E[]> {
    	final Func<Constructor<E>> constructor ;
    	final Func<? extends GetFromResultSet<?>[]> columns ;
    	private AnyObject(Func<Constructor<E>> constructor, Func<? extends GetFromResultSet<?>[]> columns) {
			this.constructor = constructor;
			this.columns = columns;
		}
		@SuppressWarnings("unchecked")
		@Override
    	public E[] run(ResultSet rs) throws SQLException {
    		try {
				final List<E> r = new ArrayList<E>() ;
				final Constructor<E> constructor = this.constructor.run(rs) ;
				final GetFromResultSet<?>[] columns = this.columns.run(rs) ;
				final Object[] args = new Object[columns.length] ;
				while (rs.next()) {
					int i = 0 ;
					for (GetFromResultSet<?> column : columns) {
						args[i++] = column.get(rs) ;
					}
					r.add(constructor.newInstance(args)) ;
				}
				return r.toArray((E[]) Array.newInstance(constructor.getDeclaringClass(), r.size())) ;
			} catch (IllegalAccessException e) {
				throw new UndeclaredThrowableException(e) ;
			} catch (InvocationTargetException e) {
				throw new UndeclaredThrowableException(e) ;
			} catch (InstantiationException e) {
				throw new UndeclaredThrowableException(e) ;
			}
    	}
    }
    
	@Override
	protected Func<E[]> get() throws SQLException {
		if (constructor != null) {
			return fObject(timeZone, constructor);
		} else {
			return fObject(timeZone, type);
		}
	}

	static FetchObjects<?> build() {
		return new FetchObjects<Object>();
	}

}
