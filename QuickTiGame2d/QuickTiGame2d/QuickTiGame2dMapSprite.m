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
#import "QuickTiGame2dMapSprite.h"
#import "QuickTiGame2dEngine.h"

@interface QuickTiGame2dMapSprite (PrivateMethods)
- (float)tex_coord_startX:(QuickTiGame2dMapTile*)tile;
- (float)tex_coord_startY:(QuickTiGame2dMapTile*)tile;
- (float)tileCoordStartX:(QuickTiGame2dMapTile*)tile;
- (float)tileCoordEndX:(QuickTiGame2dMapTile*)tile;
- (float)tileCoordStartY:(QuickTiGame2dMapTile*)tile;
- (float)tileCoordEndY:(QuickTiGame2dMapTile*)tile;
- (void)createQuadBuffer;
- (void)updateQuad:(NSInteger)index tile:(QuickTiGame2dMapTile*)cctile;
@end

@implementation QuickTiGame2dMapTile
@synthesize gid, red, green, blue, alpha, flip, width, height;
@synthesize atlasX, atlasY, firstgid, margin, border, atlasWidth, atlasHeight;
@synthesize offsetX, offsetY, initialX, initialY, positionFixed;
@synthesize image;

-(id)init {
    self = [super init];
    if (self != nil) {
        gid   = 0;
        red   = 1;
        green = 1;
        blue  = 1;
        alpha = 1;
        flip  = FALSE;
        width  = 0;
        height = 0;
        atlasX = 0;
        atlasY = 0;
        firstgid = 0;
        atlasWidth  = 0;
        atlasHeight = 0;
        offsetX = 0;
        offsetY = 0;
        initialX = 0;
        initialY = 0;
        positionFixed = FALSE;
    }
    return self;
}

-(void)cc:(QuickTiGame2dMapTile*)other {
    gid   = other.gid;
    red   = other.red;
    green = other.green;
    blue  = other.blue;
    alpha = other.alpha;
    flip  = other.flip;
    width  = other.width;
    height = other.height;
    atlasX = other.atlasX;
    atlasY = other.atlasY;
    firstgid = other.firstgid;
    atlasWidth  = other.atlasWidth;
    atlasHeight = other.atlasHeight;
    offsetX = other.offsetX;
    offsetY = other.offsetY;
    initialX = other.initialX;
    initialY = other.initialY;
    positionFixed = other.positionFixed;
}

@end

@implementation QuickTiGame2dMapSprite
@synthesize tileWidth, tileHeight, tileCount, tileCountX, tileCountY;
@synthesize firstgid, tileTiltFactorX, tileTiltFactorY;

-(id)init {
    self = [super init];
    if (self != nil) {
        tiles = [[NSMutableArray alloc] init];
        updatedTiles = [[NSMutableDictionary alloc] init];
        
        tileChanged = FALSE;
        
        firstgid = 0;
        
        orientation = MAP_ORIENTATION_ORTHOGONAL;
        
        tileTiltFactorX = 1;
        tileTiltFactorY = 1;
        
        tilesets = [[NSMutableDictionary alloc] init];
        tilesetgids = [[NSMutableArray alloc] init];
    }
    return self;
}

-(void)dealloc {
    [tiles release];
    [updatedTiles release];
    [tilesets release];
    [tilesetgids release];
    
	if (quads)   free(quads);
	if (indices) free(indices);
    
	glDeleteBuffers(1, &verticesID);
    
    [super dealloc];
}

-(BOOL)updateTileCount {
    if (width == 0 || height == 0 || tileWidth == 0 || tileHeight == 0) return FALSE;
    
    if (orientation != MAP_ORIENTATION_HEXAGONAL) {
        tileCountX = ceilf(width / tileWidth);
        tileCountY = ceilf(height / tileHeight);
        tileCount  = tileCountX * tileCountY;
    } else {
        tileCountX = ceilf(width / tileWidth);
        tileCountY = ceilf(height / (tileHeight * tileTiltFactorY));
        
        tileCount = (tileCountX * tileCountY) - (tileCountY / 2);
    }
    
    return TRUE;
}

