package com.kevin.gifmovieview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Kevin 1.使用Movie提供的Movie.decodeStream() 解析gif,然后通过文件流的方式播放
 *         2.通过canvas实现对gif拉伸，适配任何屏幕，在XML文件里面就可以对gif进行比例缩放
 */
public class GifMovieView extends View {

	private static final int	DEFAULT_MOVIEW_DURATION	= 1000;
	private volatile boolean	mPaused					= false;

	private Movie				mMovie;
	private long				mMovieStart;
	// 获取到的gif资源的width,height
	private int					mWidth, mHeight;
	// 将来根据实际设置的gif资源的width,height
	private int					mViewWidht, mViewHeight;
	private int					mMovieResourceId;
	private int					mCurrentAnimationTime	= 0;

	public GifMovieView(Context context) {
		this(context, null);
	}

	public GifMovieView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GifMovieView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setViewAttributes(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mMovie != null) {
			if (!mPaused) {
				updateAnimationTime();
				drawMovieFrame(canvas);
			} else {
				drawMovieFrame(canvas);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if (widthMode == MeasureSpec.EXACTLY) {
			mViewWidht = widthSize;
		} else {
			mViewWidht = (int) (getPaddingLeft() + mWidth + getPaddingRight());
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			mViewHeight = heightSize;
		} else {
			mViewHeight = (int) (getPaddingTop() + mHeight + getPaddingBottom());
		}

		setMeasuredDimension(mViewWidht, mViewHeight);
	}

	/**
	 * 获取自定义样式属性
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	private void setViewAttributes(Context context, AttributeSet attrs, int defStyle) {
		/**
		 * Starting from HONEYCOMB have to turn off HW acceleration to draw
		 * Movie on Canvas. View在运行时阶段禁用硬件加速
		 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		/**
		 * 获得我们所定义的自定义样式属性
		 */
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GifMoviewView, defStyle, 0);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.GifMoviewView_gif:
				mMovieResourceId = a.getResourceId(R.styleable.GifMoviewView_gif, -1);
				if (mMovieResourceId != -1) {
					mMovie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
					// gif图片宽度，高度
					mHeight = mMovie.height();
					mWidth = mMovie.width();
				}
				break;
			case R.styleable.GifMoviewView_paused:
				mPaused = a.getBoolean(R.styleable.GifMoviewView_paused, false);
				break;
			default:
				break;
			}
		}
		a.recycle();
	}

	/**
	 * 设置Gif资源
	 *
	 * @param movieResId
	 */
	public void setMovieResource(int movieResId) {
		this.mMovieResourceId = movieResId;
		mMovie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
		requestLayout();
	}

	public void setMovieTime(int time) {
		mCurrentAnimationTime = time;
		invalidate();
	}

	public void setPaused(boolean paused) {
		this.mPaused = paused;
		if (!paused) {
			mMovieStart = android.os.SystemClock.uptimeMillis() - mCurrentAnimationTime;
		}
		invalidate();
	}

	public boolean isPaused() {
		return this.mPaused;
	}

	/**
	 * Calculate current animation time
	 */
	private void updateAnimationTime() {
		long now = android.os.SystemClock.uptimeMillis();
		if (mMovieStart == 0) {
			mMovieStart = now;
		}
		int dur = mMovie.duration();
		if (dur == 0) {
			dur = DEFAULT_MOVIEW_DURATION;
		}
		mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
	}

	private void drawMovieFrame(Canvas canvas) {
		mMovie.setTime(mCurrentAnimationTime);
		// 根据屏幕大小计算缩放比例
		float saclex = (float) mViewWidht / (float) mWidth;
		float sacley = (float) mViewHeight / (float) mHeight;
		canvas.scale(saclex, sacley);
		mMovie.draw(canvas, 0, 0);
		invalidate();
		canvas.restore();
	}
}
