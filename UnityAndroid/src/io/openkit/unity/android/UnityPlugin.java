package io.openkit.unity.android;

import java.util.Locale;

import io.openkit.OKAchievementScore;
import io.openkit.OKLoginActivity;
import io.openkit.OKScore;
import io.openkit.OKUser;
import io.openkit.OKScore.ScoreRequestResponseHandler;
import io.openkit.OpenKit;
import io.openkit.leaderboards.OKLeaderboardsActivity;
import android.content.Intent;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

public class UnityPlugin {
	

	public static void logD(String format, Object... args) {
		Log.d("OpenKitPlugin", String.format(Locale.getDefault(), format, args));
	}
	
	/**
	 * Initialize OpenKit SDK with the given App ID
	 * @param appID
	 */
	public static void setAppKey(String appKey)
	{
		logD("Initialized OpenKit");
		OpenKit.initialize(UnityPlayer.currentActivity, appKey);
	}
	
	/**
	 * Sets the server endpoint for OpenKit calls
	 * @param endpoint Base URL for endpoint, e.g. "http://stage.openkit.io/"
	 */
	public static void setEndpoint(String endpoint)
	{
		OpenKit.setEndpoint(endpoint);
	}
	
	/**
	 * Shows OpenKit leaderboads for the given app
	 */
	public static void showLeaderboards()
	{
		logD("Launching Leaderboards UI");
		UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
			  public void run() {
					Intent leaderboards = new Intent(UnityPlayer.currentActivity, OKLeaderboardsActivity.class);
					UnityPlayer.currentActivity.startActivity(leaderboards);
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
			  public void run() {
					Intent loginUI = new Intent(UnityPlayer.currentActivity, OKLoginActivity.class);
					UnityPlayer.currentActivity.startActivity(loginUI);
			  }
			});
	}
	
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
	
	

}
