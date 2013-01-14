package com.moya.simplephotoviewer;

import android.graphics.Bitmap;
import android.graphics.Point;
import com.moya.simplephotoviewer.util.Size;

public class ImageInfo {
	public Bitmap mBmpResized;
	public Bitmap mBmpResized_Thumb;
	public int mFile_count;
	public int mFile_number;
	public String mFilaPath;
	public Size mImageSize = new Size(0, 0);
	public float mImageScale;
	public int mOrientation;
	public Point mOffset = new Point(0, 0);

	public ImageInfo() {
		mBmpResized = null;
		mBmpResized_Thumb = null;
		mFile_count = -1;
		mFile_number = -1;
		mFilaPath = "";
		mImageScale = 1;
		mOrientation = 1;
	}

	public boolean isRotate() {

		if (mOrientation == 6 || mOrientation == 8) {
			return true;
		}

		return false;
	}

	public float getRotate() {
		if (mOrientation == 1) {
			return 0f;
		} else if (mOrientation == 3) {
			return 180f;
		} else if (mOrientation == 6) {
			return 90f;
		} else if (mOrientation == 8) {
			return 270f;
		}
		return 0f;
	}

	public Size getOriginalImageSize() {
		if (!isRotate()) {
			return mImageSize;
		}

		return new Size(mImageSize.mH, mImageSize.mW);
	}

}
