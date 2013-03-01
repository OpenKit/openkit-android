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

package io.openkit.user;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class OKUserProfileActivity extends FragmentActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		int themeID = getResources().getIdentifier("OKActivityTheme", "style", getPackageName());
		this.setTheme(themeID);
		//this.setTheme(R.style.OKActivityTheme);
		super.onCreate(savedInstanceState);
		
		int titleID = getResources().getIdentifier("io_openkit_userprofle", "string", getPackageName());
		this.setTitle(titleID);
		
		if(savedInstanceState == null) {
			OKUserProfileFragment fragment = new OKUserProfileFragment();
			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, fragment);
			ft.commit();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{			
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

}
