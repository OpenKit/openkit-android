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

import io.openkit.OKLog;
import io.openkit.okcloud.OKCloud;
import io.openkit.okcloud.OKCloudException;
import io.openkit.okcloud.OKCloudHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;


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
					showToast("Successfully stored string: " + stringToStore);
				} else {
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
					showToast("Got the string: " + obj.toString());
				} else {
					showToast("Error getting string: " + e.getMessage());
				}
			}
		});
	}

	public void storeArray(View view) {
		JSONArray arr = new JSONArray();
		arr.put("hello");
		arr.put(-99);
		OKCloud.set(arr, "myKey2", new OKCloudHandler() {

			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					showToast("Succesfully stored JSONArray");
				} else {
					showToast("Error storing JSONArray");
				}
			}
		});
	}

	public void getArray(View view) {
		OKCloud.get("myKey2", new OKCloudHandler() {
			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					Object element0, element1;
					JSONArray arr = (JSONArray)obj;
					try {
						element0 = arr.get(0);
						element1 = arr.get(1);
					} catch (JSONException e1) {
						OKLog.d("Could not get elements of returned array!");
						return;
					}
					OKLog.d("Element 0 value: %s", element0);
					OKLog.d("Element 1 value: %d", element1);
					showToast("Successfully got JSONArray with " + arr.length() + " pairs");
				} else {
					OKLog.d("Error getting hashmap: %s", e.getMessage());
					showToast("Error getting hashmap: " + e.getMessage());
				}
			}
		});

	}

	public void storeJSON(View view) {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put("one");
		arr.put("two");
		arr.put(3);

		try {
			obj.put("property1", "hello");
			obj.put("property2", arr);
		} catch (JSONException e1) {
			OKLog.d("Could not add to sample JSON object");
		}

		OKCloud.set(obj, "myKey3", new OKCloudHandler() {
			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					OKLog.d("Successfully stored JSONObject.");
					showToast("Succesfully stored JSONObject");
				} else {
					OKLog.d("Error storing JSONObject: %s", e.getMessage());
					showToast("Error storing JSONObject");
				}
			}
		});
	}

	public void getJSON(View view) {
		OKCloud.get("myKey3", new OKCloudHandler() {
			@Override
			public void complete(Object obj, OKCloudException e) {
				if (e == null) {
					JSONObject jsonObject = (JSONObject)obj;

					// See format of properties in 'storeJSON' method above.
					String property1;
					JSONArray property2;
					try {
						property1 = (String)jsonObject.get("property1");
						property2 = (JSONArray)jsonObject.get("property2");
					} catch (JSONException e1) {
						OKLog.d("Could not get elements of returned json!");
						return;
					}

					OKLog.d("Property 1 value: %s", property1);
					OKLog.d("Property 2: value: %s", property2);
					showToast("Successfully got JSONObject with " + jsonObject.length() + " properties");
				} else {
					OKLog.d("Error getting JSONObject: %s", e.getMessage());
					showToast("Error getting JSONObject: " + e.getMessage());
				}
			}
		});
	}


}
