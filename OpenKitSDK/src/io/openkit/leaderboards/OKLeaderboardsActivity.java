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

package io.openkit.leaderboards;

import io.openkit.*;
import io.openkit.user.OKUserProfileActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;

public class OKLeaderboardsActivity extends FragmentActivity {

	private OKLeaderboardsFragment leaderboardsFragment;
	private OKAchievementsFragment achievementsFragment;

	private int leaderboardTitleID;
	private int achievementsTitleID;

	private static String LEADERBOARD_FRAGMENT_TAG = "LeaderboardsFragment";
	private static String ACHIEVEMENT_FRAGMENT_TAG = "AchievementsFragment";

	private boolean showAchievements;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int themeID = getResources().getIdentifier("OKActivityTheme", "style", getPackageName());
		this.setTheme(themeID);
		super.onCreate(savedInstanceState);

		leaderboardTitleID = getResources().getIdentifier("io_openkit_title_leaderboards", "string", getPackageName());
		achievementsTitleID = getResources().getIdentifier("io_openkit_title_achievements", "string", getPackageName());

		// Check to see if default display is Achievements instead of Leaderboards
		Bundle extrasBundle = getIntent().getExtras();
		if(extrasBundle != null) {
			showAchievements = extrasBundle.getBoolean(OKAchievement.OK_ACHIEVEMENT_KEY,false);
		} else {
			showAchievements = false;
		}

		if(savedInstanceState == null) {
			if(showAchievements) {
				showAchievementsList();
			} else {
				showLeaderboardsList();
			}
		}
	}

	private void showProfileActivity()
	{
		//If the user is logged in, show the profile view, else show the login view
		if(OKUser.getCurrentUser() != null){
			Intent showProfile = new Intent(OKLeaderboardsActivity.this, OKUserProfileActivity.class);
			startActivity(showProfile);
		}
		else{
			Intent loginIntent = new Intent(OKLeaderboardsActivity.this, OKLoginActivity.class);
			startActivity(loginIntent);
		}
	}

	private void showLeaderboardsList()
	{
		OKLog.d("Show leaderboards list");

		if(leaderboardsFragment == null) {
			leaderboardsFragment = new OKLeaderboardsFragment();
		}

		if(!leaderboardsFragment.isVisible()){

			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, leaderboardsFragment, LEADERBOARD_FRAGMENT_TAG);
			ft.commit();
		}

		this.setTitle(leaderboardTitleID);
	}

	private void showAchievementsList()
	{
		OKLog.d("Show achievements list");

		if(achievementsFragment == null) {
			achievementsFragment = new OKAchievementsFragment();
		}

		if(!achievementsFragment.isVisible()) {
			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, achievementsFragment, ACHIEVEMENT_FRAGMENT_TAG);
			ft.commit();
		}

		this.setTitle(achievementsTitleID);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		///Note: since this is a library project, this has to be an IF/ELSE
		// statement instead of a switch because Library projects can't use the R.id.NAME
		// in a switch statement
		int profileButtonId = getResources().getIdentifier("io_openkit_menu_profileButton", "id", getPackageName());
		int leaderboardButtonID = getResources().getIdentifier("io_openkit_menu_leaderboardsButton", "id", getPackageName());
		int achievementsButtonID =  getResources().getIdentifier("io_openkit_menu_achievementsButton", "id", getPackageName());

		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		} //else if (item.getItemId() == R.id.io_openkit_menu_profileButton) {
		else if (item.getItemId() == profileButtonId) {
			showProfileActivity();
			return true;
		} else if(item.getItemId() == leaderboardButtonID) {
			showLeaderboardsList();
			return true;
		} else if (item.getItemId() == achievementsButtonID) {
			showAchievementsList();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
	    MenuInflater inflater = getMenuInflater();
	    int menuID;

	    if(OKManager.INSTANCE.isAchievementsEnabled()) {
	    	menuID = getResources().getIdentifier("io_openkit_leaderboards", "menu", getPackageName());
	    } else {
	    	menuID = getResources().getIdentifier("io_openkit_menu_leaderboards_no_achievements", "menu", getPackageName());
		}
	    inflater.inflate(menuID, menu);
	    //inflater.inflate(R.menu.io_openkit_leaderboards, menu);
	    return true;
	}

}