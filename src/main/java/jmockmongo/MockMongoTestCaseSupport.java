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

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bson.BSONObject;

import com.mongodb.DBPort;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

/**
 * 
 * @author Thilo Planz
 * @author Julien Polo
 * 
 */

public abstract class MockMongoTestCaseSupport extends TestCase {

	private MockMongo mockMongo;

	private Mongo mongo;

	@Override
	protected void setUp() throws Exception {
		// make sure we cannot connect before this
		// (this could mean a real server is running on our port)

		mockMongo = new MockMongo();

		try {
			new DBPort(new ServerAddress("0.0.0.0", mockMongo.getPort()))
					.ensureOpen();
			fail("something is already listening at the Mongo port! Is a real mongo process running?");
		} catch (IOException e) {
		}
		mockMongo.start();

		mongo = new Mongo(mockMongo.getMongoURI());
	}

	@Override
	protected void tearDown() throws Exception {
		if (mockMongo != null)
			mockMongo.stop();
		if (mongo != null)
			mongo.close();
	}

	protected Mongo getMongo() {
		return mongo;
	}

	protected BSONObject assertMockMongoContainsDocument(
			String fullCollectionName, Object _id) {
		MockDBCollection c = mockMongo.getCollection(fullCollectionName);
		if (c == null) {
			fail("no collection " + fullCollectionName);
		}
		BSONObject b = c.findOne(_id);
		if (b == null) {
			fail("no document " + _id + " in " + fullCollectionName);
		}
		return b;
	}

	protected void assertMockMongoDoesNotContainDocument(
			String fullCollectionName, Object _id) {
		MockDBCollection c = mockMongo.getCollection(fullCollectionName);
		if (c == null)
			return;

		BSONObject b = c.findOne(_id);
		if (b != null) {
			fail("unexpected document " + _id + " was found in "
					+ fullCollectionName);
		}
	}

	protected void assertMockMongoFieldEquals(Object expected,
			String fullCollectionName, Object _id, String field) {
		BSONObject doc = assertMockMongoContainsDocument(fullCollectionName,
				_id);
		assertEquals(expected, BSONUtils.get(doc, field));
	}

	protected void assertMockMongoFieldContains(Object expected,
			String fullCollectionName, Object _id, String field) {
		BSONObject doc = assertMockMongoContainsDocument(fullCollectionName,
				_id);
		if (!BSONUtils.contains(doc, field, expected))
			fail("expected " + expected + " in " + field
					+ ", but there was only "
					+ Arrays.toString(BSONUtils.values(doc, field)));

	}

	protected void assertMockMongoFieldDoesNotContain(Object expected,
			String fullCollectionName, Object _id, String field) {
		BSONObject doc = assertMockMongoContainsDocument(fullCollectionName,
				_id);
		if (BSONUtils.contains(doc, field, expected))
			fail("did not expect " + expected + " in " + field
					+ ", but it was there");

	}

	protected void prepareMockData(String fullCollectionName, BSONObject data) {
		MockDBCollection c = mockMongo
				.getOrCreateCollection(fullCollectionName);
		c.insert(data);
	}

}
