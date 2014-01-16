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


import com.facebook.widget.ProfilePictureView;
import io.openkit.user.*;
import android.content.DialogInterface;
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


	private OKLoginUpdateNickFragmentHandler dialogHandler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		setCancelable(false);
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}


	public void setDialogHandler(OKLoginUpdateNickFragmentHandler handler)
	{
		dialogHandler = handler;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		int viewID, userNickTextViewId, userNickEditTextId, continueButtonId, spinnerId, profilePictureId;

		viewID = getResources().getIdentifier("io_openkit_fragment_updatenick", "layout", getActivity().getPackageName());
		userNickTextViewId = getResources().getIdentifier("io_openkit_userNickTextView", "id", getActivity().getPackageName());
		userNickEditTextId = getResources().getIdentifier("io_openkit_userNickEditText", "id", getActivity().getPackageName());
		continueButtonId = getResources().getIdentifier("io_openkit_continueButton", "id", getActivity().getPackageName());
		spinnerId = getResources().getIdentifier("io_openkit_spinner", "id", getActivity().getPackageName());
		profilePictureId = getResources().getIdentifier("io_openkit_fbProfilePicView", "id", getActivity().getPackageName());

		View view = inflater.inflate(viewID, container, false);
		userNickTextView = (TextView)view.findViewById(userNickTextViewId);
		userNickEditText = (EditText)view.findViewById(userNickEditTextId);
		continueButton = (Button)view.findViewById(continueButtonId);
		spinner = (ProgressBar)view.findViewById(spinnerId);
		profiePictureView = (ProfilePictureView)view.findViewById(profilePictureId);


		currentUser = OpenKit.getCurrentUser();
		userNickTextView.setText(currentUser.getUserNick());
		userNickEditText.setHint(currentUser.getUserNick());
		profiePictureView.setProfileId(currentUser.getFBUserID());

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


	@Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(dialogHandler != null) {
        	dialogHandler.onDismiss();
        }
    }

	private View.OnClickListener onSubmitNewNick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String newUserNick = userNickEditText.getText().toString();

			if(newUserNick.equals("") || newUserNick.equals(currentUser.getUserNick()))
			{
				OKLoginUpdateNickFragment.this.dismiss();
			}
			else {
				updateUserNick(newUserNick);
			}
		}
	};

	private void updateUserNick(String newNick)
	{
		showSpinner();

		OKUserUtilities.updateUserNick(currentUser, newNick, new CreateOrUpdateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				hideSpinner();
				OKManager.INSTANCE.handlerUserLoggedIn(user, OKLoginUpdateNickFragment.this.getActivity());
				OKLoginUpdateNickFragment.this.dismiss();
			}

			@Override
			public void onFail(Throwable error) {
				hideSpinner();
				OKLoginUpdateNickFragment.this.dismiss();
			}
		});
	}

}
