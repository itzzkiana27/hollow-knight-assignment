package com.hollowknight.view.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;

public class RainEffect {

    private Texture texture;

    private ArrayList<Drop> drops = new ArrayList<>();

    // City shaft rain area
    private float xMin = 700;
    private float xMax = 1100;

    private float yTop = 1500;
    private float yBottom = 650;

    private float speed = 350;


    private class Drop {
        float x;
        float y;

        Drop() {
            reset();
        }

        void reset() {
            x = MathUtils.random(xMin, xMax);
            y = MathUtils.random(yBottom, yTop);
        }
    }


    public RainEffect() {

        texture = new Texture(
            "particles/rain/rain_particle.png"
        );

        for(int i = 0; i < 60; i++) {
            drops.add(new Drop());
        }
    }


    public void update(float delta) {

        for(Drop drop : drops) {

            drop.y -= speed * delta;

            if(drop.y < yBottom) {
                drop.reset();
            }
        }
    }


    public void draw(SpriteBatch batch) {

        for(Drop drop : drops) {

            batch.draw(
                texture,
                drop.x,
                drop.y,
                3,
                30
            );
        }
    }
}
