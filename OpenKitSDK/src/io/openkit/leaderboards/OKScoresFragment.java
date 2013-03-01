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

import java.util.List;

import org.json.JSONObject;

import io.openkit.OKLeaderboard;
import io.openkit.OKLeaderboardTimeRange;
import io.openkit.OKScore;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
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

	private ProgressBar spinnerBar;
	private ListView listView;
	private OKLeaderboard currentLeaderboard;
	
	private Button todayScoresButton;
	private Button weekScoresButton;
	private Button allTimeScoresButton;
	
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
		/*
		spinnerBar = (ProgressBar)view.findViewById(R.id.progressSpinner);
		todayScoresButton = (Button)view.findViewById(R.id.io_openkit_leaderboards_todayButton);
		weekScoresButton = (Button)view.findViewById(R.id.io_openkit_leaderboards_thisWeekButton);
		allTimeScoresButton = (Button)view.findViewById(R.id.io_openkit_leaderboards_allTimeButton);
		*/
		
		todayScoresButton.setOnClickListener(todayScoresPressed);
		weekScoresButton.setOnClickListener(thisWeekScoresPressed);
		allTimeScoresButton.setOnClickListener(allTimeScoresPressed);
		
		if(currentLeaderboard == null)
		{
			currentLeaderboard = getLeaderboard();
		}
		
		selectCurrentTab();
		
		int listHeaderViewId, listHeaderTextViewId;
		
		listHeaderTextViewId = getResources().getIdentifier("headerTextView", "id", getActivity().getPackageName());
		listHeaderViewId = getResources().getIdentifier("list_simple_header", "layout", getActivity().getPackageName());
		
		//Inflate the list headerview
		View listHeaderView = inflater.inflate(listHeaderViewId, null);
		listHeaderTextView = (TextView)listHeaderView.findViewById(listHeaderTextViewId);
		//View listHeaderView = inflater.inflate(R.layout.list_simple_header, null);
		//listHeaderTextView = (TextView)listHeaderView.findViewById(R.id.headerTextView);
		listView.addHeaderView(listHeaderView);
		listHeaderTextView.setText(currentLeaderboard.getPlayerCountString() + " Players");
		
		return view;
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
		Toast toast = Toast.makeText(this.getActivity(), "Couldn't connect to server to get leaderboards", Toast.LENGTH_LONG);
		toast.show();
	}
	
	private void getAllTimeScores()
	{
		showProgress();
		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.AllTime);
		currentLeaderboard.getLeaderboardScores(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {
				//Create the list adapter with list of leaderboards
				allTimeScoresAdapter = new OKScoresListAdapter(OKScoresFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
				//Display the list
				OKScoresFragment.this.setListAdapter(allTimeScoresAdapter);
				hideProgress();
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				hideProgress();
				showError();
			}
		});
	}
	
	
	private void getThisWeekScores()
	{
		showProgress();
		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.OneWeek);
		currentLeaderboard.getLeaderboardScores(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {
				//Create the list adapter with list of leaderboards
				thisWeekScoresAdapater = new OKScoresListAdapter(OKScoresFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
				//Display the list
				OKScoresFragment.this.setListAdapter(thisWeekScoresAdapater);
				hideProgress();
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				hideProgress();
				showError();
			}
		});
	}
	
	private void getTodayScores()
	{
		showProgress();
		currentLeaderboard.setDisplayedTimeRange(OKLeaderboardTimeRange.OneDay);
		currentLeaderboard.getLeaderboardScores(new OKScoresResponseHandler() {

			@Override
			public void onSuccess(List<OKScore> scoresList) {
				//Create the list adapter with list of leaderboards
				todayScoresAdapter = new OKScoresListAdapter(OKScoresFragment.this.getActivity(), android.R.layout.simple_list_item_1, scoresList);
				//Display the list
				OKScoresFragment.this.setListAdapter(todayScoresAdapter);
				hideProgress();
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				hideProgress();
				showError();
			}
		});
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
		
		if(allTimeScoresAdapter == null) {
			getAllTimeScores();
		}
		else {
			this.setListAdapter(allTimeScoresAdapter);
		}
	}
	
	private void selectThisWeekScoresTab()
	{
		setButtonAsSelected(weekScoresButton);
		setButtonAsNormal(todayScoresButton);
		setButtonAsNormal(allTimeScoresButton);
		selectedTab = 2;
		
		if(thisWeekScoresAdapater == null) {
			getThisWeekScores();
		}
		else {
			this.setListAdapter(thisWeekScoresAdapater);
		}
	}
	
	private void selectTodayScoresTab()
	{
		setButtonAsSelected(todayScoresButton);
		setButtonAsNormal(weekScoresButton);
		setButtonAsNormal(allTimeScoresButton);
		selectedTab = 1;
		
		if(todayScoresAdapter == null) {
			getTodayScores();
		}
		else {
			this.setListAdapter(todayScoresAdapter);
		}
	}
	
	private void setButtonAsSelected(Button button)
	{
		int backgroundID = getResources().getIdentifier("tab_active", "drawable", getActivity().getPackageName());
		button.setBackgroundResource(backgroundID);
	}
	
	private void setButtonAsNormal(Button button)
	{
		int backgroundID = getResources().getIdentifier("io_openkit_tabbutton", "drawable", getActivity().getPackageName());
		button.setBackgroundResource(backgroundID);
	}

}
