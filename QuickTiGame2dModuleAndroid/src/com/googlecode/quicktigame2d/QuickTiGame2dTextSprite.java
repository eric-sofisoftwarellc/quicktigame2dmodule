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
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Typeface;

public class QuickTiGame2dTextSprite extends QuickTiGame2dSprite {

	private QuickTiGame2dTexture labelTexture = null;
	private String text = " ";
	private String fontFace = "";
	private float  fontSize = 0;
	private boolean isBold = false;
	private boolean isItalic = false;
	
	private boolean shouldReload = false;
	
	public QuickTiGame2dTextSprite() {
	}
	
	private void loadTextData(GL10 gl) {
		if (view == null) return;
		
		if (labelTexture == null) {
			labelTexture = new QuickTiGame2dTexture(view.getContext());
			labelTexture.setWidth(1);
			labelTexture.setHeight(1);
		}
		
		Paint forePaint = new Paint();
		Paint backPaint = new Paint();
		
    	if (fontFace.length() == 0) {
    		if (isBold && isItalic) {
    			forePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
    		} else if (isBold) {
    			forePaint.setTypeface(Typeface.DEFAULT_BOLD);
    		}
    	} else {
    		Typeface typeface = TiUIHelper.toTypeface(view.getContext(), fontFace);
    		
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

    	FontMetrics metrics = forePaint.getFontMetrics();
    	int textWidth  = (int)Math.ceil(forePaint.measureText(getText()));
    	int textHeight = (int)Math.ceil(Math.abs(metrics.ascent) + 
    			Math.abs(metrics.descent) + Math.abs(metrics.leading));
    	
    	Bitmap bitmap = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888);
    	Canvas canvas = new Canvas(bitmap);
    	canvas.drawRect(0, 0, textWidth, textHeight, backPaint);
    	canvas.drawText(getText(), 0, textHeight - metrics.descent , forePaint);

    	ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] data = os.toByteArray();
        
		labelTexture.setDebug(view.getDebug());
        labelTexture.setName(text);
        labelTexture.setWidth(textWidth);
        labelTexture.setHeight(textHeight);
        labelTexture.onLoad(gl, data);
        
        setWidth(textWidth);
        setHeight(textHeight);
	}
	
	public void reload() {
		shouldReload = true;
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

	public String getFontFace() {
		return fontFace;
	}

	public void setFontFace(String fontFace) {
		this.fontFace = fontFace;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}
}
