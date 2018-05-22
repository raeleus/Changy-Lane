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
import com.badlogic.gdx.math.MathUtils;
import com.ray3k.changeylane.Core;
import com.ray3k.changeylane.Entity;
import com.ray3k.changeylane.Maths;
import com.ray3k.changeylane.SpineTwoColorEntity;
import com.ray3k.changeylane.states.GameState;

/**
 *
 * @author Raymond
 */
public class WreckEntity extends SpineTwoColorEntity {
    private float rotationSpeed;
    private float rotationFriction;
    private static final float EXPLOSION_DELAY = .025f;
    private float explosionTimer;

    public WreckEntity() {
        super(Core.DATA_PATH + "/spine/wreck.json", "animation", GameState.twoColorPolygonBatch);
        rotationFriction = 275.0f;
        if (MathUtils.randomBoolean()) {
            rotationSpeed = MathUtils.random(50.0f, 350.0f);
        } else {
            rotationSpeed = MathUtils.random(-50.0f, -350.0f);
        }
        
        explosionTimer = EXPLOSION_DELAY;
    }

    @Override
    public void actSub(float delta) {
        rotationSpeed = Maths.approach(rotationSpeed, 0.0f, rotationFriction * delta);
        getSkeleton().getRootBone().setRotation(getSkeleton().getRootBone().getRotation() + rotationSpeed * delta);
        
        setYspeed(Maths.approach(getYspeed(), GameState.scrollingSpeed, 200.0f));
        
        explosionTimer -= delta;
        if (explosionTimer < 0) {
            explosionTimer = EXPLOSION_DELAY;
            
            ExplosionEntity explosion = new ExplosionEntity();
            explosion.setPosition(getX() - 50 + MathUtils.random(100.0f), getY() - 50 + MathUtils.random(100.0f));
            GameState.entityManager.addEntity(explosion);
        }
        
        if (getY() < -100) {
            dispose();
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
