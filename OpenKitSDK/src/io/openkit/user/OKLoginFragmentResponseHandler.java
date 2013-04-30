package io.openkit.user;

public abstract class OKLoginFragmentResponseHandler
{
	public abstract void onLoginFailed();
	public abstract void onLoginSucceeded();
	public abstract void onLoginCancelled();
}
