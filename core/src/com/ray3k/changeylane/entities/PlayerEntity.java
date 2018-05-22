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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ray3k.changeylane.Core;
import com.ray3k.changeylane.Entity;
import com.ray3k.changeylane.Maths;
import com.ray3k.changeylane.SpineTwoColorEntity;
import com.ray3k.changeylane.states.GameState;

/**
 *
 * @author Raymond
 */
public class PlayerEntity extends SpineTwoColorEntity {
    private static final float VERTICAL_MAX_SPEED = 400.0f;
    private static final float VERTICAL_ACCELERATION = 1400.0f;
    private static final float VERTICAL_BRAKE = 1000.0f;
    private int lane;

    public PlayerEntity() {
        super(Core.DATA_PATH + "/spine/player.json", "animation", GameState.twoColorPolygonBatch);
        lane = 2;
        setX(GameState.STREET_POSITION + GameState.LINE_WIDTH + GameState.LANE_WIDTH / 2.0f + (GameState.LANE_WIDTH + GameState.LINE_WIDTH) * lane);
        setY(GameState.GAME_HEIGHT / 2.0f);
    }

    @Override
    public void actSub(float delta) {
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            setYspeed(Maths.approach(getYspeed(), VERTICAL_MAX_SPEED, VERTICAL_ACCELERATION * delta));
        } else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            setYspeed(Maths.approach(getYspeed(), -VERTICAL_MAX_SPEED, VERTICAL_ACCELERATION * delta));
        } else {
            setYspeed(Maths.approach(getYspeed(), 0, VERTICAL_BRAKE * delta));
        }
        
        if (getY() < 20.0f) {
            setY(20.0f);
            if (getYspeed() < 0) setYspeed(0.0f);
        } else if (getY() > GameState.GAME_HEIGHT - 40.0f) {
            setY(GameState.GAME_HEIGHT - 40.0f);
            if (getYspeed() > 0) setYspeed(0.0f);
        }
        
        float targetX = GameState.STREET_POSITION + GameState.LINE_WIDTH + GameState.LANE_WIDTH / 2.0f + (GameState.LANE_WIDTH + GameState.LINE_WIDTH) * lane;
        
        setX(Maths.approach(getX(), targetX, 350.0f * delta));
        
        for (EnemyEntity enemy : GameState.entityManager.getAll(EnemyEntity.class)) {
            if (getSkeletonBounds().aabbIntersectsSkeleton(enemy.getSkeletonBounds())) {
                GameState.inst().addScore(GameState.wreckScore);
                
                GameState.inst().playSound("crash");
                
                if (GameState.turboTimer < 0) {
                    GameState.inst().addScore(GameState.gameOverScore);
                    
                    dispose();
                    GameState.entityManager.addEntity(new GameOverTimerEntity(5.0f));

                    WreckEntity wreck = new WreckEntity();
                    wreck.setPosition(getX(), getY());
                    wreck.setYspeed(-150.0f);
                    GameState.entityManager.addEntity(wreck);
                }
                
                WreckEntity wreck = new WreckEntity();
                wreck.setPosition(enemy.getX(), enemy.getY());
                wreck.setYspeed(enemy.getYspeed());
                GameState.entityManager.addEntity(wreck);
                enemy.dispose();
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

    public void moveLeft() {
        lane--;
        if (lane < 0) {
            lane = 0;
        }
    }
    
    public void moveRight() {
        lane++;
        if (lane > 4) {
            lane = 4;
        }
    }
}
