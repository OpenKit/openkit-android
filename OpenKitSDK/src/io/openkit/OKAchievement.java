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
import io.openkit.asynchttp.RequestParams;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
public class OKAchievement implements Parcelable{
	
	private String name;
	private int OKAPP_id;
	private int achievementId;
	private boolean inDevelopment;
	private String lockedIconUrl;
	private String unlockedIconUrl;
	private int points;
	private int goal;
	private int progress;
	
	
	public void writeToParcel(Parcel out, int flags)
	{
		//private String name;
		out.writeString(name);
		//private int OKAPP_id;
		out.writeInt(OKAPP_id);
		//private int achievementId;
		out.writeInt(achievementId);
		//private boolean inDevelopment;
		out.writeInt(boolToInt(inDevelopment));
		//private String unlockedIconUrl;
		out.writeString(unlockedIconUrl);
		//private String lockedIconUrl;
		out.writeString(lockedIconUrl);
		//private int points;
		out.writeInt(points);
		//private int goal;
		out.writeInt(goal);
		//private int progress;
		out.writeInt(progress);
	}
	
	private OKAchievement(Parcel in)
	{
		//private String name;
		name = in.readString();
		//private int OKAPP_id;
		OKAPP_id = in.readInt();
		//private int OKLeaderboard_id;
		achievementId = in.readInt();
		//private boolean inDevelopment;
		inDevelopment = intToBool(in.readInt());
		//private String unlockedIconUrl;
		unlockedIconUrl = in.readString();
		//private String lockedIconUrl;
		lockedIconUrl = in.readString();
		//private int points;
		points = in.readInt();
		//private int goal;
		goal = in.readInt();
		//private int progress;
		progress = in.readInt();
	}
	
	private static int boolToInt(boolean a)
	{
		return (a) ? 1 : 0;
	}
	
	private static boolean intToBool(int a)
	{
		return a == 1;
	}
	
	
	/**
	 * Creates OKLeaderboard from JSON
	 */
	public OKAchievement(JSONObject achievementJSON)
	{
		super();
		initFromJSON(achievementJSON);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String aName)
	{
		this.name = aName;
	}
	
	public int getOKAPP_id()
	{
		return OKAPP_id;
	}
	
	public void setOKAPP_id(int aID)
	{
		this.OKAPP_id = aID;
	}
	
	public int getAchievementId()
	{
		return achievementId;
	}
	
	public void setAchievementId(int aID)
	{
		this.achievementId = aID;
	}
	
	public boolean getInDevelopment()
	{
		return inDevelopment;
	}
	
	public void setInDevelopment(boolean aInDevelopment)
	{
		this.inDevelopment = aInDevelopment;
	}
	
	public String getLockedIconURL()
	{
		return lockedIconUrl;
	}
	
	public void setLockedIconUrl(String aURL)
	{
		this.lockedIconUrl = aURL;
	}

	public String getUnlockedIconURL()
	{
		return unlockedIconUrl;
	}
	
	public void setUnlockedIconUrl(String aURL)
	{
		this.unlockedIconUrl = aURL;
	}

	public int getPoints()
	{
		return points;
	}
	
	public void setPoints(int aCount)
	{
		this.points = aCount;
	}
	
	public int getGoal()
	{
		return goal;
	}
	
	public void setGoal(int aCount)
	{
		this.goal = aCount;
	}

	public int getProgress()
	{
		return progress;
	}
	
	public void setProgress(int aCount)
	{
		this.progress = aCount;
	}
	
	public String getPointsString()
	{
		return Integer.toString(points);
	}
	
	public String getGoalString()
	{
		return Integer.toString(goal);
	}

	public String getProgressString()
	{
		return Integer.toString(progress);
	}
	
	
	private void initFromJSON(JSONObject leaderboardJSON)
	{
		try{
			this.name = leaderboardJSON.getString("name");
			this.achievementId = leaderboardJSON.getInt("id");
			this.OKAPP_id = leaderboardJSON.getInt("app_id");
			this.inDevelopment = leaderboardJSON.getBoolean("in_development");
			this.lockedIconUrl = leaderboardJSON.getString("icon_locked_url");
			this.unlockedIconUrl = leaderboardJSON.getString("icon_url");
			this.points = leaderboardJSON.getInt("points");
			this.goal = leaderboardJSON.getInt("goal");
			this.progress = leaderboardJSON.getInt("progress");
		}
		catch(JSONException e){
			Log.e("OpenKit", "Error parsing JSON for Achievement: " + e.toString());
		}
	}
	
	/**
	 * Required for parcelable
	 * @return
	 */
	public int describeContents()
	{
		return 0;
	}
	
	
	public static final Parcelable.Creator<OKAchievement> CREATOR
	= new Parcelable.Creator<OKAchievement>() {
		public OKAchievement createFromParcel(Parcel in){
			return new OKAchievement(in);
		}

		@Override
		public OKAchievement[] newArray(int size) {
			return new OKAchievement[size];
		}
	};
	
	
	/**
	 * Gets a list of achievements for the app
	 * @param responseHandler Response handler interface with callbacks to be overridden, typically anonymously
	 */
	public static void getAchievements(OKAchievementsListResponseHandler responseHandler)
	{
		RequestParams params = new RequestParams();
		params.put("app_key", OpenKit.getOKAppID());
		
		// If a user_id is supplied, progress for each achievement will come back for this user.
		OKUser currentUser = OpenKit.getCurrentUser();
		if(currentUser != null) {
			params.put("user_id", Integer.toString(currentUser.getOKUserID()));
		}
		
		OKLog.d("Getting list of achievements");
		
		final OKAchievementsListResponseHandler finalResponseHandler = responseHandler;
		OKHTTPClient.get("achievements", params, new OKJsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(JSONObject object) {
				// This method should never be called, because the server always returns an array. 
				// If there are no leaderboards defined, the server returns an empty array.
				OKLog.d("Parsed JSON  from server with object");
				finalResponseHandler.onFailure(new IllegalArgumentException("Server returned a single JSON object when expecting an Array"), object);
			}
			
			@Override
			public void onSuccess(JSONArray array) {
				OKLog.d("Received achievements JSON array from server: " + array.toString());
				
				int numAchievements = array.length();		
				List<OKAchievement> achievements = new ArrayList<OKAchievement>(numAchievements);
				
				for(int x = 0; x < numAchievements; x++)
				{
					try {
						JSONObject ach_json = array.getJSONObject(x);
						achievements.add(new OKAchievement(ach_json));
					} catch (JSONException e) {
						OKLog.d("Error parsing list of achievements JSON: " + e.toString());
					}
				}
				
				finalResponseHandler.onSuccess(achievements);
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				OKLog.d("Failure to connect");
				finalResponseHandler.onFailure(error, null);
			}
			
			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				OKLog.d("Failure from server with object");
				finalResponseHandler.onFailure(e, null);
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				OKLog.d("Failure from server with object");
				finalResponseHandler.onFailure(e, errorResponse);
			}
		});
	}
}
