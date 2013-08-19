package io.openkit.user;


import io.openkit.OKLog;
import io.openkit.OKUser;
import io.openkit.asynchttp.AsyncHttpClient;
import io.openkit.asynchttp.OKJsonHttpResponseHandler;
import io.openkit.asynchttp.RequestParams;
import io.openkit.user.OKUserUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

public class GoogleUtils {

	public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;


	public static String[] getGoogleAccountNames(Context ctx)
	{
		Account[] accounts = getGoogleAccounts(ctx);
		String[] names = new String[accounts.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}

	public static Account[] getGoogleAccounts(Context ctx)
	{
		AccountManager manager  = AccountManager.get(ctx);
		return manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	}

	public static void showGooglePlayServicesErrorDialog(final Activity activityToShowFrom, final int code) {
		activityToShowFrom.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Dialog d = GooglePlayServicesUtil.getErrorDialog(
						code,
						activityToShowFrom,
						REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
				d.show();
			}
		});
	}

	public static abstract class GetGoogleUserInfoRequestHandler {
		public abstract void onSuccess(JSONObject userinfo);
		public abstract void onFailure();
	}

	public static void getGoogleUserInfo(String authToken, final GetGoogleUserInfoRequestHandler googleRequestHandler)
	{
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Content-Type", "application/json");
		client.addHeader("Accept", "application/json");

		RequestParams params =  new RequestParams();
		params.put("access_token", authToken);



		client.get("https://www.googleapis.com/oauth2/v1/userinfo", params, new OKJsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject object) {
				googleRequestHandler.onSuccess(object);
				OKLog.v("Successfully got Google user info");
			}

			@Override
			public void onSuccess(JSONArray array) {
				// Not expecting an Array object
				OKLog.v("Got a JSON Array when expecting a JSON object while getting Google user info");
				googleRequestHandler.onFailure();
			}

			@Override
			public void onFailure(Throwable error, String content) {
				googleRequestHandler.onFailure();
				OKLog.v( "Error getting Google user info: " + error + "content: " + content);
			}

			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				googleRequestHandler.onFailure();
				OKLog.v( "Error getting Google user info: " + e);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				googleRequestHandler.onFailure();
				OKLog.v( "Error getting Google user info: " + e);
			}
		});
	}


	public static void createOrUpdateOKUserFromGoogle(final Context ctx, final String googleAuthToken, final CreateOrUpdateOKUserRequestHandler requestHandler)
	{
		getGoogleUserInfo(googleAuthToken, new GetGoogleUserInfoRequestHandler() {

			@Override
			public void onSuccess(JSONObject userinfo) {

				String googleID, userNick;
				// Parse the user info and then create the OKUser
				try {
					//If the google account doesn't return an ID then we fail
					googleID = userinfo.getString("id");
				} catch (JSONException e) {
					requestHandler.onFail(new Error("Google user info request did not return a user ID"));
					return;
				}

				try {
					userNick = userinfo.getString("name");
				} catch (JSONException e) {
					//If the google account doesn't return a name we just give them a nickname
					userNick = "Me";
				}


				OKUser currentUser = OKUser.getCurrentUser();

				if(currentUser != null) {
					if(currentUser.getGoogleID() != null && currentUser.getGoogleID().equalsIgnoreCase(googleID)) {
						requestHandler.onSuccess(currentUser);
					} else {
						currentUser.setGoogleID(googleID);
						OKUserUtilities.updateOKUser(currentUser, requestHandler);
					}

				} else {
					OKUserUtilities.createOKUser(OKUserIDType.GoogleID, googleID, userNick, requestHandler);
				}
			}

			@Override
			public void onFailure() {
				//If the user info requests fails, let's invalidate the Google auth token
				GoogleAuthUtil.invalidateToken(ctx, googleAuthToken);
				OKLog.v("Invalidated Google auth token");
				requestHandler.onFail(new Error("Getting Google user info failed"));
			}
		});
	}


}
