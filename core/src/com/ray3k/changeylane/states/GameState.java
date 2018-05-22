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
package com.ray3k.changeylane.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.changeylane.Core;
import com.ray3k.changeylane.EntityManager;
import com.ray3k.changeylane.InputManager;
import com.ray3k.changeylane.Maths;
import com.ray3k.changeylane.ScrollingTiledDrawable;
import com.ray3k.changeylane.State;
import com.ray3k.changeylane.entities.CopEntity;
import com.ray3k.changeylane.entities.EnemyEntity;
import com.ray3k.changeylane.entities.PlayerEntity;
import com.ray3k.changeylane.entities.TrafficLightEntity;
import com.ray3k.changeylane.entities.TurboEntity;

public class GameState extends State {
    private static GameState instance;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    public static EntityManager entityManager;
    public static TextureAtlas spineAtlas;
    public static TwoColorPolygonBatch twoColorPolygonBatch;
    public static final float GAME_WIDTH = 800;
    public static final float GAME_HEIGHT = 600;
    
    private ScrollingTiledDrawable streetTile;
    private ScrollingTiledDrawable dashedTile;
    private ScrollingTiledDrawable lineTile;
    public static final float LANE_WIDTH = 65.0f;
    public static final float LINE_WIDTH = 12.0f;
    public static final int STREET_POSITION = (int) GAME_WIDTH / 2 - 192;
    
    public static float SCROLLING_SPEED_NORMAL = -200.0f;
    public static float SCROLLING_SPEED_TURBO = -500.0f;
    public static float scrollingSpeed;
    
    public static final float TURBO_TIME =  5.0f;
    public static float turboTimer;
    
    public static final float TURBO_SLOW_DOWN = 75.0f;
    
    public static final float ENEMY_MIN_SPEED = 100.0f;
    public static final float ENEMY_MAX_SPEED = 200.0f;
    
    public static final float ENEMY_MIN_SPAWN_TIMER = .5f;
    public static final float ENEMY_MAX_SPAWN_TIMER = 1.5f;
    public static final float ENEMY_SPAWN_TIMER_CHANGE = .005f;
    private float enemySpawnRate;
    private float enemySpawnTimer;
    
    public static final float COP_MIN_SPAWN_TIMER = 15.0f;
    public static final float COP_MAX_SPAWN_TIMER = 20.0f;
    private float copSpawnTimer;
    
    public static final float TURBO_MIN_SPAWN_TIMER = 25.0f;
    public static final float TURBO_MAX_SPAWN_TIMER = 40.0f;
    private float turboSpawnTimer;
    
    private Sound engineSound;
    private long engineSoundIndex;
    
    public static int secondScore;
    public static int wreckScore;
    public static int copScore;
    public static int gameOverScore;
    public static int turboScore;
    
    public static final float START_DELAY = 4.5f;
    
    public static final float POINT_DELAY = 1.0f;
    private float pointTimer;
    
