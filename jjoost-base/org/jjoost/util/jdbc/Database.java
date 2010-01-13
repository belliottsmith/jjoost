package org.jjoost.util.jdbc ;

import javax.sql.DataSource;

import org.jjoost.collections.AnyMap ;
import org.jjoost.collections.Map ;
import org.jjoost.collections.MultiMap ;
import org.jjoost.collections.maps.serial.SerialInlineMultiHashMap ;
import org.jjoost.collections.maps.serial.SerialHashMap ;
import org.jjoost.util.Function ;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException ;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A collection of utilities for performing simple tasks on databases
 * 
 * @author b.elliottsmith
 */
public class Database {

	public static enum ConnectionType {
		MS2000, MS2005, JTDS
	}

	/**
	 * A simple method to safely create and execute an SQL statement with the supplied data source
	 *  
	 * @param dataSource
	 * @param sql
	 * @throws SQLException
	 */
	public static void execute(DataSource dataSource, String sql) throws SQLException {
		Connection conn = null ;
		try {
			conn = dataSource.getConnection() ;
			execute(conn, sql) ;
		} finally {
			if (conn != null)
				conn.close() ;
		}
	}
	
	/**
	 * A simple method to safely create and execute an SQL statement with the supplied data source
	 *  
	 * @param dataSource
	 * @param sql
	 * @throws SQLException
	 */
    public static void execute(Connection conn, String sql) throws SQLException {
        Statement stmt = null ;        
        try {
            stmt = conn.createStatement() ;            
            stmt.execute(sql) ;
            if (!conn.getAutoCommit()) conn.commit() ;
        } finally {
            if (stmt != null) 
            	stmt.close() ;
        }
    }

