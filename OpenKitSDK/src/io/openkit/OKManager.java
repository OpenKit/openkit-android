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
import io.openkit.facebook.Session;
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

		getOKUserInSharedPrefs(context.getApplicationContext());

		scoreCache = new OKScoreCache(context.getApplicationContext());
		scoreCache.submitAllCachedScores();
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
	private static final String KEY_fbUserId = "FBUserID";
	private static final String KEY_userNick = "userNick";
	private static final String KEY_okUserID = "OKuserID";
	private static final String KEY_googleUserID = "googleUserID";
	private static final String KEY_customID = "customUserID";


	private void saveOKUserInSharedPrefs(Context ctx, OKUser user)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(KEY_userNick, user.getUserNick());
		editor.putInt(KEY_okUserID, user.getOKUserID());
		editor.putLong(KEY_fbUserId, user.getFBUserID());
		editor.putString(KEY_googleUserID, user.getGoogleID());
		editor.putLong(KEY_customID, user.getCustomID());

		editor.commit();
		OKLog.v("Saved OKUser: " + user);
	}

	private void deleteUserInSharedPrefs(Context ctx)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.remove(KEY_userNick);
		editor.remove(KEY_okUserID);
		editor.remove(KEY_fbUserId);
		editor.remove(KEY_googleUserID);
		editor.remove(KEY_customID);

		editor.commit();
		OKLog.v("Removed cached user");
	}

	private OKUser getOKUserInSharedPrefs(Context ctx)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		OKUser user = new OKUser();

		user.setFBUserID(settings.getLong(KEY_fbUserId, 0));
		user.setOKUserID(settings.getInt(KEY_okUserID, 0));
		user.setUserNick(settings.getString(KEY_userNick, null));
		user.setGoogleID(settings.getString(KEY_googleUserID, null));
		user.setCustomID(settings.getLong(KEY_customID, 0));

		if(user.getOKUserID() == 0)
			return null;
		else
		{
			OKLog.d("Found cached user: " + user);
			this.currentUser = user;
			return user;
		}
	}


}
