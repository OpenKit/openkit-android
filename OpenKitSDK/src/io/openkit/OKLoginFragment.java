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


import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;

import io.openkit.facebookutils.*;
import io.openkit.facebookutils.FacebookUtilities.CreateOKUserRequestHandler;
import com.facebook.*;
import io.openkit.user.*;
import android.support.v4.app.DialogFragment;
import android.accounts.Account;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;



public class OKLoginFragment extends DialogFragment
{

	private static String loginText;
	private static int loginTextResourceID;

	private Button fbLoginButton, googleLoginButton, twitterLoginButton,guestLoginButton, dontLoginButton;
	private ProgressBar spinner;
	private TextView loginTextView;
	private boolean fbLoginButtonClicked = false;

	private static boolean fbLoginEnabled = true;
	private static boolean googleLoginEnabled = true;
	private static boolean twitterLoginEnabled = false;
	private static boolean guestLoginEnabled = false;
	
	private boolean isShowingSpinner = false;
	
	private OKLoginFragmentDelegate dialogDelegate;

	private GoogleAuthRequest mGoogAuthRequest;

	public static void setFbLoginEnabled(boolean enabled) {
		fbLoginEnabled = enabled;
	}

	public static void setGoogleLoginEnabled(boolean enabled) {
		googleLoginEnabled = enabled;
	}

	public static void setLoginText(String text){
		loginText = text;
	}

	public static void setLoginTextResourceID(int id){
		loginTextResourceID = id;
	}
	
