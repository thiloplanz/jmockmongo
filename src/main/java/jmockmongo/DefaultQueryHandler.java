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

class DefaultQueryHandler implements QueryHandler {

	private final MockMongo mongo;;

	public DefaultQueryHandler(MockMongo mongo) {
		this.mongo = mongo;
	}

	public BSONObject[] handleQuery(String database, String collection,
			BSONObject command) {

		if ("system.indexes".equals(collection))
			return new BSONObject[0];

		MockDB db = mongo.getDB(database);
		if (db != null) {
			if (command.keySet().isEmpty())
				return findAll(db, database, collection);
			Object id = command.get("_id");
			if (id == null || command.keySet().size() > 1)
				throw new UnsupportedOperationException(
						"only _id queries are implemented, not " + command);
			MockDBCollection c = db.getCollection(collection);
			if (c != null) {
				BSONObject result = c.findOne(id);
				if (result != null)
					return new BSONObject[] { result };
			}
		}

		return new BSONObject[0];
	}

	private BSONObject[] findAll(MockDB db, String database, String collection) {
		MockDBCollection c = db.getCollection(collection);
		if (c == null)
			return new BSONObject[0];
		return c.documents().toArray(new BSONObject[0]);

	}

}
