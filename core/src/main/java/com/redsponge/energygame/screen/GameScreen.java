package com.redsponge.energygame.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.redsponge.energygame.component.Mappers;
import com.redsponge.energygame.component.PhysicsComponent;
import com.redsponge.energygame.component.PositionComponent;
import com.redsponge.energygame.map.MapFetcher;
import com.redsponge.energygame.map.MapManager;
import com.redsponge.energygame.util.Constants;
import com.redsponge.energygame.system.PhysicsDebugSystem;
import com.redsponge.energygame.system.PhysicsSystem;
import com.redsponge.energygame.system.PlayerSystem;
import com.redsponge.energygame.system.RenderingSystem;
import com.redsponge.energygame.util.EntityFactory;

public class GameScreen extends AbstractScreen {

    private Engine engine;
    private FitViewport background;
    private FitViewport viewport;
    private FitViewport hudViewport;
    private TiledMap map;
    private float energy;
    private ScalingViewport scale;
    private MapManager mapManager;

    private Entity player;
    private Color barColor;
    private float displayedEnergy;

    private PooledEffect currentParticle;

    public GameScreen(GameAccessor ga) {
        super(ga);
    }

    @Override
    public void show() {
        assets.finishLoading();
        assets.getResources();

        viewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        background = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        hudViewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        scale = new ScalingViewport(Scaling.fill, 1, 1);

        engine = new Engine();


        PhysicsSystem ps = new PhysicsSystem(new Vector2(0, -10), Constants.DEFAULT_PPM, null);

        mapManager = new MapManager(ps, new TmxMapLoader().load(MapFetcher.getEasyMap()), engine);

        ps.setMapManager(mapManager);
        player = EntityFactory.getPlayer(assets);

        engine.addSystem(ps);
        engine.addEntityListener(Family.all(PositionComponent.class, PhysicsComponent.class).get(), ps);


        mapManager.setSystems(engine);
        mapManager.init();

        engine.addSystem(new PlayerSystem(this, assets));
        engine.addSystem(new PhysicsDebugSystem(ps.getWorld(), viewport));
        engine.addSystem(new RenderingSystem(shapeRenderer, batch, viewport, player, mapManager, assets, this));

        engine.addEntity(player);
        barColor = new Color(0, 0, 0, 1);

        displayedEnergy = 0;
        currentParticle = assets.getParticles().sparkle.spawn(new Vector2(0, 0));
    }

    @Override
    public void tick(float delta) {
//        addEnergy(3);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        background.apply();
        shapeRenderer.setProjectionMatrix(background.getCamera().combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.rect(0, 0, background.getWorldWidth(), background.getWorldHeight());
        shapeRenderer.end();



        engine.update(delta);
        PositionComponent pos = Mappers.position.get(player);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.end();
        assets.getParticles().cleanUp();
        renderHUD();
    }

    private void renderHUD() {
        hudViewport.apply();
        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.setProjectionMatrix(hudViewport.getCamera().combined);

        shapeRenderer.begin(ShapeType.Filled);
        Color target = (energy < Constants.HEAT_THRESHOLD ? Constants.NONE_COLOR : energy < Constants.LIGHT_THRESHOLD ? Constants.HEAT_COLOR : energy < Constants.ELECTRIC_THRESHOLD ? Constants.LIGHT_COLOR : Constants.ENERGY_COLOR);
        barColor.lerp(target, 0.1f);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(10, hudViewport.getWorldHeight() - 20, (viewport.getWorldWidth() - 20), 15);
        shapeRenderer.setColor(barColor);
        displayedEnergy = (1-0.1f) * displayedEnergy + 0.1f * energy;
        float progress = displayedEnergy / Constants.MAX_ENERGY;
        shapeRenderer.rect(15, hudViewport.getWorldHeight() - 15, (viewport.getWorldWidth() - 30) * progress, 5);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        background.update(width, height, true);
        hudViewport.update(width, height, true);
        scale.update(width, height, true);
    }

    public void addEnergy(float energy) {
        this.energy += energy;
        this.energy = this.energy < 0 ? 0 : this.energy;
        this.energy = this.energy > Constants.MAX_ENERGY ? Constants.MAX_ENERGY : this.energy;
    }

    public float getEnergy() {
        return energy;
    }

    public void addToEngine(Entity entity) {
        engine.addEntity(entity);
    }

    public void removeFromEngine(Entity entity) {
        engine.removeEntity(entity);
    }

    @Override
    public void dispose() {
        map.dispose();
    }
}