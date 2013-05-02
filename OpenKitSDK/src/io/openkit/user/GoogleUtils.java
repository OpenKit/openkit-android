package io.openkit.user;


import io.openkit.asynchttp.AsyncHttpClient;
import io.openkit.asynchttp.OKJsonHttpResponseHandler;
import io.openkit.asynchttp.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;

public class GoogleUtils {

	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

	private static void logit(String message){
		Log.d("OKC", message);
	}

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

	public static void getGoogleUserInfo(String authToken)
	{
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Content-Type", "application/json");
		client.addHeader("Accept", "application/json");

		RequestParams params =  new RequestParams();
		params.put("access_token", authToken);

		client.get("https://www.googleapis.com/oauth2/v1/userinfo", params, new OKJsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject object) {
				// TODO Auto-generated method stub
				logit( "Got json object: " + object);
			}

			@Override
			public void onSuccess(JSONArray array) {
				logit( "Got json array: " + array);				
			}

			@Override
			public void onFailure(Throwable error, String content) {
				logit( "Error: " + error + "content: " + content);
			}

			@Override
			public void onFailure(Throwable e, JSONArray errorResponse) {
				logit( "Error: " + e);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				logit( "Error: " + e);
			}
		});
	}


}
