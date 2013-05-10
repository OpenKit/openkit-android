package io.openkit.leaderboards;

import java.util.List;

import io.openkit.OKAchievement;
import io.openkit.smartimageview.SmartImageView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OKAchievementsListAdapter extends ArrayAdapter<OKAchievement> {
	
	public OKAchievementsListAdapter(Context context, int resource, List<OKAchievement> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		
		int text1id = getContext().getResources().getIdentifier("text1", "id", getContext().getPackageName());
		int text2id = getContext().getResources().getIdentifier("text2", "id", getContext().getPackageName());
		int progressID = getContext().getResources().getIdentifier("io_openkit_achievement_progress", "id", getContext().getPackageName());
		int smartImageViewID = getContext().getResources().getIdentifier("smartImageView", "id", getContext().getPackageName());
		int leaderboardRowID = getContext().getResources().getIdentifier("io_openkit_row_okachievement", "layout", getContext().getPackageName());
		
		if(row == null)
		{
			LayoutInflater inflater = LayoutInflater.from(this.getContext());			
			row = inflater.inflate(leaderboardRowID, parent, false);
		}
		
		TextView label1 = (TextView)row.findViewById(text1id);
		TextView label2 = (TextView)row.findViewById(text2id);
		SmartImageView imageView = (SmartImageView)row.findViewById(smartImageViewID);
		ProgressBar progressBar = (ProgressBar)row.findViewById(progressID);
		
		OKAchievement currentAchievement = this.getItem(position);
		
		row.setTag(position);
		
		label1.setText(currentAchievement.getName());
		label2.setText(currentAchievement.getDescription());
		
		progressBar.setMax(currentAchievement.getGoal());
		progressBar.setProgress(currentAchievement.getProgress());
		
		if(currentAchievement.getProgress() >= currentAchievement.getGoal()) {
			imageView.setImageUrl(currentAchievement.getUnlockedIconURL());
		} else {
			imageView.setImageUrl(currentAchievement.getLockedIconURL());
		}
			
		return row;
	}

}
