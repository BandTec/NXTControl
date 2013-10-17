//
//    NXT Control
//    Copyright (c) 2013 Carlos Rafael Gimenes das Neves
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program. If not, see {http://www.gnu.org/licenses/}.
//
//    https://github.com/BandTec/NXTControl
//
package br.com.bandtec.nxtcontrol.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;

public final class BgDirControl extends View {
	public static interface OnBgDirControlChangeListener {
		public void onDirectionChanged(BgDirControl dirControl, String direction);
	}
	
	public static final int CENTER_DIRECTION = 5;
	private static final String[] DIRECTIONS = new String[] {
		"1", "2", "3",
		"4", "5", "6",
		"7", "8", "9"
	};
	
	private int dir, w3, h3, offX, offY;
	private boolean tracking;
	private int state;
	private OnBgDirControlChangeListener listener;
	
	public BgDirControl(Context context) {
		super(context);
		init();
	}
	
	public BgDirControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public BgDirControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		dir = CENTER_DIRECTION;
		super.setClickable(true);
		super.setFocusable(false);
	}
	
	public OnBgDirControlChangeListener getOnBgDirControlChangeListener() {
		return listener;
	}
	
	public void setOnBgDirControlChangeListener(OnBgDirControlChangeListener listener) {
		this.listener = listener;
	}
	
	public boolean isTracking() {
		return tracking;
	}
	
	public int getDirectionValue() {
		return dir;
	}
	
	public void setDirectionValue(int direction) {
		setDirectionValue(direction, true);
	}
	
	private void setDirectionValue(int direction, boolean fromUser) {
		if (direction < 1)
			direction = 1;
		else if (direction > 9)
			direction = 9;
		if (dir != direction) {
			dir = direction;
			if (!fromUser && listener != null)
				listener.onDirectionChanged(this, DIRECTIONS[dir - 1]);
			invalidate();
		}
	}
	
	public String getDirection() {
		return DIRECTIONS[dir - 1];
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void setBackground(Drawable background) {
		super.setBackground(null);
	}
	
	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(null);
	}
	
	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(0);
	}
	
	@Override
	public void setBackgroundColor(int color) {
		super.setBackgroundResource(0);
	}
	
	@Override
	public Drawable getBackground() {
		return null;
	}
	
	@Override
	@ExportedProperty(category = "drawing")
	public boolean isOpaque() {
		return true;
	}
	
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		state = UI.handleStateChanges(state, isPressed(), false, this);
	}
	
	@Override
	public int getPaddingLeft() {
		return 0;
	}
	
	@Override
	public int getPaddingTop() {
		return 0;
	}
	
	@Override
	public int getPaddingRight() {
		return 0;
	}
	
	@Override
	public int getPaddingBottom() {
		return 0;
	}
	
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
	}
	
	@Override
	protected int getSuggestedMinimumWidth() {
		return UI.defaultControlSize;
	}
	
	@Override
	public int getMinimumWidth() {
		return UI.defaultControlSize;
	}
	
	@Override
	protected int getSuggestedMinimumHeight() {
		return UI.defaultControlSize;
	}
	
	@Override
	public int getMinimumHeight() {
		return UI.defaultControlSize;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(resolveSize(UI.defaultControlSize, widthMeasureSpec), resolveSize(UI.defaultControlSize, heightMeasureSpec));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w < h) {
			w3 = w / 3;
			h3 = w3;
		} else {
			h3 = h / 3;
			w3 = h3;
		}
		final int width = w3 * 3;
		final int height = h3 * 3;
		offX = (w >> 1) - (width >> 1);
		offY = (h >> 1) - (height >> 1);
	}
	
	private void trackTouchEvent(float x, float y) {
		final int ix = (int)x - offX, iy = (int)y - offY;
		int d;
		if (ix < w3) {
			// Left
			if (iy < h3) {
				// Top
				d = 1;
			} else if (iy >= (h3 << 1)) {
				// Bottom
				d = 7;
			} else {
				// Center
				d = 4;
			}
		} else if (ix >= (w3 << 1)) {
			// Right
			if (iy < h3) {
				// Top
				d = 3;
			} else if (iy >= (h3 << 1)) {
				// Bottom
				d = 9;
			} else {
				// Center
				d = 6;
			}
		} else {
			// Center
			if (iy < h3) {
				// Top
				d = 2;
			} else if (iy >= (h3 << 1)) {
				// Bottom
				d = 8;
			} else {
				// Center
				d = 5;
			}
		}
		setDirectionValue(d, false);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled())
			return false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			tracking = true;
			setPressed(true);
			trackTouchEvent(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			if (!tracking)
				return true;
			if (getParent() != null)
				getParent().requestDisallowInterceptTouchEvent(true);
			trackTouchEvent(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:
			setPressed(false);
			if (tracking) {
				trackTouchEvent(event.getX(), event.getY());
				//setDirectionValue(CENTER_DIRECTION, false);
				tracking = false;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			setPressed(false);
			if (tracking) {
				setDirectionValue(CENTER_DIRECTION, false);
				tracking = false;
			}
			invalidate();
			break;
		}
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.translate(0, offY);
		UI.rect.top = 0;
		UI.rect.bottom = h3;
		for (int i = 1; i <= 7; i += 3) {
			UI.rect.left = offX;
			UI.rect.right = offX + w3;
			UI.drawBg(canvas, state | ((dir == i) ? UI.STATE_FOCUSED : UI.STATE_SELECTED), UI.rect, true);
			UI.rect.left += w3;
			UI.rect.right += w3;
			UI.drawBg(canvas, state | ((dir == (i + 1)) ? UI.STATE_FOCUSED : UI.STATE_SELECTED), UI.rect, true);
			UI.rect.left += w3;
			UI.rect.right += w3;
			UI.drawBg(canvas, state | ((dir == (i + 2)) ? UI.STATE_FOCUSED : UI.STATE_SELECTED), UI.rect, true);
			if (i != 7)
				canvas.translate(0, h3);
		}
		canvas.translate(0, -((h3 << 1) + offY));
	}
}
