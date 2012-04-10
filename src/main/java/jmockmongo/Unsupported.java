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

import java.util.Arrays;

import org.bson.BSONObject;

/**
 * Collection of static helper methods to check for unsupported operations.
 * 
 */

public final class Unsupported {

	public static void supportedFields(BSONObject o, String... fields) {
		f: for (String f : o.keySet()) {
			for (String check : fields) {
				if (check.equals(f))
					continue f;
			}
			throw new UnsupportedOperationException("only "
					+ Arrays.toString(fields) + " are supported: " + o);
		}
	}

	public static void supportedAndRequiredFields(BSONObject o,
			String... fields) {
		for (String f : fields) {
			if (!o.containsField(f))
				throw new UnsupportedOperationException(
						"missing required field '" + f + "': " + o);
		}
		supportedFields(o, fields);
	}

}
