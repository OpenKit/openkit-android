package io.openkit.unity.android;

import java.util.ArrayList;
import java.util.Locale;

import io.openkit.OKAchievementScore;
import io.openkit.OKLeaderboard;
import io.openkit.OKLoginActivity;
import io.openkit.OKManager;
import io.openkit.OKScore;
import io.openkit.OKUser;
import io.openkit.OKScore.ScoreRequestResponseHandler;
import io.openkit.OpenKit;
import io.openkit.facebook.FacebookRequestError;
import io.openkit.facebookutils.FacebookUtilities;
import io.openkit.leaderboards.OKLeaderboardsActivity;
import android.content.Intent;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

public class UnityPlugin {

	public static final String ASYNC_CALL_SUCCEEDED = "asyncCallSucceeded";
	public static final String ASYNC_CALL_FAILED = "asyncCallFailed";

	public static void logD(String format, Object... args) {
		Log.d("OpenKitPlugin", String.format(Locale.getDefault(), format, args));
	}

	/* Set functions for various settings */

	public static void setAppKey(String appKey)
	{
		OKManager.INSTANCE.setAppKey(appKey);
	}

	public static void setSecretKey(String secretKey)
	{
		OKManager.INSTANCE.setSecretKey(secretKey);
	}

	public static void setEndpoint(String endpoint)
	{
		OpenKit.setEndpoint(endpoint);
	}

	public static void setAchievementsEnabled(boolean enabled) {
		OKManager.INSTANCE.setAchievementsEnabled(enabled);
	}

	public static void setLeaderboardListTag(String tag){
		OKManager.INSTANCE.setLeaderboardListTag(tag);
	}

	public static void setGoogleLoginEnabled(boolean enabled) {
		OKManager.INSTANCE.setGoogleLoginEnabled(enabled);
	}

	/* Initialize method required for Android */

	public static void initialize()
	{
		logD("Initializing OpenKit native Android");
		OpenKit.initialize(UnityPlayer.currentActivity);
	}

	public static void initialize(String appKey, String secretKey)
	{
		logD("Initializing OpenKit");
		OpenKit.initialize(UnityPlayer.currentActivity, appKey, secretKey);
	}


	public static void logoutOfOpenKit()
	{
		OKManager.INSTANCE.logoutCurrentUser(UnityPlayer.currentActivity.getApplicationContext());
		logD("Logging out of OpenKit");
	}


	/* Show UI methods */

	public static void showLeaderboards()
	{
		logD("Launching Leaderboards UI");
		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent leaderboards = new Intent(UnityPlayer.currentActivity, OKLeaderboardsActivity.class);
				UnityPlayer.currentActivity.startActivity(leaderboards);
			}
		});
	}

	public static void showLeaderboard(final int leaderboardID)
	{
		logD("Launching Leaderboard with id: " + leaderboardID);
		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent leaderboardIntent = OKLeaderboard.getLeaderboardIntent(UnityPlayer.currentActivity,leaderboardID);
				UnityPlayer.currentActivity.startActivity(leaderboardIntent);
			}
		});
	}

	/**
	 * Shows OpenKit login UI for the given Unity App
	 */
	public static void showLoginUI()
	{
		logD("Launching Login UI");
		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent loginUI = new Intent(UnityPlayer.currentActivity, OKLoginActivity.class);
				UnityPlayer.currentActivity.startActivity(loginUI);
			}
		});
	}


	/* Submit scores */


	/**
	 * Submits a given score value and leaderboard ID. Uses UnitySendMessage to send a success or fail message to the gameobjectname specified
	 * @param scoreValue
	 * @param leaderboardID
	 * @param gameObjectName GameObject that acts as an event handler
	 */
	public static void submitScore(long scoreValue, int leaderboardID, int metadata, String displayString, final String gameObjectName)
	{
		logD("Submitting score");
		OKScore score = new OKScore();
		score.setScoreValue(scoreValue);
		score.setOKLeaderboardID(leaderboardID);
		score.setMetadata(metadata);
		score.setDisplayString(displayString);

		if(OKUser.getCurrentUser() == null)
		{
			UnityPlayer.UnitySendMessage(gameObjectName, "scoreSubmissionFailed", "");
		}

		score.submitScore(new ScoreRequestResponseHandler() {

			@Override
			public void onSuccess() {
				UnityPlayer.UnitySendMessage(gameObjectName, "scoreSubmissionSucceeded", "");
			}

			@Override
			public void onFailure(Throwable arg0) {
				UnityPlayer.UnitySendMessage(gameObjectName, "scoreSubmissionFailed", arg0.getLocalizedMessage());
			}
		});
	}

	public static void submitAchievementScore(int progress, int achievementID, final String gameObjectName)
	{
		logD("Submitting achievement score");

		OKAchievementScore achievementScore = new OKAchievementScore();
		achievementScore.setOKAchievementId(achievementID);
		achievementScore.setProgress(progress);

		achievementScore.submitAchievementScore(new OKAchievementScore.AchievementScoreRequestResponseHandler() {
			@Override
			public void onSuccess() {
				logD("Achievement score submitted successfully");
				UnityPlayer.UnitySendMessage(gameObjectName, "scoreSubmissionSucceeded", "");
			}

			@Override
			public void onFailure(Throwable error) {
				logD("Achievement score submission failed");
				UnityPlayer.UnitySendMessage(gameObjectName, "scoreSubmissionFailed", error.getLocalizedMessage());
			}
		});
	}

	/* Get stuff from native to Unity */

	public static int getCurrentUserOKID()
	{
		if(OpenKit.getCurrentUser() != null)
			return OpenKit.getCurrentUser().getOKUserID();
		else
			return 0;
	}

	public static String getCurrentUserNick()
	{
		if(OpenKit.getCurrentUser() != null)
			return OpenKit.getCurrentUser().getUserNick();
		else
			return null;
	}

	public static long getCurrentUserFBID()
	{
		if(OpenKit.getCurrentUser() != null)
			return OpenKit.getCurrentUser().getFBUserID();
		else
			return 0;
	}

	public static long getCurrentUserTwitterID()
	{
		if(OpenKit.getCurrentUser() != null)
			return OpenKit.getCurrentUser().getTwitterUserID();
		else
			return 0;
	}

	public static void getFacebookFriendsList(final String gameObjectName)
	{
		FacebookUtilities.GetFBFriends(new FacebookUtilities.GetFBFriendsRequestHandler() {
			@Override
			public void onSuccess(ArrayList<Long> friendsArray) {
				String friendsList = FacebookUtilities.getSerializedListOfFBFriends(friendsArray);
				UnityPlayer.UnitySendMessage(gameObjectName, ASYNC_CALL_SUCCEEDED, friendsList);
			}

			@Override
			public void onFail(FacebookRequestError error) {
				if(error != null){
					UnityPlayer.UnitySendMessage(gameObjectName, ASYNC_CALL_FAILED, error.getErrorMessage());
				} else {
					UnityPlayer.UnitySendMessage(gameObjectName, ASYNC_CALL_FAILED, "Unknown error when trying to get friends from Android native");
				}
			}
		});
	}


}
