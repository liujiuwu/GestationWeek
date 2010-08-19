package com.pure.gestationweek.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.pure.gestationweek.R;

public class DiaryListViewAdapter extends SimpleCursorAdapter {

	public DiaryListViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tv_diary_title = (TextView) view.findViewById(R.id.diary_title);
		TextView tv_diary_date = (TextView) view.findViewById(R.id.diary_date);
		String title = cursor.getString(1);
		if (title != null) {
			if (title.length() > 15) {
				title = title.substring(0, 15) + "...";
			}
		}
		tv_diary_title.setText(title);
		tv_diary_date.setText(new SimpleDateFormat("MM月dd日 HH时mm分").format(new Date((cursor.getInt(3) * 1000L))));
	}
}
