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
package jmockmongo.commands;

import jmockmongo.MockMongoTestCaseSupport;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class FindAndModifyTest extends MockMongoTestCaseSupport {

	public void testSimple() {
		prepareMockData("test.x", new BasicDBObject("_id", "x").append("field",
				"1"));

		Mongo m = getMongo();
		m.setWriteConcern(WriteConcern.SAFE);
		BSONObject before = m.getDB("test").getCollection("x").findAndModify(
				new BasicDBObject("_id", "x").append("field", "1"),
				new BasicDBObject("$set", new BasicBSONObject("field", "2")));

		assertEquals("1", before.get("field"));
		assertMockMongoFieldEquals("2", "test.x", "x", "field");

		// did not match
		assertNull(m.getDB("test").getCollection("x").findAndModify(
				new BasicDBObject("_id", "x").append("field", "1"),
				new BasicDBObject("$set", new BasicBSONObject("field", "2"))));

		assertMockMongoFieldEquals("2", "test.x", "x", "field");

	}

}
