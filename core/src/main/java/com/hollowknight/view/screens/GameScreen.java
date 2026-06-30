package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.GameController;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.view.animation.KnightAnimationManager;
import com.badlogic.gdx.math.Rectangle;

public class GameScreen extends ScreenAdapter {

    private static final float GROUND_Y = 100f;

    private static final float SOURCE_FRAME_WIDTH =
        349f;

    private static final float SOURCE_FRAME_HEIGHT =
        186f;

    private static final float KNIGHT_DRAW_HEIGHT =
        150f;

    private static final float KNIGHT_DRAW_WIDTH =
        KNIGHT_DRAW_HEIGHT
            * SOURCE_FRAME_WIDTH
            / SOURCE_FRAME_HEIGHT;

    /*
     * The supplied Knight frames appear to face left.
     *
     * Change this to true only if the direction appears
     * reversed on your computer.
     */
    private static final boolean SOURCE_FACES_RIGHT =
        false;

    private final GameController controller;

    private Stage stage;
    private Skin skin;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private KnightAnimationManager animationManager;

    public GameScreen(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        skin = new Skin(
            Gdx.files.internal("ui/uiskin.json")
        );

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        animationManager =
            new KnightAnimationManager();

        createInterface();

        Gdx.input.setInputProcessor(stage);
    }

    private void createInterface() {
        Table instructionsTable = new Table();

        instructionsTable.setFillParent(true);
        instructionsTable.top();

        Label title = new Label(
            controller.text(
                "game.animationTest.title"
            ),
            skin
        );

        title.setFontScale(1.35f);

        Label instructions = new Label(
            controller.text(
                "game.animationTest.controls1"
            )
                + "\n"
                + controller.text(
                "game.animationTest.controls2"
            )
                + "\n"
                + controller.text(
                "game.animationTest.controls3"
            )
                + "\n"
                + controller.text(
                "game.animationTest.controls4"
            )
                + "\n"
                + controller.text(
                "game.animationTest.controls5"
            )
                + "\n"
                + controller.text(
                "game.animationTest.controls6"
            ),
            skin
        );

        instructions.setAlignment(Align.center);
        instructions.setWrap(true);

        instructionsTable.add(title)
            .padTop(15f)
            .padBottom(8f)
            .row();

        instructionsTable.add(instructions)
            .width(900f)
            .row();

        TextButton backButton = new TextButton(
            controller.text(
                "game.returnToMainMenu"
            ),
            skin
        );

        backButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.returnToMainMenu();
                }
            }
        );

        Table backButtonTable = new Table();

        backButtonTable.setFillParent(true);
        backButtonTable.bottom().left();

        backButtonTable.add(backButton)
            .width(240f)
            .height(50f)
            .pad(20f);

        stage.addActor(instructionsTable);
        stage.addActor(backButtonTable);
    }

    @Override
    public void render(float delta) {
        controller.update(
            delta,
            stage.getViewport().getWorldWidth(),
            KNIGHT_DRAW_WIDTH,
            KNIGHT_DRAW_HEIGHT
        );

        Gdx.gl.glClearColor(
            0.02f,
            0.02f,
            0.05f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        drawTemporaryGround();
        drawPracticeTarget();
        drawKnight();
        drawActiveAttackHitbox();

        stage.act(
            Math.min(delta, 1f / 30f)
        );

        stage.draw();

        finishAnimationIfNecessary();
    }

    private void drawTemporaryGround() {
        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
            new Color(
                0.16f,
                0.18f,
                0.25f,
                1f
            )
        );

        shapeRenderer.rect(
            0f,
            0f,
            stage.getViewport().getWorldWidth(),
            GROUND_Y
        );

        shapeRenderer.end();
    }
    private void drawPracticeTarget() {
        Rectangle target =
            controller.getPracticeTargetBounds();

        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        if (
            controller
                .isPracticeTargetFlashing()
        ) {
            shapeRenderer.setColor(
                Color.WHITE
            );
        } else {
            shapeRenderer.setColor(
                0.45f,
                0.48f,
                0.55f,
                1f
            );
        }

        shapeRenderer.rect(
            target.x,
            target.y,
            target.width,
            target.height
        );

        /*
         * Temporary eyes, only to make the
         * target easier to recognize.
         */
        shapeRenderer.setColor(
            0.12f,
            0.13f,
            0.18f,
            1f
        );

        float eyeRadius = 4f;

        float eyeY =
            target.y
                + target.height * 0.64f;

        shapeRenderer.circle(
            target.x
                + target.width * 0.34f,
            eyeY,
            eyeRadius
        );

        shapeRenderer.circle(
            target.x
                + target.width * 0.66f,
            eyeY,
            eyeRadius
        );

        shapeRenderer.end();
    }

    private void drawKnight() {
        Player player = controller.getPlayer();

        TextureRegion frame =
            animationManager.getFrame(
                player.getAnimationType(),
                player.getAnimationTime()
            );

        batch.setProjectionMatrix(
            stage.getCamera().combined
        );

        batch.begin();

        float x = player.getPosition().x;
        float y = player.getPosition().y;

        boolean shouldFlip =
            player.isFacingRight()
                != SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                x + KNIGHT_DRAW_WIDTH,
                y,
                -KNIGHT_DRAW_WIDTH,
                KNIGHT_DRAW_HEIGHT
            );
        } else {
            batch.draw(
                frame,
                x,
                y,
                KNIGHT_DRAW_WIDTH,
                KNIGHT_DRAW_HEIGHT
            );
        }

        batch.end();
    }
    private void drawActiveAttackHitbox() {
        if (
            !controller
                .isAttackHitboxActive()
        ) {
            return;
        }

        Rectangle hitbox =
            controller.getAttackHitbox();

        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            Color.RED
        );

        shapeRenderer.rect(
            hitbox.x,
            hitbox.y,
            hitbox.width,
            hitbox.height
        );

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void finishAnimationIfNecessary() {
        Player player = controller.getPlayer();

        PlayerAnimationType animationType =
            player.getAnimationType();

        if (
            animationManager.isLooping(
                animationType
            )
        ) {
            return;
        }

        if (
            animationManager.isFinished(
                animationType,
                player.getAnimationTime()
            )
        ) {
            controller.onAnimationFinished(
                animationType
            );
        }
    }

    @Override
    public void resize(
        int width,
        int height
    ) {
        stage.getViewport().update(
            width,
            height,
            true
        );
    }

    @Override
    public void hide() {
        if (
            Gdx.input.getInputProcessor() == stage
        ) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (animationManager != null) {
            animationManager.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        if (stage != null) {
            stage.dispose();
        }

        if (skin != null) {
            skin.dispose();
        }
    }
}
