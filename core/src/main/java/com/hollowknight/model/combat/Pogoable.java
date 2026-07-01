package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents an object that can be struck from above
 * to perform a pogo bounce.
 */
public interface Pogoable {

    Rectangle getPogoBounds();

    boolean canBePogoed();

    void onPogo();
}
