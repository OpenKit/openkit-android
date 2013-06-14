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

import io.openkit.*;
import io.openkit.asynchttp.*;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;


public class OKCloudAsyncRequest {

	public interface CompletionHandler {
		public void complete(String response, OKCloudException e);
	}

	public String relativePath;
	public String requestMethod;
	public HashMap<String, String> params;

	//private HashMap<String, String> headers;

	// ==============================================================================
	// Public
	// ==============================================================================
	public OKCloudAsyncRequest(String relativePath, String requestMethod,
			HashMap<String, String> params) {
		this.relativePath = relativePath;
		this.requestMethod = requestMethod;
		this.params = params;
	}

	public void performWithCompletionHandler(final CompletionHandler h) {
		if (requestMethod.equals("POST")) {
			postWithCompletionHandler(relativePath, h);
		} else if (requestMethod.equals("GET")){
			getWithCompletionHandler(relativePath, h);
		} else {
			OKLog.d("Unknown request method!");
		}
	}
	
	// ==============================================================================
	// Private
	// ==============================================================================
	private void getWithCompletionHandler(String relativeURL, final CompletionHandler h) {
		
		RequestParams requestParams = new RequestParams(params);
		requestParams.put("app_key", OpenKit.getAppKey());
		
		OKHTTPClient.get(relativeURL, requestParams, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				OKLog.v("GET started.");
			}

			@Override
			public void onSuccess(String response) {
				OKLog.v("GET succeeded, got response: %s", response);
				h.complete(response, null);
			}

			@Override
			public void onFailure(Throwable e, String response) {
				String errMessage = e.getMessage();
				OKLog.v("GET failed: %s", errMessage);
				h.complete(null, new OKCloudException(errMessage));
			}

			@Override
			public void onFinish() {
				OKLog.v("GET finished.");
			}
		});
	}

	private void postWithCompletionHandler(String relativeURL, final CompletionHandler h) 
	{
		JSONObject requestParamsJSON = new JSONObject();
		try {
			requestParamsJSON.put("app_key", OpenKit.getAppKey());
			for (Map.Entry<String, String> entry : params.entrySet()) {
				requestParamsJSON.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException e) {
			h.complete(null,  new OKCloudException("Could not add post params to json object"));
			return;
		}
		
		OKHTTPClient.postJSON(relativeURL, requestParamsJSON, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				OKLog.v("POST started.");
			}

			@Override
			public void onSuccess(String response) {
				OKLog.v("POST succeeded, got response: %s", response);
				h.complete(response, null);
			}

			@Override
			public void onFailure(Throwable e, String response) {
				String errMessage = e.getMessage();
				OKLog.v("POST failed, %s", errMessage);
				h.complete(null, new OKCloudException(errMessage));
			}

			@Override
			public void onFinish() {
				OKLog.v("POST finished.");
			}
		});
	}

}
