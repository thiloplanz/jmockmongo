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

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import jmockmongo.commands.Count;
import jmockmongo.commands.IsMaster;
import jmockmongo.commands.ListDatabases;
import jmockmongo.wire.LoggingHandler;
import jmockmongo.wire.MessageDecoder;
import jmockmongo.wire.ReplyHandler;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.mongodb.DBAddress;

public class MockMongo {

	private ChannelGroup channels;

	private ServerBootstrap bootstrap;

	private ConcurrentHashMap<String, MockDB> data;

	MockDB getDB(String database) {
		return data.get(database);
	}

	MockDB getOrCreateDB(String database) {
		MockDB db = data.get(database);
		if (db != null)
			return db;
		data.putIfAbsent(database, new MockDB(database));
		return data.get(database);
	}

	MockDBCollection getCollection(String fullCollectionName) {
		String[] fc = fullCollectionName.split("\\.", 2);
		MockDB db = getDB(fc[0]);
		if (db == null)
			return null;
		return db.getCollection(fc[1]);
	}

	MockDBCollection getOrCreateCollection(String fullCollectionName) {
		String[] fc = fullCollectionName.split("\\.", 2);
		return getOrCreateDB(fc[0]).getOrCreateCollection(fc[1]);
	}

	MockDBCollection getCollection(String database, String collectionName) {
		MockDB db = getDB(database);
		if (db == null)
			return null;
		return db.getCollection(collectionName);
	}

	public void start() {
		data = new ConcurrentHashMap<String, MockDB>();
		final ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors
						.newCachedThreadPool());

		channels = new DefaultChannelGroup("jmockmongo");
		bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {

				final ReplyHandler handler = new ReplyHandler();
				handler.setCommandHandler("isMaster", new IsMaster());
				handler.setCommandHandler("listDatabases", new ListDatabases());
				handler.setCommandHandler("count", new Count());

				handler
						.setQueryHandler(new DefaultQueryHandler(MockMongo.this));

				handler.setInsertHandler(new DefaultInsertHandler(
						MockMongo.this));

				handler.setUpdateHandler(new DefaultUpdateHandler(
						MockMongo.this));

				handler.setDeleteHandler(new DefaultDeleteHandler(
						MockMongo.this));

				return Channels.pipeline(new SimpleChannelHandler() {

					@Override
					public void channelOpen(ChannelHandlerContext ctx,
							ChannelStateEvent e) throws Exception {
						channels.add(e.getChannel());
						super.channelOpen(ctx, e);
					}

				}, new MessageDecoder(), new LoggingHandler(), handler);
			}
		});

		channels.add(bootstrap.bind(new InetSocketAddress(DBAddress
				.defaultPort())));
	}

	public void stop() {
		if (channels != null) {
			channels.close().awaitUninterruptibly();
		}
		channels = null;
		if (bootstrap != null) {
			bootstrap.releaseExternalResources();
		}
		bootstrap = null;

	}

}
