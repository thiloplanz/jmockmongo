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

import java.util.Iterator;

import org.bson.BSONObject;
import org.jboss.netty.buffer.ChannelBuffer;

class InsertMessage extends Message {

	static final int OP_CODE_INSERT = 2002;

	// struct {
	// MsgHeader header; // standard message header
	// int32 flags; // bit vector - see below
	// cstring fullCollectionName; // "dbname.collectionname"
	// document* documents; // one or more documents to insert into the
	// collection
	// }

	private final int flags;

	private final String fullCollectionName;

	String getFullCollectionName() {
		return fullCollectionName;
	}

	InsertMessage(ChannelBuffer data) {
		super(data, OP_CODE_INSERT);
		byte[] fourBytes = new byte[4];
		flags = readInt32(data, fourBytes);
		fullCollectionName = readCString(data);
	}

	Iterator<BSONObject> documents() {
		final ChannelBuffer copy = data.slice();
		final byte[] fourBytes = new byte[4];
		return new Iterator<BSONObject>() {

			public boolean hasNext() {
				return copy.readable();
			}

			public BSONObject next() {
				return readDocument(copy, fourBytes);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public String toString() {
		return super.toString() + " collection: " + fullCollectionName;
	}
}
