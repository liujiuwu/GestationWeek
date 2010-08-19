package com.pure.gestationweek;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.pure.gestationweek.util.GestationDiaryDbAdapter;

public class GestationDiary extends Activity {
	private Button btn_save_diary;
	private Button btn_diary_cancel;
	private EditText et_diary_title;
	private EditText et_diary_content;
	private Long mRowId;
	private GestationDiaryDbAdapter dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.gestation_diary);
		dbHelper = new GestationDiaryDbAdapter(this);
		dbHelper.open();

		btn_save_diary = (Button) findViewById(R.id.btn_save_diary);
		btn_diary_cancel = (Button) findViewById(R.id.btn_diary_cancel);
		et_diary_title = (EditText) findViewById(R.id.et_diary_title);
		et_diary_content = (EditText) findViewById(R.id.et_diary_content);

		mRowId = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String diaryTitle = extras.getString(GestationDiaryDbAdapter.KEY_DIARY_TITLE);
			String diaryContent = extras.getString(GestationDiaryDbAdapter.KEY_DIARY_CONTENT);
			mRowId = extras.getLong(GestationDiaryDbAdapter.KEY_ROWID);

			if (diaryTitle != null) {
				et_diary_title.setText(diaryTitle);
			}
			if (diaryContent != null) {
				et_diary_content.setText(diaryContent);
			}
		}

		btn_save_diary.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String diaryTitle = et_diary_title.getText().toString();
				String diaryContent = et_diary_content.getText().toString();
				Calendar now = Calendar.getInstance();
				int diaryDate = (int) (now.getTime().getTime() / 1000L);
				if (mRowId != null) {
					dbHelper.updateDiary(mRowId, diaryTitle, diaryContent);
				} else {
					dbHelper.createDiary(diaryTitle, diaryContent, diaryDate);
				}
				gotoGestationWeek();
			}
		});

		btn_diary_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoGestationWeek();
			}
		});
	}

	private void gotoGestationWeek() {
		Intent mIntent = new Intent();
		setResult(RESULT_OK, mIntent);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}

}
