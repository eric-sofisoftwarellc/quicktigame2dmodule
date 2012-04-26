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
package com.googlecode.quicktigame2d.proxy;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import com.googlecode.quicktigame2d.GameView;
import com.googlecode.quicktigame2d.GameViewEventListener;
import com.googlecode.quicktigame2d.QuickTiGame2dCameraInfo;
import com.googlecode.quicktigame2d.QuickTiGame2dConstant;
import com.googlecode.quicktigame2d.QuickTiGame2dTransform;
import com.googlecode.quicktigame2d.Quicktigame2dModule;

import android.app.Activity;
import android.util.Log;

@Kroll.proxy(creatableInModule=Quicktigame2dModule.class)
public class GameViewProxy extends TiViewProxy implements GameViewEventListener {

	private Stack<SceneProxy> sceneStack = new Stack<SceneProxy>();
	private HashMap<String, Object> screenInfoCache = new HashMap<String, Object>();
	private HashMap<String, Object> cameraInfoCache = new HashMap<String, Object>();

	private TransformProxy cameraTransform;
	
	// Constructor
	public GameViewProxy() {
		super();
	}

    @Override
    public void handleCreationDict(KrollDict options) {
    	super.handleCreationDict(options);
    	if (options.containsKey("debug")) {
    		setDebug(options.getBoolean("debug"));
    	}
    	if (options.containsKey("fps")) {
    		setFps(options.getDouble("fps").floatValue());
    	}
    	if (options.containsKey("orientation")) {
    		setOrientation(options.getInt("orientation").intValue());
    	}
    	if (options.containsKey("enableOnDrawFrameEvent")) {
    		setEnableOnDrawFrameEvent(options.getBoolean("enableOnDrawFrameEvent"));
    	}
    	if (options.containsKey("enableOnFpsEvent")) {
    		setEnableOnFpsEvent(options.getBoolean("enableOnFpsEvent"));
    	}
    	if (options.containsKey("onFpsInterval")) {
    		setOnFpsInterval(options.getInt("onFpsInterval").intValue());
    	}
    	if (options.containsKey("correctionHint")) {
    		setCorrectionHint(options.getInt("correctionHint").intValue());
    	}
    	if (options.containsKey("textureFilter")) {
    		setTextureFilter(options.getInt("textureFilter").intValue());
    	}
    }
    
    @Override
    public void setActivity(Activity activity) {
        super.setActivity(activity);
            
    	if (activity instanceof TiBaseActivity) {
    		((TiBaseActivity)activity).addOnLifecycleEventListener(getView());
    	}
    }
	
	@Override
	public TiUIView createView(Activity activity) {
		GameView view = new GameView(this);
		view.addListener(this);
		return view;
	}
	
	public GameView getView() {
		return (GameView)getOrCreateView();
	}
	
	@Kroll.method
	public void startCurrentScene() {
		getView().startCurrentScene();
	}
	
	@Kroll.method
	public void loadTexture(String texture) {
		getView().loadTexture(texture);
	}

	@Kroll.method
	public void unloadTexture(String texture) {
		getView().unloadTexture(texture);
	}

	@Kroll.method
	public void start() {
		getView().start();
	}

	@Kroll.method
	public void stop() {
		getView().stop();
	}
	
	@Kroll.method
	public void pause() {
		getView().pause();
	}
	
	@Kroll.method
	public void color(float red, float green, float blue) {
		getView().color(red, green, blue);
	}
	
	@Kroll.method
	public void alpha(float alpha) {
		getView().alpha(alpha);
	}

	@Kroll.method
	public SceneProxy pushScene(SceneProxy scene) {
		if (topScene() != null) topScene().onDeactivate();
		
		getView().pushScene(scene.getScene());
		sceneStack.push(scene);
		
		scene.onActivate();
		return scene;
	}
	
	@Kroll.method
	public SceneProxy popScene() {
		getView().popScene();
		try {
			SceneProxy proxy = sceneStack.pop();
			proxy.onDeactivate();
			if (topScene() != null) topScene().onActivate();
			
			return proxy;
		} catch (EmptyStackException e) {
			return null;
		}
	}
	
