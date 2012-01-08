package org.jjoost.util.concurrent.atomic;

import java.lang.reflect.Field;


public final class AtomicIntUpdater {

	private final long offset;
	public AtomicIntUpdater(Class<?> c, String field) {
		this(Unsafe.field(c, int.class, field));
	}
	public AtomicIntUpdater(Field f) {
		if (f.getType() != int.class) {
			throw new IllegalArgumentException("Field " + f + " is of type " + f.getType());
		}
		offset = Unsafe.objectFieldOffset(f);
	}

	public final boolean compareAndSet(Object trg, int exp, int upd) {
		return Unsafe.compareAndSetInt(trg, offset, exp, upd);
	}
	
	public final void putVolatile(Object trg, int val) {
		Unsafe.putIntVolatile(trg, offset, val);
	}
	
	public final int get(Object trg) {
		return Unsafe.getInt(trg, offset);
	}
	
	public final int getVolatile(Object trg) {
		return Unsafe.getIntVolatile(trg, offset);
	}
	
	public final boolean decrementIfAboveZero(Object trg) {
		while (true) {
			final int cur = getVolatile(trg);
			if (cur <= 0) {
				return false;
			} else if (compareAndSet(trg, cur, cur - 1)) {
				return true;
			}
		}
	}
	
	public final boolean incrementIfNonNegative(Object trg) {
		while (true) {
			final int cur = getVolatile(trg);
			if (cur >= 0) {
				return false;
			} else if (compareAndSet(trg, cur, cur + 1)) {
				return true;
			}
		}
	}
	
	public final boolean setIfSmaller(Object trg, int val) {
		while (true) {
			final int cur = getVolatile(trg);
			if (cur <= val) {
				return false;
			} else if (compareAndSet(trg, cur, val)) {
				return true;
			}
		}
	}
	
	public final boolean setIfLarger(Object trg, int val) {
		while (true) {
			final int cur = getVolatile(trg);
			if (cur >= val) {
				return false;
			} else if (compareAndSet(trg, cur, val)) {
				return true;
			}
		}
	}
	
	public final int addAndGet(Object trg, int val) {
		while (true) {
			final int cur = getVolatile(trg);
			if (compareAndSet(trg, cur, cur + val)) {
				return cur;
			}
		}
	}

	public final int decrementAndGet(Object trg) {
		while (true) {
			final int cur = getVolatile(trg);
			if (compareAndSet(trg, cur, cur - 1)) {
				return cur;
			}
		}
	}
	
	public final int setAndGetDelta(Object trg, int upd) {
		while (true) {
			final int cur = getVolatile(trg);
			if (compareAndSet(trg, cur, upd)) {
				return cur - upd;
			}
		}
	}
	
	public long incrementAndGet(Object trg) {
		while (true) {
			final int cur = getVolatile(trg);
			if (compareAndSet(trg, cur, cur + 1)) {
				return cur;
			}
		}
	}
	
}
