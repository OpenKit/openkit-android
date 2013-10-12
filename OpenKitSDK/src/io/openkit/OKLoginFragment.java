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
import io.openkit.user.*;
import android.support.v4.app.DialogFragment;
import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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

	private static boolean fbLoginEnabled = true;
	private static boolean googleLoginEnabled = true;
	private static boolean twitterLoginEnabled = false;
	private static boolean guestLoginEnabled = false;
	private boolean isShowingSpinner = false;

	private FBLoginRequest fbLoginRequest;
	private GoogleAuthRequest mGoogAuthRequest;

	private OKLoginFragmentDelegate dialogDelegate;
	private OKLoginActivityHandler activityHandler;

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
		fbLoginRequest = new FBLoginRequest();
		OKLog.v("OKLoginFragment oncreate");
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		setCancelable(false);
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setOnKeyListener(backButtonListener);
		return dialog;
	}

	// If the back button is pressed, close the dialog
	public DialogInterface.OnKeyListener backButtonListener = new DialogInterface.OnKeyListener() {

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				dialogDelegate.onLoginCancelled();
			}
			return false;
		}
	};

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}







	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getDialog().setTitle("Login");

		fbLoginRequest.onCreateView(savedInstanceState, this);

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
						GoogleUtils.createOrUpdateOKUserFromGoogle(OKLoginFragment.this.getActivity(), authToken, new CreateOrUpdateOKUserRequestHandler() {
							@Override
							public void onSuccess(OKUser user) {
								hideSpinner();
								OKLog.v("Created OKUser successfully!");
								OKManager.INSTANCE.handlerUserLoggedIn(user, OKLoginFragment.this.getActivity());
								dialogDelegate.onLoginSucceeded();
							}

							@Override
							public void onFail(Throwable error) {
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
		if(button.getVisibility() != View.GONE) {
			button.setVisibility(View.INVISIBLE);
		}
	}

	// If the button is Invisible, set it it Visible (e.g. do not set View.gone to View.Visible)
	private void setButtonVisibleIfNotGone(Button button) {
		if(button.getVisibility() != View.GONE) {
			button.setVisibility(View.VISIBLE);
		}
	}

	private void showSpinner()
	{
		isShowingSpinner = true;
		spinner.setVisibility(View.VISIBLE);
		dontLoginButton.setVisibility(View.INVISIBLE);

		setButtonInvisibleIfNotGone(fbLoginButton);
		setButtonInvisibleIfNotGone(twitterLoginButton);
		setButtonInvisibleIfNotGone(googleLoginButton);
		setButtonInvisibleIfNotGone(guestLoginButton);
	}

	private void hideSpinner()
	{
		isShowingSpinner = false;
		spinner.setVisibility(View.INVISIBLE);
		dontLoginButton.setVisibility(View.VISIBLE);

		setButtonVisibleIfNotGone(fbLoginButton);
		setButtonVisibleIfNotGone(twitterLoginButton);
		setButtonVisibleIfNotGone(googleLoginButton);
		setButtonVisibleIfNotGone(guestLoginButton);
	}

	/**
	 * Starts the Facebook authentication process. Performs Facebook authentication using the best method available
	 * (native Android dialog, single sign on through Facebook application, or using a web view shown inside the app)
	 */
	private void loginToFB()
	{
		FBLoginRequestHandler loginRequestHandler = new FBLoginRequestHandler() {

			@Override
			public void onFBLoginSucceeded() {
				authorizeFBUserWithOpenKit();
			}

			@Override
			public void onFBLoginError(String errorMessage) {
				OKLog.v("Fb login failed in the callback");
				hideSpinner();
				if(errorMessage != null) {
					FacebookUtilities.showErrorMessage(errorMessage, OKLoginFragment.this.getActivity());
				}
			}

			@Override
			public void onFBLoginCancelled() {
				OKLog.v("FB Login cancelled");
				hideSpinner();
			}
		};

		fbLoginRequest.setRequestHandler(loginRequestHandler);
		showSpinner();
		fbLoginRequest.loginToFacebook(this);
	}

	/**
	 * Called after the user is authenticated with Facebook. Uses the the Facebook authentication token to look up
	 * the user's facebook id, then gets the corresponding OKUser to this facebook ID.
	 */
	private void authorizeFBUserWithOpenKit()
	{
		FacebookUtilities.CreateOrUpdateOKUserFromFacebook(new CreateOrUpdateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				hideSpinner();
				OKLog.v("Got OKUser successfully!");
				OKManager.INSTANCE.handlerUserLoggedIn(user, OKLoginFragment.this.getActivity());
				dialogDelegate.onLoginSucceeded();
			}

			@Override
			public void onFail(Throwable error) {
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


	/* Below methods are overridden to add the Facebook session and Google auth lifecycle callbacks */

	@Override
	public void onStart() {
		super.onStart();
		fbLoginRequest.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		fbLoginRequest.onStop();
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

		fbLoginRequest.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		fbLoginRequest.onSaveInstanceState(outState);
	}

	public OKLoginActivityHandler getActivityHandler() {
		return activityHandler;
	}

	public void setActivityHandler(OKLoginActivityHandler activityHandler) {
		this.activityHandler = activityHandler;
	}
}
