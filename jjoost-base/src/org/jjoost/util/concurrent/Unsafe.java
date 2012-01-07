package org.jjoost.util.concurrent;

import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;

@SuppressWarnings("restriction")
@Deprecated
class Unsafe {

	static final sun.misc.Unsafe INST = getUnsafe();
	
	private static final sun.misc.Unsafe getUnsafe() {
		
		sun.misc.Unsafe unsafe = null;
		try {
			Class<?> uc = sun.misc.Unsafe.class;
			Field[] fields = uc.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].getName().equals("theUnsafe")) {
					fields[i].setAccessible(true);
					unsafe = (sun.misc.Unsafe) fields[i].get(uc);
					break;
				}
			}
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
		
		return unsafe;
	}
	
	public static long fieldOffset(Class<?> clazz, String field) {
		try {
			final Field f = clazz.getDeclaredField(field);
			return Unsafe.INST.objectFieldOffset(f);
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}

	}

}
