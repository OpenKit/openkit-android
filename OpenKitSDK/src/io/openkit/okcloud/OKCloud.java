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

import java.util.HashMap;
import java.util.LinkedHashMap;
import io.openkit.*;

import org.codehaus.jackson.map.ObjectMapper;

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
		String objRep;
		StringBuilder strOut = new StringBuilder();
		
		if(OpenKit.getCurrentUser() == null) {
			h.complete(null, new OKCloudException("User is not logged in. User must be logged in when making cloud requests."));
			return;
		}
		
		if (!encodeObj(o, strOut)) {
			h.complete(null, new OKCloudException("Could not serialize this object."));
			return;
		}
		
		objRep = strOut.toString();
		final Object decodedObj = decodeStr(objRep);
		if (decodedObj == null) {
			h.complete(null, new OKCloudException("Serialization of object succeeded, but deserialization did not."));
			return;
		}
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user_id", String.valueOf(OpenKit.getCurrentUser().getOKUserID()));
		params.put("field_key", key);
		params.put("field_value", objRep);
		
		OKCloudAsyncRequest req = new OKCloudAsyncRequest("developer_data", "POST", params);
		req.performWithCompletionHandler(new OKCloudAsyncRequest.CompletionHandler() {
			
			@Override
			public void complete(String response, OKCloudException e) {
				h.complete(decodedObj, e);
			}
			
		});
	}

	private void iget(final String key, final OKCloudHandler h) {
		
		if(OpenKit.getCurrentUser() == null) {
			h.complete(null, new OKCloudException("User is not logged in. User must be logged in when making cloud requests."));
			return;
		}
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user_id", String.valueOf(OpenKit.getCurrentUser().getOKUserID()));
		String path = String.format("developer_data/%s", key);
		
		OKCloudAsyncRequest req = new OKCloudAsyncRequest(path, "GET", params);
		req.performWithCompletionHandler(new OKCloudAsyncRequest.CompletionHandler() {
			
			@Override
			public void complete(String response, OKCloudException e) {
				Object retVal = null;
				OKCloudException retErr = e;
				if (retErr == null) {
					ObjectMapper mapper = new ObjectMapper();
					try {
						LinkedHashMap<?,?> resObj = mapper.readValue(response, LinkedHashMap.class);
						retVal = resObj.get(key);
					} catch(Exception badError) {
						retErr = new OKCloudException("Bad stuff is happening.");
					}
				}
				h.complete(retVal, retErr);
			}
			
		});
	}
}
