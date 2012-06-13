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

public class DefaultUpdateHandler implements UpdateHandler {

	private final MockMongo mongo;

	public DefaultUpdateHandler(MockMongo mongo) {
		this.mongo = mongo;
	}

	public Result handleUpdate(String database, String collection,
			boolean upsert, boolean multiUpdate, BSONObject selector,
			BSONObject update) {

		if (multiUpdate || upsert)
			throw new UnsupportedOperationException(
					"multiUpdate and upsert not implemented");

		DefaultQueryHandler query = new DefaultQueryHandler(mongo);
		BSONObject[] target = query.handleQuery(database, collection, selector);
		if (target.length > 0) {
			BSONObject t = target[0];
			final Object _id = t.get("_id");
			boolean hasModifier = false;
			for (String op : update.keySet()) {
				if (op.startsWith("$")) {
					hasModifier = true;
					break;
				}
				// "." is not supported
				if (op.contains("."))
					throw new UnsupportedOperationException(
							"nested fields not implemented");
			}

			if (hasModifier) {
				for (String op : update.keySet()) {
					if ("$set".equals(op)) {
						BSONObject $set = (BSONObject) update.get("$set");
						for (String k : $set.keySet()) {
							// if _id is present, it must match
							if ("_id".equals(k))
								if (!_id.equals($set.get(k)))
									throw new UnsupportedOperationException(
											"cannot change _id of a document");

							// "." is not supported
							if (op.contains("."))
								throw new UnsupportedOperationException(
										"nested fields not implemented");
							t.put(k, $set.get(k));
						}
					} else if ("$addToSet".equals(op)) {
						BSONObject $set = (BSONObject) update.get("$addToSet");
						for (String k : $set.keySet()) {
							Object x = $set.get(k);
							if (x instanceof BSONObject) {
								BSONObject b = (BSONObject) x;
								if (b.containsField("$each")) {
									for (Object each : BSONUtils.values(b,
											"$each"))
										BSONUtils.addToSet(t, k, each);
									continue;
								}
							}
							BSONUtils.addToSet(t, k, x);
						}
					} else {
						throw new UnsupportedOperationException(op
								+ " is not implemented " + update);
					}
				}
				return new Result(1);
			}

			// if _id is present, it must match
			if (update.containsField("_id")) {
				if (!_id.equals(update.get("_id")))
					throw new UnsupportedOperationException(
							"cannot change _id of a document");
			}
			update.put("_id", _id);
			t.keySet().clear();
			t.putAll(update);
			return new Result(1);

		}

		return new Result(0);
	}

}
