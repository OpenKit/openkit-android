package io.openkit.user;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;

public abstract class GoogleAuthRequestHandler {
	public abstract void onUserCancelled();
	public abstract void onReceivedAuthToken(String authToken);
	public abstract void onLoginFailed(Exception e);
	public abstract void onLoginFailedWithPlayException(GooglePlayServicesAvailabilityException playEx);
	
}