package com.hollowknight.model.world;

import com.badlogic.gdx.math.Rectangle;

public final class Platform {

    private final Rectangle bounds;

    public Platform(
        float x,
        float y,
        float width,
        float height
    ) {
        bounds = new Rectangle(
            x,
            y,
            width,
            height
        );
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
