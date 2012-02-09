package org.jjoost.database;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.jjoost.collections.Set;
import org.jjoost.collections.iters.MappedIterable;
import org.jjoost.collections.iters.OnceIterable;
import org.jjoost.collections.sets.serial.Intern;
import org.jjoost.collections.sets.serial.SerialHashSet;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters;
//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;
//import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public abstract class Fetch<R, F extends Fetch<R, F>> {

	public static FetchScalar<?> scalar() {
		return FetchScalar.build();
	}
	
	public static FetchColumn<?> column() {
		return FetchColumn.build();
	}
	
	public static FetchLabeledMatrix labeledMatrix() {
		return FetchLabeledMatrix.build();
	}
	
	public static FetchHashMap<?, ?, ?> hashMap() {
		return FetchHashMap.build();
	}
	
	public static FetchList<?> list() {
		return FetchList.build();
	}
	
	public static FetchMatrix matrix() {
		return FetchMatrix.build();
	}
	
	public static FetchPartedMatrix partedMatrix() {
		return FetchPartedMatrix.build();
	}
	
	public static FetchMatrixLookup.Type0<?, Object[], Object[]> matrixLookup() {
		return FetchMatrixLookup.plain();
	}
	
	public static FetchMatrixLookup.Type0<?, LinkedHashMap<String, Object>, LinkedHashMap<String, Object>> labeledMatrixLookup() {
		return FetchMatrixLookup.labeled();
	}
	
	public static FetchObjects<?> objects() {
		return FetchObjects.build();
	}
	
//	public static FetchOrderedMap<?, ?, ?> orderedMap() {
//		return FetchOrderedMap.build();
//	}
//	
	public static FetchRowLookup.Type0<?, Object, Object[]> rowLookup() {
		return FetchRowLookup.build();
	}
	
//	protected static final ExecutorService EXEC = new ThreadPoolExecutor(0, 64, 5L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), Threads.getThreadFactory("Database-Background-Worker", true)) ;
//	protected static final ScheduledExecutorService CANCEL = new ScheduledThreadPoolExecutor(1, Threads.getThreadFactory("Database-Timeout-Scheduler", true)) ;

//	protected static final Logger LOG = Logger.getLogger();
	
	protected long timeoutMillis;
	protected DataSource datasource;
	protected Connection connection;
	protected TimeZone timeZone;
	private int retriesOnDeadlock = -1;
	private int backoffOnDeadlock = -1;
	private boolean abortOnFailure;
	private int runAtOnce;
	private int resultQueueCap;

	protected Fetch() { }
	protected Fetch(Fetch<?, ?> copy) { 
		this.timeoutMillis = copy.timeoutMillis;
		this.datasource = copy.datasource;
		this.connection = copy.connection;
		this.timeZone = copy.timeZone;
		this.retriesOnDeadlock = copy.retriesOnDeadlock;
		this.backoffOnDeadlock = copy.backoffOnDeadlock;
		this.abortOnFailure = copy.abortOnFailure;
		this.runAtOnce = copy.runAtOnce;
		this.resultQueueCap = copy.resultQueueCap;
	}	
	
	public long getTimeoutMillis() {
		return timeoutMillis;
	}
	
	@SuppressWarnings("unchecked")
	public F withTimeout(long time, TimeUnit units) {
		timeoutMillis = units.toMillis(time);
		return (F) this;
	}

	public DataSource getDatasource() {
		return datasource;
	}

	@SuppressWarnings("unchecked")
	public F from(DataSource datasource) {
		if (retriesOnDeadlock < 0 && backoffOnDeadlock < 0) {
			retriesOnDeadlock = 3;
			backoffOnDeadlock = 10;
		}
		this.datasource = datasource;
		return (F) this;
	}

	public Connection getConnection() {
		return connection;
	}

	@SuppressWarnings("unchecked")
	public F from(Connection connection) {
		this.connection = connection;
		return (F) this;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	@SuppressWarnings("unchecked")
	public F withTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		return (F) this;
	}
	
	@SuppressWarnings("unchecked")
	public F withDeadlockRetries(int count, int backoff) {
		this.retriesOnDeadlock = count;
		this.backoffOnDeadlock = backoff;
		return (F) this;
	}

	@SuppressWarnings("unchecked")
	public F withAbortOnFailure(boolean on) {
		this.abortOnFailure = on;
		return (F) this;
	}
	
	@SuppressWarnings("unchecked")
	public F withMaxAsync(int count) {
		this.runAtOnce = count;
		return (F) this;
	}
	
	@SuppressWarnings("unchecked")
	public F withMaxResultsWaiting(int count) {
		this.resultQueueCap = count;
		return (F) this;
	}
	
	public R run(String sql) throws SQLException {
		if (connection != null) {
			return get(connection, sql, get(), retriesOnDeadlock, backoffOnDeadlock).run();
		} else {
			return get(datasource, sql, get(), retriesOnDeadlock, backoffOnDeadlock).run();
		}
	}
	
//	public Future<R> runAsync(String sql) throws SQLException {
//		if (connection != null) {
//			return async(connection, sql, get(), retriesOnDeadlock, backoffOnDeadlock, timeoutMillis, TimeUnit.MILLISECONDS);
//		} else {
//			return async(datasource, sql, get(), retriesOnDeadlock, backoffOnDeadlock, timeoutMillis, TimeUnit.MILLISECONDS);
//		}
//	}
	
	public Iterable<R> run(Iterable<String> sql) throws SQLException {
		return new MappedIterable<Cncl<R>, R>(
				connection != null 
					? get(connection, sql, get(), retriesOnDeadlock, backoffOnDeadlock)
					: get(datasource, sql, get(), retriesOnDeadlock, backoffOnDeadlock), 
				new Function<Cncl<R>, R>() {
					private static final long serialVersionUID = 5499146850271933194L;
					@Override
					public R apply(Cncl<R> v) {
						try {
							return v.run();
						} catch (SQLException e) {
							throw new UndeclaredThrowableException(e);
						}
					}
				});
	}
	
//	public Iterable<SQLTask<R>> runAsync(Iterable<String> sql) throws SQLException {
//		if (connection != null) {
//			return async(connection, sql, get(), retriesOnDeadlock, backoffOnDeadlock, abortOnFailure, runAtOnce, resultQueueCap, timeoutMillis, TimeUnit.MILLISECONDS);
//		} else {
//			return async(datasource, sql, get(), retriesOnDeadlock, backoffOnDeadlock, abortOnFailure, runAtOnce, resultQueueCap, timeoutMillis, TimeUnit.MILLISECONDS);
//		}	
//	}
//	
	protected abstract Func<R> get() throws SQLException;
	
	public static abstract class SQLTask<E> implements Future<E> {
		private final Runnable cancelRemaining ;
		private final String query ;
		private final int positionOfQuery ;
		SQLTask(Runnable cancelRemaining, String query, int positionOfQuery) {
			this.query = query;
			this.positionOfQuery = positionOfQuery;
			this.cancelRemaining = cancelRemaining ;
		}		
		public final String query() {
			return query ;
		}
		public final int positionOfQuery() {
			return positionOfQuery ;
		}
		public final void cancelRemaining() {
			cancelRemaining.run() ;
		}
		@Override
		public final boolean cancel(boolean flag) {
			return false ;
		}
		@Override
		public final E get(long l, TimeUnit timeunit) throws InterruptedException, ExecutionException, TimeoutException {
			return get() ;
		}
		public abstract boolean success() ;
	}
	
	protected static final class SQLTaskSuccess<E> extends SQLTask<E> {
		private final E result ;
		public SQLTaskSuccess(Runnable cancelRemaining, String query, int positionOfQuery, E result) {
			super(cancelRemaining, query, positionOfQuery) ;
			this.result = result;
		}
		@Override
		public E get() throws InterruptedException, ExecutionException {
			return result ;
		}
		@Override
		public final boolean isCancelled() {
			return false ;
		}
		@Override
		public boolean isDone() {
			return true ;
		}		
		public boolean success() {
			return true ;
		}
	}
	
	protected static final class SQLTaskFailure<E> extends SQLTask<E> {
		private final Throwable result ;
		public SQLTaskFailure(Runnable cancelRemaining, String query, int positionOfQuery, Throwable result) {
			super(cancelRemaining, query, positionOfQuery) ;
			this.result = result ;
		}
		@Override
		public E get() throws InterruptedException, ExecutionException {
			throw new ExecutionException(result) ;
		}
		@Override
		public final boolean isCancelled() {
			return false ;
		}
		@Override
		public boolean isDone() {
			return true ;
		}		
		public boolean success() {
			return false ;
		}
	}
	
	protected static final class SQLTaskCancelled<E> extends SQLTask<E> {
		public SQLTaskCancelled(Runnable cancelRemaining, String query, int positionOfQuery) {
			super(cancelRemaining, query, positionOfQuery) ;
		}
		@Override
		public E get() throws InterruptedException, ExecutionException {
			throw new CancellationException() ;
		}
		@Override
		public final boolean isCancelled() {
			return true ;
		}
		@Override
		public boolean isDone() {
			return false ;
		}		
		public boolean success() {
			return false ;
		}
	}
	
    protected static interface Cncl<E> {
    	public E run() throws SQLException ;
    	public boolean cancel() throws SQLException ;
    	public boolean cancelled() ;
    }
    
    protected static final class Constant<E> implements Func<E> {
    	final E self ;
		protected Constant(E self) {
			this.self = self;
		}
		@Override
		public E run(ResultSet rs) throws SQLException {
			return self ;
		}    	
    }
    
    protected static interface Func<E> {
    	public E run(ResultSet rs) throws SQLException ;
    }
    
    protected static interface PrepAct<E extends Statement> {
    	public E prep(Connection conn) throws SQLException ;
    }
    
    protected static interface Act<P extends Statement, A, R> {
    	public R run(P stmt, A args) throws SQLException ;
    }
    
    protected static final class StmtPrep implements PrepAct<Statement> {
    	protected static final StmtPrep INST = new StmtPrep() ;
    	@Override
    	public Statement prep(Connection conn) throws SQLException {
    		return conn.createStatement() ;
    	}
    }
    
    protected static final class CallStmtPrep implements PrepAct<CallableStatement> {
    	private final String call ;
    	public CallStmtPrep(String call) {
			this.call = call;
		}
		@Override
    	public CallableStatement prep(Connection conn) throws SQLException {
    		return conn.prepareCall(call) ;
    	}
    }
    
    protected static final class ExecAct implements Act<Statement, String, Void> {
    	protected static final ExecAct INST = new ExecAct() ;
    	@Override
    	public Void run(Statement stmt, String sql) throws SQLException {
//			LOG.trace("Executing %s", sql) ;
    		stmt.execute(sql) ;
    		return null ;
    	}
    }
    
    protected static final class ExecUpdateAct implements Act<Statement, String, Integer> {
    	protected static final ExecUpdateAct INST = new ExecUpdateAct() ;
		@Override
		public Integer run(Statement stmt, String sql) throws SQLException {
//			LOG.trace("Executing %s", sql) ;
			return stmt.executeUpdate(sql) ;
		}
    }
    
