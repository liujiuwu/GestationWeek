package com.pure.gestationweek.widget;

import java.util.Date;
import java.util.Map;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.View;
import android.widget.RemoteViews;

import com.pure.gestationweek.GestationWeek;
import com.pure.gestationweek.GestationWeekConfig;
import com.pure.gestationweek.R;
import com.pure.gestationweek.util.Utils;

public class GestationWeekWidget extends AppWidgetProvider {
	private SharedPreferences gestationWeekConfig;
	public static final String ACTION_NEXT_TIP = "com.android.misterwidget.NEXT_TIP";
	public static final String ACTION_POKE = "com.android.misterwidget.HEE_HEE";

	public static final String EXTRA_TIMES = "times";

	public static final String PREFS_NAME = "Protips";
	public static final String PREFS_TIP_NUMBER = "widget_tip";

	private int mIconRes = R.drawable.droidman_open;
	private AppWidgetManager mWidgetManager = null;
	private int[] mWidgetIds;
	private Context mContext;

	private void setup(Context context) {
		mContext = context;
		mWidgetManager = AppWidgetManager.getInstance(context);
		mWidgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(context, GestationWeekWidget.class));
		gestationWeekConfig = context.getSharedPreferences(GestationWeek.GESTATION_WEEK_CONFIG, 0);
	}

	public void goodmorning() {
		try {
			setIcon(R.drawable.droidman_down_closed);
			Thread.sleep(500);
			setIcon(R.drawable.droidman_down_open);
			Thread.sleep(200);
			setIcon(R.drawable.droidman_down_closed);
			Thread.sleep(100);
			setIcon(R.drawable.droidman_down_open);
			Thread.sleep(600);
		} catch (InterruptedException ex) {
		}
		mIconRes = R.drawable.droidman_open;
		refresh();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		setup(context);

		if (intent.getAction().equals(ACTION_NEXT_TIP)) {
			SharedPreferences.Editor pref = context.getSharedPreferences(PREFS_NAME, 0).edit();
			pref.commit();
			refresh();
		} else if (intent.getAction().equals(ACTION_POKE)) {
			blink(intent.getIntExtra(EXTRA_TIMES, 1));
		} else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
			goodmorning();
		} else {
			mIconRes = R.drawable.droidman_open;
			refresh();
		}
	}

	private void refresh() {
		RemoteViews rv = buildUpdate(mContext);
		for (int i : mWidgetIds) {
			mWidgetManager.updateAppWidget(i, rv);
		}
	}

	private void setIcon(int resId) {
		mIconRes = resId;
		refresh();
	}

	private void blink(int blinks) {
		setIcon(R.drawable.droidman_closed);
		try {
			Thread.sleep(100);
			while (0 < --blinks) {
				setIcon(R.drawable.droidman_open);
				Thread.sleep(200);
				setIcon(R.drawable.droidman_closed);
				Thread.sleep(100);
			}
		} catch (InterruptedException ex) {
		}
		setIcon(R.drawable.droidman_open);
	}

	public RemoteViews buildUpdate(Context context) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		Resources res = context.getResources();

		Intent bcast = new Intent(context, GestationWeekWidget.class);
		bcast.setAction(ACTION_NEXT_TIP);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, bcast, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.tip_bubble, pending);

		bcast = new Intent(context, GestationWeekWidget.class);
		bcast.setAction(ACTION_POKE);
		bcast.putExtra(EXTRA_TIMES, 1);
		pending = PendingIntent.getBroadcast(context, 0, bcast, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.bugdroid, pending);

		int calcType = gestationWeekConfig.getInt(GestationWeekConfig.CALC_TYPE, 0);
		long mcLong = gestationWeekConfig.getLong(GestationWeekConfig.MAMA_MC, System.currentTimeMillis());
		Map<String, Integer> gestationWeek = Utils.getGestationWeek(calcType, mcLong);

		String ycq = Utils.formatDate(new Date(gestationWeek.get(Utils.GESTATION_WEEK_YCQ) * 1000L), res.getString(R.string.date_format));
		String yhy = getWeekDayDescn(context, gestationWeek.get(Utils.GESTATION_WEEK_DAYS));
		String djs = getWeekDayDescn(context, gestationWeek.get(Utils.GESTATION_WEEK_DJS_DAYS));
		String week = res.getString(R.string.now_week, gestationWeek.get(Utils.GESTATION_WEEK_WEEK));
		String month = res.getString(R.string.now_month, gestationWeek.get(Utils.GESTATION_WEEK_MONTH));

		String msg = res.getString(R.string.gestation_week_tip, ycq, yhy, week, month, djs);
		updateViews.setTextViewText(R.id.tip_message, msg);
		updateViews.setViewVisibility(R.id.tip_bubble, View.VISIBLE);
		updateViews.setImageViewResource(R.id.bugdroid, mIconRes);
		return updateViews;
	}

	private String getWeekDayDescn(Context context, int days) {
		Resources res = context.getResources();
		int week = days / 7;
		int day = days % 7;
		String weekDescn = context.getResources().getString(R.string.week_descn, week);
		String dayDescn = (day > 0) ? res.getString(R.string.day_descn, day) : "";
		return (weekDescn + dayDescn + res.getString(R.string.days_descn, days));
	}
}