-(void)onLoad {
    if (loaded) return;
    
    [super onLoad];
    
    if (tileWidth  == 0) tileWidth  = width;
    if (tileHeight == 0) tileHeight = height;
    
    if ([self updateTileCount]) {
        [self createQuadBuffer];
    }
}

-(void)onDispose {
    [super onDispose];
}

-(void)bindVertex {
    // overwrite parent function..to do nothing
}

-(void)drawFrame {
    @synchronized (transforms) {
        [self onTransform];
    }
    
    @synchronized (updatedTiles) {
        tileChanged = [updatedTiles count] > 0;
        if (tileChanged) {
            for (NSNumber* num in updatedTiles) {
                [self updateQuad:[num intValue] tile:[updatedTiles objectForKey:num]];
            }
            [updatedTiles removeAllObjects];
        }
    }
    
    @synchronized (animations) {
        if (animating && [animations count] > 0) {
            for (NSString* name in animations) {
                double uptime = [QuickTiGame2dEngine uptime];
                QuickTiGame2dAnimationFrame* animation = [animations objectForKey:name];
                if ([animation getLastOnAnimationDelta:uptime] < animation.interval) {
                    continue;
                }
                int index = animation.nameAsInt;
                if (index >= 0) {
                    QuickTiGame2dMapTile* updateTileCache = [self getTile:index];
                    updateTileCache.gid = [animation getNextIndex:tileCount withIndex:updateTileCache.gid];
                    
                    @synchronized (updatedTiles) {
                        [self setTile:index tile:updateTileCache];
                    }
                }
                animation.lastOnAnimationInterval = uptime;
            }
        }
    }
    
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity(); 
    
    glEnableClientState(GL_COLOR_ARRAY);
    
    // unbind all buffers
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	glBindBuffer(GL_ARRAY_BUFFER, 0);
	glBindTexture(GL_TEXTURE_2D, 0);
    	
    // update position
    glTranslatef(x * orthFactorX, y * orthFactorY, 0);
	
    // rotate angle, center x, center y, center z, axis
    glTranslatef(param_rotate[1], param_rotate[2], param_rotate[3]);
    if (param_rotate[4] == AXIS_X) {
        glRotatef(param_rotate[0], 1, 0, 0);
    } else if (param_rotate[4] == AXIS_Y) {
        glRotatef(param_rotate[0], 0, 1, 0);
    } else {
        glRotatef(param_rotate[0], 0, 0, 1);
    }
    glTranslatef(-param_rotate[1], -param_rotate[2], -param_rotate[3]);
	
    // scale param x, y, z, center x, center y, center z
    glTranslatef(param_scale[3], param_scale[4], param_scale[5]);
    glScalef(param_scale[0], param_scale[1], param_scale[2]);
    glTranslatef(-param_scale[3], -param_scale[4], -param_scale[5]);
    
	glBindBuffer(GL_ARRAY_BUFFER, verticesID);

    // Update the buffer when the tile data has been changed
    if (tileChanged) {
        glBufferSubData(GL_ARRAY_BUFFER, 0, 128 * tileCount, quads);
        tileChanged = FALSE;
    }
    
	// Configure the vertex pointer which will use the currently bound VBO for its data
    glVertexPointer(2, GL_FLOAT, 32, 0);
    glColorPointer(4, GL_FLOAT,  32,   (GLvoid*)(4 * 4));
    glTexCoordPointer(2, GL_FLOAT, 32, (GLvoid*)(4 * 2));
    
	if (hasTexture) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, self.texture.textureId);
    }
	
    glBlendFunc(srcBlendFactor, dstBlendFactor);
	
    glDrawElements(GL_TRIANGLES, tileCount * 6, GL_UNSIGNED_SHORT, indices);
    
	glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    
    glDisableClientState(GL_COLOR_ARRAY);
}

