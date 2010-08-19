package com.pure.gestationweek;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker.OnDateChangedListener;

public class GestationWeekConfig extends Activity {
	public static final String BABY_NAME = "baby_name";
	public static final String CALC_TYPE = "calc_type";
	public static final String MAMA_MC = "mama_mc";
	public static final String MAMA_NAME = "mama_name";
	public final static String TAG = "GestationWeekConfig";
	private SharedPreferences gestationWeekConfig;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText et_mama_name;
	private EditText et_baby_name;
	private DatePicker dp_mama_mc;
	private Spinner sp_calc_type;
	private int selected_calc_type = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.gestation_week_config);
		init();
	}

	private void init() {
		gestationWeekConfig = this.getApplicationContext().getSharedPreferences(GestationWeek.GESTATION_WEEK_CONFIG, 0);

		sp_calc_type = (Spinner) findViewById(R.id.sp_calc_type);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.calc_types, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_calc_type.setAdapter(adapter);
		sp_calc_type.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selected_calc_type = sp_calc_type.getSelectedItemPosition();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		sp_calc_type.setSelection(gestationWeekConfig.getInt(CALC_TYPE, selected_calc_type));

		et_mama_name = (EditText) findViewById(R.id.et_mama_name);
		et_mama_name.setText(gestationWeekConfig.getString(MAMA_NAME, ""));

		long mamaMcLong = gestationWeekConfig.getLong(MAMA_MC, System.currentTimeMillis());
		Calendar mamaMc = Calendar.getInstance();
		mamaMc.clear();
		mamaMc.setTimeInMillis(mamaMcLong);

		dp_mama_mc = (DatePicker) findViewById(R.id.dp_mama_mc);
		dp_mama_mc.init(mamaMc.get(Calendar.YEAR), mamaMc.get(Calendar.MONTH), mamaMc.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			}
		});

		et_baby_name = (EditText) findViewById(R.id.et_baby_name);
		et_baby_name.setText(gestationWeekConfig.getString(BABY_NAME, ""));

		btn_ok = (Button) findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(btnOkListener);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoGestationWeek();
			}
		});
	}

	private void gotoGestationWeek() {
		Intent intent = new Intent(GestationWeekConfig.this, GestationWeek.class);
		startActivity(intent);
		finish();
	}

	private OnClickListener btnOkListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			String mamaName = et_mama_name.getText().toString();
			String babyName = et_baby_name.getText().toString();
			Calendar mamaMc = Calendar.getInstance();
			mamaMc.clear();
			mamaMc.set(Calendar.YEAR, dp_mama_mc.getYear());
			mamaMc.set(Calendar.MONTH, dp_mama_mc.getMonth());
			mamaMc.set(Calendar.DAY_OF_MONTH, dp_mama_mc.getDayOfMonth());
			Log.i(TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mamaMc.getTime()));
			gestationWeekConfig.edit().putString(MAMA_NAME, mamaName).putLong(MAMA_MC, mamaMc.getTime().getTime()).putInt(CALC_TYPE, selected_calc_type).putString(BABY_NAME, babyName).commit();
			gotoGestationWeek();
		}
	};
}
