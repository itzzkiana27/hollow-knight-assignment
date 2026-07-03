package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
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
import com.hollowknight.model.enemy.Crawlid;
import com.hollowknight.model.enemy.HuskHornhead;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.model.world.Platform;
import com.hollowknight.view.animation.CrawlidAnimationManager;
import com.hollowknight.view.animation.HuskHornheadAnimationManager;
import com.hollowknight.view.animation.KnightAnimationManager;
import com.hollowknight.view.camera.GameCamera;

public class GameScreen extends ScreenAdapter {

    /*
     * Complete temporary level dimensions.
     */
    private static final float WORLD_WIDTH =
        2400f;

    private static final float WORLD_HEIGHT =
        1000f;

    /*
     * Visible camera area.
     */
    private static final float CAMERA_VIEW_WIDTH =
        1280f;

    private static final float CAMERA_VIEW_HEIGHT =
        720f;

    /*
     * Knight rendering values.
     */
    private static final float
        KNIGHT_SOURCE_FRAME_WIDTH = 349f;

    private static final float
        KNIGHT_SOURCE_FRAME_HEIGHT = 186f;

    private static final float
        KNIGHT_DRAW_HEIGHT = 150f;

    private static final float
        KNIGHT_DRAW_WIDTH =
        KNIGHT_DRAW_HEIGHT
            * KNIGHT_SOURCE_FRAME_WIDTH
            / KNIGHT_SOURCE_FRAME_HEIGHT;

    /*
     * The supplied Knight frames face left.
     */
    private static final boolean
        KNIGHT_SOURCE_FACES_RIGHT = false;

    /*
     * Husk Hornhead rendering values.
     *
     * Every Husk frame is 239 x 219.
     */
    private static final float
        HUSK_SOURCE_WIDTH = 239f;

    private static final float
        HUSK_SOURCE_HEIGHT = 219f;

    private static final float
        HUSK_DRAW_HEIGHT = 145f;

    private static final float
        HUSK_DRAW_WIDTH =
        HUSK_DRAW_HEIGHT
            * HUSK_SOURCE_WIDTH
            / HUSK_SOURCE_HEIGHT;

    /*
     * The supplied Husk frames face left.
     */
    private static final boolean
        HUSK_SOURCE_FACES_RIGHT = false;

    /*
     * Change to true to display the Husk body
     * and vision rectangles.
     */
    private static final boolean
        DRAW_HUSK_DEBUG = false;

    /*
     * Crawlid walk frames are 301 x 149.
     *
     * Death frames have a slightly different canvas
     * size. The renderer calculates their dimensions
     * dynamically while keeping the same scale.
     */
    private static final float
        CRAWLID_SOURCE_HEIGHT = 149f;

    private static final float
        CRAWLID_DRAW_HEIGHT = 105f;

    private static final float
        CRAWLID_DRAW_SCALE =
        CRAWLID_DRAW_HEIGHT
            / CRAWLID_SOURCE_HEIGHT;

    /*
     * The supplied Crawlid frames face left.
     */
    private static final boolean
        CRAWLID_SOURCE_FACES_RIGHT = false;

    /*
     * Change to true to display the Crawlid body.
     */
    private static final boolean
        DRAW_CRAWLID_DEBUG = false;

    private final GameController controller;

    private Stage stage;
    private Skin skin;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private KnightAnimationManager
        knightAnimationManager;

    private HuskHornheadAnimationManager
        huskAnimationManager;

    private CrawlidAnimationManager
        crawlidAnimationManager;

    private GameCamera worldCamera;

    public GameScreen(
        GameController controller
    ) {
        this.controller = controller;
    }

