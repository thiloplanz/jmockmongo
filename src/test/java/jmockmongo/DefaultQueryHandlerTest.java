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

import java.net.UnknownHostException;
import java.util.List;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class DefaultQueryHandlerTest extends MockMongoTestCaseSupport {

	public void testFindOneById() throws UnknownHostException, MongoException,
			InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				"test"));

		Mongo m = getMongo();
		assertEquals("test", m.getDB("x").getCollection("x").findOne("x").get(
				"field"));

	}

	public void testFindAll() throws UnknownHostException, MongoException,
			InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				"test"));

		Mongo m = getMongo();
		assertEquals("[{ \"_id\" : \"x\" , \"field\" : \"test\"}]", m
				.getDB("x").getCollection("x").find().toArray().toString());

		prepareMockData("x.x", new BasicBSONObject("_id", "y").append("field",
				"test"));
		assertEquals(2, m.getDB("x").getCollection("x").find().toArray().size());
	}

	public void testFindByField() throws UnknownHostException, MongoException,
			InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				"test"));

		Mongo m = getMongo();
		assertEquals("[{ \"_id\" : \"x\" , \"field\" : \"test\"}]", m
				.getDB("x").getCollection("x").find(
						new BasicDBObject("field", "test")).toArray()
				.toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("field", "not test")).toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("not field", "test")).toArray().toString());

	}

	public void testFindByField_$in() throws UnknownHostException,
			MongoException, InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				"test"));

		Mongo m = getMongo();
		assertEquals("[{ \"_id\" : \"x\" , \"field\" : \"test\"}]", m
				.getDB("x").getCollection("x").find(
						new BasicDBObject("field", new BasicDBObject("$in",
								new String[] { "test", "nope" }))).toArray()
				.toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("field", new BasicDBObject("$in",
						new String[] { "not test", "nope" }))).toArray()
				.toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("not field", new BasicDBObject("$in",
						new String[] { "test", "nope" }))).toArray().toString());

	}

	public void testFindByField_$oid() throws UnknownHostException,
			MongoException, InterruptedException {

		ObjectId oid = new ObjectId("4fa213bc036483d83b01ad44");
		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				oid));

		Mongo m = getMongo();
		assertEquals(
				"[{ \"_id\" : \"x\" , \"field\" : { \"$oid\" : \"4fa213bc036483d83b01ad44\"}}]",
				m.getDB("x").getCollection("x").find(
						new BasicDBObject("field", oid)).toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("field", new ObjectId())).toArray()
				.toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("not field", oid)).toArray().toString());
	}

	public void testFindByField_Integer() throws UnknownHostException,
			MongoException, InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				1));

		Mongo m = getMongo();
		assertEquals("[{ \"_id\" : \"x\" , \"field\" : 1}]", m.getDB("x")
				.getCollection("x").find(new BasicDBObject("field", 1))
				.toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("field", 2)).toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("not field", 1)).toArray().toString());
	}

	public void testFindByField_Long() throws UnknownHostException,
			MongoException, InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				1l));

		Mongo m = getMongo();
		assertEquals("[{ \"_id\" : \"x\" , \"field\" : 1}]", m.getDB("x")
				.getCollection("x").find(new BasicDBObject("field", 1l))
				.toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("field", 1)).toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("field", 2l)).toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("not field", 1l)).toArray().toString());
	}

	public void testFindByFieldAndId() throws UnknownHostException,
			MongoException, InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				"test"));

		Mongo m = getMongo();
		assertEquals("[{ \"_id\" : \"x\" , \"field\" : \"test\"}]", m
				.getDB("x").getCollection("x").find(
						new BasicDBObject("field", "test").append("_id", "x"))
				.toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("field", "not test").append("_id", "x"))
				.toArray().toString());
		assertEquals("[]", m.getDB("x").getCollection("x").find(
				new BasicDBObject("not field", "test").append("_id", "x"))
				.toArray().toString());

	}

	public void testSort() {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				10));
		prepareMockData("x.x", new BasicBSONObject("_id", "y").append("field",
				9));
		prepareMockData("x.x", new BasicBSONObject("_id", 1).append("field",
				"x"));
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find().sort(new BasicDBObject("field", 1)).toArray();
			assertEquals(1, result.get(0).get("_id"));
			assertEquals("y", result.get(1).get("_id"));
			assertEquals("x", result.get(2).get("_id"));
		}
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find().sort(new BasicDBObject("field", -1)).toArray();
			assertEquals("x", result.get(0).get("_id"));
			assertEquals("y", result.get(1).get("_id"));
			assertEquals(1, result.get(2).get("_id"));
		}
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find().sort(new BasicDBObject("_id", 1)).toArray();
			assertEquals("x", result.get(0).get("_id"));
			assertEquals("y", result.get(1).get("_id"));
			assertEquals(1, result.get(2).get("_id"));
		}
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find().sort(new BasicDBObject("_id", -1)).toArray();
			assertEquals(1, result.get(0).get("_id"));
			assertEquals("y", result.get(1).get("_id"));
			assertEquals("x", result.get(2).get("_id"));
		}

	}

	public void test$gt() {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				10));
		prepareMockData("x.x", new BasicBSONObject("_id", "y").append("field",
				9));
		prepareMockData("x.x", new BasicBSONObject("_id", 1).append("field",
				"x"));
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find(
							new BasicDBObject("field", new BasicDBObject("$gt",
									9))).toArray();
			assertEquals("x", result.get(0).get("_id"));
			assertEquals(1, result.size());
		}
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find(
							new BasicDBObject("_id", new BasicDBObject("$gt",
									"y"))).toArray();
			assertEquals(1, result.get(0).get("_id"));
			assertEquals(1, result.size());
		}

		prepareMockData("x.x", new BasicBSONObject("_id", new BasicBSONObject(
				"_id", 1).append("x", 1)).append("field", "complex"));
		prepareMockData("x.x", new BasicBSONObject("_id", new BasicBSONObject(
				"_id", 1).append("x", 2)).append("field", "complex"));
		
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find(
							new BasicDBObject("_id", new BasicDBObject("$gt",
									new BasicBSONObject(
											"_id", 1).append("x", 1))).append("field", "complex")).toArray();
			assertEquals("{ \"_id\" : 1 , \"x\" : 2}", result.get(0).get("_id").toString());
			assertEquals(1, result.size());
		}

	}

	public void test$lt() {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				10));
		prepareMockData("x.x", new BasicBSONObject("_id", "y").append("field",
				9));
		prepareMockData("x.x", new BasicBSONObject("_id", 1).append("field",
				"x"));
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find(
							new BasicDBObject("field", new BasicDBObject("$lt",
									9))).toArray();
			assertEquals(1, result.get(0).get("_id"));
			assertEquals(1, result.size());
		}
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find(
							new BasicDBObject("_id", new BasicDBObject("$lt",
									"y"))).toArray();
			assertEquals("x", result.get(0).get("_id"));
			assertEquals(1, result.size());
		}

		
		prepareMockData("x.x", new BasicBSONObject("_id", new BasicBSONObject(
				"_id", 1).append("x", 1)).append("field", "complex"));
		prepareMockData("x.x", new BasicBSONObject("_id", new BasicBSONObject(
				"_id", 1).append("x", 2)).append("field", "complex"));
		
		{
			List<DBObject> result = getMongo().getDB("x").getCollection("x")
					.find(
							new BasicDBObject("_id", new BasicDBObject("$lt",
									new BasicBSONObject(
											"_id", 1).append("x", 2))).append("field", "complex")).toArray();
			assertEquals("{ \"_id\" : 1 , \"x\" : 1}", result.get(0).get("_id").toString());
			assertEquals(1, result.size());
		}

	}

}
