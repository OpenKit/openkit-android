package io.openkit.facebookutils;

import io.openkit.OKLog;
import com.facebook.Session;
import com.facebook.SessionState;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class FBLoginRequest {

	public FBLoginRequest()
	{
		super();
	}

	private FBLoginRequestHandler requestHandler;

	public FBLoginRequestHandler getRequestHandler() {
		return requestHandler;
	}

	public void setRequestHandler(FBLoginRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	public void openCachedFBSession(Context ctx)
	{
		Session cachedSession = Session.openActiveSessionFromCache(ctx);

		if(cachedSession != null) {
			Session.setActiveSession(cachedSession);
			OKLog.v("Opened cached FB Session");
		} else {
			OKLog.v("Did not find cached FB session");
		}
	}

	public void onCreateView(Bundle savedInstanceState, Fragment fragment)
	{
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(fragment.getActivity(), null, sessionStatusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(fragment.getActivity());
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(fragment).setCallback(sessionStatusCallback));
			}
		}
	}

	public void loginToFacebook(Fragment fragment)
	{
		Session session = Session.getActiveSession();

		if(!session.isOpened() && !session.isClosed()){
			session.openForRead(new Session.OpenRequest(fragment)
			//.setPermissions(Arrays.asList("basic_info"))
			.setCallback(sessionStatusCallback));
		}
		else if(session.isOpened())
		{
			//Facebook session is already open, just authorize the user with OpenKit
			requestHandler.onFBLoginSucceeded();
		}
		else {
			Session.openActiveSession(fragment.getActivity(), fragment, true, sessionStatusCallback);
		}
	}

	/* Facebook session state change handler. This method handles all cases of Facebook auth */
	private Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {

			// Log all facebook exceptions
			if(exception != null){
				OKLog.v("SessionState changed exception: " + exception + " hash code: " + exception.hashCode());
			}

			FacebookUtilities.logSessionState(state);

			// If the session is opened, authorize the user, if the session is closed then display an error message if necessary
			if (state.isOpened())
			{
				OKLog.v("FB Session is Open");
				if(requestHandler != null) {
					requestHandler.onFBLoginSucceeded();
				}

			} else if (state.isClosed()) {
				OKLog.v("FB Session is Closed");
				if(exception != null) {
					String errorMessage = FacebookUtilities.ShouldShowFacebookError(exception);
					if(requestHandler != null) {
						requestHandler.onFBLoginError(errorMessage);
					}
				}
			}
		}
	};

	public void onStart() {
		Session.getActiveSession().addCallback(sessionStatusCallback);
	}

	public void onStop() {
		Session.getActiveSession().removeCallback(sessionStatusCallback);
	}

	public void onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data)
	{
		// Handle activity result for Facebook auth
		Session.getActiveSession().onActivityResult(fragment.getActivity(), requestCode, resultCode, data);
	}

	public void onSaveInstanceState(Bundle outState) {
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}







}
