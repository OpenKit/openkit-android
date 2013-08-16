package io.openkit.leaderboards;

import java.util.ArrayList;
import java.util.List;

import io.openkit.OKLog;
import io.openkit.OKScore;
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

		SQLiteDatabase db = this.getWritableDatabase();
		db.insert(TABLE_SCORES, null, values);
		db.close();
		OKLog.v("Inserted score into db: " + score);
	}

	public List<OKScore> getAllCachedScores()
	{
		String selectQuery = "SELECT * FROM " + TABLE_SCORES;
		return getScoresWithQuerySQL(selectQuery);
	}

	public List<OKScore> getScoresWithQuerySQL(String querySQL)
	{
		List<OKScore> scoresList = new ArrayList<OKScore>();

		SQLiteDatabase db = this.getReadableDatabase();
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
		return scoresList;
	}




}
