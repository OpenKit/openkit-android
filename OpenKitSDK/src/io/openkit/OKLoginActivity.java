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
	private static OKLoginActivityHandler activityHandler;

	public static OKLoginActivityHandler getActivityHandler() {
		return activityHandler;
	}

	public static void setActivityHandler(OKLoginActivityHandler activityHandler) {
		OKLoginActivity.activityHandler = activityHandler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//wrActivity = new WeakReference<OKLoginActivity>(this);

		if(loginDialog != null) {
			loginDialog.setDelegate(this);

			if(activityHandler != null) {
				loginDialog.setActivityHandler(activityHandler);
			}
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
		callLoginActivityHandler();
		OKLog.v("Successfully logged in, dismissing LoginActivity");
		OKLoginActivity.this.finish();
	}

	@Override
	public void onLoginFailed() {
		loginDialog.dismiss();
		callLoginActivityHandler();
		OKLog.v("Login failed, dismissing login activity");
		OKLoginActivity.this.finish();
	}

	@Override
	public void onLoginCancelled() {
		loginDialog.dismiss();
		callLoginActivityHandler();
		OKLog.v("Login canceled by user, dismissing login activity");
		OKLoginActivity.this.finish();
	}

	private void callLoginActivityHandler()
	{
		if(loginDialog.getActivityHandler() != null) {
			loginDialog.getActivityHandler().onLoginDialogComplete();
			loginDialog.setActivityHandler(null);
			OKLoginActivity.setActivityHandler(null);
		}
	}

	private void showLoginFragment()
	{
		FragmentManager fm = getSupportFragmentManager();
		loginDialog = new OKLoginFragment();
		loginDialog.setDelegate(this);

		if(activityHandler != null) {
			loginDialog.setActivityHandler(activityHandler);
		}

		loginDialog.show(fm, TAG_LOGINFRAGMENT);
	}


}

