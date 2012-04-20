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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.googlecode.quicktigame2d.QuickTiGame2dMapSprite;
import com.googlecode.quicktigame2d.QuickTiGame2dMapTile;
import com.googlecode.quicktigame2d.Quicktigame2dModule;

@Kroll.proxy(creatableInModule=Quicktigame2dModule.class)
public class MapSpriteProxy extends SpriteProxy {
	
	public MapSpriteProxy() {
		sprite = new QuickTiGame2dMapSprite();
	}
	
	public void onNotification(KrollDict info) {
		if (info.getString("eventName").equals("onload")) {
	        if (sprite.getWidth()  == 0) sprite.setWidth(info.getInt("width"));
	        if (sprite.getHeight() == 0) sprite.setHeight(info.getInt("height"));
	        
	        info.remove("width");
	        info.remove("height");
		}
		
		super.onNotification(info);
	}

	private QuickTiGame2dMapSprite getMapSprite() {
		return (QuickTiGame2dMapSprite)sprite;
	}
	
	@Override
    public void handleCreationDict(KrollDict options) {
    	super.handleCreationDict(options);
    	if (options.containsKey("border")) {
    		setBorder(options.getInt("border"));
    	}
    	if (options.containsKey("margin")) {
    		setMargin(options.getInt("margin"));
    	}
    	if (options.containsKey("tileWidth")) {
    		setTileWidth(options.getInt("tileWidth"));
    	}
    	if (options.containsKey("tileHeight")) {
    		setTileHeight(options.getInt("tileHeight"));
    	}
    }

	@SuppressWarnings("rawtypes")
	@Kroll.method
	public HashMap getTile(int index) {
		HashMap<String, Object> info = new HashMap<String, Object>();
		
		QuickTiGame2dMapTile tile = getMapSprite().getTile(index);
		
		info.put("index", Integer.valueOf(index));
		info.put("gid",   Integer.valueOf(tile.gid));
		info.put("red",   Double.valueOf(tile.red));
		info.put("green", Double.valueOf(tile.green));
		info.put("blue",  Double.valueOf(tile.blue));
		info.put("alpha", Double.valueOf(tile.alpha));
		info.put("flip",  Boolean.valueOf(tile.flip));
		
		info.put("screenX",  Double.valueOf(getMapSprite().getX() + tile.initialX + tile.offsetX));
		info.put("screenY",  Double.valueOf(getMapSprite().getY() + tile.initialY + tile.offsetY));
		info.put("width",    Double.valueOf(tile.width  > 0 ? tile.width  : getMapSprite().getWidth()));
		info.put("height",   Double.valueOf(tile.height > 0 ? tile.height : getMapSprite().getHeight()));
		info.put("margin",   Double.valueOf(tile.margin));
		info.put("border",   Double.valueOf(tile.border));
		
		
		return info;
	}
	
