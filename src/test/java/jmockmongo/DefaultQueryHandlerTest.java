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

import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class DefaultQueryHandlerTest extends MockMongoSetup {

	public void testFindOneById() throws UnknownHostException, MongoException,
			InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				"test"));

		Mongo m = new Mongo();
		assertEquals("test", m.getDB("x").getCollection("x").findOne("x").get(
				"field"));
		m.close();

	}

	public void testFindAll() throws UnknownHostException, MongoException,
			InterruptedException {

		prepareMockData("x.x", new BasicBSONObject("_id", "x").append("field",
				"test"));

		Mongo m = new Mongo();
		assertEquals("[{ \"_id\" : \"x\" , \"field\" : \"test\"}]", m
				.getDB("x").getCollection("x").find().toArray().toString());
		m.close();

	}

}