//    protected static <E> Future<E> async(DataSource dataSource, String sql, Func<E> f, int retriesOnDeadlock, int backoffOnDeadlock, long timeout, TimeUnit units) throws SQLException {
//    	return async(get(dataSource, sql, f, retriesOnDeadlock, backoffOnDeadlock), timeout, units) ;
//    }
//    
//    protected static <E> Future<E> async(Connection conn, String sql, Func<E> f, int retriesOnDeadlock, int backoffOnDeadlock, long timeout, TimeUnit units) throws SQLException {
//    	return async(get(conn, sql, f, retriesOnDeadlock, backoffOnDeadlock), timeout, units) ;
//    }
    
//    protected static <E> Iterable<SQLTask<E>> async(DataSource dataSource, Iterable<String> sqls, Func<E> f, int retriesOnDeadlock, int backoffOnDeadlock, boolean abortOnFailure, int atOnce, int resultQueueCap, long timeout, TimeUnit units) throws SQLException {    	
//    	final List<String> sql = Iters.toList(sqls) ; 
//    	return async(get(dataSource, sql, f, retriesOnDeadlock, backoffOnDeadlock), sql, abortOnFailure, atOnce, resultQueueCap, timeout, units) ;
//    }
//    
//    protected static <E> Iterable<SQLTask<E>> async(Connection conn, Iterable<String> sqls, Func<E> f, int retriesOnDeadlock, int backoffOnDeadlock, boolean abortOnFailure, int atOnce, int resultQueueCap, long timeout, TimeUnit units) throws SQLException {
//    	final List<String> sql = Iters.toList(sqls) ; 
//    	return async(get(conn, sql, f, retriesOnDeadlock, backoffOnDeadlock), sql, abortOnFailure, atOnce, resultQueueCap, timeout, units) ;
//    }
//    
//    protected static <E> Future<E> async(final Cncl<E> task, final long timeout, final TimeUnit units) {
//    	final ScheduledFuture<?> cancelTask = timeout <= 0 ? null : CANCEL.schedule(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					task.cancel() ;
//				} catch (SQLException e) {
//				}
//			}
//    	}, timeout, units) ;    	
//    	return new CnclFuture<E>(EXEC.submit(new Callable<E>() {
//			@Override
//			public E call() throws Exception {
//				final E r = task.run() ;
//				if (cancelTask != null) {
//					cancelTask.cancel(false) ;
//				}
//				return r ;
//			}
//    	}), task, cancelTask) ;
//    }
    
