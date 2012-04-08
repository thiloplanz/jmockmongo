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

import junit.framework.TestCase;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public class BSONUtilsTest extends TestCase {

	public void testAddToSet() {
		BSONObject b = new BasicBSONObject();
		BSONUtils.addToSet(b, "set", "x");
		assertEquals("{ \"set\" : [ \"x\"]}", b.toString());
		BSONUtils.addToSet(b, "set", "x");
		assertEquals("{ \"set\" : [ \"x\"]}", b.toString());
		BSONUtils.addToSet(b, "set", 1);
		assertEquals("{ \"set\" : [ \"x\" , 1]}", b.toString());

		BSONUtils.addToSet(b, "set", new byte[1]);
		assertEquals("{ \"set\" : [ \"x\" , 1 , <Binary Data>]}", b.toString());
		BSONUtils.addToSet(b, "set", new byte[1]);
		assertEquals("{ \"set\" : [ \"x\" , 1 , <Binary Data>]}", b.toString());
	}

}
