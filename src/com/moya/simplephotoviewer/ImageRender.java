package com.moya.simplephotoviewer;

import java.io.File;
import java.io.IOException;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Debug;
import android.view.View;

import com.moya.simplephotoviewer.util.Logger;
import com.moya.simplephotoviewer.util.Size;
import com.moya.simplephotoviewer.util.Util;

public class ImageRender {
	private Size mViewSize = new Size(0, 0);
	private View mParent;

	private ImageInfo mImageInfo;
	private LoadImageAsyncTask mTask;
	private ImageInfo mImageInfoNext;
	private LoadImageAsyncTask mTaskNext;
	private ImageInfo mImageInfoPrevious;
	private LoadImageAsyncTask mTaskPrevious;
	private Object mLock = new Object();

	private Point mVirtualOffset = new Point(0, 0);
	private float mVirtualScale = 1f;

	public ImageRender(View parent) {
		mParent = parent;
	}

	public void Show(String filepath) {
		if (filepath == null) {
			return;
		}

		int cache_state = 0;
		if (mImageInfoNext != null && filepath.equals(mImageInfoNext.mFilaPath)) {
			mImageInfoPrevious = mImageInfo;
			mImageInfo = mImageInfoNext;

			cache_state = 1;
		} else if (mImageInfoPrevious != null && filepath.equals(mImageInfoPrevious.mFilaPath)) {
			mImageInfoNext = mImageInfo;
			mImageInfo = mImageInfoPrevious;

			cache_state = 2;
		} else {
			mImageInfo = new ImageInfo();
			mImageInfo.mFilaPath = filepath;
		}

		if (mTask != null) {
			mTask.cancel(false);
			mTask = null;
		}
		if (mTaskNext != null) {
			mTaskNext.cancel(false);
			mTaskNext = null;
		}
		if (mTaskPrevious != null) {
			mTaskPrevious.cancel(false);
			mTaskPrevious = null;
		}

		if (mImageInfo.mBmpResized == null) {
			LoadImageAsyncTask task = new LoadImageAsyncTask(mImageInfo, true);
			task.execute("");
			mTask = task;
			LoadThumb(mImageInfo);
		}

		if (cache_state == 0) {
			mImageInfoNext = new ImageInfo();
			mImageInfoNext.mFilaPath = Util.getNextFile(filepath);
			LoadImageAsyncTask taskNext = new LoadImageAsyncTask(mImageInfoNext, false);
			taskNext.execute("");
			mTaskNext = taskNext;

			mImageInfoPrevious = new ImageInfo();
			mImageInfoPrevious.mFilaPath = Util.getPreviousFile(filepath);
			LoadImageAsyncTask taskPrevious = new LoadImageAsyncTask(mImageInfoPrevious, false);
			taskPrevious.execute("");
			mTaskPrevious = taskPrevious;
		} else if (cache_state == 1) {
			mImageInfoNext = new ImageInfo();
			mImageInfoNext.mFilaPath = Util.getNextFile(filepath);
			LoadImageAsyncTask taskNext = new LoadImageAsyncTask(mImageInfoNext, false);
			taskNext.execute("");
			mTaskNext = taskNext;
		} else if (cache_state == 2) {
			mImageInfoPrevious = new ImageInfo();
			mImageInfoPrevious.mFilaPath = Util.getPreviousFile(filepath);
			LoadImageAsyncTask taskPrevious = new LoadImageAsyncTask(mImageInfoPrevious, false);
			taskPrevious.execute("");
			mTaskPrevious = taskPrevious;
		}
	}

	private void loadSize(ImageInfo info) {
		if (mViewSize.mW <= 0 || mViewSize.mH <= 0) {
			return;
		}
		if (info.mFilaPath == null) {
			return;
		}
		File file = new File(info.mFilaPath);
		if (!file.isFile()) {
			return;
		}

		try {
			ExifInterface exif = new ExifInterface(info.mFilaPath);
			info.mOrientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
		} catch (IOException e) {
			info.mOrientation = 1;
		}

		// creating to read option object
		BitmapFactory.Options options = new BitmapFactory.Options();

		// getting image size information
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(info.mFilaPath, options);

		if (!info.isRotate()) {
			info.mImageSize.mW = options.outWidth;
			info.mImageSize.mH = options.outHeight;
		} else {
			info.mImageSize.mW = options.outHeight;
			info.mImageSize.mH = options.outWidth;
		}
	}

