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

import java.util.Map;

import jmockmongo.BSONUtils;
import jmockmongo.CommandHandler;
import jmockmongo.MockDB;
import jmockmongo.Unsupported;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public class DbStats implements CommandHandler {

	private final Map<String, MockDB> data;

	public DbStats(Map<String, MockDB> data) {
		this.data = data;
	}

	public BSONObject handleCommand(String database, BSONObject command) {
		Unsupported.supportedFields(command, "dbstats", "scale");
		int scale = 1;
		Integer s = BSONUtils.getInteger(command, "scale");
		if (s != null)
			scale = s;

		MockDB db = data.get(database);

		if (db == null)
			db = new MockDB(database);

		BasicBSONObject result = new BasicBSONObject("ok", 1).append("db",
				database);
		result.append("collections", db.countCollections());
		int count = db.countObjects();
		result.append("objects", count);

		// TODO ? storage sizes make not much sense here...
		int dataSize = 0;

		result.append("dataSize", dataSize / scale);
		result.append("avgObjectSize", count == 0 ? 0 : dataSize
				/ (count * scale));
		result.append("storageSize", dataSize);
		result.append("numExtents", 1);

		// just primary key indexes
		result.append("indexes", db.countCollections());
		result.append("indexSize", 0);

		result.append("fileSize", 0);
		result.append("nsSizeMB", 0);
		return result;
	}
}
