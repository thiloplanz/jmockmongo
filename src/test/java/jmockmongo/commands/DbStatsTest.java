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

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;

public class DbStatsTest extends MockMongoTestCaseSupport {

	private CommandResult getStats() {
		return getMongo().getDB("test").getStats();
	}

	public void testDb() {
		assertEquals("test", getStats().get("db"));
	}

	public void testCollections() {
		assertEquals(0, getStats().get("collections"));
		prepareMockData("test.x", new BasicDBObject("_id", 1));
		prepareMockData("test.y", new BasicDBObject("_id", 1));
		prepareMockData("notest.y", new BasicDBObject("_id", 1));
		assertEquals(2, getStats().get("collections"));
	}

	public void testObjects() {
		assertEquals(0, getStats().get("objects"));
		prepareMockData("test.x", new BasicDBObject("_id", 1));
		prepareMockData("test.x", new BasicDBObject("_id", 2));
		prepareMockData("notest.x", new BasicDBObject("_id", 1));
		assertEquals(2, getStats().get("objects"));
	}

}
