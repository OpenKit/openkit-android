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

import io.openkit.asynchttp.OKJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class OKScore {
	
	private int OKScoreID;
	private long scoreValue;
	private int OKLeaderboardID;
	private OKUser user;
	private int rank;
	private int metadata;
	private String displayString;
	
	public OKScore()
	{
		super();
	}
	
	
	/**
	 * Creates OKScore object from JSON
	 * @param scoreJSON
	 */
	public OKScore(JSONObject scoreJSON)
	{
		super();
		initFromJSON(scoreJSON);
	}
	
	private void initFromJSON(JSONObject scoreJSON)
	{	
		try {
			this.OKLeaderboardID = scoreJSON.getInt("leaderboard_id");
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
		try {
			this.OKScoreID = scoreJSON.getInt("id");
		} catch (JSONException e){
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
		try {
			this.scoreValue = scoreJSON.getLong("value");
		} catch (JSONException e){
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
		try {
			this.rank = scoreJSON.getInt("rank");
		} catch (JSONException e){
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
		try {
			this.user = new OKUser(scoreJSON.getJSONObject("user"));
		} catch (JSONException e){
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
		try {
			this.metadata = scoreJSON.getInt("metadata");
		} catch (JSONException e){
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
		// Get the display string. Android JSON parsing returns the string "null" for null strings instead of
		// return null, so we check this using json.isNull and TextUtils.isEmpty for empty strings
		// and return null if it's empty or null
		try {
			if(scoreJSON.isNull("display_string") || TextUtils.isEmpty(scoreJSON.getString("display_string"))) {
				this.displayString = null;
			} else {
				this.displayString = scoreJSON.getString("display_string");
			}
		} catch (JSONException e){
			this.displayString = null;
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public void setRank(int aRank)
	{
		this.rank = aRank;
	}
	
	public int getOKScoreID()
	{
		return OKScoreID;
	}
	
	public void setOKScoreID(int aID)
	{
		this.OKScoreID = aID;
	}
	
	public long getScoreValue()
	{
		return scoreValue;
	}
	
	public void setScoreValue(long aValue)
	{
		this.scoreValue = aValue;
	}
	
	public int getOKLeaderboardID()
	{
		return OKLeaderboardID;
	}
	
	public void setOKLeaderboardID(int aID)
	{
		this.OKLeaderboardID = aID;
	}
	
	public OKUser getOKUser()
	{
		return user;
	}
	
	public void setOKUser(OKUser aUser)
	{
		this.user = aUser;
	}
	
	public void setMetadata(int aMetadata)
	{
		this.metadata = aMetadata;
	}
	
	public int getMetadata()
	{
		return metadata;
	}
	
	public void setDisplayString(String aDisplayValue)
	{
		this.displayString = aDisplayValue;
	}
	
	public String getDisplayString()
	{
		return this.displayString;
	}
	
	public interface ScoreRequestResponseHandler
	{
		void onSuccess();
		void onFailure(Throwable error);
	}
	
	public void submitScore(final ScoreRequestResponseHandler responseHandler)
	{
		OKUser currentUser = OKUser.getCurrentUser();

		if(currentUser == null) {
			responseHandler.onFailure(new Throwable("Current user is not logged in. To submit a score, the user must be logged into OpenKit"));
			return;
		}

		try {
			JSONObject scoreJSON = getScoreAsJSON();
			
			JSONObject requestParams = new JSONObject();
			requestParams.put("app_key", OpenKit.getOKAppID());
			requestParams.put("score", scoreJSON);

			OKHTTPClient.postJSON("/scores", requestParams, new OKJsonHttpResponseHandler() {

				@Override
				public void onSuccess(JSONObject object) {
					responseHandler.onSuccess();
				}

				@Override
				public void onSuccess(JSONArray array) {
					//This should not be called, submitting a score should
					// not return an array, so this is an errror case
					responseHandler.onFailure(new Throwable("Unknown error from OpenKit servers. Received an array when expecting an object"));
				}

				@Override
				public void onFailure(Throwable error, String content) {
					responseHandler.onFailure(error);
				}

				@Override
				public void onFailure(Throwable e, JSONArray errorResponse) {
					responseHandler.onFailure(new Throwable(errorResponse.toString()));
				}

				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					responseHandler.onFailure(new Throwable(errorResponse.toString()));
				}
			});

		} catch (JSONException e) {
			responseHandler.onFailure(new Throwable("OpenKit JSON parsing error"));
		}

	}
	
	private JSONObject getScoreAsJSON() throws JSONException
	{
		JSONObject scoreJSON = new JSONObject();
		
		scoreJSON.put("value", this.scoreValue);
		scoreJSON.put("leaderboard_id", this.OKLeaderboardID);
		scoreJSON.put("user_id", OKUser.getCurrentUser().getOKUserID());
		scoreJSON.put("metadata", this.metadata);
		scoreJSON.put("display_string", this.displayString);
		
		return scoreJSON;
	}
	
}