    /**
     * A simple method to safely create and execute an SQL update statement with the supplied data source
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static int executeUpdate(DataSource dataSource, String sql) throws SQLException {
    	Connection conn = null ;
    	try {
    		conn = dataSource.getConnection() ;
    		return executeUpdate(conn, sql) ;
    	} finally {
    		if (conn != null)
    			conn.close() ;
    	}
    }
    /**
     * A simple method to safely create and execute an SQL update statement with the supplied data source
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static int executeUpdate(Connection conn, String sql) throws SQLException {
        Statement stmt = null ;
        try {
            stmt = conn.createStatement() ;
            final int r = stmt.executeUpdate(sql) ;
        	if (!conn.getAutoCommit()) conn.commit() ;
        	return r ;
        } finally {
            if (stmt != null) 
            	stmt.close() ;
        }
    }
    
    /**
     * A simple method to safely create and execute an SQL update statement with the supplied data source
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static int[] executeBatchUpdate(DataSource dataSource, List<String> sql) throws SQLException {
    	Connection conn = null ;
    	try {
    		conn = dataSource.getConnection() ;
    		return executeBatchUpdate(conn, sql) ;
    	} finally {
    		if (conn != null)
    			conn.close() ;
    	}
    }
    /**
     * A simple method to safely create and execute an SQL update statement with the supplied data source
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static int[] executeBatchUpdate(Connection conn, List<String> sql) throws SQLException {
		final boolean autocommit = conn.getAutoCommit() ;
		try {
			conn.setAutoCommit(false) ;
			final Savepoint sp = conn.setSavepoint() ;
			try {
		    	final Statement stmt = conn.createStatement() ;
		    	try {
		       		for (String s : sql)
		       			stmt.addBatch(s) ;
		    		final int[] r = stmt.executeBatch() ;
		    		conn.commit() ;
		    		return r ;
		    	} finally {
		            if (stmt != null) 
		            	stmt.close() ;
		    	}
			} catch (Exception e) {
	    		conn.rollback(sp) ;
	    		if (e instanceof SQLException)
	    			throw ((SQLException) e) ;
	    		else if (e instanceof RuntimeException)
	    			throw ((RuntimeException) e) ;
	    		else throw new UndeclaredThrowableException(e) ;
			} 
		} finally {
            try {
            	conn.setAutoCommit(autocommit) ;
            } catch (Exception e) {            	
            }
		}
    }

    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static Object getScalar(DataSource dataSource, String sql) throws SQLException {
    	return getScalar(dataSource, sql, null, Object.class) ;
    }
    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static <E> E getScalar(DataSource dataSource, String sql, Class<E> clazz) throws SQLException {
    	return getScalar(dataSource, sql, null, clazz) ;
    }
    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static <E> E getScalar(DataSource dataSource, String sql, TimeZone tz, Class<E> clazz) throws SQLException {
    	Connection conn = null ;
    	try {
    		conn = dataSource.getConnection() ;
    		return getScalar(conn, sql, tz, clazz) ;
    	} finally {
    		if (conn != null) conn.close() ;
    	}
    }
    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static Object getScalar(Connection conn, String sql) throws SQLException {
    	return getScalar(conn, sql, null, Object.class) ;
    }
    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static <E> E getScalar(Connection conn, String sql, Class<E> clazz) throws SQLException {
    	return getScalar(conn, sql, null, clazz) ;
    }
    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static <E> E getScalar(Connection conn, String sql, TimeZone tz, Class<E> clazz) throws SQLException {
        Statement stmt = null ;
        ResultSet rs = null ;
        try {
            stmt = conn.createStatement() ;
            rs = stmt.executeQuery(sql) ;
            if (rs.next()) 
            	return getResultSetItemGetter(clazz, tz, 0).get(rs) ;
        	return null;
        } finally {
        	if (rs != null) rs.close() ;
        	if (stmt != null) stmt.close() ;
        }
    }
    
    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Object tryGetScalar(final DataSource dataSource, final String sql, long timeout, TimeUnit units) throws SQLException, InterruptedException, ExecutionException, TimeoutException {
    	Callable<Object> call = new Callable<Object>() {
    		public Object call() throws Exception {
    			return getScalar(dataSource, sql) ;
    		}
    	} ;
    	ExecutorService svc = Executors.newSingleThreadExecutor() ;
    	Future<Object> result = svc.submit(call) ;
    	svc.shutdown() ;
    	return result.get(timeout, units) ;
    }
    
    /**
     * A simple method to safely execute an SQL statement with the supplied data source and return the first result on the first column (or null if none)
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Object tryGetScalar(final Connection conn, final String sql, long timeout, TimeUnit units) throws SQLException, InterruptedException, ExecutionException, TimeoutException {
    	Callable<Object> call = new Callable<Object>() {
			public Object call() throws Exception {
				return getScalar(conn, sql) ;
			}
    	} ;
    	ExecutorService svc = Executors.newSingleThreadExecutor() ;
    	Future<Object> result = svc.submit(call) ;
    	svc.shutdown() ;
    	return result.get(timeout, units) ;
    }

    /**
     * A simple method to safely execute an sql statement and return its first column as a list of objects
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static List<Object> getList(DataSource dataSource, String sql) throws SQLException {
    	return getList(dataSource, sql, Object.class) ;
    }
    
    /**
     * A simple method to safely execute an sql statement and return its first column as a list of type clazz
     * 
     * @param <E>
     * @param dataSource
     * @param sql
     * @param clazz
     * @return
     * @throws SQLException
     */
    public static <E> List<E> getList(DataSource dataSource, String sql, Class<E> clazz) throws SQLException {
        Connection conn = null ;
        try {        	
        	conn = dataSource.getConnection() ;
        	return getList(conn, sql, clazz) ;
        } finally {
        	if (conn != null) conn.close() ;
        }
    }
    
