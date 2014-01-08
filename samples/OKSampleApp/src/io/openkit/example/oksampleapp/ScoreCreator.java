package io.openkit.example.oksampleapp;

import io.openkit.OKHTTPClient;
import io.openkit.OKLog;
import io.openkit.OKScore;
import io.openkit.OKScore.ScoreRequestResponseHandler;
import io.openkit.OKUser;
import io.openkit.OpenKit;
import io.openkit.asynchttp.OKJsonHttpResponseHandler;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import io.openkit.user.CreateOrUpdateOKUserRequestHandler;
import io.openkit.user.OKUserIDType;
import io.openkit.user.OKUserUtilities;

import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * This class creates a bunch of users and scores for a leaderboard. It uses Facebook to create fake users based off of your facebook friends
 * and uses the FB ids to create random FB,Twitter,Google, and CustomAuth users.
 */
public class ScoreCreator {

	public static void CreateGlobalScores(int aNumScores, int aLeaderboardID, final int maxScoreValue)
	{
		final int numScores = aNumScores;
		final int leaderboardID = aLeaderboardID;

		OKLog.d("Getting list of FB friends");

		Session session = Session.getActiveSession();

		if(session != null && session.isOpened())
		{
			Request friendsRequest = Request.newMyFriendsRequest(session, new GraphUserListCallback() {

				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					FacebookRequestError error = response.getError();

					if(error != null) {
						OKLog.d("Error getting Facebook friends");
						//requestHandler.onFail(error);
					} else {
						OKLog.d("Got %d facebook friends", users.size());


						Random generator = new Random();

						for(int x = 0; x < numScores; x++) {

							int friendIndex = generator.nextInt(users.size());
							CreateScoreForFBUser(users.get(friendIndex), leaderboardID, x, maxScoreValue);
						}


					}
				}
			});

			friendsRequest.executeAsync();
		} else {
			OKLog.v("FB session not open");
		}
	}

	public static void CreateFriendsScores(int aNumScores, int aLeaderboardID, int maxScoreValue)
	{
		final int numScores = aNumScores;
		final int leaderboardID = aLeaderboardID;

		OKLog.d("Getting list of FB friends");

		Session session = Session.getActiveSession();

		if(session != null && session.isOpened())
		{
			Request friendsRequest = Request.newMyFriendsRequest(session, new GraphUserListCallback() {

				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					FacebookRequestError error = response.getError();

					if(error != null) {
						OKLog.d("Error getting Facebook friends");
					} else {
						OKLog.d("Got %d facebook friends", users.size());


						Random generator = new Random();

						for(int x = 0; x < numScores; x++) {

							int friendIndex = generator.nextInt(users.size());
							CreateFriendScore(users.get(friendIndex), leaderboardID, x);
						}

					}
				}
			});

			friendsRequest.executeAsync();
		} else {
			OKLog.v("FB session not open");
		}
	}

	public static void CreateScoreForFBUser(GraphUser user, final int leaderboardID, int seed, final int maxScoreSize)
	{

		long fbIDLong = Long.parseLong(user.getId());
		long newFBID = fbIDLong + 5;


		String userName = user.getName();

		final Random generator = new Random(seed);



		OKUserUtilities.createOKUser(OKUserIDType.FacebookID, String.valueOf(newFBID), userName, new CreateOrUpdateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				OKScore score = new OKScore();
				score.setOKLeaderboardID(leaderboardID);
				score.setOKUser(user);
				score.setScoreValue(generator.nextInt(maxScoreSize));

				submitScoreForUser(user, score, new ScoreRequestResponseHandler() {

					@Override
					public void onSuccess() {
						OKLog.v("Submitted score");

					}

					@Override
					public void onFailure(Throwable error) {
						OKLog.v("Score submission failed");
					}
				});
			}

			@Override
			public void onFail(Throwable error) {
				// TODO Auto-generated method stub
				OKLog.v("failed to create user");
			}
		});
	}



	public static void CreateFriendScore(GraphUser user, final int leaderboardID, int seed)
	{
		String fbID = user.getId();
		String userName = user.getName();

		final Random generator = new Random(seed);


		OKUserUtilities.createOKUser(OKUserIDType.FacebookID, fbID, userName, new CreateOrUpdateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				OKScore score = new OKScore();
				score.setOKLeaderboardID(leaderboardID);
				score.setOKUser(user);
				score.setScoreValue(generator.nextInt(10000));

				submitScoreForUser(user, score, new ScoreRequestResponseHandler() {

					@Override
					public void onSuccess() {
						OKLog.v("Submitted score");

					}

					@Override
					public void onFailure(Throwable error) {
						OKLog.v("Score submission failed");
					}
				});
			}

			@Override
			public void onFail(Throwable error) {
				// TODO Auto-generated method stub
				OKLog.v("failed to create user");
			}
		});
	}



	public static void submitScoreForUser(OKUser user, OKScore score, final ScoreRequestResponseHandler responseHandler)
	{

		try {
			JSONObject scoreJSON = getScoreAsJSON(score, user);

			JSONObject requestParams = new JSONObject();
			requestParams.put("app_key", OpenKit.getAppKey());
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

	private static JSONObject getScoreAsJSON(OKScore score, OKUser user) throws JSONException
	{
		JSONObject scoreJSON = new JSONObject();

		scoreJSON.put("value", score.getScoreValue());
		scoreJSON.put("leaderboard_id", score.getOKLeaderboardID());
		scoreJSON.put("user_id", user.getOKUserID());
		scoreJSON.put("metadata", score.getMetadata());
		scoreJSON.put("display_string", score.getDisplayString());

		return scoreJSON;
	}

}