    @Override
    public void show() {
        /*
         * The Stage is used only for fixed
         * screen-space interface elements.
         */
        stage = new Stage(
            new ScreenViewport()
        );

        skin = new Skin(
            Gdx.files.internal(
                "ui/uiskin.json"
            )
        );

        batch = new SpriteBatch();

        shapeRenderer =
            new ShapeRenderer();

        knightAnimationManager =
            new KnightAnimationManager();

        huskAnimationManager =
            new HuskHornheadAnimationManager();

        crawlidAnimationManager =
            new CrawlidAnimationManager();

        worldCamera = new GameCamera(
            CAMERA_VIEW_WIDTH,
            CAMERA_VIEW_HEIGHT,
            WORLD_WIDTH,
            WORLD_HEIGHT
        );

        worldCamera.resize(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        resetWorldCamera();

        createInterface();

        Gdx.input.setInputProcessor(stage);
    }

    private void resetWorldCamera() {
        Player player =
            controller.getPlayer();

        worldCamera.reset(
            getPlayerCameraTargetX(player),
            getPlayerCameraTargetY(player)
        );
    }

    private void createInterface() {
        Table instructionsTable =
            new Table();

        instructionsTable.setFillParent(
            true
        );

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

        instructions.setAlignment(
            Align.center
        );

        instructions.setWrap(true);

        instructionsTable.add(title)
            .padTop(15f)
            .padBottom(8f)
            .row();

        instructionsTable
            .add(instructions)
            .width(900f)
            .row();

        TextButton backButton =
            new TextButton(
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
                    controller
                        .returnToMainMenu();
                }
            }
        );

        Table backButtonTable =
            new Table();

        backButtonTable.setFillParent(
            true
        );

        backButtonTable
            .bottom()
            .left();

        backButtonTable.add(backButton)
            .width(240f)
            .height(50f)
            .pad(20f);

        stage.addActor(
            instructionsTable
        );

