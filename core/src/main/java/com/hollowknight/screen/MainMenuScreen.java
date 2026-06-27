package com.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.hollowknight.HollowKnightGame;

public class MainMenuScreen extends ScreenAdapter {
    private final HollowKnightGame game;

    public MainMenuScreen(HollowKnightGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("Main menu opened");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
    }
}
