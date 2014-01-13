package com.kircherelectronics.simplelinearacceleration.gauge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/*
 * Simple Linear Acceleration
 * Copyright (C) 2013, Kaleb Kircher - Boki Software, Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Draws an analog gauge for displaying rotation measurements in three-space
 * from device sensors.
 * 
 * Note that after Android 4.0 TextureView exists, as does SurfaceView for
 * Android 3.0 which won't hog the UI thread like View will. This should only be
 * used with devices or certain libraries that require View.
 * 
 * @author Kaleb, Scott Bannick
 * @version %I%, %G%
 * @see http://developer.android.com/reference/android/view/View.html
 * @since Scott Bannick 5/19/2013: Made gui changes.
 */
public final class GaugeRotationHolo extends View
{

	/*
	 * Developer Note: In the interest of keeping everything as fast as
	 * possible, only the measurements are redrawn, the gauge background and
	 * display information are drawn once per device orientation and then cached
	 * so they can be reused. All allocation and reclaiming of memory should
	 * occur before and after the handler is posted to the thread, but never
	 * while the thread is running. Allocation and reclamation of memory while
	 * the handler is posted to the thread will cause the GC to run, resulting
	 * in long delays (up to 600ms) while the GC cleans up memory. The frame
	 * rate to drop dramatically if the GC is running often, so try to keep it
	 * happy and out of the way.
	 * 
	 * Avoid iterators, Set or Map collections (use SparseArray), + to
	 * concatenate Strings (use StringBuffers) and above all else boxed
	 * primitives (Integer, Double, Float, etc).
	 */

	/*
	 * Developer Note: TextureView can only be used in a hardware accelerated
	 * window. When rendered in software, TextureView will draw nothing! On
	 * Android 3.0 devices this means a manifest declaration. On older devices,
	 * other implementations than TetureView will be required.
	 */

	/*
	 * Developer Note: There are some things to keep in mind when it comes to
	 * Android and hardware acceleration. What we see in Android 4.0 is �full�
	 * hardware acceleration. All UI elements in windows, and third-party apps
	 * will have access to the GPU for rendering. Android 3.0 had the same
	 * system, but now developers will be able to specifically target Android
	 * 4.0 with hardware acceleration. Google is encouraging developers to
	 * update apps to be fully-compatible with this system by adding the
	 * hardware acceleration tag in an app�s manifest. Android has always used
	 * some hardware accelerated drawing.
	 * 
	 * Since before 1.0 all window compositing to the display has been done with
	 * hardware. "Full" hardware accelerated drawing within a window was added
	 * in Android 3.0. The implementation in Android 4.0 is not any more full
	 * than in 3.0. Starting with 3.0, if you set the flag in your app saying
	 * that hardware accelerated drawing is allowed, then all drawing to the
	 * application�s windows will be done with the GPU. The main change in this
	 * regard in Android 4.0 is that now apps that are explicitly targeting 4.0
	 * or higher will have acceleration enabled by default rather than having to
	 * put android:handwareAccelerated="true" in their manifest. (And the reason
	 * this isn�t just turned on for all existing applications is that some
	 * types of drawing operations can�t be supported well in hardware and it
	 * also impacts the behavior when an application asks to have a part of its
	 * UI updated. Forcing hardware accelerated drawing upon existing apps will
	 * break a significant number of them, from subtly to significantly.)
	 */

	private static final String TAG = GaugeRotationHolo.class.getSimpleName();

	// drawing tools
	private RectF rimOuterRect;
	private RectF rimTopRect;
	private RectF rimBottomRect;
	private RectF rimLeftRect;
	private RectF rimRightRect;
	private Paint rimOuterPaint;

	// Keep static bitmaps of the gauge so we only have to redraw if we have to
	// Static bitmap for the bezel of the gauge
	private Bitmap bezel;
	// Static bitmap for the face of the gauge
	private Bitmap face;
	// Static bitmap for the scales of the gauge
	private Bitmap rollScales;

	// scale configuration
	private static int totalNicks = 5;
	private static float degreesPerNick = 360.0f / totalNicks;

	// Keep track of the rotation of the device
	private float[] rotation = new float[3];

