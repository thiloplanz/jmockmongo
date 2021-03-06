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

public class Result {

	// ok - true indicates the getLastError command completed successfully. This
	// does NOT indicate there wasn't a last error.
	private final boolean ok;

	private final int n;

	private final String error;

	public Result(int n) {
		ok = true;
		this.n = n;
		this.error = null;
	}

	public Result(String error) {
		ok = true;
		n = 0;
		this.error = error;
	}

	public boolean isOk() {
		return ok;
	}

	public int getN() {
		return n;
	}

	public BSONObject toBSON() {
		BasicBSONObject r = new BasicBSONObject("ok", ok ? 1 : 0)
				.append("n", n);
		if (error != null)
			r.append("err", error);
		return r;
	}

}
