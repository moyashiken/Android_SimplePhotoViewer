package com.moya.simplephotoviewer.util;

import android.util.Log;

public class Logger {
	static final boolean LOG_OUT = false;
	static final String APP_NAME = "SimplePhotoViewer";

	static public boolean isLogOut(){
		return LOG_OUT;
	}
	
	static public void v(String msg) {
		if (LOG_OUT) {
			Log.v(APP_NAME, msg);
		}
	}

	static public void d(String msg) {
		if (LOG_OUT) {
			Log.v(APP_NAME, msg);
		}
	}

	static public void i(String msg) {
		if (LOG_OUT) {
			Log.i(APP_NAME, msg);
		}
	}

	static public void e(String msg) {
		Log.e(APP_NAME, msg);
	}

}
