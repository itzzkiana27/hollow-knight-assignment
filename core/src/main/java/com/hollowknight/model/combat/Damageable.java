package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;

public interface Damageable {

    Rectangle getBounds();

    void takeDamage(int damage);

    boolean isAlive();
}
