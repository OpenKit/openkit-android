openkit-android-sdk
===================

Android SDK for OpenKit Social Leaderboard and achievements.

This open-source library allows you to integrate OpenKit leaderboards, cloud
data storage, and user account management into your Android app.  OpenKit
relies on Facebook and Twitter for user authentication. Your users login with
those services, and there is no "OpenKit account" that is shown to them. 


Quick Start: Run the Sample App
===============================

The quickest way to get your hands dirty is to run the sample app that comes
with the SDK.  Instructions for doing so are found here: http://openkit.io/documentation/sample-apps#android


Basic SDK Usage
=================

Be sure to read how to integrate the SDK into
your app at http://openkit.io/documentation/#android


Initialize the SDK and set your application id
----------------------------------------------
In your main activity and all launchable activities, be sure to intialize the SDK:

Import the package
```java
import io.openkit.*;
```

Specify your application key in onCreate. You can get your application key from the OpenKit dashboard.
```java
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	...
	// Grab your app key and secret key from the OpenKit dashboard at http://developer.openkit.io/
	String myAppKey = "BspfxiqMuYxNEotLeGLm";
	String mySecretKey = "2sHQOuqgwzocUdiTsTWzyQlOy1paswYLGjrdRWWf";

	// Initialize OpenKit. You must call this when your app starts (so we call it in onCreate in our MainActivity)
	OpenKit.configure(this, myAppKey, mySecretKey);
	...
}
```

Update AndroidManifest.xml
--------------------------

Make sure your application has declared the appropriate permissions: "INTERNET" and "ACCESS_NETWORK_STATE". In your AndroidManifest.xml file, add the following lines right after the "application" tag.

```xml
<!-- Must add the INTERNET permission for OpenKit and the Facebook SDK to Work -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- Must add the GET_ACCOUNTS and USE_CREDENTIALS perimissions to support Google auth -->
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.USE_CREDENTIALS" />
```

Declare the necessary OpenKit activities in your AndroidManifest.xml file. The OpenKit SDK includes several Activities that are used to show leaderboards and provide user log in. The OpenKit SDK also relies on the Facebook Android SDK, so you need to declare the following activities in your manifest file. Then should go inside the "application" tag.

```xml
<application>
	...
	<!-- Declare the OpenKit activities as follows, these are required for OpenKit login and to show leaderboards -->
	<!-- You can copy the below exactly -->
	<activity
	            android:name="io.openkit.OKLoginActivity"
	            android:theme="@style/Theme.Transparent" />
	<activity android:name="io.openkit.leaderboards.OKLeaderboardsActivity" />
	<activity android:name="io.openkit.leaderboards.OKScoresActivity" />
	<activity android:name="io.openkit.user.OKUserProfileActivity" />
	<activity android:name="io.openkit.facebook.LoginActivity" />
	...
</application>
```

Declare your Facebook application ID in AndroidManifest.xml . This also goes inside the "application" tag.
```xml 
<application>
	...
	<!-- Metadata tag required by facebook SDK. References the FB app id stored in strings -->
    <meta-data
         android:name="com.facebook.sdk.ApplicationId"
         android:value="@string/fb_app_id" />
	...
</application>
```


User accounts
==============
Because OpenKit uses Facebook and Google as authentication providers, you don't need to worry about user account management.

OpenKit provides a user class, OKUser, that manages most of the functionality you'll need for account management. 

To get the current OpenKit user, simply call:

```java
if(OpenKit.getCurrentUser() != null) {
	//User is logged in
	OKUser currentUser = OpenKit.getCurrentUser();
}
else {
	// No user is logged in
}
```
You can get the current user any time, it will return null if the user is not authenticated. 

User Login
----------

If you're using OpenKit leaderboards, your users will be prompted to log in when the Leaderboards UI is shown. You can optionally prompt them to login at anytime:

```java
Intent launchOKLogin = new Intent(MainActivity.this, OKLoginActivity.class);
startActivity(launchOKLogin);
// You can also use startActivityForResult(launchOKLogin, LOGIN_ACTIVITY_RESULT_CODE);
// and then check for the current user
```


Social Leaderboards
===================
The OpenKit SDK provides a drop in solution for cross-platform, social leaderboards that work on both iOS and Android. You define your leaderboards and their attributes in the OpenKit dashboard.

Show Leaderboards
------------------
Import the leaderboards package

```java
import io.openkit.leaderboards.*;
```

Start the Leaderboards activity. If the user isn't logged in, they will be prompted to login when the activity is shown.
```java
Intent launchOKLeaderboards = new Intent(MainActivity.this, OKLeaderboardsActivity.class);
startActivity(launchOKLeaderboards);
```

This will show a list of all the leaderboards defined for your app.

Show a Single Leaderboard
-------------------------

To show  a single leaderboard, use this convenience method from OKLeaderboard to get an intent. MainActivity is a context (usually the calling activity).

```java
Intent leaderboardIntent = OKLeaderboard.getLeaderboardIntent(MainActivity.this,leaderboardID);
startActivity(leaderboardIntent);
```

Submit a Score
--------------
To submit a score, you simply create an OKScore object, set it's value, and then call submit. The OpenKit SDK has built in local caching for offline score submission, as well as caching scores when the player is not logged into OpenKit, so you can always submit scores. 

You can use anonymous callbacks to detect the success and failure cases, and handle them appropriately. 

```java
OKScore score = new OKScore();
score.setScoreValue(123434); 
score.setOKLeaderboardID(TOP_SCORES_LEADERBOARD_ID); 
//Leaderboard ID comes from the OpenKit dashboard

score.submitScore(new OKScore.ScoreRequestResponseHandler() {
	@Override
	public void onSuccess() {
		Log.i("OpenKit", "Score submission successful");
	}
	
	@Override
	public void onFailure(Throwable error) {
		Log.i("OpenKit", "Score submission failed: " + error);
	});
```


Achievements
============
The OpenKit SDK provides a single activity that shows both leaderboards and achievements. By default, this activity shows leaderboards first.

Show Achievements
------------------
If you want to show achievements directly, use this convenience method. MainActivity is a context (usually the calling activity).

```java
Intent launchAchievementsIntent = OKAchievement.getAchievementsIntent(MainActivity.this);
startActivity(launchAchievementsIntent);
```

Submit Achievement Progress
----------------------------
The API to submit achievement progress is very similar to submitting scores. You can use anonymous callbacks to detect the success and failure cases of submission.


```java
OKAchievementScore achievementScore = new OKAchievementScore();
			achievementScore.setProgress(10);
			achievementScore.setOKAchievementId(sampleAchievemetID);
			achievementScore.submitAchievementScore(new OKAchievementScore.AchievementScoreRequestResponseHandler() {
				@Override
				public void onSuccess() {
					Log.i("OpenKit","Submitted an achievement score!");
				}

				@Override
				public void onFailure(Throwable error) {
					Log.i("OpenKit","Failed to submit achievement score.");
				}
			});
```
