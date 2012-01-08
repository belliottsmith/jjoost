package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class FetchPartedMatrix extends Fetch<Object[][], FetchPartedMatrix> {

	private Class<?>[] columns;
	public FetchPartedMatrix select(Class<?> ... columns) {
		this.columns = columns;
		return this;
	}	
	public Class<?>[] getColumnTypes() {
		return columns;
	}

    private static Func<Object[][]> fPartitionedMatrices(TimeZone tz, int keyCount, Class<?> ... columnTypes) throws SQLException {
    	return new PartitionedMatrix(keyCount, getColumnBuilders(tz, columnTypes)) ;
    }
    private static final class PartitionedMatrix implements Func<Object[][]> {
    	final int keyCount ;
    	final Func<BuildColumnFromResultSet[]> columns ;
    	public PartitionedMatrix(int keyCount, Func<BuildColumnFromResultSet[]> columns) {
    		this.keyCount = keyCount ;
    		this.columns = columns;
    	}
    	@Override
    	public Object[][] run(ResultSet rs) throws SQLException {
    		final List<Object[]> mxs = new ArrayList<Object[]>() ;
    		final int keyCount = this.keyCount ;
    		final BuildColumnFromResultSet[] columns = this.columns.run(rs) ;
    		Object[] lastKey = null ;
    		Object[] nextKey = new Object[keyCount] ;
    		while (rs.next()) {
    			for (int i = 0 ; i != keyCount ; i++) {
    				nextKey[i] = columns[i].get(rs) ;
    			}
    			if (lastKey == null || !java.util.Arrays.equals(lastKey, nextKey)) {
    				if (lastKey != null) {
        	    		final Object[] mx = new Object[columns.length] ;
        	    		for (int i = 0 ; i != columns.length ; i++) {
        	    			mx[i] = columns[i].getResult() ;
        	    		}
        	    		mxs.add(mx) ;
    				} else {
    					lastKey = new Object[keyCount] ;
    				}
    	    		final Object[] tmp = lastKey ;
    	    		lastKey = nextKey ;
    	    		nextKey = tmp ;
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
    			mxs.add(mx) ;
    		}
    		return mxs.toArray(new Object[mxs.size()][]) ;
    	}
    }
    
    private int keyCount;
    public FetchPartedMatrix partBy(int keyCount) {
    	this.keyCount = keyCount;
    	return this;
    }
    
	protected Func<Object[][]> get() throws SQLException {
		return fPartitionedMatrices(timeZone, keyCount, getColumnTypes());
	}
	
	static FetchPartedMatrix build() {
		return new FetchPartedMatrix();
	}

}
