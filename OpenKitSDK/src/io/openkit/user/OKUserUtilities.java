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

package io.openkit.user;

import io.openkit.OKHTTPClient;
import io.openkit.OKLog;
import io.openkit.OKLoginUpdateNickFragment;
import io.openkit.OKUser;
import io.openkit.OpenKit;
import io.openkit.asynchttp.OKJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.FragmentManager;
import android.util.Log;

public class OKUserUtilities 
{
	public static void showUpdateNickDialog(FragmentManager fm)
	{
		OKLoginUpdateNickFragment nickDialog = new OKLoginUpdateNickFragment();
		nickDialog.show(fm, "OKLoginUpdateNickFragment");
	}
		
	
	public static void updateUserNick(final OKUser user, String newNick, final UpdateUserRequestHandler requestHandler)
	{
		//Setup the request parameters
		JSONObject requestParams = new JSONObject();
		try {
			JSONObject userDict = new JSONObject();
			userDict.put("nick", newNick);
			
			requestParams.put("app_key", OpenKit.getOKAppID());
			requestParams.put("user", userDict);
			
		} catch (JSONException e) {
			Log.e("OpenKit", "Error creating JSON request for updating user nick: " + e);
			requestHandler.onFail(e);
		}
		
		String requestPath = "/users/" + Integer.toString(user.getOKUserID());
		
		OKHTTPClient.putJSON(requestPath, requestParams, new OKJsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(JSONObject object) {
				OKUser responseUser = new OKUser(object);
				
				if(responseUser.getOKUserID() == user.getOKUserID()){
					requestHandler.onSuccess(responseUser);
					OKLog.v("Succesfully updated user nickname");
				}
				else {
					requestHandler.onFail(new Throwable("Unknown error from OpenKit when trying to update user nick"));
				}
			}
			
			@Override
			public void onSuccess(JSONArray array) {
				requestHandler.onFail(new Throwable("Received a JSON array when expecting a JSON object"));
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				requestHandler.onFail(error);
			}
			
			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				requestHandler.onFail(e);
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				requestHandler.onFail(e);
			}
		});
	}

	public static JSONObject getJSONRepresentationOfUser(OKUser user)
	{
		JSONObject object = new JSONObject();

		try {
			object.putOpt("nick", user.getUserNick());
			object.putOpt("id", user.getOKUserID());
			object.putOpt("fb_id", user.getFBUserID());
			object.putOpt("twitter_id", user.getTwitterUserID());
		}
		catch (JSONException e) {
			Log.e("Tag","Exception thrown when converting user to JSON object: " + e);
		}
		return object;
	}
	
	

}
