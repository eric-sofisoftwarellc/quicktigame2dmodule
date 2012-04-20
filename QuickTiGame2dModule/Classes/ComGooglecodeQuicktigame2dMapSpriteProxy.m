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
#import "ComGooglecodeQuicktigame2dMapSpriteProxy.h"
#import "TiUtils.h"

@implementation ComGooglecodeQuicktigame2dMapSpriteProxy

- (id)init {
    self = [super init];
    if (self != nil) {
        // we don't want parent sprite instance so release it here.
        [sprite release];
        
        // create our particles instance
        sprite = [[QuickTiGame2dMapSprite alloc] init];
        
        tileInfoCache = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)dealloc {
    [tileInfoCache release];
    [super dealloc];
}

/*
 * notification event that is issued by game engine
 * onload, ongainedfocus, enterframe, onlostfocus, ondispose
 */
- (void)onNotification:(NSString*)type userInfo:(NSDictionary*)userInfo {
    
    if ([type isEqualToString:@"onload"]) {
        if (sprite.width  == 0) sprite.width  = [[userInfo objectForKey:@"width"]  intValue];
        if (sprite.height == 0) sprite.height = [[userInfo objectForKey:@"height"] intValue];
    }
    
    [super onNotification:type userInfo:userInfo];
}

#pragma Public APIs

-(id)getTile:(id)args {
    ENSURE_SINGLE_ARG(args, NSNumber);
    NSInteger index = [args intValue];
    
    QuickTiGame2dMapSprite* mapSprite = (QuickTiGame2dMapSprite*)sprite;
    
    if (index >= mapSprite.tileCount) {
        return nil;
    }
    
    QuickTiGame2dMapTile* tile = [mapSprite getTile:index];
    
    [tileInfoCache setValue:NUMINT(index)         forKey:@"index"];
    [tileInfoCache setValue:NUMINT(tile.gid)      forKey:@"gid"];
    [tileInfoCache setValue:NUMFLOAT(tile.red)    forKey:@"red"];
    [tileInfoCache setValue:NUMFLOAT(tile.green)  forKey:@"green"];
    [tileInfoCache setValue:NUMFLOAT(tile.blue)   forKey:@"blue"];
    [tileInfoCache setValue:NUMFLOAT(tile.alpha)  forKey:@"alpha"];
    [tileInfoCache setValue:NUMBOOL(tile.flip)    forKey:@"flip"];
    [tileInfoCache setValue:NUMFLOAT(mapSprite.x + tile.initialX + tile.offsetX)  forKey:@"screenX"];
    [tileInfoCache setValue:NUMFLOAT(mapSprite.y + tile.initialY + tile.offsetY)  forKey:@"screenY"];
    
    [tileInfoCache setValue:NUMFLOAT(tile.width  > 0 ? tile.width  : mapSprite.width)   forKey:@"width"];
    [tileInfoCache setValue:NUMFLOAT(tile.height > 0 ? tile.height : mapSprite.height)  forKey:@"height"];
    [tileInfoCache setValue:NUMFLOAT(tile.margin)  forKey:@"margin"];
    [tileInfoCache setValue:NUMFLOAT(tile.border)  forKey:@"border"];
    
    return tileInfoCache;
}

-(id)updateTile:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary);
    
    NSInteger index  = [TiUtils intValue:@"index"  properties:args  def:0];
    NSInteger gid    = [TiUtils intValue:@"gid"    properties:args  def:-1];
    float     red    = [TiUtils floatValue:@"red"    properties:args  def:-1];
    float     green  = [TiUtils floatValue:@"green"  properties:args  def:-1];
    float     blue   = [TiUtils floatValue:@"blue"   properties:args  def:-1];
    float     alpha  = [TiUtils floatValue:@"alpha"  properties:args  def:-1];
    
    if (index >= ((QuickTiGame2dMapSprite*)sprite).tileCount) {
        return NUMBOOL(FALSE);
    }
    
    QuickTiGame2dMapTile* tile = [((QuickTiGame2dMapSprite*)sprite) getTile:index];
    
    if (gid   >= 0) tile.gid   = gid;
    if (red   >= 0) tile.red   = red;
    if (green >= 0) tile.green = green;
    if (blue  >= 0) tile.blue  = blue;
    if (alpha >= 0) tile.alpha = alpha;
    
    if ([args objectForKey:@"flip"] != nil) {
        tile.flip = [TiUtils boolValue:@"flip"  properties:args def:FALSE];
    }
    
    [((QuickTiGame2dMapSprite*)sprite) setTile:index tile:tile];
    
    return NUMBOOL(TRUE);
}

