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

import io.openkit.OKUserUtilities.UpdateUserNickRequestHandler;

import com.facebook.widget.ProfilePictureView;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OKLoginUpdateNickFragment extends DialogFragment 
{
	
	private TextView userNickTextView;
	private EditText userNickEditText;
	private Button continueButton;
	private ProgressBar spinner;
	private ProfilePictureView profiePictureView;
	
	private OKUser currentUser;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		setCancelable(false);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.io_openkit_fragment_updatenick, container, false);
		
		userNickTextView = (TextView)view.findViewById(R.id.io_openkit_userNickTextView);
		userNickEditText = (EditText)view.findViewById(R.id.io_openkit_userNickEditText);
		continueButton = (Button)view.findViewById(R.id.io_openkit_continueButton);
		spinner = (ProgressBar)view.findViewById(R.id.io_openkit_spinner);
		profiePictureView = (ProfilePictureView)view.findViewById(R.id.io_openkit_fbProfilePicView);
		
		currentUser = OpenKit.getCurrentUser();
		userNickTextView.setText(currentUser.getUserNick());
		userNickEditText.setHint(currentUser.getUserNick());	
		profiePictureView.setProfileId(Long.toString(currentUser.getFBUserID()));
	
		continueButton.setOnClickListener(onSubmitNewNick);
		
		return view;
	}
	
	//Show Spinner
	private void showSpinner()
	{
		spinner.setVisibility(View.VISIBLE);
		continueButton.setVisibility(View.INVISIBLE);
	}
	
	private void hideSpinner()
	{
		spinner.setVisibility(View.INVISIBLE);
		continueButton.setVisibility(View.VISIBLE);
	}
	
	private void dismissNickUpdateDialog()
	{
		OKLoginDialogListener delegate = (OKLoginDialogListener)OKLoginUpdateNickFragment.this.getActivity();
		delegate.nickUpdateCompleted();
	}
	
	private View.OnClickListener onSubmitNewNick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String newUserNick = userNickEditText.getText().toString();
			
			if(newUserNick.isEmpty() || newUserNick.equals(currentUser.getUserNick()))
			{
				dismissNickUpdateDialog();
			}
			else {
				updateUserNick(newUserNick);
			}
		}
	};
	
	private void updateUserNick(String newNick)
	{
		showSpinner();
		
		OKUserUtilities.updateUserNick(currentUser, newNick, new UpdateUserNickRequestHandler() {
			
			@Override
			public void onSuccess(OKUser user) {
				hideSpinner();
				OpenKitSingleton.INSTANCE.handlerUserLoggedIn(user, OKLoginUpdateNickFragment.this.getActivity());
				dismissNickUpdateDialog();
			}
			
			@Override
			public void onFail(Throwable error) {
				hideSpinner();
				dismissNickUpdateDialog();
			}
		});
	}
	
}
