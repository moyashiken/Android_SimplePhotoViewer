package com.moya.simplephotoviewer;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moya.simplephotoviewer.util.Logger;
import com.moya.simplephotoviewer.util.Util;

public class ImageView extends Activity implements OnClickListener {

	ImageView mImageView;
	Button btn_image_previous;
	Button btn_image_next;
	ImageScalingView mImageScalingView;
	Button btn_scale_fit;
	Button btn_scale_dot;

	int mImageNumber = -1;
	int mImageCount = -1;
	private String mFilaPath;

	ExifData mExif;
	ExifData mExifOld;

	List<TextView> mExifTextViewList = new ArrayList<TextView>();

	SDCardBroadcastReceiver mSDCardBroadcastReceiver = new SDCardBroadcastReceiver();

	public class SDCardBroadcastReceiver extends BroadcastReceiver {
		public SDCardBroadcastReceiver() {
			super();
		}

		public void onReceive(Context context, Intent intent) {
			Logger.d("SDCardBroadCastReceiver:onReceive " + intent.getAction());
			finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_image_view);

		mFilaPath = SharedPrefSetting.getData(this, SharedPrefSetting.SETTING_CURRENT_IMAGE_PATH);
		mImageNumber = Util.getNumber(mFilaPath);
		mImageCount = Util.getImageFileCount(Util.getDirectory(mFilaPath));

		int width;
		int height;
		Configuration config = getResources().getConfiguration();
		Display display = getWindowManager().getDefaultDisplay();
		width = display.getWidth(); // deprecated
		height = display.getHeight(); // deprecated
		int orientation = 0;

		if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			width = (int) (height * (float) (3.0 / 2.0));
			if ((display.getWidth() - width) < 70) {
				width = display.getWidth() - 70;
			}
			orientation = LinearLayout.HORIZONTAL;
		} else {
			height = (int) (width * (float) (3.0 / 2.0));
			if ((display.getHeight() - height) < 70) {
				height = display.getHeight() - 70;
			}
			orientation = LinearLayout.VERTICAL;
		}

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		layoutParams.setMargins(0, 0, 0, 0);
		layoutParams.width = width;
		layoutParams.height = height;

		mImageScalingView = new ImageScalingView(this);
		mImageScalingView.setFilePath(mFilaPath);

		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(orientation);

		LayoutInflater inflater = LayoutInflater.from(this);
		View imageControlView = inflater.inflate(R.layout.image_control, null);

		btn_image_previous = (Button) imageControlView.findViewById(R.id.button_image_previous);
		btn_image_previous.setOnClickListener(this);
		btn_image_next = (Button) imageControlView.findViewById(R.id.button_image_next);
		btn_image_next.setOnClickListener(this);

		btn_scale_fit = (Button) imageControlView.findViewById(R.id.button_scale_fit);
		btn_scale_fit.setOnClickListener(this);
		btn_scale_dot = (Button) imageControlView.findViewById(R.id.button_scale_dot);
		btn_scale_dot.setOnClickListener(this);

		mImageScalingView.setLayoutParams(layoutParams);
		mImageScalingView.setBackgroundColor(0xff000020);

		linearLayout.addView(mImageScalingView);
		linearLayout.addView(imageControlView);

		setContentView(linearLayout);

	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		registerReceiver(mSDCardBroadcastReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mSDCardBroadcastReceiver);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public void onStart() {
		super.onStart();

		setInfo();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		SharedPrefSetting.setData(this, SharedPrefSetting.SETTING_CURRENT_IMAGE_PATH, mFilaPath);

		this.finish();

		Intent i = new Intent(this, ImageView.class);
		this.startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_image_view, menu);
		return true;
	}

	private void setInfo() {

		TextView tv = (TextView) findViewById(R.id.text_number);
		tv.setText(mImageCount - mImageNumber + "/" + mImageCount);

		tv = (TextView) findViewById(R.id.text_filename);
		tv.setText(Util.getFilename(mFilaPath));

		showExif();

		if (mExif != null) {
			tv = (TextView) findViewById(R.id.text_date);
			tv.setText(mExif.getDate());

			tv = (TextView) findViewById(R.id.text_time);
			tv.setText(mExif.getTime());
		}

		File file = new File(mFilaPath);
		BigDecimal bi = new BigDecimal(((double) file.length() / (1024 * 1024)));
		tv = (TextView) findViewById(R.id.text_filesize);
		tv.setText(bi.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + "MB");

		if (mImageNumber == 0) {
			btn_image_previous.setEnabled(true);
			btn_image_next.setEnabled(false);
		} else if ((mImageCount - 1) == mImageNumber) {
			btn_image_previous.setEnabled(false);
			btn_image_next.setEnabled(true);
		} else {
			btn_image_previous.setEnabled(true);
			btn_image_next.setEnabled(true);
		}

	}

	public void onClick(View v) {
		String dir = SharedPrefSetting.getData(this, SharedPrefSetting.SETTING_PATH);

		if (v == btn_image_previous) {

			++mImageNumber;

			if (mImageNumber >= Util.getImageFileCount(dir)) {
				mImageNumber = Util.getImageFileCount(dir) - 1;
				return;
			}
			mFilaPath = Util.getFilePathFromNumber(dir, mImageNumber);

			mImageScalingView.LoadImage(mFilaPath);
			mImageScalingView.invalidate();
			setInfo();

		} else if (v == btn_image_next) {

			--mImageNumber;
			if (mImageNumber < 0) {
				mImageNumber = 0;
				return;
			}
			mFilaPath = Util.getFilePathFromNumber(dir, mImageNumber);

			mImageScalingView.LoadImage(mFilaPath);
			mImageScalingView.invalidate();
			setInfo();
		} else if (v == btn_scale_fit) {
			mImageScalingView.setScaleFit();
		} else if (v == btn_scale_dot) {
			mImageScalingView.setScaleDot();
		}

	}

	private void showExif() {
		mExifOld = mExif;
		mExif = ExifData.getExif(mFilaPath);

		setExifTextView();
	}

	private void setExifTextView() {

		if (mExifTextViewList.size() <= 0) {
			LinearLayout layout = (LinearLayout) this.findViewById(R.id.exif_area);

			for (int cnt = 0; cnt < ExifData.EXIF_DATA_COUNT; ++cnt) {
				TextView tv = new TextView(getApplicationContext());

				// TextViewを入れるLinearLayoutを準備
				LinearLayout ll = new LinearLayout(this);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				lp.setMargins(3, 5, 1, 0);
				tv.setLayoutParams(lp);
				ll.addView(tv);

				layout.addView(ll);
				mExifTextViewList.add(tv);
			}
		}
		for (int cnt = 0; cnt < ExifData.EXIF_DATA_COUNT; ++cnt) {

			mExifTextViewList.get(cnt).setText(mExif.mTag[cnt]);
			if (mExifOld != null && mExifOld.mTag[cnt] != null) {
				if (!mExifOld.mTag[cnt].equals(mExif.mTag[cnt])) {
					mExifTextViewList.get(cnt).setTextColor(0xff00ff00);
				} else {
					mExifTextViewList.get(cnt).setTextColor(0xffffffff);
				}
			} else {
				mExifTextViewList.get(cnt).setTextColor(0xffffffff);
			}
		}
	}

}
