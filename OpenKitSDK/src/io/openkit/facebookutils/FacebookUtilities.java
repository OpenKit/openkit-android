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

package io.openkit.facebookutils;

import io.openkit.*;
import io.openkit.asynchttp.OKJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.openkit.facebook.*;
import io.openkit.facebook.Request.GraphUserCallback;
import io.openkit.facebook.model.GraphUser;



public class FacebookUtilities 
{
	
	public interface CreateOKUserRequestHandler
	{
		public void onSuccess(OKUser user);
		public void onFail(Error error);
	}
	
	public static void AuthorizeUserWithFacebook(final CreateOKUserRequestHandler requestHandler)
	{
		Session session = Session.getActiveSession();
		
		if(session.isOpened())
		{
			//Perform a 'ME' request to get user info
			Request.executeMeRequestAsync(session, new GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if(user != null) {
						String userID = user.getId();
						String userNick = user.getName();
						
						CreateOKUserWithFacebookID(userID, userNick, requestHandler);	
					}
				}
			});
		}
		else
		{
			requestHandler.onFail(new Error("Not current logged into FB"));
		}
	}
		
	public static void CreateOKUserWithFacebookID(String fbID, String userNick, final CreateOKUserRequestHandler requestHandler)
	{
		JSONObject jsonParams = new JSONObject();
		
		try 
		{	
			jsonParams.put("nick", userNick);
			jsonParams.put("fb_id", fbID);
			jsonParams.put("app_key", OpenKit.getOKAppID());
		} catch (JSONException e1) {
			requestHandler.onFail(new Error("Error creating JSON params for request: " + e1));
		} 
		
		OKLog.d("Creating user with FB id");
		
		OKHTTPClient.postJSON("users", jsonParams, new OKJsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(JSONObject object) {
				OKUser currentUser = new OKUser(object);
				requestHandler.onSuccess(currentUser);
			}
			
			@Override
			public void onSuccess(JSONArray array) {
				requestHandler.onFail(new Error("Request cameback as an array when expecting a object: " + array));
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				requestHandler.onFail(new Error("Error: " + error + " content: " + content));
			}
			
			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				requestHandler.onFail(new Error("Error: " + e + " JSON response: " + errorResponse));
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				requestHandler.onFail(new Error("Error: " + e + " JSON response: " + errorResponse));
			}
		});
	}

}
