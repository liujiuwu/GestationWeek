package com.pure.gestationweek.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GestationDiaryDbAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_DIARY_TITLE = "diary_title";
	public static final String KEY_DIARY_CONTENT = "diary_content";
	public static final String KEY_DIARY_DATE = "diary_date";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_TABLE = "gestation_diaries";
	private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (_id integer primary key autoincrement, " + "diary_title text not null,diary_content text,diary_date integer);";
	private static final String DATABASE_NAME = "gestation_week";
	private static final int DATABASE_VERSION = 1;
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}

	public GestationDiaryDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public void createTable() {
		mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		mDb.execSQL(DATABASE_CREATE);
	}

	public GestationDiaryDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createDiary(String diaryTitle, String diaryContent, int diaryDate) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DIARY_TITLE, diaryTitle);
		initialValues.put(KEY_DIARY_CONTENT, diaryContent);
		initialValues.put(KEY_DIARY_DATE, diaryDate);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean deleteDiary(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor getAllDiaries() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_DIARY_TITLE, KEY_DIARY_CONTENT, KEY_DIARY_DATE }, null, null, null, null, "diary_date desc");
	}

	public Cursor getDiary(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_DIARY_TITLE, KEY_DIARY_CONTENT, KEY_DIARY_DATE }, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public boolean updateDiary(long rowId, String diaryTitle, String diaryContent) {
		ContentValues args = new ContentValues();
		args.put(KEY_DIARY_TITLE, diaryTitle);
		args.put(KEY_DIARY_CONTENT, diaryContent);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
