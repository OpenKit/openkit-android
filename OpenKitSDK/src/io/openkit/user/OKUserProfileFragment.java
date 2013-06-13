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

import io.openkit.*;
import com.facebook.widget.ProfilePictureView;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class OKUserProfileFragment extends Fragment 
{
	private TextView userNickTextView;
	private ProfilePictureView profiePictureView;
	private Button logoutButton;
	private Button changeNickButton;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		int viewID = getResources().getIdentifier("io_openkit_fragment_userprofile", "layout", getActivity().getPackageName());
		View view = inflater.inflate(viewID, container, false);
		//View view = inflater.inflate(R.layout.io_openkit_fragment_userprofile, container, false);
		
		
		int userNickTextViewId, profilePictureViewId, logoutButtonId, updateNickButtonId;
		
		userNickTextViewId = getResources().getIdentifier("io_openkit_userNickTextView", "id", getActivity().getPackageName());
		profilePictureViewId = getResources().getIdentifier("io_openkit_fbProfilePicView", "id", getActivity().getPackageName());
		logoutButtonId = getResources().getIdentifier("io_openkit_logoutButton", "id", getActivity().getPackageName());
		updateNickButtonId = getResources().getIdentifier("io_openkit_changeNickButton", "id", getActivity().getPackageName());
		
		userNickTextView = (TextView)view.findViewById(userNickTextViewId);
		profiePictureView = (ProfilePictureView)view.findViewById(profilePictureViewId);
		logoutButton = (Button)view.findViewById(logoutButtonId);
		changeNickButton = (Button)view.findViewById(updateNickButtonId);
		
		logoutButton.setOnClickListener(logoutButtonClicked);
		changeNickButton.setOnClickListener(changeNickClicked);
		
		updateView();
		
		return view;
	}
	
	private void updateView()
	{
		OKUser currentUser = OKUser.getCurrentUser();
		userNickTextView.setText(currentUser.getUserNick());
		profiePictureView.setProfileId(Long.toString(currentUser.getFBUserID()));
	}
	
	private OnClickListener logoutButtonClicked = new OnClickListener() 
	{
		@Override
		public void onClick(View v) {
			OKUser.logoutCurrentUser(OKUserProfileFragment.this.getActivity());
			OKUserProfileFragment.this.getActivity().finish();
		}
	};
	
	private OnClickListener changeNickClicked = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
			OKLoginUpdateNickFragment nickDialog = new OKLoginUpdateNickFragment();
			
			//Update the view when the dialog is completed 
			nickDialog.setDialogHandler(new OKLoginUpdateNickFragmentHandler() {
				@Override
				public void onDismiss() {
					updateView();
				}
			});
			
			nickDialog.show(fm, "updateNick");
		}
	};

}