    public static GameState inst() {
        return instance;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        instance = this;
        
        spineAtlas = Core.assetManager.get(Core.DATA_PATH + "/spine/Changy Lane.atlas", TextureAtlas.class);
        
        score = 0;
        
        inputManager = new InputManager();
        
        gameCamera = new OrthographicCamera();
//        gameCamera.position.set(0.0f, 0.0f, 0.0f);
        gameViewport = new ExtendViewport(GAME_WIDTH, GAME_HEIGHT, gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        gameViewport.apply();
        
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Changy Lane.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                PlayerEntity player = entityManager.get(PlayerEntity.class);
                
                if (player != null) {
                    if (keycode == Keys.LEFT) {
                        player.moveLeft();
                    } else if (keycode == Keys.RIGHT) {
                        player.moveRight();
                    }
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        entityManager = new EntityManager();
        
        twoColorPolygonBatch = new TwoColorPolygonBatch(3100);
        
        createStageElements();
        
        streetTile = new ScrollingTiledDrawable(spineAtlas.findRegion("street-tile"));
        dashedTile = new ScrollingTiledDrawable(spineAtlas.findRegion("dashed-line"));
        lineTile = new ScrollingTiledDrawable(spineAtlas.findRegion("line"));
        
        PlayerEntity player = new PlayerEntity();
        entityManager.addEntity(player);
        
        enemySpawnRate = ENEMY_MAX_SPAWN_TIMER;
        enemySpawnTimer = START_DELAY;
        
        scrollingSpeed = SCROLLING_SPEED_NORMAL;
        
        copSpawnTimer = MathUtils.random(COP_MIN_SPAWN_TIMER, COP_MAX_SPAWN_TIMER);
        
        engineSound = Gdx.audio.newSound(Gdx.files.local(Core.DATA_PATH + "/sfx/engine.wav"));
        engineSoundIndex = engineSound.loop();
        
        TrafficLightEntity light = new TrafficLightEntity();
        light.setPosition(GAME_WIDTH / 2.0f, GAME_HEIGHT / 2.0f);
        entityManager.addEntity(light);
        
        turboSpawnTimer = START_DELAY;
        
        String string = Gdx.files.local(Core.DATA_PATH + "/data/points.txt").readString();
        String strings[] = string.split("\\r\\n");
        System.out.println(strings[0]);
        System.out.println(strings[0].split(":")[1]);
        secondScore = Integer.parseInt(strings[0].split(":")[1]);
        wreckScore = Integer.parseInt(strings[1].split(":")[1]);
        copScore = Integer.parseInt(strings[2].split(":")[1]);
        gameOverScore = Integer.parseInt(strings[3].split(":")[1]);
        turboScore = Integer.parseInt(strings[4].split(":")[1]);
        
        pointTimer = POINT_DELAY;
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label("Score: 0", skin);
        label.setName("score");
        root.add(label).expand().top().left().pad(10.0f);
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0 / 255.0f, 146 / 255.0f, 69 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        int accumulator = STREET_POSITION;
        lineTile.draw(spriteBatch, accumulator, 0.0f, LINE_WIDTH, GAME_HEIGHT);
        accumulator += LINE_WIDTH;
        
        streetTile.draw(spriteBatch, accumulator, 0.0f, LANE_WIDTH, GAME_HEIGHT);
        accumulator += LANE_WIDTH;
        
        dashedTile.draw(spriteBatch, accumulator, 0.0f, LINE_WIDTH, GAME_HEIGHT);
        accumulator += LINE_WIDTH;
        
        streetTile.draw(spriteBatch, accumulator, 0.0f, LANE_WIDTH, GAME_HEIGHT);
        accumulator += LANE_WIDTH;
        
        dashedTile.draw(spriteBatch, accumulator, 0.0f, LINE_WIDTH, GAME_HEIGHT);
        accumulator += LINE_WIDTH;
        
        streetTile.draw(spriteBatch, accumulator, 0.0f, LANE_WIDTH, GAME_HEIGHT);
        accumulator += LANE_WIDTH;
        
        dashedTile.draw(spriteBatch, accumulator, 0.0f, LINE_WIDTH, GAME_HEIGHT);
        accumulator += LINE_WIDTH;
        
        streetTile.draw(spriteBatch, accumulator, 0.0f, LANE_WIDTH, GAME_HEIGHT);
        accumulator += LANE_WIDTH;
        
        dashedTile.draw(spriteBatch, accumulator, 0.0f, LINE_WIDTH, GAME_HEIGHT);
        accumulator += LINE_WIDTH;
        
        streetTile.draw(spriteBatch, accumulator, 0.0f, LANE_WIDTH, GAME_HEIGHT);
        accumulator += LANE_WIDTH;
        
        lineTile.draw(spriteBatch, accumulator, 0.0f, LINE_WIDTH, GAME_HEIGHT);
        accumulator += LINE_WIDTH;
        
        spriteBatch.end();
        
        twoColorPolygonBatch.setProjectionMatrix(gameCamera.combined);
        twoColorPolygonBatch.begin();
        twoColorPolygonBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        entityManager.draw(spriteBatch, delta);
        twoColorPolygonBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        dashedTile.setOffsetY(dashedTile.getOffsetY() + scrollingSpeed * delta);
        streetTile.setOffsetY(streetTile.getOffsetY() + scrollingSpeed * delta);
        lineTile.setOffsetY(lineTile.getOffsetY() + scrollingSpeed * delta);
        
        enemySpawnRate -= ENEMY_SPAWN_TIMER_CHANGE * delta;
        if (enemySpawnRate < ENEMY_MIN_SPAWN_TIMER) {
            enemySpawnRate = ENEMY_MIN_SPAWN_TIMER;
        }
        
        enemySpawnTimer -= delta;
        if (enemySpawnTimer < 0) {
            if (turboTimer < 0) {
                enemySpawnTimer = enemySpawnRate;
            } else {
                enemySpawnTimer = enemySpawnRate / 2.0f;
            }
            
            EnemyEntity enemy = new EnemyEntity();
            int lane = MathUtils.random(4);
            enemy.setX(GameState.STREET_POSITION + GameState.LINE_WIDTH + GameState.LANE_WIDTH / 2.0f + (GameState.LANE_WIDTH + GameState.LINE_WIDTH) * lane);
            enemy.setY(GameState.GAME_HEIGHT + 300);
            enemy.setYspeed(-MathUtils.random(ENEMY_MIN_SPEED, ENEMY_MAX_SPEED));
            com.esotericsoftware.spine.Skin skin = enemy.getSkeleton().getData().getSkins().random();
            while (skin.getName().equals("default")) skin = enemy.getSkeleton().getData().getSkins().random();
            enemy.getSkeleton().setSkin(skin);
            entityManager.addEntity(enemy);
        }
        
        copSpawnTimer -= delta;
        if (copSpawnTimer < 0) {
            copSpawnTimer = MathUtils.random(COP_MIN_SPAWN_TIMER, COP_MAX_SPAWN_TIMER);
            
            CopEntity cop = new CopEntity();
            int lane = MathUtils.random(4);
            cop.setX(GameState.STREET_POSITION + GameState.LINE_WIDTH + GameState.LANE_WIDTH / 2.0f + (GameState.LANE_WIDTH + GameState.LINE_WIDTH) * lane);
            cop.setY(-800);
            cop.setYspeed(900.0f);
            entityManager.addEntity(cop);
        }
        
        turboSpawnTimer -= delta;
        
        if (turboSpawnTimer < 0) {
            turboSpawnTimer = MathUtils.random(TURBO_MIN_SPAWN_TIMER, TURBO_MAX_SPAWN_TIMER);
            
            TurboEntity turbo = new TurboEntity();
            int lane = MathUtils.random(4);
            turbo.setX(GameState.STREET_POSITION + GameState.LINE_WIDTH + GameState.LANE_WIDTH / 2.0f + (GameState.LANE_WIDTH + GameState.LINE_WIDTH) * lane);
            turbo.setY(GameState.GAME_HEIGHT + 200);
            turbo.setYspeed(GameState.scrollingSpeed);
            entityManager.addEntity(turbo);
        }
        
        if (turboTimer >= 0) {
            engineSound.setPitch(engineSoundIndex, 1.5f);
            
            turboTimer -= delta;
            if (turboTimer < 0) {
                scrollingSpeed = Maths.approach(scrollingSpeed, SCROLLING_SPEED_NORMAL, TURBO_SLOW_DOWN * delta);
                
                engineSound.setPitch(engineSoundIndex, 1.0f + .5f * (scrollingSpeed - SCROLLING_SPEED_NORMAL) / (SCROLLING_SPEED_TURBO - SCROLLING_SPEED_NORMAL));
                
                if (!MathUtils.isEqual(scrollingSpeed, SCROLLING_SPEED_NORMAL)) {
                    turboTimer = 0;
                } else {
                    PlayerEntity player = entityManager.get(PlayerEntity.class);
                    if (player != null) {
                        player.getAnimationState().setAnimation(0, "animation", true);
                    }
                }
            }
        } else {
            if (entityManager.get(PlayerEntity.class) != null) {
                if (Gdx.input.isKeyPressed(Keys.UP)) {
                    engineSound.setPitch(engineSoundIndex, 1.2f);
                } else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
                    engineSound.setPitch(engineSoundIndex, .9f);
                } else {
                    engineSound.setPitch(engineSoundIndex, 1.0f);
                }
            } else {
                engineSound.stop();
            }
        }
        
        pointTimer -= delta;
        if (pointTimer < 0) {
            pointTimer = POINT_DELAY;
            
            addScore(secondScore);
        }
        
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Core.stateManager.loadState("menu");
        }
    }

