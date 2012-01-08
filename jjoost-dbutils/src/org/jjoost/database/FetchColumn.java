package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

public class FetchColumn<E> extends Fetch<E[], FetchColumn<E>> {

	private Class<E> type;
	public <NE> FetchColumn<NE> select(Class<NE> type) {
		@SuppressWarnings("unchecked")
		final FetchColumn<NE> r = (FetchColumn<NE>) this;
		r.type = type;
		return r;
	}
	
	public Class<E> getColumnType() {
		return type;
	}

    private static <E> Func<E[]> fColumn(TimeZone tz, Class<? extends E> columnType) throws SQLException {
    	return new Column<E>(getResultSetColumnBuilder(columnType, tz, 0)) ;
    }
    private static final class Column<E> implements Func<E[]> {
		final BuildColumnFromResultSet column ;
		public Column(BuildColumnFromResultSet column) {
			this.column = column;
		}
		@Override
	    @SuppressWarnings("unchecked")
		public E[] run(ResultSet rs) throws SQLException {
			init(column, rs) ;
	    	while (rs.next()) {
	    		column.fetch(rs) ;
	    	}
	    	return (E[]) column.getResult() ;
		}
    }
    
	@Override
	protected Func<E[]> get() throws SQLException {
		return fColumn(timeZone, type);
	}
	
	static FetchColumn<?> build() {
		return new FetchColumn<Object>();
	}

}
