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

public class QuickTiGame2dMapTile {
    public String image;
    public int   firstgid;
    public int   gid;
    public float red;
    public float green;
    public float blue;
    public float alpha;
    public boolean flip;
    
    public float margin;
    public float border;
    public float width;
    public float height;
    public float atlasX;
    public float atlasY;
    public float atlasWidth;
    public float atlasHeight;
    public float offsetX;
    public float offsetY;
    
    public boolean  positionFixed;
    public float initialX;
    public float initialY;
    
    public QuickTiGame2dMapTile() {
        gid   = 0;
        red   = 1;
        green = 1;
        blue  = 1;
        alpha = 1;
        flip  = false;
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
        positionFixed = false;
        image = null;
    }
    
    public void cc(QuickTiGame2dMapTile other) {
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
}