	private float getFitScale(ImageInfo info) {
		float scaleW = (float) info.mImageSize.mW / mViewSize.mW;
		float scaleH = (float) info.mImageSize.mH / mViewSize.mH;

		// Logger.v("setFitScale" + "[" + info.mFilaPath + "]:"
		// + "info.mImageScale=" + info.mImageScale);
		return Math.max(scaleW, scaleH);
	}

	private void LoadImage(ImageInfo info) {
		Logger.v("LoadImage : file=" + info.mFilaPath);

		if (info.mFilaPath == null) {
			return;
		}

		File file = new File(info.mFilaPath);
		if (!file.isFile()) {
			return;
		}

		if (Logger.isLogOut()) {
			Runtime runtime = Runtime.getRuntime();

			Logger.v("Debug.getNativeHeapAllocatedSize()=" + (Debug.getNativeHeapAllocatedSize() / 1024 / 1024) + "MB");
			Logger.v("runtime.maxMemory()=" + (runtime.maxMemory() / 1024 / 1024) + "MB");
			Logger.v("usedMemory = " + (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024) + "MB");
		}

		synchronized (mLock) {
			BitmapFactory.Options options = new BitmapFactory.Options();

			options.inSampleSize = getLoadingScaleHigh(info.mImageScale);

			int w = (int) ((float) mViewSize.mW * info.mImageScale);
			int h = (int) ((float) mViewSize.mH * info.mImageScale);
			Rect rect = new Rect(info.mOffset.x, info.mOffset.y, info.mOffset.x + w, info.mOffset.y + h);
			Logger.v("LoadImage rect=" + rect);

			if (rect.left < 0) {
				rect.left = 0;
			}
			if (rect.top < 0) {
				rect.top = 0;
			}
			if (rect.right > info.mImageSize.mW) {
				rect.right = info.mImageSize.mW;
			}
			if (rect.bottom > info.mImageSize.mH) {
				rect.bottom = info.mImageSize.mH;
			}
			if (info.isRotate()) {
				int tmp = rect.left;
				rect.left = rect.top;
				rect.top = tmp;
				tmp = rect.right;
				rect.right = rect.bottom;
				rect.bottom = tmp;
			}

			Bitmap bmpImageFile = null;
			try {
				BitmapRegionDecoder regionDecoder;
				regionDecoder = BitmapRegionDecoder.newInstance(info.mFilaPath, false);
				bmpImageFile = regionDecoder.decodeRegion(rect, options);
				if (bmpImageFile == null) {
					Logger.e("decodeRegion error rect=" + rect);
					return;
				}

				Matrix matrix = new Matrix();
				float scale = 1f;
				if (!info.isRotate()) {
					scale = Math.min((float) mViewSize.mW / bmpImageFile.getWidth(),
							(float) mViewSize.mH / bmpImageFile.getHeight());
				} else {
					scale = Math.min((float) mViewSize.mW / bmpImageFile.getHeight(),
							(float) mViewSize.mH / bmpImageFile.getWidth());
				}
				matrix.postScale((float) scale, (float) scale, 0, 0);
				matrix.postRotate(info.getRotate());

				info.mBmpResized = Bitmap.createBitmap(bmpImageFile, 0, 0, bmpImageFile.getWidth(), bmpImageFile.getHeight(),
						matrix, true);

			} catch (IOException e) {
				Logger.e(e.getMessage());
			} catch (OutOfMemoryError e) {
				java.lang.System.gc();
				e.printStackTrace();
			}
		}

	}

	private void LoadThumb(ImageInfo info) {
		ExifInterface ei;
		try {
			if (mViewSize.mW <= 0 || mViewSize.mH <= 0) {
				return;
			}
			ei = new ExifInterface(info.mFilaPath);

			if (ei.hasThumbnail()) {
				byte[] data = ei.getThumbnail();
				if (data == null) {
					return;
				}
				Bitmap bmpThumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);

				Matrix matrix = new Matrix();
				float scale = Math.min((float) mViewSize.mW / (float) bmpThumbnail.getWidth(), (float) mViewSize.mH
						/ (float) bmpThumbnail.getHeight());

				matrix.postScale(scale, scale, 0, 0);

				try {
					ExifInterface exif = new ExifInterface(info.mFilaPath);
					info.mOrientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
				} catch (IOException e) {
					info.mOrientation = 1;
				}
				matrix.postRotate(info.getRotate());

				info.mBmpResized_Thumb = Bitmap.createBitmap(bmpThumbnail, 0, 0, bmpThumbnail.getWidth(),
						bmpThumbnail.getHeight(), matrix, true);

				Logger.v("info.mBmpResized_Thumb.size=" + info.mBmpResized_Thumb.getWidth() + ", "
						+ info.mBmpResized_Thumb.getHeight());

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			java.lang.System.gc();
			e.printStackTrace();
		}
	}

