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


import io.openkit.facebookutils.*;
import io.openkit.facebookutils.FacebookUtilities.CreateOKUserRequestHandler;

import io.openkit.facebook.*;
import io.openkit.user.*;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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
	
	private Button fbLoginButton;
	private Button dontLoginButton;
	private ProgressBar spinner;
	private TextView loginTextView;
	
	
	private static OKLoginFragmentResponseHandler responseHandler;
	
	private boolean fbLoginButtonClicked = false;
	
	public static void setLoginText(String text)
	{
		loginText = text;
	}
	
	public static void setLoginTextResourceID(int id)
	{
		loginTextResourceID = id;
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
	
	public void show(FragmentManager manager, String tag, OKLoginFragmentResponseHandler handler)
	{
		responseHandler = handler;
		show(manager, tag);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getDialog().setTitle("Login");
		
		int viewID, fbLoginButtonId, dontLoginButtonId, spinnerId, loginTextViewID;
		
		viewID = getResources().getIdentifier("io_openkit_fragment_logindialog", "layout", getActivity().getPackageName());
		fbLoginButtonId = getResources().getIdentifier("io_openkit_fbSignInButton", "id", getActivity().getPackageName());
		dontLoginButtonId = getResources().getIdentifier("io_openkit_dontSignInButton", "id", getActivity().getPackageName());
		spinnerId = getResources().getIdentifier("io_openkit_spinner", "id", getActivity().getPackageName());
		loginTextViewID = getResources().getIdentifier("io_openkit_loginTitleTextView", "id", getActivity().getPackageName());
		

		View view = inflater.inflate(viewID, container, false);
		fbLoginButton = (Button)view.findViewById(fbLoginButtonId);
		dontLoginButton = (Button)view.findViewById(dontLoginButtonId);
		spinner = (ProgressBar)view.findViewById(spinnerId);
		loginTextView = (TextView)view.findViewById(loginTextViewID);
		
		//Show customizable string if set
		if(loginText != null) {
			loginTextView.setText(loginText);
		} else if (loginTextResourceID != 0) {
			loginTextView.setText(loginTextResourceID);
		}
		
		fbLoginButton.setOnClickListener(loginButtonClick);
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
	
	private View.OnClickListener loginButtonClick = new View.OnClickListener() {
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
			responseHandler.onLoginCancelled();
		}
	};
	
	
	private void showSpinner()
	{
		spinner.setVisibility(View.VISIBLE);
		fbLoginButton.setVisibility(View.INVISIBLE);
		dontLoginButton.setVisibility(View.INVISIBLE);
	}
	
	private void hideSpinner()
	{
		spinner.setVisibility(View.INVISIBLE);
		fbLoginButton.setVisibility(View.VISIBLE);
		dontLoginButton.setVisibility(View.VISIBLE);
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
		FacebookUtilities.AuthorizeUserWithFacebook(new CreateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				hideSpinner();
				OKLog.v("Created OKUser successfully!");
				OpenKitSingleton.INSTANCE.handlerUserLoggedIn(user, OKLoginFragment.this.getActivity());
				responseHandler.onLoginSucceeded();
			}

			@Override
			public void onFail(Error error) {
				hideSpinner();
				OKLog.v("Failed to create OKUSER: " + error);
				
				showLoginErrorMessage("Sorry, but there was an error reaching the OpenKit server. Please try again later");
			}
		});
	}
	
	private void showLoginErrorMessage(String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(OKLoginFragment.this.getActivity());
 		builder.setTitle("Error");
 		builder.setMessage(message);
 		builder.setNegativeButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				responseHandler.onLoginFailed();
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
		
		if(exception != null && exception.getClass() == io.openkit.facebook.FacebookOperationCanceledException.class)
		{
			OKLog.v("User cancelled Facebook login");
			
			//Special check for the keyhash issue, otherwise just dismiss because the user cancelled
			if(exception.getMessage().equalsIgnoreCase(keyhashErrorString))
			{
				showLoginErrorMessage("There was an error logging in with Facebook. Your Facebook application may not be configured correctly. Make sure you have added the correct Android keyhash(es) to your Facebook application");
				return;
			} else {
				responseHandler.onLoginCancelled();				
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }
	

	
}