    /**
     * A simple method to safely execute an sql statement and return its first column as a list of type clazz
     * 
     * @param <E>
     * @param dataSource
     * @param sql
     * @param clazz
     * @return
     * @throws SQLException
     */
    public static <E> List<E> getList(Connection conn, String sql, Class<E> clazz) throws SQLException {
    	return getList(conn, sql, null, clazz) ;
    }
    /**
     * A simple method to safely execute an sql statement and return its first column as a list of type clazz
     * 
     * @param <E>
     * @param dataSource
     * @param sql
     * @param clazz
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
	public static <E> List<E> getList(Connection conn, String sql, TimeZone tz, Class<E> clazz) throws SQLException {
    	return java.util.Arrays.asList((E[]) getMatrix(conn, sql, tz, clazz)[0]) ;
    }

    public static Object[] getMatrix(DataSource dataSource, String sql, Class<?> ... columnTypes) throws SQLException {
    	return getMatrix(dataSource, sql, null, columnTypes) ;
    }
    public static Object[] getMatrix(DataSource dataSource, String sql, TimeZone tz, Class<?> ... columnTypes) throws SQLException {
        Connection conn = null ;
        try {        	
        	conn = dataSource.getConnection() ;
        	return getMatrix(conn, sql, tz, columnTypes) ;
        } finally {
        	if (conn != null) conn.close() ;
        }
    }
    public static Object[] getMatrix(Connection conn, String sql, Class<?> ... columnTypes) throws SQLException {
    	return getMatrix(conn, sql, null, columnTypes) ;
    }
    public static Object[] getMatrix(Connection conn, String sql, TimeZone tz, Class<?> ... columnTypes) throws SQLException {
    	final BuildColumnFromResultSet[] columns = new BuildColumnFromResultSet[columnTypes.length] ;
    	for (int i = 0 ; i != columnTypes.length ; i++) {
    		columns[i] = getResultSetColumnBuilder(columnTypes[i], tz, i) ;
    	}
        Statement stmt = null ;
        ResultSet rs = null ;
        try {
            stmt = conn.createStatement() ;
            rs = stmt.executeQuery(sql) ;
            while (rs.next())
            	for (BuildColumnFromResultSet column : columns)
            		column.fetch(rs) ;
            final Object[] results = new Object[columns.length] ;
            for (int i = 0 ; i != columns.length ; i++) {
            	results[i] = columns[i].getResult() ;
            }
        	return results ;
        } finally {
        	if (rs != null) rs.close() ;
        	if (stmt != null) stmt.close() ;
        }
    }
    
    public static <E, F> Map<E, F> getMap(Connection conn, String sql, Class<E> domain, Class<F> range) throws SQLException {
    	return getMap(conn, sql, domain, range, null) ;
    }
    /**
     * A simple method to safely execute an sql statement and return its first column as a list of type clazz
     * 
     * @param <E>
     * @param dataSource
     * @param sql
     * @param clazz
     * @return
     * @throws SQLException
     */
    public static <E, F> Map<E, F> getMap(Connection conn, String sql, Class<E> domain, Class<F> range, TimeZone tz) throws SQLException {
    	Map<E, F> result = new SerialHashMap<E, F>() ;
    	fillMap(conn, sql, domain, range, tz, result) ;
    	return result ;
    }
    
    public static <E, F> MultiMap<E, F> getMultiMap(Connection conn, String sql, Class<E> domain, Class<F> range) throws SQLException {
    	return getMultiMap(conn, sql, domain, range, null) ;
    }
    /**
     * A simple method to safely execute an sql statement and return its first two columns as a map with provided domain/range
     * 
     * @param <E>
     * @param dataSource
     * @param sql
     * @param clazz
     * @return
     * @throws SQLException
     */
    public static <E, F> MultiMap<E, F> getMultiMap(Connection conn, String sql, Class<E> domain, Class<F> range, TimeZone tz) throws SQLException {
    	MultiMap<E, F> result = new SerialInlineMultiHashMap<E, F>() ;
    	fillMap(conn, sql, domain, range, tz, result) ;
    	return result ;
    }
    
    public static <E, F> Map<E, F> getMap(DataSource dataSource, String sql, Class<E> domain, Class<F> range) throws SQLException {
    	return getMap(dataSource, sql, domain, range, null) ;
    }

