package io.openkit.leaderboards;

import java.util.List;

import org.json.JSONObject;

import io.openkit.OKLeaderboard;
import io.openkit.OKLeaderboardTimeRange;
import io.openkit.OKLog;
import io.openkit.OKScore;
import io.openkit.facebook.Session;
import io.openkit.facebook.SessionState;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

public class OKSocialLeaderboardFragment extends ListFragment {

	private ProgressBar spinnerBar;
	private ListView listView;
	private OKLeaderboard currentLeaderboard;

	private OKScoresListAdapter scoresListAdapter;

	public static OKSocialLeaderboardFragment newInstance(OKLeaderboard leaderboard)
	{
		OKSocialLeaderboardFragment fragment = new OKSocialLeaderboardFragment();

		Bundle args = new Bundle();
		args.putParcelable(OKLeaderboard.LEADERBOARD_KEY, leaderboard);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
	{
		int viewID = getResources().getIdentifier("io_openkit_fragment_oksocialleaderboard", "layout", getActivity().getPackageName());
		View view = inflater.inflate(viewID, container, false);

		listView = (ListView)view.findViewById(android.R.id.list);

		int spinnerBarId;
		spinnerBarId = getResources().getIdentifier("progressSpinner", "id", getActivity().getPackageName());
		spinnerBar = (ProgressBar)view.findViewById(spinnerBarId);

		if(currentLeaderboard == null)
		{
			currentLeaderboard = getLeaderboard();
		}

		if(scoresListAdapter == null)
			getScores();

		return view;
	}

	private void getScores()
	{
		OKLog.v("Getting Scores");

		showProgress();

		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.AllTime);

		currentLeaderboard.getLeaderboardScores(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {
				scoresListAdapter = new OKScoresListAdapter(OKSocialLeaderboardFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
				OKSocialLeaderboardFragment.this.setListAdapter(scoresListAdapter);
				hideProgress();
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				hideProgress();
			}
		});

	}

	private OKLeaderboard getLeaderboard()
	{
		return (OKLeaderboard)getArguments().getParcelable(OKLeaderboard.LEADERBOARD_KEY);
	}

	private void showProgress()
	{
		spinnerBar.setVisibility(View.VISIBLE);
		listView.setVisibility(View.INVISIBLE);
	}

	private void hideProgress()
	{
		listView.setVisibility(View.VISIBLE);
		spinnerBar.setVisibility(View.INVISIBLE);
	}


	/* Facebook session state change handler. This method handles all cases of Facebook auth */
	private Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {

			// Log all facebook exceptions
			if(exception != null)
			{
				OKLog.v("SessionState changed exception: " + exception + " hash code: " + exception.hashCode());
			}

			//Log what is happening with the Facebook session for debug help
			switch (state) {
			case OPENING:
				OKLog.v("SessionState Opening");
				break;
			case CREATED:
				OKLog.v("SessionState Created");
				break;
			case OPENED:
				OKLog.v("SessionState Opened");
				break;
			case CLOSED_LOGIN_FAILED:
				OKLog.v("SessionState Closed Login Failed");
				break;
			case OPENED_TOKEN_UPDATED:
				OKLog.v("SessionState Opened Token Updated");
				break;
			case CREATED_TOKEN_LOADED:
				OKLog.v("SessionState created token loaded" );
				break;
			case CLOSED:
				OKLog.v("SessionState closed");
				break;
			default:
				OKLog.v("Session State Default case");
				break;
			}

			// If the session is opened, authorize the user, if the session is closed
			if (state.isOpened())
			{
				OKLog.v("FB Session is Open");
				/*if(fbLoginButtonClicked){
					OKLog.v("Authorizing user with Facebook");
					authorizeFBUserWithOpenKit();
					fbLoginButtonClicked = false;
				}*/
			} else if (state.isClosed()) {
				OKLog.v("FB Session is Closed");
				/*if(fbLoginButtonClicked) {
					facebookLoginFailed(exception);
					fbLoginButtonClicked = false;
				}*/
			}
		}
	};


	/* Below methods are overridden to add the Facebook session lifecycle callbacks */

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(sessionStatusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(sessionStatusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		// Handle activity result for Facebook auth
		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}
}
