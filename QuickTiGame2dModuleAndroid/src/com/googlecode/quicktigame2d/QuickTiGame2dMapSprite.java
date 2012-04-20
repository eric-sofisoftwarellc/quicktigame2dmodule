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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.appcelerator.kroll.common.Log;

import com.googlecode.quicktigame2d.opengl.GLHelper;

public class QuickTiGame2dMapSprite extends QuickTiGame2dSprite {
    private float[] quads;
    private short[] indices;
    
    private List<QuickTiGame2dMapTile> tiles = new ArrayList<QuickTiGame2dMapTile>();
    private Map<Integer, QuickTiGame2dMapTile> updatedTiles = new HashMap<Integer, QuickTiGame2dMapTile>();
    
    private float tileWidth;
    private float tileHeight;
    
    private int tileCount;
    private int tileCountX;
    private int tileCountY;
    
	private int[] verticesID = new int[1];
    boolean tileChanged = false;

    private FloatBuffer quadsBuffer;
    private ShortBuffer indicesBuffer;
    
    private int firstgid;
    
    private int orientation;
    private float tileTiltFactorX = 1.0f;
    private float tileTiltFactorY = 1.0f;
    
    private Map<String, Map<String, String>> tilesets = new HashMap<String, Map<String, String>>();
    private List<Map<String, String>> tilesetgids = new ArrayList<Map<String, String>>();
    
    public QuickTiGame2dMapSprite() {
		firstgid = 0;
	}
    
    public boolean updateTileCount() {
        if (width == 0 || height == 0 || tileWidth == 0 || tileHeight == 0) return false;
        
	    if (orientation != QuickTiGame2dConstant.MAP_ORIENTATION_HEXAGONAL) {
	    	tileCountX = (int)Math.ceil(width / tileWidth);
	    	tileCountY = (int)Math.ceil(height / tileHeight);
	    	tileCount  = tileCountX * tileCountY;
	    } else {
	        tileCountX = (int)Math.ceil(width / tileWidth);
	        tileCountY = (int)Math.ceil(height / (tileHeight * tileTiltFactorY));
	        
	        tileCount = (tileCountX * tileCountY) - (tileCountY / 2);
	    }
	    return true;
    }
    
	public void onLoad(GL10 gl, QuickTiGame2dGameView view) {
		if (loaded) return;
		
		super.onLoad(gl, view);
		
	    if (tileWidth  == 0) tileWidth  = width;
	    if (tileHeight == 0) tileHeight = height;
	    
	    if (updateTileCount()) {
	    	createQuadBuffer(gl);
	    }
	}
	
	public void onDrawFrame(GL10 gl10, boolean fpsTimeElapsed) {
		GL11 gl = (GL11)gl10;

	    synchronized (transforms) {
			if (fpsTimeElapsed) {
				onTransform();
			}
	    }
	    
	    synchronized (updatedTiles) {
	        tileChanged = updatedTiles.size() > 0;
	        if (tileChanged) {
	            for (Map.Entry<Integer, QuickTiGame2dMapTile> e : updatedTiles.entrySet()) {
	                updateQuad(e.getKey().intValue(), e.getValue());
	            }
	            updatedTiles.clear();
	        }
	    }
	    
	    synchronized (animations) {
			if (animating && animations.size() > 0) {
				for (String name : animations.keySet()) {
					double uptime = QuickTiGame2dGameView.uptime();
					QuickTiGame2dAnimationFrame animation = animations.get(name);
					if (animation.getLastOnAnimationDelta(uptime) < animation.getInterval()) {
						continue;
					}
					int index = animation.getNameAsInt();
					if (index >= 0) {
						QuickTiGame2dMapTile updatedTile = getTile(index);
						updatedTile.gid = animation.getNextIndex(tileCount, updatedTile.gid);
						
					    synchronized (updatedTiles) {
					    	setTile(index, updatedTile);
					    }
					}
					animation.setLastOnAnimationInterval(uptime);
				}
			}
	    }
		
	    gl.glMatrixMode(GL11.GL_MODELVIEW);
	    gl.glLoadIdentity(); 
	    
	    gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
	    
	    // unbind all buffers
	    gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	    gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
	    gl.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	    // update position
	    gl.glTranslatef(x * orthFactorX, y * orthFactorY, 0);
		
	    // rotate angle, center x, center y, center z, axis
	    gl.glTranslatef(param_rotate[1], param_rotate[2], param_rotate[3]);
	    if (param_rotate[4] == QuickTiGame2dConstant.AXIS_X) {
	    	gl.glRotatef(param_rotate[0], 1, 0, 0);
	    } else if (param_rotate[4] == QuickTiGame2dConstant.AXIS_Y) {
	    	gl.glRotatef(param_rotate[0], 0, 1, 0);
	    } else {
	    	gl.glRotatef(param_rotate[0], 0, 0, 1);
	    }
	    gl.glTranslatef(-param_rotate[1], -param_rotate[2], -param_rotate[3]);
		
	    // scale param x, y, z, center x, center y, center z
	    gl.glTranslatef(param_scale[3], param_scale[4], param_scale[5]);
	    gl.glScalef(param_scale[0], param_scale[1], param_scale[2]);
	    gl.glTranslatef(-param_scale[3], -param_scale[4], -param_scale[5]);
	    
	    gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, verticesID[0]);
	    
