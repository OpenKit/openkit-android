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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;



public class OKLoginActivity extends FragmentActivity implements OKLoginFragmentDelegate
{
	private OKLoginFragment loginDialog;

	//private static WeakReference<OKLoginActivity> wrActivity = null;

	private static final String TAG_LOGINFRAGMENT = "OKLoginFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//wrActivity = new WeakReference<OKLoginActivity>(this);

		if(loginDialog != null) {
			loginDialog.setDelegate(this);
		}

		if(savedInstanceState == null)
		{
			//Show the login fragment when the activity is first created / launched
			showLoginFragment();
		}
	}

	@Override
	public void onLoginSucceeded()
	{
		loginDialog.dismiss();
		OKLog.v("Successfully logged in, dismissing LoginActivity");
		OKLoginActivity.this.finish();
	}

	@Override
	public void onLoginFailed() {
		loginDialog.dismiss();
		OKLog.v("Login failed, dismissing login activity");
		OKLoginActivity.this.finish();
	}

	@Override
	public void onLoginCancelled() {
		loginDialog.dismiss();
		OKLog.v("Login canceled by user, dismissing login activity");
		OKLoginActivity.this.finish();
	}

	private void showLoginFragment()
	{
		FragmentManager fm = getSupportFragmentManager();
		loginDialog = new OKLoginFragment();
		loginDialog.setDelegate(this);
		loginDialog.show(fm, TAG_LOGINFRAGMENT);
	}

	/*
	private void showUserNickUpdateFragment()
	{
		if((wrActivity.get() != null && (wrActivity.get().isFinishing() != true))) {

			FragmentManager fm = wrActivity.get().getSupportFragmentManager();
			updateNickDialog = new OKLoginUpdateNickFragment();
			updateNickDialog.show(fm, "OKLoginUpdateNickFragment");

			updateNickDialog.setDialogHandler(new OKLoginUpdateNickFragmentHandler() {
				@Override
				public void onDismiss() {
					OKLoginActivity.this.finish();
				}
			});
		}
	}
	*/



}