-(id)updateTiles:(id)args {
    if ([[args objectAtIndex:0] isKindOfClass:[NSDictionary class]]) {
        NSMutableArray* data = [[NSMutableArray alloc] init];
        
        for (NSDictionary* value in args) {
            [data addObject:[value objectForKey:@"gid"]];
        }
        
        [((QuickTiGame2dMapSprite*)sprite) setTiles:data];
        
        [data release];
    } else {
        [((QuickTiGame2dMapSprite*)sprite) setTiles:args];
    }
    
    return NUMBOOL(TRUE);
}

-(id)removeTile:(id)args {
    ENSURE_SINGLE_ARG(args, NSNumber);
    NSInteger index = [args intValue];
    
    if (index >= ((QuickTiGame2dMapSprite*)sprite).tileCount) {
        return NUMBOOL(FALSE);
    }
    
    [((QuickTiGame2dMapSprite*)sprite) removeTile:index];
    
    return NUMBOOL(TRUE);
}

-(id)flipTile:(id)args {
    ENSURE_SINGLE_ARG(args, NSNumber);
    NSInteger index = [args intValue];
    
    if (index >= ((QuickTiGame2dMapSprite*)sprite).tileCount) {
        return NUMBOOL(FALSE);
    }
    
    [((QuickTiGame2dMapSprite*)sprite) flipTile:index];
    
    return NUMBOOL(TRUE);
}

- (id)border {
    return NUMINT(sprite.border);
}

- (void)setBorder:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    sprite.border = [value intValue];
}

- (id)margin {
    return NUMINT(sprite.margin);
}

- (void)setMargin:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    sprite.margin = [value intValue];
}

- (id)tileWidth {
    return NUMINT(((QuickTiGame2dMapSprite*)sprite).tileWidth);
}

- (void)setTileWidth:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    ((QuickTiGame2dMapSprite*)sprite).tileWidth = [value intValue];
    [((QuickTiGame2dMapSprite*)sprite) updateTileCount];
}

- (id)tileHeight {
    return NUMINT(((QuickTiGame2dMapSprite*)sprite).tileHeight);
}

- (void)setTileHeight:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    ((QuickTiGame2dMapSprite*)sprite).tileHeight = [value intValue];
    [((QuickTiGame2dMapSprite*)sprite) updateTileCount];
}

- (id)firstgid {
    return NUMINT(((QuickTiGame2dMapSprite*)sprite).firstgid);
}

- (void)setFirstgid:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    ((QuickTiGame2dMapSprite*)sprite).firstgid = [value intValue];
}

- (id)orientation {
    return NUMINT(((QuickTiGame2dMapSprite*)sprite).orientation);
}

- (void)setOrientation:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    ((QuickTiGame2dMapSprite*)sprite).orientation = [value intValue];
    [((QuickTiGame2dMapSprite*)sprite) updateTileCount];
}

- (id)tileCount {
    return NUMINT(((QuickTiGame2dMapSprite*)sprite).tileCount);
}

- (id)tileCountX {
    return NUMINT(((QuickTiGame2dMapSprite*)sprite).tileCountX);
}

- (id)tileCountY {
    return NUMINT(((QuickTiGame2dMapSprite*)sprite).tileCountY);
}

- (id)tiles {
    return [((QuickTiGame2dMapSprite*)sprite) getTiles];
}

- (void)setTiles:(id)value {
    [self updateTiles:value];
}

