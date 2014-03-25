package io.openkit.unity.android;

import java.util.ArrayList;

import java.util.Locale;



import io.openkit.OKAchievement;
import io.openkit.OKAchievementScore;
import io.openkit.OKLeaderboard;
import io.openkit.OKLoginActivity;
import io.openkit.OKLoginActivityHandler;
import io.openkit.OKManager;
import io.openkit.OKScore;
import io.openkit.OKUser;
import io.openkit.OKScore.ScoreRequestResponseHandler;
import io.openkit.OpenKit;
import com.facebook.FacebookRequestError;
import com.facebook.Session;

import io.openkit.facebookutils.FacebookUtilities;
import io.openkit.leaderboards.OKLeaderboardsActivity;
import android.content.Intent;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

public class UnityPlugin {

	private static final String ASYNC_CALL_SUCCEEDED = "asyncCallSucceeded";
	private static final String ASYNC_CALL_FAILED = "asyncCallFailed";

	private static void OKBridgeLog(String format, Object... args) {
		Log.d("OpenKitPlugin", String.format(Locale.getDefault(), format, args));
	}

	/*-------------------------------------------------------------------------
	 * Region:  Initialize method required for Android
	 */
	public static void configure(String appKey, String secretKey, String endpoint)
	{
		OKBridgeLog("Initializing OpenKit from Android native with endpoint: " + endpoint);
		OpenKit.configure(UnityPlayer.currentActivity, appKey, secretKey, endpoint);
	}

	public static void logoutOfOpenKit()
	{
		OKManager.INSTANCE.logoutCurrentUser(UnityPlayer.currentActivity.getApplicationContext());
		OKBridgeLog("Logging out of OpenKit");
	}

	public static void logoutFacebook()
	{
		Session currentSession = Session.getActiveSession();

		if(currentSession != null) {
			currentSession.closeAndClearTokenInformation();
		}
	}



	/*-------------------------------------------------------------------------
	 * Region:  Set functions for various settings
	 */

	public static void setAchievementsEnabled(boolean enabled) {
		OKManager.INSTANCE.setAchievementsEnabled(enabled);
	}

	public static void setLeaderboardListTag(String tag){
		OKManager.INSTANCE.setLeaderboardListTag(tag);
	}

	public static void setGoogleLoginEnabled(boolean enabled) {
		OKManager.INSTANCE.setGoogleLoginEnabled(enabled);
	}

	/*-------------------------------------------------------------------------
	 * Region: showUI methods
	 */

	public static void showLeaderboards()
	{
		OKBridgeLog("Launching Leaderboards UI");
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
		OKBridgeLog("Launching Leaderboard with id: " + leaderboardID);
		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent leaderboardIntent = OKLeaderboard.getLeaderboardIntent(UnityPlayer.currentActivity,leaderboardID);
				UnityPlayer.currentActivity.startActivity(leaderboardIntent);
			}
		});
	}

	public static void showAchievements()
	{
		OKBridgeLog("Launching achievements UI");
		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent achievementsIntent = OKAchievement.getAchievementsIntent(UnityPlayer.currentActivity);
				UnityPlayer.currentActivity.startActivity(achievementsIntent);
			}
		});
	}

	/**
	 * Shows OpenKit login UI for the given Unity App
	 */
	public static void showLoginUI()
	{
		OKBridgeLog("Launching Login UI");
		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent loginUI = new Intent(UnityPlayer.currentActivity, OKLoginActivity.class);
				UnityPlayer.currentActivity.startActivity(loginUI);
			}
		});
	}

	public static void showLoginUIWithCallback(final String gameObjectName)
	{
		OKBridgeLog("Launching Login UI with callback");

		OKLoginActivity.setActivityHandler(new OKLoginActivityHandler() {

			@Override
			public void onLoginDialogComplete() {
				String returnString = "OpenKit login dialog finished";
				UnityPlayer.UnitySendMessage(gameObjectName, ASYNC_CALL_SUCCEEDED, returnString);
			}
		});

		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent loginUI = new Intent(UnityPlayer.currentActivity, OKLoginActivity.class);
				UnityPlayer.currentActivity.startActivity(loginUI);
			}
		});
	}


	/*-------------------------------------------------------------------------
	 * Region: Submit scores methods
	 */

	/**
	 * Submits a given score value and leaderboard ID. Uses UnitySendMessage to send a success or fail message to the gameobjectname specified
	 * @param scoreValue
	 * @param leaderboardID
	 * @param gameObjectName GameObject that acts as an event handler
	 */
	public static void submitScore(long scoreValue, int leaderboardID, int metadata, String displayString, final String gameObjectName)
	{
		OKBridgeLog("Submitting score");
		OKScore score = new OKScore();
		score.setScoreValue(scoreValue);
		score.setOKLeaderboardID(leaderboardID);
		score.setMetadata(metadata);
		score.setDisplayString(displayString);

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
		OKBridgeLog("Submitting achievement score");

		OKAchievementScore achievementScore = new OKAchievementScore();
		achievementScore.setOKAchievementId(achievementID);
		achievementScore.setProgress(progress);

		achievementScore.submitAchievementScore(new OKAchievementScore.AchievementScoreRequestResponseHandler() {
			@Override
			public void onSuccess() {
				OKBridgeLog("Achievement score submitted successfully");
				UnityPlayer.UnitySendMessage(gameObjectName, "scoreSubmissionSucceeded", "");
			}

			@Override
			public void onFailure(Throwable error) {
				OKBridgeLog("Achievement score submission failed");
				UnityPlayer.UnitySendMessage(gameObjectName, "scoreSubmissionFailed", error.getLocalizedMessage());
			}
		});
	}


	/*-------------------------------------------------------------------------
	 * Region: Get stuff from native to Unity
	 */

	public static boolean isCurrentUserAuthenticated()
	{
		return (OKUser.getCurrentUser() != null);
	}

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

	public static String getCurrentUserFBID()
	{
		if(OpenKit.getCurrentUser() != null)
			return OpenKit.getCurrentUser().getFBUserID();
		else
			return null;
	}

	public static String getCurrentUserGoogleID()
	{
		if(OpenKit.getCurrentUser() != null)
			return OpenKit.getCurrentUser().getGoogleID();
		else
			return null;
	}

	public static String getCurrentUserCustomID()
	{
		if(OpenKit.getCurrentUser() != null)
			return OpenKit.getCurrentUser().getCustomID();
		else
			return null;
	}

	public static boolean isFBSessionOpen()
	{
		return FacebookUtilities.isFBSessionOpen();
	}


	public static void getFacebookFriendsList(final String gameObjectName)
	{
		OKBridgeLog("getFBFriends");

		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				FacebookUtilities.GetFBFriends(new FacebookUtilities.GetFBFriendsRequestHandler() {
					@Override
					public void onSuccess(ArrayList<Long> friendsArray) {
						OKBridgeLog("getFBFriends success");
						String friendsList = FacebookUtilities.getSerializedListOfFBFriends(friendsArray);
						UnityPlayer.UnitySendMessage(gameObjectName, ASYNC_CALL_SUCCEEDED, friendsList);
					}

					@Override
					public void onFail(FacebookRequestError error) {
						OKBridgeLog("getFBFriends fail");
						if(error != null){
							UnityPlayer.UnitySendMessage(gameObjectName, ASYNC_CALL_FAILED, error.getErrorMessage());
						} else {
							UnityPlayer.UnitySendMessage(gameObjectName, ASYNC_CALL_FAILED, "Unknown error when trying to get friends from Android native");
						}
					}
				});

			}
		});
	}


}