    /**
     * A simple method to safely execute an sql statement and return its first column as a list of type clazz
     * 
     * @param <E>
     * @param dataSource
     * @param sql
     * @param clazz
     * @return
     * @throws SQLException
     */
    public static <E, F> Map<E, F> getMap(DataSource dataSource, String sql, Class<E> domain, Class<F> range, TimeZone tz) throws SQLException {
    	Map<E, F> result = new SerialHashMap<E, F>() ;
    	fillMap(dataSource, sql, domain, range, tz, result) ;
    	return result ;
    }
    
    public static <E, F> MultiMap<E, F> getMultiMap(DataSource dataSource, String sql, Class<E> domain, Class<F> range) throws SQLException {
    	return getMultiMap(dataSource, sql, domain, range, null) ;
    }
    
    /**
     * A simple method to safely execute an sql statement and return its first two columns as a map with provided domain/range
     * 
     * @param <E>
     * @param dataSource
     * @param sql
     * @param clazz
     * @return
     * @throws SQLException
     */
    public static <E, F> MultiMap<E, F> getMultiMap(DataSource dataSource, String sql, Class<E> domain, Class<F> range, TimeZone tz) throws SQLException {
    	MultiMap<E, F> result = new SerialInlineMultiHashMap<E, F>() ;
    	fillMap(dataSource, sql, domain, range, tz, result) ;
    	return result ;
    }
    
    public static <E, F> void fillMap(DataSource dataSource, String sql, Class<E> domain, Class<F> range, TimeZone tz, AnyMap<E, F> result) throws SQLException {
        Connection conn = null ;
        try {        	
        	conn = dataSource.getConnection() ;
        	fillMap(conn, sql, domain, range, tz, result) ;
        } finally {
        	if (conn != null) conn.close() ;
        }
    }

    public static <E, F> void fillMap(Connection conn, String sql, Class<E> dom, Class<F> rng, TimeZone tz, AnyMap<E, F> result) throws SQLException {
    	final GetFromResultSet<E> domain = getResultSetItemGetter(dom, tz, 0) ;
    	final GetFromResultSet<F> range = getResultSetItemGetter(rng, tz, 1) ;
    	fillMap(conn, sql, domain, range, result) ;
    }
    
    public static <E, F> void fillMap(Connection conn, String sql, GetFromResultSet<E> domain, GetFromResultSet<F> range, AnyMap<E, F> result) throws SQLException {
    	Statement stmt = null ;
    	ResultSet rs = null ;
    	try {
    		stmt = conn.createStatement() ;
    		rs = stmt.executeQuery(sql) ;
    		while (rs.next()) 
    			result.put(domain.get(rs), range.get(rs)) ;
    	} finally {
    		if (rs != null) rs.close() ;
    		if (stmt != null) stmt.close() ;
    	}
    }
    
    @SuppressWarnings("unchecked")
	public static Object[] lookupColumns(Object[] matrix, int[] cols, Function[] lookups, Class[] ranges) {
    	final Object[] ret = matrix.clone() ;
    	for (int i = 0 ; i != cols.length ; i++) {
    		final Function f = lookups[i] ;
    		final Object[] src = (Object[]) ret[cols[i]] ;    		
    		final Object trg = Array.newInstance(ranges[i], src.length) ;
    		for (int v = 0 ; v != src.length ; v++) {
    			Array.set(trg, v, f.apply(src[v])) ;
    		}
    		ret[cols[i]] = trg ;
    	}
    	return ret ;
    }
    
    @SuppressWarnings("unchecked")
	public static Object[] lookupColumn(Object[] matrix, int col, Function lookup, Class range) {
    	return lookupColumns(matrix, new int[] { col }, new Function[] { lookup } , new Class[] { range }) ;
    }
    
