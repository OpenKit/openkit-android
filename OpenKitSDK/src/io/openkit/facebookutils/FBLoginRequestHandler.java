package io.openkit.facebookutils;

public interface FBLoginRequestHandler
{
	void onFBLoginSucceeded();
	void onFBLoginCancelled();
	void onFBLoginError(String errorMessage);
}
