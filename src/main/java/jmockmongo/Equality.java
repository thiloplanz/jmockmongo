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

import org.bson.types.ObjectId;

class Equality implements QueryPredicate {

	private final Object[] options;

	Equality(Object[] options) {
		for (Object o: options){
			if (o instanceof String 
					|| o instanceof ObjectId
					|| o instanceof Long
					|| o instanceof Integer
					|| o instanceof Boolean) {
				continue;
			}
			throw new UnsupportedOperationException("unsupported datatype for $eq: "+o.getClass().getName());
		}
		this.options = options;
	}

	public boolean test(Object value) {
		for (Object o : options)
			if (o.equals(value))
				return true;
		return false;
	}

}