    @SuppressWarnings("unchecked")
	private static final <E> GetFromResultSet<E> getResultSetItemGetter(Class<E> clazz, TimeZone tz, int col) {
		if (clazz.isPrimitive()) {
			throw new IllegalArgumentException("Primitives are not supported for this type of getter") ;
		} else {
			if (clazz == java.lang.String.class) {
				return (GetFromResultSet<E>) new GetStringFromResultSet(col + 1) ;
			} else if (clazz == java.util.Date.class) {
				return (GetFromResultSet<E>) new GetDateFromResultSet(tz, col + 1) ;
//			} else if (clazz == org.joda.time.LocalDate.class) {
//				return (GetFromResultSet<E>) new GetLocalDateFromResultSet(tz, col + 1) ;
//			} else if (clazz == org.joda.time.DateTime.class) {
//				return (GetFromResultSet<E>) new GetDateTimeFromResultSet(tz, col + 1) ;
			} else if (clazz == java.lang.Double.class) {
				return (GetFromResultSet<E>) new GetDoubleFromResultSet(col + 1) ;    				
			} else if (clazz == java.lang.Integer.class) {
				return (GetFromResultSet<E>) new GetIntegerFromResultSet(col + 1) ;
			} else if (clazz == java.lang.Long.class) {
				return (GetFromResultSet<E>) new GetLongFromResultSet(col + 1) ;
			} else if (clazz == java.sql.Timestamp.class) {
				return (GetFromResultSet<E>) new GetTimestampFromResultSet(tz, col + 1) ;
			} else if (clazz == java.sql.Date.class) {
				return (GetFromResultSet<E>) new GetSQLDateFromResultSet(tz, col + 1) ;
			} else if (clazz == java.lang.Boolean.class) {
				return (GetFromResultSet<E>) new GetBooleanFromResultSet(col + 1) ;
			} else if (Enum.class.isAssignableFrom(clazz)) {
				return new GetEnumFromResultSet(col + 1, clazz) ;
			} else {
				return new GetObjectFromResultSet(clazz, col + 1) ;
			}
		}
    }
    
    private static final BuildColumnFromResultSet getResultSetColumnBuilder(Class<?> clazz, TimeZone tz, int col) {
		if (clazz.isPrimitive()) {
			if (clazz == double.class) {
				return new BuildPrimitiveDoubleColumnFromResultSet(col + 1) ;
			} else if (clazz == int.class) {
					return new BuildPrimitiveIntColumnFromResultSet(col + 1) ;
			} else if (clazz == boolean.class) {
				return new BuildPrimitiveBooleanColumnFromResultSet(col + 1) ;
			} else {
				throw new IllegalArgumentException("Only doubles and ints are currently supported as primitives. Implementing more is a copy-paste job though, so feel free...") ;
			}
		} else {
	    	if (clazz == java.util.Date.class) {
				return new BuildDateColumnFromResultSet(tz, col + 1) ;
	    	} else if (clazz == java.lang.String.class) {
				return new BuildStringColumnFromResultSet(col + 1) ;
//			} else if (clazz == org.joda.time.LocalDate.class) {
//				return new BuildLocalDateColumnFromResultSet(tz, col + 1) ;
//			} else if (clazz == org.joda.time.DateTime.class) {
//				return new BuildDateTimeColumnFromResultSet(tz, col + 1) ;
			} else if (clazz == java.lang.Double.class) {
				return new BuildDoubleColumnFromResultSet(col + 1) ;    				
			} else if (clazz == java.lang.Integer.class) {
				return new BuildIntegerColumnFromResultSet(col + 1) ;    				
			} else if (clazz == java.lang.Long.class) {
				return new BuildLongColumnFromResultSet(col + 1) ;    				
			} else if (clazz == java.sql.Timestamp.class) {
				return new BuildTimestampColumnFromResultSet(tz, col + 1) ;
			} else if (clazz == java.sql.Date.class) {
				return new BuildSQLDateColumnFromResultSet(tz, col + 1) ;
			} else if (clazz == java.lang.Boolean.class) {
				return new BuildBooleanColumnFromResultSet(col + 1) ;
			} else if (Enum.class.isAssignableFrom(clazz)) {
				return new BuildEnumColumnFromResultSet(col + 1, clazz) ;
			} else {
				return new BuildObjectColumnFromResultSet(clazz, col + 1) ;
			}
		}
    }
    
	private abstract static class GetFromResultSet<E> {
		private static final long serialVersionUID = -5242654405330263281L ;
		final Class<E> clazz ;
		final int column ;
		GetFromResultSet(Class<E> clazz, int column) {
			this.clazz = clazz ;
			this.column = column ;
		}
		abstract E get(ResultSet rs) throws SQLException ;
	}
	
