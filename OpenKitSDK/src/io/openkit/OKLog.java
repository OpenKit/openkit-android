/**
 * Copyright 2012 OpenKit
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openkit;

import java.util.Locale;

import android.util.Log;

public class OKLog {
	static final boolean VERBOSE = true;

	public static void d(String format, Object... args) {
		Log.d("OpenKit", String.format(Locale.getDefault(), format, args));
	}

	/* Have to add versions without multiple args because there are lots of log statements that use
	 * string casting and sometimes the String.Format throws an exception.
	 */
	public static void d(String message) {
		Log.d("OpenKit", message);
	}

	public static void v(String message) {
		if (VERBOSE) {
			Log.v("OpenKit",message);
		}
	}

	public static void v(String format, Object... args) {
		if (VERBOSE) {
			Log.v("OpenKit", String.format(Locale.getDefault(), format, args));
		}
	}

}
