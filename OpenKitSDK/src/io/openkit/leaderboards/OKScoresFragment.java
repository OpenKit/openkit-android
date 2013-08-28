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

package io.openkit.leaderboards;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.commonsware.cwac.merge.MergeAdapter;

import io.openkit.OKLeaderboard;
import io.openkit.OKLeaderboardTimeRange;
import io.openkit.OKLog;
import io.openkit.OKScore;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OKScoresFragment extends ListFragment
{
	private OKScoresListAdapter allTimeScoresAdapter;
	private OKScoresListAdapter thisWeekScoresAdapater;
	private OKScoresListAdapter todayScoresAdapter;

	private OKScore topScoreAllTime;
	private OKScore topScoreThisWeek;
	private OKScore topScoreToday;

	private ProgressBar spinnerBar;
	private ListView listView;
	private OKLeaderboard currentLeaderboard;

	private Button todayScoresButton;
	private Button weekScoresButton;
	private Button allTimeScoresButton;

	private Button moreScoresButton;

	private int selectedTab;

	private TextView listHeaderTextView;

	public static OKScoresFragment newInstance(OKLeaderboard leaderboard)
	{
		OKScoresFragment fragment = new OKScoresFragment();

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
		selectedTab = 3;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		int viewID = getResources().getIdentifier("io_openkit_fragment_okscores", "layout", getActivity().getPackageName());
		View view = inflater.inflate(viewID, container, false);
		//View view = inflater.inflate(R.layout.io_openkit_fragment_okscores, container, false);

		listView = (ListView)view.findViewById(android.R.id.list);

		int spinnerBarId, todayScoresButtonId, weekScoresButtonId, allTimeScoresButtonId;

		spinnerBarId = getResources().getIdentifier("progressSpinner", "id", getActivity().getPackageName());
		todayScoresButtonId = getResources().getIdentifier("io_openkit_leaderboards_todayButton", "id", getActivity().getPackageName());
		weekScoresButtonId = getResources().getIdentifier("io_openkit_leaderboards_thisWeekButton", "id", getActivity().getPackageName());
		allTimeScoresButtonId = getResources().getIdentifier("io_openkit_leaderboards_allTimeButton", "id", getActivity().getPackageName());

		spinnerBar = (ProgressBar)view.findViewById(spinnerBarId);
		todayScoresButton = (Button)view.findViewById(todayScoresButtonId);
		weekScoresButton = (Button)view.findViewById(weekScoresButtonId);
		allTimeScoresButton = (Button)view.findViewById(allTimeScoresButtonId);

		todayScoresButton.setOnClickListener(todayScoresPressed);
		weekScoresButton.setOnClickListener(thisWeekScoresPressed);
		allTimeScoresButton.setOnClickListener(allTimeScoresPressed);

		if(currentLeaderboard == null)
		{
			currentLeaderboard = getLeaderboard();
		}

		moreScoresButton = new Button(this.getActivity());
		moreScoresButton.setText("Show more scores");
		moreScoresButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.setEnabled(false);
				getMoreScores(currentLeaderboard.getDisplayedTimeRange(), v);
			}
		});


		selectCurrentTab();



		return view;
	}

	private View getHeaderView(String headerText)
	{
		LayoutInflater inflater = this.getActivity().getLayoutInflater();

		int listHeaderViewId, listHeaderTextViewId;
		listHeaderTextViewId = getResources().getIdentifier("headerTextView", "id", getActivity().getPackageName());
		listHeaderViewId = getResources().getIdentifier("list_simple_header", "layout", getActivity().getPackageName());

		//Inflate the list headerview
		View listHeaderView = inflater.inflate(listHeaderViewId, null);
		listHeaderTextView = (TextView)listHeaderView.findViewById(listHeaderTextViewId);
		listHeaderTextView.setText(headerText);

		return listHeaderView;
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

	private void showError()
	{
		if(this.getActivity() != null) {
			Toast toast = Toast.makeText(this.getActivity(), "Couldn't connect to server to get leaderboards", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	/**
	 * Gets the scores for this leaderboard and also the user's top score for this leaderboard
	 * @param range
	 */
	private void getScores(final OKLeaderboardTimeRange range)
	{
		showProgress();
		currentLeaderboard.setDisplayedTimeRange(range);

		//Get the scores for the leaderboard
		currentLeaderboard.getLeaderboardScores(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {

				// Choose the right adapater based on which view of leaderboards we're looking at
				switch (range) {
				case AllTime:
					allTimeScoresAdapter = new OKScoresListAdapter(OKScoresFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
					break;
				case OneWeek:
					thisWeekScoresAdapater = new OKScoresListAdapter(OKScoresFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
					break;
				case OneDay:
					todayScoresAdapter = new OKScoresListAdapter(OKScoresFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
					break;
				}

				updateListView();
				hideProgress();
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				hideProgress();
				showError();
			}

		});

		currentLeaderboard.getUsersTopScoreForLeaderboard(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {

				OKLog.d("User's top score is: " + scoresList);

				// Choose the right adapater based on which view of leaderboards we're looking at
				switch (range) {
				case AllTime:
					topScoreAllTime = scoresList.get(0);
					break;
				case OneWeek:
					topScoreThisWeek = scoresList.get(0);
					break;
				case OneDay:
					topScoreToday = scoresList.get(0);
					break;
				}

				updateListView();
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				hideProgress();
			}

		});

	}

	/**
	 * Updates the listview by creating a new merge adapater, and then adds the individual list adapters that are needed.
	 */
	private void updateListView()
	{
		MergeAdapter mergeAdapter = new MergeAdapter();

		OKScore topScore;
		OKScoresListAdapter scoresAdapater;

		switch (currentLeaderboard.getDisplayedTimeRange()) {
		case AllTime:
			topScore = topScoreAllTime;
			scoresAdapater = allTimeScoresAdapter;
			break;
		case OneDay:
			topScore = topScoreToday;
			scoresAdapater = todayScoresAdapter;
			break;
		default:
			topScore = topScoreThisWeek;
			scoresAdapater = thisWeekScoresAdapater;
			break;
		}

		if(topScore != null)
		{
			mergeAdapter.addView(getHeaderView("Your High Score"));
			List<OKScore> list = new ArrayList<OKScore>();
			list.add(topScore);
			mergeAdapter.addAdapter(new OKScoresListAdapter(this.getActivity(), android.R.layout.simple_list_item_1, list));
		}

		if(scoresAdapater != null)
		{
			mergeAdapter.addView(getHeaderView(currentLeaderboard.getPlayerCountString() + " Players"));
			mergeAdapter.addAdapter(scoresAdapater);
			mergeAdapter.addView(moreScoresButton);
		}

		this.setListAdapter(mergeAdapter);
	}

	// Get the next set of scores
	private void getMoreScores(OKLeaderboardTimeRange range, final View v)
	{
		final OKScoresListAdapter adapter = getAdapterforRange(range);

		// You can't get MORE scores if you don't have any scores to begin with
		if(adapter == null){
			return;
		}

		currentLeaderboard.setDisplayedTimeRange(range);

		// Calculate how many pages of scores have already been loaded
		int numScores = adapter.getCount();
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
					adapter.add(scoresList.get(x));
				}

				v.setEnabled(true);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				//Didn't get any more scores, renable the button
				v.setEnabled(true);
			}
		});
	}

	private void getAllTimeScores()
	{
		getScores(OKLeaderboardTimeRange.AllTime);
	}

	private void getThisWeekScores()
	{
		getScores(OKLeaderboardTimeRange.OneWeek);
	}

	private void getTodayScores()
	{
		getScores(OKLeaderboardTimeRange.OneDay);
	}

	private View.OnClickListener todayScoresPressed = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			selectTodayScoresTab();
		}
	};

	private View.OnClickListener thisWeekScoresPressed = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			selectThisWeekScoresTab();
		}
	};
	private View.OnClickListener allTimeScoresPressed = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			selectAllTimeScoresTab();
		}
	};

	private void selectCurrentTab()
	{
		switch (selectedTab) {
		case 3:
			selectAllTimeScoresTab();
			break;
		case 2:
			selectThisWeekScoresTab();
			break;
		default:
			selectTodayScoresTab();
			break;
		}
	}

	private void selectAllTimeScoresTab()
	{
		setButtonAsSelected(allTimeScoresButton);
		setButtonAsNormal(todayScoresButton);
		setButtonAsNormal(weekScoresButton);
		selectedTab = 3;

		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.AllTime);

		if(allTimeScoresAdapter == null) {
			getAllTimeScores();
		}
		else {
			updateListView();
		}
	}

	private void selectThisWeekScoresTab()
	{
		setButtonAsSelected(weekScoresButton);
		setButtonAsNormal(todayScoresButton);
		setButtonAsNormal(allTimeScoresButton);
		selectedTab = 2;

		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.OneWeek);

		if(thisWeekScoresAdapater == null) {
			getThisWeekScores();
		}
		else {
			updateListView();
		}
	}

	private void selectTodayScoresTab()
	{
		setButtonAsSelected(todayScoresButton);
		setButtonAsNormal(weekScoresButton);
		setButtonAsNormal(allTimeScoresButton);
		selectedTab = 1;

		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.OneDay);

		if(todayScoresAdapter == null) {
			getTodayScores();
		}
		else {
			updateListView();
		}
	}

	private void setButtonAsSelected(Button button)
	{
		int backgroundID = getResources().getIdentifier("io_openkit_tabbutton_selected", "drawable", getActivity().getPackageName());
		button.setBackgroundResource(backgroundID);
	}

	private void setButtonAsNormal(Button button)
	{
		int backgroundID = getResources().getIdentifier("io_openkit_tabbutton", "drawable", getActivity().getPackageName());
		button.setBackgroundResource(backgroundID);
	}

	private OKScoresListAdapter getAdapterforRange(OKLeaderboardTimeRange range)
	{
		switch (range) {
		case AllTime:
			return allTimeScoresAdapter;
		case OneDay:
			return todayScoresAdapter;
		default:
			return thisWeekScoresAdapater;
		}
	}

}
