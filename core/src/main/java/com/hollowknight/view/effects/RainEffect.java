package com.hollowknight.view.effects;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

public class RainEffect {

    private final Texture texture;
    private final ArrayList<Drop> drops = new ArrayList<>();

    /*
     * City of Tears runtime coordinates.
     * Tiled y = 1536..3072 becomes runtime y = 4928..6464.
     */
    private final float xMin = 150f;
    private final float xMax = 1650f;

    private final float yTop = 6460f;
    private final float yBottom = 4928f;

    private final float speed = 900f;

    private static final int DROP_COUNT = 750;

    private class Drop {
        float x;
        float y;
        float length;
        float width;

        Drop() {
            reset(true);
        }

        void reset(boolean randomY) {
            x = MathUtils.random(xMin, xMax);
            y = randomY
                ? MathUtils.random(yBottom, yTop)
                : yTop;

            length = MathUtils.random(20f, 44f);
            width = MathUtils.random(3f, 5f);
        }
    }

    public RainEffect() {
        /*
         * Make our own bright rain texture instead of relying on
         * a tiny/faint PNG that may be almost invisible.
         */
        Pixmap pixmap = new Pixmap(4, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.65f, 0.82f, 1f, 0.55f);
        pixmap.fillRectangle(1, 0, 2, 64);

        texture = new Texture(pixmap);
        pixmap.dispose();

        for (int i = 0; i < DROP_COUNT; i++) {
            drops.add(new Drop());
        }
    }

    public void update(float delta) {
        for (Drop drop : drops) {
            drop.y -= speed * delta;

            if (drop.y < yBottom) {
                drop.reset(false);
            }
        }
    }

    public void draw(SpriteBatch batch) {
        batch.setColor(0.75f, 0.88f, 1f, 0.95f);

        for (Drop drop : drops) {
            batch.draw(
                texture,
                drop.x,
                drop.y,
                drop.width,
                drop.length
            );
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void dispose() {
        texture.dispose();
    }
}
