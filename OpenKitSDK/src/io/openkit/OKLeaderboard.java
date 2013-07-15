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

import io.openkit.asynchttp.*;
import io.openkit.leaderboards.*;
import java.util.ArrayList;
import java.util.List;
import org.json.*;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
public class OKLeaderboard implements Parcelable{

	private String name;
	private int OKAPP_id;
	private int OKLeaderboard_id;
	private boolean inDevelopment;
	private LeaderboardSortType sortType;
	private String icon_url;
	private int playerCount;
	//This isn't parcelable since it's only used in display
	private OKLeaderboardTimeRange displayTimeRange = OKLeaderboardTimeRange.AllTime;

	public static final String LEADERBOARD_KEY = "OKLeaderboard";

	public static final int NUM_SCORES_PER_PAGE = 25;

	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		//private String name;
		out.writeString(name);
		//private int OKAPP_id;
		out.writeInt(OKAPP_id);
		//private int OKLeaderboard_id;
		out.writeInt(OKLeaderboard_id);
		//private boolean inDevelopment;
		out.writeInt(boolToInt(inDevelopment));
		//private LeaderboardSortType sortType;
		out.writeString((sortType == null) ? "" : sortType.name());
		//private String icon_url;
		out.writeString(icon_url);
		//private int playerCount;
		out.writeInt(playerCount);
	}

	private OKLeaderboard(Parcel in)
	{
		//private String name;
		name = in.readString();
		//private int OKAPP_id;
		OKAPP_id = in.readInt();
		//private int OKLeaderboard_id;
		OKLeaderboard_id = in.readInt();
		//private boolean inDevelopment;
		inDevelopment = intToBool(in.readInt());
		//private LeaderboardSortType sortType;
		try{
			sortType = LeaderboardSortType.valueOf(in.readString());
		}
		catch (Exception e) {
			sortType = null;
		}
		//private String icon_url;
		icon_url = in.readString();
		//private int playerCount;
		playerCount = in.readInt();
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
	public OKLeaderboard(JSONObject leaderboardJSON)
	{
		super();
		initFromJSON(leaderboardJSON);
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

	public int getOKLeaderboard_id()
	{
		return OKLeaderboard_id;
	}

	public void setOKLeaderboard_id(int aID)
	{
		this.OKLeaderboard_id = aID;
	}

	public boolean getInDevelopment()
	{
		return inDevelopment;
	}

	public void setInDevelopment(boolean aInDevelopment)
	{
		this.inDevelopment = aInDevelopment;
	}

	public LeaderboardSortType getSortType()
	{
		return sortType;
	}

	public void setSortType(LeaderboardSortType aType)
	{
		this.sortType = aType;
	}

	public String getIconURL()
	{
		return icon_url;
	}

	public void setIconURL(String aURL)
	{
		this.icon_url = aURL;
	}

	public int getPlayerCount()
	{
		return playerCount;
	}

	public String getPlayerCountString()
	{
		return Integer.toString(playerCount);
	}

	public void setPlayerCount(int aCount)
	{
		this.playerCount = aCount;
	}

	public OKLeaderboardTimeRange getDisplayedTimeRange()
	{
		return displayTimeRange;
	}

	public void setDisplayedTimeRange(OKLeaderboardTimeRange range)
	{
		this.displayTimeRange = range;
	}

	private void initFromJSON(JSONObject leaderboardJSON)
	{
		try{
			this.name = leaderboardJSON.getString("name");
			this.OKLeaderboard_id = leaderboardJSON.getInt("id");
			this.OKAPP_id = leaderboardJSON.getInt("app_id");
			this.inDevelopment = leaderboardJSON.getBoolean("in_development");
			this.icon_url = leaderboardJSON.getString("icon_url");
			this.playerCount = leaderboardJSON.getInt("player_count");

			String sortTypeString = leaderboardJSON.getString("sort_type");

			if(sortTypeString.equalsIgnoreCase("HighValue")){
				this.sortType = LeaderboardSortType.HighValue;
			}
			else {
				this.sortType = LeaderboardSortType.LowValue;
			}
		}
		catch(JSONException e){
			Log.e("OpenKit", "Error parsing JSON for leaderboard: " + e.toString());
		}
	}

	/**
	 * Required for parcelable
	 * @return
	 */
	@Override
	public int describeContents()
	{
		return 0;
	}


	public static final Parcelable.Creator<OKLeaderboard> CREATOR
	= new Parcelable.Creator<OKLeaderboard>() {
		@Override
		public OKLeaderboard createFromParcel(Parcel in){
			return new OKLeaderboard(in);
		}

		@Override
		public OKLeaderboard[] newArray(int size) {
			return new OKLeaderboard[size];
		}
	};


	/**
	 * Gets a list of leaderboards for the app
	 * @param responseHandler Response handler interface with callbacks to be overridden, typically anonymously
	 */
	public static void getLeaderboards(OKLeaderboardsListResponseHandler responseHandler)
	{
		RequestParams params = new RequestParams();
		params.put("app_key",OpenKit.getAppKey());

		OKLog.d("Getting list of leaderboards");

		final OKLeaderboardsListResponseHandler finalResponseHandler = responseHandler;

		OKHTTPClient.get("leaderboards", params, new OKJsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject object) {
				// This method should never be called, because the server always returns an array.
				// If there are no leaderboards defined, the server returns an empty array.
				OKLog.d("Parsed JSON  from server with object");
				finalResponseHandler.onFailure(new IllegalArgumentException("Server returned a single JSON object when expecting an Array"), object);
			}

			@Override
			public void onSuccess(JSONArray array) {
				OKLog.d("Received leaderboards JSON array from server: " + array.toString());

				int maxPlayerCount = 0;

				int numLeaderboards = array.length();
				List<OKLeaderboard> leaderboards = new ArrayList<OKLeaderboard>(numLeaderboards);

				for(int x = 0; x < numLeaderboards; x++)
				{
					try {
						JSONObject leaderBoard = array.getJSONObject(x);
						leaderboards.add(new OKLeaderboard(leaderBoard));

						if(leaderboards.get(x).getPlayerCount() > maxPlayerCount) {
							maxPlayerCount = leaderboards.get(x).getPlayerCount();
						}

					} catch (JSONException e) {
						OKLog.d("Error parsing list of leaderboards JSON: " + e.toString());
					}
				}

				finalResponseHandler.onSuccess(leaderboards, maxPlayerCount);
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


	/**
	 * Gets the current user's top score for this leaderboard. If the user is not logged in, calls onFailure and returns.
	 * @param responseHandler Response handler for the request.
	 */
	public void getUsersTopScoreForLeaderboard(final OKScoresResponseHandler responseHandler)
	{
		OKUser currentUser = OpenKit.getCurrentUser();

		if(currentUser == null)
		{
			responseHandler.onFailure(new Throwable("User is not logged in, no top score"), null);
			return;
		}

		RequestParams params = new RequestParams();
		params.put("app_key",OpenKit.getAppKey());
		params.put("leaderboard_id", Integer.toString(this.OKLeaderboard_id));
		params.put("user_id", Integer.toString(currentUser.getOKUserID()));
		params.put("leaderboard_range", getParamForLeaderboardDisplayRange());

		OKHTTPClient.get("best_scores/user", params, new OKJsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject object) {
				OKScore topScore = new OKScore(object);
				List<OKScore> list = new ArrayList<OKScore>();
				list.add(topScore);
				responseHandler.onSuccess(list);
			}

			//We expect a single OKScore object from the API, not an array
			@Override
			public void onSuccess(JSONArray array) {
				responseHandler.onFailure(new Throwable("Received an array when getting users top score. Expected a single object!"), null);
			}

			@Override
			public void onFailure(Throwable error, String content) {
				responseHandler.onFailure(error, null);
			}

			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				responseHandler.onFailure(e, null);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				responseHandler.onFailure(e, errorResponse);
			}
		});
	}

	/**
	 * Returns the correct parameter to use with the server REST API for the leaderboard time range
	 * @return
	 */
	private String getParamForLeaderboardDisplayRange()
	{
		switch (displayTimeRange) {
		case OneDay:
			return "today";
		case OneWeek:
			return "this_week";
		default:
			return "all_time";
		}
	}

	/**
	 * Get scores for the given leaderboard (only the first page of scores)
	 * @param responseHandler ResponseHandler called on success/fail
	 */
	public void getLeaderboardScores(OKScoresResponseHandler responseHandler)
	{
		getLeaderboardScores(1, responseHandler);
	}


	/**
	 * Get scores for the given leaderboard and page number
	 * @param pageNumber Page number in leaderboard scores pagination
	 * @param responseHandler ResponseHandler called on success/fail
	 */
	public void getLeaderboardScores(int pageNumber, OKScoresResponseHandler responseHandler)
	{
		RequestParams params = new RequestParams();
		params.put("app_key",OpenKit.getAppKey());
		params.put("leaderboard_id", Integer.toString(this.OKLeaderboard_id));
		params.put("leaderboard_range", getParamForLeaderboardDisplayRange());
		params.put("page_num", Integer.toString(pageNumber));
		params.put("num_per_page",Integer.toString(NUM_SCORES_PER_PAGE));

		OKLog.d("Getting leaderboard scores for range: " + getParamForLeaderboardDisplayRange());

		final OKScoresResponseHandler finalResponseHandler = responseHandler;

		OKHTTPClient.get("best_scores", params, new OKJsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject object) {
				//This should never be called because the server
				//always responds with an array of scores, even if it's empty
				// or only has 1 score
				finalResponseHandler.onFailure(new IllegalArgumentException("Server returned only one JSON object instead of an array"), null);
			}

			@Override
			public void onSuccess(JSONArray array)
			{
				int numScores = array.length();
				List<OKScore> scoresList = new ArrayList<OKScore>(numScores);

				for(int x = 0; x < numScores; x++)
				{
					JSONObject score;
					try {
						score = array.getJSONObject(x);
						scoresList.add(new OKScore(score));
					} catch (JSONException e) {
						OKLog.d("Error parsing list of scores for a leaderboard: " + e.toString());
					}
				}

				finalResponseHandler.onSuccess(scoresList);
			}

			@Override
			public void onFailure(Throwable error, String content) {
				finalResponseHandler.onFailure(error, null);
			}

			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				finalResponseHandler.onFailure(e, null);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				finalResponseHandler.onFailure(e, errorResponse);
			}
		});
	}

	public void getFacebookFriendsScoresWithFacebookFriends()
	{
		//TODO
	}

	public void getScoresFromGPG()
	{
		//TODO
	}

	public void getSocialScoresFromGPG()
	{
		//TODO
	}

	public void getUsersTopScoreFromGPG()
	{
		//TODO
	}

	public void showGlobalScoresFromGPG()
	{
		//TODO

	}



}
