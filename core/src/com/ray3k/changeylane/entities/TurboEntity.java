/*
 * The MIT License
 *
 * Copyright 2018 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.changeylane.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ray3k.changeylane.Core;
import com.ray3k.changeylane.Entity;
import com.ray3k.changeylane.SpineTwoColorEntity;
import com.ray3k.changeylane.states.GameState;

/**
 *
 * @author Raymond
 */
public class TurboEntity extends SpineTwoColorEntity {

    public TurboEntity() {
        super(Core.DATA_PATH + "/spine/turbo.json", "animation", GameState.twoColorPolygonBatch);
    }

    @Override
    public void actSub(float delta) {
        PlayerEntity player = GameState.entityManager.get(PlayerEntity.class);
        if (player != null) {
            if (player.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                GameState.inst().addScore(GameState.turboScore);
                
                GameState.inst().turboMode();
                dispose();
            }
        }
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }
    
}
