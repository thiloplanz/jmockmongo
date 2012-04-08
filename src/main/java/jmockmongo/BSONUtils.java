/**
 * Copyright (c) 2012, Thilo Planz. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License, Version 2.0
 * as published by the Apache Software Foundation (the "License").
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * You should have received a copy of the License along with this program.
 * If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package jmockmongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bson.BSON;
import org.bson.BSONObject;

/**
 * Utility class to work with BSON data.
 * <ul>
 * <li>performs loss-less type conversions
 * <li>supports nested fields ("a.b.c"), including auto-vivification
 * <li>fields that are null, empty array, or empty object are treated as missing
 * (setting to null removes the field)
 * </ul>
 * 
 */

class BSONUtils {

	private static <T> T notNull(T x) {
		if (x == null)
			return x;
		if (x instanceof Map<?, ?>) {
			if (((Map<?, ?>) x).isEmpty())
				return null;
			return x;
		}

		if (x instanceof Object[]) {
			if (((Object[]) x).length == 0)
				return null;
			return x;
		}

		if (x instanceof byte[]) {
			if (((byte[]) x).length == 0)
				return null;
			return x;
		}

		if (x instanceof Collection<?>) {
			if (((Collection<?>) x).isEmpty())
				return null;
			return x;
		}

		return x;

	}

	static Object get(BSONObject b, String fieldName) {
		if (!fieldName.contains("."))
			return notNull(b.get(fieldName));
		String[] path = fieldName.split("\\.", 2);
		Object nested = b.get(path[0]);
		if (nested == null)
			return null;
		if (nested instanceof BSONObject)
			return get((BSONObject) nested, path[1]);
		throw new IllegalArgumentException("cannot get field `" + fieldName
				+ "` of " + b);
	}

	private static Object getRequired(BSONObject b, String fieldName) {
		Object x = get(b, fieldName);
		if (x == null)
			throw new IllegalArgumentException("required field `" + fieldName
					+ "` is missing in " + b);
		return x;
	}

	static Long toLong(Object x) {
		if (x == null)
			return null;
		if (x instanceof Long)
			return (Long) x;
		if (x instanceof Integer)
			return Long.valueOf(((Integer) x).intValue());
		if (x instanceof String)
			return Long.valueOf((String) x);
		throw new IllegalArgumentException("cannot convert `" + x
				+ "` into a Long");
	}

	static Integer toInteger(Object x) {
		if (x == null)
			return null;
		if (x instanceof Integer)
			return (Integer) x;
		if (x instanceof Long)
			return Integer.valueOf(x.toString());
		if (x instanceof String)
			return Integer.valueOf((String) x);
		throw new IllegalArgumentException("cannot convert `" + x
				+ "` into a Long");
	}

	static String toString(Object x) {
		if (x == null)
			return null;
		if (x instanceof String)
			return (String) x;
		if (x instanceof Number)
			return x.toString();

		throw new IllegalArgumentException("cannot convert `" + x
				+ "` into a String");
	}

	static Long getLong(BSONObject b, String fieldName) {
		return toLong(get(b, fieldName));
	}

	static Integer getInteger(BSONObject b, String fieldName) {
		return toInteger(get(b, fieldName));
	}

	static int getRequiredInt(BSONObject b, String fieldName) {
		return toInteger(getRequired(b, fieldName)).intValue();
	}

	static long getRequiredLong(BSONObject b, String fieldName) {
		return toLong(getRequired(b, fieldName)).longValue();
	}

	static String getString(BSONObject b, String fieldName) {
		return toString(get(b, fieldName));
	}

	static BSONObject getObject(BSONObject b, String fieldName) {
		Object x = get(b, fieldName);
		if (x == null)
			return null;
		if (x instanceof BSONObject)
			return (BSONObject) x;
		throw new IllegalArgumentException("cannot convert `" + x
				+ "` into a BSONObject");
	}

	static Object removeField(BSONObject b, String fieldName) {
		if (fieldName.contains("."))
			throw new UnsupportedOperationException("not yet implemented");
		return b.removeField(fieldName);
	}

