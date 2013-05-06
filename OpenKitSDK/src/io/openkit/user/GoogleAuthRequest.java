package io.openkit.user;

import io.openkit.OKLog;

import java.io.IOException;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;


import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GoogleAuthRequest {

	public static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 2001;

	private GoogleAuthRequestHandler mRequestHandler;
	private Fragment mFragment;
	private Account mAccount;
	//private String mAuthToken;

	public GoogleAuthRequest(Fragment callingFragment, Account accountToLoginWith) {
		mFragment = callingFragment;
		mAccount = accountToLoginWith;
	}

	public void loginWithGoogleAccount(GoogleAuthRequestHandler requestHandler)
	{
		mRequestHandler = requestHandler;
		getAccountAuthTokenInBackground(mRequestHandler);
	}
	
	/**
	 * The activity initiating the Google auth request must call this function in it's onActivityResult function
	 * @param resultCode
	 * @param data
	 * @param requestHandler
	 */
	public void handleGoogleAuthorizeResult(int resultCode, Intent data) 
	{
		if (data == null) {
			mRequestHandler.onLoginFailed(new Exception("Unknown error after handling google auth result"));
			return;
		}
		if (resultCode == Activity.RESULT_OK) {
			//Retry asking for the token
			OKLog.v("GoogleLogin: retrying login after receiving result_OK");
			getAccountAuthTokenInBackground(mRequestHandler);
			return;
		}
		if (resultCode == Activity.RESULT_CANCELED) {
			OKLog.v("GoogleLogin: User cancelled Google login process");
			mRequestHandler.onUserCancelled();
			return;
		}

		mRequestHandler.onLoginFailed(new Exception("Unknown error after handling google auth result"));
		return;
	}

	private void getAccountAuthTokenInBackground(final GoogleAuthRequestHandler requestHandler)
	{
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				getAccountAuthTokenBlocking(requestHandler);
				return null;
			}

			@Override
			protected void onPostExecute(Void result){
				OKLog.v("Finished background async task getAccountAuthTokenBlocking");
			}
		};

		task.execute();
	}

	private void getAccountAuthTokenBlocking(GoogleAuthRequestHandler requestHandler)
	{
		try {
			String authToken = GoogleAuthUtil.getToken(mFragment.getActivity(), mAccount.name, SCOPE);
			//mAuthToken = authToken;
			OKLog.v( "GoogleLogin: got auth token");
			requestHandler.onReceivedAuthToken(authToken);
		} catch (GooglePlayServicesAvailabilityException playEx) {
			// GooglePlayServices.apk is either old, disabled, or not present.
			// GoogleUtils.showGooglePlayServicesErrorDialog(MainActivity.this, playEx.getConnectionStatusCode());
			requestHandler.onLoginFailedWithPlayException(playEx);
		} catch (UserRecoverableAuthException userException) {
			// Unable to authenticate, but the user can fix this.
			// Forward the user to the appropriate activity.
			OKLog.v("GoogleLogin: user recoverable exception: " + userException);
			mFragment.startActivityForResult(userException.getIntent(), REQUEST_CODE_RECOVER_FROM_AUTH_ERROR);
		} catch (GoogleAuthException e) {
			requestHandler.onLoginFailed(e);
		} catch (IOException e) {	
			requestHandler.onLoginFailed(e);
		} 
	}



}
