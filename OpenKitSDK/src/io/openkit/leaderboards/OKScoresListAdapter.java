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

import io.openkit.OKScore;

import java.util.List;

import com.facebook.widget.ProfilePictureView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class OKScoresListAdapter extends ArrayAdapter<OKScore>
{
	private final List<OKScore>items;
	private boolean showAlternateBG;

	public OKScoresListAdapter(Context context, int resource, List<OKScore> objects)
	{
		super(context, resource, objects);
		this.items = objects;
	}

	public List<OKScore> getItems()
	{
		return items;
	}

	public void showAlternateBGColor()
	{
		showAlternateBG =true;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;

		if(row == null){
			int listItemID = getContext().getResources().getIdentifier("io_openkit_listitem_okscore", "layout", getContext().getPackageName());
			LayoutInflater inflater = LayoutInflater.from(this.getContext());
			row = inflater.inflate(listItemID,  parent, false);
		}

		if(showAlternateBG) {
			int backgroundColorId = getContext().getResources().getIdentifier("io_openkit_player_top_score_bg", "color", getContext().getPackageName());
			int bgColor = getContext().getResources().getColor(backgroundColorId);
			row.setBackgroundColor(bgColor);
		}

		OKScore currentScore = this.getItem(position);

		int label1Id, label2Id, rankLabelId, pictureViewId;

		label1Id = getContext().getResources().getIdentifier("text1", "id", getContext().getPackageName());
		label2Id = getContext().getResources().getIdentifier("text2", "id", getContext().getPackageName());
		rankLabelId = getContext().getResources().getIdentifier("io_openkit_scoreRankTextView", "id", getContext().getPackageName());
		pictureViewId = getContext().getResources().getIdentifier("io_openkit_scorePicView", "id", getContext().getPackageName());

		TextView label1 = (TextView)row.findViewById(label1Id);
		TextView label2 = (TextView)row.findViewById(label2Id);
		TextView rankLabel = (TextView)row.findViewById(rankLabelId);
		ProfilePictureView pictureView = (ProfilePictureView)row.findViewById(pictureViewId);

		// If the display string is not null, show that, otherwise show
		// the score value as a fallback

		if(currentScore.getDisplayString() != null) {
			label2.setText(currentScore.getDisplayString());
		} else {
			label2.setText(Long.toString(currentScore.getScoreValue()));
		}

		if(currentScore.getRank() == 0) {
			rankLabel.setText("");
		} else {
			rankLabel.setText(Integer.toString(currentScore.getRank()));
		}

		pictureView.setCropped(true);

		if(currentScore.getOKUser() != null)
		{
			label1.setText(currentScore.getOKUser().getUserNick());
			pictureView.setProfileId(currentScore.getOKUser().getFBUserID());
		}

		row.setTag(position);

		return row;
	}

}
