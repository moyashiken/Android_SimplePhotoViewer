package com.moya.simplephotoviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.moya.simplephotoviewer.util.Logger;

public class ImageScalingView extends View {
	private ImageRender mImageRender;
	private String mFilePath;

	private float mPosX;
	private float mPosY;

	private float mLastTouchX;
	private float mLastTouchY;

	private float mScaleFocalX;
	private float mScaleFocalY;

	private static final int INVALID_POINTER_ID = -1;

	private int mActivePointerId = INVALID_POINTER_ID;

	private ScaleGestureDetector mScaleDetector;

	private float mScaleFactor = 1.f;

	public ImageScalingView(Context context) {
		this(context, null, 0);
	}

	public ImageScalingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageScalingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

		mImageRender = new ImageRender(this);

		init();
	}

	private void init() {

		mScaleFocalX = -1;
		mScaleFocalY = -1;
	}

	public void LoadImage(String filaPath) {
		if (mImageRender != null) {

			setFilePath(filaPath);

			mImageRender.Show(mFilePath);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.

		mScaleDetector.onTouchEvent(ev);

		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_DOWN: {

			mPosX = 0;
			mPosY = 0;
			mLastTouchX = ev.getX();
			mLastTouchY = ev.getY();
			mActivePointerId = ev.getPointerId(0);

			Logger.v("ACTION_DOWN mPosX=" + mPosX + ":mPosY=" + mPosY + ":mLastTouchX=" + mLastTouchX + ":mLastTouchY="
					+ mLastTouchY);
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = ev.findPointerIndex(mActivePointerId);
			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);

			// Only move if the ScaleGestureDetector isn't processing a gesture.
			if (!mScaleDetector.isInProgress()) {
				final float dx = x - mLastTouchX;
				final float dy = y - mLastTouchY;

				mPosX += dx;
				mPosY += dy;

				mImageRender.setImageOffset((int) mPosX, (int) mPosY, true);

				invalidate();
			}

			mLastTouchX = x;
			mLastTouchY = y;

			Logger.v("ACTION_MOVE mPosX=" + mPosX + ":mPosY=" + mPosY + ":mLastTouchX=" + mLastTouchX + ":mLastTouchY="
					+ mLastTouchY);
			break;
		}

		case MotionEvent.ACTION_UP: {
			Logger.v("ACTION_UP");

			final int pointerIndex = ev.findPointerIndex(mActivePointerId);
			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);

			// Only move if the ScaleGestureDetector isn't processing a gesture.
			if (!mScaleDetector.isInProgress()) {
				final float dx = x - mLastTouchX;
				final float dy = y - mLastTouchY;

				mPosX += dx;
				mPosY += dy;

				mImageRender.setImageOffset((int) mPosX, (int) mPosY, false);
				// mImageRender.Show(false);

				invalidate();
			}

			mActivePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
			Logger.v("ACTION_CANCEL");
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {
			final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastTouchX = ev.getX(newPointerIndex);
				mLastTouchY = ev.getY(newPointerIndex);
				mActivePointerId = ev.getPointerId(newPointerIndex);
			}

			Logger.v("ACTION_POINTER_UP mPosX=" + mPosX + ":mPosY=" + mPosY + ":mLastTouchX=" + mLastTouchX + ":mLastTouchY="
					+ mLastTouchY);

			break;
		}
		}

		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		mImageRender.setViewSize(this.getWidth(), this.getHeight());
		mImageRender.Show(mFilePath);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.save();
		// canvas.translate(mPosX, mPosY);
		// canvas.scale(mScaleFactor, mScaleFactor);
		// canvas.rotate(45.0f);
		// mIcon.draw(canvas);

		mImageRender.draw(canvas);

		// Paint paint = new Paint();
		// canvas.drawColor(Color.WHITE);
		// paint.setColor(Color.BLUE);
		// paint.setAntiAlias(true);
		// paint.setTextSize(24);
		// canvas.drawText("Hello, SurfaceView!", paint.getTextSize(), 0,
		// paint);

		canvas.restore();
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mScaleFocalX = detector.getFocusX();
			mScaleFocalY = detector.getFocusY();
			invalidate();
			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			// Log.d(TAG, "onScaleEnd : "+ detector.getScaleFactor());
			// _scaleFactor *= detector.getScaleFactor();
			// invalidate();
			int x = (int) detector.getFocusX() - (int) mScaleFocalX;
			int y = (int) detector.getFocusY() - (int) mScaleFocalY;

			mScaleFocalX = -1;
			mScaleFocalY = -1;

			mImageRender.setImageScale(mScaleFactor, x, y, false);
			// mImageRender.initVirtual();
			// mImageRender.setImageScale(mScaleFactor);
			// invalidate();
			super.onScaleEnd(detector);
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 20.0f));

			// Log.v("", "mScaleFactor =" + mScaleFactor);
			// Log.v("", "onscalace x =" + detector.getCurrentSpanX());
			// Log.v("", "getFocusX x =" + detector.getFocusX());
			mScaleFocalX = detector.getFocusX();
			mScaleFocalY = detector.getFocusY();
			Logger.v("mScaleFocalX=" + mScaleFocalX + ", mScaleFocalY" + mScaleFocalY);
			int x = (int) detector.getFocusX() - (int) mScaleFocalX;
			int y = (int) detector.getFocusY() - (int) mScaleFocalY;

			// Point v_offset = mImageRender.getmVirtualOffset();
			// Point offset = mImageRender.getmImageOffset();
			// float offsetX = (current_focal_x - mImageRender.getmViewWidth() /
			// 2f);
			// float offsetY = (current_focal_y - mImageRender.getmViewHeight()
			// / 2f);
			// mImageRender.setmVirtualOffset((int)offsetX, (int)offsetY);

			// mImageRender.setmVirtualOffset((int)mScaleFocalX,
			// (int)mScaleFocalY);
			// mImageRender.setImageOffset(current_focal_x - (int) mScaleFocalX,
			// current_focal_y - (int) mScaleFocalY);
			// mImageRender.setmVirtualScale(mScaleFactor);

			mImageRender.setImageScale(mScaleFactor, (int) mScaleFocalX, (int) mScaleFocalY, true);

			invalidate();
			return true;
		}
	}

	public void setScaleFit() {
		mImageRender.setScaleFit();
	}

	public void setScaleDot() {
		mImageRender.setScaleDot();
	}

	// -----------------------------------------------------------------------
	// getter / setter
	// -----------------------------------------------------------------------
	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}

}
