package com.hollowknight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

public final class CrystalGuardianLaserRenderer {

    private static final String ASSET_PATH =
            "sprites/effects/crystal_guardian/" + "laser_sheet.png";

    private static final float REFERENCE_FRAME_RATE = 30f;

    private static final int PULSE_FRAME_COUNT = 8;

    private static final int THIN_FRAME_COUNT = 3;

    private static final float THIN_BEAM_HEIGHT = 18f;

    private static final float THICK_BEAM_HEIGHT = 31f;

    private static final float EMITTER_SIZE = 58f;

    private static final float EMITTER_OVERLAP = 16f;

    private static final float END_FLARE_WIDTH = 78f;

    private static final float END_FLARE_HEIGHT = 94f;

    private final Texture texture;

    private final TextureRegion thinBeamRegion;

    private final TextureRegion thickBeamRegion;

    private final TextureRegion emitterRegion;

    private final TextureRegion endFlareRegion;

    public CrystalGuardianLaserRenderer() {
        texture = new Texture(Gdx.files.internal(ASSET_PATH));

        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        thinBeamRegion = new TextureRegion(texture, 209, 76, 117, 11);

        thickBeamRegion = new TextureRegion(texture, 115, 157, 117, 41);

        emitterRegion = new TextureRegion(texture, 354, 31, 95, 98);

        endFlareRegion = new TextureRegion(texture, 7, 160, 105, 126);
    }

    public void draw(
            SpriteBatch batch,
            Rectangle beamBounds,
            int direction,
            float laserActiveTime,
            Matrix4 projectionMatrix) {
        if (beamBounds == null
                || beamBounds.width <= 0f
                || beamBounds.height <= 0f
                || direction == 0) {
            return;
        }

        boolean thinPhase = isThinPhase(laserActiveTime);

        TextureRegion bodyRegion = thinPhase ? thinBeamRegion : thickBeamRegion;

        float bodyHeight = thinPhase ? THIN_BEAM_HEIGHT : THICK_BEAM_HEIGHT;

        float beamCenterY = beamBounds.y + beamBounds.height / 2f;

        float bodyY = beamCenterY - bodyHeight / 2f;

        float emitterX = direction > 0 ? beamBounds.x : beamBounds.x + beamBounds.width;

        float farEndX = direction > 0 ? beamBounds.x + beamBounds.width : beamBounds.x;

        int oldSourceFunction = batch.getBlendSrcFunc();

        int oldDestinationFunction = batch.getBlendDstFunc();

        batch.setProjectionMatrix(projectionMatrix);

        batch.begin();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        drawBeamBody(batch, bodyRegion, beamBounds, bodyY, bodyHeight, direction);

        drawEndFlare(batch, farEndX, beamCenterY, direction);

        drawEmitter(batch, emitterX, beamCenterY);

        batch.setColor(Color.WHITE);

        batch.setBlendFunction(oldSourceFunction, oldDestinationFunction);

        batch.end();
    }

    public void dispose() {
        texture.dispose();
    }

    private boolean isThinPhase(float activeTime) {
        int referenceFrame = Math.max(0, (int) (activeTime * REFERENCE_FRAME_RATE));

        int pulseFrame = referenceFrame % PULSE_FRAME_COUNT;

        return pulseFrame < THIN_FRAME_COUNT;
    }

    private void drawBeamBody(
            SpriteBatch batch,
            TextureRegion region,
            Rectangle beamBounds,
            float drawY,
            float drawHeight,
            int direction) {
        batch.setColor(1f, 1f, 1f, 0.96f);

        if (direction > 0) {
            batch.draw(region, beamBounds.x, drawY, beamBounds.width, drawHeight);
        } else {

            batch.draw(
                    region, beamBounds.x + beamBounds.width, drawY, -beamBounds.width, drawHeight);
        }
    }

    private void drawEmitter(SpriteBatch batch, float centerX, float centerY) {
        batch.setColor(1f, 1f, 1f, 0.95f);

        batch.draw(
                emitterRegion,
                centerX - EMITTER_SIZE / 2f,
                centerY - EMITTER_SIZE / 2f,
                EMITTER_SIZE,
                EMITTER_SIZE);
    }

    private void drawEndFlare(SpriteBatch batch, float farEndX, float centerY, int direction) {
        batch.setColor(1f, 1f, 1f, 0.90f);

        float drawY = centerY - END_FLARE_HEIGHT / 2f;

        if (direction > 0) {

            batch.draw(
                    endFlareRegion,
                    farEndX - END_FLARE_WIDTH,
                    drawY,
                    END_FLARE_WIDTH,
                    END_FLARE_HEIGHT);
        } else {

            batch.draw(
                    endFlareRegion,
                    farEndX + END_FLARE_WIDTH,
                    drawY,
                    -END_FLARE_WIDTH,
                    END_FLARE_HEIGHT);
        }
    }
}
