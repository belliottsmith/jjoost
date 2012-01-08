package org.jjoost.util.concurrent.atomic;

import java.lang.reflect.Field;


public final class AtomicRefUpdater<C, V> {

	private final long offset;
	public static <C, V> AtomicRefUpdater<C, V> get(Class<C> c, Class<V> f, String field) {
		return new AtomicRefUpdater<C, V>(c, f, field);
	}
	public AtomicRefUpdater(Class<C> c, Class<V> f, String field) {
		this(c, f, Unsafe.field(c, f, field));
	}
	public AtomicRefUpdater(Class<C> c, Class<V> ftype, Field f) {
		if (f.getType().isPrimitive()) {
			throw new IllegalArgumentException("Field " + f + " is primitive");
		}
		if (!ftype.equals(f.getType())) {
			throw new IllegalArgumentException("Type of field " + f + " does not match provided type " + ftype);
		}
		offset = Unsafe.objectFieldOffset(f);
	}
	
	public final boolean compareAndSet(Object trg, Object exp, Object upd) {
		return Unsafe.compareAndSetObject(trg, offset, exp, upd);
	}
	
	public final void putVolatile(Object trg, Object val) {
		Unsafe.putObjectVolatile(trg, offset, val);
	}
	
	public final Object get(Object trg) {
		return Unsafe.getObject(trg, offset);
	}
	
	public final Object getVolatile(Object trg) {
		return Unsafe.getObjectVolatile(trg, offset);
	}
	
}
