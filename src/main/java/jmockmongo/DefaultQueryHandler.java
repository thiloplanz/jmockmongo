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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

public class DefaultQueryHandler implements QueryHandler {

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
			Object id = null;
			Map<String, Object[]> equalityFilters = new HashMap<String, Object[]>();
			for (String field : command.keySet()) {
				if ("_id".equals(field)) {
					id = command.get(field);
					if (id instanceof BSONObject) {
						for (String s : ((BSONObject) id).keySet()) {
							if (s.equals("$ref") || s.equals("$id"))
								continue;
							if (s.startsWith("$"))
								throw new UnsupportedOperationException(s
										+ " queries are not implemented:"
										+ command);
						}
					}
				} else {
					if (field.startsWith("$"))
						throw new UnsupportedOperationException(field
								+ " queries are not implemented:" + command);
					if (field.contains("."))
						throw new UnsupportedOperationException(
								"nested field queries are not implemented: "
										+ command);
					Object value = command.get(field);
					if (value instanceof String || value instanceof ObjectId) {
						equalityFilters.put(field, new Object[] { value });
					} else if (value instanceof BSONObject) {
						BSONObject filters = (BSONObject) value;
						for (String f : filters.keySet()) {
							if ("$in".equals(f)) {
								equalityFilters.put(field, Unsupported
										.onlyStrings(BSONUtils.values(filters,
												f)));

							} else {
								throw new UnsupportedOperationException(
										"unsupported query " + f + " for "
												+ field + ": " + filters);
							}
						}
					} else
						throw new UnsupportedOperationException(
								"unsupported query for " + field + ": "
										+ command);

				}
			}
			BSONObject[] all = null;
			if (id == null) {
				all = findAll(db, database, collection);
			} else {
				MockDBCollection c = db.getCollection(collection);
				if (c != null) {
					BSONObject result = c.findOne(id);
					if (result != null)
						all = new BSONObject[] { result };
				}
			}
			if (all != null && all.length > 0) {
				if (equalityFilters.isEmpty())
					return all;

				List<BSONObject> result = new ArrayList<BSONObject>(all.length);
				candidates: for (BSONObject x : all) {
					equalities: for (Map.Entry<String, Object[]> e : equalityFilters
							.entrySet()) {
						Object xx = x.get(e.getKey());
						for (Object v : e.getValue()) {
							if (v.equals(xx))
								continue equalities;
						}
						continue candidates;
					}
					result.add(x);
				}
				return result.toArray(new BSONObject[0]);
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
