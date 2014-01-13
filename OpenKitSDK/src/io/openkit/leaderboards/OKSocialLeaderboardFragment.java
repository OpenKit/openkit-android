package io.openkit.leaderboards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import com.commonsware.cwac.merge.MergeAdapter;

import io.openkit.OKLeaderboard;
import io.openkit.OKLeaderboardTimeRange;
import io.openkit.OKLog;
import io.openkit.OKManager;
import io.openkit.OKScore;
import io.openkit.OKUser;
import com.facebook.FacebookRequestError;
import io.openkit.facebookutils.FBLoginPrompt;
import io.openkit.facebookutils.FBLoginRequest;
import io.openkit.facebookutils.FBLoginRequestHandler;
import io.openkit.facebookutils.FacebookUtilities;
import io.openkit.facebookutils.FacebookUtilities.GetFBFriendsRequestHandler;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OKSocialLeaderboardFragment extends ListFragment {

	private ProgressBar spinnerBar;
	private ListView listView;
	private OKLeaderboard currentLeaderboard;
	private Button moreScoresButton;
	private OKScoresListAdapter scoresListAdapter;
	private OKScoresListAdapter friendsScoresListAdapter;
	private OKScoresListAdapter topScoreAdapter;
	private FBLoginRequest fbLoginRequest;
	private int numSocialRequestsRunning = 0;

	public static OKSocialLeaderboardFragment newInstance(OKLeaderboard leaderboard)
	{
		OKSocialLeaderboardFragment fragment = new OKSocialLeaderboardFragment();
		Bundle args = new Bundle();
		args.putParcelable(OKLeaderboard.LEADERBOARD_KEY, leaderboard);
		fragment.setArguments(args);
		return fragment;
	}

	public static OKSocialLeaderboardFragment newInstance(int leaderboardID)
	{
		OKSocialLeaderboardFragment fragment = new OKSocialLeaderboardFragment();
		Bundle args = new Bundle();
		args.putInt(OKLeaderboard.LEADERBOARD_ID_KEY, leaderboardID);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setRetainInstance(true);
		fbLoginRequest = new FBLoginRequest();
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	private void getLeaderboardFromOpenKitWithID()
	{
		int currentLeaderboardID = getArguments().getInt(OKLeaderboard.LEADERBOARD_ID_KEY);

		showProgress();

		OKLeaderboard.getLeaderboard(currentLeaderboardID, new OKLeaderboardsListResponseHandler() {

			@Override
			public void onSuccess(List<OKLeaderboard> leaderboardList, int playerCount) {
				if(leaderboardList.size() > 0) {
					currentLeaderboard = leaderboardList.get(0);
					OKSocialLeaderboardFragment.this.getActivity().setTitle(currentLeaderboard.getName());
					getScores();
				} else {
					errorLoadingLeaderboard();
				}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				OKLog.d("Error getting leaderboard: " + e + errorResponse);
				errorLoadingLeaderboard();
			}
		});
	}

	private void errorLoadingLeaderboard() {
		spinnerBar.setVisibility(View.INVISIBLE);
		if(this.getActivity() != null) {
			Toast.makeText(this.getActivity(), "Sorry, but the leaderboard can't be loaded right now. Please try again later. ", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		int menuID = getActivity().getResources().getIdentifier("io_openkit_menu_activity_scores", "menu", getActivity().getPackageName());
	    inflater.inflate(menuID, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int inviteButtonID = getResources().getIdentifier("io_openkit_menu_inviteButton", "id", getActivity().getPackageName());

		if(item.getItemId() == inviteButtonID) {
			showFacebookRequestsDialog();
			return true;
		}

		return super.onOptionsItemSelected(item);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		fbLoginRequest.onCreateView(savedInstanceState, this);

		int viewID = getResources().getIdentifier("io_openkit_fragment_oksocialleaderboard", "layout", getActivity().getPackageName());
		View view = inflater.inflate(viewID, container, false);

		listView = (ListView)view.findViewById(android.R.id.list);

		int spinnerBarId;
		spinnerBarId = getResources().getIdentifier("progressSpinner", "id", getActivity().getPackageName());
		spinnerBar = (ProgressBar)view.findViewById(spinnerBarId);

		// Start loading the scores if the current leaderboard is already available
		// Current leaderboard may have to be fetched from server if view was
		// initiated with only a leaderboard ID

		currentLeaderboard = getLeaderboard();

		if(scoresListAdapter == null && currentLeaderboard != null) {
			getScores();
		} else if(currentLeaderboard == null) {
			getLeaderboardFromOpenKitWithID();
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

	private void loginToFacebook(final boolean showRequestsDialog)
	{
		FBLoginRequestHandler loginRequestHandler = new FBLoginRequestHandler() {

			@Override
			public void onFBLoginSucceeded() {
				FacebookUtilities.CreateOrUpdateOKUserFromFacebook(OKSocialLeaderboardFragment.this.getActivity().getApplicationContext());
				getSocialScoresFromOpenKit();
				if(showRequestsDialog) {
					showFacebookRequestsDialog();
				}
			}

			@Override
			public void onFBLoginError(String errorMessage) {
				if(errorMessage != null) {
					FacebookUtilities.showErrorMessage(errorMessage, OKSocialLeaderboardFragment.this.getActivity());
				}
			}

			@Override
			public void onFBLoginCancelled() {
				OKLog.v("FB Login cancelled");
			}
		};

		fbLoginRequest.setRequestHandler(loginRequestHandler);
		fbLoginRequest.loginToFacebook(this);
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
			//mergeAdapter.addView(moreScoresButton);
		}

		// Show the top score if the rank of the top score == 0 || rank < numTopScores shown
		if(topScoreAdapter != null && scoresListAdapter != null) {
			int playerRank = topScoreAdapter.getItem(0).getRank();

			if(playerRank == 0 || playerRank > scoresListAdapter.getCount()) {
				mergeAdapter.addAdapter(topScoreAdapter);
			}
		}

		if(scoresListAdapter != null && scoresListAdapter.getCount() > 0) {
			mergeAdapter.addView(moreScoresButton);
		}

		this.setListAdapter(mergeAdapter);

		showPromptForFBIfNecessary();
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
				loginToFacebook(false);
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

		fbTextView.setText("Invite some friends from");
		loginButton.setText("Facebook");

		loginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				FacebookUtilities.showAppRequestsDialog("Check out this game!", getActivity(), getActivity().getApplicationContext());
			}
		});

		return fbLoginRow;
	}

	private void showFacebookRequestsDialog()
	{
		if(FacebookUtilities.isFBSessionOpen()) {
			FacebookUtilities.showAppRequestsDialog("Check out this game!", getActivity(), getActivity().getApplicationContext());
		} else {
			loginToFacebook(true);
		}
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

				if(topScoreAdapter != null && topScoreAdapter.getCount() > 0) {
					OKScore topScore = topScoreAdapter.getItem(0);

					if(topScore.getRank() <= scoresListAdapter.getCount()) {
						topScoreAdapter.remove(topScore);
					}
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

		startedSocialRequest();
		OKLog.v("Getting users top score from OpenKit");
		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.AllTime);
		currentLeaderboard.getUsersTopScoreForLeaderboard(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {

				if(scoresList.size() > 0) {
					// Need to create a copy of the top score because the user's top score has 2 different ranks: a social rank and a global rank.
					// The social section sets the rank to the relative rank in that section
					OKScore topScore = scoresList.get(0);
					OKScore copiedTopScore = new OKScore(topScore);
					List<OKScore> copiedTopScoreList = new ArrayList<OKScore>(1);
					copiedTopScoreList.add(copiedTopScore);
					topScoreAdapter = new OKScoresListAdapter(OKSocialLeaderboardFragment.this.getActivity(), android.R.layout.simple_list_item_1, copiedTopScoreList);
					topScoreAdapter.showAlternateBGColor();

					addScoresToSocialScoresListAdapater(scoresList);
				}
				stoppedSocialRequest();
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

	private void showPromptForFBIfNecessary()
	{
		if(!OKManager.INSTANCE.hasShownFBLoginPrompt() && (OKUser.getCurrentUser() == null || OKUser.getCurrentUser().getFBUserID() == null)) {
			OKManager.INSTANCE.setHasShownFBLoginPrompt(true);
			showPromptForFacebook();
		}
	}

	private void showPromptForFacebook()
	{
		FBLoginPrompt prompt = new FBLoginPrompt();

		prompt.setDelegate(new FBLoginPrompt.FBLoginPromptDelegate() {
			@Override
			public void onDialogComplete(int buttonPressed) {
				if(buttonPressed == 1) {
					loginToFacebook(false);
				}
			}
		});

		prompt.show(getFragmentManager(), "FBLOGINPROMPT");
	}


	/* Below methods are overridden to add the Facebook session lifecycle callbacks */

	@Override
	public void onStart() {
		super.onStart();
		fbLoginRequest.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		fbLoginRequest.onStop();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		fbLoginRequest.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		fbLoginRequest.onSaveInstanceState(outState);
	}
}