	    if (tileChanged) {
		    quadsBuffer.put(quads);
		    quadsBuffer.position(0);
			gl.glBufferData(GL11.GL_ARRAY_BUFFER, 128 * tileCount, quadsBuffer, GL11.GL_STATIC_DRAW);
	    	tileChanged = false;
	    }
	    
		// Configure the vertex pointer which will use the currently bound VBO for its data
	    gl.glVertexPointer(2, GL11.GL_FLOAT, 32, 0);
	    gl.glColorPointer(4, GL11.GL_FLOAT,  32,   (4 * 4));
	    gl.glTexCoordPointer(2, GL11.GL_FLOAT, 32, (4 * 2));

		if (hasTexture) {
			gl.glEnable(GL11.GL_TEXTURE_2D);
			gl.glBindTexture(GL11.GL_TEXTURE_2D, getTexture().getTextureId());
	    }
		
		gl.glBlendFunc(srcBlendFactor, dstBlendFactor);
		
		gl.glDrawElements(GL11.GL_TRIANGLES, tileCount * 6, GL11.GL_UNSIGNED_SHORT, indicesBuffer);
	    
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    
		gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	public void onDispose() {
		super.onDispose();
	}

	protected void bindVertex(GL10 gl10) {
	    // overwrite parent function..to do nothing
	}
	
	// disable frame animation
	public boolean setFrameIndex(int index, boolean force) {
		return true;
	}
	
