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

import java.util.Iterator;

import org.bson.BSONObject;

class DefaultInsertHandler implements InsertHandler {

	private final MockMongo mongo;

	DefaultInsertHandler(MockMongo mongo) {
		this.mongo = mongo;
	}

	public Result handleInsert(String database, String collection,
			boolean continueOnError, Iterator<BSONObject> docs) {

		if (continueOnError)
			throw new UnsupportedOperationException(
					"continueOnError not yet implemented");

		MockDBCollection c = mongo.getOrCreateDB(database)
				.getOrCreateCollection(collection);
		try {
			int n = 0;
			while (docs.hasNext()) {
				c.insert(docs.next());
				n++;
			}
			return new Result(n);
		} catch (IllegalArgumentException e) {
			return new Result("E11000 duplicate key error");
		}
	}

}
