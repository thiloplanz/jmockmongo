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

class QueryMessage extends Message {

	static final int OP_CODE_QUERY = 2004;

	// struct OP_QUERY {
	// MsgHeader header; // standard message header
	// int32 flags; // bit vector of query options. See below for details.
	// cstring fullCollectionName; // "dbname.collectionname"
	// int32 numberToSkip; // number of documents to skip
	// int32 numberToReturn; // number of documents to return
	// // in the first OP_REPLY batch
	// document query; // query object. See below for details.
	// [ document returnFieldSelector; ] // Optional. Selector indicating the
	// fields
	// // to return. See below for details.
	// }

	private final int flags;

	private final String fullCollectionName;

	private final int numberToSkip;

	private final int numberToReturn;

	private BSONObject query;

	private BSONObject returnFieldSelector;

	QueryMessage(ChannelBuffer data) {
		super(data, OP_CODE_QUERY);
		byte[] fourBytes = new byte[4];
		flags = readInt32(data, fourBytes);
		fullCollectionName = readCString(data);
		numberToSkip = readInt32(data, fourBytes);
		numberToReturn = readInt32(data, fourBytes);
	}

	BSONObject getQuery() {
		if (query == null) {
			byte[] fourBytes = new byte[4];
			query = readDocument(data, fourBytes);
			if (data.readable())
				returnFieldSelector = readDocument(data, fourBytes);
		}
		return query;
	}

	BSONObject getReturnFieldSelector() {
		if (query == null)
			getQuery();
		return returnFieldSelector;
	}

	String getFullCollectionName() {
		return fullCollectionName;
	}

	@Override
	public String toString() {
		return super.toString()
				+ " collection: "
				+ fullCollectionName
				+ " query: "
				+ getQuery()
				+ (numberToSkip > 0 ? " skip: " + numberToSkip : "")
				+ (numberToReturn > 0 ? " return: " + numberToReturn : "")
				+ (returnFieldSelector != null ? " returnFieldSelector: "
						+ returnFieldSelector : "");
	}

}
