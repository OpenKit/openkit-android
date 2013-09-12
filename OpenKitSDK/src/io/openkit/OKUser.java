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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class OKUser {

	private int OKUserID;
	private String userNick;
	private String FBUserID;
	private String googleID;
	private String customID;


	/**
	 * Creates OKUser with JSONObject
	 * @param userJSON JSON for user
	 */

	public OKUser(JSONObject userJSON)
	{
		super();
		initFromJSON(userJSON);
	}

	public OKUser() {
		super();
	}

	/**
	 * Logs the current user out of OpenKit and clears all cached data on the device associated
	 * with the user
	 * @param context Requires a context to remove cached data
	 */
	public static void logoutCurrentUser(Context context)
	{
		OKManager.INSTANCE.logoutCurrentUser(context);
	}

	/**
	 * Get the current logged in OKUser
	 * @return OKUser or null if the user hasn't logged in
	 */
	public static OKUser getCurrentUser()
	{
		return OpenKit.getCurrentUser();
	}

	private void initFromJSON(JSONObject userJSON)
	{
		// Set to 0 as default value
		this.OKUserID = 0;

		try {
			this.OKUserID = userJSON.getInt("id");
			this.userNick = userJSON.getString("nick");

			this.FBUserID = userJSON.optString("fb_id");
			this.googleID = userJSON.optString("google_id");
			this.customID = userJSON.optString("custom_id");

		} catch (JSONException e) {
			Log.e("OpenKit", "Error parsing user JSON: " + e.toString());
		}
	}

	public int getOKUserID()
	{
		return OKUserID;
	}

	public void setOKUserID(int aID)
	{
		this.OKUserID = aID;
	}

	public String getFBUserID()
	{
		return FBUserID;
	}

	public void setFBUserID(String aFBID)
	{
		if(OKJSONParser.isZeroStringLiteral(aFBID)) {
			aFBID = null;
		}
		this.FBUserID = aFBID;
	}

	public String getUserNick()
	{
		return userNick;
	}

	public void setUserNick(String aNick)
	{
		this.userNick = aNick;
	}

	public String getGoogleID() {
		return googleID;
	}

	public void setGoogleID(String googleID) {
		if(OKJSONParser.isZeroStringLiteral(googleID)) {
			googleID = null;
		}
		this.googleID = googleID;
	}

	public String getCustomID() {
		return customID;
	}

	public void setCustomID(String customID) {
		if(OKJSONParser.isZeroStringLiteral(customID)) {
			customID = null;
		}
		this.customID = customID;
	}

	@Override
	public String toString() {
		return "OKUser id: " + getOKUserID() + " nick: " + getUserNick() +  " fb_id: " + getFBUserID() + " google_id: " + getGoogleID() + " custom_id: " + getCustomID() + "\n";
	}

}
