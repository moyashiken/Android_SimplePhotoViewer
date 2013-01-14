package com.moya.simplephotoviewer;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.moya.simplephotoviewer.util.Logger;
import com.moya.simplephotoviewer.util.Util;

public class MainActivity extends Activity implements OnClickListener {

	private TextView mTV_Folder;
	private Button mBTN_folder;
	private Spinner mSPN_FileList;
	private Button mBTN_Refresh;
	private Button mBTN_ShowImage;

	private final int REQUEST_CODE_PICK_DIR = 1;

	SDCardBroadcastReceiver mSDCardBroadcastReceiver = new SDCardBroadcastReceiver();

	public class SDCardBroadcastReceiver extends BroadcastReceiver {
		public SDCardBroadcastReceiver() {
			super();
		}

		public void onReceive(Context context, Intent intent) {
			Logger.d("SDCardBroadCastReceiver onReceive " + intent.getAction());

			setFilelist();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBTN_folder = (Button) findViewById(R.id.button_select_folder);
		mBTN_folder.setOnClickListener(this);
		mTV_Folder = (TextView) findViewById(R.id.textView_path);
		mSPN_FileList = (Spinner) findViewById(R.id.spinner_filelist);
		mBTN_Refresh = (Button) findViewById(R.id.button_refresh);
		mBTN_Refresh.setOnClickListener(this);
		mBTN_ShowImage = (Button) findViewById(R.id.button_show_image);
		mBTN_ShowImage.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		mTV_Folder.setText(SharedPrefSetting.getData(this, SharedPrefSetting.SETTING_PATH));

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		registerReceiver(mSDCardBroadcastReceiver, intentFilter);

		setFilelist();
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mSDCardBroadcastReceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onClick(View v) {
		if (v == mBTN_folder) {
			Intent fileExploreIntent = new Intent(FileBrowserActivity.INTENT_ACTION_SELECT_DIR, null, this,
					FileBrowserActivity.class);
			fileExploreIntent.putExtra(FileBrowserActivity.startDirectoryParameter,
					SharedPrefSetting.getData(this, SharedPrefSetting.SETTING_PATH));
			startActivityForResult(fileExploreIntent, REQUEST_CODE_PICK_DIR);
		} else if (v == mBTN_Refresh) {
			setFilelist();
		} else if (v == mBTN_ShowImage) {
			showImage();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_PICK_DIR) {
			if (resultCode == this.RESULT_OK) {
				String dir = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
				// Toast.makeText(this,
				// "Received DIRECTORY path from file browser:\n" + dir,
				// Toast.LENGTH_LONG).show();

				SharedPrefSetting.reset(this);
				SharedPrefSetting.setData(this, SharedPrefSetting.SETTING_PATH, dir);

				setFilelist();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setFilelist() {
		String dir = SharedPrefSetting.getData(this, SharedPrefSetting.SETTING_PATH);
		mTV_Folder.setText(dir);

		List<String> list = Util.getFileNameList(Util.getFileListDes(dir));

		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);

		mSPN_FileList.setAdapter(adapter);
	}

	private void showImage() {

		int select = mSPN_FileList.getSelectedItemPosition();
		if (select < 0) {
			return;
		}

		String dir = SharedPrefSetting.getData(this, SharedPrefSetting.SETTING_PATH);
		String filepath = Util.getFilePathFromNumber(dir, select);

		SharedPrefSetting.setData(this, SharedPrefSetting.SETTING_CURRENT_IMAGE_PATH, filepath);
		Intent i = new Intent(this, ImageView.class);
		this.startActivity(i);
	}
}
