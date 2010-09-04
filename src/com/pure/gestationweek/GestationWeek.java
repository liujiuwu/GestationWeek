package com.pure.gestationweek;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost.OnTabChangeListener;

import com.pure.gestationweek.util.DiaryListViewAdapter;
import com.pure.gestationweek.util.GestationDiaryDbAdapter;
import com.pure.gestationweek.util.LogUtil;
import com.pure.gestationweek.util.Utils;

public class GestationWeek extends TabActivity {
	public final static String GESTATION_WEEK_CONFIG = "gestation_week_config";
	private static final String TAB_4 = "tab_4";
	private static final String TAB_3 = "tab_3";
	private static final String TAB_2 = "tab_2";
	private static final String TAB_1 = "tab_1";
	private static final int CONFIG_CODE = 1;
	private static final int CREATE_DIARY_CODE = 2;
	private static final int EDIT_DIARY_CODE = 3;
	private final static String TAG = "GestationWeek";
	private static final int MENU_CONFIG = Menu.FIRST;
	private static final int MENU_HELP = MENU_CONFIG + 1;
	private static final int MENU_ABOUT = MENU_HELP + 1;
	private static final int MENU_EXIT = MENU_ABOUT + 1;
	private SharedPreferences gestationWeekConfig;
	private TextView tv_mama_name;
	private TextView tv_mama_week;
	private TextView tv_now;
	private TextView tv_mama_now_week;
	private TextView tv_mama_now_month;
	private TextView tv_mama_ycq;
	private TextView tv_mama_djs;
	private RadioButton rb_view_week;
	private RadioButton rb_view_month;
	private Button btn_view_up;
	private Button btn_view_down;
	private Button btn_new_diary;
	private Button btn_clean_diary;

	private TextView tv_descn_title;
	private TextView tv_descn_content;
	private TextView tv_diary_count;
	private ListView ls_diaries;

	private TabHost mTabHost;
	private int viewType = 0;
	private int week = 1;
	private int month = 1;
	private int viewWeek = 1;
	private int viewMonth = 1;
	private Context context;

	private GestationDiaryDbAdapter dbHelper;
	private Cursor mDiaryCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		dbHelper = new GestationDiaryDbAdapter(this);
		dbHelper.open();

