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

import org.bson.BSON;
import org.bson.BSONException;
import org.bson.BSONObject;
import org.bson.io.Bits;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class MessageDecoder extends FrameDecoder {

	static final int readInt32(ChannelBuffer buffer, byte[] fourBytes) {
		buffer.readBytes(fourBytes);
		return Bits.readInt(fourBytes);
	}

	static final long readInt64(ChannelBuffer buffer, byte[] eightBytes) {
		buffer.readBytes(eightBytes);
		return Bits.readLong(eightBytes);
	}

	static final BSONObject readDocument(ChannelBuffer buffer, byte[] fourBytes) {
		int length = readInt32(buffer, fourBytes);
		byte[] bson = new byte[length];
		buffer.readBytes(bson, 4, length - 4);
		System.arraycopy(fourBytes, 0, bson, 0, 4);
		return BSON.decode(bson);

	}

	static final String readCString(ChannelBuffer buffer) {
		// find the terminating zero-byte
		int length = buffer.bytesBefore((byte) 0);
		byte[] bytes = new byte[length];
		buffer.readBytes(bytes);
		// also skip the terminator
		buffer.readByte();
		try {
			return new String(bytes, "UTF-8");
		} catch (java.io.UnsupportedEncodingException uee) {
			throw new BSONException("impossible", uee);
		}
	}

	static final String readUTF8String(ChannelBuffer buffer, byte[] fourBytes) {
		int size = readInt32(buffer, fourBytes);
		// this is just protection in case it's corrupted, to avoid huge strings
		if (size <= 0 || size > (32 * 1024 * 1024))
			throw new BSONException("bad string size: " + size);

		byte[] b = new byte[size];
		buffer.readBytes(b);

		try {
			return new String(b, 0, size - 1, "UTF-8");
		} catch (java.io.UnsupportedEncodingException uee) {
			throw new BSONException("impossible", uee);
		}
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {

		// struct MsgHeader {
		// int32 messageLength; // total message size, including this
		// int32 requestID; // identifier for this message
		// int32 responseTo; // requestID from the original request
		// // (used in reponses from db)
		// int32 opCode; // request type - see table below
		// }

		if (buffer.readableBytes() < 4)
			return null;
		byte[] fourBytes = new byte[4];
		buffer.markReaderIndex();
		int messageLength = readInt32(buffer, fourBytes);
		buffer.resetReaderIndex();

		if (buffer.readableBytes() < messageLength) {
			return null;
		}

		return Message.readMessage(buffer, messageLength);

	}
}
