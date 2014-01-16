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

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import com.facebook.Session;
import io.openkit.facebookutils.FBLoginRequest;
import io.openkit.leaderboards.OKScoreCache;

/**
 * Singleton instance that stores OpenKit global items
 * such as the current user and OpenKit application. This should not be
 * accessed directly.
 * @author suneets
 *
 */

public enum OKManager {
	INSTANCE;

	private OKUser currentUser;
	private String appKey;
	private String secretKey;
	private OKScoreCache scoreCache;
	private String leaderboardListTag;
	private boolean isAchievementsEnabled = true;
	private ArrayList<Long> fbFriendsArrayList;
	private boolean hasShownFBLoginPrompt = false;

	public static final String OPENKIT_SDK_VERSION = "1.1";

	/**
	 * Initialize the OpenKit SDK with your credentials
	 * @param context Context needed, pass in your activity or app context
	 * @param appKey Your App key from the OpenKit dashboard
	 * @param secretKey Your SecretKey from the OpenKit dashboard
	 * @param secretKey
	 */
	public void configure(Context context, String appKey, String secretKey)
	{
		configure(context, appKey, secretKey, null);
	}

	/**
	 * Initialize the OpenKit SDK with your credentials
	 * @param context Context needed, pass in your activity or app context
	 * @param appKey Your App key from the OpenKit dashboard
	 * @param secretKey Your SecretKey from the OpenKit dashboard
	 * @param endpoint OpenKit server endpoint. Leave as null for default or specify your own URL
	 */
	public void configure(Context context, String appKey, String secretKey, String endpoint)
	{
		this.appKey = appKey;
		this.secretKey = secretKey;

		if(endpoint != null) {
			OKHTTPClient.setEndpoint(endpoint);
		} else {
			OKHTTPClient.setEndpoint(OKHTTPClient.DEFAULT_ENDPOINT);
		}

		// Get cached OKUser
		getOKUserInSharedPrefs(context.getApplicationContext());

		// Submit any cached scores
		scoreCache = new OKScoreCache(context.getApplicationContext());
		scoreCache.submitAllCachedScores();

		OKLog.d("OpenKit configured with endpoint: " + OKHTTPClient.getEndpoint());

		// Open cached FB session
		FBLoginRequest fbLoginRequest = new FBLoginRequest();
		fbLoginRequest.openCachedFBSession(context.getApplicationContext());

	}

	/**
	 * @return Returns the OpenKit application key.
	 */
	public String getAppKey() {
		return this.appKey;
	}

	public String getSecretKey() {
		return secretKey;
	}


	public OKUser getCurrentUser() {
		return currentUser;
	}

	public OKScoreCache getSharedCache() {
		return scoreCache;
	}

	public ArrayList<Long> getFbFriendsArrayList() {
		return fbFriendsArrayList;
	}

	public void setFbFriendsArrayList(ArrayList<Long> fbFriendsArrayList) {
		this.fbFriendsArrayList = fbFriendsArrayList;
	}

	public boolean hasShownFBLoginPrompt() {
		return hasShownFBLoginPrompt;
	}

	public void setHasShownFBLoginPrompt(boolean hasShownFBLoginPrompt) {
		this.hasShownFBLoginPrompt = hasShownFBLoginPrompt;
	}

	public boolean isAchievementsEnabled() {
		return isAchievementsEnabled;
	}

	public void setAchievementsEnabled(boolean isAchievementsEnabled) {
		this.isAchievementsEnabled = isAchievementsEnabled;
	}

	public String getLeaderboardListTag() {
		return leaderboardListTag;
	}

	public void setLeaderboardListTag(String leaderboardListTag) {
		this.leaderboardListTag = leaderboardListTag;
	}


	public void setGoogleLoginEnabled(boolean enabled) {
		OKLoginFragment.setGoogleLoginEnabled(enabled);
	}

	/**
	 * OpenKit internal only. Must call this method when a user logs in with OpenKit.
	 * @param aUser User
	 * @param context Context required to save user in preferences
	 */
	public void handlerUserLoggedIn(OKUser aUser, Context context)
	{
		logoutUserFromSharedPrefsOnNextTry = false;
		this.currentUser = aUser;
		saveOKUserInSharedPrefs(context.getApplicationContext(), currentUser);
		getSharedCache().submitAllCachedScores();
	}

