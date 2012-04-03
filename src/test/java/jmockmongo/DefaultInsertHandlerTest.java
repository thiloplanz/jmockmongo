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

import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class DefaultInsertHandlerTest extends MockMongoSetup {

	public void testSimpleInsert() throws UnknownHostException, MongoException,
			InterruptedException {

		Mongo m = new Mongo();
		WriteResult result = m.getDB("x").getCollection("x").insert(
				WriteConcern.SAFE,
				new BasicDBObject("_id", "x").append("field", "test"));

		assertMockMongoFieldEquals("test", "x.x", "x", "field");
		assertEquals("test", m.getDB("x").getCollection("x").findOne("x").get(
				"field"));
		assertEquals(1, result.getN());

	}
}
