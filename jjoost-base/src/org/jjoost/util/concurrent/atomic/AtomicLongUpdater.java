package org.jjoost.util.concurrent.atomic;

import java.lang.reflect.Field;


public final class AtomicLongUpdater {

	private final long offset;
	public AtomicLongUpdater(Class<?> c, String field) {
		this(Unsafe.field(c, long.class, field));
	}
	public AtomicLongUpdater(Field f) {
		if (f.getType() != long.class) {
			throw new IllegalArgumentException("Field " + f + " is of type " + f.getType());
		}
		offset = Unsafe.objectFieldOffset(f);
	}

	public final boolean compareAndSet(Object trg, long exp, long upd) {
		return Unsafe.compareAndSetLong(trg, offset, exp, upd);
	}
	
	public final void putVolatile(Object trg, long val) {
		Unsafe.putLongVolatile(trg, offset, val);
	}
	
	public final long get(Object trg) {
		return Unsafe.getLong(trg, offset);
	}
	
	public final long getVolatile(Object trg) {
		return Unsafe.getLongVolatile(trg, offset);
	}
	
	public final boolean decrementIfAboveZero(Object trg) {
		while (true) {
			final long cur = getVolatile(trg);
			if (cur <= 0) {
				return false;
			} else if (compareAndSet(trg, cur, cur - 1)) {
				return true;
			}
		}
	}
	
	public final boolean incrementIfNonNegative(Object trg) {
		while (true) {
			final long cur = getVolatile(trg);
			if (cur >= 0) {
				return false;
			} else if (compareAndSet(trg, cur, cur + 1)) {
				return true;
			}
		}
	}
	
	public final boolean setIfSmaller(Object trg, long val) {
		while (true) {
			final long cur = getVolatile(trg);
			if (cur <= val) {
				return false;
			} else if (compareAndSet(trg, cur, val)) {
				return true;
			}
		}
	}
	
	public final boolean setIfLarger(Object trg, long val) {
		while (true) {
			final long cur = getVolatile(trg);
			if (cur >= val) {
				return false;
			} else if (compareAndSet(trg, cur, val)) {
				return true;
			}
		}
	}
	
	public final long addAndGet(Object trg, long val) {
		while (true) {
			final long cur = getVolatile(trg);
			if (compareAndSet(trg, cur, cur + val)) {
				return cur;
			}
		}
	}

	public final long decrementAndGet(Object trg) {
		while (true) {
			final long cur = getVolatile(trg);
			if (compareAndSet(trg, cur, cur - 1)) {
				return cur;
			}
		}
	}
	
	public final long setAndGetDelta(Object trg, long upd) {
		while (true) {
			final long cur = getVolatile(trg);
			if (compareAndSet(trg, cur, upd)) {
				return cur - upd;
			}
		}
	}
	
	public long incrementAndGet(Object trg) {
		while (true) {
			final long cur = getVolatile(trg);
			if (compareAndSet(trg, cur, cur + 1)) {
				return cur;
			}
		}
	}
	
}
