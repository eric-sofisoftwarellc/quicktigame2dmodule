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
#import "QuickTiGame2dTextSprite.h"
#import "QuickTiGame2dEngine.h"

@interface QuickTiGame2dTextSprite (PrivateMethods)
-(void)loadTextData;
@end

@implementation QuickTiGame2dTextSprite
@synthesize text, fontSize, fontFace;

-(CGFloat)systemFontSize {
    return [UIFont systemFontSize];
}

-(void)loadTextData {
    UIFont* font = [UIFont systemFontOfSize:[UIFont systemFontSize]];
    
    if ([fontFace length] > 0) {
        NSInteger size = fontSize > 0 ? fontSize : [UIFont systemFontSize];
        font = [UIFont fontWithName:fontFace size:size];
    } else if (fontSize > 0) {
        font = [UIFont systemFontOfSize:fontSize];
    }
    
    CGSize textSize = [text sizeWithFont:font]; 
    
    int textWidth  = textSize.width;
    int textHeight = textSize.height;
    NSUInteger blength = textWidth * textHeight * 4;
    
    GLubyte *bitmap = (GLubyte *)malloc(blength);
    memset(bitmap, 0, blength);
    
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef context = CGBitmapContextCreate(bitmap, textWidth, textHeight,
                                                 8, textWidth * 4, colorSpace,
                                                 kCGImageAlphaPremultipliedLast);
    
    UIGraphicsPushContext(context);
    
    [[UIColor whiteColor] set];
    [text drawAtPoint:CGPointMake(0, 0) withFont:font];
    
    UIGraphicsPopContext();
    
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(context);
    
    labelTexture.name   = text;
    labelTexture.width  = textWidth;
    labelTexture.height = textHeight;
    labelTexture.data   = bitmap;
    labelTexture.dataLength = blength;
    
    [labelTexture onLoadWithBytes];
    [labelTexture freeData];
    
    self.width  = textWidth;
    self.height = textHeight;
}

-(void)reload {
    shouldReload = TRUE;
}

-(void)onLoad {
    [self loadTextData];
    
    hasTexture = TRUE;
 
    [self createTextureBuffer];
    [self bindVertex];
    
    shouldReload = FALSE;
    loaded = TRUE;
}

/*
 * returns whether Y axis of texture should flipped or not
 */
-(BOOL)flipY {
    return TRUE;
}

-(void)drawFrame {
    if (shouldReload) {
        [labelTexture onDispose];
        [self loadTextData];
        [self bindVertex];
        shouldReload = FALSE;
    }
    [super drawFrame];
}
-(void)onDispose {
    [super onDispose];
}

-(QuickTiGame2dTexture*)texture {
    return labelTexture;
}

- (id)init {
    self = [super init];
    if (self != nil) {
        labelTexture = [[QuickTiGame2dTexture alloc] init];
        labelTexture.width  = 1;
        labelTexture.height = 1;
        
        self.text = @"";
        self.fontFace = nil;
        self.fontSize = 0;
        
        shouldReload = FALSE;
    }
    return self;
}

-(void)dealloc {
    [labelTexture release];
    [text release];
    [super dealloc];
}
@end

