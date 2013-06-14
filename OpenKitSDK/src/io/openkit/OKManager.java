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

import android.content.Context;
import android.content.SharedPreferences;
import io.openkit.facebook.Session;

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
	private String OKAppID;
	
	/**
	 * OpenKit internal only, developers should use OpenKit.initialize()
	 * @param context
	 * @param OKAppID
	 */
	public void initialize(Context context, String OKAppID)
	{
		this.OKAppID = OKAppID;
		this.getCurrentUser(context);
	}
	
	/**
	 * @return Returns the OpenKit application ID.
	 */
	public String getOKAppID()
	{
		return this.OKAppID;
	}
	
	public OKUser getCurrentUser()
	{
		return currentUser;
	}
	
	/**
	 * Get current user from shared preferences if stored
	 * @param Context, required to pull current user stored in SharedPreferences
	 * @return Current user, or null if user is not logged in
	 */
	private OKUser getCurrentUser(Context ctx)
	{
		if(currentUser != null) {
			return currentUser;
		}
		else {
			return getOKUserInSharedPrefs(ctx);
		}
	}
	
	/**
	 * OpenKit internal only. Must call this method when a user logs in with OpenKit.
	 * @param aUser User
	 * @param context Context required to save user in preferences
	 */
	public void handlerUserLoggedIn(OKUser aUser, Context context)
	{
		this.currentUser = aUser;
		saveOKUserInSharedPrefs(context, currentUser);
	}
	
	/**
	 * Log the current user out of OpenKit and delete all cached/stored data associated with the user. Requires a context.
	 * @param context
	 */
	public void logoutCurrentUser(Context context)
	{
		this.currentUser = null;
		deleteUserInSharedPrefs(context);
		Session session = Session.getActiveSession();
		if(session != null)
			session.closeAndClearTokenInformation();
	}
	
	/**
	 * Constants used for saving and retrieving OKUser in user preferences 
	 */
	
	private static final String OK_PREFS_NAME = "openkit_pref";
	private static final String KEY_fbUserId = "FBUserID";
	private static final String KEY_twitterUserID = "twitterUserID";
	private static final String KEY_userNick = "userNick";
	private static final String KEY_okUserID = "OKuserID";
	
	
	private void saveOKUserInSharedPrefs(Context ctx, OKUser user)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(KEY_userNick, user.getUserNick());
		editor.putInt(KEY_okUserID, user.getOKUserID());
		editor.putLong(KEY_fbUserId, user.getFBUserID());
		editor.putLong(KEY_twitterUserID, user.getTwitterUserID());
		
		editor.commit();
	}
	
	private void deleteUserInSharedPrefs(Context ctx)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.remove(KEY_userNick);
		editor.remove(KEY_okUserID);
		editor.remove(KEY_fbUserId);
		editor.remove(KEY_twitterUserID);
		
		editor.commit();
	}
	
	private OKUser getOKUserInSharedPrefs(Context ctx)
	{
		SharedPreferences settings = ctx.getSharedPreferences(OK_PREFS_NAME, Context.MODE_PRIVATE);
		OKUser user = new OKUser();
		
		user.setFBUserID(settings.getLong(KEY_fbUserId, 0));
		user.setOKUserID(settings.getInt(KEY_okUserID, 0));
		user.setTwitterUserID(settings.getLong(KEY_twitterUserID, 0));
		user.setUserNick(settings.getString(KEY_userNick, null));
		
		if(user.getOKUserID() == 0)
			return null;
		else
		{
			this.currentUser = user;
			return user;
		}
	}
}