	// Rectangle to draw the earth section of the gauge face
	private RectF earthRect;
	// Rectangle to draw the face of the gauge
	private RectF faceRect;
	// Rectangle to draw the rim of the gauge
	private RectF rimRect;
	// Rectangle to draw the roll angle scales
	private RectF rollScaleRect;
	// Rectangle to draw the sky section of the gauge face
	private RectF skyRect;
	// Rectangle to draw the sky section of the gauge face
	private RectF skyBackgroundRect;

	// Paint to draw the red arrow for the roll angle scales
	private Paint arrowPaint;
	// Paint to draw the gauge bitmaps
	private Paint backgroundPaint;
	// Paint to draw the earth portion of the gauge face
	private Paint earthPaint;
	// Paint to draw the bitmap for the pitch angle scale's guide gauge
	private Paint gaugeGuidePaint;
	// Paint to draw numbers
	private Paint numericPaint;
	// Paint to draw the outer rim of the bezel
	private Paint rimCirclePaint;
	// Paint to draw the rim of the bezel
	private Paint rimPaint;
	// Paint to draw the shadow of the bezel
	private Paint rimShadowPaint;
	// Paint to draw the scales
	private Paint scalePaint;
	// Paint to draw the sky portion of the gauge face
	private Paint skyPaint;
	// Paint to draw the small tick marks
	private Paint smallTickPaint;
	// Paint to draw the thick tick marks
	private Paint thickScalePaint;

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 */
	public GaugeRotationHolo(Context context)
	{
		super(context);

		initDrawingTools();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 */
	public GaugeRotationHolo(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		initDrawingTools();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GaugeRotationHolo(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		initDrawingTools();
	}

	/**
	 * Update the rotation of the device.
	 * 
	 * @param rotation
	 */
	public void updateRotation(float[] rotation)
	{
		this.rotation = rotation;

		this.invalidate();
	}

	private void initDrawingTools()
	{
		// Rectangle for the rim of the gauge bezel
		rimRect = new RectF(0.12f, 0.12f, 0.88f, 0.88f);

		// Paint for the rim of the gauge bezel
		rimPaint = new Paint();
		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		// The linear gradient is a bit skewed for realism
		rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, Color
				.rgb(0, 0, 0), Color.rgb(0, 0, 0), Shader.TileMode.CLAMP));

		float rimOuterSize = -0.04f;
		rimOuterRect = new RectF();
		rimOuterRect.set(rimRect.left + rimOuterSize, rimRect.top
				+ rimOuterSize, rimRect.right - rimOuterSize, rimRect.bottom
				- rimOuterSize);

		// still a work in progress changing the rimOuterSize will not
		// dynamically
		// change the small rectangles to the appropriate size.
		rimTopRect = new RectF(0.5f, 0.106f, 0.5f, 0.06f);
		rimTopRect.set(rimTopRect.left + rimOuterSize, rimTopRect.top
				+ rimOuterSize, rimTopRect.right - rimOuterSize,
				rimTopRect.bottom - rimOuterSize);

		rimBottomRect = new RectF(0.5f, 0.94f, 0.5f, 0.894f);
		rimBottomRect.set(rimBottomRect.left + rimOuterSize, rimBottomRect.top
				+ rimOuterSize, rimBottomRect.right - rimOuterSize,
				rimBottomRect.bottom - rimOuterSize);

		rimLeftRect = new RectF(0.106f, 0.5f, 0.06f, 0.5f);
		rimLeftRect.set(rimLeftRect.left + rimOuterSize, rimLeftRect.top
				+ rimOuterSize, rimLeftRect.right - rimOuterSize,
				rimLeftRect.bottom - rimOuterSize);

		rimRightRect = new RectF(0.94f, 0.5f, 0.894f, 0.5f);
		rimRightRect.set(rimRightRect.left + rimOuterSize, rimRightRect.top
				+ rimOuterSize, rimRightRect.right - rimOuterSize,
				rimRightRect.bottom - rimOuterSize);

		rimOuterPaint = new Paint();
		rimOuterPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		rimOuterPaint.setColor(Color.rgb(255, 255, 255));

		// Paint for the outer circle of the gauge bezel
		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
		rimCirclePaint.setStrokeWidth(0.005f);

		float rimSize = 0.02f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		earthRect = new RectF();
		earthRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		skyRect = new RectF();
		skyRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		skyBackgroundRect = new RectF();
		skyBackgroundRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		// now set to black
		skyPaint = new Paint();
		skyPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		skyPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, Color
				.rgb(0, 0, 0), Color.rgb(0, 0, 0), Shader.TileMode.CLAMP));

		// now set to white
		earthPaint = new Paint();
		earthPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		earthPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, Color
				.rgb(238, 238, 238), Color.rgb(238, 238, 238),
				Shader.TileMode.CLAMP));

		rimShadowPaint = new Paint();
		rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect
				.width() / 2.0f, new int[]
		{ 0x00000000, 0x00000500, 0x50000500 }, new float[]
		{ 0.96f, 0.96f, 0.99f }, Shader.TileMode.MIRROR));
		rimShadowPaint.setStyle(Paint.Style.FILL);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(Color.WHITE);
		scalePaint.setStrokeWidth(0.005f);
		scalePaint.setAntiAlias(true);

		arrowPaint = new Paint();
		arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		arrowPaint.setColor(Color.RED);
		arrowPaint.setStrokeWidth(0.005f);
		arrowPaint.setAntiAlias(true);

		thickScalePaint = new Paint();
		thickScalePaint.setStyle(Paint.Style.STROKE);
		thickScalePaint.setColor(Color.WHITE);
		thickScalePaint.setStrokeWidth(0.008f);
		thickScalePaint.setAntiAlias(true);

		numericPaint = new Paint();
		numericPaint.setTextSize(0.035f);
		numericPaint.setColor(Color.WHITE);
		numericPaint.setTypeface(Typeface.DEFAULT);
		numericPaint.setAntiAlias(true);
		numericPaint.setTextAlign(Paint.Align.CENTER);
		// Bug issue with 4.2.1, only displays first digit. The answer is to
		// include setLinearText(true) on your paint for the text. This method
		// is showing as deprecated, but it's the only solution for the text to
		// display properly.
		numericPaint.setLinearText(true);

		smallTickPaint = new Paint();
		smallTickPaint.setStyle(Paint.Style.STROKE);
		smallTickPaint.setColor(Color.argb(100, 255, 255, 255));
		smallTickPaint.setStrokeWidth(0.005f);
		smallTickPaint.setAntiAlias(true);

		gaugeGuidePaint = new Paint();
		gaugeGuidePaint.setFilterBitmap(true);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);

		int chosenDimension = Math.min(chosenWidth, chosenHeight);

		setMeasuredDimension(chosenDimension, chosenDimension);
	}

	private int chooseDimension(int mode, int size)
	{
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			return size;
		}
		else
		{ // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		}
	}

	// in case there is no size specified
	private int getPreferredSize()
	{
		return 300;
	}

	/**
	 * Draw the gauge rim.
	 * 
	 * @param canvas
	 */
	private void drawRim(Canvas canvas)
	{
		// First draw the most back rim
		canvas.drawOval(rimOuterRect, rimOuterPaint);
		// Then draw the small black line
		canvas.drawOval(rimRect, rimPaint);
		// now the outer rim circle
		// canvas.drawOval(rimRect, rimCirclePaint);

		canvas.drawRect(rimTopRect, rimOuterPaint);
		// bottom rect
		canvas.drawRect(rimBottomRect, rimOuterPaint);
		// left rect
		canvas.drawRect(rimLeftRect, rimOuterPaint);
		// right rect
		canvas.drawRect(rimRightRect, rimOuterPaint);
	}

	/**
	 * Draw the gauge face.
	 * 
	 * @param canvas
	 */
	private void drawFace(Canvas canvas)
	{
		// free the old bitmap
		if (face != null)
		{
			face.recycle();
		}

		face = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas faceCanvas = new Canvas(face);
		float scale = (float) getWidth();
		faceCanvas.scale(scale, scale);

		float rimSize = 0.02f;

		float radius = ((rimRect.left + rimSize) + (rimRect.right - rimSize)) / 2;

		float aPos = -rotation[1];

		if (aPos < 0)
		{
			aPos = 0;
		}

		float aNeg = -rotation[1];

		if (aNeg > 0)
		{
			aNeg = 0;
		}

		float radiusSquaredNeg = (float) Math.pow(radius, 2);

		float aSquaredNeg = (float) Math.pow(aNeg, 2);

		float bNeg = (float) Math.sqrt(radiusSquaredNeg - aSquaredNeg);

		float radiusSquaredPos = (float) Math.pow(radius, 2);

		float aSquaredPos = (float) Math.pow(aPos, 2);

		float bPos = (float) Math.sqrt(radiusSquaredPos - aSquaredPos);

		skyBackgroundRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		if (aPos == 0)
		{
			faceCanvas.drawArc(skyBackgroundRect,
					(float) (0 + (-rotation[0] * (180 / Math.PI))), 360, true,
					skyPaint);
		}
		if (aNeg== 0)
		{
			faceCanvas.drawArc(skyBackgroundRect,
					(float) (0 + (-rotation[0] * (180 / Math.PI))), 360, true,
					earthPaint);
		}

		skyRect.set((rimRect.left + rimSize) + (radius - bPos) / 2, rimRect.top
				+ rimSize, (rimRect.right - rimSize) - (radius - bPos) / 2,
				(rimRect.bottom - rimSize) - (aPos));

		faceCanvas.drawArc(skyRect,
				(float) (0 + (-rotation[0] * (180 / Math.PI))), -180, true,
				skyPaint);

		earthRect.set((rimRect.left + rimSize) + (radius - bNeg) / 2,
				(rimRect.top + rimSize) - (aNeg), (rimRect.right - rimSize)
						- (radius - bNeg) / 2, rimRect.bottom - rimSize);

		// canvas.drawOval(faceRect, facePaint);
		faceCanvas.drawArc(earthRect,
				(float) (0 + (-rotation[0] * (180 / Math.PI))), 180, true,
				earthPaint);

		canvas.drawBitmap(face, 0, 0, backgroundPaint);
	}

	/**
	 * Draw the gauge roll scales.
	 * 
	 * @param canvas
	 */
	private void drawRollScale(Canvas canvas)
	{
		canvas.drawArc(rollScaleRect,
				(float) (-16 + (rotation[1] * (180 / Math.PI))), -148, false,
				scalePaint);

		totalNicks = 11;
		degreesPerNick = 180.0f / totalNicks;

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		for (int i = 0; i < totalNicks; ++i)
		{
			float y1 = rollScaleRect.top;
			float y2 = y1 - 0.020f;

			if (i % 3 == 0 && i != 6)
			{
				canvas.drawLine(0.5f, y1, 0.5f, y2 - 0.015f, thickScalePaint);
			}
			else
			{
				canvas.drawLine(0.5f, y1, 0.5f, y2, scalePaint);
			}

			if (i == 0)
			{
				canvas.rotate((float) (-90 + (rotation[1] * (180 / Math.PI))),
						0.5f, 0.5f);
			}

			canvas.rotate(degreesPerNick, 0.5f, 0.5f);
		}
		canvas.restore();
	}

	/**
	 * Draw the gauge bezel.
	 * 
	 * @param canvas
	 */
	private void drawBezel(Canvas canvas)
	{
		if (bezel == null)
		{
			Log.w(TAG, "Bezel not created");
		}
		else
		{
			canvas.drawBitmap(bezel, 0, 0, backgroundPaint);
		}
	}

	/**
	 * Draw the gauge scales.
	 * 
	 * @param canvas
	 */
	private void drawScale(Canvas canvas)
	{
		// free the old bitmap
		if (rollScales != null)
		{
			rollScales.recycle();
		}

		rollScales = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas rollScaleCanvas = new Canvas(rollScales);
		float scale = (float) getWidth();
		rollScaleCanvas.scale(scale, scale);

		drawRollScale(rollScaleCanvas);

		canvas.drawBitmap(rollScales, 0, 0, backgroundPaint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d(TAG, "Size changed to " + w + "x" + h);

		regenerateBezel();
	}

	/**
	 * Regenerate the background image. This should only be called when the size
	 * of the screen has changed. The background will be cached and can be
	 * reused without needing to redraw it.
	 */
	private void regenerateBezel()
	{
		// free the old bitmap
		if (bezel != null)
		{
			bezel.recycle();
		}

		bezel = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas bezelCanvas = new Canvas(bezel);
		float scale = (float) getWidth();
		bezelCanvas.scale(scale, scale);

		drawRim(bezelCanvas);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		drawBezel(canvas);
		drawFace(canvas);
		//drawScale(canvas);

		float scale = (float) getWidth();
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);

		canvas.restore();
	}

}
