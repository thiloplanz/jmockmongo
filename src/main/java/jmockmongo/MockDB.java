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

import java.util.concurrent.ConcurrentHashMap;

public class MockDB {

	private final String name;

	private final ConcurrentHashMap<String, MockDBCollection> collections = new ConcurrentHashMap<String, MockDBCollection>();

	MockDB(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public MockDBCollection getCollection(String collectionName) {
		return collections.get(collectionName);

	}

	public MockDBCollection getOrCreateCollection(String collectionName) {
		MockDBCollection collection = collections.get(collectionName);
		if (collection != null)
			return collection;
		collections.putIfAbsent(collectionName, new MockDBCollection(
				collectionName));
		return collections.get(collectionName);
	}

}
