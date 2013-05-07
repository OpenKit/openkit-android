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
package io.openkit.example.oksampleapp;



import java.util.List;

import org.json.JSONObject;

import io.openkit.facebook.widget.ProfilePictureView;

import io.openkit.*;
import io.openkit.leaderboards.*;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {
	
	private Button loginToOpenKitButton;
	private Button showLeaderboardsButton;
	private Button submitScoresButton;
	private Button cloudDataButton;
	private Button logoutButton;
	private Button submitAchievementButton, showAchievementsButton;
	
	private ProfilePictureView profilePictureView;
	
	private TextView userNameTextView;
	
	private static final int LOGIN_ACTIVITY_RESULT_CODE = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		OpenKit.setEndpoint("http://10.0.1.21:3000");
		OpenKit.initialize(this,"VwfMRAl5Gc4tirjw");
		
		showLeaderboardsButton = (Button)findViewById(R.id.LeaderboardsButton);
		loginToOpenKitButton = (Button)findViewById(R.id.OKLoginButton);
		submitScoresButton = (Button)findViewById(R.id.submitScoreButton);
		cloudDataButton = (Button)findViewById(R.id.cloudDataButton);
		logoutButton = (Button)findViewById(R.id.OKLogoutButton);
		profilePictureView = (ProfilePictureView)findViewById(R.id.fbProfilePicView);
		userNameTextView = (TextView)findViewById(R.id.userNameTextView);
		
		submitAchievementButton = (Button)findViewById(R.id.submitAchievementButton);
		showAchievementsButton = (Button)findViewById(R.id.showAchievementsButton);
		
		loginToOpenKitButton.setOnClickListener(loginToOpenKitClickedClickListener);
		showLeaderboardsButton.setOnClickListener(showOKLeaderboards);
		submitScoresButton.setOnClickListener(submitScore);
		cloudDataButton.setOnClickListener(cloudDataDemoClickListener);
		logoutButton.setOnClickListener(logoutOfOpenKit);
		
		submitAchievementButton.setOnClickListener(submitAchievementProgress);
		showAchievementsButton.setOnClickListener(showAchievements);
		
		//Update the view with the current user
		updateView();
		
	}
	
	/**
	 * This method upates the view based on whether the user is logged into OpenKit or not
	 */
	private void updateView()
	{
		if(OpenKit.getCurrentUser() != null){
			//Get the current user
			OKUser currentUser = OpenKit.getCurrentUser();
			
			//Hide the login button
			loginToOpenKitButton.setVisibility(View.GONE);
			logoutButton.setVisibility(View.VISIBLE);
			
			//Show the user's profile pic and nickname
			profilePictureView.setProfileId(Long.toString(currentUser.getFBUserID()));
			userNameTextView.setText(currentUser.getUserNick());
			
			// Note: we currently use ProfilePicView from the Facebook SDK to show user profile images
			// only because Twitter authentication is not yet implemented. Once Twitter auth is in place,
			// this will be replaced by an OKUserProfilePicView class.
		}
		else {
			//Show the login button
			loginToOpenKitButton.setVisibility(View.VISIBLE);
			logoutButton.setVisibility(View.GONE);
			
			//Not signed in
			userNameTextView.setText(R.string.notLoginString);
			profilePictureView.setProfileId("");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/**
	 * OnClickListener that launches the OKLoginActivity to ask a user to Login to OpenKit.
	 * 
	 * This method uses startActivityForResult() on the OKLoginActivity, but this is not required.
	 * You can show OKLoginActivity with startActivity() also.
	 */
	private View.OnClickListener loginToOpenKitClickedClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent launchOKLogin = new Intent(MainActivity.this, OKLoginActivity.class);
			startActivityForResult(launchOKLogin, LOGIN_ACTIVITY_RESULT_CODE);
			//You can also use startActivit(launchOKLogin);
		}
	};
	
	/**
	 * Showing sample of how to handle the activity result after prompting for login
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == LOGIN_ACTIVITY_RESULT_CODE)
		{
			// Check if it was the login activity that is presenting the result
			
			// Perform work here that requires the user to be logged in
		}
		
		if(OpenKit.getCurrentUser() != null)
		{
			// Perform work here that requires the user to be logged in
			//This is always the way to check if the user is logged in.
			OKLog.v("onActivityResult, user is logged in");
		}
		
		//Update the view
		updateView();
	}
	
	@Override
	public void onResume()
	{
		updateView();
		super.onResume();
	}

	
	/**
	 * OnClickListener that logs the current user out of OpenKit
	 */
	private View.OnClickListener logoutOfOpenKit = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			OKUser.logoutCurrentUser(MainActivity.this);
			
			//Update the view
			updateView();
		}
	};
	
	/**
	 * Launch OKLeaderboards to show leaderboards
	 */
	private View.OnClickListener showOKLeaderboards = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			//Get the leaderboards
			Intent launchOKLeaderboards = new Intent(MainActivity.this, OKLeaderboardsActivity.class);
			startActivity(launchOKLeaderboards);
		}
	};
	
	
	/**
	 * Submit achievement progress to achievement
	 */
	private View.OnClickListener submitAchievementProgress = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			OKAchievementScore achievementScore = new OKAchievementScore();
			achievementScore.setProgress(3);
			achievementScore.setOKAchievementId(1);
			achievementScore.submitAchievementScore(new OKAchievementScore.AchievementScoreRequestResponseHandler() {
				@Override
				public void onSuccess() {
					OKLog.d("Submitted an achievement score!");
				}

				@Override
				public void onFailure(Throwable error) {
					OKLog.d("Failed to submit achievement score.");
				}
			});
		}
	};
	
	private View.OnClickListener showAchievements = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			//Get the leaderboards
			OKAchievement.getAchievements(new OKAchievementsListResponseHandler() {

				@Override
				public void onSuccess(List<OKAchievement> achievementsList) {
					OKLog.d("Got a list of achievements!");
					OKAchievement ach = achievementsList.get(0);
					OKLog.d("Progress of first achievement is " + ach.getProgress());
				}

				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					OKLog.d("Failed to get list of achievements: " + errorResponse);
				}
			});
			
		}
	};
	
	
	/**
	 * Launch the cloud storage demo activity
	 */
	private View.OnClickListener cloudDataDemoClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent launchCloudDemo = new Intent(MainActivity.this, OKCloudSampleActivity.class);
			startActivity(launchCloudDemo);
		}
	};
	
	/**
	 * Launch the view to submit scores
	 */
	private View.OnClickListener submitScore = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Intent launchScoreSubmitter = new Intent(MainActivity.this, SubmitScoreActivity.class);
			startActivity(launchScoreSubmitter);
		}
	};

}
