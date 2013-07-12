package io.openkit.example.oksampleapp;

import io.openkit.*;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SubmitScoreActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_submit_score);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_submit_score, menu);
		return true;
	}

	public void submitScoreLevel1(View v) {
		// The leaderboard ID for this leaderboard is from the OpenKit dashboard
	    promptForScoreAndSubmit(25);
	}

	public void submitScoreLevel2(View v) {
		// The leaderboard ID for this leaderboard is from the OpenKit dashboard
	    promptForScoreAndSubmit(26);
	}

	public void submitScoreLevel3(View v) {
		// The leaderboard ID for this leaderboard is from the OpenKit dashboard
	    promptForScoreAndSubmit(27);
	}

	public void submitScore(final OKScore scoreToSubmit)
	{
		scoreToSubmit.submitScore(new OKScore.ScoreRequestResponseHandler() {

			@Override
			public void onSuccess() {
				Toast.makeText(SubmitScoreActivity.this, "Submitted score successfully with value: " + scoreToSubmit.getScoreValue(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(Throwable error) {
				Toast.makeText(SubmitScoreActivity.this, "Failed to submit score with error: " + error, Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * Display an alert dialog to get a score value, and then submit a score to the given leaderboard
	 * @param leaderboardID Leaderboard ID taken from the OpenKit dashboard
	 */
	private void promptForScoreAndSubmit(final int leaderboardID)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Submit Score");
		alert.setMessage("Enter a score value");

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		alert.setView(input);

		alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			String value = input.getText().toString();

			//Create the score, set it's score value, and set the leaderboard ID which is passed in as a parameter
			OKScore score = new OKScore();
			score.setScoreValue(Long.parseLong(value));

			//Set the metadata to 1 + the score value
			score.setMetadata((int)score.getScoreValue() + 1);

			//Set the display string to include the unit "OpenKit Points"
			score.setDisplayString(score.getScoreValue() + " OpenKit points");
			score.setOKLeaderboardID(leaderboardID);
			submitScore(score);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  @Override
		public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled
			 return;
		  }
		});

		alert.show();
	}

}