	// this method returns the following value
	// 0 ～ 1.999 = 1
	// 2 ～ 2.999 = 2
	// 3 ～ 4.999 = 4
	// 5 ～ 8.999 = 8
	public int getLoadingScale(float input) {
		// n = Multiplier( 0 = 1, 1= 2, 2 = 4, 3 = 8, 4 = 16... )
		int scale = 0;
		for (int n = 0; n < 7; ++n) {
			float scale_next = scale * 2;
			if (scale_next == 0) {
				scale_next = 1;
			}

			if (scale < (int) input && (int) input <= scale_next) {
				return (int) scale_next;
			}

			scale = (int) scale_next;
		}
		return 1;
	}

	// this method returns the following value
	// 0 ～ 1.9 = 1
	// 2 ～ 3.999 = 2
	// 4 ～ 7.999 = 4
	// 8 ～ 15.999 = 8
	public int getLoadingScaleHigh(float input) {
		// n = Multiplier( 0 = 1, 1= 2, 2 = 4, 3 = 8, 4 = 16... )
		int scale = 1;
		for (int n = 0; n < 7; ++n) {
			float scale_next = scale * 2;
			if (scale_next > input) {
				return scale;
			}
			scale = (int) scale_next;
		}
		return 1;
	}

	public void draw(Canvas canvas) {
		if (mImageInfo == null) {
			return;
		}
		Bitmap bmp;
		if (mImageInfo.mBmpResized_Thumb != null && mImageInfo.mBmpResized == null) {
			bmp = mImageInfo.mBmpResized_Thumb;
		} else if (mImageInfo.mBmpResized != null) {
			bmp = mImageInfo.mBmpResized;
		} else {
			return;
		}

		int left = mVirtualOffset.x;
		int top = mVirtualOffset.y;

		// calc small image to center position
		if (bmp.getWidth() < mViewSize.mW) {
			left = (mViewSize.mW - bmp.getWidth()) / 2;
		}
		if (bmp.getHeight() < mViewSize.mH) {
			top = (mViewSize.mH - bmp.getHeight()) / 2;
		}

		Rect rect_src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		Rect rect_dist = new Rect(left, top, (int) ((left + (bmp.getWidth() * mVirtualScale))),
				(int) ((top + (bmp.getHeight() * mVirtualScale))));
		canvas.drawBitmap(bmp, rect_src, rect_dist, null);
	}

	private float getFitScale() {
		if (mImageInfo == null) {
			return 1;
		}
		float scale = 0;

		float WByH = ((float) mViewSize.mH / mImageInfo.mImageSize.mH) * mImageInfo.mImageSize.mW;

		if (mViewSize.mW <= WByH) {
			scale = (float) mViewSize.mW / mImageInfo.mImageSize.mW;
		} else {
			scale = (float) mViewSize.mH / mImageInfo.mImageSize.mH;
		}
		Logger.v("getFitScale ret=" + scale);
		return scale;
	}

	public void setScaleFit() {
		mImageInfo.mOffset.x = 0;
		mImageInfo.mOffset.y = 0;
		LoadImageAsyncTask task = new LoadImageAsyncTask(mImageInfo, true);
		task.execute("");
		mTask = task;
	}

	public void setScaleDot() {
		if (mImageInfo.mBmpResized != null) {
			mImageInfo.mImageScale = 1;

			mImageInfo.mOffset.x = (mImageInfo.mImageSize.mW - mViewSize.mW) / 2;
			mImageInfo.mOffset.y = (mImageInfo.mImageSize.mH - mViewSize.mH) / 2;

			LoadImage(mImageInfo);

			mParent.invalidate();
		}
	}

	private void releaseTask(LoadImageAsyncTask task) {
		if (task == mTask) {
			mTask = null;
		} else if (task == mTaskNext) {
			mTaskNext = null;
		} else if (task == mTaskPrevious) {
			mTaskPrevious = null;
		} else {
			Logger.e("error program structure.");
		}
	}

	// -----------------------------------------------------------------------
	// Load Thread
	// -----------------------------------------------------------------------
	protected class LoadImageAsyncTask extends AsyncTask<String, Integer, Long> implements OnCancelListener {
		private ImageInfo mInfo;
		private final boolean mIsMain;