	@Kroll.method
	public SceneProxy topScene() {
		try {
			return sceneStack.peek();
		} catch (EmptyStackException e) {
			return null;
		}
	}
	
	@Kroll.method
	public SceneProxy replaceScene(SceneProxy scene) {
		if (topScene() != null) topScene().onDeactivate();
		
		try {
			sceneStack.pop();
		} catch (EmptyStackException e) {
			// do nothing
		}
		
		getView().replaceScene(scene.getScene());
		sceneStack.push(scene);
		scene.onActivate();
		
		return topScene();
	}
	
	@Kroll.method
	public double uptime() {
		return getView().uptime();
	}

	@Kroll.method
	public void snapshot() {
		getView().snapshot();
	}
	
	@Kroll.method
	public void releaseSnapshot() {
		getView().releaseSnapshot();
	}
	
	@Kroll.method
	public void add(Object obj) {
		throw new UnsupportedOperationException("QuickTiGame2d GameView.add is removed and can not be used from version 0.3");
	}
	
	@Kroll.method
	public void addHUD(SpriteProxy sprite) {
		getView().addHUD(sprite.getSprite());
	}
	
	@Kroll.method
	public void removeHUD(SpriteProxy sprite) {
		getView().removeHUD(sprite.getSprite());
	}
	
	@Kroll.method
	public void resetCamera() {
		getView().resetCamera();
	}
	
	@Kroll.method
	public void moveCamera(TransformProxy cameraTransform) {
		this.cameraTransform = cameraTransform;
		getView().moveCamera(this.cameraTransform.getTransformer());
	}

