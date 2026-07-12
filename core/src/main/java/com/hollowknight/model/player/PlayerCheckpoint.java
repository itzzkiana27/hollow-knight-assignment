package com.hollowknight.model.player;

import com.badlogic.gdx.math.Vector2;

public final class PlayerCheckpoint {

    private final Vector2 position;

    public PlayerCheckpoint(float initialX, float initialY) {
        position = new Vector2(initialX, initialY);
    }

    public void save(float x, float y) {
        position.set(x, y);
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }
}
