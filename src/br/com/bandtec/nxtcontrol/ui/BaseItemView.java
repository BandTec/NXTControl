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
import android.view.View;
import android.view.ViewDebug.ExportedProperty;

public final class BaseItemView extends View {
	private String text, ellipsizedText;
	private int state, width;
	
	public BaseItemView(Context context) {
		super(context);
	}
	
	private void processEllipsis() {
		ellipsizedText = UI.ellipsizeText(text, UI._22sp, width - (UI._8dp << 1));
	}
	
	public void setItemState(String text, int state) {
		final int w = getWidth();
		this.state = (this.state & ~(UI.STATE_CURRENT | UI.STATE_SELECTED | UI.STATE_MULTISELECTED)) | state;
		//watch out, DO NOT use equals()!
		if (this.text == text && width == w)
			return;
		this.width = w;
		this.text = text;
		processEllipsis();
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
	public void invalidateDrawable(Drawable drawable) {
	}
	
	@Override
	protected boolean verifyDrawable(Drawable drawable) {
		return false;
	}
	
	@Override
	@ExportedProperty(category = "drawing")
	public boolean isOpaque() {
		return (state != 0);
	}
	
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		state = UI.handleStateChanges(state, isPressed(), isFocused(), this);
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
		setMeasuredDimension(resolveSize(0, widthMeasureSpec), UI.defaultControlSize);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (width != w) {
			width = w;
			processEllipsis();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		final int txtColor = ((state == 0) ? UI.color_text : UI.color_text_selected);
		getDrawingRect(UI.rect);
		UI.drawBg(canvas, state, UI.rect, false);
		UI.drawText(canvas, ellipsizedText, txtColor, UI._22sp, UI._8dp, (UI.defaultControlSize >> 1) - (UI._22spBox >> 1) + UI._22spYinBox);
	}
}
