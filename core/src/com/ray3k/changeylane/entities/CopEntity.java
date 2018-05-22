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
public class CopEntity extends SpineTwoColorEntity {
    private WarningEntity warning;
    private boolean playedSiren;

    public CopEntity() {
        super(Core.DATA_PATH + "/spine/cop.json", "animation", GameState.twoColorPolygonBatch);
        playedSiren = false;
    }

    @Override
    public void actSub(float delta) {
        if (getY() > GameState.GAME_HEIGHT + 100) {
            if (GameState.entityManager.get(PlayerEntity.class) != null) {
                GameState.inst().addScore(GameState.copScore);
            }
            
            dispose();
        }
        
        if (getY() > -100 && !playedSiren) {
            playedSiren = true;
            warning.dispose();
            GameState.inst().playSound("siren");
        }
        
        for (EnemyEntity enemy : GameState.entityManager.getAll(EnemyEntity.class)) {
            if (getSkeletonBounds().aabbIntersectsSkeleton(enemy.getSkeletonBounds())) {
                GameState.inst().playSound("crash");
                
                enemy.dispose();
                
                WreckEntity wreck = new WreckEntity();
                wreck.setPosition(enemy.getX(), enemy.getY());
                wreck.setYspeed(-150.0f);
                GameState.entityManager.addEntity(wreck);
            }
        }
        
        PlayerEntity player = GameState.entityManager.get(PlayerEntity.class);
        if (player != null) {
            if (getSkeletonBounds().aabbIntersectsSkeleton(player.getSkeletonBounds())) {
                GameState.inst().addScore(GameState.wreckScore);
                
                GameState.inst().playSound("crash");
                if (GameState.turboTimer < 0) {
                    player.dispose();
                    GameState.entityManager.addEntity(new GameOverTimerEntity(5.0f));

                    WreckEntity wreck = new WreckEntity();
                    wreck.setPosition(player.getX(), player.getY());
                    wreck.setYspeed(-150.0f);
                    GameState.entityManager.addEntity(wreck);
                } else {
                    dispose();

                    WreckEntity wreck = new WreckEntity();
                    wreck.setPosition(getX(), getY());
                    wreck.setYspeed(-150.0f);
                    GameState.entityManager.addEntity(wreck);
                }
            }
        }
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
        warning = new WarningEntity();
        warning.setX(getX());
        warning.setY(50.0f);
        GameState.entityManager.addEntity(warning);
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