-(float)tex_coord_startX:(QuickTiGame2dMapTile*)tile {
    int tileNo = tile.gid - tile.firstgid;
    if ([tilesets count] > 1) {
        float awidth = tile.atlasWidth > 0 ? tile.atlasWidth : width;
        float twidth = tile.width > 0 ? tile.width : tileWidth;
        
        int xcount = (int)round((awidth - (tile.margin * 2) + tile.border) / (float)(twidth  + tile.border));
        int xindex = tileNo % xcount;
        return tile.atlasX + ((tile.border + twidth) * xindex) + tile.margin;
    } else {
        int xcount = (int)round((self.texture.width - (margin * 2) + border) / (float)(tileWidth  + border));
        int xindex = tileNo % xcount;
        
        return ((border + tileWidth) * xindex) + margin;
    }
}

-(float)tex_coord_startY:(QuickTiGame2dMapTile*)tile {
    int tileNo = tile.gid - tile.firstgid;
    
    if ([tilesets count] > 1) {
        float awidth  = tile.atlasWidth  > 0 ? tile.atlasWidth  : width;
        float aheight = tile.atlasHeight > 0 ? tile.atlasHeight : height;
        float twidth = tile.width > 0 ? tile.width : tileWidth;
        float theight = tile.height > 0 ? tile.height : tileHeight;
        int xcount = (int)round((awidth  - (tile.margin * 2) + tile.border) / (float)(twidth  + tile.border));
        int ycount = (int)round((aheight - (tile.margin * 2) + tile.border) / (float)(theight + tile.border));
        int yindex = [self flipY] ? ycount - (tileNo / xcount) - 1 : (tileNo / xcount);
                
        return tile.atlasY + ((tile.border + theight) * yindex) + tile.margin;
    } else {
        int xcount = (int)round((self.texture.width - (margin * 2) + border) / (float)(tileWidth  + border));
        int ycount = (int)round((self.texture.height - (margin * 2) + border) / (float)(tileHeight + border));
        int yindex = [self flipY] ? ycount - (tileNo / xcount) - 1 : (tileNo / xcount);
        
        return ((border + tileHeight) * yindex) + margin;
    }
}

-(float)tileCoordStartX:(QuickTiGame2dMapTile*)tile {
    return [self tex_coord_startX:tile] / (float)self.texture.glWidth + [self getTexelHalfX];
}

-(float)tileCoordEndX:(QuickTiGame2dMapTile*)tile {
    float twidth = tile.width > 0 ? tile.width : tileWidth;
    return (float)([self tex_coord_startX:tile] + twidth) / (float)self.texture.glWidth - [self getTexelHalfX];
}

-(float)tileCoordStartY:(QuickTiGame2dMapTile*)tile {
    float theight = tile.height > 0 ? tile.height : tileHeight;
    return (float)([self tex_coord_startY:tile] + theight) / (float)self.texture.glHeight - [self getTexelHalfY];
}

-(float)tileCoordEndY:(QuickTiGame2dMapTile*)tile {
    return [self tex_coord_startY:tile] / (float)self.texture.glHeight + [self getTexelHalfY];
}

