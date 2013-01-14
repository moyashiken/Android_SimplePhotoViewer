package com.moya.simplephotoviewer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefSetting {

	public static final int SETTING_PATH = 0;
	public static final int SETTING_CURRENT_IMAGE_PATH = 1;
	public static final int SETTING_SIZE = 2;

	private static final Object LOCK = new Object();
	static private List<String> mList;

	static public void setData(Context context, int id, String data) {
		loadFromSharedPref(context);

		if (!data.equals(mList.get(id))) {
			mList.set(id, data);
			saveToSharedPref(context);
		}
	}

	static public String getData(Context context, int id) {
		loadFromSharedPref(context);

		return mList.get(id);
	}

	static protected void loadFromSharedPref(Context context) {

		if (mList == null) {
			mList = new ArrayList<String>();

			// Must be synchronized since we can have different threads wanting
			// to write to and read from the shared preferences at the same
			// time.
			synchronized (LOCK) {
				SharedPreferences pref;
				pref = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
				int size = pref.getInt("setting_size", -1);

				if (SETTING_SIZE != size) {
					reset(context);
				} else {
					for (int i = 0; i < SETTING_SIZE; i++) {
						String data = pref.getString(Integer.toString(i), "");
						mList.add(data);
					}
				}
			}
		}
	}

	static protected void saveToSharedPref(Context context) {
		if (mList == null) {
			return;
		}
		// Must be synchronized since we can have different threads wanting
		// to write to and read from the shared preferences at the same
		// time.
		synchronized (LOCK) {
			SharedPreferences pref;
			SharedPreferences.Editor editor;
			pref = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
			editor = pref.edit();
			editor.clear();

			editor.putInt("setting_size", SETTING_SIZE);
			for (int i = 0; i < SETTING_SIZE; i++) {
				editor.putString(Integer.toString(i), mList.get(i));
			}

			editor.apply();
		}
	}

	static public void reset(Context context) {
		if (mList == null) {
			return;
		}
		mList.clear();
		mList.add("/"); // SETTING_PATH
		mList.add(""); // SETTING_CURRENT_IMAGE_PATH
	}

	static public void resetAndSave(Context context) {
		reset(context);
		saveToSharedPref(context);
	}
}