	@Kroll.method
	public boolean updateTile(@SuppressWarnings("rawtypes") HashMap info) {
		int index = -1;
		int gid   = -1;
		float red   = -1;
		float green = -1;
		float blue  = -1;
		float alpha = -1;
		
		if (info.containsKey("index")) {
			index = TiConvert.toInt(info.get("index"));
		}
		if (info.containsKey("gid")) {
			gid = TiConvert.toInt(info.get("gid"));
		}
		if (info.containsKey("red")) {
			red = (float)TiConvert.toDouble(info.get("red"));
		}
		if (info.containsKey("green")) {
			green = (float)TiConvert.toDouble(info.get("green"));
		}
		if (info.containsKey("blue")) {
			blue = (float)TiConvert.toDouble(info.get("blue"));
		}
		if (info.containsKey("alpha")) {
			alpha = (float)TiConvert.toDouble(info.get("alpha"));
		}

	    if (index >= getMapSprite().getTileCount()) {
	        return false;
	    }
	    
	    QuickTiGame2dMapTile tile = getMapSprite().getTile(index);
	    
	    if (gid   >= 0) tile.gid   = gid;
	    if (red   >= 0) tile.red   = red;
	    if (green >= 0) tile.green = green;
	    if (blue  >= 0) tile.blue  = blue;
	    if (alpha >= 0) tile.alpha = alpha;
	    
		if (info.containsKey("flip")) {
			tile.flip = TiConvert.toBoolean(info.get("flip"));
		}
	    
	    getMapSprite().setTile(index, tile);
	    
	    return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Kroll.method
	public boolean updateTiles(List list) {
		if (list.get(0) instanceof Map) {
			List<Integer> data = new ArrayList<Integer>();
			
			for (Object e : list) {
				Map tile = (Map)e;
				if (tile.get("gid") == null) continue;
				data.add(Integer.valueOf(tile.get("gid").toString()));
			}
			getMapSprite().setTiles(data);
		} else {
			getMapSprite().setTiles(list);
		}
		
		return true;
	}
	
	@Kroll.method
	public boolean removeTile(int index) {
	    return getMapSprite().removeTile(index);
	}
	
	@Kroll.method
	public boolean flipTile(int index) {
	    return getMapSprite().flipTile(index);
	}
	
	@Kroll.getProperty @Kroll.method
	public int getBorder() {
		return sprite.getBorder();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setBorder(int border) {
		sprite.setBorder(border);
	}

	@Kroll.getProperty @Kroll.method
	public int getMargin() {
		return sprite.getMargin();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setMargin(int margin) {
		sprite.setMargin(margin);
	}
	
	@Kroll.getProperty @Kroll.method
	public float getTileWidth() {
		return getMapSprite().getTileWidth();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTileWidth(float tileWidth) {
		getMapSprite().setTileWidth(tileWidth);
		getMapSprite().updateTileCount();
	}
	
	@Kroll.getProperty @Kroll.method
	public float getTileHeight() {
		return getMapSprite().getTileHeight();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTileHeight(float tileHeight) {
		getMapSprite().setTileHeight(tileHeight);
		getMapSprite().updateTileCount();
	}
	
	@Kroll.getProperty @Kroll.method
	public int getFirstgid() {
		return getMapSprite().getFirstgid();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setFirstgid(int firstgid) {
		getMapSprite().setFirstgid(firstgid);
	}

	@Kroll.getProperty @Kroll.method
	public int getOrientation() {
		return getMapSprite().getOrientation();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setOrientation(int orientation) {
		getMapSprite().setOrientation(orientation);
		getMapSprite().updateTileCount();
	}
	
	@Kroll.getProperty @Kroll.method
	public int getTileCount() {
		return getMapSprite().getTileCount();
	}
	
	@Kroll.getProperty @Kroll.method
	public int getTileCountX() {
		return getMapSprite().getTileCountX();
	}
	
	@Kroll.getProperty @Kroll.method
	public int getTileCountY() {
		return getMapSprite().getTileCountY();
	}

	@Kroll.setProperty @Kroll.method
	public void setWidth(int width) {
		super.setWidth(width);
		getMapSprite().updateTileCount();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setHeight(int height) {
		super.setHeight(height);
		getMapSprite().updateTileCount();
	}
	
	
	@Kroll.getProperty @Kroll.method
	public Integer[] getTiles() {
		List<Integer> tiles = getMapSprite().getTiles();
		return tiles.toArray(new Integer[tiles.size()]);
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTiles(Object[] tiles) {
		List<Integer> data = new ArrayList<Integer>();
		for (int i = 0; i < tiles.length; i++) {
			data.add(Integer.valueOf((int)Double.parseDouble((tiles[i].toString()))));
		}
		
		updateTiles(data);
	}
	
	@Kroll.getProperty @Kroll.method
	public float getTileTiltFactorX() {
		return getMapSprite().getTileTiltFactorX();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTileTiltFactorX(float value) {
		getMapSprite().setTileTiltFactorX(value);
	}

	@Kroll.getProperty @Kroll.method
	public float getTileTiltFactorY() {
		return getMapSprite().getTileTiltFactorY();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTileTiltFactorY(float value) {
		getMapSprite().setTileTiltFactorY(value);
	}

	@Kroll.getProperty @Kroll.method
	public Map<String, Map<String, String>> getTilesets() {
		return getMapSprite().getTilesets();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTilesets(Object[] args) {
		for (int i = 0; i < args.length; i++) {
			Map<String, String> param = new HashMap<String, String>();
			param.put("offsetX", "0");
			param.put("offsetY", "0");
			
			@SuppressWarnings("rawtypes")
			Map info = (Map) args[i];
			
			for (Object key : info.keySet()) {
				if ("atlas".equals(key)) {
					@SuppressWarnings("rawtypes")
					Map atlasValues = (Map) info.get(key);
					for (Object atlasKey : atlasValues.keySet()) {
						if ("x".equals(atlasKey)) {
							param.put("atlasX", String.valueOf(atlasValues.get(atlasKey)));
						} else if ("y".equals(atlasKey)) {
							param.put("atlasY", String.valueOf(atlasValues.get(atlasKey)));
						} else if ("w".equals(atlasKey)) {
							param.put("atlasWidth", String.valueOf(atlasValues.get(atlasKey)));
						} else if ("h".equals(atlasKey)) {
							param.put("atlasHeight", String.valueOf(atlasValues.get(atlasKey)));
						}
					}
				} else {
					param.put(String.valueOf(key), String.valueOf(info.get(key)));
				}
			}
			
			getMapSprite().addTileset(param);
		}
	}

	@Kroll.method
	public void stop(int tileIndex) {
		getMapSprite().deleteAnimation(String.valueOf(tileIndex));
	}

	@Kroll.method
	public void animate(int tileIndex, Object arg1, int arg2, int arg3, int arg4) {
		if (arg1.getClass().isArray()) {
			Object[] framesObj = (Object[])arg1;
			
			int[] frames = new int[framesObj.length];
			for (int i = 0; i < frames.length; i++) {
				frames[i] = (int)Double.parseDouble(framesObj[i].toString());
			}
			
			getMapSprite().animateTile(tileIndex, frames, arg2, arg3);
		} else {
			getMapSprite().animateTile(tileIndex, (int)Double.parseDouble(arg1.toString()), arg2, arg3, arg4);
		}
	}

}