//    protected static <E> Iterable<SQLTask<E>> async(final Iterable<Cncl<E>> coreTasks, final List<String> sql, final boolean abortOnFailure, final int atOnce, final int resultQueueCap, final long timeout, final TimeUnit units) {
//    	final Queue<Cncl<SQLTask<E>>> work = new LockFreeNoDelayQueue<Cncl<SQLTask<E>>>() ;
//    	final AtomicReference<Future<?>> cancelTaskRef = new AtomicReference<Future<?>>() ;
//    	final Queue<SQLTask<E>> results = new LockFreeNoDelayQueue<SQLTask<E>>() ;
//    	final Runnable cancel = new Runnable() {
//    		final QueueView<Cncl<SQLTask<E>>> cancel = work.view() ;
//    		@Override
//    		public void run() {    			
//				for (Cncl<SQLTask<E>> task : cancel) {
//	    			try { task.cancel() ; } catch (SQLException e) { }
//				}
//				if (cancelTaskRef.get() != null) {
//					cancelTaskRef.get().cancel(false) ;
//				}
//    		}
//    	} ;
//    	final ScheduledFuture<?> cancelTask = timeout <= 0 ? null : CANCEL.schedule(cancel, timeout, units) ;
//    	cancelTaskRef.set(cancelTask) ;
//    	int count = 0 ;
//    	for (final Cncl<E> task : coreTasks) {
//    		final String query = sql.get(count) ;
//    		final int position = count ;
//        	work.add(new Cncl<SQLTask<E>>() {
//        		@Override
//        		public boolean cancelled() {
//        			return task.cancelled() ;
//        		}
//    			@Override
//    			public boolean cancel() throws SQLException {
//    				return task.cancel() ;
//    			}
//    			@Override
//    			public SQLTask<E> run() throws SQLException {
//    				if (task.cancelled()) {
//    					return new SQLTaskCancelled<E>(cancel, query, position) ;
//    				}
//    				try {
//    					final E r = task.run() ;
//    					return new SQLTaskSuccess<E>(cancel, query, position, r) ;
//    				} catch (Throwable t) {
//    					if (task.cancelled()) {
//    						return new SQLTaskCancelled<E>(cancel, query, position) ;
//    					} else {
//    						return new SQLTaskFailure<E>(cancel, query, position, t) ;
//    					}
//    				}
//    			}
//    		}) ;
//    		count++ ;
//    	}
//    	final AtomicReference<Throwable> catastrophe = new AtomicReference<Throwable>() ;
//    	final AtomicInteger remaining = new AtomicInteger(count) ;
//    	final Semaphore guard = new Semaphore(resultQueueCap) ;
//    	for (int i = 0 ; i != atOnce ; i++) {
//    		chain(remaining, guard, work, results, cancel, abortOnFailure, catastrophe) ;
//    	}
//    	return new OnceIterable<SQLTask<E>>(new Iterator<SQLTask<E>>() {
//			@Override public boolean hasNext() { return remaining.get() > 0 || !results.isEmpty() ; }
//			@Override public void remove() { throw new UnsupportedOperationException() ; }    		
//			@Override 
//			public SQLTask<E> next() {
//				try {
//					while (true) {
//						if (catastrophe.get() != null){
//							throw new UndeclaredThrowableException(catastrophe.get()) ;
//						} else if (remaining.get() == 0) {
//							if (results.isEmpty()) {
//								throw new NoSuchElementException() ;
//							} else {
//								return results.poll() ;
//							}
//						} else {
//							final SQLTask<E> r = results.poll(Long.MAX_VALUE) ;
//							if (r != null) {
//								guard.release() ;
//								return r ;
//							}
//						}
//					}
//				} catch (InterruptedException e) {
//					cancel.run() ;
//					throw new UndeclaredThrowableException(e, "Interrupted - cancelling tasks") ;
//				}
//			}
//    	}) ;
//    }
//    
//    protected static <E> void chain(final AtomicInteger remaining, final Semaphore guard, final QueueView<Cncl<SQLTask<E>>> work, final Queue<SQLTask<E>> results, final Runnable cancel, final boolean abortOnFailure, final AtomicReference<Throwable> catastrophe) {
//    	final Cncl<SQLTask<E>> task = work.poll() ;
//    	if (task != null) {
//        	EXEC.execute(new Runnable() {
//        		@Override
//        		public void run() {
//        			guard.acquireUninterruptibly() ;
//        			try {        	
//        				final SQLTask<E> result = task.run() ;
//        				remaining.decrementAndGet() ;
//        				results.add(result) ;        				
//        				if (abortOnFailure && !result.success() && !result.isCancelled()) {
//        					cancel.run() ;
//        				}
//        				chain(remaining, guard, work, results, cancel, abortOnFailure, catastrophe) ;
//        			} catch (Throwable t) {
//        				catastrophe.compareAndSet(null, t) ;
//        				results.add(null) ;
//        				cancel.run() ;
//        			}
//        		}
//        	}) ;
//    	}
//    }
//    
    protected static class CnclFuture<E> implements Future<E> {
    	final Future<E> wrapped ;
    	final Cncl<?> cncl ;
    	final Future<?> cancelTask ;
    	public CnclFuture(Future<E> wrapped, Cncl<?> cncl, Future<?> cancelTask) {
    		this.wrapped = wrapped ;
    		this.cncl = cncl ;
    		this.cancelTask = cancelTask ;
    	}		
    	public boolean cancel(boolean flag) {
    		cancelTask.cancel(false) ;
    		if (!flag) {
    			return wrapped.cancel(false) ;
    		} else {
    			try {
    				wrapped.cancel(true) ;
    				cncl.cancel() ;
    				return true ;
    			} catch (Exception e) {
    				return false ;
    			}
    		}
    	}
    	public E get() throws InterruptedException, ExecutionException {
    		return wrapped.get();
    	}
    	public E get(long l, TimeUnit timeunit) throws InterruptedException, ExecutionException, TimeoutException {
    		return wrapped.get(l, timeunit);
    	}
    	public boolean isCancelled() {
    		return wrapped.isCancelled();
    	}
    	public boolean isDone() {
    		return wrapped.isDone();
    	}
    	
    }
    
    protected static <P extends Statement, A, R> Cncl<R> get(final Connection conn, final A arg, final PrepAct<P> prep, final Act<? super P, ? super A, ? extends R> act) throws SQLException {
    	final P stmt = prep.prep(conn) ;
    	return new Cncl<R>() {
    		final AtomicReference<Boolean> cancelled = new AtomicReference<Boolean>() ;
    		@Override
    		public boolean cancel() throws SQLException {
    			if (cancelled.compareAndSet(null, Boolean.TRUE)) {
   					stmt.cancel() ;
   					return true ;
    			}
    			return false ;
    		}
    		@Override
    		public R run() throws SQLException {
    			try {
        			final R r ;
        			try {
        				r = act.run(stmt, arg) ;
        			} catch (Throwable t) {
        				tidyAndThrow(stmt, t) ;
        				throw new IllegalStateException() ;
        			}
        			stmt.close() ;
        			return r ;
    			} finally {
    				cancelled.compareAndSet(null, Boolean.FALSE) ;
    			}
    		}
			@Override
			public boolean cancelled() {
				return cancelled.get() == Boolean.TRUE ;
			}    		
    	} ;
    }
    protected static <P extends Statement, A, R> Cncl<R> get(final DataSource dataSource, final A arg, final PrepAct<P> prep, final Act<? super P, ? super A, ? extends R> act) throws SQLException {
    	final Connection conn = dataSource.getConnection() ;
    	final Cncl<R> wrapped = get(conn, arg, prep, act) ;
    	return new Cncl<R>() {
    		public boolean cancelled() {
    			return wrapped.cancelled() ;
    		}
    		@Override
    		public boolean cancel() throws SQLException {
    			final boolean cancelled = wrapped.cancel() ;
    			if (cancelled) {
    				conn.close() ;
    				return true ;
    			}
    			return false ;
    		}
    		@Override
    		public R run() throws SQLException {
    			final R r ;
    			try {
    				r = wrapped.run() ;
    			} catch (Throwable t) {
    				tidyAndThrow(conn, t) ;
    				throw new IllegalStateException() ;
    			}
    			conn.close() ;
    			return r ;
    		}
    	} ;
    }
    
    protected static abstract class CnclGet<E> implements Cncl<E> {
    	
    	final int retriesOnDeadlock;
    	final int backoffOnDeadlock;
    	volatile Boolean cancelled ;
		private CnclGet(int retriesOnDeadlock, int backoffOnDeadlock) {
			this.retriesOnDeadlock = retriesOnDeadlock;
			this.backoffOnDeadlock = backoffOnDeadlock;
		}

		@Override
		public synchronized boolean cancel() throws SQLException {
			if (cancelled == null) {
				_cancel();
				cancelled = Boolean.TRUE ;
				return true ;
			}
			return false ;
		}
		
		public boolean cancelled() {
			return cancelled == Boolean.TRUE ;
		}
		
		abstract void _cancel() throws SQLException;
		abstract void _init() throws SQLException;
		abstract void _tidyAndThrow(Throwable t) throws SQLException;
		abstract E _run() throws SQLException;
		
		@Override
		public E run() throws SQLException {
			_init();
			E r ;
		   	int tries = 0 ;
		   	int wait = 0 ;
		   	while (true) {
		   		try {
		   			r = _run() ;
		   			break ;
		   		} catch (SQLException e) {
					if (e.getMessage().toLowerCase().contains("deadlock victim")) {
		   				if (tries >= retriesOnDeadlock) {
		   					throw new SQLException("Was the victim of too many deadlocks", e) ;
		   				} else {
		       				tries++ ;
		       				wait += backoffOnDeadlock;
		       				try {
		   						Thread.sleep(wait * 1000) ;
		   					} catch (InterruptedException _) {
		   						throw new SQLException("Thread was the victim of a deadlock exception, and was interrupted whilst waiting to retry", e) ;
		   					}
		   					continue ;
		   				}
		   			} else {
		   				_tidyAndThrow(e);
		   			}
		   		} catch (Throwable t) {
		   			_tidyAndThrow(t) ;
		   			throw new IllegalStateException() ;
		   		} finally {
		   			synchronized (this) {
		   				if (cancelled == null) {
		   					cancelled = Boolean.FALSE ;
		   				}
		   			}
		   		}
		   	}
			return r ;
		}
    }
    
    protected static <E> Iterable<Cncl<E>> get(final Connection conn, final Iterable<String> sqls, final Func<E> f, final int retriesOnDeadlock, final int backoffOnDeadlock) throws SQLException {
    	final List<Cncl<E>> tasks = new ArrayList<Cncl<E>>() ;
    	for (String sql : sqls) {
    		final Cncl<E> task = get(conn, sql, f, retriesOnDeadlock, backoffOnDeadlock) ;
    		tasks.add(task) ;
    	}
    	return tasks ;
    }
    
    protected static <E> Cncl<E> get(final Connection conn, final String sql, final Func<E> f, final int retriesOnDeadlock, final int backoffOnDeadlock) throws SQLException {
    	return new CnclGet<E>(backoffOnDeadlock, backoffOnDeadlock) {
    		volatile Statement stmt;
        	volatile ResultSet rs;
			@Override
			synchronized void _init() throws SQLException {
				if (cancelled != null) {
					throw new SQLException("Task was cancelled") ;
				}
				stmt = conn.createStatement();
			}

			@Override
			void _tidyAndThrow(Throwable t) throws SQLException {
				tidyAndThrow(rs, stmt, t);
			}

			@Override
			E _run() throws SQLException {
				rs = stmt.executeQuery(sql);
				final E r = f.run(rs);
				if (stmt != null) {
					 // if we have cancelled the statement, we could have set stmt to null and still successfully completed the query (depending on promptness of stmt.cancel()), so guard against null stmt
					stmt.close();
					stmt = null; // should not be able to get a deadlock exception after here, so run() should not be called with stmt as null
				}
				rs.close();
				rs = null;
				return r;
			}

			@Override
			void _cancel() throws SQLException {
				if (stmt != null) {
					stmt.cancel();
					stmt = null;
				}
			}
    	} ;
    }
    
    protected static <E> Iterable<Cncl<E>> get(final DataSource dataSource, final Iterable<String> sqls, final Func<E> f, int retriesOnDeadlock, int backoffOnDeadlock) throws SQLException {
    	final List<Cncl<E>> tasks = new ArrayList<Cncl<E>>() ;
    	for (String sql : sqls) {
    		tasks.add(get(dataSource, sql, f, retriesOnDeadlock, backoffOnDeadlock)) ;
    	}
    	return tasks ;
    }
    protected static <E> Cncl<E> get(final DataSource dataSource, final String sql, final Func<E> f, final int retriesOnDeadlock, final int backoffOnDeadlock) throws SQLException {
    	return new CnclGet<E>(backoffOnDeadlock, backoffOnDeadlock) {
        	Connection conn ;
        	volatile Cncl<E> cur ;
			@Override
			synchronized void _init() throws SQLException {
				if (cancelled != null) {
					throw new SQLException("Task was cancelled") ;
				}
				conn = dataSource.getConnection() ;
				cur = get(conn, sql, f, -1, -1);
			}

			@Override
			void _tidyAndThrow(Throwable t) throws SQLException {
				tidyAndThrow(conn, t);
			}

			@Override
			E _run() throws SQLException {
				final Cncl<E> cur = this.cur;
				if (cur == null) {
					throw new SQLException("Task was cancelled") ;
				}
				final E r = cur.run();
				this.cur = null;
				conn.close();
				conn = null;
				return r;
			}

			@Override
			void _cancel() throws SQLException {
				if (cur != null) {
					cur.cancel() ;
					cur = null;
				}
			}
    	} ;
    }
    
    protected static void tidyAndThrow(ResultSet rs, Statement stmt, Throwable t) throws SQLException {
    	try { if (rs != null) rs.close() ; } catch (Exception _) { }
    	try { stmt.close() ; } catch (Exception _) { }
    	tidyAndThrow(t) ;
    }
    
    protected static void tidyAndThrow(Statement stmt, Throwable t) throws SQLException {
    	try { stmt.close() ; } catch (Exception _) { }
    	tidyAndThrow(t) ;
    }
    
    protected static void tidyAndThrow(Connection conn, Throwable t) throws SQLException {
		try { conn.close() ; } catch (Exception _) { }
		tidyAndThrow(t) ;
    }
    
    protected static void tidyAndThrow(Throwable t) throws SQLException {
    	if (t instanceof SQLException) {
    		throw (SQLException) t ;
    	} else if (t instanceof RuntimeException) {
    		throw (RuntimeException) t ;
    	} else if (t instanceof Error){
    		throw (Error) t ;
    	} else {
    		throw new UndeclaredThrowableException(t) ;
    	}
    }
    
    protected static Func<String[]> getColumnNames(final int startCol) {
    	return new Func<String[]>() {
    		private static final long serialVersionUID = 1L;
    		@Override
    		public String[] run(ResultSet rs) throws SQLException {
    			final ResultSetMetaData rsmd = rs.getMetaData();
    	    	final String[] names = new String[rsmd.getColumnCount() - startCol] ;
    	    	for (int i = 0 ; i != names.length ; i++) {
    	    		names[i] = rsmd.getColumnName(i + startCol + 1);
    	    	}
    	    	return names ;
    		}
    	} ;
    }
    protected static Func<BuildColumnFromResultSet[]> getColumnBuilders(final TimeZone tz) {
    	return new Func<BuildColumnFromResultSet[]>() {
    		private static final long serialVersionUID = 1L;
    		@Override
    		public BuildColumnFromResultSet[] run(ResultSet rs) throws SQLException {
    	    	final ResultSetMetaData md = rs.getMetaData() ;
    	    	final BuildColumnFromResultSet[] columns = new BuildColumnFromResultSet[md.getColumnCount()] ;
    	    	for (int i = 0 ; i != columns.length ; i++) {
    	    		columns[i] = getResultSetColumnBuilder(md, tz, i) ;
    	    	}
    	    	init(columns, rs) ;
    	    	return columns ;
    		}
    	} ;
    }
    protected static Func<BuildColumnFromResultSet[]> getColumnBuilders(final TimeZone tz, final Class<?> ... columnTypes) {
    	return getColumnBuilders(0, tz, columnTypes) ;
    }
    protected static Func<BuildColumnFromResultSet[]> getColumnBuilders(final int startCol, final TimeZone tz, final Class<?> ... columnTypes) {
    	return new Func<BuildColumnFromResultSet[]>() {
    		private static final long serialVersionUID = 1L;
    		@Override
    		public BuildColumnFromResultSet[] run(ResultSet rs) throws SQLException {
    	    	final BuildColumnFromResultSet[] columns = new BuildColumnFromResultSet[columnTypes.length] ;
    	    	for (int i = 0 ; i != columnTypes.length ; i++) {
    	    		columns[i] = getResultSetColumnBuilder(columnTypes[i], tz, i + startCol) ;
    	    	}
    	    	init(columns, rs) ;
    	    	return columns ;
    		}
    	} ;
    }
    protected static Func<GetFromResultSet<?>[]> getItemGetters(final TimeZone tz) {
    	return new Func<GetFromResultSet<?>[]>() {
			private static final long serialVersionUID = 1L;
			@Override
			public GetFromResultSet<?>[] run(ResultSet rs) throws SQLException {
		    	final ResultSetMetaData md = rs.getMetaData() ;
		    	final GetFromResultSet<?>[] columns = new GetFromResultSet[md.getColumnCount()] ;
		    	for (int i = 0 ; i != columns.length ; i++) {
		    		columns[i] = getResultSetItemGetter(md.getColumnType(i + 1), tz, i) ;
		    	}
		    	return columns ;
			}
    	} ;
    }
    protected static <V> Func<GetFromResultSet<? extends V>[]> getItemGetters(final TimeZone tz, final int startCol, final Class<? extends V> ... columnTypes) {
    	return getItemGetters(tz, startCol, false, columnTypes) ;
    }
    protected static <V> Func<GetFromResultSet<? extends V>[]> getItemGetters(final TimeZone tz, final int startCol, final boolean convertPrimitivesToObjects, final Class<? extends V> ... columnTypes) {
    	return new Func<GetFromResultSet<? extends V>[]>() {
			private static final long serialVersionUID = 1L;
			@Override
			public GetFromResultSet<? extends V>[] run(ResultSet v) {
		    	@SuppressWarnings("unchecked")
				final GetFromResultSet<? extends V>[] columns = new GetFromResultSet[columnTypes.length] ;
		    	for (int i = 0 ; i != columnTypes.length ; i++) {
		    		columns[i] = getResultSetItemGetter(columnTypes[i], tz, i + startCol, convertPrimitivesToObjects) ;
		    	}
		    	return columns ;
			}
    	} ;
    }
    protected static <E> Func<GetFromResultSet<? extends E>[]> getItemGetters(final TimeZone tz, final Class<? extends E> columnType) {
    	return new Func<GetFromResultSet<? extends E>[]>() {
    		private static final long serialVersionUID = 1L;
    		@SuppressWarnings("unchecked")
			@Override
    		public GetFromResultSet<? extends E>[] run(ResultSet rs) throws SQLException {
    	    	final GetFromResultSet<? extends E>[] columns = new GetFromResultSet[rs.getMetaData().getColumnCount()] ;
    	    	for (int i = 0 ; i != columns.length ; i++) {
    	    		columns[i] = getResultSetItemGetter(columnType, tz, i) ;
    	    	}
    	    	return columns ;
    		}
    	} ;
    }
    
    protected static Object[] matrix(BuildColumnFromResultSet[] columns) {
		final Object[] mx = new Object[columns.length] ;
		for (int i = 0 ; i != columns.length ; i++) {
			mx[i] = columns[i].getResult() ;
		}
		return mx ;
    }
    
    protected static void init(BuildColumnFromResultSet[] columns, ResultSet rs) throws SQLException {
    	// in principle used to setup row count, but cannot obtain it from anywhere!
    }
    
    protected static void init(BuildColumnFromResultSet column, ResultSet rs) throws SQLException {
    	// in principle used to setup row count, but cannot obtain it from anywhere!
    }
    
    @SuppressWarnings("unchecked")
	protected static final List<Class<?>> BOOLEANS = java.util.Arrays.asList((Class<?>)boolean.class, Boolean.class) ;
    @SuppressWarnings("unchecked")
	protected static final List<Class<?>> PRIMITIVE_NUMBERS = java.util.Arrays.asList((Class<?>)byte.class, short.class, int.class, long.class, float.class, double.class) ;