        stage.addActor(
            backButtonTable
        );
    }

    @Override
    public void render(float delta) {
        /*
         * The controller receives the width of the
         * complete temporary level.
         */
        controller.update(
            delta,
            WORLD_WIDTH,
            KNIGHT_DRAW_WIDTH,
            KNIGHT_DRAW_HEIGHT
        );

        updateWorldCamera(delta);

        Gdx.gl.glClearColor(
            0.02f,
            0.02f,
            0.05f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        /*
         * Apply the moving camera before drawing
         * world-space objects.
         */
        worldCamera.apply();

        drawPlatforms();
        drawSpikeHazard();

        drawHuskHornhead();

        if (DRAW_HUSK_DEBUG) {
            drawHuskHornheadDebug();
        }

        drawCrawlid();

        if (DRAW_CRAWLID_DEBUG) {
            drawCrawlidDebug();
        }

        drawKnight();
        drawActiveAttackHitbox();

        stage.act(
            Math.min(
                delta,
                1f / 30f
            )
        );

        /*
         * Restore the fixed UI viewport before
         * drawing the HUD and Scene2D actors.
         */
        stage.getViewport().apply();

        drawPlayerHud();

        stage.draw();

        finishAnimationIfNecessary();
    }

    private void updateWorldCamera(
        float delta
    ) {
        Player player =
            controller.getPlayer();

        worldCamera.update(
            Math.min(
                delta,
                1f / 30f
            ),
            getPlayerCameraTargetX(player),
            getPlayerCameraTargetY(player)
        );
    }

    private float getPlayerCameraTargetX(
        Player player
    ) {
        return player.getPosition().x
            + KNIGHT_DRAW_WIDTH / 2f;
    }

    private float getPlayerCameraTargetY(
        Player player
    ) {
        return player.getPosition().y
            + KNIGHT_DRAW_HEIGHT / 2f;
    }

    private void drawPlatforms() {
        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
            0.16f,
            0.18f,
            0.25f,
            1f
        );

        for (
            Platform platform
            : controller.getPlatforms()
        ) {
            Rectangle bounds =
                platform.getBounds();

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            );
        }

        shapeRenderer.end();
    }

    private void drawSpikeHazard() {
        Rectangle spikes =
            controller.getSpikeBounds();

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        if (
            controller.isSpikeFlashing()
        ) {
            shapeRenderer.setColor(
                Color.WHITE
            );
        } else {
            shapeRenderer.setColor(
                0.72f,
                0.74f,
                0.82f,
                1f
            );
        }

        int spikeCount = 6;

        float spikeWidth =
            spikes.width / spikeCount;

        for (
            int index = 0;
            index < spikeCount;
            index++
        ) {
            float left =
                spikes.x
                    + index * spikeWidth;

            float right =
                left + spikeWidth;

            float center =
                left + spikeWidth / 2f;

            shapeRenderer.triangle(
                left,
                spikes.y,
                right,
                spikes.y,
                center,
                spikes.y
                    + spikes.height
            );
        }

        shapeRenderer.end();
    }

    private void drawHuskHornhead() {
        HuskHornhead husk =
            controller.getHuskHornhead();

        TextureRegion frame =
            huskAnimationManager.getFrame(
                husk.getAnimationType(),
                husk.getAnimationTime()
            );

        Rectangle body =
            husk.getBounds();

        /*
         * Center the larger source frame around the
         * smaller physical collision body.
         */
        float drawX =
            body.x
                + body.width / 2f
                - HUSK_DRAW_WIDTH / 2f;

        float drawY =
            body.y - 7f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        if (husk.isFlashing()) {
            batch.setColor(
                1f,
                0.55f,
                0.55f,
                1f
            );
        }

        boolean shouldFlip =
            husk.isFacingRight()
                != HUSK_SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + HUSK_DRAW_WIDTH,
                drawY,
                -HUSK_DRAW_WIDTH,
                HUSK_DRAW_HEIGHT
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                HUSK_DRAW_WIDTH,
                HUSK_DRAW_HEIGHT
            );
        }

        batch.setColor(
            Color.WHITE
        );

        batch.end();
    }

    private void drawHuskHornheadDebug() {
        HuskHornhead husk =
            controller.getHuskHornhead();

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        /*
         * Yellow: physical body.
         */
        shapeRenderer.setColor(
            Color.YELLOW
        );

        Rectangle body =
            husk.getBounds();

        shapeRenderer.rect(
            body.x,
            body.y,
            body.width,
            body.height
        );

        /*
         * Green: forward field of vision.
         */
        if (husk.isAlive()) {
            shapeRenderer.setColor(
                Color.GREEN
            );

            Rectangle vision =
                husk.getVisionBounds();

            shapeRenderer.rect(
                vision.x,
                vision.y,
                vision.width,
                vision.height
            );
        }

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void drawCrawlid() {
        Crawlid crawlid =
            controller.getCrawlid();

        TextureRegion frame =
            crawlidAnimationManager.getFrame(
                crawlid.getAnimationType(),
                crawlid.getAnimationTime()
            );

        Rectangle body =
            crawlid.getBounds();

        /*
         * Use one scale for all Crawlid actions.
         * This prevents the differently sized death
         * canvases from being stretched.
         */
        float drawWidth =
            frame.getRegionWidth()
                * CRAWLID_DRAW_SCALE;

        float drawHeight =
            frame.getRegionHeight()
                * CRAWLID_DRAW_SCALE;

        float drawX =
            body.x
                + body.width / 2f
                - drawWidth / 2f;

        float drawY =
            body.y - 2f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        if (crawlid.isFlashing()) {
            batch.setColor(
                1f,
                0.55f,
                0.55f,
                1f
            );
        }

        boolean shouldFlip =
            crawlid.isFacingRight()
                != CRAWLID_SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(
            Color.WHITE
        );

        batch.end();
    }

    private void drawCrawlidDebug() {
        Crawlid crawlid =
            controller.getCrawlid();

        Rectangle body =
            crawlid.getBounds();

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            Color.YELLOW
        );

        shapeRenderer.rect(
            body.x,
            body.y,
            body.width,
            body.height
        );

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void drawKnight() {
        if (
            !controller.shouldDrawPlayer()
        ) {
            return;
        }

        Player player =
            controller.getPlayer();

        TextureRegion frame =
            knightAnimationManager.getFrame(
                player.getAnimationType(),
                player.getAnimationTime()
            );

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        float x =
            player.getPosition().x;

        float y =
            player.getPosition().y;

        boolean shouldFlip =
            player.isFacingRight()
                != KNIGHT_SOURCE_FACES_RIGHT;

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
            worldCamera.getCombined()
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

    private void drawPlayerHud() {
        int currentMasks =
            controller.getCurrentMasks();

        int maximumMasks =
            controller.getMaximumMasks();

        float screenHeight =
            stage
                .getViewport()
                .getWorldHeight();

        float maskStartX = 28f;
        float maskY = screenHeight - 32f;

        float maskSpacing = 31f;
        float maskRadius = 11f;

        float vesselCenterX =
            maskStartX
                + maximumMasks
                * maskSpacing
                + 20f;

        float vesselCenterY = maskY;

        float vesselOuterRadius = 20f;
        float vesselInnerRadius = 15f;

        /*
         * The HUD uses the fixed stage camera.
         */
        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        /*
         * Health masks.
         */
        for (
            int index = 0;
            index < maximumMasks;
            index++
        ) {
            if (index < currentMasks) {
                shapeRenderer.setColor(
                    0.92f,
                    0.94f,
                    1f,
                    1f
                );
            } else {
                shapeRenderer.setColor(
                    0.18f,
                    0.19f,
                    0.24f,
                    1f
                );
            }

            shapeRenderer.circle(
                maskStartX
                    + index * maskSpacing,
                maskY,
                maskRadius
            );
        }

        /*
         * Soul vessel background.
         */
        shapeRenderer.setColor(
            0.10f,
            0.11f,
            0.16f,
            1f
        );

        shapeRenderer.circle(
            vesselCenterX,
            vesselCenterY,
            vesselOuterRadius
        );

        shapeRenderer.setColor(
            0.20f,
            0.22f,
            0.29f,
            1f
        );

        shapeRenderer.circle(
            vesselCenterX,
            vesselCenterY,
            vesselInnerRadius
        );

        /*
         * Soul fill.
         */
        float soulRatio =
            controller.getSoulFillRatio();

        float vesselBottom =
            vesselCenterY
                - vesselInnerRadius;

        float fillTop =
            vesselBottom
                + vesselInnerRadius
                * 2f
                * soulRatio;

        shapeRenderer.setColor(
            0.93f,
            0.95f,
            1f,
            1f
        );

        int rows = 30;

        float rowHeight =
            vesselInnerRadius
                * 2f
                / rows;

        for (
            int row = 0;
            row < rows;
            row++
        ) {
            float rowY =
                vesselBottom
                    + row * rowHeight;

            if (rowY > fillTop) {
                break;
            }

            float relativeY =
                rowY
                    - vesselCenterY
                    + rowHeight / 2f;

            float halfWidth =
                (float) Math.sqrt(
                    Math.max(
                        0f,
                        vesselInnerRadius
                            * vesselInnerRadius
                            - relativeY
                            * relativeY
                    )
                );

            shapeRenderer.rect(
                vesselCenterX
                    - halfWidth,
                rowY,
                halfWidth * 2f,
                rowHeight + 0.5f
            );
        }

        /*
         * Focus progress bar.
         */
        if (controller.isFocusing()) {
            float barWidth = 80f;
            float barHeight = 6f;

            float barX =
                vesselCenterX
                    - barWidth / 2f;

            float barY =
                vesselCenterY
                    - vesselOuterRadius
                    - 13f;

            shapeRenderer.setColor(
                0.15f,
                0.16f,
                0.21f,
                1f
            );

            shapeRenderer.rect(
                barX,
                barY,
                barWidth,
                barHeight
            );

            shapeRenderer.setColor(
                0.90f,
                0.92f,
                1f,
                1f
            );

            shapeRenderer.rect(
                barX,
                barY,
                barWidth
                    * controller
                    .getFocusProgress(),
                barHeight
            );
        }

        shapeRenderer.end();

        /*
         * HUD outlines.
         */
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            0.55f,
            0.58f,
            0.68f,
            1f
        );

        for (
            int index = 0;
            index < maximumMasks;
            index++
        ) {
            shapeRenderer.circle(
                maskStartX
                    + index * maskSpacing,
                maskY,
                maskRadius
            );
        }

        shapeRenderer.circle(
            vesselCenterX,
            vesselCenterY,
            vesselOuterRadius
        );

        shapeRenderer.end();
    }

    private void finishAnimationIfNecessary() {
        Player player =
            controller.getPlayer();

        PlayerAnimationType animationType =
            player.getAnimationType();

        if (
            knightAnimationManager.isLooping(
                animationType
            )
        ) {
            return;
        }

        if (
            knightAnimationManager.isFinished(
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
        if (worldCamera != null) {
            worldCamera.resize(
                width,
                height
            );
        }

        if (stage != null) {
            stage.getViewport().update(
                width,
                height,
                true
            );
        }
    }

    @Override
    public void hide() {
        if (
            stage != null
                && Gdx.input
                .getInputProcessor()
                == stage
        ) {
            Gdx.input.setInputProcessor(
                null
            );
        }
    }

    @Override
    public void dispose() {
        if (
            knightAnimationManager != null
        ) {
            knightAnimationManager.dispose();
        }

        if (
            huskAnimationManager != null
        ) {
            huskAnimationManager.dispose();
        }

        if (
            crawlidAnimationManager != null
        ) {
            crawlidAnimationManager.dispose();
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
