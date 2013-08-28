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

import io.openkit.*;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OKLeaderboardsFragment extends ListFragment {

	private OKLeaderboardsListAdapter listAdapter;
	private ProgressBar spinnerBar;
	private ListView listView;
	private TextView listHeaderTextView;

	private boolean startedLeaderboardsRequest;

	private int numPlayers;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Note: have to lookup id manually for Unity
		int viewID = getResources().getIdentifier("io_openkit_fragment_leaderboards", "layout", getActivity().getPackageName());
		View view = inflater.inflate(viewID, container, false);

		int spinnerBarID = getResources().getIdentifier("progressSpinner", "id", getActivity().getPackageName());
		int listHeaderViewID = getResources().getIdentifier("list_simple_header", "layout", getActivity().getPackageName());
		int listHeaderTextViewID = getResources().getIdentifier("headerTextView", "id", getActivity().getPackageName());

		listView = (ListView)view.findViewById(android.R.id.list);
		spinnerBar = (ProgressBar)view.findViewById(spinnerBarID);


		View listHeaderView = inflater.inflate(listHeaderViewID, null);
		listHeaderTextView = (TextView)listHeaderView.findViewById(listHeaderTextViewID);
		listView.addHeaderView(listHeaderView);

		listHeaderTextView.setText(numPlayers + " Players");

		//Only do this the first time when fragment is created
		if(!startedLeaderboardsRequest) {
			getLeaderboards();

			/*
			if(OKUser.getCurrentUser() == null)
			{
				OKLog.v("Launching login view becuase no user is logged in");
				Intent launchLogin = new Intent(this.getActivity(), OKLoginActivity.class);
				startActivity(launchLogin);
			}*/
		}

		OKLog.v("On create view leaderboards");

		return view;
	}

	private void getLeaderboards()
	{
		startedLeaderboardsRequest = true;

		spinnerBar.setVisibility(View.VISIBLE);

		//Get the leaderboards
		OKLeaderboard.getLeaderboards(new OKLeaderboardsListResponseHandler() {

			@Override
			public void onSuccess(List<OKLeaderboard> leaderboardList, int playerCount) {
				listAdapter = new OKLeaderboardsListAdapter(OKLeaderboardsFragment.this.getActivity(),
						android.R.layout.simple_list_item_1, leaderboardList);

				numPlayers = playerCount;

				//Add a header to the list. Must be done before setting list adapter
				listHeaderTextView.setText(numPlayers + " Players");

				//Display the list
				OKLeaderboardsFragment.this.setListAdapter(listAdapter);
				listView.setVisibility(View.VISIBLE);
				spinnerBar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				spinnerBar.setVisibility(View.INVISIBLE);
				if(OKLeaderboardsFragment.this.getActivity() != null) {
					Toast toast = Toast.makeText(OKLeaderboardsFragment.this.getActivity(), "Couldn't connect to server to get leaderboards", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});
	}


}
