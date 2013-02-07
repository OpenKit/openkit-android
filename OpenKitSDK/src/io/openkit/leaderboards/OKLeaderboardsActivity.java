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

package io.openkit.leaderboards;

import io.openkit.*;
import io.openkit.user.OKUserProfileActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;

public class OKLeaderboardsActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.OKActivityTheme);
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.io_openkit_title_leaderboards);
		
		if(savedInstanceState == null) {
			OKLeaderboardsFragment fragment = new OKLeaderboardsFragment();
			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, fragment);
			ft.commit();
		}
	}
	
	private void showProfileActivity()
	{
		//If the user is logged in, show the profile view, else show the login view
		if(OKUser.getCurrentUser() != null)
		{
			Intent showProfile = new Intent(OKLeaderboardsActivity.this, OKUserProfileActivity.class);
			startActivity(showProfile);
		}
		else
		{
			Intent loginIntent = new Intent(OKLeaderboardsActivity.this, OKLoginActivity.class);
			startActivity(loginIntent);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{	
		///Note: since this is a library project, this has to be an IF/ELSE
		// statement instead of a switch because Library projects can't use the R.id.NAME
		// in a switch statement
		
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		} else if (item.getItemId() == R.id.io_openkit_menu_profileButton) {
			showProfileActivity();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.io_openkit_leaderboards, menu);
	    return true;
	}

}