package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;

public interface Pogoable {

    Rectangle getPogoBounds();

    boolean canBePogoed();

    void onPogo();
}
