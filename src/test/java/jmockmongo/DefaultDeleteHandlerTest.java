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

import org.bson.BasicBSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class DefaultDeleteHandlerTest extends MockMongoTestCaseSupport {

	public void testDeleteNothing() throws UnknownHostException,
			MongoException, InterruptedException {

		Mongo m = getMongo();
		WriteResult result = m.getDB("x").getCollection("x").remove(
				new BasicDBObject("_id", "x"), WriteConcern.SAFE);
		assertEquals(0, result.getN());

	}

	public void testDeleteOneById() throws UnknownHostException,
			MongoException, InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x"));

		Mongo m = getMongo();
		WriteResult result = m.getDB("x").getCollection("x").remove(
				new BasicDBObject("_id", "x"), WriteConcern.SAFE);
		assertEquals(1, result.getN());
		assertMockMongoDoesNotContainDocument("x.x", "x");

	}

}