//	protected static final List<Class<?>> STRING_TYPES = java.util.Arrays.asList((Class<?>)String.class, Str.class) ;
    protected static final Set<Class<?>> STRING_TYPES = new SerialHashSet<Class<?>>() ;
    static {
    	STRING_TYPES.add(String.class);
    }
    protected static boolean compatible(Class<?> javaType, int sqlType) {
    	switch (sqlType) {
    	case java.sql.Types.BOOLEAN:
    		return BOOLEANS.contains(javaType) ;
    	case java.sql.Types.BIT:
    		return BOOLEANS.contains(javaType) || PRIMITIVE_NUMBERS.contains(javaType) || Number.class.isAssignableFrom(javaType) ;
    	case java.sql.Types.TINYINT:
    	case java.sql.Types.SMALLINT:
    	case java.sql.Types.INTEGER:
    	case java.sql.Types.BIGINT:
    	case java.sql.Types.REAL:
    	case java.sql.Types.FLOAT:
    	case java.sql.Types.DOUBLE:
    	case java.sql.Types.DECIMAL:
    	case java.sql.Types.NUMERIC:
    		return PRIMITIVE_NUMBERS.contains(javaType) || Number.class.isAssignableFrom(javaType) ;    		
    	case java.sql.Types.DATE:
    	case java.sql.Types.TIMESTAMP:
    		return java.util.Date.class.isAssignableFrom(javaType) || org.joda.time.ReadableInstant.class.isAssignableFrom(javaType) || org.joda.time.ReadablePartial.class.isAssignableFrom(javaType) ;
    	case java.sql.Types.CHAR:
    	case java.sql.Types.NCHAR:
    	case java.sql.Types.NVARCHAR:
    	case java.sql.Types.LONGNVARCHAR:
    	case java.sql.Types.LONGVARCHAR:
    	case java.sql.Types.VARCHAR:
    		return STRING_TYPES.contains(javaType) || Enum.class.isAssignableFrom(javaType) ;
    	case java.sql.Types.BINARY:
    	case java.sql.Types.BLOB:
    	case java.sql.Types.VARBINARY:
    		return javaType == byte[].class ;
    	case java.sql.Types.ARRAY:
    	case java.sql.Types.CLOB:
    	case java.sql.Types.DATALINK:
    	case java.sql.Types.DISTINCT:
    	case java.sql.Types.JAVA_OBJECT:
    	case java.sql.Types.LONGVARBINARY:
    	case java.sql.Types.NCLOB:
    	case java.sql.Types.OTHER:
    	case java.sql.Types.REF:
    	case java.sql.Types.ROWID:
    	case java.sql.Types.SQLXML:
    	case java.sql.Types.STRUCT:
    	case java.sql.Types.TIME:
    	case java.sql.Types.NULL:
    	default:
    		throw new IllegalStateException("Unsupported sql type " + sqlType) ;
    	}
    }
    
    protected static final <E> GetFromResultSet<E> getResultSetItemGetter(Class<? extends E> clazz, TimeZone tz, int col) {
    	return getResultSetItemGetter(clazz, tz, col, false) ;
    }
    @SuppressWarnings("unchecked")
	protected static final <E> GetFromResultSet<E> getResultSetItemGetter(Class<? extends E> clazz, TimeZone tz, int col, boolean convertPrimitivesToObjects) {
		if (clazz.isPrimitive()) {
			if (convertPrimitivesToObjects) {
				// dangerous - only used internally for keying
				if (clazz == int.class) {
					return (GetFromResultSet<E>) new GetIntegerFromResultSet(col + 1) ;
				} else if (clazz == long.class) {
					return (GetFromResultSet<E>) new GetLongFromResultSet(col + 1) ;
				} else if (clazz == double.class) {
					return (GetFromResultSet<E>) new GetDoubleFromResultSet(col + 1) ;
				} else if (clazz == boolean.class) {
					return (GetFromResultSet<E>) new GetBooleanFromResultSet(col + 1) ;
				} else {
					return (GetFromResultSet<E>) new GetObjectFromResultSet<Object>(Object.class, col + 1) ;
				}
			} else {
				throw new IllegalArgumentException("Primitives are not supported for this type of getter") ;
			}
		} else {
			if (clazz == java.lang.String.class) {
				return (GetFromResultSet<E>) new GetStringFromResultSet(col + 1) ;
			} else if (clazz == java.util.Date.class) {
				return (GetFromResultSet<E>) new GetDateFromResultSet(tz, col + 1) ;
			} else if (clazz == org.joda.time.LocalDate.class) {
				return (GetFromResultSet<E>) new GetLocalDateFromResultSet(col + 1) ;
			} else if (clazz == org.joda.time.DateTime.class) {
				return (GetFromResultSet<E>) new GetDateTimeFromResultSet(tz, col + 1) ;
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
//			} else if (clazz == Str.class) {
//				return (GetFromResultSet<E>) new GetStrFromResultSet(col + 1) ;
			} else if (clazz == byte[][].class) {
				return (GetFromResultSet<E>) new GetByteArrayFromResultSet(col + 1) ;
			} else if (Enum.class.isAssignableFrom(clazz)) {
				return new GetEnumFromResultSet(col + 1, clazz) ;
			} else {
				return new GetObjectFromResultSet<E>(clazz, col + 1) ;
			}
		}
    }

    protected static final BuildColumnFromResultSet getResultSetColumnBuilder(Class<?> clazz, TimeZone tz, int col) {
		if (clazz.isPrimitive()) {
			if (clazz == double.class) {
				return new BuildPrimitiveDoubleColumnFromResultSet(col + 1) ;
			} else if (clazz == float.class) {
				return new BuildPrimitiveFloatColumnFromResultSet(col + 1) ;
			} else if (clazz == int.class) {
					return new BuildPrimitiveIntColumnFromResultSet(col + 1) ;
			} else if (clazz == short.class) {
				return new BuildPrimitiveShortColumnFromResultSet(col + 1) ;
			} else if (clazz == long.class) {
				return new BuildPrimitiveLongColumnFromResultSet(col + 1) ;
			} else if (clazz == boolean.class) {
				return new BuildPrimitiveBooleanColumnFromResultSet(col + 1) ;
			} else if (clazz == char.class) {
				return new BuildPrimitiveCharColumnFromResultSet(col + 1) ;
			} else if (clazz == byte.class) {
				return new BuildPrimitiveByteColumnFromResultSet(col + 1) ;
			} else {
				throw new IllegalArgumentException("Only doubles and ints are currently supported as primitives. Implementing more is a copy-paste job though, so feel free...") ;
			}
		} else {
	    	if (clazz == java.util.Date.class) {
				return new BuildDateColumnFromResultSet(tz, col + 1) ;
	    	} else if (clazz == java.lang.String.class) {
				return new BuildStringColumnFromResultSet(col + 1) ;
			} else if (clazz == org.joda.time.LocalDate.class) {
				return new BuildLocalDateColumnFromResultSet(col + 1) ;
			} else if (clazz == org.joda.time.DateTime.class) {
				return new BuildDateTimeColumnFromResultSet(tz, col + 1) ;
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
//			} else if (clazz == Str.class) {
//				return new BuildStrColumnFromResultSet(col + 1) ;
			} else if (clazz == byte[][].class) {
				return new BuildByteArrayColumnFromResultSet(col + 1) ;
			} else if (Enum.class.isAssignableFrom(clazz)) {
				return new BuildEnumColumnFromResultSet(col + 1, clazz) ;
			} else {
				return new BuildObjectColumnFromResultSet(clazz, col + 1) ;
			}
		}
    }

    protected static final BuildColumnFromResultSet getResultSetColumnBuilder(ResultSetMetaData md, TimeZone tz, int col) throws SQLException {
    	switch (md.getColumnType(col + 1)) {
    	case java.sql.Types.BOOLEAN:
    	case java.sql.Types.BIT:
    		switch (md.isNullable(col + 1)) {
    		case java.sql.ResultSetMetaData.columnNoNulls:
    			return new BuildPrimitiveBooleanColumnFromResultSet(col + 1) ;
    		default:
    			return new BuildBooleanColumnFromResultSet(col + 1) ;
    		}
    	case java.sql.Types.TINYINT:
    		switch (md.isNullable(col + 1)) {
    		case java.sql.ResultSetMetaData.columnNoNulls:
    			return new BuildPrimitiveByteColumnFromResultSet(col + 1) ;
    		default:
    			return new BuildIntegerColumnFromResultSet(col + 1) ;
    		}
    	case java.sql.Types.SMALLINT:
    		switch (md.isNullable(col + 1)) {
    		case java.sql.ResultSetMetaData.columnNoNulls:
    			return new BuildPrimitiveShortColumnFromResultSet(col + 1) ;
    		default:
    			return new BuildIntegerColumnFromResultSet(col + 1) ;
    		}
    	case java.sql.Types.INTEGER:
    		switch (md.isNullable(col + 1)) {
    		case java.sql.ResultSetMetaData.columnNoNulls:
    			return new BuildPrimitiveIntColumnFromResultSet(col + 1) ;
    		default:
    			return new BuildIntegerColumnFromResultSet(col + 1) ;
    		}
    	case java.sql.Types.BIGINT:
    		switch (md.isNullable(col + 1)) {
    		case java.sql.ResultSetMetaData.columnNoNulls:
    			return new BuildPrimitiveLongColumnFromResultSet(col + 1) ;
    		default:
    			return new BuildLongColumnFromResultSet(col + 1) ;
    		}
    	case java.sql.Types.REAL:
    		return new BuildPrimitiveFloatColumnFromResultSet(col + 1) ;
    	case java.sql.Types.FLOAT:
    	case java.sql.Types.DOUBLE:
    		return new BuildPrimitiveDoubleColumnFromResultSet(col + 1) ;
    	case java.sql.Types.DECIMAL:
    	case java.sql.Types.NUMERIC:
    		return new BuildBigDecimalColumnFromResultSet(col + 1) ;
    	case java.sql.Types.DATE:
    	case java.sql.Types.TIMESTAMP:
    		return new BuildDateTimeColumnFromResultSet(tz, col + 1) ;
    	case java.sql.Types.CHAR:
    	case java.sql.Types.NCHAR:
    	case java.sql.Types.NVARCHAR:
    	case java.sql.Types.LONGNVARCHAR:
    	case java.sql.Types.LONGVARCHAR:
    	case java.sql.Types.VARCHAR:
    		return new BuildStringColumnFromResultSet(col + 1) ;
    	case java.sql.Types.BINARY:
    	case java.sql.Types.BLOB:
    	case java.sql.Types.VARBINARY:
    		return new BuildByteArrayColumnFromResultSet(col + 1) ;
    	case java.sql.Types.ARRAY:
    	case java.sql.Types.CLOB:
    	case java.sql.Types.DATALINK:
    	case java.sql.Types.DISTINCT:
    	case java.sql.Types.JAVA_OBJECT:
    	case java.sql.Types.LONGVARBINARY:
    	case java.sql.Types.NCLOB:
    	case java.sql.Types.OTHER:
    	case java.sql.Types.REF:
    	case java.sql.Types.ROWID:
    	case java.sql.Types.SQLXML:
    	case java.sql.Types.STRUCT:
    	case java.sql.Types.TIME:
    	case java.sql.Types.NULL:
    	default:
    		throw new IllegalStateException("Unsupported sql type " + md.getColumnTypeName(col + 1)) ;
    	}
    }
    
    protected static final GetFromResultSet<?> getResultSetItemGetter(int sqlType, TimeZone tz, int col) {
    	switch (sqlType) {
    	case java.sql.Types.BOOLEAN:
    	case java.sql.Types.BIT:
    		return new GetBooleanFromResultSet(col + 1) ;
    	case java.sql.Types.TINYINT:
    	case java.sql.Types.SMALLINT:
    	case java.sql.Types.INTEGER:
    		return new GetIntegerFromResultSet(col + 1) ;
    	case java.sql.Types.BIGINT:
    		return new GetLongFromResultSet(col + 1) ;
    	case java.sql.Types.REAL:
    	case java.sql.Types.FLOAT:
    	case java.sql.Types.DOUBLE:
    		return new GetDoubleFromResultSet(col + 1) ;
    	case java.sql.Types.DECIMAL:
    	case java.sql.Types.NUMERIC:
    		return new GetBigDecimalFromResultSet(col + 1) ;
    	case java.sql.Types.DATE:
    	case java.sql.Types.TIMESTAMP:
    		return new GetDateTimeFromResultSet(tz, col + 1) ;
    	case java.sql.Types.CHAR:
    	case java.sql.Types.NCHAR:
    	case java.sql.Types.NVARCHAR:
    	case java.sql.Types.LONGNVARCHAR:
    	case java.sql.Types.LONGVARCHAR:
    	case java.sql.Types.VARCHAR:
    		return new GetStringFromResultSet(col + 1) ;
    	case java.sql.Types.BINARY:
    	case java.sql.Types.BLOB:
    	case java.sql.Types.VARBINARY:
    		return new GetByteArrayFromResultSet(col + 1) ;
    	case java.sql.Types.ARRAY:
    	case java.sql.Types.CLOB:
    	case java.sql.Types.DATALINK:
    	case java.sql.Types.DISTINCT:
    	case java.sql.Types.JAVA_OBJECT:
    	case java.sql.Types.LONGVARBINARY:
    	case java.sql.Types.NCLOB:
    	case java.sql.Types.OTHER:
    	case java.sql.Types.REF:
    	case java.sql.Types.ROWID:
    	case java.sql.Types.SQLXML:
    	case java.sql.Types.STRUCT:
    	case java.sql.Types.TIME:
    	case java.sql.Types.NULL:
    	default:
    		throw new IllegalStateException("Unsupported sql type " + sqlType) ;
    	}
    }
    
	protected static abstract class GetFromResultSet<E> {
		private static final long serialVersionUID = -5242654405330263281L ;
		final Class<? extends E> clazz ;
		final int column ;
		final Function<E, E> f ;
		GetFromResultSet(Class<? extends E> clazz, int column) {
			this(clazz, column, Functions.<E>identity()) ;
		}
		GetFromResultSet(Class<? extends E> clazz, int column, Function<E, E> f) {
			this.clazz = clazz ;
			this.column = column ;
			this.f = f ;
		}
		abstract E geti(ResultSet rs) throws SQLException ;
		public final E get(ResultSet rs) throws SQLException {
			return f.apply(geti(rs)) ;
		}
	}

	protected static class GetByteArrayFromResultSet extends GetFromResultSet<byte[]> {
		public GetByteArrayFromResultSet(int column) {
			super(byte[].class, column) ;
		}
		public final byte[] geti(ResultSet rs) throws SQLException {
			return rs.getBytes(column) ;
		}
	}
	
    protected static class GetDateFromResultSet extends GetFromResultSet<java.util.Date> {
		final Calendar cal ;
		public GetDateFromResultSet(TimeZone tz, int column) {
			super(java.util.Date.class, column) ;
    		if (tz == null) {
    			throw new IllegalArgumentException("TimeZone cannot be null") ;
    		}
			this.cal = Calendar.getInstance(tz) ;
		}
		public final java.util.Date geti(ResultSet rs) throws SQLException {
			final Timestamp val = rs.getTimestamp(column, cal) ;
			return new java.util.Date(val.getTime()) ;
		}
	}

    protected static class GetBooleanFromResultSet extends GetFromResultSet<java.lang.Boolean> {
    	public GetBooleanFromResultSet(int column) {
    		super(java.lang.Boolean.class, column) ;
    	}
    	public final java.lang.Boolean geti(ResultSet rs) throws SQLException {
			Boolean r = rs.getBoolean(column) ? Boolean.TRUE : Boolean.FALSE ;
			return rs.wasNull() ? null : r ;
    	}
    }

    protected static class GetStringFromResultSet extends GetFromResultSet<java.lang.String> {
    	public GetStringFromResultSet(int column) {
    		super(java.lang.String.class, column, new Intern<String>(10000)) ;
    	}
    	public final java.lang.String geti(ResultSet rs) throws SQLException {
    		return rs.getString(column) ;
    	}
    }