	public void setDelegate(OKLoginFragmentDelegate delegate) {
		dialogDelegate = delegate;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		setCancelable(false);
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getDialog().setTitle("Login");

		int viewID, fbLoginButtonId, dontLoginButtonId, spinnerId, loginTextViewID, googleLoginButtonID, twitterLoginButtonID, guestLoginButtonID;

		viewID = getResources().getIdentifier("io_openkit_fragment_logindialog", "layout", getActivity().getPackageName());
		fbLoginButtonId = getResources().getIdentifier("io_openkit_fbSignInButton", "id", getActivity().getPackageName());
		dontLoginButtonId = getResources().getIdentifier("io_openkit_dontSignInButton", "id", getActivity().getPackageName());
		spinnerId = getResources().getIdentifier("io_openkit_spinner", "id", getActivity().getPackageName());
		loginTextViewID = getResources().getIdentifier("io_openkit_loginTitleTextView", "id", getActivity().getPackageName());
		googleLoginButtonID = getResources().getIdentifier("io_openkit_googleSignInButton", "id", getActivity().getPackageName());
		twitterLoginButtonID = getResources().getIdentifier("io_openkit_twitterSignInButton", "id", getActivity().getPackageName());
		guestLoginButtonID = getResources().getIdentifier("io_openkit_guestSignInButton", "id", getActivity().getPackageName());


		View view = inflater.inflate(viewID, container, false);
		fbLoginButton = (Button)view.findViewById(fbLoginButtonId);
		dontLoginButton = (Button)view.findViewById(dontLoginButtonId);
		spinner = (ProgressBar)view.findViewById(spinnerId);
		loginTextView = (TextView)view.findViewById(loginTextViewID);
		googleLoginButton = (Button)view.findViewById(googleLoginButtonID);
		twitterLoginButton = (Button)view.findViewById(twitterLoginButtonID);
		guestLoginButton = (Button)view.findViewById(guestLoginButtonID);

		//Show customizable string if set
		if(loginText != null) {
			loginTextView.setText(loginText);
		} else if (loginTextResourceID != 0) {
			loginTextView.setText(loginTextResourceID);
		}

		//Only show the correct buttons
		fbLoginButton.setVisibility(getButtonLayoutVisibility(fbLoginEnabled, fbLoginButton));
		googleLoginButton.setVisibility(getButtonLayoutVisibility(googleLoginEnabled, googleLoginButton));
		guestLoginButton.setVisibility(getButtonLayoutVisibility(guestLoginEnabled, guestLoginButton));
		twitterLoginButton.setVisibility(getButtonLayoutVisibility(twitterLoginEnabled, twitterLoginButton));

		if(isShowingSpinner){
			showSpinner();
		}

		fbLoginButton.setOnClickListener(fbLoginButtonClick);
		googleLoginButton.setOnClickListener(googleLoginButtonClick);
		dontLoginButton.setOnClickListener(dismissSignInClick);		

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity(), null, sessionStatusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(getActivity());
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(sessionStatusCallback));
			}
		}


		return view;
	}


	private View.OnClickListener googleLoginButtonClick = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {

			//Check to see if there are any Google accounts defined
			Account[] googAccounts = GoogleUtils.getGoogleAccounts(OKLoginFragment.this.getActivity());

			if(googAccounts.length == 0) {
				showLoginErrorMessageFromStringIdentifierName("io_openkit_googleNoAccts");
			}
			else if(googAccounts.length == 1) {
				performGoogleAuth(googAccounts[0]);
			}
			else {
				//There are multiple accounts, show a selector to choose which Account to perform Auth on 
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			    builder.setTitle("Choose an Account");
			    builder.setItems(GoogleUtils.getGoogleAccountNames(OKLoginFragment.this.getActivity()), new OnClickListener() {	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Account account = GoogleUtils.getGoogleAccounts(OKLoginFragment.this.getActivity())[which];
						performGoogleAuth(account);
					}
				});
			         
			    builder.create().show();
			}

		}
	};

	private void performGoogleAuth(Account account)
	{
		mGoogAuthRequest = new GoogleAuthRequest(OKLoginFragment.this,account);

		showSpinner();

		mGoogAuthRequest.loginWithGoogleAccount(new GoogleAuthRequestHandler() {
			@Override
			public void onUserCancelled() {
				hideSpinner();
				dialogDelegate.onLoginCancelled();

			}

			@Override
			public void onReceivedAuthToken(final String authToken) {
				// Create the user from the UI thread because onReceivedAuthToken is called from a background thread
				// and all OK network requests are performed on a background thread anyways
				OKLoginFragment.this.getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						GoogleUtils.createOKUserFromGoogle(OKLoginFragment.this.getActivity(), authToken, new CreateOKUserRequestHandler() {
							@Override
							public void onSuccess(OKUser user) {
								OKLog.v("Correct callback is called");
								hideSpinner();
								OKLog.v("Created OKUser successfully!");
								OKManager.INSTANCE.handlerUserLoggedIn(user, OKLoginFragment.this.getActivity());
								dialogDelegate.onLoginSucceeded();
							}

							@Override
							public void onFail(Error error) {
								hideSpinner();
								int errorMessageId = OKLoginFragment.this.getResources().getIdentifier("io_openkit_OKLoginError", "string", OKLoginFragment.this.getActivity().getPackageName());
								String message = OKLoginFragment.this.getString(errorMessageId);
								showLoginErrorMessage(message);
							}
						});
					}
				});
			}

			@Override
			public void onLoginFailedWithPlayException(
					final GooglePlayServicesAvailabilityException playEx) 
			{
				//Need to run this on UIthread becuase this may be called from a background thread and this shows an error dialog
				OKLoginFragment.this.getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						hideSpinner();

						int errorMessageId = OKLoginFragment.this.getResources().getIdentifier("io_openkit_googlePlayServicesError", "string", OKLoginFragment.this.getActivity().getPackageName());
						String message = OKLoginFragment.this.getString(errorMessageId);

						showLoginErrorMessage(message);

						//Can't use helper method below to show error message because we don't include the resources from Google play services SDK
						//GoogleUtils.showGooglePlayServicesErrorDialog(OKLoginFragment.this.getActivity(), playEx.getConnectionStatusCode());
					}
				});

			}

			@Override
			public void onLoginFailed(final Exception e) {
				OKLoginFragment.this.getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						OKLog.d("Google auth login failed with exception: " + e);
						hideSpinner();
						int errorMessageId = OKLoginFragment.this.getResources().getIdentifier("io_openkit_googleLoginError", "string", OKLoginFragment.this.getActivity().getPackageName());
						String message = OKLoginFragment.this.getString(errorMessageId);
						showLoginErrorMessage(message);	
					}
				});
			}
		});
	}

	private View.OnClickListener fbLoginButtonClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			OKLog.v("Pressed FB login button");
			fbLoginButtonClicked = true;
			loginToFB();
		}
	};

	private View.OnClickListener dismissSignInClick = new View.OnClickListener() 
	{
		@Override
		public void onClick(View v) {
			dialogDelegate.onLoginCancelled();
		}
	};

	private int getButtonLayoutVisibility(boolean enabled, Button button)
	{
		if(enabled) {
			return button.getVisibility();
		} else {
			return View.GONE;
		}
	}

	// If the button is Visible, set it to Invisible (e.g. do not set View.Gone to View.Invisible)
	private void setButtonInvisibleIfNotGone(Button button) {
		if(button.getVisibility() == View.VISIBLE) {
			button.setVisibility(View.INVISIBLE);
		}
	}

	// If the button is Invisible, set it it Visible (e.g. do not set View.gone to View.Visible)
	private void setButtonVisibleIfNotGone(Button button) {
		if(button.getVisibility() == View.INVISIBLE) {
			button.setVisibility(View.INVISIBLE);
		}
	}

	private void showSpinner()
	{
		isShowingSpinner = true;
		spinner.setVisibility(View.VISIBLE);
		setButtonInvisibleIfNotGone(fbLoginButton);
		setButtonInvisibleIfNotGone(twitterLoginButton);
		setButtonInvisibleIfNotGone(googleLoginButton);
		setButtonInvisibleIfNotGone(guestLoginButton);
		setButtonInvisibleIfNotGone(dontLoginButton);
	}

	private void hideSpinner()
	{
		isShowingSpinner = false;
		spinner.setVisibility(View.INVISIBLE);
		setButtonVisibleIfNotGone(fbLoginButton);
		setButtonVisibleIfNotGone(twitterLoginButton);
		setButtonVisibleIfNotGone(googleLoginButton);
		setButtonVisibleIfNotGone(guestLoginButton);
		setButtonVisibleIfNotGone(dontLoginButton);
	}

	/**
	 * Starts the Facebook authentication process. Performs Facebook authentication using the best method available 
	 * (native Android dialog, single sign on through Facebook application, or using a web view shown inside the app)
	 */
	private void loginToFB()
	{
		showSpinner();

		Session session = Session.getActiveSession();

		if(!session.isOpened() && !session.isClosed()){
			session.openForRead(new Session.OpenRequest(this)
			//.setPermissions(Arrays.asList("basic_info"))
			.setCallback(sessionStatusCallback));
		}
		else if(session.isOpened())
		{
			//Facebook session is already open, just authorize the user with OpenKit
			authorizeFBUserWithOpenKit();
		}
		else {
			Session.openActiveSession(getActivity(), this, true, sessionStatusCallback);
		}
	}

	/**
	 * Called after the user is authenticated with Facebook. Uses the the Facebook authentication token to look up
	 * the user's facebook id, then gets the corresponding OKUser to this facebook ID.
	 */
	private void authorizeFBUserWithOpenKit()
	{
		FacebookUtilities.CreateOKUserFromFacebook(new CreateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				hideSpinner();
				OKLog.v("Created OKUser successfully!");
				OKManager.INSTANCE.handlerUserLoggedIn(user, OKLoginFragment.this.getActivity());
				dialogDelegate.onLoginSucceeded();
			}

			@Override
			public void onFail(Error error) {
				hideSpinner();
				OKLog.v("Failed to create OKUSER: " + error);

				showLoginErrorMessage("Sorry, but there was an error reaching the OpenKit server. Please try again later");
			}
		});
	}
	

	private void showLoginErrorMessageFromStringIdentifierName(String id)
	{
		int errorMessageId = OKLoginFragment.this.getResources().getIdentifier(id, "string", OKLoginFragment.this.getActivity().getPackageName());
		String message = OKLoginFragment.this.getString(errorMessageId);
		showLoginErrorMessage(message);
	}
	
	private void showLoginErrorMessage(String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(OKLoginFragment.this.getActivity());
		builder.setTitle("Error");
		builder.setMessage(message);
		builder.setNegativeButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogDelegate.onLoginFailed();
			}
		});
		builder.setCancelable(false);

		// create alert dialog
		AlertDialog alertDialog = builder.create();

		// show it
		alertDialog.show();
	}

	static String keyhashErrorString = "remote_app_id does not match stored id ";

	/**
	 * Handler function for when Facebook login fails
	 * @param exception Exception from Facebook SDK
	 */
	private void facebookLoginFailed(Exception exception)
	{
		OKLog.v("Facebook login failed");

		if(exception != null && exception.getClass() == com.facebook.FacebookOperationCanceledException.class)
		{
			OKLog.v("User cancelled Facebook login");

			//Special check for the keyhash issue, otherwise just dismiss because the user cancelled
			if(exception.getMessage().equalsIgnoreCase(keyhashErrorString))
			{
				showLoginErrorMessage("There was an error logging in with Facebook. Your Facebook application may not be configured correctly. Make sure you have added the correct Android keyhash(es) to your Facebook application");
				return;
			} else {
				dialogDelegate.onLoginCancelled();				
			}
		}
		else {
			showLoginErrorMessage("There was an unknown error while logging into Facebook. Please try again");
		}
	}

	/* Facebook session state change handler. This method handles all cases of Facebook auth */
	private Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {

			// Log all facebook exceptions
			if(exception != null)
			{
				OKLog.v("SessionState changed exception: " + exception + " hash code: " + exception.hashCode());
			}

			//Log what is happening with the Facebook session for debug help
			switch (state) {
			case OPENING:
				OKLog.v("SessionState Opening");
				break;
			case CREATED:
				OKLog.v("SessionState Created");
				break;
			case OPENED:
				OKLog.v("SessionState Opened");
				break;
			case CLOSED_LOGIN_FAILED:
				OKLog.v("SessionState Closed Login Failed");
				break;
			case OPENED_TOKEN_UPDATED:
				OKLog.v("SessionState Opened Token Updated");
				break;
			case CREATED_TOKEN_LOADED:
				OKLog.v("SessionState created token loaded" );
				break;
			case CLOSED:
				OKLog.v("SessionState closed");
				break;
			default:
				OKLog.v("Session State Default case");
				break;
			}

			// If the session is opened, authorize the user, if the session is closed 
			if (state.isOpened()) 
			{
				OKLog.v("FB Session is Open");
				if(fbLoginButtonClicked){
					OKLog.v("Authorizing user with Facebook");
					authorizeFBUserWithOpenKit();
					fbLoginButtonClicked = false;
				}
			} else if (state.isClosed()) {
				OKLog.v("FB Session is Closed");
				if(fbLoginButtonClicked) {
					facebookLoginFailed(exception);
					fbLoginButtonClicked = false;
				}
			}
		}
	};

	/* Below methods are overridden to add the Facebook session lifecycle callbacks */

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(sessionStatusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(sessionStatusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		// Handle activity result for Google auth
		if (requestCode == GoogleAuthRequest.REQUEST_CODE_RECOVER_FROM_AUTH_ERROR) {
			if(mGoogAuthRequest != null) {
				mGoogAuthRequest.handleGoogleAuthorizeResult(resultCode, data);
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
		// Handle activity result for Facebook auth
		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}



}
