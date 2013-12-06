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
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;


public class OKHTTPClient {

	/* Client SDK default parameters */
	public static final String DEFAULT_ENDPOINT = "http://api.openkit.io/";
	public static final String SERVER_API_VERSION = "v1";

	/* Networking error codes */
	public static final int UNSUBSCRIBED_USER_ERROR_CODE = 410;

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
		if(endpoint != null) {
			BASE_URL = endpoint;
		} else {
			BASE_URL = DEFAULT_ENDPOINT;
		}
	}

	public static String getEndpoint()
	{
		return BASE_URL;
	}

	public static void get(String relativeUrl, RequestParams params, AsyncHttpResponseHandler responseHandler)
	{
		HttpGet request = new HttpGet(AsyncHttpClient.getUrlWithQueryString(getAbsoluteUrl(relativeUrl), params));
		sign(request);
		client.get(request, responseHandler);
	}


	public static void postJSON(String relativeUrl, JSONObject requestParams, AsyncHttpResponseHandler responseHandler)
	{
		StringEntity sEntity = getJSONString(requestParams);
		HttpPost request = new HttpPost(getAbsoluteUrl(relativeUrl));

		if(sEntity == null) {
			responseHandler.onFailure(new Throwable("JSON encoding error"), "JSON encoding error");
		}
		else {
			request.setEntity(sEntity);
			sign(request);
			client.post(request, "application/json", responseHandler);
		}
	}

	public static void putJSON(String relativeUrl, JSONObject requestParams, AsyncHttpResponseHandler responseHandler)
	{
		StringEntity sEntity = getJSONString(requestParams);
		HttpPut request = new HttpPut(getAbsoluteUrl(relativeUrl));

		if(sEntity == null) {
			responseHandler.onFailure(new Throwable("JSON encoding error"), "JSON encoding error");
		}
		else {
			request.setEntity(sEntity);
			sign(request);
			client.put(request, "application/json", responseHandler);
		}
	}

	public static boolean isErrorCodeInFourHundreds(Throwable e)
	{
		if(e instanceof HttpResponseException) {
			HttpResponseException responseException = (HttpResponseException)e;
			int statusCode = responseException.getStatusCode();

			return (statusCode >= 400 && statusCode < 500);
		} else {
			return false;
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
			sEntity = new StringEntity(jsonObject.toString(),HTTP.UTF_8);
			sEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			return sEntity;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private static String getAbsoluteUrl(String relativeURL)
	{
		OKURLBuilder urlBuilder = new OKURLBuilder(BASE_URL);
		urlBuilder.appendPathComponent(SERVER_API_VERSION);
		urlBuilder.appendPathComponent(relativeURL);
		return urlBuilder.build();
	}

}
