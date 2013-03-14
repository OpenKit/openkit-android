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
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;



public class OKLoginFragment extends DialogFragment
{
	
	private Button fbLoginButton;
	private Button dontLoginButton;
	private ProgressBar spinner;
	
	private boolean loginButtonClicked = false;
	
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
		
		int viewID, fbLoginButtonId, dontLoginButtonId, spinnerId;
		
		viewID = getResources().getIdentifier("io_openkit_fragment_logindialog", "layout", getActivity().getPackageName());
		fbLoginButtonId = getResources().getIdentifier("io_openkit_fbSignInButton", "id", getActivity().getPackageName());
		dontLoginButtonId = getResources().getIdentifier("io_openkit_dontSignInButton", "id", getActivity().getPackageName());
		spinnerId = getResources().getIdentifier("io_openkit_spinner", "id", getActivity().getPackageName());
		
		/*
		View view = inflater.inflate(R.layout.io_openkit_fragment_logindialog, container, false);
		fbLoginButton = (Button)view.findViewById(R.id.io_openkit_fbSignInButton);
		dontLoginButton = (Button)view.findViewById(R.id.io_openkit_dontSignInButton);
		spinner = (ProgressBar)view.findViewById(R.id.io_openkit_spinner);
		*/
		View view = inflater.inflate(viewID, container, false);
		fbLoginButton = (Button)view.findViewById(fbLoginButtonId);
		dontLoginButton = (Button)view.findViewById(dontLoginButtonId);
		spinner = (ProgressBar)view.findViewById(spinnerId);
		
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
	
	private OnClickListener loginButtonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			OKLog.v("Pressed FB login button");
			loginButtonClicked = true;
			loginToFB();
		}
	};
	
	private OnClickListener dismissSignInClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			OKLoginDialogListener delegate = (OKLoginDialogListener)OKLoginFragment.this.getActivity();
			delegate.dismissSignin();
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
	
	private void loginToFB()
	{
		showSpinner();
		
		Session session = Session.getActiveSession();
		
		if(!session.isOpened() && !session.isClosed()){
			session.openForRead(new Session.OpenRequest(this).setCallback(sessionStatusCallback));
		}
		else {
			Session.openActiveSession(getActivity(), this, true, sessionStatusCallback);
		}
	}
	
	private void authorizeFBUserWithOpenKit()
	{
		loginButtonClicked = false;
		FacebookUtilities.AuthorizeUserWithFacebook(new CreateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				hideSpinner();
				OKLog.v("Created OKUser successfully!");
				OpenKitSingleton.INSTANCE.handlerUserLoggedIn(user, OKLoginFragment.this.getActivity());
				
				OKLoginDialogListener delegate = (OKLoginDialogListener)OKLoginFragment.this.getActivity();
				delegate.loginSuccessful(user);
			}

			@Override
			public void onFail(Error error) {
				hideSpinner();
				OKLog.v("Failed to create OKUSER: " + error);
				
				OKLoginDialogListener delegate = (OKLoginDialogListener)OKLoginFragment.this.getActivity();
				delegate.loginFailed();
			}
		});
	}
	
	
	private Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {
		
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			switch (state) {
			case OPENING:
				OKLog.v("SessionState Opening");
				break;
			case CREATED:
				OKLog.v("SessionState Created");
				break;
			case OPENED:
				OKLog.v("SessionState Opened");				
				if(loginButtonClicked){
					authorizeFBUserWithOpenKit();
				}
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
		}
	};
	
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
