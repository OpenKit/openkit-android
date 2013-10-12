package io.openkit.facebookutils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FBLoginPrompt extends DialogFragment
{
	public interface FBLoginPromptDelegate
	{
		void onDialogComplete(int buttonPressed);
	}

	private Button fbLoginButton,dontLoginButton;
	private FBLoginPromptDelegate delegate;

	public FBLoginPromptDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(FBLoginPromptDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		setCancelable(false);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setOnKeyListener(backButtonListener);
		return dialog;
	}

	// If the back button is pressed, close the dialog
	public DialogInterface.OnKeyListener backButtonListener = new DialogInterface.OnKeyListener() {

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				FBLoginPrompt.this.dismiss();
			}
			return false;
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		int viewID, fbLoginButtonId, dontLoginButtonId;
		viewID = getResources().getIdentifier("io_openkit_fragment_fbprompt", "layout", getActivity().getPackageName());
		fbLoginButtonId = getResources().getIdentifier("io_openkit_fbSignInButton", "id", getActivity().getPackageName());
		dontLoginButtonId = getResources().getIdentifier("io_openkit_dontSignInButton", "id", getActivity().getPackageName());
		View view = inflater.inflate(viewID, container, false);

		fbLoginButton = (Button)view.findViewById(fbLoginButtonId);
		dontLoginButton = (Button)view.findViewById(dontLoginButtonId);
		fbLoginButton.setOnClickListener(clickedFBButton);
		dontLoginButton.setOnClickListener(clickedNoThanksButton);
		return view;
	}


	private View.OnClickListener clickedFBButton = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if(getDelegate() != null) {
				getDelegate().onDialogComplete(1);
			}
			FBLoginPrompt.this.dismiss();
		}
	};

	private View.OnClickListener clickedNoThanksButton = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if(getDelegate() != null) {
				getDelegate().onDialogComplete(2);
			}
			FBLoginPrompt.this.dismiss();
		}
	};


}