	@Kroll.setProperty @Kroll.method
	public void setScreen(@SuppressWarnings("rawtypes") HashMap info) {
		if (info.containsKey("width")) {
			int width = TiConvert.toInt(info.get("width"));
			if (width > 0) getView().setGameViewWidth(width);
		}
		if (info.containsKey("height")) {
			int height = TiConvert.toInt(info.get("height"));
			if (height > 0) getView().setGameViewHeight(height);
		}
		if (info.containsKey("viewportWidth")) {
			int width = TiConvert.toInt(info.get("viewportWidth"));
			if (width > 0) getView().setGameViewportWidth(width);
		}
		if (info.containsKey("viewportHeight")) {
			int height = TiConvert.toInt(info.get("viewportHeight"));
			if (height > 0) getView().setGameViewportHeight(height);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Kroll.getProperty @Kroll.method
	public HashMap getScreen() {
		screenInfoCache.put("width" , getView().getGameViewWidth());
		screenInfoCache.put("height", getView().getGameViewHeight());
		screenInfoCache.put("framebufferWidth" , getView().getFramebufferWidth());
		screenInfoCache.put("framebufferHeight", getView().getFramebufferHeight());
		screenInfoCache.put("viewportWidth" , getView().getGameViewportWidth());
		screenInfoCache.put("viewportHeight", getView().getGameViewportHeight());
		
		return screenInfoCache;
	}
	
	@Kroll.setProperty @Kroll.method
	public void setCamera(@SuppressWarnings("rawtypes") HashMap info) {
		QuickTiGame2dCameraInfo camera = getView().getCamera();
		
		if (info.containsKey("eyeX")) {
			camera.eyeX = TiConvert.toFloat(info.get("eyeX"));
		}
		if (info.containsKey("eyeY")) {
			camera.eyeY = TiConvert.toFloat(info.get("eyeY"));
		}
		if (info.containsKey("eyeZ")) {
			camera.eyeZ = TiConvert.toFloat(info.get("eyeZ"));
		}
		if (info.containsKey("centerX")) {
			camera.centerX = TiConvert.toFloat(info.get("centerX"));
		}
		if (info.containsKey("centerY")) {
			camera.centerY = TiConvert.toFloat(info.get("centerY"));
		}
		if (info.containsKey("centerZ")) {
			camera.centerZ = TiConvert.toFloat(info.get("centerZ"));
		}
		
		getView().setCamera(camera);
	}
	
	@SuppressWarnings("rawtypes")
	@Kroll.getProperty @Kroll.method
	public HashMap getCamera() {
		QuickTiGame2dCameraInfo camera = getView().getCamera();
		
		cameraInfoCache.put("eyeX" , camera.eyeX);
		cameraInfoCache.put("eyeY",  camera.eyeY);
		cameraInfoCache.put("eyeZ",  camera.eyeZ);
		cameraInfoCache.put("centerX", camera.centerX);
		cameraInfoCache.put("centerY", camera.centerY);
		cameraInfoCache.put("centerZ", camera.centerZ);
		cameraInfoCache.put("upX", camera.upX);
		cameraInfoCache.put("upY", camera.upY);
		cameraInfoCache.put("upZ", camera.upZ);
		cameraInfoCache.put("zNear", camera.zNear);
		cameraInfoCache.put("zFar",  camera.zFar);
		
		return cameraInfoCache;
	}
	
	@Kroll.setProperty @Kroll.method
	public void setFps(float fps) {
		getView().setFps(fps);
	}
	
	@Kroll.getProperty @Kroll.method
	public float getFps() {
		return getView().getFps();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setDebug(boolean enabled) {
		getView().setDebug(enabled);
	}
	
	@Kroll.getProperty @Kroll.method
	public boolean getDebug() {
		return getView().getDebug();
	}

	@Kroll.setProperty @Kroll.method
	public void setOrientation(int orientation) {
		getView().setOrientation(orientation);
	}
	
	@Kroll.getProperty @Kroll.method
	public int getOrientation() {
		return getView().getOrientation();
	}

	@Kroll.setProperty @Kroll.method
	public void setEnableOnDrawFrameEvent(boolean enabled) {
		getView().enableOnDrawFrameEvent(enabled);
	}
	
	@Kroll.getProperty @Kroll.method
	public boolean getEnableOnDrawFrameEvent() {
		return getView().isOnDrawFrameEventEnabled();
	}

	@Kroll.setProperty @Kroll.method
	public void setEnableOnFpsEvent(boolean enabled) {
		getView().enableOnFpsEvent(enabled);
	}
	
	@Kroll.getProperty @Kroll.method
	public boolean getEnableOnFpsEvent() {
		return getView().isOnFpsEventEnabled();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setOnFpsInterval(int onFpsInterval) {
		getView().setOnFpsInterval(onFpsInterval);
	}
	
	@Kroll.getProperty @Kroll.method
	public int getOnFpsInterval() {
		return getView().getOnFpsInterval();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setCorrectionHint(int hint) {
		getView().setCorrectionHint(hint);
	}
	
	@Kroll.getProperty @Kroll.method
	public int getCorrectionHint() {
		return getView().getCorrectionHint();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTextureFilter(int filter) {
		getView().setTextureFilter(filter);
	}
	
	@Kroll.getProperty @Kroll.method
	public int getTextureFilter() {
		return getView().getTextureFilter();
	}

	@Kroll.setProperty @Kroll.method
	public void setUseFastTimer(boolean enabled) {
		// this property is only available on iOS so do nothing here
		Log.w(Quicktigame2dModule.LOG_TAG, "gameview.useFastTimer is deprecated. Use gameview.timerType instead.");
	}
	
	@Kroll.getProperty @Kroll.method
	public boolean getUseFastTimer() {
		return false;
	}

	@Kroll.setProperty @Kroll.method
	public void setTimerType(int type) {
		// this property is only available on iOS so do nothing here
	}
	
	@Kroll.getProperty @Kroll.method
	public int getTimerType() {
		return QuickTiGame2dConstant.TIMER_DEFAULT;
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		KrollDict notificationEventCache = new KrollDict();
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("eventName", "onsurfacechanged");
		notificationEventCache.put("width",  getView().getGameViewWidth());
		notificationEventCache.put("height", getView().getGameViewHeight());
		this.fireEvent("onsurfacechanged", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onLoad() {
		KrollDict notificationEventCache = new KrollDict();
		if (getDebug()) Log.d(Quicktigame2dModule.LOG_TAG, "GameViewProxy.onLoad");
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("eventName", "onload");
		notificationEventCache.put("width",  getView().getGameViewWidth());
		notificationEventCache.put("height", getView().getGameViewHeight());
		this.fireEvent("onload", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onDrawFrame(int delta) {
		KrollDict notificationEventCache = new KrollDict();
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("delta", delta);
		notificationEventCache.put("eventName", "enterframe");
		this.fireEvent("enterframe", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onFps(int delta, float fps) {
		if (!getView().isOnFpsEventEnabled()) return;
		
		KrollDict notificationEventCache = new KrollDict();
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("delta", delta);
		notificationEventCache.put("fps",   fps);
		notificationEventCache.put("eventName", "onfps");
		this.fireEvent("onfps", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}
	
	@Override
	public void onLoadSprite(String name) {
		KrollDict notificationEventCache = new KrollDict();
		if (getDebug()) Log.d(Quicktigame2dModule.LOG_TAG, String.format("GameViewProxy.onLoadSprite %s", name));
		notificationEventCache.put("eventName", "onloadsprite");
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("name", name);
		this.fireEvent("onloadsprite", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onUnloadSprite(String name) {
		KrollDict notificationEventCache = new KrollDict();
		if (getDebug()) Log.d(Quicktigame2dModule.LOG_TAG, String.format("GameViewProxy.onUnloadSprite %s", name));
		notificationEventCache.put("eventName", "onunloadsprite");
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("name", name);
		this.fireEvent("onunloadsprite", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onDispose() {
		KrollDict notificationEventCache = new KrollDict();
		if (getDebug()) Log.d(Quicktigame2dModule.LOG_TAG, "GameViewProxy.onDispose");
		notificationEventCache.put("eventName", "ondispose");
		notificationEventCache.put("uptime", uptime());
		this.fireEvent("ondispose", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onGainedFocus() {
		KrollDict notificationEventCache = new KrollDict();
		if (getDebug()) Log.d(Quicktigame2dModule.LOG_TAG, "GameViewProxy.onGainedFocus");
		notificationEventCache.put("eventName", "ongainedfocus");
		notificationEventCache.put("uptime", uptime());
		this.fireEvent("ongainedfocus", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onLostFocus() {
		KrollDict notificationEventCache = new KrollDict();
		if (getDebug()) Log.d(Quicktigame2dModule.LOG_TAG, "GameViewProxy.onLostFocus");
		notificationEventCache.put("eventName", "onlostfocus");
		notificationEventCache.put("uptime", uptime());
		this.fireEvent("onlostfocus", notificationEventCache);
		if (topScene() != null) topScene().onNotification(notificationEventCache);
	}

	@Override
	public void onStartTransform(QuickTiGame2dTransform transform) {
		KrollDict notificationEventCache = new KrollDict();
		notificationEventCache.put("eventName", "onstarttransform");
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("source", transform);
		
		fireOnCameraTransformNotification(notificationEventCache);
		
		if (topScene() != null) topScene().onTransformNotification(notificationEventCache);
	}

	@Override
	public void onCompleteTransform(QuickTiGame2dTransform transform) {
		KrollDict notificationEventCache = new KrollDict();
		notificationEventCache.put("eventName", "oncompletetransform");
		notificationEventCache.put("uptime", uptime());
		notificationEventCache.put("source", transform);
		
		fireOnCameraTransformNotification(notificationEventCache);

		if (topScene() != null) topScene().onTransformNotification(notificationEventCache);
	}
	
	private void fireOnCameraTransformNotification(KrollDict info) {
		if (cameraTransform != null) {
			Object source = info.get("source");
			if (cameraTransform.getTransformer() == source) {

				KrollDict eventParam = new KrollDict();
				eventParam.putAll(info);
				eventParam.remove("source");
				
				String name = "unknown";
				if (info.getString("eventName").equals("onstarttransform")) {
					name = "start";
				} else if (info.getString("eventName").equals("oncompletetransform")) {
					name = "complete";
				}

				cameraTransform.fireEvent(name, eventParam);

				if (name.equals("complete")) {
					cameraTransform = null;
				}
			}
		}
	}
}