//    protected static class GetStrFromResultSet extends GetFromResultSet<Str> {
//    	public GetStrFromResultSet(int column) {
//    		super(Str.class, column, new Intern<Str>()) ;
//    	}
//    	public final Str geti(ResultSet rs) throws SQLException {
//    		return Str.standardAlphabet().encode(rs.getString(column)) ;
//    	}
//    }

    protected static class GetByteFromResultSet extends GetFromResultSet<java.lang.Byte> {
    	public GetByteFromResultSet(int column) {
    		super(java.lang.Byte.class, column) ;
    	}
    	public final java.lang.Byte geti(ResultSet rs) throws SQLException {
    		final byte val = rs.getByte(column) ;
    		return rs.wasNull() ? null : Byte.valueOf(val) ;
    	}
    }
    
    protected static class GetCharFromResultSet extends GetFromResultSet<java.lang.Character> {
    	public GetCharFromResultSet(int column) {
    		super(java.lang.Character.class, column) ;
    	}
    	public final java.lang.Character geti(ResultSet rs) throws SQLException {
    		final String val = rs.getString(column) ;
    		if (val == null) {
    			return null ;
    		} else if (val.length() != 1) {
    			throw new SQLException("'" + val + "' cannot be represented as a char") ;
    		} else {
    			return Character.valueOf(val.charAt(0)) ;
    		}
    	}
    }
    
    protected static class GetShortFromResultSet extends GetFromResultSet<java.lang.Short> {
    	public GetShortFromResultSet(int column) {
    		super(java.lang.Short.class, column) ;
    	}
    	public final java.lang.Short geti(ResultSet rs) throws SQLException {
    		final short val = rs.getShort(column) ;
    		return rs.wasNull() ? null : Short.valueOf(val) ;
    	}
    }
    
    protected static class GetIntegerFromResultSet extends GetFromResultSet<java.lang.Integer> {
    	public GetIntegerFromResultSet(int column) {
    		super(java.lang.Integer.class, column) ;
    	}
    	public final java.lang.Integer geti(ResultSet rs) throws SQLException {
    		final int val = rs.getInt(column) ;
    		return rs.wasNull() ? null : Integer.valueOf(val) ;
    	}
    }

    protected static class GetLongFromResultSet extends GetFromResultSet<java.lang.Long> {
    	public GetLongFromResultSet(int column) {
    		super(java.lang.Long.class, column) ;
    	}
    	public final java.lang.Long geti(ResultSet rs) throws SQLException {
    		final long val = rs.getLong(column) ;
    		return rs.wasNull() ? null : Long.valueOf(val) ;
    	}
    }

    protected static class GetFloatFromResultSet extends GetFromResultSet<java.lang.Float> {
    	public GetFloatFromResultSet(int column) {
    		super(java.lang.Float.class, column) ;
    	}
    	public final java.lang.Float geti(ResultSet rs) throws SQLException {
    		final float val = rs.getFloat(column) ;
    		return rs.wasNull() ? null : Float.valueOf(val) ;
    	}
    }
    
    protected static class GetDoubleFromResultSet extends GetFromResultSet<java.lang.Double> {
    	public GetDoubleFromResultSet(int column) {
    		super(java.lang.Double.class, column) ;
    	}
    	public final java.lang.Double geti(ResultSet rs) throws SQLException {
    		final double val = rs.getDouble(column) ;
    		return rs.wasNull() ? null : Double.valueOf(val) ;
    	}
    }

    protected static class GetBigDecimalFromResultSet extends GetFromResultSet<BigDecimal> {
    	public GetBigDecimalFromResultSet(int column) {
    		super(BigDecimal.class, column) ;
    	}
    	public final BigDecimal geti(ResultSet rs) throws SQLException {
    		return rs.getBigDecimal(column) ;
    	}
    }
    
    protected static class GetLocalDateFromResultSet extends GetFromResultSet<org.joda.time.LocalDate> {
    	final Calendar cal ;
    	final DateTimeZone tz ;
    	public GetLocalDateFromResultSet(int column) {
    		super(org.joda.time.LocalDate.class, column, new Intern<LocalDate>(10000)) ;
    		this.cal = Calendar.getInstance() ;
    		this.tz = DateTimeZone.forTimeZone(cal.getTimeZone()) ;
    	}
    	public final org.joda.time.LocalDate geti(ResultSet rs) throws SQLException {
    		final java.sql.Timestamp val = rs.getTimestamp(column, cal) ;
    		return val == null ? null : new DateTime(val, tz).toLocalDate();
    	}
    }

    protected static class GetDateTimeFromResultSet extends GetFromResultSet<org.joda.time.DateTime> {
    	final Calendar cal ;
    	final DateTimeZone tz ;
    	public GetDateTimeFromResultSet(TimeZone tz, int column) {
    		super(org.joda.time.DateTime.class, column) ;
    		if (tz == null) {
    			throw new IllegalArgumentException("TimeZone cannot be null") ;
    		}
    		this.cal = Calendar.getInstance(tz) ;
    		this.tz = DateTimeZone.forTimeZone(tz) ;
    	}
    	public final org.joda.time.DateTime geti(ResultSet rs) throws SQLException {
    		final java.sql.Timestamp val = rs.getTimestamp(column, cal) ;
    		return val == null ? null : new DateTime(val, tz) ;
    	}
    }

    protected static class GetTimestampFromResultSet extends GetFromResultSet<java.sql.Timestamp> {
		final Calendar cal ;
		public GetTimestampFromResultSet(TimeZone tz, int column) {
			super(java.sql.Timestamp.class, column) ;
    		if (tz == null) {
    			throw new IllegalArgumentException("TimeZone cannot be null") ;
    		}
			this.cal = Calendar.getInstance(tz) ;
		}
		public final java.sql.Timestamp geti(ResultSet rs) throws SQLException {
			return rs.getTimestamp(column, cal) ;
		}
	}

	protected static class GetSQLDateFromResultSet extends GetFromResultSet<java.sql.Date> {
		final Calendar cal ;
		public GetSQLDateFromResultSet(TimeZone tz, int column) {
			super(java.sql.Date.class, column) ;
    		if (tz == null) {
    			throw new IllegalArgumentException("TimeZone cannot be null") ;
    		}
			this.cal = Calendar.getInstance(tz) ;
		}
		public final java.sql.Date geti(ResultSet rs) throws SQLException {
			return rs.getDate(column, cal) ;
		}
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	protected static class GetEnumFromResultSet extends GetFromResultSet {
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
		public final Object geti(ResultSet rs) throws SQLException {
			final String val = rs.getString(column) ;
			try {
				return rs.wasNull() ? null : m.invoke(null, val) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}

	protected static class GetObjectFromResultSet<D> extends GetFromResultSet<D> {
		public GetObjectFromResultSet(Class<? extends D> clazz, int column) {
			super(clazz, column) ;
		}
		public final D geti(ResultSet rs) throws SQLException {
			return clazz.cast(rs.getObject(column)) ;
		}
	}

	protected static interface BuildColumnFromResultSet {
		abstract BuildColumnFromResultSet newInstance();
		abstract void fetch(ResultSet rs) throws SQLException ;
		abstract Object getResult() ;
		abstract void setRowCount(int count) ;
		abstract Object get(ResultSet rs) throws SQLException ;
	}

	protected static final class BuildObjectColumnFromResultSet extends GetObjectFromResultSet<Object> implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		Object[] results ;
		int count ;
		BuildObjectColumnFromResultSet(Class<?> clazz, int column) {
			super(clazz, column) ;
			results = (Object[]) Array.newInstance(clazz, 10) ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new Object[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = clazz.cast(rs.getObject(column)) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildObjectColumnFromResultSet(clazz, column);
		}
	}

	protected static final class BuildByteArrayColumnFromResultSet extends GetByteArrayFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		byte[][] results ;
		int count ;
		BuildByteArrayColumnFromResultSet(int column) {
			super(column) ;
			results = new byte[10][] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new byte[count][];
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getBytes(column) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildByteArrayColumnFromResultSet(column);
		}
	}
	
	protected static final class BuildDateColumnFromResultSet extends GetDateFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		java.util.Date[] results ;
		int count ;
		BuildDateColumnFromResultSet(TimeZone tz, int column) {
			super(tz, column) ;
			results = new java.util.Date[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new java.util.Date[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final Timestamp val = rs.getTimestamp(column, cal) ;
			results[count++] = val == null ? null : new java.util.Date(val.getTime()) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildDateColumnFromResultSet(cal.getTimeZone(), column);
		}
	}

	protected static final class BuildLocalDateColumnFromResultSet extends GetLocalDateFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		LocalDate[] results ;
		int count ;
		BuildLocalDateColumnFromResultSet(int column) {
			super(column) ;
			results = new LocalDate[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new LocalDate[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final Timestamp val = rs.getTimestamp(column, cal) ;
			results[count++] = val == null ? null : f.apply(new DateTime(val, tz).toLocalDate()) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildLocalDateColumnFromResultSet(column);
		}
	}

	protected static final class BuildDateTimeColumnFromResultSet extends GetDateTimeFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		DateTime[] results ;
		int count ;
		BuildDateTimeColumnFromResultSet(TimeZone tz, int column) {
			super(tz, column) ;
			results = new DateTime[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new DateTime[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final Timestamp val = rs.getTimestamp(column, cal) ;
			results[count++] = val == null ? null : new DateTime(val, tz) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildDateTimeColumnFromResultSet(cal.getTimeZone(), column);
		}
	}

	protected static final class BuildSQLDateColumnFromResultSet extends GetSQLDateFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		java.sql.Date[] results ;
		int count ;
		BuildSQLDateColumnFromResultSet(TimeZone tz, int column) {
			super(tz, column) ;
			results = new java.sql.Date[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new java.sql.Date[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getDate(column, cal) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildSQLDateColumnFromResultSet(cal.getTimeZone(), column);
		}
	}

	protected static final class BuildTimestampColumnFromResultSet extends GetTimestampFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		Timestamp[] results ;
		int count ;
		BuildTimestampColumnFromResultSet(TimeZone tz, int column) {
			super(tz, column) ;
			results = new Timestamp[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new Timestamp[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getTimestamp(column, cal) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildTimestampColumnFromResultSet(cal.getTimeZone(), column);
		}
	}

	protected static final class BuildPrimitiveBooleanColumnFromResultSet extends GetBooleanFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		boolean[] results ;
		int count ;
		BuildPrimitiveBooleanColumnFromResultSet(int column) {
			super(column) ;
			results = new boolean[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new boolean[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getBoolean(column) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveBooleanColumnFromResultSet(column);
		}
	}

	protected static final class BuildPrimitiveByteColumnFromResultSet extends GetByteFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		byte[] results ;
		int count ;
		BuildPrimitiveByteColumnFromResultSet(int column) {
			super(column) ;
			results = new byte[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new byte[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final byte val = rs.getByte(column) ;
			results[count++] = rs.wasNull() ? Byte.MIN_VALUE : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveByteColumnFromResultSet(column);
		}
	}
	
	protected static final class BuildPrimitiveCharColumnFromResultSet extends GetCharFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		char[] results ;
		int count ;
		BuildPrimitiveCharColumnFromResultSet(int column) {
			super(column) ;
			results = new char[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new char[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final String val = rs.getString(column) ;
			if (val == null) {
				count++ ;
			} else if (val.length() > 1) {
				throw new SQLException(val + " is not a valid char value") ;
			} else {
				results[count++] = val.charAt(0) ;
			}
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveCharColumnFromResultSet(column);
		}
	}
	
	protected static final class BuildPrimitiveShortColumnFromResultSet extends GetShortFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		short[] results ;
		int count ;
		BuildPrimitiveShortColumnFromResultSet(int column) {
			super(column) ;
			results = new short[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new short[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final short val = rs.getShort(column) ;
			results[count++] = rs.wasNull() ? Short.MIN_VALUE : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveShortColumnFromResultSet(column);
		}
	}
	
	protected static final class BuildPrimitiveIntColumnFromResultSet extends GetIntegerFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		int[] results ;
		int count ;
		BuildPrimitiveIntColumnFromResultSet(int column) {
			super(column) ;
			results = new int[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new int[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final int val = rs.getInt(column) ;
			results[count++] = rs.wasNull() ? Integer.MIN_VALUE : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveIntColumnFromResultSet(column);
		}
	}

	protected static final class BuildPrimitiveLongColumnFromResultSet extends GetLongFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		long[] results ;
		int count ;
		BuildPrimitiveLongColumnFromResultSet(int column) {
			super(column) ;
			results = new long[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new long[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final long val = rs.getLong(column) ;
			results[count++] = rs.wasNull() ? Long.MIN_VALUE : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveLongColumnFromResultSet(column);
		}
	}
	
	protected static final class BuildPrimitiveFloatColumnFromResultSet extends GetFloatFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		float[] results ;
		int count ;
		BuildPrimitiveFloatColumnFromResultSet(int column) {
			super(column) ;
			results = new float[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new float[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final float val = rs.getFloat(column) ;
			results[count++] = rs.wasNull() ? Float.NaN : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveFloatColumnFromResultSet(column);
		}
	}
	
	protected static final class BuildPrimitiveDoubleColumnFromResultSet extends GetDoubleFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		double[] results ;
		int count ;
		BuildPrimitiveDoubleColumnFromResultSet(int column) {
			super(column) ;
			results = new double[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new double[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final double val = rs.getDouble(column) ;
			results[count++] = rs.wasNull() ? Double.NaN : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildPrimitiveDoubleColumnFromResultSet(column);
		}
	}

	protected static final class BuildBigDecimalColumnFromResultSet extends GetBigDecimalFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		BigDecimal[] results ;
		int count ;
		BuildBigDecimalColumnFromResultSet(int column) {
			super(column) ;
			results = new BigDecimal[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new BigDecimal[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = rs.getBigDecimal(column) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildBigDecimalColumnFromResultSet(column);
		}
	}
	
	protected static final class BuildBooleanColumnFromResultSet extends GetBooleanFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		Boolean[] results ;
		int count ;
		BuildBooleanColumnFromResultSet(int column) {
			super(column) ;
			results = new Boolean[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new Boolean[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			Boolean r = rs.getBoolean(column) ? Boolean.TRUE : Boolean.FALSE ;
			results[count++] = rs.wasNull() ? null : r ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildBooleanColumnFromResultSet(column);
		}
	}

	protected static final class BuildStringColumnFromResultSet extends GetStringFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		String[] results ;
		int count ;
		BuildStringColumnFromResultSet(int column) {
			super(column) ;
			results = new String[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new String[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			results[count++] = f.apply(rs.getString(column)) ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildStringColumnFromResultSet(column);
		}
	}

//	protected static final class BuildStrColumnFromResultSet extends GetStrFromResultSet implements BuildColumnFromResultSet {
//		private static final long serialVersionUID = -5242654405330263281L ;
//		Str[] results ;
//		int count ;
//		BuildStrColumnFromResultSet(int column) {
//			super(column) ;
//			results = new Str[10] ;
//			count = 0 ;
//		}
//		public final void setRowCount(int count) {
//			results = new Str[count] ;
//		}
//		public final void fetch(ResultSet rs) throws SQLException {
//			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
//			results[count++] = f.f(Str.standardAlphabet().encode(rs.getString(column))) ;
//		}
//		@Override
//		public final Object getResult() {
//			final Object r = java.util.Arrays.copyOf(results, count) ;
//			count = 0 ;
//			return r ;
//		}
//		@Override
//		public BuildColumnFromResultSet newInstance() {
//			return new BuildStrColumnFromResultSet(column);
//		}
//	}

	protected static final class BuildDoubleColumnFromResultSet extends GetDoubleFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		Double[] results ;
		int count ;
		BuildDoubleColumnFromResultSet(int column) {
			super(column) ;
			results = new Double[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new Double[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final double val = rs.getDouble(column) ;
			results[count++] = rs.wasNull() ? null : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildDoubleColumnFromResultSet(column);
		}
	}

	protected static final class BuildIntegerColumnFromResultSet extends GetIntegerFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		Integer[] results ;
		int count ;
		BuildIntegerColumnFromResultSet(int column) {
			super(column) ;
			results = new Integer[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new Integer[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
    		final Integer val = rs.getInt(column) ;
    		results[count++] = rs.wasNull() ? null : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildIntegerColumnFromResultSet(column);
		}
	}

	protected static final class BuildLongColumnFromResultSet extends GetLongFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		Long[] results ;
		int count ;
		BuildLongColumnFromResultSet(int column) {
			super(column) ;
			results = new Long[10] ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new Long[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final Long val = rs.getLong(column) ;
			results[count++] = rs.wasNull() ? null : val ;
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildLongColumnFromResultSet(column);
		}
	}

	protected static final class BuildEnumColumnFromResultSet extends GetEnumFromResultSet implements BuildColumnFromResultSet {
		private static final long serialVersionUID = -5242654405330263281L ;
		Enum<?>[] results ;
		int count ;
		BuildEnumColumnFromResultSet(int column, Class<?> clazz) {
			super(column, clazz) ;
			results = (Enum<?>[]) Array.newInstance(clazz, 10) ;
			count = 0 ;
		}
		public final void setRowCount(int count) {
			results = new Enum[count] ;
		}
		public final void fetch(ResultSet rs) throws SQLException {
			if (count == results.length) results = java.util.Arrays.copyOf(results, count << 1) ;
			final String val = rs.getString(column) ;
			try {
				results[count++] = rs.wasNull() ? null : (Enum<?>) m.invoke(null, val) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
		@Override
		public final Object getResult() {
			final Object r = java.util.Arrays.copyOf(results, count) ;
			count = 0 ;
			return r ;
		}
		@Override
		public BuildColumnFromResultSet newInstance() {
			return new BuildEnumColumnFromResultSet(column, clazz);
		}
	}
	
}