		public LoadImageAsyncTask(ImageInfo info, boolean main) {
			mInfo = info;
			mIsMain = main;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Long doInBackground(String... params) {

			loadSize(mInfo);
			mInfo.mImageScale = getFitScale(mInfo);
			LoadImage(mInfo);
			return 123L;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onPostExecute(Long result) {
			releaseTask(this);

			if (mIsMain == true) {
				if (mInfo.mFilaPath.equals(mImageInfo.mFilaPath)) {
					mParent.invalidate();
				}
			}
		}

		public void onCancel(DialogInterface dialog) {
			this.cancel(true);
		}
	}


	private void setImageOffsetInternal(int x, int y, boolean virtual) {
		if (virtual) {
			
			if (getFitScale(mImageInfo) != mImageInfo.mImageScale) {
				mVirtualOffset.x = x;
				mVirtualOffset.y = y;
			}
		} else {
			x *= -1;
			if (!mImageInfo.isRotate()) {
				y *= -1;
			}
			int max_x = mImageInfo.mImageSize.mW - (int) (mImageInfo.mImageScale * (float) mViewSize.mW);
			int max_y = mImageInfo.mImageSize.mH - (int) (mImageInfo.mImageScale * (float) mViewSize.mH);

			mVirtualOffset.x = 0;
			mVirtualOffset.y = 0;
			mImageInfo.mOffset.x += (int) ((float) x * mImageInfo.mImageScale);
			mImageInfo.mOffset.y += (int) ((float) y * mImageInfo.mImageScale);

			if (mImageInfo.mOffset.x < 0) {
				mImageInfo.mOffset.x = 0;
			} else if (max_x <= mImageInfo.mOffset.x) {
				mImageInfo.mOffset.x = max_x - 1;
			}
			if (mImageInfo.mOffset.y < 0) {
				mImageInfo.mOffset.y = 0;
			} else if (max_y <= mImageInfo.mOffset.y) {
				mImageInfo.mOffset.y = max_y - 1;
			}
		}
	}
	private void setImageScaleInternal(float scale, int x, int y, boolean virtual) {

		// if (virtual) {
		// mVirtualScale = scale;
		//
		// float width = mVirtualScale * mViewSize.mW;
		// float hight = mVirtualScale * mViewSize.mH;
		// int left = (int)(x - (width / 2));
		// int top = (int)((hight / 2) - y);
		//
		// Logger.v("mImageInfo.mBmpResized.getWidth()=" +
		// mImageInfo.mBmpResized.getWidth() + ", mViewSize.mW=" +
		// mViewSize.mW);
		// Logger.v("mImageInfo.mBmpResized.getHeight()=" +
		// mImageInfo.mBmpResized.getHeight() + ", mViewSize.mH=" +
		// mViewSize.mH);
		// // if (mImageInfo.mBmpResized.getWidth() < mViewSize.mW) {
		// // left = 0;
		// // }
		// // if (mImageInfo.mBmpResized.getHeight() < mViewSize.mH) {
		// // top = 0;
		// // }
		// Logger.v("setImageScaleInternal width="+width+",hight="+hight+",left"+left+",top="+top);
		// setImageOffsetInternal(left, top, virtual);
		//
		// } else {
		// mVirtualScale = 1f;
		// float scaleW = (float) mImageInfo.mImageSize.mW / mViewSize.mW;
		// float scaleH = (float) mImageInfo.mImageSize.mH / mViewSize.mH;
		//
		// float max = Math.max(scaleW, scaleH);
		// if (max < scale) {
		// scale = max;
		// } else if (scale < 1) {
		// scale = 1f;
		// }
		//
		// mImageInfo.mImageScale = scale;
		// }
		// //setImageOffsetInternal(x, y, virtual);
	}
	// -----------------------------------------------------------------------
	// getter / setter
	// -----------------------------------------------------------------------

	public int getmViewSize() {
		return mViewSize.mW;
	}

	public void setViewSize(int w, int h) {
		mViewSize.set(w, h);
	}

	public Size getViewSize() {
		return mViewSize;
	}

	public void setImageOffset(int x, int y, boolean virtual) {
		Logger.v("setImageOffset x=" + x + ",y=" + y + ",virtual=" + virtual);

		setImageOffsetInternal(x, y, virtual);
		if (!virtual) {
			LoadImage(mImageInfo);
		}
	}

	public void setImageScale(float scale, int x, int y, boolean virtual) {
		// Logger.v("setImageScale scale =" + scale + ",x=" + x + ",y=" + y +
		// ",virtual=" + virtual);
		//
		// setImageScaleInternal(scale, x, y, virtual);
		// //
		// // if( !virtual ){
		// // LoadImage(mImageInfo);
		// // }
	}
}