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
	private long FBUserID;
	private long twitterUserID;
	private String userNick;
	
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
		OpenKitSingleton.INSTANCE.logoutCurrentUser(context);
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
		try {
			this.OKUserID = userJSON.getInt("id");
			this.userNick = userJSON.getString("nick");
			
			//We use optLong for fbID and Twitter ID becuase an OKUser will typically not have both, only 1 or the other
			this.FBUserID = userJSON.optLong("fb_id");
			this.twitterUserID = userJSON.optLong("twitter_id");
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
	
	public long getFBUserID()
	{
		return FBUserID;
	}
	
	public void setFBUserID(long aFBID)
	{
		this.FBUserID = aFBID;
	}
	
	public long getTwitterUserID()
	{
		return twitterUserID;
	}
	
	public void setTwitterUserID(long aTwitterID)
	{
		this.twitterUserID = aTwitterID;
	}
	
	public String getUserNick()
	{
		return userNick;
	}
	
	public void setUserNick(String aNick)
	{
		this.userNick = aNick;
	}

}
