package com.hollowknight.view.effects;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class RainEffect {

    private static final int DROP_COUNT = 240;

    private static final float EDGE_MARGIN = 70f;
    private static final float WIND_SPEED = 42f;
    private static final float DROP_ROTATION = -6f;

    private final Texture texture;
    private final TextureRegion textureRegion;
    private final ArrayList<Drop> drops = new ArrayList<>();

    private boolean initialized;

    private static final class Drop {
        float x;
        float y;
        float length;
        float width;
        float speed;
        float alpha;
    }

    public RainEffect() {
        /*
         * A tiny soft white-blue strip is stretched into each drop. Keeping
         * the source texture narrow prevents the old thick vertical bars.
         */
        Pixmap pixmap = new Pixmap(2, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.72f, 0.86f, 1f, 1f);
        pixmap.fill();

        texture = new Texture(pixmap);
        textureRegion = new TextureRegion(texture);
        texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );
        pixmap.dispose();

        for (int index = 0; index < DROP_COUNT; index++) {
            drops.add(new Drop());
        }
    }

    public void update(
        float delta,
        OrthographicCamera camera
    ) {
        if (camera == null) {
            return;
        }

        float visibleWidth = camera.viewportWidth * camera.zoom;
        float visibleHeight = camera.viewportHeight * camera.zoom;

        float left = camera.position.x - visibleWidth / 2f - EDGE_MARGIN;
        float right = camera.position.x + visibleWidth / 2f + EDGE_MARGIN;
        float bottom = camera.position.y - visibleHeight / 2f - EDGE_MARGIN;
        float top = camera.position.y + visibleHeight / 2f + EDGE_MARGIN;

        if (!initialized) {
            for (Drop drop : drops) {
                resetDrop(
                    drop,
                    left,
                    right,
                    bottom,
                    top,
                    true
                );
            }

            initialized = true;
        }

        float safeDelta = MathUtils.clamp(
            delta,
            0f,
            1f / 15f
        );

        for (Drop drop : drops) {
            drop.x -= WIND_SPEED * safeDelta;
            drop.y -= drop.speed * safeDelta;

            boolean farOutsideCamera =
                drop.x < left - visibleWidth
                    || drop.x > right + visibleWidth
                    || drop.y < bottom - visibleHeight
                    || drop.y > top + visibleHeight;

            if (farOutsideCamera) {
                resetDrop(
                    drop,
                    left,
                    right,
                    bottom,
                    top,
                    true
                );
            } else if (
                drop.y + drop.length < bottom
                    || drop.x + drop.width < left
            ) {
                resetDrop(
                    drop,
                    left,
                    right,
                    bottom,
                    top,
                    false
                );
            }
        }
    }

    private void resetDrop(
        Drop drop,
        float left,
        float right,
        float bottom,
        float top,
        boolean anywhere
    ) {
        drop.x = MathUtils.random(left, right);
        drop.y = anywhere
            ? MathUtils.random(bottom, top)
            : MathUtils.random(top, top + 180f);

        drop.length = MathUtils.random(18f, 42f);
        drop.width = MathUtils.random(0.70f, 1.45f);
        drop.speed = MathUtils.random(520f, 760f);
        drop.alpha = MathUtils.random(0.28f, 0.58f);
    }

    public void draw(SpriteBatch batch) {
        for (Drop drop : drops) {
            batch.setColor(
                0.62f,
                0.80f,
                1f,
                drop.alpha
            );

            batch.draw(
                textureRegion,
                drop.x,
                drop.y,
                drop.width / 2f,
                drop.length / 2f,
                drop.width,
                drop.length,
                1f,
                1f,
                DROP_ROTATION
            );
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void dispose() {
        texture.dispose();
    }
}
