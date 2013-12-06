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
import io.openkit.user.OKUserUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class OKScore {

	private int OKScoreID;
	private long scoreValue;
	private int OKLeaderboardID;
	private OKUser user;
	private int rank;
	private int metadata;
	private String displayString;
	private boolean submitted;

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

	public OKScore(OKScore toBeCopied)
	{
		super();
		this.OKScoreID = toBeCopied.OKScoreID;
		this.scoreValue = toBeCopied.scoreValue;
		this.OKLeaderboardID = toBeCopied.OKLeaderboardID;
		this.user = toBeCopied.user;
		this.rank = toBeCopied.rank;
		this.metadata = toBeCopied.metadata;
		this.displayString = toBeCopied.displayString;
		this.submitted = toBeCopied.submitted;
	}

	private void initFromJSON(JSONObject scoreJSON)
	{
		this.OKLeaderboardID = OKJSONParser.safeParseInt("leaderboard_id", scoreJSON);
		this.OKScoreID = OKJSONParser.safeParseInt("id", scoreJSON);
		this.rank = OKJSONParser.safeParseInt("rank", scoreJSON);
		this.metadata = OKJSONParser.safeParseInt("metadata", scoreJSON);
		this.displayString = OKJSONParser.safeParseString("display_string", scoreJSON);
		this.scoreValue = OKJSONParser.safeParseLong("value",scoreJSON);

		try {
			this.user = new OKUser(scoreJSON.getJSONObject("user"));
		} catch (JSONException e){
			e.printStackTrace();
			OKLog.d("OpenKit", "Error parsing score JSON: " + e.toString());
		}
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int aRank) {
		this.rank = aRank;
	}

	public int getOKScoreID() {
		return OKScoreID;
	}

	public void setOKScoreID(int aID) {
		this.OKScoreID = aID;
	}

	public long getScoreValue() {
		return scoreValue;
	}

	public void setScoreValue(long aValue) {
		this.scoreValue = aValue;
	}

	public int getOKLeaderboardID() {
		return OKLeaderboardID;
	}

	public void setOKLeaderboardID(int aID) {
		this.OKLeaderboardID = aID;
	}

	public OKUser getOKUser() {
		return user;
	}

	public void setOKUser(OKUser aUser) {
		this.user = aUser;
	}

	public void setMetadata(int aMetadata) {
		this.metadata = aMetadata;
	}

	public int getMetadata() {
		return metadata;
	}

	public void setDisplayString(String aDisplayValue) {
		this.displayString = aDisplayValue;
	}

	public String getDisplayString() {
		return this.displayString;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}


	public interface ScoreRequestResponseHandler
	{
		void onSuccess();
		void onFailure(Throwable error);
	}

	public void submitScore(final ScoreRequestResponseHandler responseHandler)
	{
		OKUser currentUser = OKUser.getCurrentUser();
		setOKUser(currentUser);

		if(OKManager.INSTANCE.getSharedCache() == null) {
			Log.e("OpenKit", "Error: score cache came back as null");
		}

		boolean shouldSubmit = OKManager.INSTANCE.getSharedCache().storeScoreInCacheIfBetterThanLocalCachedScores(this);

		if(currentUser != null && shouldSubmit) {
			submitScoreBase(new ScoreRequestResponseHandler() {

				@Override
				public void onSuccess() {
					OKManager.INSTANCE.getSharedCache().updateCachedScoreSubmitted(OKScore.this);
					responseHandler.onSuccess();
				}

				@Override
				public void onFailure(Throwable error) {
					responseHandler.onFailure(error);
				}
			});
		} else {
			OKLog.v("Score was not submitted");
			if(currentUser == null) {
				responseHandler.onFailure(new Throwable("Current user is not logged in. To submit a score, the user must be logged into OpenKit. The score was cached and will be submitted to OpenKit when the user logs in."));
			} else {
				responseHandler.onFailure(new Throwable("The score was not submitted to the OpenKit server because it is not better than previous submitted score."));
			}
		}

	}

	public void cachedScoreSubmit(final ScoreRequestResponseHandler responseHandler)
	{
		submitScoreBase(responseHandler);
	}

	private void submitScoreBase(final ScoreRequestResponseHandler responseHandler)
	{
		try {
			JSONObject scoreJSON = getScoreAsJSON();

			JSONObject requestParams = new JSONObject();
			requestParams.put("app_key", OpenKit.getAppKey());
			requestParams.put("score", scoreJSON);

			OKHTTPClient.postJSON("/scores", requestParams, new OKJsonHttpResponseHandler() {

				@Override
				public void onSuccess(JSONObject object) {
					OKScore.this.setSubmitted(true);
					responseHandler.onSuccess();
				}

				@Override
				public void onSuccess(JSONArray array) {
					//This should not be called, submitting a score should
					// not return an array, so this is an errror case
					OKScore.this.setSubmitted(false);
					responseHandler.onFailure(new Throwable("Unknown error from OpenKit servers. Received an array when expecting an object"));
				}

				@Override
				public void onFailure(Throwable error, String content) {
					OKScore.this.setSubmitted(false);
					OKUserUtilities.checkIfErrorIsUnsubscribedUserError(error);
					responseHandler.onFailure(error);
				}

				@Override
				public void onFailure(Throwable e, JSONArray errorResponse) {
					OKScore.this.setSubmitted(false);
					OKUserUtilities.checkIfErrorIsUnsubscribedUserError(e);
					responseHandler.onFailure(e);
				}

				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					OKScore.this.setSubmitted(false);
					OKUserUtilities.checkIfErrorIsUnsubscribedUserError(e);
					responseHandler.onFailure(e);
				}
			});

		} catch (JSONException e) {
			responseHandler.onFailure(new Throwable("OpenKit JSON parsing error"));
			OKScore.this.setSubmitted(false);
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

	@Override
	public String toString() {
		return "OKScore id: " + OKScoreID + " value: " + scoreValue +  " leaderboard ID: " + OKLeaderboardID + " metadata: " + metadata + " display string: " + displayString + " submitted: " + submitted + "\n";
	}

}
