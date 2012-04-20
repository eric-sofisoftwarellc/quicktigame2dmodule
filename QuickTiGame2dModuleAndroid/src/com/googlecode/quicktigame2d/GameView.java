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

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;

public class GameView extends TiUIView implements OnLifecycleEvent {
	
	public GameView(TiViewProxy proxy) {
		super(proxy);
		setNativeView(new QuickTiGame2dGameView(proxy.getActivity()));
	}

	@Override
	public void processProperties(KrollDict d) {
		super.processProperties(d);
	}
	
	public QuickTiGame2dGameView getGameView() {
		return (QuickTiGame2dGameView)nativeView;
	}
	
	public void startCurrentScene() {
		getGameView().startCurrentScene();
	}
	
	public void loadTexture(String texture) {
		getGameView().commitLoadTexture(texture);
	}

	public void unloadTexture(String texture) {
		getGameView().commitUnloadTexture(texture);
	}
	
	public void start() {
		getGameView().start();
	}
	
	public void stop() {
		getGameView().stop();
	}
	
	public void pause() {
		getGameView().pause();
	}
	
	public void snapshot() {
		getGameView().snapshot();
	}
	
	public void releaseSnapshot() {
		getGameView().releaseSnapshot();
	}

	public float getFps() {
		return getGameView().getFps();
	}
	
	public void setFps(float fps) {
		getGameView().setFps(fps);
	}
	
	public void setDebug(boolean enabled) {
		getGameView().setDebug(enabled);
	}
	
	public boolean getDebug() {
		return getGameView().getDebug();
	}

	public void color(float red, float green, float blue, float alpha) {
		getGameView().color(red, green, blue, alpha);
	}
	
	public void color(float red, float green, float blue) {
		getGameView().color(red, green, blue);
	}
	
	public void alpha(float alpha) {
		getGameView().alpha(alpha);
	}
	
	public QuickTiGame2dScene pushScene(QuickTiGame2dScene scene) {
		return getGameView().pushScene(scene);
	}

	public QuickTiGame2dScene popScene() {
		return getGameView().popScene();
	}

	public QuickTiGame2dScene topScene() {
		return getGameView().topScene();
	}
	
	public QuickTiGame2dScene replaceScene(QuickTiGame2dScene scene) {
		return getGameView().replaceScene(scene);
	}

	public void setGameViewWidth(int width) {
		getGameView().setGameViewWidth(width);
	}
	
	public void setGameViewHeight(int height) {
		getGameView().setGameViewHeight(height);
	}
	
	public void setGameViewSize(int width, int height) {
		getGameView().setGameViewSize(width, height);
	}
	
	public int getGameViewWidth() {
		return getGameView().getGameViewWidth();
	}
	
	public int getGameViewHeight() {
		return getGameView().getGameViewHeight();
	}
	
	public int getFramebufferWidth() {
		return getGameView().getFramebufferWidth();
	}
	
	public int getFramebufferHeight() {
		return getGameView().getFramebufferHeight();
	}
	
	public void setGameViewportWidth(int value) {
		getGameView().setFramebufferWidth(value);
	}
	
	public int getGameViewportWidth() {
		return getGameView().getFramebufferWidth();
	}
	
	public void setGameViewportHeight(int value) {
		getGameView().setFramebufferHeight(value);
	}
	
	public int getGameViewportHeight() {
		return getGameView().getFramebufferHeight();
	}
	
	public int getOrientation() {
		return getGameView().getOrientation();
	}
	
	public void setOrientation(int orientation) {
		getGameView().setOrientation(orientation);
	}
	
	public double uptime() {
		return QuickTiGame2dGameView.uptime();
	}
	
	@Override
	public void onDestroy(Activity activity) {
		getGameView().onDispose();
	}

	@Override
	public void onPause(Activity activity) {
		getGameView().onLostFocus();
	}

	@Override
	public void onResume(Activity activity) {
		getGameView().onGainedFocus();
	}

	@Override
	public void onStart(Activity activity) {
		// nothing to do
	}

	@Override
	public void onStop(Activity activity) {
		// nothing to do
	}
	
	public void addListener(GameViewEventListener listener) {
		getGameView().addListener(listener);
	}
	
	public void removeListener(GameViewEventListener listener) {
		getGameView().removeListener(listener);
	}
	
	public boolean isOnDrawFrameEventEnabled() {
		return getGameView().isOnDrawFrameEventEnabled();
	}

	public void enableOnDrawFrameEvent(boolean enabled) {
		getGameView().enableOnDrawFrameEvent(enabled);
	}

	public boolean isOnFpsEventEnabled() {
		return getGameView().isOnFpsEventEnabled();
	}

	public void enableOnFpsEvent(boolean enabled) {
		getGameView().enableOnFpsEvent(enabled);
	}
	
	public int getOnFpsInterval() {
		return getGameView().getOnFpsInterval();
	}

	public void setOnFpsInterval(int onFpsInterval) {
		getGameView().setOnFpsInterval(onFpsInterval);
	}

	public QuickTiGame2dCameraInfo getCamera() {
		return getGameView().getCamera();
	}
	
	public void setCamera(QuickTiGame2dCameraInfo camera) {
		getGameView().setCamera(camera);
	}
	
	public void resetCamera() {
		getGameView().resetCamera();
	}
	
	public void moveCamera(QuickTiGame2dTransform transform) {
		getGameView().transformCamera(transform);
	}
	
	public void addHUD(QuickTiGame2dSprite sprite) {
		getGameView().addHUD(sprite);
	}
	
	public void removeHUD(QuickTiGame2dSprite sprite) {
		getGameView().removeHUD(sprite);
	}
	
	public int getCorrectionHint() {
		return QuickTiGame2dGameView.correctionHint;
	}
	
	public void setCorrectionHint(int hint) {
		QuickTiGame2dGameView.correctionHint = hint;
	}
	
	public int getTextureFilter() {
		return QuickTiGame2dGameView.textureFilter;
	}
	
	public void setTextureFilter(int filter) {
		QuickTiGame2dGameView.textureFilter = filter;
	}
}