- (void)createQuadBuffer {
    clearGLErrors(@"before createQuadBuffer");
    
    //
    // quad = ([vertex x, vertex y, texture x, texture y, red, green, blue, alpha] * 4) = 8 * 4 * (float=4bytes) = 128 bytes
    //
    quads   = calloc(sizeof(float) * 32, tileCount);
    indices = calloc(sizeof(GLushort),   tileCount * 6);
    
    [tiles removeAllObjects];
    
    for( int i = 0; i < tileCount; i++) {
		indices[i * 6 + 0] = i * 4 + 0;
		indices[i * 6 + 1] = i * 4 + 1;
		indices[i * 6 + 2] = i * 4 + 2;
		
		indices[i * 6 + 3] = i * 4 + 2;
		indices[i * 6 + 4] = i * 4 + 3;
		indices[i * 6 + 5] = i * 4 + 0;
        
		QuickTiGame2dMapTile* tile = [[QuickTiGame2dMapTile alloc] init];
        
        tile.alpha = 0;
        
        [tiles addObject:tile];
        
        [self updateQuad:i tile:tile];
        
        [tile release];
	}

    //
	// initialize texture vertex
    //
    NSInteger index = 0;
    for(int ty = 0; ty < tileCountY; ty++) {
        for (int tx = 0; tx < tileCountX; tx++) {
            int vi = index * 32;
            
            if (orientation == MAP_ORIENTATION_ISOMETRIC) {
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
            } else if (orientation == MAP_ORIENTATION_HEXAGONAL) {
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
                quads[vi + 8] = (tx * tileWidth);               // vertex  x
                quads[vi + 9] = (ty * tileHeight) + tileHeight; // vertex  y
                
                // -----------------------------
                quads[vi + 16] = (tx * tileWidth)  + tileWidth;  // vertex  x
                quads[vi + 17] = (ty * tileHeight) + tileHeight; // vertex  y
                
                // -----------------------------
                quads[vi + 24] = (tx * tileWidth) + tileWidth;  // vertex  x
                quads[vi + 25] = (ty * tileHeight);             // vertex  y
            }
            index++;
        }
	}
    
	// Generate the vertices VBO
	glGenBuffers(1, &verticesID);
    glBindBuffer(GL_ARRAY_BUFFER, verticesID);
    glBufferData(GL_ARRAY_BUFFER, 128 * tileCount, quads, GL_DYNAMIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
	
    clearGLErrors(@"createQuadBuffer");
}

- (void)updateQuad:(NSInteger)index tile:(QuickTiGame2dMapTile*)cctile{
    if (index >= [tiles count]) return;
    
    int vi = index * 32;
    
    QuickTiGame2dMapTile* tile = [tiles objectAtIndex:index];
    [tile cc:cctile];
    
    if (tile.gid - tile.firstgid < 0) tile.alpha = 0;
    
    quads[vi + 2] = tile.flip ? [self tileCoordEndX:tile] : [self tileCoordStartX:tile]; // texture x
    quads[vi + 3] = [self tileCoordEndY:tile]; // texture y
    
    quads[vi + 4] = tile.red * tile.alpha;   // red
    quads[vi + 5] = tile.green * tile.alpha; // green
    quads[vi + 6] = tile.blue * tile.alpha;  // blue
    quads[vi + 7] = tile.alpha; // alpha
    
    // -----------------------------
    quads[vi + 10] = tile.flip ? [self tileCoordEndX:tile] : [self tileCoordStartX:tile];
    quads[vi + 11] = [self tileCoordStartY:tile];
    
    quads[vi + 12] = tile.red * tile.alpha;   // red
    quads[vi + 13] = tile.green * tile.alpha; // green
    quads[vi + 14] = tile.blue * tile.alpha;  // blue
    quads[vi + 15] = tile.alpha; // alpha
    
    // -----------------------------
    quads[vi + 18] = tile.flip ? [self tileCoordStartX:tile] : [self tileCoordEndX:tile];
    quads[vi + 19] = [self tileCoordStartY:tile];
    
    quads[vi + 20] = tile.red * tile.alpha;   // red
    quads[vi + 21] = tile.green * tile.alpha; // green
    quads[vi + 22] = tile.blue * tile.alpha;  // blue
    quads[vi + 23] = tile.alpha; // alpha
    
    // -----------------------------
    
    quads[vi + 26] = tile.flip ? [self tileCoordStartX:tile] : [self tileCoordEndX:tile];
    quads[vi + 27] = [self tileCoordEndY:tile];
    
    quads[vi + 28] = tile.red * tile.alpha;   // red
    quads[vi + 29] = tile.green * tile.alpha; // green
    quads[vi + 30] = tile.blue * tile.alpha;  // blue
    quads[vi + 31] = tile.alpha; // alpha
    
    if (tile.width > 0 && tile.height > 0) {
        
        if (!tile.positionFixed) {
            tile.initialX = quads[vi + 0];
            tile.initialY = quads[vi + 1];
            tile.positionFixed = TRUE;
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

-(void)setTile:(NSInteger)index gid:(NSInteger)gid {
    QuickTiGame2dMapTile* tile = [[QuickTiGame2dMapTile alloc] init];
    tile.gid = gid;
    tile.alpha = 1;
    
    [self setTile:index tile:tile];
    
    [tile release];
}

-(void)setTile:(NSInteger)index tile:(QuickTiGame2dMapTile*)tile {
    
    tile.firstgid = firstgid;
    
    if ([tilesets count] > 1 && tile.gid >= 0) {
        if (tile.image == nil) {
            for (id gids in tilesetgids) {
                NSInteger tsgid = [[gids objectForKey:@"firstgid"] intValue];
                if (tsgid > tile.gid) {
                    break;
                }
                tile.image = [gids objectForKey:@"image"];
            }
        }
        
        if (tile.image != nil) {
            NSDictionary* prop = [tilesets objectForKey:tile.image];
            
            tile.width  = [[prop objectForKey:@"tilewidth"]  floatValue];
            tile.height = [[prop objectForKey:@"tileheight"] floatValue];
            tile.firstgid = [[prop objectForKey:@"firstgid"] intValue];
            tile.margin = [[prop objectForKey:@"margin"] floatValue];
            tile.border = [[prop objectForKey:@"border"] floatValue];
            
            tile.offsetX  = [[prop objectForKey:@"offsetX"]  floatValue];
            tile.offsetY  = [[prop objectForKey:@"offsetY"]  floatValue];
            
            tile.atlasX = [[prop objectForKey:@"atlasX"] floatValue];
            tile.atlasY = [[prop objectForKey:@"atlasY"] floatValue];
            tile.atlasWidth  = [[prop objectForKey:@"atlasWidth"] floatValue];
            tile.atlasHeight = [[prop objectForKey:@"atlasHeight"] floatValue];
        }
    }
    
    [updatedTiles setObject:tile forKey:[NSNumber numberWithInt:index]];
}

-(void)setTiles:(NSArray*)data {
    for (int i = 0; i < [data count]; i++) {
        QuickTiGame2dMapTile* tile = [[QuickTiGame2dMapTile alloc] init];
        tile.gid = [[data objectAtIndex:i] intValue];
        tile.alpha = 1;
        
        [self setTile:i tile:tile];
        
        [tile release];
    }
}

-(void)updateImageSize {
    // map width and height should be updated manually!
}

-(BOOL)removeTile:(NSInteger)index {
    if (index >= [tiles count]) return FALSE;
    
    QuickTiGame2dMapTile* tile = [tiles objectAtIndex:index];
    tile.alpha = 0;
    
    [updatedTiles setObject:tile forKey:[NSNumber numberWithInt:index]];
    
    return TRUE;
}

-(BOOL)flipTile:(NSInteger)index {
    if (index >= [tiles count]) return FALSE;
    
    QuickTiGame2dMapTile* tile = [tiles objectAtIndex:index];
    tile.flip = !tile.flip;
    
    [updatedTiles setObject:tile forKey:[NSNumber numberWithInt:index]];
    
    return TRUE;
}

-(QuickTiGame2dMapTile*)getTile:(NSInteger)index {
    if (index >= [tiles count]) return nil;
    
    return [tiles objectAtIndex:index];
}

-(NSArray*)getTiles {
    NSMutableArray* data = [[NSMutableArray alloc] init];
    
    if ([tiles count] == 0) {
        for (int i = 0; i < [updatedTiles count]; i++) {
            [data addObject:[NSNumber numberWithInt:-1]];
        }
        for (NSNumber* num in updatedTiles) {
            [data replaceObjectAtIndex:[num intValue] withObject: 
                [NSNumber numberWithInt:((QuickTiGame2dMapTile*)[updatedTiles objectForKey:num]).gid]];
        }
    } else {
        for (int i = 0; i < [tiles count]; i++) {
            QuickTiGame2dMapTile* tile = [tiles objectAtIndex:i];
            [data addObject:[NSNumber numberWithInt:tile.gid]];
        }
    }
    
    
    return [data autorelease];
}

-(void)setAlpha:(float)alpha {
    [super setAlpha:alpha];
    for (int i = 0; i < [tiles count]; i++) {
        QuickTiGame2dMapTile* tile = [tiles objectAtIndex:i];
        tile.alpha = alpha;
        [self setTile:i tile:tile];
    }
}

-(void)setOrientation:(NSInteger)value {
    orientation = value;
    
    if (orientation == MAP_ORIENTATION_ISOMETRIC) {
        tileTiltFactorX = 0.5f;
        tileTiltFactorY = 0.25f;
    } else if (orientation == MAP_ORIENTATION_HEXAGONAL) {
        tileTiltFactorX = 0.5f;
        tileTiltFactorY = 0.75f;
    } else {
        tileTiltFactorX = 1.0f;
        tileTiltFactorY = 1.0f;
    }
}

-(NSInteger)orientation {
    return orientation;
}

-(void)addTileset:(NSDictionary*)prop {

    NSArray* checker = [NSArray arrayWithObjects:
            @"image", @"tilewidth", @"tileheight", @"offsetX", @"offsetY",
            @"firstgid", @"margin", @"border", @"atlasX",
            @"atlasY", @"atlasWidth", @"atlasHeight", nil];

    for (id key in checker) {
        if ([prop objectForKey:key] == nil) {
            NSLog(@"[ERROR] '%@' property not found for tileset", key);
            return;
        }
    }
    
    if ([tilesets count] == 0) {
        self.firstgid   = [[prop objectForKey:@"firstgid"]   intValue];
        self.tileWidth  = [[prop objectForKey:@"tilewidth"]  floatValue];
        self.tileHeight = [[prop objectForKey:@"tileheight"] floatValue];
    }
    
    [tilesetgids addObject:[NSDictionary dictionaryWithObjectsAndKeys:
            [prop objectForKey:@"firstgid"], @"firstgid",
            [prop objectForKey:@"image"], @"image",
            nil]];
    [tilesets setObject:prop forKey:[prop objectForKey:@"image"]];
}

-(NSDictionary*)tilesets {
    return tilesets;
}

// disable frame animation
-(BOOL)setFrameIndex:(NSInteger)index force:(BOOL)force {
    return true;
}

-(void)animateTile:(NSInteger)tileIndex start:(NSInteger)start count:(NSInteger)count interval:(NSInteger)interval loop:(NSInteger)loop {
    QuickTiGame2dAnimationFrame* animation = [[QuickTiGame2dAnimationFrame alloc] init];
    
    animation.name  = [NSString stringWithFormat:@"%d", tileIndex];
    [animation updateNameAsInt];
    animation.start = start;
    animation.count = count;
    animation.interval = interval;
    animation.loop     = loop;
    
    [self addAnimation:animation];
    
    [animation release];
    
    animating = TRUE;
}

-(void)animateTile:(NSInteger)tileIndex frames:(NSArray*)frames interval:(NSInteger)interval {
    [self animateTile:tileIndex frames:frames interval:interval loop:0];
}

-(void)animateTile:(NSInteger)tileIndex frames:(NSArray*)frames interval:(NSInteger)interval loop:(NSInteger)loop {
    QuickTiGame2dAnimationFrame* animation = [[QuickTiGame2dAnimationFrame alloc] init];
    
    animation.name  = [NSString stringWithFormat:@"%d", tileIndex];
    [animation updateNameAsInt];
    animation.count = [frames count];
    animation.interval = interval;
    animation.loop     = loop;
    
    [animation initializeIndividualFrames];
    for (int i = 0; i < [frames count]; i++) {
        [animation setFrame:i withValue:[[frames objectAtIndex:i] intValue]];
    }
    
    [self addAnimation:animation];
    
    [animation release];
    
    animating = TRUE;
}

@end