	private static void put(BSONObject b, String fieldName, Object x) {
		x = notNull(x);
		if (x == null) {
			removeField(b, fieldName);
		} else {
			if (fieldName.contains("."))
				throw new UnsupportedOperationException("not yet implemented");
			b.put(fieldName, x);
		}
	}

	static Integer putInteger(BSONObject b, String fieldName, Object x) {
		Integer i = toInteger(x);
		put(b, fieldName, i);
		return i;
	}

	static Long putLong(BSONObject b, String fieldName, Object x) {
		Long l = toLong(x);
		put(b, fieldName, l);
		return l;
	}

	static String putString(BSONObject b, String fieldName, Object x) {
		String s = toString(x);
		put(b, fieldName, s);
		return s;
	}

	private static final Long MAX_INT = Long.valueOf(Integer.MAX_VALUE);
	private static final Long MIN_INT = Long.valueOf(Integer.MIN_VALUE);

	/**
	 * stores a number as Integer, if it fits, or as a Long, if not. This saves
	 * space in the database, but you lose the ability to sort or do range
	 * queries.
	 */
	static Number putIntegerOrLong(BSONObject b, String fieldName, Object x) {
		if (x instanceof Integer)
			return putInteger(b, fieldName, x);

		Long l = toLong(x);
		if (l == null) {
			removeField(b, fieldName);
			return null;
		}
		if (l.compareTo(MAX_INT) < 0 && l.compareTo(MIN_INT) > 0) {
			Integer i = l.intValue();
			b.put(fieldName, i);
			return i;
		}
		b.put(fieldName, l);
		return l;
	}

	/**
	 * BSON objects are considered equal when their binary encoding matches
	 */
	static boolean equals(BSONObject a, BSONObject b) {
		return a.keySet().equals(b.keySet())
				&& Arrays.equals(BSON.encode(a), BSON.encode(b));
	}

	static boolean equals(Object a, Object b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		if (a instanceof BSONObject)
			if (b instanceof BSONObject)
				return equals((BSONObject) a, (BSONObject) b);
			else
				return false;
		if (a instanceof byte[]) {
			if (b instanceof byte[])
				return Arrays.equals((byte[]) a, (byte[]) b);
			else
				return false;
		}
		return a.equals(b);
	}

	/**
	 * @return true, if the field contains (in case of an array) or is equal to
	 *         (in case of a single value) the given Object
	 */
	static boolean contains(BSONObject b, String fieldName, Object toLookFor) {
		Object list = get(b, fieldName);
		if (list == null)
			return false;
		if (list instanceof List<?>) {
			for (Object o : (List<?>) list) {
				if (equals(o, toLookFor))
					return true;

			}
			return false;
		}
		if (list instanceof Object[]) {
			for (Object o : (Object[]) list) {
				if (equals(o, toLookFor))
					return true;

			}
			return false;
		}
		return (equals(list, toLookFor));
	}

	/**
	 * adds the element to the array, if it does not already exists there. Array
	 * will be created if necessary. Single elements will be updated to arrays
	 * is necessary.
	 * 
	 * Just adds one object (no "$each" processing)
	 */

	@SuppressWarnings("unchecked")
	static void addToSet(BSONObject b, String fieldName, Object value) {
		if (contains(b, fieldName, value))
			return;

		Object list = get(b, fieldName);
		if (list == null) {
			List<Object> l = new ArrayList<Object>();
			l.add(value);
			put(b, fieldName, l);
			return;
		}

		if (list instanceof List<?>) {
			((List) list).add(value);
			return;
		}
		if (list instanceof Object[]) {
			List<Object> l = new ArrayList<Object>();
			for (Object x : (Object[]) list) {
				l.add(x);
			}
			l.add(value);
			put(b, fieldName, l);
			return;
		}
		List<Object> l = new ArrayList<Object>();
		l.add(list);
		l.add(value);
		put(b, fieldName, l);
		return;

	}

	static Object[] values(BSONObject b, String fieldName) {
		Object x = get(b, fieldName);
		if (x == null)
			return new Object[0];
		if (x instanceof List<?>)
			return ((List<?>) x).toArray();
		if (x instanceof Object[])
			return Arrays.copyOf((Object[]) x, ((Object[]) x).length);
		return new Object[] { x };
	}
}