	/**
	 * Log the current user out of OpenKit and delete all cached/stored data associated with the user. Requires a context.
	 * @param context
	 */
	public void logoutCurrentUser(Context context)
	{
		this.currentUser = null;
		deleteUserInSharedPrefs(context.getApplicationContext());
		Session session = Session.getActiveSession();
		if(session != null) {
			session.closeAndClearTokenInformation();
		}

		getSharedCache().clearCachedSubmittedScores();
	}

	public void logoutCurrentUserWithoutClearingFB()
	{
		this.currentUser = null;
		getSharedCache().clearCachedSubmittedScores();
		logoutUserFromSharedPrefsOnNextTry = true;
	}


	/**
	 * Get current user from shared preferences if stored
	 * @param Context, required to pull current user stored in SharedPreferences
	 * @return Current user, or null if user is not logged in
	 */
	public OKUser getCurrentUser(Context ctx)
	{
		if(currentUser != null) {
			return currentUser;
		}
		else {
			return getOKUserInSharedPrefs(ctx);
		}
	}

/* ----------------------------------------------------------------------------------------
 * region: private
 */

	/**
	 * Constants used for saving and retrieving OKUser in user preferences
	 */

	private static final String OK_PREFS_NAME = "openkit_pref";
	private static final String KEY_FB_ID = "FBUserID";
	private static final String KEY_USER_NICK = "userNick";
	private static final String KEY_OKUSER_ID = "OKuserID";
	private static final String KEY_GOOGLE_ID = "googleUserID";
	private static final String KEY_CUSTOM_ID = "customUserID";

	private boolean logoutUserFromSharedPrefsOnNextTry = false;

	private void saveOKUserInSharedPrefs(Context ctx, OKUser user)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(KEY_USER_NICK, user.getUserNick());
		editor.putInt(KEY_OKUSER_ID, user.getOKUserID());
		editor.putString(KEY_FB_ID, user.getFBUserID());
		editor.putString(KEY_GOOGLE_ID, user.getGoogleID());
		editor.putString(KEY_CUSTOM_ID, user.getCustomID());

		editor.commit();
		OKLog.v("Saved OKUser: " + user);
	}

	private void deleteUserInSharedPrefs(Context ctx)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.remove(KEY_USER_NICK);
		editor.remove(KEY_OKUSER_ID);
		editor.remove(KEY_FB_ID);
		editor.remove(KEY_GOOGLE_ID);
		editor.remove(KEY_CUSTOM_ID);

		editor.commit();
		OKLog.v("Removed cached user");
	}

	private OKUser getOKUserInSharedPrefs(Context ctx)
	{
		if(logoutUserFromSharedPrefsOnNextTry) {
			deleteUserInSharedPrefs(ctx);
			OKLog.v("Deleting user from prefs bc logoutUserFromSharedPrefsOnNextTry set to true");
			logoutUserFromSharedPrefsOnNextTry = false;
			return null;
		}

		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		OKUser user = new OKUser();

		String fbID = safeGetStringForSharedPrefKey(settings, KEY_FB_ID);
		String googleID = safeGetStringForSharedPrefKey(settings, KEY_GOOGLE_ID);
		String customID = safeGetStringForSharedPrefKey(settings, KEY_CUSTOM_ID);

		user.setFBUserID(fbID);
		user.setGoogleID(googleID);
		user.setCustomID(customID);

		user.setOKUserID(settings.getInt(KEY_OKUSER_ID, 0));
		user.setUserNick(settings.getString(KEY_USER_NICK, null));

		if(user.getOKUserID() == 0) {
			return null;
		} else {
			OKLog.d("Found cached user: "+ user);
			this.currentUser = user;
			return user;
		}
	}

	// Some OKUser properties used to be stored as long and are not stored as strings so
	// use this method to check for the old version
	private String safeGetStringForSharedPrefKey(SharedPreferences settings, String key)
	{
		String retVal = null;
		try {
			retVal = settings.getString(key, null);
			//We don't want userID strings of "0"
			if(OKJSONParser.isZeroStringLiteral(retVal)) {
				retVal = null;
			}
		} catch(ClassCastException ex) {
			long retValLong = settings.getLong(key, 0);

			if(retValLong != 0) {
				retVal = Long.toString(retValLong);
			} else {
				retVal = null;
			}
		}

		return retVal;
	}
}
