openkit-android-sdk
===================

Android SDK for OpenKit Leaderboards and Cloud Storage

This open-source library allows you to integrate OpenKit leaderboards, cloud
data storage, and user account management into your Android app.  OpenKit
relies on Facebook and Twitter for user authentication. Your users login with
those services, and there is no "OpenKit account" that is shown to them. 


Quick Start: Run the Sample App
===============================

The quickest way to get your hands dirty is to run the sample app that comes
with the SDK.  Instructions for doing so are found here: http://openkit.io/docs/android


Basic SDK Usage
=================

Be sure to read how to integrate the SDK into
your app at http://openkit.io/docs/android-integration


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
	OpenKit.initialize(this,"SD3fSD3SDlJu");
	...
}
```

Update AndroidManifest.xml
--------------------------

Make sure your application has declared the appropriate permissions: "INTERNET" and "ACCESS_NETWORK_STATE". In your AndroidManifest.xml file, add the following lines right after the <Applcation> tag.

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Declare the necessary OpenKit activities in your AndroidManifest.xml file. The OpenKit SDK includes several Activities that are used to show leaderboards and provide user log in. The OpenKit SDK also relies on the Facebook Android SDK, so you need to declare the following activities in your manifest file. Then should go inside the <application> tag.

```xml
<application>
	...
	<!-- Declare the OpenKit activities as follows, these are required for OpenKit login and to show leaderboards -->
	<!-- You can copy the below exactly -->
	<activity android:name="io.openkit.OKLoginActivity" android:theme="@style/Theme.Transparent" />
	<activity android:name="io.openkit.leaderboards.OKLeaderboardsActivity" />
	<activity android:name="io.openkit.leaderboards.OKScoresActivity" />
	<activity android:name="io.openkit.user.OKUserProfileActivity" />
	<!-- Facebook login activity declaration required by Facebook SDK. Also required by OpenKit SDK. Copy this exactly. -->
    <activity android:name="com.facebook.LoginActivity" />
	...
</application>
```


User accounts
==============
Because OpenKit uses Facebook and Twitter(coming soon!) as authentication providers, you don't need to worry about user account management.

OpenKit provides a user class, OKUser, that manages most of the functionality you'll need for account management. 

Users are unique to each developer, but can be shared across multiple OpenKit applications from the same developer account. 

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

If you're using cloud storage, the cloud storage calls require an authenticated user.




Leaderboards
=============
The OpenKit SDK provides a drop in solution for cross-platform leaderboards that work on both iOS and Android.

You define your leaderboards and their attributes in the OpenKit dashboard, and the client 

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

Submit a Score
--------------
To submit a score, you simply create an OKScore object, set it's value, and then call submit. 

Submitting a score requires the user to be authenticated.

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





Cloud Storage
=============
OpenKit allows you to seamlessly store data user data in the cloud. Saving user progress, game state, and other user information is as easy as using get and set methods. This data can then be accessed on both iOS and Android.

The OKCloud class provides a single set/get API pair, which automatically scopes the stored data by user. 

OKCloud requires that the user be authenticated before making get/set requests. 

Simple Example
--------------
Let's take a simple example, first storing the string "Hello world" for the key "myKey":

First, import the necessary package:
```java
import io.openkit.okcloud*;
```
Now, call OKCloud.set. This will be stored for the current authenticated OKUser.
```java
OKCloud.set("Hello world", "myKey", new OKCloudHandler() {
   @Override
   public void complete(Object obj, OKCloudException e) {
     if (e == null) {
       OKLog.d("Successfully stored string.");
     } else {
       OKLog.d("Error storing string: %s", e.getMessage());
     }
   }
 });
```
Sometime later, you can get the "Hello World" back with: 
```java
OKCloud.get("myKey", new OKCloudHandler(){
  @Override
  public void complete(Object obj, OKCloudException e) {
    if (e == null) {
      OKLog.d("Got the string: %s", obj);		// "Got the string: Hello world"
    } else {
      OKLog.d("Error getting string: %s", e.getMessage());
    }
  }
});
```
Data Types 
------------
Along with Strings, the following data types can be stored successfully: 

* HashMap
* Array
* Integer
* Boolean
* Double
* Strings

Each of the above will be serialized and deserialized automatically for you.  For example, if we initialize a HashMap like this: 

```java
final Object[] arr = { "one", "two", 1 };
HashMap<Object, Object> obj = new HashMap<Object, Object>();
obj.put("property1", "foo");
obj.put("property2", -99);
obj.put("property3", arr);s
```

We can then store the full object like this: 

```java
OKCloud.set(obj, "myKey2", new OKCloudHandler() {
	@Override
	public void complete(Object obj, OKCloudException e) {
		if (e == null) {
			OKLog.d("Successfully stored HashMap.");
		} else {
			OKLog.d("Error storing HashMap: %@", e.getMessage());
		}
	}
});
```

And then we can retrieve it (and print the deserialized data types) with:  

```java
  OKCloud.get("myKey2", new OKCloudHandler() {
    @Override
    public void complete(Object obj, OKCloudException e) {
      if (e == null) {
        LinkedHashMap<?, ?> hm = (LinkedHashMap<?, ?>)obj;
        Object val1 = hm.get("property1");
        Object val2 = hm.get("property2");
        Object val3 = hm.get("property3");
        OKLog.d("Property 1:\n  value: %s\n  class: %s", val1, val1.getClass().getName());
        OKLog.d("Property 2:\n  value: %d\n  class: %s", val2, val2.getClass().getName());
        OKLog.d("Property 3:\n  value: %s\n  class: %s", val3.toString(), val3.getClass().getName());
      } else {
        OKLog.d("Error getting string: %s", e.getMessage());
      }
    }
  });
```

This will output: 

```
Property 1: 
  value: foo
  class: String
Property 2:
  value: -99 
  class: Integer
Property 3: 
  value: ["one", "two", 1]
  class: ArrayList
```


