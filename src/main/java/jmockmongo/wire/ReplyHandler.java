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

package jmockmongo.wire;

import java.util.HashMap;
import java.util.Map;

import jmockmongo.CommandHandler;
import jmockmongo.InsertHandler;
import jmockmongo.QueryHandler;
import jmockmongo.UpdateHandler;

import org.bson.BSONObject;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class ReplyHandler extends SimpleChannelUpstreamHandler {

	private final Map<String, CommandHandler> commands = new HashMap<String, CommandHandler>();

	private QueryHandler queryHandler;

	private InsertHandler insertHandler;

	private UpdateHandler updateHandler;

	public void setCommandHandler(String command, CommandHandler handler) {
		commands.put(command, handler);
	}

	public void setQueryHandler(QueryHandler handler) {
		queryHandler = handler;
	}

	public void setInsertHandler(InsertHandler handler) {
		insertHandler = handler;
	}

	public void setUpdateHandler(UpdateHandler handler) {
		updateHandler = handler;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		Message request = (Message) e.getMessage();
		if (request instanceof QueryMessage) {
			QueryMessage query = (QueryMessage) request;
			String fc = query.getFullCollectionName();
			String[] db = fc.split("\\.", 2);

			if ("$cmd".equals(db[1])) {
				BSONObject command = query.getQuery();
				String c = command.keySet().iterator().next();
				CommandHandler handler = commands.get(c);
				if (handler != null) {
					BSONObject result = handler.handleCommand(db[0], command);
					e.getChannel().write(
							ReplyMessage.reply((Message) e.getMessage(), 0, 0,
									0, result).getBytes());
					return;
				}

				throw new UnsupportedOperationException("command " + c + " "
						+ request.toString());

			}
			if (queryHandler != null) {
				BSONObject[] result = queryHandler.handleQuery(db[0], db[1],
						query.getQuery());
				e.getChannel().write(
						ReplyMessage.reply((Message) e.getMessage(), 0, 0, 0,
								result).getBytes());
				return;
			}

		}
		if (request instanceof InsertMessage) {
			if (insertHandler != null) {
				InsertMessage insert = (InsertMessage) request;
				String fc = insert.getFullCollectionName();
				String[] db = fc.split("\\.", 2);
				insertHandler.handleInsert(db[0], db[1], false, insert
						.documents());
				return;
			}
		}

		if (request instanceof UpdateMessage) {
			if (updateHandler != null) {
				UpdateMessage update = (UpdateMessage) request;
				String fc = update.getFullCollectionName();
				String[] db = fc.split("\\.", 2);
				updateHandler.handleUpdate(db[0], db[1], false, false, update
						.getSelector(), update.getUpdate());
				return;
			}
		}

		throw new UnsupportedOperationException(request.toString());

	}
}