	private void createQuadBuffer(GL10 gl10) {
	    
		GL11 gl = (GL11)gl10;
		
	    quads     = new float[32 * tileCount];
	    indices   = new short[tileCount * 6];
	    
	    tiles.clear();
	    
	    for( int i = 0; i < tileCount; i++) {
			indices[i * 6 + 0] = (short) (i * 4 + 0);
			indices[i * 6 + 1] = (short) (i * 4 + 1);
			indices[i * 6 + 2] = (short) (i * 4 + 2);
			
			indices[i * 6 + 3] = (short) (i * 4 + 2);
			indices[i * 6 + 4] = (short) (i * 4 + 3);
			indices[i * 6 + 5] = (short) (i * 4 + 0);
			
			QuickTiGame2dMapTile tile = new QuickTiGame2dMapTile();
			tile.alpha = 0;
			tiles.add(tile);
			
			updateQuad(i, tile);
		}

	    //
		// initialize texture vertex
	    //
	    int index = 0;
	    for(int ty = 0; ty < tileCountY; ty++) {
	        for (int tx = 0; tx < tileCountX; tx++) {
	            int vi = index * 32;
	            
	            if (orientation == QuickTiGame2dConstant.MAP_ORIENTATION_ISOMETRIC) {
	            	float iso_startX = (tx * tileTiltFactorX  * tileWidth)  - (ty * tileTiltFactorX  * tileWidth);
	            	float iso_startY = (ty * tileTiltFactorY * tileHeight) + (tx * tileTiltFactorY * tileHeight);

	            	quads[vi + 0] = iso_startX;  // vertex  x
	            	quads[vi + 1] = iso_startY;  // vertex  y

	            	// -----------------------------
	            	quads[vi + 8] = iso_startX;              // vertex  x
	            	quads[vi + 9] = iso_startY + tileHeight; // vertex  y

	            	// -----------------------------
	            	quads[vi + 16] = iso_startX + tileWidth; // vertex  x
	            	quads[vi + 17] = iso_startY + tileHeight; // vertex  y

	            	// -----------------------------
	            	quads[vi + 24] = iso_startX + tileWidth; // vertex  x
	            	quads[vi + 25] = iso_startY;             // vertex  y
	            	
	            } else if (orientation == QuickTiGame2dConstant.MAP_ORIENTATION_HEXAGONAL) {
	                if (ty % 2 == 1 && tx >= tileCountX - 1) {
	                    continue;
	                } else if (index >= tileCount) {
	                    break;
	                }
	                float hex_startX = ((ty % 2) * tileWidth * tileTiltFactorX) + (tx * tileWidth);
	                float hex_startY = (ty * tileTiltFactorY * tileHeight);
	                
	                quads[vi + 0] = hex_startX;  // vertex  x
	                quads[vi + 1] = hex_startY;  // vertex  y
	                
	                // -----------------------------
	                quads[vi + 8] = hex_startX;              // vertex  x
	                quads[vi + 9] = hex_startY + tileHeight; // vertex  y
	                
	                // -----------------------------
	                quads[vi + 16] = hex_startX + tileWidth; // vertex  x
	                quads[vi + 17] = hex_startY + tileHeight; // vertex  y
	                
	                // -----------------------------
	                quads[vi + 24] = hex_startX + tileWidth; // vertex  x
	                quads[vi + 25] = hex_startY;             // vertex  y
	            	
	            } else {
	            	quads[vi + 0] = tx * tileWidth;  // vertex  x
	            	quads[vi + 1] = ty * tileHeight; // vertex  y

	            	// -----------------------------
	            	quads[vi + 8] = (tx * tileWidth); // vertex  x
	            	quads[vi + 9] = (ty * tileHeight) + tileHeight; // vertex  y

	            	// -----------------------------
	            	quads[vi + 16] = (tx * tileWidth)  + tileWidth;  // vertex  x
	            	quads[vi + 17] = (ty * tileHeight) + tileHeight; // vertex  y

	            	// -----------------------------
	            	quads[vi + 24] = (tx * tileWidth) + tileWidth;  // vertex  x
	            	quads[vi + 25] = (ty * tileHeight);  // vertex  y
	            }
	            index++;
	        }
		}
	    
	    quadsBuffer = GLHelper.createFloatBuffer(quads);
	    indicesBuffer = GLHelper.createShortBuffer(indices);

	    // Generate the vertices VBO
		gl.glGenBuffers(1, verticesID, 0);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, verticesID[0]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 128 * tileCount, quadsBuffer, GL11.GL_STATIC_DRAW);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
	}

	private float tex_coord_startX(QuickTiGame2dMapTile tile) {
		int tileNo = tile.gid - tile.firstgid;
		
		if (tilesets.size() > 1) {
	        float awidth = tile.atlasWidth > 0 ? tile.atlasWidth : width;
	        float twidth = tile.width > 0 ? tile.width : tileWidth;
	        
	        int xcount = (int)Math.round((awidth - (tile.margin * 2) + tile.border) / (float)(twidth  + tile.border));
	        int xindex = tileNo % xcount;
	        return tile.atlasX + ((tile.border + twidth) * xindex) + tile.margin;
		} else {
			int xcount = (int)Math.round((getTexture().getWidth() - (margin * 2) + border) / (float)(tileWidth  + border));
			int xindex = tileNo % xcount;
			return ((border + tileWidth) * xindex) + margin;
		}
	}

	private float tex_coord_startY(QuickTiGame2dMapTile tile) {
	    int tileNo = tile.gid - tile.firstgid;
	    
		if (tilesets.size() > 1) {
	        float awidth  = tile.atlasWidth  > 0 ? tile.atlasWidth  : width;
	        float aheight = tile.atlasHeight > 0 ? tile.atlasHeight : height;
	        float twidth = tile.width > 0 ? tile.width : tileWidth;
	        float theight = tile.height > 0 ? tile.height : tileHeight;
	        
	        int xcount = (int)Math.round((awidth  - (tile.margin * 2) + tile.border) / (float)(twidth  + tile.border));
	        int ycount = (int)Math.round((aheight - (tile.margin * 2) + tile.border) / (float)(theight + tile.border));
	        int yindex = flipY() ? ycount - (tileNo / xcount) - 1 : (tileNo / xcount);
	                
	        return tile.atlasY + ((tile.border + theight) * yindex) + tile.margin;
		} else {
			int xcount = (int)Math.round((getTexture().getWidth() - (margin * 2) + border) / (float)(tileWidth  + border));
			int ycount = (int)Math.round((getTexture().getHeight() - (margin * 2) + border) / (float)(tileHeight + border));
			int yindex = flipY() ? ycount - (tileNo / xcount) - 1 : (tileNo / xcount);
			return ((border + tileHeight) * yindex) + margin;
		}
	}

	private float tileCoordStartX(QuickTiGame2dMapTile tile) {
	    return tex_coord_startX(tile) / (float)getTexture().getGlWidth() + getTexelHalfX();
	}

	private float tileCoordEndX(QuickTiGame2dMapTile tile) {
	    float twidth = tile.width > 0 ? tile.width : tileWidth;
	    return (float)(tex_coord_startX(tile) + twidth) / (float)getTexture().getGlWidth() - getTexelHalfX();
	}

	private float tileCoordStartY(QuickTiGame2dMapTile tile) {
	    float theight = tile.height > 0 ? tile.height : tileHeight;
	    return (float)(tex_coord_startY(tile) + theight) / (float)getTexture().getGlHeight() - getTexelHalfY();
	}

	private float tileCoordEndY(QuickTiGame2dMapTile tile) {
	    return tex_coord_startY(tile) / (float)getTexture().getGlHeight() + getTexelHalfY();
	}

	private void updateQuad(int index, QuickTiGame2dMapTile cctile) {
	    if (index >= tiles.size()) return;
	    
	    int vi = index * 32;
	    QuickTiGame2dMapTile tile = tiles.get(index);
	    tile.cc(cctile);
	    
	    if (tile.gid - tile.firstgid < 0) tile.alpha = 0;
	    
	    quads[vi + 2] = tile.flip? tileCoordEndX(tile) : tileCoordStartX(tile); // texture x
	    quads[vi + 3] = tileCoordEndY(tile);  // texture y
	    
	    quads[vi + 4] = tile.red * tile.alpha;   // red
	    quads[vi + 5] = tile.green * tile.alpha; // green
	    quads[vi + 6] = tile.blue * tile.alpha;  // blue
	    quads[vi + 7] = tile.alpha; // alpha
	    
	    // -----------------------------
	    quads[vi + 10] = tile.flip? tileCoordEndX(tile) : tileCoordStartX(tile);
	    quads[vi + 11] = tileCoordStartY(tile);
	    
	    quads[vi + 12] = tile.red * tile.alpha;   // red
	    quads[vi + 13] = tile.green * tile.alpha; // green
	    quads[vi + 14] = tile.blue * tile.alpha;  // blue
	    quads[vi + 15] = tile.alpha; // alpha
	    
	    // -----------------------------
	    quads[vi + 18] = tile.flip ? tileCoordStartX(tile) : tileCoordEndX(tile);
	    quads[vi + 19] = tileCoordStartY(tile);
	    
	    quads[vi + 20] = tile.red * tile.alpha;   // red
	    quads[vi + 21] = tile.green * tile.alpha; // green
	    quads[vi + 22] = tile.blue * tile.alpha;  // blue
	    quads[vi + 23] = tile.alpha; // alpha
	    
	    // -----------------------------
	    
	    quads[vi + 26] = tile.flip ? tileCoordStartX(tile) : tileCoordEndX(tile);
	    quads[vi + 27] = tileCoordEndY(tile);
	    
	    quads[vi + 28] = tile.red * tile.alpha;   // red
	    quads[vi + 29] = tile.green * tile.alpha; // green
	    quads[vi + 30] = tile.blue * tile.alpha;  // blue
	    quads[vi + 31] = tile.alpha; // alpha
	    
	    
	    if (tile.width > 0 && tile.height > 0) {
	        
	        if (!tile.positionFixed) {
	            tile.initialX = quads[vi + 0];
	            tile.initialY = quads[vi + 1];
	            tile.positionFixed = true;
	        }
	        
	        quads[vi + 0] = tile.initialX + tile.offsetX;  // vertex  x
	        quads[vi + 1] = tile.initialY + tile.offsetY;  // vertex  y
	            
	        quads[vi + 8]  = tile.initialX + tile.offsetX; // vertex  x
	        quads[vi + 25] = tile.initialY + tile.offsetY; // vertex  y
	        // -----------------------------
	        
	        quads[vi + 9]  = quads[vi + 1] + tile.height; // vertex  y
	        
	        // -----------------------------
	        quads[vi + 16] = quads[vi + 0] + tile.width;  // vertex  x
	        quads[vi + 17] = quads[vi + 1] + tile.height; // vertex  y
	        
	        // -----------------------------
	        quads[vi + 24] = quads[vi + 0] + tile.width;  // vertex  x
	    }
	    
	}
	
	public void setTile(int index, int gid) {
	    QuickTiGame2dMapTile tile = new QuickTiGame2dMapTile();
	    tile.gid = gid;
	    tile.alpha = 1;
	    
	    setTile(index, tile);
	}

	public void setTile(int index, QuickTiGame2dMapTile tile) {
	    
	    tile.firstgid = firstgid;
        
	    if (tilesets.size() > 1 && tile.gid >= 0) {
	        if (tile.image == null) {
	            for (Map<String, String> gids : tilesetgids) {
	                int tsgid = (int)Float.parseFloat(gids.get("firstgid"));
	                if (tsgid > tile.gid) {
	                    break;
	                }
	                tile.image = gids.get("image");
	            }
	        }
	        
	        if (tile.image != null) {
	            Map<String, String> prop = tilesets.get(tile.image);
	            
	            tile.width  = Float.parseFloat(prop.get("tilewidth"));
	            tile.height = Float.parseFloat(prop.get("tileheight"));
	            tile.firstgid = (int)Float.parseFloat(prop.get("firstgid"));
	            tile.margin = Float.parseFloat(prop.get("margin"));
	            tile.border = Float.parseFloat(prop.get("border"));
	            
	            tile.offsetX  = Float.parseFloat(prop.get("offsetX"));
	            tile.offsetY  = Float.parseFloat(prop.get("offsetY"));
	            
	            tile.atlasX = Float.parseFloat(prop.get("atlasX"));
	            tile.atlasY = Float.parseFloat(prop.get("atlasY"));
	            tile.atlasWidth  = Float.parseFloat(prop.get("atlasWidth"));;
	            tile.atlasHeight = Float.parseFloat(prop.get("atlasHeight"));;
	        }
	    }
		
		updatedTiles.put(Integer.valueOf(index), tile);
	}

	public void setTiles(List<Integer> data) {
	    for (int i = 0; i < data.size(); i++) {
	        QuickTiGame2dMapTile tile = new QuickTiGame2dMapTile();
	        tile.gid = data.get(i).intValue();
	        tile.alpha = 1;
	        
	        setTile(i, tile);
	    }
	}

	public boolean removeTile(int index) {
	    if (index >= tiles.size()) return false;
	    
	    QuickTiGame2dMapTile tile = tiles.get(index);
	    tile.alpha = 0;
	    
	    updatedTiles.put(Integer.valueOf(index), tile);
	    
	    return true;
	}

	public boolean flipTile(int index) {
	    if (index >= tiles.size()) return false;
	    
	    QuickTiGame2dMapTile tile = tiles.get(index);
	    tile.flip = !tile.flip;
	    
	    updatedTiles.put(Integer.valueOf(index), tile);
	    
	    return true;
	}
	
	public QuickTiGame2dMapTile getTile(int index) {
	    if (index >= tiles.size()) return null;
	    
	    return tiles.get(index);
	}

	public List<Integer> getTiles() {
		List<Integer> data = new ArrayList<Integer>();
		
		if (tiles.size() == 0) {
	        for (int i = 0; i < updatedTiles.size(); i++) {
	            data.add(Integer.valueOf(-1));
	        }
	        for (Integer num : updatedTiles.keySet()) {
	            data.set(num.intValue(), updatedTiles.get(num).gid); 
	        }
			
		} else {
			for (int i = 0; i < tiles.size(); i++) {
				data.add(Integer.valueOf(tiles.get(i).gid));
			}
		}
		return data;
	}
	
	public void setImage(String image) {
		this.image = image;
	}

	public float getTileWidth() {
		return tileWidth;
	}
	
	public void setTileWidth(float tileWidth) {
		this.tileWidth = tileWidth;
	}
	
	public float getTileHeight() {
		return tileHeight;
	}
	
	public void setTileHeight(float tileHeight) {
		this.tileHeight = tileHeight;
	}
	
	public int getTileCount() {
		return tileCount;
	}
	
	public int getTileCountX() {
		return tileCountX;
	}
	
	public int getTileCountY() {
		return tileCountY;
	}
	
	public void setFirstgid(int firstgid) {
		this.firstgid = firstgid;
	}
	
	public int getFirstgid() {
		return firstgid;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
		
	    if (orientation == QuickTiGame2dConstant.MAP_ORIENTATION_ISOMETRIC) {
	        tileTiltFactorX = 0.5f;
	        tileTiltFactorY = 0.25f;
	    } else if (orientation == QuickTiGame2dConstant.MAP_ORIENTATION_HEXAGONAL) {
	        tileTiltFactorX = 0.5f;
	        tileTiltFactorY = 0.75f;
	    } else {
	        tileTiltFactorX = 1.0f;
	        tileTiltFactorY = 1.0f;
	    }
	}
	
	public void setAlpha(float alpha) {
		super.setAlpha(alpha);
		for (int i = 0; i < tiles.size(); i++) {
			QuickTiGame2dMapTile tile = tiles.get(i);
			tile.alpha = alpha;
			setTile(i, tile);
		}
	}

	public float getTileTiltFactorX() {
		return tileTiltFactorX;
	}

	public void setTileTiltFactorX(float tileTiltFactorX) {
		this.tileTiltFactorX = tileTiltFactorX;
	}

	public float getTileTiltFactorY() {
		return tileTiltFactorY;
	}

	public void setTileTiltFactorY(float tileTiltFactorY) {
		this.tileTiltFactorY = tileTiltFactorY;
	}

	public void addTileset(Map<String, String> prop) {

	    String[] checker = {
	            "image", "tilewidth", "tileheight", "offsetX", "offsetY",
	            "firstgid", "margin", "border", "atlasX",
	            "atlasY", "atlasWidth", "atlasHeight"
	    };

	    for (String key : checker) {
	        if (prop.get(key) == null) {
	            Log.e(Quicktigame2dModule.LOG_TAG, String.format("'%s' property not found for tileset", key));
	            return;
	        }
	    }
	    
	    if (tilesets.size() == 0) {
	        this.firstgid   = (int)Float.parseFloat(prop.get("firstgid"));
	        this.tileWidth  = Float.parseFloat(prop.get("tilewidth"));
	        this.tileHeight = Float.parseFloat(prop.get("tileheight"));
	    }
	    
	    Map<String, String> gids = new HashMap<String, String>();
	    gids.put("firstgid", prop.get("firstgid"));
	    gids.put("image",    prop.get("image"));
	    tilesetgids.add(gids);
	    
	    tilesets.put(prop.get("image"), prop);
	}

	public Map<String, Map<String, String>> getTilesets() {
	    return tilesets;
	}
	
	public void animateTile(int tileIndex, int start, int count, int interval, int loop) {
	    QuickTiGame2dAnimationFrame animation = new QuickTiGame2dAnimationFrame();
	    
	    animation.setName(String.valueOf(tileIndex));
	    animation.updateNameAsInt();
	    
	    animation.setStart(start);
	    animation.setCount(count);
	    animation.setInterval(interval);
	    animation.setLoop(loop);

	    addAnimation(animation);
	    
	    animating = true;
	}

	public void animateTile(int tileIndex, int[] frames, int interval) {
	    animateTile(tileIndex, frames, interval, 0);
	}

	public void animateTile(int tileIndex, int[] frames, int interval, int loop) {
	    QuickTiGame2dAnimationFrame animation = new QuickTiGame2dAnimationFrame();
	    
	    animation.setName(String.valueOf(tileIndex));
	    animation.updateNameAsInt();
	    
	    animation.setCount(frames.length);
	    animation.setInterval(interval);
	    animation.setLoop(loop);

	    animation.initializeIndividualFrames();
	    for (int i = 0; i < frames.length; i++) {
	        animation.setFrame(i, frames[i]);
	    }
	    
	    addAnimation(animation);
	    
	    animating = true;
	}

}
