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
package io.openkit.example.oksampleapp;

import io.openkit.*;
import io.openkit.okcloud.*;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedHashMap;


public class OKCloudSampleActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_okcloud_sample);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_okcloud_sample, menu);
		return true;
	}
	
	private void showToast(String toastText)
	{
		Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public void storeString(View view) {
		final String stringToStore = "I'm a string and I'm stored in the cloud!";
		
		OKCloud.set(stringToStore, "myKey1", new OKCloudHandler() {
			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					OKLog.d("Successfully stored string.");
					showToast("Successfully stored string: " + stringToStore);
				} else {
					OKLog.d("Error storing string: %s", e.getMessage());
					showToast("Error storing string: " + e.getMessage());
				}
			}
		});
	}

	public void getString(View view) {
		OKCloud.get("myKey1", new OKCloudHandler(){
			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					OKLog.d("Got the string: %s", obj);
					showToast("Got the string: " + obj.toString());
				} else {
					OKLog.d("Error getting string: %s", e.getMessage());
					showToast("Error getting string: " + e.getMessage());
				}
			}
		});
	}
	
	// See types in "Raw" Data Binding Example
	// http://wiki.fasterxml.com/JacksonInFiveMinutes
	public void storeHashMap(View view) {
		final Object[] arr = { "one", "two", 1 };
		HashMap<Object, Object> obj = new HashMap<Object, Object>();
		obj.put("property1", "foo");
		obj.put("property2", -99);
		obj.put("property3", arr);

		OKCloud.set(obj, "myKey2", new OKCloudHandler() {
			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					OKLog.d("Successfully stored HashMap.");
					showToast("Succesfully stored HashMap");
				} else {
					OKLog.d("Error storing HashMap: %s", e.getMessage());
					showToast("Error storing HashMap");
				}
			}
		});
	}

	public void getHashMap(View view) {
		OKCloud.get("myKey2", new OKCloudHandler() {
			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					LinkedHashMap<?, ?> casted_obj = (LinkedHashMap<?, ?>)obj;
					Object val1 = casted_obj.get("property1");
					Object val2 = casted_obj.get("property2");
					Object val3 = casted_obj.get("property3");
					OKLog.d("Property 1:\n  value: %s\n  class: %s", val1, val1.getClass().getName());
					OKLog.d("Property 2:\n  value: %d\n  class: %s", val2, val2.getClass().getName());
					OKLog.d("Property 3:\n  value: %s\n  class: %s", val3.toString(), val3.getClass().getName());
					showToast("Successfully got HashMap with " + casted_obj.size() + " pairs");
				} else {
					OKLog.d("Error getting hashmap: %s", e.getMessage());
					showToast("Error getting hashmap: " + e.getMessage());
				}
			}
		});
	}


}
