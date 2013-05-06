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

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import io.openkit.asynchttp.*;

public class OKHTTPClient {
	
	private static AsyncHttpClient initializeClient()
	{
		AsyncHttpClient asyncClient = new AsyncHttpClient();
		asyncClient.addHeader("Content-Type", "application/json");
		asyncClient.addHeader("Accept", "application/json");
		asyncClient.setTimeout(6000);
		return asyncClient;
	}
	
	private static String BASE_URL = "http://10.0.1.24:3000/";
		
	private static AsyncHttpClient client = initializeClient();
	
	public String getOKAppID()
	{
		return OpenKit.getOKAppID();
	}
	
	public static void setEndpoint(String endpoint)
	{
		BASE_URL = endpoint;
	}
	
	public static void get(String url, RequestParams params, 
			AsyncHttpResponseHandler responseHandler)
	{	
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}
	
	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler)
	{
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}
	
	public static void postJSON(String url, JSONObject requestParams, AsyncHttpResponseHandler responseHandler)
	{
		StringEntity sEntity = getJSONString(requestParams);
		
		if(sEntity == null) {
			responseHandler.onFailure(new Throwable("JSON encoding error"), "JSON encoding error");
		}
		else {
			client.post(null, getAbsoluteUrl(url), sEntity, "application/json", responseHandler);
		}
	}
	
	public static void putJSON(String url, JSONObject requestParams, AsyncHttpResponseHandler responseHandler)
	{
		StringEntity sEntity = getJSONString(requestParams);
		
		if(sEntity == null) {
			responseHandler.onFailure(new Throwable("JSON encoding error"), "JSON encoding error");
		}
		else {
			client.put(null, getAbsoluteUrl(url), sEntity, "application/json", responseHandler);
		}
	}
	
	private static StringEntity getJSONString(JSONObject jsonObject)
	{
		StringEntity sEntity = null;
		try {
			sEntity = new StringEntity(jsonObject.toString());
			sEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			return sEntity;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	private static String getAbsoluteUrl(String relativeURL)
	{
		if(!BASE_URL.endsWith("/"))
			return BASE_URL + "/" + relativeURL;
		else
			return BASE_URL + relativeURL;
	}

}
