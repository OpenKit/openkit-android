package io.openkit.leaderboards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.openkit.OKHTTPClient;
import io.openkit.OKLog;
import io.openkit.OKScore;
import io.openkit.OKUser;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class OKScoreCache extends SQLiteOpenHelper{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "OKCACHEDB";
	private static final String TABLE_SCORES = "OKCACHE";

	private static final String KEY_ID = "id";
	private static final String KEY_LEADERBOARD_ID = "leaderboardID";
	private static final String KEY_SCOREVALUE = "scoreValue";
	private static final String KEY_METADATA = "metadata";
	private static final String KEY_DISPLAYSTRING = "displayString";
	private static final String KEY_SUBMITTED = "submitted";

	private int numOpenedConnections = 0;

	public OKScoreCache(Context ctx)
	{
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createCacheTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SCORES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_LEADERBOARD_ID + " INTEGER, "+ KEY_SCOREVALUE + " BIGINT, " + KEY_METADATA + " INTEGER, " + KEY_DISPLAYSTRING + " VARCHAR(255), " + KEY_SUBMITTED + " BOOLEAN);";
		db.execSQL(createCacheTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
	}

	public void insertScore(OKScore score)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_DISPLAYSTRING, score.getDisplayString());
		values.put(KEY_LEADERBOARD_ID, score.getOKLeaderboardID());
		values.put(KEY_METADATA, score.getMetadata());
		values.put(KEY_SCOREVALUE, score.getScoreValue());
		values.put(KEY_SUBMITTED, score.isSubmitted());

		SQLiteDatabase db = getWritableDatabase();
		long rowID = db.insert(TABLE_SCORES, null, values);
		score.setOKScoreID((int)rowID);
		close();
		OKLog.v("Inserted score into db: " + score);
	}

	public void deleteScore(OKScore score)
	{
		if(score.getOKScoreID() <=0) {
			OKLog.v("Tried to delete a score from cache without an ID");
			return;
		}

		SQLiteDatabase db = getWritableDatabase();
	    db.delete(TABLE_SCORES , KEY_ID + " = ?",
	            new String[] { String.valueOf(score.getOKScoreID()) });
	    close();
	}

	public void updateCachedScoreSubmitted(OKScore score)
	{
		if(score.getOKScoreID() <=0) {
			OKLog.v("Tried to update a score from cache without an ID");
			return;
		}

	    ContentValues values = new ContentValues();
	    values.put(KEY_SUBMITTED, score.isSubmitted());

	    SQLiteDatabase db = getWritableDatabase();
	    db.update(TABLE_SCORES , values, KEY_ID + " = ?", new String[] { String.valueOf(score.getOKScoreID()) });
		close();
	}

	public List<OKScore> getCachedScoresForLeaderboardID(int leaderboardID, boolean submittedScoresOnly)
	{
		String queryFormat;
		if(submittedScoresOnly) {
			queryFormat = "SELECT * FROM %s WHERE leaderboardID=%d AND submitted=1";
		} else {
			queryFormat = "SELECT * FROM %s WHERE leaderboardID=%d";
		}
		String selectQuery = String.format(queryFormat, TABLE_SCORES, leaderboardID);
		return getScoresWithQuerySQL(selectQuery);
	}

	public List<OKScore> getUnsubmittedCachedScores()
	{
		String queryFormat = "SELECT * FROM %s WHERE submitted=0";
		String selectQuery = String.format(queryFormat, TABLE_SCORES);
		return getScoresWithQuerySQL(selectQuery);
	}


	public List<OKScore> getAllCachedScores()
	{
		String queryFormat = "SELECT * FROM %s";
		String selectQuery = String.format(queryFormat, TABLE_SCORES);
		return getScoresWithQuerySQL(selectQuery);
	}

	private List<OKScore> getScoresWithQuerySQL(String querySQL)
	{
		List<OKScore> scoresList = new ArrayList<OKScore>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(querySQL, null);

		if(cursor.moveToFirst()) {
			do {
				OKScore score = new OKScore();
				score.setOKScoreID(cursor.getInt(0));
				score.setOKLeaderboardID(cursor.getInt(1));
				score.setScoreValue(cursor.getLong(2));
				score.setMetadata(cursor.getInt(3));
				score.setDisplayString(cursor.getString(4));

				int submitted = cursor.getInt(5);
				if(submitted == 0) {
					score.setSubmitted(false);
				}
				else {
					score.setSubmitted(true);
				}

				scoresList.add(score);
			} while (cursor.moveToNext());
		}

		close();

		return scoresList;
	}

	public void clearCachedSubmittedScores()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_SCORES + " WHERE " + KEY_SUBMITTED + "=1");
		close();
		//logScoreCache();
	}

	// Unused for now, use clearCachedSubmittedScores() instead. This drops the entire table, where as clearCachedSubmitted() is used to clear
	// out scores that have been submitted when a userID changes
	/*
	private void clearCache()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
		this.onCreate(db);
	}*/

	/*
	private void logScoreCache()
	{
		List<OKScore> scoreList = getAllCachedScores();

		OKLog.d("Score cache contains:");
		for(int x =0; x < scoreList.size(); x++)
		{
			OKLog.d("1: " + scoreList.get(x));
		}
	}*/

	private void submitCachedScore(final OKScore score)
	{
		if(OKUser.getCurrentUser() != null) {
			score.setOKUser(OKUser.getCurrentUser());
			score.cachedScoreSubmit(new OKScore.ScoreRequestResponseHandler() {

				@Override
				public void onSuccess() {
					OKLog.v("Submitted cached score successfully: " + score);
					updateCachedScoreSubmitted(score);
				}

				@Override
				public void onFailure(Throwable error) {
					// If the server responds with an error code in the 400s, delete the score from the cache
					if(OKHTTPClient.isErrorCodeInFourHundreds(error)) {
						OKLog.v("Deleting score from cache because server responded with error code in 400s");
						deleteScore(score);
					}
				}
			});
		} else {
			OKLog.v("Tried to submit cached score without having an OKUser logged in");
		}
	}


	public void submitAllCachedScores()
	{
		if(OKUser.getCurrentUser() == null) {
			return;
		}

		List<OKScore> cachedScores = getUnsubmittedCachedScores();

		for(int x = 0; x < cachedScores.size(); x++)
		{
			OKScore score = cachedScores.get(x);
			submitCachedScore(score);
		}
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase()
	{
		numOpenedConnections++;
		return super.getReadableDatabase();
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase()
	{
		numOpenedConnections++;
		return super.getWritableDatabase();
	}

	@Override
	public synchronized void close()
	{
		numOpenedConnections--;
		if(numOpenedConnections == 0) {
			super.close();
		}
	}


	public boolean storeScoreInCacheIfBetterThanLocalCachedScores(OKScore score)
	{
		List<OKScore> cachedScores;
		// If there is a user logged in, we should compare against scores that have already been submitted to decide whether
		// to submit the new score, and not all scores. E.g. if there is an unsubmitted score for some reason that has a higher value than the
		// one to submit, we should still submit it. This is because for some reason there might be an unsubmitted score stored that will never
		// get submitted for some unknown reason.

		if(OKUser.getCurrentUser() != null) {
			cachedScores = getCachedScoresForLeaderboardID(score.getOKLeaderboardID(), true);
		} else {
			cachedScores = getCachedScoresForLeaderboardID(score.getOKLeaderboardID(), false);
		}

		if(cachedScores.size() <= 1) {
			insertScore(score);
			return true;
		} else {
			// Sort the scores in descending order
			Comparator<OKScore> descendingComparator = new Comparator<OKScore>() {
				@Override
				public int compare(OKScore s1, OKScore s2) {
			        return (s1.getScoreValue()>s2.getScoreValue() ? -1 : (s1.getScoreValue()==s2.getScoreValue() ? 0 : 1));
			    }
			};

			Collections.sort(cachedScores,descendingComparator);

			OKScore higestScore = cachedScores.get(0);
			OKScore lowestScore = cachedScores.get(cachedScores.size()-1);

			if(score.getScoreValue() > higestScore.getScoreValue()) {
				deleteScore(higestScore);
				insertScore(score);
				return true;
			} else if (score.getScoreValue() < lowestScore.getScoreValue()) {
				deleteScore(lowestScore);
				insertScore(score);
				return true;
			}
		}

		return false;
	}

}
