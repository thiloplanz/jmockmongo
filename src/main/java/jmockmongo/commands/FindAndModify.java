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

import jmockmongo.CommandHandler;
import jmockmongo.DefaultQueryHandler;
import jmockmongo.DefaultUpdateHandler;
import jmockmongo.MockMongo;
import jmockmongo.Unsupported;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public class FindAndModify implements CommandHandler {

	private final MockMongo mongo;

	public FindAndModify(MockMongo mongo) {
		this.mongo = mongo;
	}

	public BSONObject handleCommand(String database, BSONObject command) {
		Unsupported.supportedAndRequiredFields(command, "findandmodify",
				"query", "update");
		String collection = (String) command.get("findandmodify");
		BSONObject query = (BSONObject) command.get("query");
		BSONObject update = (BSONObject) command.get("update");

		BSONObject[] data = new DefaultQueryHandler(mongo).handleQuery(
				database, collection, query);

		if (data.length == 0)
			return new BasicBSONObject("ok", 1).append("n", 0);
		if (data.length > 1)
			return new BasicBSONObject("ok", 0).append("n", data.length);

		BSONObject result = (BSONObject) ((BasicBSONObject) data[0]).clone();
		new DefaultUpdateHandler(mongo).handleUpdate(database, collection,
				false, false, query, update);
		return new BasicBSONObject("ok", 1).append("n", 1).append("value",
				result);
	}

}