    @Override
    public void dispose() {
        if (twoColorPolygonBatch != null) {
            twoColorPolygonBatch.dispose();
        }
    }

    @Override
    public void stop() {
        stage.dispose();
        engineSound.stop();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, false);
        stage.getViewport().update(width, height, true);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        if (score > highscore) {
            highscore = score;
        }
        
        Label label = stage.getRoot().findActor("score");
        label.setText("Score: " + Integer.toString(this.score));
    }
    
    public void addScore(int score) {
        this.score += score;
        if (this.score > highscore) {
            highscore = this.score;
        }
        
        Label label = stage.getRoot().findActor("score");
        label.setText("Score: " + Integer.toString(this.score));
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void playSound(String name) {
        playSound(name, 1.0f, 1.0f);
    }
    
    public void playSound (String name, float volume) {
        playSound(name, volume, 1.0f);
    }
    
    /**
     * 
     * @param name
     * @param volume
     * @param pitch .5 to 2. 1 is default
     */
    public void playSound(String name, float volume, float pitch) {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/" + name + ".wav", Sound.class).play(volume, pitch, 0.0f);
    }
    
    public void turboMode() {
        playSound("screech");
        
        scrollingSpeed = SCROLLING_SPEED_TURBO;
        turboTimer = TURBO_TIME;
        PlayerEntity player = entityManager.get(PlayerEntity.class);
        if (player != null) {
            player.getAnimationState().setAnimation(0, "boosting", true);
        }
    }
}