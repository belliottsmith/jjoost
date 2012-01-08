package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TimeZone;

public class FetchList<E> extends Fetch<List<E>, FetchList<E>> {

	private Class<E> type;
	public <NE> FetchList<NE> select(Class<NE> type) {
		@SuppressWarnings("unchecked")
		final FetchList<NE> r = (FetchList<NE>) this;
		r.type = type;
		return r;
	}
	
	public Class<E> getColumnType() {
		return type;
	}

    private static <E> Func<List<E>> fList(TimeZone tz, Class<? extends E> columnType) throws SQLException {
    	return new FList<E>(getResultSetColumnBuilder(columnType, tz, 0)) ;
    }
    private static final class FList<E> implements Func<List<E>> {
		final BuildColumnFromResultSet column ;
		public FList(BuildColumnFromResultSet column) {
			this.column = column;
		}
		@Override
	    @SuppressWarnings("unchecked")
		public List<E> run(ResultSet rs) throws SQLException {
			init(column, rs) ;
	    	while (rs.next()) {
	    		column.fetch(rs) ;
	    	}
	    	return java.util.Arrays.asList((E[]) column.getResult()) ;
		}
    }
    
	@Override
	protected Func<List<E>> get() throws SQLException {
		return fList(timeZone, type);
	}

	static FetchList<?> build() {
		return new FetchList<Object>();
	}

}
