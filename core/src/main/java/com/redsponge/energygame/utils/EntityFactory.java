package com.redsponge.energygame.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.redsponge.energygame.components.CircleBottomComponent;
import com.redsponge.energygame.components.ColliderComponent;
import com.redsponge.energygame.components.EnemyComponent;
import com.redsponge.energygame.components.PhysicsComponent;
import com.redsponge.energygame.components.PlayerComponent;
import com.redsponge.energygame.components.PositionComponent;
import com.redsponge.energygame.components.SizeComponent;
import com.redsponge.energygame.components.VelocityComponent;

public class EntityFactory {


    public static Entity getPlayer() {
        Entity player = new Entity();
        player.add(new PositionComponent(100, 100));
        player.add(new VelocityComponent(0, 0));
        player.add(new SizeComponent(20, 20));
        player.add(new PhysicsComponent(BodyType.DynamicBody));
        player.add(new PlayerComponent());
        player.add(new ColliderComponent());
        player.add(new CircleBottomComponent(10));

        return player;
    }
    public static Entity getEnemy(float x, float y, float width, float height) {
        Entity enemy = new Entity();
        enemy.add(new PositionComponent(x, y));
        enemy.add(new SizeComponent(width, height));
        enemy.add(new PhysicsComponent(BodyType.DynamicBody));
        enemy.add(new EnemyComponent());

        return enemy;
    }
}
