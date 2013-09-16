package io.openkit.leaderboards;

import java.util.List;

import org.json.JSONObject;

import io.openkit.OKAchievement;
import io.openkit.OKAchievementsListResponseHandler;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OKAchievementsFragment extends ListFragment {

	private OKAchievementsListAdapter listAdapter;
	private ProgressBar spinnerBar;
	private ListView listView;
	private TextView listHeaderTextView;

	private boolean startedAchievementsRequest;

	private int numAchievements;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//We can use the same view as leaderboards-- it's just a list view with a progress spinner and a list headerview
		int viewID = getResources().getIdentifier("io_openkit_fragment_leaderboards", "layout", getActivity().getPackageName());
		View view = inflater.inflate(viewID, container, false);

		int spinnerBarID = getResources().getIdentifier("progressSpinner", "id", getActivity().getPackageName());
		int listHeaderViewID = getResources().getIdentifier("list_simple_header", "layout", getActivity().getPackageName());
		int listHeaderTextViewID = getResources().getIdentifier("headerTextView", "id", getActivity().getPackageName());

		listView = (ListView)view.findViewById(android.R.id.list);
		spinnerBar = (ProgressBar)view.findViewById(spinnerBarID);

		//Inflate the list headerview
		View listHeaderView = inflater.inflate(listHeaderViewID, null);
		listHeaderTextView = (TextView)listHeaderView.findViewById(listHeaderTextViewID);
		listView.addHeaderView(listHeaderView);

		//Only do this the first time when fragment is created
		if(!startedAchievementsRequest) {
			getAchievements();
		}

		listHeaderTextView.setText(numAchievements +  " Achievements");

		return view;
	}

	private void getAchievements()
	{
		spinnerBar.setVisibility(View.VISIBLE);

		OKAchievement.getAchievements(new OKAchievementsListResponseHandler() {

			@Override
			public void onSuccess(List<OKAchievement> achievementList) {
				listAdapter = new OKAchievementsListAdapter(OKAchievementsFragment.this.getActivity(), android.R.layout.simple_list_item_1, achievementList);

				numAchievements = achievementList.size();

				listHeaderTextView.setText(numAchievements + " Achievements");

				OKAchievementsFragment.this.setListAdapter(listAdapter);
				listView.setVisibility(View.VISIBLE);
				spinnerBar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				spinnerBar.setVisibility(View.INVISIBLE);
				if(OKAchievementsFragment.this.getActivity() != null) {
					Toast toast = Toast.makeText(OKAchievementsFragment.this.getActivity(), "Couldn't connect to server to get achivements", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		startedAchievementsRequest = true;
	}

}
