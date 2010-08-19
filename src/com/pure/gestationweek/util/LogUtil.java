package com.pure.gestationweek.util;

import android.util.Log;

public class LogUtil {
	public static boolean logOff = false;

	public static void show(String tag, String msg, int level) {
		if (!logOff) {
			switch (level) {
			case Log.DEBUG:
				Log.d(tag, msg);
				break;
			case Log.ERROR:
				Log.e(tag, msg);
				break;
			case Log.INFO:
				Log.i(tag, msg);
				break;
			case Log.VERBOSE:
				Log.v(tag, msg);
				break;
			case Log.WARN:
				Log.w(tag, msg);
				break;
			}
		}
	}
}
