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

import io.openkit.asynchttp.AsyncHttpClient;
import io.openkit.asynchttp.AsyncHttpResponseHandler;
import io.openkit.asynchttp.RequestParams;

import java.io.UnsupportedEncodingException;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;


public class OKHTTPClient {

	public static final String DEFAULT_ENDPOINT = "http://development.openkit.io/";

	private static AsyncHttpClient initializeClient()
	{
		AsyncHttpClient asyncClient = new AsyncHttpClient();
		asyncClient.addHeader("Content-Type", "application/json");
		asyncClient.addHeader("Accept", "application/json");
		asyncClient.setTimeout(10000);
		return asyncClient;
	}

	private static String BASE_URL = DEFAULT_ENDPOINT;

	private static AsyncHttpClient client = initializeClient();
	private static CommonsHttpOAuthConsumer oauthConsumer = null;

	public String getAppKey()
	{
		return OpenKit.getAppKey();
	}

	public static void setEndpoint(String endpoint)
	{
		BASE_URL = endpoint;
	}

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
	{
		HttpGet request = new HttpGet(AsyncHttpClient.getUrlWithQueryString(getAbsoluteUrl(url), params));
		sign(request);
		client.get(request, responseHandler);
	}


	public static void postJSON(String url, JSONObject requestParams, AsyncHttpResponseHandler responseHandler)
	{
		StringEntity sEntity = getJSONString(requestParams);
		HttpPost request = new HttpPost(getAbsoluteUrl(url));

		if(sEntity == null) {
			responseHandler.onFailure(new Throwable("JSON encoding error"), "JSON encoding error");
		}
		else {
			request.setEntity(sEntity);
			sign(request);
			client.post(request, "application/json", responseHandler);
		}
	}

	public static void putJSON(String url, JSONObject requestParams, AsyncHttpResponseHandler responseHandler)
	{
		StringEntity sEntity = getJSONString(requestParams);
		HttpPut request = new HttpPut(getAbsoluteUrl(url));

		if(sEntity == null) {
			responseHandler.onFailure(new Throwable("JSON encoding error"), "JSON encoding error");
		}
		else {
			request.setEntity(sEntity);
			sign(request);
			client.put(request, "application/json", responseHandler);
		}
	}

	private static void sign(HttpRequest request)
	{
		if (oauthConsumer == null) {
			oauthConsumer = new CommonsHttpOAuthConsumer(OKManager.INSTANCE.getAppKey(), OKManager.INSTANCE.getSecretKey());
		}
		try {
			oauthConsumer.sign(request);
		} catch (OAuthMessageSignerException e) {
			OKLog.v("Oauth Signature Failed (1).");
			oauthConsumer = null;
		} catch (OAuthExpectationFailedException e) {
			OKLog.v("Oauth Signature Failed (2).");
			oauthConsumer = null;
		} catch (OAuthCommunicationException e) {
			OKLog.v("Oauth Signature Failed (3).");
			oauthConsumer = null;
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
		if(BASE_URL.endsWith("/")) {
			if(relativeURL.startsWith("/")) {
				return BASE_URL + relativeURL.substring(1);
			} else {
				return BASE_URL + relativeURL;
			}
		} else {
			if(relativeURL.startsWith("/")) {
				return BASE_URL + relativeURL;
			} else {
				return BASE_URL + '/' + relativeURL;
			}
		}
	}

}
