package com.pure.gestationweek.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class Utils {
	public static final String GESTATION_WEEK_MONTH = "gestationWeek_month";
	public static final String GESTATION_WEEK_DJS_DAYS = "gestationWeek_djs_days";
	public static final String GESTATION_WEEK_YCQ = "gestationWeek_ycq";
	public static final String GESTATION_WEEK_WEEK = "gestationWeek_week";
	public static final String GESTATION_WEEK_DAYS = "gestationWeek_days";
	private static final int ONE_DAY = 86400000;

	public static String formatDate(Date date) {
		return formatDate(date, "yyyy-MM-dd");
	}

	public static String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	public static int distanceDay(Calendar st, Calendar et) {
		st.clear(Calendar.HOUR_OF_DAY);
		st.clear(Calendar.MINUTE);
		st.clear(Calendar.SECOND);
		st.clear(Calendar.MILLISECOND);

		et.clear(Calendar.HOUR_OF_DAY);
		et.clear(Calendar.MINUTE);
		et.clear(Calendar.SECOND);
		et.clear(Calendar.MILLISECOND);
		return (int) ((et.getTime().getTime() - st.getTime().getTime()) / ONE_DAY);
	}

	public static int distanceMonth(Calendar st, Calendar et) {
		st.clear(Calendar.HOUR_OF_DAY);
		st.clear(Calendar.MINUTE);
		st.clear(Calendar.SECOND);
		st.clear(Calendar.MILLISECOND);

		et.clear(Calendar.HOUR_OF_DAY);
		et.clear(Calendar.MINUTE);
		et.clear(Calendar.SECOND);
		et.clear(Calendar.MILLISECOND);
		return (et.get(Calendar.YEAR) - st.get(Calendar.YEAR)) * 12 + (et.get(Calendar.MONTH) - st.get(Calendar.MONTH));
	}

	public static int inMonth(Calendar mc, Calendar now) {
		return distanceMonth(mc, now) + 1;
	}

	public static Map<String, Integer> getGestationWeek(int calcType, long mcLong) {
		Map<String, Integer> gestationWeek = new HashMap<String, Integer>();
		Calendar mc = Calendar.getInstance();
		mc.setTimeInMillis(mcLong);

		Calendar now = Calendar.getInstance();
		int days = Utils.distanceDay(mc, now) + 1;
		int week = days / 7;
		int day = days % 7;

		gestationWeek.put(GESTATION_WEEK_DAYS, days);
		if (day > 0) {
			week += 1;
		}
		gestationWeek.put(GESTATION_WEEK_WEEK, week);

		int month = Utils.inMonth(mc, now);
		gestationWeek.put(GESTATION_WEEK_MONTH, month);

		if (calcType == 0) {
			mc.add(Calendar.DAY_OF_YEAR, 280);
		} else {
			mc.add(Calendar.MONTH, 9);
			mc.add(Calendar.DAY_OF_MONTH, 7);
		}
		gestationWeek.put(GESTATION_WEEK_YCQ, (int) (mc.getTime().getTime() / 1000L));

		days = Utils.distanceDay(now, mc);
		gestationWeek.put(GESTATION_WEEK_DJS_DAYS, days);
		return gestationWeek;
	}
}
