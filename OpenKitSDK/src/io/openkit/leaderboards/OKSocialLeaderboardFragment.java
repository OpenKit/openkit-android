package io.openkit.leaderboards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import com.commonsware.cwac.merge.MergeAdapter;

import io.openkit.OKLeaderboard;
import io.openkit.OKLeaderboardTimeRange;
import io.openkit.OKLog;
import io.openkit.OKScore;
import io.openkit.OKUser;
import io.openkit.facebook.FacebookRequestError;
import io.openkit.facebook.Session;
import io.openkit.facebook.SessionState;
import io.openkit.facebookutils.FacebookUtilities;
import io.openkit.facebookutils.FacebookUtilities.GetFBFriendsRequestHandler;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OKSocialLeaderboardFragment extends ListFragment {

	private ProgressBar spinnerBar;
	private ListView listView;
	private OKLeaderboard currentLeaderboard;
	private Button moreScoresButton;

	private OKScoresListAdapter scoresListAdapter;
	private OKScoresListAdapter friendsScoresListAdapter;

	private int numSocialRequestsRunning = 0;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
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

		// Setup the Facebook cached session if there is one
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity(), null, sessionStatusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(getActivity());
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(sessionStatusCallback));
			}
		}

		moreScoresButton = new Button(this.getActivity());
		moreScoresButton.setText("Show more scores");
		moreScoresButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.setEnabled(false);
				getMoreGlobalScores();
			}
		});

		return view;
	}

	private void updateListView()
	{
		if(this.getActivity() == null)
			return;

		MergeAdapter mergeAdapter = new MergeAdapter();

		mergeAdapter.addView(getHeaderView("Friends"));

		if(friendsScoresListAdapter != null && friendsScoresListAdapter.getCount() > 0) {
			mergeAdapter.addAdapter(friendsScoresListAdapter);
		}

		if (isShowingSocialScoresProgressBar()) {
			mergeAdapter.addView(getSpinnerRow());
		}

		if (!FacebookUtilities.isFBSessionOpen()) {
			mergeAdapter.addView(getFBLoginRow());
		} else if(FacebookUtilities.isFBSessionOpen() && friendsScoresListAdapter != null && friendsScoresListAdapter.getCount() <= 1 && !isShowingSocialScoresProgressBar()) {
			// Show invite friends
			mergeAdapter.addView(getFBInviteRow());
		}

		mergeAdapter.addView(getHeaderView("All Players"));

		if(scoresListAdapter != null && scoresListAdapter.getCount() > 0) {
			mergeAdapter.addAdapter(scoresListAdapter);
			mergeAdapter.addView(moreScoresButton);
		}

		this.setListAdapter(mergeAdapter);
	}

	private View getFBLoginRow()
	{
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		int fbLoginRowID = getResources().getIdentifier("io_openkit_list_fb_login", "layout", getActivity().getPackageName());
		View fbLoginRow = inflater.inflate(fbLoginRowID, null);


		int loginButtonID = getResources().getIdentifier("io_openkit_fbSignInButton", "id", getActivity().getPackageName());
		Button fbLoginButton = (Button)fbLoginRow.findViewById(loginButtonID);

		fbLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				loginToFacebook();
			}
		});
		return fbLoginRow;
	}

	private View getSpinnerRow()
	{
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		int spinnerRowID = getResources().getIdentifier("io_openkit_list_spinner", "layout", getActivity().getPackageName());
		View spinnerRow = inflater.inflate(spinnerRowID, null);
		return spinnerRow;
	}

	private View getFBInviteRow()
	{
		View fbLoginRow = getFBLoginRow();

		int textViewId, fbLoginbuttonId;
		textViewId = getResources().getIdentifier("io_openkit_fbloginTitleTextView", "id", getActivity().getPackageName());
		fbLoginbuttonId = getResources().getIdentifier("io_openkit_fbSignInButton", "id", getActivity().getPackageName());

		TextView fbTextView = (TextView)fbLoginRow.findViewById(textViewId);
		Button loginButton = (Button)fbLoginRow.findViewById(fbLoginbuttonId);

		fbTextView.setText("Invite some friends");
		loginButton.setText("Invite some friends");

		loginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				FacebookUtilities.showAppRequestsDialog("Check out this game!", getActivity(), getActivity().getApplicationContext());
			}
		});

		return fbLoginRow;
	}

	private View getHeaderView(String headerText)
	{
		LayoutInflater inflater = this.getActivity().getLayoutInflater();

		int listHeaderViewId, listHeaderTextViewId;
		listHeaderTextViewId = getResources().getIdentifier("headerTextView", "id", getActivity().getPackageName());
		listHeaderViewId = getResources().getIdentifier("list_simple_header", "layout", getActivity().getPackageName());

		//Inflate the list headerview
		View listHeaderView = inflater.inflate(listHeaderViewId, null);
		TextView listHeaderTextView = (TextView)listHeaderView.findViewById(listHeaderTextViewId);
		listHeaderTextView.setText(headerText);

		return listHeaderView;
	}


	private void getScores()
	{
		showProgress();

		OKLog.v("Getting global Scores");
		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.AllTime);
		currentLeaderboard.getLeaderboardScores(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {
				scoresListAdapter = new OKScoresListAdapter(OKSocialLeaderboardFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
				hideProgress();
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				hideProgress();
			}
		});

		getSocialScores();
	}

	private void getMoreGlobalScores()
	{
		// You can't get MORE scores if you don't have any scores to begin with
		if(scoresListAdapter == null){
			return;
		}

		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.AllTime);

		// Calculate how many pages of scores have already been loaded
		int numScores = scoresListAdapter.getCount();
		int currentPageNumber = numScores / OKLeaderboard.NUM_SCORES_PER_PAGE;
		if(currentPageNumber*OKLeaderboard.NUM_SCORES_PER_PAGE < numScores) {
			currentPageNumber++;
		}

		int nextPageNumber = currentPageNumber + 1;

		// Get the next page of scores
		currentLeaderboard.getLeaderboardScores(nextPageNumber, new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {

				//Iterate through and add each score because Android 2.3 doesn't have addAll
				for(int x = 0; x < scoresList.size(); x++)
				{
					scoresListAdapter.add(scoresList.get(x));
				}

				moreScoresButton.setEnabled(true);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				//Didn't get any more scores, renable the button
				moreScoresButton.setEnabled(true);
			}
		});
	}

	private void getSocialScores()
	{
		getSocialScoresFromOpenKit();
		getUsersTopScoreFromOpenKit();
	}

	private void getSocialScoresFromOpenKit()
	{
		if(FacebookUtilities.isFBSessionOpen())
		{

			startedSocialRequest();

			FacebookUtilities.GetFBFriends(new GetFBFriendsRequestHandler() {

				@Override
				public void onSuccess(ArrayList<Long> friendsArray) {
					currentLeaderboard.getFacebookFriendsScoresWithFacebookFriends(friendsArray, new OKScoresResponseHandler() {

						@Override
						public void onSuccess(List<OKScore> scoresList) {
							OKLog.v("Got %d social scores!", scoresList.size());
							addScoresToSocialScoresListAdapater(scoresList);
							stoppedSocialRequest();
						}

						@Override
						public void onFailure(Throwable e, JSONObject errorResponse) {
							OKLog.v("Failed to get social scores from OpenKit: " + e);
							stoppedSocialRequest();
						}
					});
				}

				@Override
				public void onFail(FacebookRequestError error) {
					OKLog.v("Failed to get Facebook friends");
					stoppedSocialRequest();
				}
			});
		}
	}

	private void addScoresToSocialScoresListAdapater(List<OKScore> scoresList)
	{
		if(this.getActivity() == null)
			return;

		if(friendsScoresListAdapter == null) {
			sortSocialScores(scoresList);
			friendsScoresListAdapter = new OKScoresListAdapter(this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
		} else {
			List<OKScore> previousScoresList, newScoreslist;
			previousScoresList = friendsScoresListAdapter.getItems();
			previousScoresList.addAll(scoresList);
			newScoreslist = previousScoresList;
			sortSocialScores(newScoreslist);
			friendsScoresListAdapter = new OKScoresListAdapter(OKSocialLeaderboardFragment.this.getActivity(), android.R.layout.simple_list_item_1, newScoreslist);
		}

		updateListView();
	}

	private void getUsersTopScoreFromOpenKit()
	{
		if(OKUser.getCurrentUser() == null)
			return;

		startedSocialRequest();
		OKLog.v("Getting users top score from OpenKit");
		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.AllTime);
		currentLeaderboard.getUsersTopScoreForLeaderboard(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {
				stoppedSocialRequest();
				addScoresToSocialScoresListAdapater(scoresList);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				stoppedSocialRequest();
				OKLog.v("Failed to get user's top score");
			}
		});
	}

	private void sortSocialScores(List<OKScore> scoresList)
	{
		//Sort social scores and set their relative ranks
		Collections.sort(scoresList, currentLeaderboard.getScoreComparator());

		for(int x = 0; x < scoresList.size(); x++)
		{
			scoresList.get(x).setRank(x+1);
		}
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
		updateListView();
	}

	private void startedSocialRequest()
	{
		numSocialRequestsRunning++;
		if(numSocialRequestsRunning == 1)
			updateListView();
	}

	private void stoppedSocialRequest()
	{
		numSocialRequestsRunning--;
		if(numSocialRequestsRunning <= 0)
			updateListView();
	}

	private boolean isShowingSocialScoresProgressBar()
	{
		return (numSocialRequestsRunning > 0);
	}


	private void loginToFacebook()
	{
		Session session = Session.getActiveSession();

		if(!session.isOpened() && !session.isClosed()){
			session.openForRead(new Session.OpenRequest(this)
			//.setPermissions(Arrays.asList("basic_info"))
			.setCallback(sessionStatusCallback));
		}
		else if(session.isOpened())
		{
			//Facebook session is already open, just authorize the user with OpenKit
			loggedIntoFB();
		}
		else {
			Session.openActiveSession(getActivity(), this, true, sessionStatusCallback);
		}
	}


	private void loggedIntoFB()
	{
		FacebookUtilities.CreateOrUpdateOKUserFromFacebook(this.getActivity().getApplicationContext());
		getSocialScoresFromOpenKit();
	}

	/* Facebook session state change handler. This method handles all cases of Facebook auth */
	private Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {

			// Log all facebook exceptions
			if(exception != null){
				OKLog.v("SessionState changed exception: " + exception + " hash code: " + exception.hashCode());
			}

			FacebookUtilities.logSessionState(state);

			// If the session is opened, authorize the user, if the session is closed then display an error message if necessary
			if (state.isOpened())
			{
				OKLog.v("FB Session is Open");
				loggedIntoFB();

			} else if (state.isClosed()) {
				OKLog.v("FB Session is Closed");
				if(exception != null) {
					String errorMessage = FacebookUtilities.ShouldShowFacebookError(exception);
					if(errorMessage != null) {
						FacebookUtilities.showErrorMessage(errorMessage, OKSocialLeaderboardFragment.this.getActivity());
					}
				}
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
