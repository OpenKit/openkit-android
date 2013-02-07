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
	public OKScoresListAdapter(Context context, int resource, List<OKScore> objects)
	{
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		
		if(row == null){
			LayoutInflater inflater = LayoutInflater.from(this.getContext());
			row = inflater.inflate(io.openkit.R.layout.io_openkit_listitem_okscore, parent, false);
		}
		
		OKScore currentScore = this.getItem(position);
		
		TextView label1 = (TextView)row.findViewById(io.openkit.R.id.text1);
		TextView label2 = (TextView)row.findViewById(io.openkit.R.id.text2);
		TextView rankLabel = (TextView)row.findViewById(io.openkit.R.id.io_openkit_scoreRankTextView);
		ProfilePictureView pictureView = (ProfilePictureView)row.findViewById(io.openkit.R.id.io_openkit_scorePicView);
		
		label2.setText(Integer.toString(currentScore.getScoreValue()));
		rankLabel.setText(Integer.toString(currentScore.getRank()));
		pictureView.setCropped(true);
		
		if(currentScore.getOKUser() != null)
		{
			label1.setText(currentScore.getOKUser().getUserNick());
			pictureView.setProfileId(Long.toString(currentScore.getOKUser().getFBUserID()));
		}
		
		row.setTag(position);
		
		return row;
	}

}
