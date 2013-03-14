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

import io.openkit.*;
import io.openkit.smartimageview.*;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class OKLeaderboardsListAdapter extends ArrayAdapter<OKLeaderboard> {

	public OKLeaderboardsListAdapter(Context context, int resource, List<OKLeaderboard> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		
		int text1id = getContext().getResources().getIdentifier("text1", "id", getContext().getPackageName());
		int text2id = getContext().getResources().getIdentifier("text2", "id", getContext().getPackageName());
		int smartImageViewID = getContext().getResources().getIdentifier("smartImageView", "id", getContext().getPackageName());
		int leaderboardRowID = getContext().getResources().getIdentifier("okleaderboardrow", "layout", getContext().getPackageName());
		
		if(row == null)
		{
			LayoutInflater inflater = LayoutInflater.from(this.getContext());			
			
			row = inflater.inflate(leaderboardRowID, parent, false);
			//row = inflater.inflate(R.layout.okleaderboardrow, parent, false);
		}
		

		TextView label1 = (TextView)row.findViewById(text1id);
		TextView label2 = (TextView)row.findViewById(text2id);
		//TextView label1 = (TextView)row.findViewById(R.id.text1);
		//TextView label2 = (TextView)row.findViewById(R.id.text2);
		
		SmartImageView imageView = (SmartImageView)row.findViewById(smartImageViewID);
		//SmartImageView imageView = (SmartImageView)row.findViewById(R.id.smartImageView);
		
		OKLeaderboard currentLeaderboard = this.getItem(position);
		
		row.setOnClickListener(rowClickListener);
		row.setTag(position);
		
		label1.setText(currentLeaderboard.getName());
		label2.setText(Integer.toString(currentLeaderboard.getPlayerCount()) + " players");
		
		imageView.setImageUrl(currentLeaderboard.getIconURL());
		return row;
	}
	
	public OnClickListener rowClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer)v.getTag();
			OKLeaderboard clickedLeaderboard = OKLeaderboardsListAdapter.this.getItem(position);
			
			Intent showLeaderboard = new Intent(OKLeaderboardsListAdapter.this.getContext(), OKScoresActivity.class);
			showLeaderboard.putExtra(OKLeaderboard.LEADERBOARD_KEY, clickedLeaderboard);
			
			OKLeaderboardsListAdapter.this.getContext().startActivity(showLeaderboard);
		}
	};
	


}
