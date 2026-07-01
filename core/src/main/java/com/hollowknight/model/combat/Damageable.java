package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents an object that can receive combat damage.
 */
public interface Damageable {

    Rectangle getBounds();

    void takeDamage(int damage);

    boolean isAlive();
}
