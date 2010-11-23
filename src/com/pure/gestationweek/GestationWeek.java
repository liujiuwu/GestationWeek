package com.pure.gestationweek;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TabHost.OnTabChangeListener;

import com.pure.gestationweek.util.DiaryListViewAdapter;
import com.pure.gestationweek.util.GestationDiaryDbAdapter;
import com.pure.gestationweek.util.LogUtil;
import com.pure.gestationweek.util.Utils;

public class GestationWeek extends TabActivity {
	public final static String GESTATION_WEEK_CONFIG = "gestation_week_config";
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
	private TextView tv_mama_week;
	private TextView tv_now;
	private TextView tv_mama_now_week;
	private TextView tv_mama_now_month;
	private TextView tv_mama_ycq;
	private TextView tv_mama_djs;
	private TextView tv_mama_bmi;
	private Button btn_view_up;
	private Button btn_view_down;
	private Button btn_new_diary;
	private Button btn_clean_diary;
	private ImageView img_w1;
	private ImageView img_w2;
	private ImageView img_w3;
	private ImageView img_w4;

	private TextView tv_descn_title;
	private TextView tv_descn_content;
	private TextView tv_diary_count;
	private ListView ls_diaries;

	private TabHost mTabHost;
	private int week = 1;
	private int month = 1;
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
		init();
	}

	private void init() {
		LogUtil.show(TAG, "->init gestationweek", Log.DEBUG);
		mTabHost = getTabHost();
		Resources res = getResources();
		LayoutInflater.from(this).inflate(R.layout.gestation_week, mTabHost.getTabContentView(), true);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_1).setIndicator(getString(R.string.tab_1_name), res.getDrawable(R.drawable.ic_tab_01)).setContent(R.id.tab1_content));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_2).setIndicator(getString(R.string.tab_2_name), res.getDrawable(R.drawable.ic_tab_02)).setContent(R.id.tab2_content));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_3).setIndicator(getString(R.string.tab_3_name), res.getDrawable(R.drawable.ic_tab_03)).setContent(R.id.tab3_content));
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabTag) {
				if (TAB_1.equals(tabTag)) {
					calcGestationWeek();
				} else if (TAB_2.equals(tabTag)) {
					tv_descn_content.setText(getResources().getStringArray(R.array.month_descns)[viewMonth - 1]);
				} else if (TAB_3.equals(tabTag)) {
					renderListView();
				}
			}
		});

		tv_now = (TextView) findViewById(R.id.tv_now);
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
		btn_new_diary = (Button) findViewById(R.id.btn_new_diary);
		btn_clean_diary = (Button) findViewById(R.id.btn_clean_diary);
		tv_mama_bmi = (TextView) findViewById(R.id.tv_mama_bmi);

		Calendar now = Calendar.getInstance();
		String today = String.format("%s %s", Utils.formatDate(new Date(), getString(R.string.date_format)), getResources().getStringArray(R.array.weeks)[now.get(Calendar.DAY_OF_WEEK) - 1]);
		tv_now.setText(today);

		calcGestationWeek();

		btn_view_up.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (viewMonth > 1) {
					viewMonth--;
				} else {
					viewMonth = 10;
				}
				setViewDescn(viewMonth);
			}
		});

		btn_view_down.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (viewMonth < 10) {
					viewMonth++;
				} else {
					viewMonth = 1;
				}
				setViewDescn(viewMonth);
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
				new AlertDialog.Builder(context).setTitle("提示").setMessage("确定删除该日志吗?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
		setViewDescn(viewMonth);
	}

	private void setViewDescn(int view) {
		tv_descn_title.setText(getString(R.string.view_month_title, view));
		tv_descn_content.setText(getResources().getStringArray(R.array.month_descns)[view - 1]);
		img_w1 = (ImageView) findViewById(R.id.img_w1);
		img_w2 = (ImageView) findViewById(R.id.img_w2);
		img_w3 = (ImageView) findViewById(R.id.img_w3);
		img_w4 = (ImageView) findViewById(R.id.img_w4);

		switch (view) {
		case 1:
			img_w1.setImageResource(R.drawable.m1w1);
			img_w2.setImageResource(R.drawable.m1w2);
			img_w3.setImageResource(R.drawable.m1w3);
			img_w4.setImageResource(R.drawable.m1w4);
			break;
		case 2:
			img_w1.setImageResource(R.drawable.m2w5);
			img_w2.setImageResource(R.drawable.m2w6);
			img_w3.setImageResource(R.drawable.m2w7);
			img_w4.setImageResource(R.drawable.m2w8);
			break;
		case 3:
			img_w1.setImageResource(R.drawable.m3w9);
			img_w2.setImageResource(R.drawable.m3w10);
			img_w3.setImageResource(R.drawable.m3w11);
			img_w4.setImageResource(R.drawable.m3w12);
			break;
		case 4:
			img_w1.setImageResource(R.drawable.m4w13);
			img_w2.setImageResource(R.drawable.m4w14);
			img_w3.setImageResource(R.drawable.m4w15);
			img_w4.setImageResource(R.drawable.m4w16);
			break;
		case 5:
			img_w1.setImageResource(R.drawable.m5w17);
			img_w2.setImageResource(R.drawable.m5w18);
			img_w3.setImageResource(R.drawable.m5w19);
			img_w4.setImageResource(R.drawable.m5w20);
			break;
		case 6:
			img_w1.setImageResource(R.drawable.m6w21);
			img_w2.setImageResource(R.drawable.m6w22);
			img_w3.setImageResource(R.drawable.m6w23);
			img_w4.setImageResource(R.drawable.m6w24);
			break;
		case 7:
			img_w1.setImageResource(R.drawable.m7w25);
			img_w2.setImageResource(R.drawable.m7w26);
			img_w3.setImageResource(R.drawable.m7w27);
			img_w4.setImageResource(R.drawable.m7w28);
			break;
		case 8:
			img_w1.setImageResource(R.drawable.m8w29);
			img_w2.setImageResource(R.drawable.m8w30);
			img_w3.setImageResource(R.drawable.m8w31);
			img_w4.setImageResource(R.drawable.m8w32);
			break;
		case 9:
			img_w1.setImageResource(R.drawable.m9w33);
			img_w2.setImageResource(R.drawable.m9w34);
			img_w3.setImageResource(R.drawable.m9w35);
			img_w4.setImageResource(R.drawable.m9w36);
			break;
		case 10:
			img_w1.setImageResource(R.drawable.m10w37);
			img_w2.setImageResource(R.drawable.m10w38);
			img_w3.setImageResource(R.drawable.m10w39);
			img_w4.setImageResource(R.drawable.m10w40);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		calcGestationWeek();
		if (requestCode == CREATE_DIARY_CODE || requestCode == EDIT_DIARY_CODE) {
			mTabHost.setCurrentTabByTag(TAB_3);
			renderListView();
		}

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

		String ch = gestationWeekConfig.getString(GestationWeekConfig.MAMA_HEIGHT, "");
		String cw = gestationWeekConfig.getString(GestationWeekConfig.MAMA_WEIGHT, "");
		float chf = 0f;
		float cwf = 0f;
		if (ch != null && ch.length() > 0) {
			boolean ret = Utils.validateHeight(ch);
			if (ret) {
				chf = Float.parseFloat(ch);
			}
		}

		if (cw != null && cw.length() > 0) {
			boolean ret = Utils.validateWeight(cw);
			if (ret) {
				cwf = Float.parseFloat(cw);
			}
		}

		if (chf > 0 && cwf > 0) {
			tv_mama_bmi.setText(Utils.formatNum(cwf / (chf * chf)) + "");
		}

		Map<String, Integer> gestationWeek = Utils.getGestationWeek(calcType, mcLong);
		tv_mama_week.setText(getWeekDayDescn(gestationWeek.get(Utils.GESTATION_WEEK_DAYS)));

		week = gestationWeek.get(Utils.GESTATION_WEEK_WEEK);
		month = gestationWeek.get(Utils.GESTATION_WEEK_MONTH);
		tv_mama_now_week.setText(getString(R.string.now_week, week));
		tv_mama_now_month.setText(getString(R.string.now_month, month));
		tv_mama_ycq.setText(Utils.formatDate(new Date(gestationWeek.get(Utils.GESTATION_WEEK_YCQ) * 1000L), getString(R.string.date_format)));
		tv_mama_djs.setText(getWeekDayDescn(gestationWeek.get(Utils.GESTATION_WEEK_DJS_DAYS)));
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
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog result = null;
		Builder builder;

		switch (id) {
		case MENU_HELP:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.app_name) + getString(R.string.menu_item_help));
			builder.setMessage(getString(R.string.app_help));
			builder.setPositiveButton(getString(R.string.bt_ok), null);
			result = builder.create();
			break;
		case MENU_ABOUT:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.app_name) + " " + getString(R.string.app_version));
			builder.setMessage(getString(R.string.app_about));
			builder.setPositiveButton(getString(R.string.bt_ok), null);
			result = builder.create();
			break;
		default:
			result = super.onCreateDialog(id);
			break;
		}
		return result;
	}

}