    private static final class GetDateFromResultSet extends GetFromResultSet<java.util.Date> {
		final Calendar cal ;
		public GetDateFromResultSet(TimeZone tz, int column) {
			super(java.util.Date.class, column) ;
			this.cal = Calendar.getInstance(tz) ;
		}
		public java.util.Date get(ResultSet rs) throws SQLException {
			final Timestamp val = rs.getTimestamp(column, cal) ;
			return new java.util.Date(val.getTime()) ;
		}
	}
	
    private static final class GetBooleanFromResultSet extends GetFromResultSet<java.lang.Boolean> {
    	public GetBooleanFromResultSet(int column) {
    		super(java.lang.Boolean.class, column) ;
    	}
    	public java.lang.Boolean get(ResultSet rs) throws SQLException {
    		return rs.getBoolean(column) ;
    	}
    }
    
    private static final class GetStringFromResultSet extends GetFromResultSet<java.lang.String> {
    	public GetStringFromResultSet(int column) {
    		super(java.lang.String.class, column) ;
    	}
    	public java.lang.String get(ResultSet rs) throws SQLException {
    		return rs.getString(column) ;
    	}
    }
    
    private static final class GetIntegerFromResultSet extends GetFromResultSet<java.lang.Integer> {
    	public GetIntegerFromResultSet(int column) {
    		super(java.lang.Integer.class, column) ;
    	}
    	public java.lang.Integer get(ResultSet rs) throws SQLException {
    		final Integer val = rs.getInt(column) ;
    		return rs.wasNull() ? null : val ;
    	}
    }
    
    private static final class GetLongFromResultSet extends GetFromResultSet<java.lang.Long> {
    	public GetLongFromResultSet(int column) {
    		super(java.lang.Long.class, column) ;
    	}
    	public java.lang.Long get(ResultSet rs) throws SQLException {
    		final Long val = rs.getLong(column) ;
    		return rs.wasNull() ? null : val ;
    	}
    }
    
    private static final class GetDoubleFromResultSet extends GetFromResultSet<java.lang.Double> {
    	public GetDoubleFromResultSet(int column) {
    		super(java.lang.Double.class, column) ;
    	}
    	public java.lang.Double get(ResultSet rs) throws SQLException {
    		final Double val = rs.getDouble(column) ;
    		return rs.wasNull() ? null : val ;
    	}
    }
    
//    private static final class GetLocalDateFromResultSet extends GetFromResultSet<org.joda.time.LocalDate> {
//    	final Calendar cal ;
//    	final DateTimeZone tz ;
//    	public GetLocalDateFromResultSet(TimeZone tz, int column) {
//    		super(org.joda.time.LocalDate.class, column) ;
//    		this.cal = Calendar.getInstance(tz) ;
//    		this.tz = DateTimeZone.forTimeZone(tz) ;
//    	}
//    	public org.joda.time.LocalDate get(ResultSet rs) throws SQLException {
//    		final java.sql.Timestamp val = rs.getTimestamp(column, cal) ;
//    		return val == null ? null : Joda.toLocalDate(val, tz) ;
//    	}
//    }
//    
//    private static final class GetDateTimeFromResultSet extends GetFromResultSet<org.joda.time.DateTime> {
//    	final Calendar cal ;
//    	final DateTimeZone tz ;
//    	public GetDateTimeFromResultSet(TimeZone tz, int column) {
//    		super(org.joda.time.DateTime.class, column) ;
//    		this.cal = Calendar.getInstance(tz) ;
//    		this.tz = DateTimeZone.forTimeZone(tz) ;
//    	}
//    	public org.joda.time.DateTime get(ResultSet rs) throws SQLException {
//    		final java.sql.Timestamp val = rs.getTimestamp(column, cal) ;
//    		return val == null ? null : Joda.toDateTime(val, tz) ;
//    	}
//    }
//    
    private static final class GetTimestampFromResultSet extends GetFromResultSet<java.sql.Timestamp> {
		final Calendar cal ;
		public GetTimestampFromResultSet(TimeZone tz, int column) {
			super(java.sql.Timestamp.class, column) ;
			this.cal = Calendar.getInstance(tz) ;
		}
		public java.sql.Timestamp get(ResultSet rs) throws SQLException {
			return rs.getTimestamp(column, cal) ;
		}
	}
	
