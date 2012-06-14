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

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import junit.framework.TestCase;

public class BSONComparatorTest extends TestCase {

	public void testInteger() {
		assertTrue(BSONComparator.INSTANCE.compare(1, 2) < 0);
		assertTrue(BSONComparator.INSTANCE.compare(1, 0) > 0);
		assertEquals(0, BSONComparator.INSTANCE.compare(2, 2));
		assertTrue(BSONComparator.INSTANCE.compare(1, 1.0d) > 0);
		assertTrue(BSONComparator.INSTANCE.compare(1, "1") > 0);
		assertTrue(BSONComparator.INSTANCE.compare(1, 1l) < 0);
	}

	public void testObject() {
		{
			BSONObject x = new BasicBSONObject("_id", "x");
			BSONObject y = new BasicBSONObject("_id", "y");
			assertEquals(0, BSONComparator.INSTANCE.compare(x, x));
			assertTrue(BSONComparator.INSTANCE.compare(x, y) < 0);
		}
		{
			BSONObject x = new BasicBSONObject("_id", new BasicBSONObject("x",
					1).append("y", 1));
			BSONObject y = new BasicBSONObject("_id", new BasicBSONObject("x",
					1).append("y", 2));
			assertEquals(0, BSONComparator.INSTANCE.compare(x, x));
			assertTrue(BSONComparator.INSTANCE.compare(x, y) < 0);
		}
		{
			BSONObject x = new BasicBSONObject("_id", new BasicBSONObject("x",
					1).append("y", 1));
			BSONObject y = new BasicBSONObject("_id", new BasicBSONObject("x",
					1).append("y", Integer.MAX_VALUE));
			assertEquals(0, BSONComparator.INSTANCE.compare(x, x));
			assertTrue(BSONComparator.INSTANCE.compare(x, y) < 0);
		}

	}
}
