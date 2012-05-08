// Copyright (c) 2012 quicktigame2d project
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// * Redistributions of source code must retain the above copyright notice,
//   this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
// * Neither the name of the project nor the names of its contributors may be
//   used to endorse or promote products derived from this software without
//   specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
// EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// 
package com.googlecode.quicktigame2d;

import java.io.ByteArrayOutputStream;

import javax.microedition.khronos.opengles.GL10;

import org.appcelerator.titanium.util.TiUIHelper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class QuickTiGame2dTextSprite extends QuickTiGame2dSprite {

	private QuickTiGame2dTexture labelTexture = null;
	private String text = "";
	private String fontFamily = "";
	private float  fontSize = 0;
	private boolean isBold = false;
	private boolean isItalic = false;
	
	private boolean shouldReload = false;
	private boolean shouldUpdateWidth = true;
	
	private Layout.Alignment textAlign = Layout.Alignment.ALIGN_NORMAL;
	
	public QuickTiGame2dTextSprite() {
		// default text color equals black
		color(0, 0, 0);
	}
	
	private void loadTextData(GL10 gl) {
		if (view == null) return;
		
		if (labelTexture == null) {
			labelTexture = new QuickTiGame2dTexture(view.getContext());
			labelTexture.setWidth(1);
			labelTexture.setHeight(1);
		}
		
		TextPaint forePaint = new TextPaint();
		Paint backPaint = new Paint();
		
    	if (fontFamily.length() == 0) {
    		if (isBold && isItalic) {
    			forePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
    		} else if (isBold) {
    			forePaint.setTypeface(Typeface.DEFAULT_BOLD);
    		}
    	} else {
    		Typeface typeface = TiUIHelper.toTypeface(view.getContext(), fontFamily);
    		
    		if (isBold && isItalic) {
    			forePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD_ITALIC));
    		} else if (isBold) {
    			forePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
    		} else {
    			forePaint.setTypeface(typeface);
    		}
    	}
    	
    	forePaint.setColor(Color.WHITE);
    	if (fontSize > 0) forePaint.setTextSize(fontSize);
    	forePaint.setAntiAlias(true);
    	
    	backPaint.setColor(Color.TRANSPARENT);
    	backPaint.setStyle(Style.FILL);

    	if (shouldUpdateWidth) {
        	if (text.length() == 0) {
        		width = (int)Math.ceil(forePaint.measureText(" "));
        	} else {
            	width  = (int)Math.ceil(forePaint.measureText(getText()));
        	}
    	}
    	
    	StaticLayout wrapLayout = new StaticLayout(text, forePaint, width, textAlign, 1, 1, false);
    	int textWidth = wrapLayout.getWidth();
    	int textHeight = wrapLayout.getHeight();
    	
    	Bitmap bitmap = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888);
    	Canvas canvas = new Canvas(bitmap);
    	canvas.drawRect(0, 0, textWidth, textHeight, backPaint);
    	wrapLayout.draw(canvas);

    	ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] data = os.toByteArray();
        
		labelTexture.setDebug(view.getDebug());
        labelTexture.setName(text);
        labelTexture.setWidth(textWidth);
        labelTexture.setHeight(textHeight);
        labelTexture.onLoad(gl, data);
        
        this.width  = textWidth;
        this.height = textHeight;
	}
	
	public void reload() {
		shouldReload = true;
	}
	
	@Override
	public void setWidth(int width) {
		if (loaded) reload();
		shouldUpdateWidth = false;
		this.width = width;
	}
	
    @Override
	public void onLoad(GL10 gl, QuickTiGame2dGameView view) {
		this.view = view;
		
    	loadTextData(gl);
    	
    	hasTexture = true;
    	
    	createTextureBuffer(gl);
    	bindVertex(gl);
    	
		shouldReload = false;
		loaded = true;
	}

    @Override
	public void onDrawFrame(GL10 gl, boolean fpsTimeElapsed) {
    	if (shouldReload) {
    		labelTexture.onDispose(gl);
    		loadTextData(gl);
    		bindVertex(gl);
    		shouldReload = false;
    	}
    	super.onDrawFrame(gl, fpsTimeElapsed);
    }

    @Override
	public QuickTiGame2dTexture getTexture() {
    	return labelTexture;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFace) {
		this.fontFamily = fontFace;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public Layout.Alignment getTextAlign() {
		return textAlign;
	}

	public void setTextAlign(Layout.Alignment textAlign) {
		this.textAlign = textAlign;
	}
}
