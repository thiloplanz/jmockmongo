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

import static jmockmongo.wire.MessageDecoder.readInt32;

import org.bson.BSON;
import org.bson.BSONObject;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

class ReplyMessage extends Message {

	static final int OP_CODE_REPLY = 1;

	// struct {
	// MsgHeader header; // standard message header
	// int32 responseFlags; // bit vector - see details below
	// int64 cursorID; // cursor id if client needs to do get more's
	// int32 startingFrom; // where in the cursor this reply is starting
	// int32 numberReturned; // number of documents in the reply
	// document* documents; // documents
	// }
	//	

	private final int responseFlags;

	private final long cursorId;

	private final int startingFrom;

	private final int numberReturned;

	ReplyMessage(ChannelBuffer data) {
		super(data, OP_CODE_REPLY);
		byte[] fourBytes = new byte[4];
		byte[] eightBytes = new byte[8];
		responseFlags = readInt32(data, fourBytes);
		cursorId = MessageDecoder.readInt64(data, eightBytes);
		startingFrom = readInt32(data, fourBytes);
		numberReturned = readInt32(data, fourBytes);
	}

	@Override
	public String toString() {
		if (cursorId != 0) {
			return super.toString() + " [cursor #" + cursorId + "@"
					+ startingFrom + "(" + numberReturned + ")]";
		}
		return super.toString() + " with " + numberReturned + " docs";
	}

	static ReplyMessage reply(Message request, int responseFlags,
			long cursorId, int startingFrom, BSONObject... docs) {

		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		writeInt(buffer, responseFlags);
		writeLong(buffer, cursorId);
		writeInt(buffer, startingFrom);
		writeInt(buffer, docs.length);
		for (BSONObject doc : docs)
			buffer.writeBytes(BSON.encode(doc));
		ChannelBuffer header = ChannelBuffers.buffer(16);
		outputMessageHeader(header, buffer.readableBytes() + 16, 1, request
				.getRequestId(), OP_CODE_REPLY);

		return new ReplyMessage(ChannelBuffers.wrappedBuffer(header, buffer));
	}
}
