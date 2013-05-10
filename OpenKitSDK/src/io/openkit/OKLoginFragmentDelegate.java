package io.openkit;

public interface OKLoginFragmentDelegate {
	
	public void onLoginSucceeded();
	public void onLoginFailed();
	public void onLoginCancelled();
	
}
