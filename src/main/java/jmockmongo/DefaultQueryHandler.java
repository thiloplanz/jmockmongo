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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

import com.mongodb.QueryOperators;


/**
 * 
 * @author Thilo Planz
 * @author Justin Corpron
 *
 */

public class DefaultQueryHandler implements QueryHandler {

	private final MockMongo mongo;;

	public DefaultQueryHandler(MockMongo mongo) {
		this.mongo = mongo;
	}

	public BSONObject[] handleQuery(String database, String collection,
			BSONObject command) {

		if ("system.indexes".equals(collection))
			return new BSONObject[0];

		// https://jira.mongodb.org/browse/SERVER-6078
		BSONObject sort = null;
		if (command.containsField("query")) {
			Object q = command.get("query");
			if (q instanceof BSONObject) {
				BSONObject qq = (BSONObject) q;
				Unsupported.supportedFields(command, "query", "orderby");
				sort = BSONUtils.getObject(command, "orderby");
				command = qq;
				if (sort != null && sort.keySet().isEmpty())
					sort = null;
			}
		}

		MockDB db = mongo.getDB(database);
		if (db != null) {
			if (command.keySet().isEmpty())
				return findAll(db, database, collection, sort);
			Object id = null;
			Map<String, List<QueryPredicate>> filters = new HashMap<String, List<QueryPredicate>>();
			for (String field : command.keySet()) {
				if ("_id".equals(field)) {
					id = command.get(field);
					if (id instanceof BSONObject) {
						BSONObject options = (BSONObject) id;

						for (String s : options.keySet()) {
							if (s.equals("$ref") || s.equals("$id"))
								continue;
							if (QueryOperators.GT.equals(s)) {
								multiPut(filters, field, new GreaterThan(
										options.get(s)));
								id = null;
							} else if (QueryOperators.LT.equals(s)) {
								multiPut(filters, field, new LowerThan(options
										.get(s)));
								id = null;
							} else if (QueryOperators.GTE.equals(s)) {
								multiPut(filters, field,
										new GreaterThanOrEqual(options.get(s)));
								id = null;
							} else if (QueryOperators.LTE.equals(s)) {
								multiPut(filters, field, new LowerThanOrEqual(
										options.get(s)));
								id = null;
							} else if (s.startsWith("$"))
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
					if (value instanceof String 
							|| value instanceof ObjectId
							|| value instanceof Long
							|| value instanceof Integer
							|| value instanceof Boolean) {
						multiPut(filters, field, 
								new Equality(new Object[] { value }));
					} else if (value instanceof BSONObject) {
						BSONObject options = (BSONObject) value;
						for (String f : options.keySet()) {
							if ("$in".equals(f)) {
								multiPut(filters, field, new Equality(BSONUtils
										.values(options, f)));
							} else if (QueryOperators.GT.equals(f)) {
								multiPut(filters, field, new GreaterThan(
										options.get(f)));
							} else if (QueryOperators.LT.equals(f)) {
								multiPut(filters, field, new LowerThan(options
										.get(f)));
							} else if (QueryOperators.GTE.equals(f)) {
								multiPut(filters, field,
										new GreaterThanOrEqual(options.get(f)));
							} else if (QueryOperators.LTE.equals(f)) {
								multiPut(filters, field, new LowerThanOrEqual(
										options.get(f)));
							} else {
								throw new UnsupportedOperationException(
										"unsupported query " + f + " for "
												+ field + ": " + options);
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
				all = findAll(db, database, collection, sort);
			} else {
				MockDBCollection c = db.getCollection(collection);
				if (c != null) {
					BSONObject result = c.findOne(id);
					if (result != null)
						all = new BSONObject[] { result };
				}
			}
			if (all != null && all.length > 0) {
				if (filters.isEmpty())
					return all;

				List<BSONObject> result = new ArrayList<BSONObject>(all.length);
				candidates: for (BSONObject x : all) {
					for (Map.Entry<String, List<QueryPredicate>> e : filters
							.entrySet()) {
						Object xx = x.get(e.getKey());
						for (QueryPredicate q : e.getValue())
							if (!q.test(xx))
								continue candidates;

					}
					result.add(x);
				}
				return result.toArray(new BSONObject[0]);
			}

		}

		return new BSONObject[0];
	}

	private void multiPut(Map<String, List<QueryPredicate>> map, String field,
			QueryPredicate filter) {
		List<QueryPredicate> existing = map.get(field);
		if (existing != null)
			existing.add(filter);
		else {
			List<QueryPredicate> n = new ArrayList<QueryPredicate>();
			n.add(filter);
			map.put(field, n);
		}
	}

	private BSONObject[] findAll(MockDB db, String database, String collection,
			BSONObject sort) {
		if (sort != null && sort.keySet().size() > 1)
			throw new UnsupportedOperationException(
					"multi-key sorting not yet implemented");

		MockDBCollection c = db.getCollection(collection);
		if (c == null)
			return new BSONObject[0];
		BSONObject[] all = c.documents().toArray(new BSONObject[0]);
		if (sort == null || all.length == 1)
			return all;

		final String sortField = sort.keySet().iterator().next();
		Object a = sort.get(sortField);
		if (a instanceof Boolean)
			;
		else if (a.equals(1))
			a = true;
		else if (a.equals(-1))
			a = false;
		else
			throw new IllegalArgumentException("sort order for " + sortField
					+ " should be 1 or -1, not " + a);

		final boolean asc = (Boolean) a;

		Arrays.sort(all, new Comparator<BSONObject>() {

			public int compare(BSONObject arg0, BSONObject arg1) {
				int r = BSONComparator.INSTANCE.compare(arg0.get(sortField),
						arg1.get(sortField));
				return asc ? r : -r;
			}

		});

		return all;

	}
}