- (void)setWidth:(id)value {
    [super setWidth:value];
    [((QuickTiGame2dMapSprite*)sprite) updateTileCount];
}

- (void)setHeight:(id)value {
    [super setHeight:value];
    [((QuickTiGame2dMapSprite*)sprite) updateTileCount];
}

- (id)tileTiltFactorX {
    return NUMFLOAT(((QuickTiGame2dMapSprite*)sprite).tileTiltFactorX);
}

- (void)setTileTiltFactorX:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    ((QuickTiGame2dMapSprite*)sprite).tileTiltFactorX = [value floatValue];
}

- (id)tileTiltFactorY {
    return NUMFLOAT(((QuickTiGame2dMapSprite*)sprite).tileTiltFactorY);
}

- (void)setTileTiltFactorY:(id)value {
    ENSURE_SINGLE_ARG(value, NSNumber);
    ((QuickTiGame2dMapSprite*)sprite).tileTiltFactorY = [value floatValue];
}

- (id)tilesets {
    return [((QuickTiGame2dMapSprite*)sprite) tilesets];
}

- (void)setTilesets:(id)args {
    for (NSUInteger i = 0; i < [args count]; i++) {
        NSMutableDictionary* param = [[NSMutableDictionary alloc] init];
        
        [param setObject:@"0" forKey:@"offsetX"];
        [param setObject:@"0" forKey:@"offsetY"];
        
        NSDictionary* info = [args objectAtIndex:i];
        for (id key in info) {
            id value = [info objectForKey:key];
            if ([key isEqualToString:@"atlas"]) {
                for (id atlasKey in value) {
                    if ([atlasKey isEqualToString:@"x"]) {
                        [param setObject:[value objectForKey:atlasKey] forKey:@"atlasX"];
                    } else if ([atlasKey isEqualToString:@"y"]) {
                        [param setObject:[value objectForKey:atlasKey] forKey:@"atlasY"];
                    } else if ([atlasKey isEqualToString:@"w"]) {
                        [param setObject:[value objectForKey:atlasKey] forKey:@"atlasWidth"];
                    } else if ([atlasKey isEqualToString:@"h"]) {
                        [param setObject:[value objectForKey:atlasKey] forKey:@"atlasHeight"];
                    }
                }
            } else {
                [param setObject:value forKey:key];
            }
        }
        [((QuickTiGame2dMapSprite*)sprite) addTileset:param];
        [param release];
    }
}

-(void)stop:(id)args {
    ENSURE_SINGLE_ARG(args, NSNumber);
    [(QuickTiGame2dMapSprite*)sprite deleteAnimation:[NSString stringWithFormat:@"%d", [args intValue]]];
}

- (void)animate:(id)args {
    if ([args count] >= 3 && [[args objectAtIndex:1] isKindOfClass:[NSArray class]]) {
        NSInteger tileIndex = [[args objectAtIndex:0] intValue];
        NSArray* frames     =  [args objectAtIndex:1];
        NSInteger interval  = [[args objectAtIndex:2] intValue];
        
        if ([args count] == 3) {
            [((QuickTiGame2dMapSprite*)sprite) animateTile:tileIndex frames:frames interval:interval];
        } else {
            [((QuickTiGame2dMapSprite*)sprite) animateTile:tileIndex frames:frames interval:interval loop:[[args objectAtIndex:3] intValue]];
        }
        return;
    }
    
    if ([args count] < 5) {
        NSLog(@"Too few arguments for sprite.animate(index, start, count, interval, loop)");
        return;
    }
    
    NSInteger tileIndex = [[args objectAtIndex:0] intValue];
    NSInteger start = [[args objectAtIndex:1] intValue];
    NSInteger count = [[args objectAtIndex:2] intValue];
    NSInteger interval = [[args objectAtIndex:3] intValue];
    NSInteger loop = [[args objectAtIndex:4] intValue];
    
    [((QuickTiGame2dMapSprite*)sprite) animateTile:tileIndex start:start count:count interval:interval loop:loop];
}

@end
 