	private static final class GetSQLDateFromResultSet extends GetFromResultSet<java.sql.Date> {
		final Calendar cal ;
		public GetSQLDateFromResultSet(TimeZone tz, int column) {
			super(java.sql.Date.class, column) ;
			this.cal = Calendar.getInstance(tz) ;
		}
		public java.sql.Date get(ResultSet rs) throws SQLException {
			return rs.getDate(column, cal) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final class GetEnumFromResultSet extends GetFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final Method m ;
		GetEnumFromResultSet(int column, Class<?> clazz) {
			super(clazz, column) ;
			try {
				m = clazz.getDeclaredMethod("valueOf", String.class) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
		public Object get(ResultSet rs) throws SQLException {
			final String val = rs.getString(column) ;
			try {
				return rs.wasNull() ? null : m.invoke(null, val) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	private static final class GetObjectFromResultSet<D> extends GetFromResultSet<D> {
		public GetObjectFromResultSet(Class<D> clazz, int column) {
			super(clazz, column) ;
		}
		public D get(ResultSet rs) throws SQLException {
			return clazz.cast(rs.getObject(column)) ;
		}
	}

	private abstract static class BuildColumnFromResultSet {
		abstract void fetch(ResultSet rs) throws SQLException ;
		abstract Object getResult() ;
	}
	
	private static final class BuildObjectColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final Class<?> clazz ;
		final int column ;
		Object[] results ;
		int count ;
		BuildObjectColumnFromResultSet(Class<?> clazz, int column) {
			this.clazz = clazz ;
			this.column = column ;
			results = (Object[]) Array.newInstance(clazz, 10) ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = clazz.cast(rs.getObject(column)) ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildDateColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		final Calendar cal ;
		java.util.Date[] results ;
		int count ;
		BuildDateColumnFromResultSet(TimeZone tz, int column) {
			this.column = column ;
			results = new java.util.Date[10] ;
			count = 0 ;
			cal = Calendar.getInstance(tz) ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final Timestamp val = rs.getTimestamp(column, cal) ;
			results[count++] = val == null ? null : new java.util.Date(val.getTime()) ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
//	private static final class BuildLocalDateColumnFromResultSet extends BuildColumnFromResultSet {
//		private static final long serialVersionUID = -5242654405330263281L ;
//		final int column ;
//		final Calendar cal ;
//		LocalDate[] results ;
//		DateTimeZone tz ;
//		int count ;
//		BuildLocalDateColumnFromResultSet(TimeZone tz, int column) {
//			this.column = column ;
//			results = new LocalDate[10] ;
//			count = 0 ;
//			cal = Calendar.getInstance(tz) ;
//			this.tz = DateTimeZone.forTimeZone(tz) ;
//		}
//		void fetch(ResultSet rs) throws SQLException {
//			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
//			final Timestamp val = rs.getTimestamp(column, cal) ;
//			results[count++] = val == null ? null : Joda.toLocalDate(val, tz) ;
//		}
//		@Override
//		Object getResult() {
//			return java.util.Arrays.copyOf(results, count) ;
//		}		
//	}
//	
//	private static final class BuildDateTimeColumnFromResultSet extends BuildColumnFromResultSet {
//		private static final long serialVersionUID = -5242654405330263281L ;
//		final int column ;
//		final Calendar cal ;
//		DateTime[] results ;
//		DateTimeZone tz ;
//		int count ;
//		BuildDateTimeColumnFromResultSet(TimeZone tz, int column) {
//			this.column = column ;
//			results = new DateTime[10] ;
//			count = 0 ;
//			cal = Calendar.getInstance(tz) ;
//			this.tz = DateTimeZone.forTimeZone(tz) ;
//		}
//		void fetch(ResultSet rs) throws SQLException {
//			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
//			final Timestamp val = rs.getTimestamp(column, cal) ;
//			results[count++] = val == null ? null : new DateTime(val, tz) ;
//		}
//		@Override
//		Object getResult() {
//			return java.util.Arrays.copyOf(results, count) ;
//		}		
//	}
//	
	private static final class BuildSQLDateColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		final Calendar cal ;
		java.sql.Date[] results ;
		int count ;
		BuildSQLDateColumnFromResultSet(TimeZone tz, int column) {
			this.column = column ;
			results = new java.sql.Date[10] ;
			count = 0 ;
			cal = Calendar.getInstance(tz) ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getDate(column, cal) ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildTimestampColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		final Calendar cal ;
		Timestamp[] results ;
		int count ;
		BuildTimestampColumnFromResultSet(TimeZone tz, int column) {
			this.column = column ;
			results = new Timestamp[10] ;
			count = 0 ;
			cal = Calendar.getInstance(tz) ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getTimestamp(column, cal) ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildPrimitiveDoubleColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		double[] results ;
		int count ;
		BuildPrimitiveDoubleColumnFromResultSet(int column) {
			this.column = column ;
			results = new double[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final double val = rs.getDouble(column) ;			
			results[count++] = rs.wasNull() ? Double.NaN : val ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildPrimitiveIntColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		int[] results ;
		int count ;
		BuildPrimitiveIntColumnFromResultSet(int column) {
			this.column = column ;
			results = new int[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final int val = rs.getInt(column) ;			
			results[count++] = rs.wasNull() ? Integer.MIN_VALUE : val ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildPrimitiveBooleanColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		boolean[] results ;
		int count ;
		BuildPrimitiveBooleanColumnFromResultSet(int column) {
			this.column = column ;
			results = new boolean[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getBoolean(column) ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildBooleanColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		Boolean[] results ;
		int count ;
		BuildBooleanColumnFromResultSet(int column) {
			this.column = column ;
			results = new Boolean[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			Boolean r = rs.getBoolean(column) ? Boolean.TRUE : Boolean.FALSE ;
			results[count++] = rs.wasNull() ? null : r ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildStringColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		String[] results ;
		int count ;
		BuildStringColumnFromResultSet(int column) {
			this.column = column ;
			results = new String[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getString(column) ;				
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildDoubleColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		Double[] results ;
		int count ;
		BuildDoubleColumnFromResultSet(int column) {
			this.column = column ;
			results = new Double[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final double val = rs.getDouble(column) ;
			results[count++] = rs.wasNull() ? null : val ;				
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildIntegerColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		Integer[] results ;
		int count ;
		BuildIntegerColumnFromResultSet(int column) {
			this.column = column ;
			results = new Integer[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
    		final Integer val = rs.getInt(column) ;
    		results[count++] = rs.wasNull() ? null : val ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildLongColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		Long[] results ;
		int count ;
		BuildLongColumnFromResultSet(int column) {
			this.column = column ;
			results = new Long[10] ;
			count = 0 ;
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final Long val = rs.getLong(column) ;
			results[count++] = rs.wasNull() ? null : val ;
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}		
	}
	
	private static final class BuildEnumColumnFromResultSet extends BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		final int column ;
		Enum<?>[] results ;
		int count ;
		final Method m ;
		BuildEnumColumnFromResultSet(int column, Class<?> clazz) {
			this.column = column ;
			results = (Enum<?>[]) Array.newInstance(clazz, 10) ;
			count = 0 ;
			try {
				m = clazz.getDeclaredMethod("valueOf", String.class) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
		void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final String val = rs.getString(column) ;
			try {
				results[count++] = rs.wasNull() ? null : (Enum<?>) m.invoke(null, val) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
		@Override
		Object getResult() {
			return java.util.Arrays.copyOf(results, count) ;
		}
	}
	
}
