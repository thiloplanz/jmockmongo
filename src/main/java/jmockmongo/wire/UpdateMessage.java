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

import static jmockmongo.wire.MessageDecoder.readCString;
import static jmockmongo.wire.MessageDecoder.readDocument;
import static jmockmongo.wire.MessageDecoder.readInt32;

import org.bson.BSONObject;
import org.jboss.netty.buffer.ChannelBuffer;

public class UpdateMessage extends Message {

	static final int OP_CODE_UPDATE = 2001;

	// struct OP_UPDATE {
	// MsgHeader header; // standard message header
	// int32 ZERO; // 0 - reserved for future use
	// cstring fullCollectionName; // "dbname.collectionname"
	// int32 flags; // bit vector. see below
	// document selector; // the query to select the document
	// document update; // specification of the update to perform
	// }

	private final String fullCollectionName;

	String getFullCollectionName() {
		return fullCollectionName;
	}

	private final int flags;

	private BSONObject selector;

	private BSONObject update;

	UpdateMessage(ChannelBuffer data) {
		super(data, OP_CODE_UPDATE);
		byte[] fourBytes = new byte[4];
		// int32 ZERO
		readInt32(data, fourBytes);
		fullCollectionName = readCString(data);
		flags = readInt32(data, fourBytes);
	}

	BSONObject getSelector() {
		if (selector == null) {
			byte[] fourBytes = new byte[4];
			selector = readDocument(data, fourBytes);
			update = readDocument(data, fourBytes);
		}
		return selector;
	}

	BSONObject getUpdate() {
		getSelector();
		return update;
	}

	@Override
	public String toString() {
		return super.toString() + " collection: " + fullCollectionName;
	}
}
