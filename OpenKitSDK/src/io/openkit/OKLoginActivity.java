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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;


public class OKLoginActivity extends FragmentActivity implements OKLoginDialogListener{
	
	private OKLoginFragment loginDialog;
	private OKLoginUpdateNickFragment updateNickDialog;
	
	private static final String TAG_LOGINFRAGMENT = "OKLoginFragment";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(savedInstanceState == null)
        {
        	//Show the login fragment when the activity is first created / launched
        	showLoginFragment();
        }
    }
	
	private void showLoginFragment()
	{
		FragmentManager fm = getSupportFragmentManager();
        loginDialog = new OKLoginFragment(); 
        loginDialog.show(fm, TAG_LOGINFRAGMENT);
	}
	
	private void showUserNickUpdateFragment()
	{
		FragmentManager fm = getSupportFragmentManager();
		updateNickDialog = new OKLoginUpdateNickFragment();
		updateNickDialog.show(fm, "OKLoginUpdateNickFragment");
	}

	@Override
	public void dismissSignin() {
		loginDialog.dismiss();
		this.finish();
	}

	@Override
	public void loginSuccessful(OKUser user) {
		OKLog.v("Successfully logged in, now showing nick update view");
		loginDialog.dismiss();
		
		showUserNickUpdateFragment();
	}

	@Override
	public void loginFailed() {
		OKLog.v("Login failed");
		if(loginDialog != null) {
			loginDialog.dismiss();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Error");
 		builder.setMessage("Sorry, but there was an error reaching the OpenKit server. Please try again later");
 		builder.setNegativeButton("OK",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				OKLoginActivity.this.finish();
			}
		});
 		builder.setCancelable(false);

		// create alert dialog
		AlertDialog alertDialog = builder.create();

		// show it
		alertDialog.show();
	}

	@Override
	public void nickUpdateCompleted() {
		OKLog.v("User nick updating dismissed");
		this.finish();
	}

}

