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

import java.util.Comparator;

import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.types.ObjectId;

class BSONComparator implements Comparator<Object> {

	static final BSONComparator INSTANCE = new BSONComparator();
	
	@SuppressWarnings("unchecked")
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0;
		int t1 = getBSONType(o1);
		int t2 = getBSONType(o2);
		if (t1 != t2)
			return t1-t2;
		if (o1 instanceof Integer || o1 instanceof String || o1 instanceof Number || o1 instanceof ObjectId )
			return ((Comparable)o1).compareTo(o2);
		
		throw new IllegalArgumentException("cannot compare "+ o1.getClass().getName());
	}

	
	static byte getBSONType(Object x){
		if (x == null)
			return BSON.NULL;
		if (x instanceof String)
			return BSON.STRING;
		if (x instanceof Integer)
			return BSON.NUMBER_INT;
		if (x instanceof Long)
			return BSON.NUMBER_LONG;
		if (x instanceof ObjectId)
			return BSON.OID;
		if (x instanceof BSONObject)
			return BSON.OBJECT;
		throw new IllegalArgumentException(x.getClass().getName());
	}
}
