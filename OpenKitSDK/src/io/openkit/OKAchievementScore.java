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


public class OKAchievementScore {

	private int progress;
	private int OKAchievementId;

	public OKAchievementScore()
	{
		super();
	}

	public int getProgres()
	{
		return progress;
	}

	public void setProgress(int n)
	{
		this.progress = n;
	}

	public int getOKAchievementId()
	{
		return OKAchievementId;
	}

	public void setOKAchievementId(int aID)
	{
		this.OKAchievementId = aID;
	}


	public interface AchievementScoreRequestResponseHandler
	{
		void onSuccess();
		void onFailure(Throwable error);
	}

	public void submitAchievementScore(final AchievementScoreRequestResponseHandler responseHandler)
	{
		OKUser currentUser = OKUser.getCurrentUser();
		if(currentUser == null) {
			responseHandler.onFailure(new Throwable("Current user is not logged in. To submit an achievement score, the user must be logged into OpenKit"));
			return;
		}

		try {
			JSONObject achievementScoreJSON = getAchievementScoreAsJSON();

			JSONObject requestParams = new JSONObject();
			requestParams.put("app_key", OpenKit.getAppKey());
			requestParams.put("achievement_score", achievementScoreJSON);

			OKHTTPClient.postJSON("/achievement_scores", requestParams, new OKJsonHttpResponseHandler() {

				@Override
				public void onSuccess(JSONObject object) {
					responseHandler.onSuccess();
				}

				@Override
				public void onSuccess(JSONArray array) {
					//This should not be called, submitting an achievementScore should
					// not return an array, so this is an errror case
					responseHandler.onFailure(new Throwable("Unknown error from OpenKit servers. Received an array when expecting an object"));
				}

				@Override
				public void onFailure(Throwable error, String content) {
					OKUserUtilities.checkIfErrorIsUnsubscribedUserError(error);
					responseHandler.onFailure(error);
				}

				@Override
				public void onFailure(Throwable e, JSONArray errorResponse) {
					OKUserUtilities.checkIfErrorIsUnsubscribedUserError(e);
					responseHandler.onFailure(new Throwable(errorResponse.toString()));
				}

				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					OKUserUtilities.checkIfErrorIsUnsubscribedUserError(e);
					responseHandler.onFailure(new Throwable(errorResponse.toString()));
				}
			});

		} catch (JSONException e) {
			responseHandler.onFailure(new Throwable("OpenKit JSON parsing error"));
		}

	}

	private JSONObject getAchievementScoreAsJSON() throws JSONException
	{
		JSONObject achievementScoreJSON = new JSONObject();
		achievementScoreJSON.put("progress", this.progress);
		achievementScoreJSON.put("achievement_id", this.OKAchievementId);
		achievementScoreJSON.put("user_id", OKUser.getCurrentUser().getOKUserID());
		return achievementScoreJSON;
	}

}
