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

import io.openkit.user.OKLoginFragmentResponseHandler;
import io.openkit.user.OKLoginUpdateNickFragmentHandler;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;



public class OKLoginActivity extends FragmentActivity
{
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
        loginDialog.show(fm, TAG_LOGINFRAGMENT, new OKLoginFragmentResponseHandler() {
			
        	@Override
			public void onLoginSucceeded() {
				loginDialog.dismiss();
				OKLog.v("Successfully logged in, now showing nick update view");
				showUserNickUpdateFragment();
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
		});
	}
	
	private void showUserNickUpdateFragment()
	{
		FragmentManager fm = getSupportFragmentManager();
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

