package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.TimeZone;

public class FetchLabeledMatrix extends Fetch<LinkedHashMap<String, Object>, FetchLabeledMatrix> {

	private Class<?>[] types;
	public FetchLabeledMatrix select(Class<?> ... types) {
		this.types = types;
		return this;
	}
	
	public Class<?>[] getColumnTypes() {
		return types;
	}

    private static Func<LinkedHashMap<String, Object>> fColumnLookup(TimeZone tz, Class<?> ... columnTypes) throws SQLException {
    	return new ColumnLookup(getColumnBuilders(tz, columnTypes)) ;
    }
    private static Func<LinkedHashMap<String, Object>> fColumnLookup(TimeZone tz) throws SQLException {
    	return new ColumnLookup(getColumnBuilders(tz)) ;
    }
    private static final class ColumnLookup implements Func<LinkedHashMap<String, Object>> {
    	final Func<BuildColumnFromResultSet[]> columns ;
		public ColumnLookup(Func<BuildColumnFromResultSet[]> columns) {
			this.columns = columns;
		}
		@Override
		public LinkedHashMap<String, Object> run(ResultSet rs) throws SQLException {
			final BuildColumnFromResultSet[] columns = this.columns.run(rs) ;
	    	final ResultSetMetaData md = rs.getMetaData() ;
	    	while (rs.next()) {
	    		for (BuildColumnFromResultSet column : columns) {
	    			column.fetch(rs) ;
	    		}
	    	}
	    	final LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>() ;
	    	for (int i = 0 ; i != columns.length ; i++) {
	    		results.put(md.getColumnLabel(i + 1), columns[i].getResult()) ;
	    	}
	    	return results ;
		}
    }
    
	@Override
	protected Func<LinkedHashMap<String, Object>> get() throws SQLException {		
		return types == null ? fColumnLookup(timeZone) : fColumnLookup(timeZone, types);
	}

	static FetchLabeledMatrix build() {
		return new FetchLabeledMatrix();
	}

}
