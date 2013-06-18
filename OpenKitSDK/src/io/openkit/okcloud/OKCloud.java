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

package io.openkit.okcloud;

import io.openkit.OKHTTPClient;
import io.openkit.OKLog;
import io.openkit.OpenKit;
import io.openkit.asynchttp.OKJsonHttpResponseHandler;
import io.openkit.asynchttp.RequestParams;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OKCloud {

	//==============================================================================
	// Public API
	//==============================================================================
	public static void get(String key, OKCloudHandler h) {
		OKCloud c = new OKCloud();
		c.iget(key, h);
	}

	public static void set(Object o, String key, OKCloudHandler h) {
		OKCloud c = new OKCloud();
		c.iset(o, key, h);
	}


	//==============================================================================
	// Private
	//==============================================================================
	private boolean encodeObj(Object o, StringBuilder out) {
		boolean success = true;
		ObjectMapper mapper = new ObjectMapper();
		try {
			out.append(mapper.writeValueAsString(o));
		} catch (Exception e) {
			OKLog.d("Could not encode object!");
			success = false;
		}
		return success;
	}

	private Object decodeStr(String s) {
		ObjectMapper mapper = new ObjectMapper();
		Object o;
		try {
			o = mapper.readValue(s, Object.class);
		} catch (Exception e) {
			OKLog.d("Could not decode String!!");
			return null;
		}
		return o;
	}

	// Verifies that we can serialize and deserialize this object before
	// storing to redis.
	private void iset(Object o, String key, final OKCloudHandler h) {

		if(OpenKit.getCurrentUser() == null) {
			h.complete(null, new OKCloudException("User is not logged in. User must be logged in when making cloud requests."));
			return;
		}

		JSONObject x = new JSONObject();
		try {
			x.put("user_id", OpenKit.getCurrentUser().getOKUserID());
			x.put("field_key", key);
			x.put("field_value", o);
		} catch (JSONException e) {
			OKLog.d("Object passed to OKCloud.set() could not be set as JSON value.");
		}

		OKHTTPClient.postJSON("/developer_data", x, new OKJsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject object) {
				h.complete(object, null);
			}

			@Override
			public void onSuccess(JSONArray array) {
				OKLog.v("You should never see me.");
			}

			@Override
			public void onFailure(Throwable error, String content) {
				h.complete(null, new OKCloudException("Failed to store JSON."));
			}

			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				h.complete(null, new OKCloudException("Failed to store JSON."));
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				h.complete(null, new OKCloudException("Failed to store JSON."));
			}
		});
	}

	private void iget(final String key, final OKCloudHandler h) {

		if(OpenKit.getCurrentUser() == null) {
			h.complete(null, new OKCloudException("User is not logged in. User must be logged in when making cloud requests."));
			return;
		}

		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(OpenKit.getCurrentUser().getOKUserID()));
		OKHTTPClient.get(String.format("/developer_data/%s", key), params, new OKJsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject object) {
				Object x;
				try {
					x = object.get(key);
				} catch (JSONException e) {
					OKLog.d("Could not get object out of returned JSON.");
					return;
				}
				h.complete(x, null);
			}

			@Override
			public void onSuccess(JSONArray array) {
				OKLog.v("You should never see me.");
			}

			@Override
			public void onFailure(Throwable error, String content) {
				h.complete(null, new OKCloudException("Failed to store JSON."));
			}

			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				h.complete(null, new OKCloudException("Failed to store JSON."));
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				h.complete(null, new OKCloudException("Failed to store JSON."));
			}
		});
	}
}