		gestationWeekConfig = this.getApplicationContext().getSharedPreferences(GESTATION_WEEK_CONFIG, 0);
		if (gestationWeekConfig == null || "".equals(gestationWeekConfig.getString(GestationWeekConfig.MAMA_NAME, ""))) {
			gotoGestationWeekConfigActivity();
			return;
		}
		init();
	}

	private void init() {
		LogUtil.show(TAG, "->init gestationweek", Log.DEBUG);
		mTabHost = getTabHost();
		Resources res = getResources();
		LayoutInflater.from(this).inflate(R.layout.gestation_week, mTabHost.getTabContentView(), true);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_1).setIndicator(getString(R.string.tab_1_name), res.getDrawable(R.drawable.heat)).setContent(R.id.tab1_content));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_2).setIndicator(getString(R.string.tab_2_name)).setContent(R.id.tab2_content));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_3).setIndicator(getString(R.string.tab_3_name)).setContent(R.id.tab3_content));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_4).setIndicator(getString(R.string.tab_4_name)).setContent(R.id.tab4_content));
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabTag) {
				if (TAB_1.equals(tabTag)) {
					calcGestationWeek();
				} else if (TAB_2.equals(tabTag)) {
					tv_descn_content.setText(getResources().getStringArray(R.array.week_descns)[viewWeek - 1]);
				} else if (TAB_3.equals(tabTag)) {
					renderListView();
				}
			}
		});

		tv_now = (TextView) findViewById(R.id.tv_now);
		tv_mama_name = (TextView) findViewById(R.id.tv_mama_name);
		tv_mama_week = (TextView) findViewById(R.id.tv_mama_week);
		tv_mama_now_week = (TextView) findViewById(R.id.tv_mama_now_week);
		tv_mama_now_month = (TextView) findViewById(R.id.tv_mama_now_month);
		tv_mama_ycq = (TextView) findViewById(R.id.tv_mama_ycq);
		tv_mama_djs = (TextView) findViewById(R.id.tv_mama_djs);
		ls_diaries = (ListView) findViewById(R.id.ls_diaries);
		tv_descn_title = (TextView) findViewById(R.id.tv_descn_title);
		tv_descn_content = (TextView) findViewById(R.id.tv_descn_content);
		tv_diary_count = (TextView) findViewById(R.id.tv_diary_count);
		btn_view_up = (Button) findViewById(R.id.btn_view_up);
		btn_view_down = (Button) findViewById(R.id.btn_view_down);
		rb_view_week = (RadioButton) findViewById(R.id.rb_view_week);
		rb_view_month = (RadioButton) findViewById(R.id.rb_view_month);
		btn_new_diary = (Button) findViewById(R.id.btn_new_diary);
		btn_clean_diary = (Button) findViewById(R.id.btn_clean_diary);

		Calendar now = Calendar.getInstance();
		String today = String.format("%s %s", Utils.formatDate(new Date(), getString(R.string.date_format)), getResources().getStringArray(R.array.weeks)[now.get(Calendar.DAY_OF_WEEK) - 1]);
		tv_now.setText(today);
		tv_mama_name.setText(gestationWeekConfig.getString(GestationWeekConfig.MAMA_NAME, ""));

		calcGestationWeek();

		btn_view_up.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (rb_view_week.isChecked()) {
					if (viewWeek > 1) {
						viewWeek--;
					} else {
						viewWeek = 40;
					}
					setViewDescn(viewWeek);
				} else {
					if (viewMonth > 1) {
						viewMonth--;
					} else {
						viewMonth = 10;
					}
					setViewDescn(viewMonth);
				}
			}
		});

		btn_view_down.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (rb_view_week.isChecked()) {
					if (viewWeek < 40) {
						viewWeek++;
					} else {
						viewWeek = 1;
					}
					setViewDescn(viewWeek);
				} else {
					if (viewMonth < 10) {
						viewMonth++;
					} else {
						viewMonth = 1;
					}
					setViewDescn(viewMonth);
				}
			}
		});

		rb_view_week.setChecked(true);
		rb_view_week.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					viewType = 0;
					viewWeek = week;
					btn_view_up.setText(getString(R.string.btn_up_week));
					btn_view_down.setText(getString(R.string.btn_down_week));
					setViewDescn(viewWeek);
				}
			}
		});

		rb_view_month.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					viewType = 1;
					viewMonth = month;
					btn_view_up.setText(getString(R.string.btn_up_month));
					btn_view_down.setText(getString(R.string.btn_down_month));
					setViewDescn(viewMonth);
				}
			}
		});

		btn_new_diary.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoCreateDiaryActivity();
			}
		});

		btn_clean_diary.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(context).setTitle("提示").setMessage("确定清除所有日志吗?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dbHelper.createTable();
						renderListView();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
			}
		});

		ls_diaries.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor c = mDiaryCursor;
				c.moveToPosition(position);
				Intent editDiary = new Intent(context, GestationDiary.class);
				editDiary.putExtra(GestationDiaryDbAdapter.KEY_ROWID, id);
				editDiary.putExtra(GestationDiaryDbAdapter.KEY_DIARY_TITLE, c.getString(c.getColumnIndexOrThrow(GestationDiaryDbAdapter.KEY_DIARY_TITLE)));
				editDiary.putExtra(GestationDiaryDbAdapter.KEY_DIARY_CONTENT, c.getString(c.getColumnIndexOrThrow(GestationDiaryDbAdapter.KEY_DIARY_CONTENT)));
				startActivityForResult(editDiary, EDIT_DIARY_CODE);
			}
		});

		ls_diaries.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
				new AlertDialog.Builder(context).setTitle("提示").setMessage("确定该日志吗?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dbHelper.deleteDiary(id);
						renderListView();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
				return true;
			}
		});
		setViewDescn(viewWeek);
	}

	private void setViewDescn(int view) {
		if (viewType == 0) {
			tv_descn_title.setText(getString(R.string.view_week_title, view));
			tv_descn_content.setText(getResources().getStringArray(R.array.week_descns)[view - 1]);
		} else {
			tv_descn_title.setText(getString(R.string.view_month_title, view));
			tv_descn_content.setText(getResources().getStringArray(R.array.month_descns)[view - 1]);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/*calcGestationWeek();

		if (requestCode == CREATE_DIARY_CODE || requestCode == EDIT_DIARY_CODE) {
			mTabHost.setCurrentTabByTag(TAB_3);
			renderListView();
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_CONFIG, 0, getString(R.string.menu_item_config)).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_HELP, 0, getString(R.string.menu_item_help)).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, MENU_ABOUT, 0, getString(R.string.menu_item_about)).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_EXIT, 0, getString(R.string.menu_item_exit)).setIcon(android.R.drawable.ic_lock_power_off);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CONFIG:
			gotoGestationWeekConfigActivity();
			return true;
		case MENU_HELP:
			showDialog(MENU_HELP);
			return true;
		case MENU_ABOUT:
			showDialog(MENU_ABOUT);
			return true;
		case MENU_EXIT:
			finish();
			return true;
		}
		return false;
	}

	private void gotoGestationWeekConfigActivity() {
		Intent intent = new Intent(GestationWeek.this, GestationWeekConfig.class);
		startActivityForResult(intent, CONFIG_CODE);
	}

	private void gotoCreateDiaryActivity() {
		Intent intent = new Intent(GestationWeek.this, GestationDiary.class);
		startActivityForResult(intent, CREATE_DIARY_CODE);
	}

	private void calcGestationWeek() {
		int calcType = gestationWeekConfig.getInt(GestationWeekConfig.CALC_TYPE, 0);
		long mcLong = gestationWeekConfig.getLong(GestationWeekConfig.MAMA_MC, System.currentTimeMillis());

		Map<String, Integer> gestationWeek = Utils.getGestationWeek(calcType, mcLong);
		tv_mama_week.setText(getWeekDayDescn(gestationWeek.get(Utils.GESTATION_WEEK_DAYS)));

		week = gestationWeek.get(Utils.GESTATION_WEEK_WEEK);
		month = gestationWeek.get(Utils.GESTATION_WEEK_MONTH);
		tv_mama_now_week.setText(getString(R.string.now_week, week));
		tv_mama_now_month.setText(getString(R.string.now_month, month));
		tv_mama_ycq.setText(Utils.formatDate(new Date(gestationWeek.get(Utils.GESTATION_WEEK_YCQ) * 1000L), getString(R.string.date_format)));
		tv_mama_djs.setText(getWeekDayDescn(gestationWeek.get(Utils.GESTATION_WEEK_DJS_DAYS)));
		viewWeek = week;
		viewMonth = month;
	}

	private String getWeekDayDescn(int days) {
		int week = days / 7;
		int day = days % 7;
		String weekDescn = getString(R.string.week_descn, week);
		String dayDescn = (day > 0) ? getString(R.string.day_descn, day) : "";
		return (weekDescn + dayDescn + getString(R.string.days_descn, days));
	}

	private void renderListView() {
		mDiaryCursor = dbHelper.getAllDiaries();
		this.startManagingCursor(mDiaryCursor);
		String[] from = new String[] { GestationDiaryDbAdapter.KEY_DIARY_TITLE, GestationDiaryDbAdapter.KEY_DIARY_DATE };
		int[] to = new int[] { R.id.diary_title, R.id.diary_date };
		DiaryListViewAdapter diaries = new DiaryListViewAdapter(this, R.layout.diary_row, mDiaryCursor, from, to);
		ls_diaries.setAdapter(diaries);
		tv_diary_count.setText(getString(R.string.diary_count, ls_diaries.getCount()));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}

}