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

import java.net.UnknownHostException;

import jmockmongo.MockMongoTestCaseSupport;

import org.bson.BasicBSONObject;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class CountTest extends MockMongoTestCaseSupport {

	public void testFullCount() throws UnknownHostException, MongoException {

		Mongo m = getMongo();
		assertEquals(0, m.getDB("x").getCollection("x").count());

		prepareMockData("x.x", new BasicBSONObject("_id", "x"));
		assertEquals(1, m.getDB("x").getCollection("x").count());

		prepareMockData("x.x", new BasicBSONObject("_id", "y"));
		assertEquals(2, m.getDB("x").getCollection("x").count());

	}

}
