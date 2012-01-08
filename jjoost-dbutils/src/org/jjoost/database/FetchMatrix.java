package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

public class FetchMatrix extends Fetch<Object[], FetchMatrix> {

	private Class<?>[] columns;
	public FetchMatrix select(Class<?> ... columns) {
		this.columns = columns;
		return this;
	}
	
	public Class<?>[] getColumnTypes() {
		return columns;
	}

    private static Func<Object[]> fMatrix(TimeZone tz, Class<?> ... columnTypes) throws SQLException {
    	return new Matrix(getColumnBuilders(tz, columnTypes)) ;
    }
    
    private static Func<Object[]> fMatrix(TimeZone tz) throws SQLException {
    	return new Matrix(getColumnBuilders(tz)) ;
    }
    
    protected static final class Matrix implements Func<Object[]> {
    	final Func<BuildColumnFromResultSet[]> columns ;
		public Matrix(Func<BuildColumnFromResultSet[]> columns) {
			this.columns = columns;
		}
		@Override
		public Object[] run(ResultSet rs) throws SQLException {
			final BuildColumnFromResultSet[] columns = this.columns.run(rs) ;
	    	while (rs.next()) {
	    		for (BuildColumnFromResultSet column : columns) {
	    			column.fetch(rs) ;
	    		}
	    	}
	    	final Object[] results = new Object[columns.length] ;
	    	for (int i = 0 ; i != columns.length ; i++) {
	    		results[i] = columns[i].getResult() ;
	    	}
	    	return results ;
		}
    }

	protected Func<Object[]> get() throws SQLException {
		final Class<?>[] columns = getColumnTypes();
		if (columns != null) {
			return fMatrix(timeZone, columns);
		} else {
			return fMatrix(timeZone);
		}
	}

	static FetchMatrix build() {
		return new FetchMatrix();
	}
	
}
