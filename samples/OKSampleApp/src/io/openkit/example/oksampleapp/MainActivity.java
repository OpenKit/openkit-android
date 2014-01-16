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


import com.facebook.widget.ProfilePictureView;
import io.openkit.*;
import io.openkit.leaderboards.*;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	private Button loginToOpenKitButton;
	private Button showLeaderboardsButton;
	private Button submitScoresButton;
	private Button showAchievementsButton;
	private Button logoutButton;
	private Button submitAchievementButton;

	private ProfilePictureView profilePictureView;

	private TextView userNameTextView;

	private static final int LOGIN_ACTIVITY_RESULT_CODE = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Grab your app key and secret key from the OpenKit dashboard at http://developer.openkit.io/
		String myAppKey = "BspfxiqMuYxNEotLeGLm";
		String mySecretKey = "2sHQOuqgwzocUdiTsTWzyQlOy1paswYLGjrdRWWf";


		// Initialize OpenKit. You must call this when your app starts (so we call it in onCreate in our MainActivity)
		OpenKit.configure(this, myAppKey, mySecretKey);

		// To disable achievements from showing in the UI, setAchievementsEnabled(false)
		//OKManager.INSTANCE.setAchievementsEnabled(false);

		// Set the leaderboard list tag. By default, client asks
	    // for tag = "v1". In the OpenKit dashboard, new leaderboards
	    // have a default tag of "v1" as well. You can use this
	    // tag feature to display different leaderboards in different
	    // versions of your game. Each leaderboard can have multiple tags, but the client
	    // will only display one tag.
		//OKManager.INSTANCE.setLeaderboardListTag("v2");

		// Showing how to disable GoogleAuth from the UI if you don't want to use it
		//OKManager.INSTANCE.setGoogleLoginEnabled(false);

		showLeaderboardsButton = (Button)findViewById(R.id.LeaderboardsButton);
		loginToOpenKitButton = (Button)findViewById(R.id.OKLoginButton);
		submitScoresButton = (Button)findViewById(R.id.submitScoreButton);
		logoutButton = (Button)findViewById(R.id.OKLogoutButton);
		profilePictureView = (ProfilePictureView)findViewById(R.id.fbProfilePicView);
		userNameTextView = (TextView)findViewById(R.id.userNameTextView);

		showAchievementsButton = (Button)findViewById(R.id.showAchievementsButton);
		submitAchievementButton = (Button)findViewById(R.id.submitAchievementButton);

		loginToOpenKitButton.setOnClickListener(loginToOpenKitClickedClickListener);
		showLeaderboardsButton.setOnClickListener(showOKLeaderboards);
		submitScoresButton.setOnClickListener(submitScore);
		logoutButton.setOnClickListener(logoutOfOpenKit);
		showAchievementsButton.setOnClickListener(showAchievements);

		submitAchievementButton.setOnClickListener(submitAchievementProgress);

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
			profilePictureView.setProfileId(currentUser.getFBUserID());
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

			OKLoginActivity.setActivityHandler(new OKLoginActivityHandler() {
				@Override
				public void onLoginDialogComplete() {
					OKLog.v("Finished showing the OpenKit login dialog");
				}
			});

			Intent launchOKLogin = new Intent(MainActivity.this, OKLoginActivity.class);
			startActivity(launchOKLogin);
		}
	};

	/**
	 * Showing sample of how to handle the activity result after prompting for login
	 */
	@Override
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

			//Show the entire list of leaderboards
			Intent launchOKLeaderboards = new Intent(MainActivity.this, OKLeaderboardsActivity.class);
			startActivity(launchOKLeaderboards);

			// Code below shows how to launch directly to a specific leaderboard ID
			//Intent launchleaderboard = OKLeaderboard.getLeaderboardIntent(MainActivity.this, 30);
			//startActivity(launchleaderboard);
		}
	};

	private View.OnClickListener showAchievements = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent launchAchievementsIntent = OKAchievement.getAchievementsIntent(MainActivity.this);
			startActivity(launchAchievementsIntent);
		}
	};


	private static final int sampleAchievemetID = 188;

	/**
	 * Submit achievement progress to achievement
	 */
	private View.OnClickListener submitAchievementProgress = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			OKAchievementScore achievementScore = new OKAchievementScore();
			achievementScore.setProgress(10);
			achievementScore.setOKAchievementId(sampleAchievemetID);
			achievementScore.submitAchievementScore(new OKAchievementScore.AchievementScoreRequestResponseHandler() {
				@Override
				public void onSuccess() {
					OKLog.d("Submitted an achievement score!");
					Toast.makeText(MainActivity.this.getApplicationContext(), "Unlocked achievement!", Toast.LENGTH_LONG).show();
				}

				@Override
				public void onFailure(Throwable error) {
					Toast.makeText(MainActivity.this.getApplicationContext(), "Failed to unlock achievement with error" + error, Toast.LENGTH_LONG).show();
					OKLog.d("Failed to submit achievement score.");
				}
			});
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
