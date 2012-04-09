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

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public class DefaultDeleteHandler implements DeleteHandler {

	private final MockMongo mongo;

	DefaultDeleteHandler(MockMongo mongo) {
		this.mongo = mongo;
	}

	public Result handleDelete(String database, String collection,
			boolean singleRemove, BSONObject selector) {
		if (!singleRemove)
			throw new UnsupportedOperationException(
					"multiRemove not implemented");

		DefaultQueryHandler query = new DefaultQueryHandler(mongo);
		BSONObject[] target = query.handleQuery(database, collection, selector);
		if (target.length > 0) {
			mongo.getCollection(database, collection).remove(
					new BasicBSONObject("_id", target[0].get("_id")));
			return new Result(1);
		}

		return new Result(0);
	}

}
