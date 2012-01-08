package org.jjoost.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

public class FetchScalar<E> extends Fetch<E, FetchScalar<E>> {

	private Class<E> type;
	public <NE> FetchScalar<NE> select(Class<NE> type) {
		@SuppressWarnings("unchecked")
		final FetchScalar<NE> r = (FetchScalar<NE>) this;
		r.type = type;
		return r;
	}
	
	public Class<E> getColumnType() {
		return type;
	}

    private static <E> Func<E> fScalar(TimeZone tz, Class<? extends E> type) throws SQLException {
    	return new Scalar<E>(getResultSetItemGetter(type, tz, 0)) ;
    }
    private static final class Scalar<E> implements Func<E> {
    	final GetFromResultSet<? extends E> get ;
    	public Scalar(GetFromResultSet<? extends E> get) {
			this.get = get;
		}
		@Override
    	public E run(ResultSet rs) throws SQLException {
			if (!rs.next()) {
				return null ;
			}
    		return get.get(rs) ;
    	}
    }
    
	@Override
	protected Func<E> get() throws SQLException {
		return fScalar(timeZone, type);
	}

	static FetchScalar<?> build() {
		return new FetchScalar<Object>();
	}

}
