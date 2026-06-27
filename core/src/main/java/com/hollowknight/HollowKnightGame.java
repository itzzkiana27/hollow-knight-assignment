package com.hollowknight;

import com.badlogic.gdx.Game;
import com.hollowknight.screen.MainMenuScreen;

public class HollowKnightGame extends Game {
    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
