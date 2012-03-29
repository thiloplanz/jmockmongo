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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

abstract class Message {

	// struct MsgHeader {
	// int32 messageLength; // total message size, including this
	// int32 requestID; // identifier for this message
	// int32 responseTo; // requestID from the original request
	// // (used in reponses from db)
	// int32 opCode; // request type - see table below
	// }

	private final int messageLength;

	private final int requestId;

	private final int responseTo;

	protected final int opCode;

	protected final ChannelBuffer data;

	Message(ChannelBuffer data, int expectedOpCode) {
		byte[] fourBytes = new byte[4];

		int check = data.readableBytes();
		this.messageLength = readInt32(data, fourBytes);
		if (check != messageLength) {
			throw new IllegalArgumentException(String.format(
					"message buffer has unexpected size (%d instead of %d)",
					check, messageLength));
		}
		this.requestId = readInt32(data, fourBytes);
		this.responseTo = readInt32(data, fourBytes);
		this.opCode = readInt32(data, fourBytes);
		this.data = data;
		if (opCode != expectedOpCode) {
			throw new IllegalArgumentException(String.format(
					"message buffer has unexpected opcode (%d instead of %d)",
					opCode, expectedOpCode));
		}
	}

	static void writeInt(ChannelBuffer buffer, int x) {
		buffer.writeByte(x >> 0);
		buffer.writeByte(x >> 8);
		buffer.writeByte(x >> 16);
		buffer.writeByte(x >> 24);
	}

	static void writeLong(ChannelBuffer buffer, long x) {
		buffer.writeByte((byte) (0xFFL & (x >> 0)));
		buffer.writeByte((byte) (0xFFL & (x >> 8)));
		buffer.writeByte((byte) (0xFFL & (x >> 16)));
		buffer.writeByte((byte) (0xFFL & (x >> 24)));
		buffer.writeByte((byte) (0xFFL & (x >> 32)));
		buffer.writeByte((byte) (0xFFL & (x >> 40)));
		buffer.writeByte((byte) (0xFFL & (x >> 48)));
		buffer.writeByte((byte) (0xFFL & (x >> 56)));
	}

	static void outputMessageHeader(ChannelBuffer buffer, int messageLength,
			int requestId, int responseTo, int opCode) {
		writeInt(buffer, messageLength);
		writeInt(buffer, requestId);
		writeInt(buffer, responseTo);
		writeInt(buffer, opCode);
	}

	static Message readMessage(ChannelBuffer buffer, int messageLength) {
		byte[] fourBytes = new byte[4];
		// messageLength
		readInt32(buffer, fourBytes);
		// requestId
		readInt32(buffer, fourBytes);
		// responseTo
		readInt32(buffer, fourBytes);
		int opCode = readInt32(buffer, fourBytes);
		buffer.resetReaderIndex();
		switch (opCode) {
		case ReplyMessage.OP_CODE_REPLY:
			return new ReplyMessage(buffer.readBytes(messageLength));
		case QueryMessage.OP_CODE_QUERY:
			return new QueryMessage(buffer.readBytes(messageLength));
		case InsertMessage.OP_CODE_INSERT:
			return new InsertMessage(buffer.readBytes(messageLength));
		default:
			throw new IllegalArgumentException(String.format(
					"message buffer has unsupported opcode (%d)", opCode));

		}
	}

	String getOpCodeName() {
		switch (opCode) {
		case 1:
			return "OP_REPLY";
		case 1000:
			return "OP_MSG";
		case 2001:
			return "OP_UPDATE";
		case 2002:
			return "OP_INSERT";
		case 2003:
			return "RESERVED";
		case 2004:
			return "OP_QUERY";
		case 2005:
			return "OP_GET_MORE";
		case 2006:
			return "OP_DELETE";
		case 2007:
			return "OP_KILL_CURSORS";
		}
		return String.format("?(%d)", opCode);
	}

	ChannelBuffer getBytes() {
		return ChannelBuffers.unmodifiableBuffer(data.slice(0, messageLength));
	}

	int getRequestId() {
		return requestId;
	}

	@Override
	public String toString() {
		if (responseTo != 0) {
			return "[" + getOpCodeName() + "] Re:#" + responseTo + " ("
					+ messageLength + " bytes)";
		}
		return "[" + getOpCodeName() + "] #" + requestId + " (" + messageLength
				+ " bytes)";
	}

}
