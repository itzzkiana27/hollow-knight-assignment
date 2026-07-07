package com.hollowknight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hollowknight.model.world.CrackedWall;

/**
 * Draws the runtime states and falling debris of a Tiled CrackedWall.
 */
public final class CrackedWallRenderer {

    private final Texture initialTexture;
    private final Texture brokenTexture;
    private final Texture debrisTextureA;
    private final Texture debrisTextureB;

    public CrackedWallRenderer(CrackedWall wall) {
        if (wall == null) {
            initialTexture = null;
            brokenTexture = null;
            debrisTextureA = null;
            debrisTextureB = null;
            return;
        }

        initialTexture = loadTexture(
            wall.getInitialSpritePath()
        );

        brokenTexture = loadTexture(
            wall.getBrokenSpritePath()
        );

        debrisTextureA = loadTexture(
            wall.getDebrisSpriteAPath()
        );

        debrisTextureB = loadTexture(
            wall.getDebrisSpriteBPath()
        );
    }

    private Texture loadTexture(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        Texture texture = new Texture(
            Gdx.files.internal(path)
        );

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        return texture;
    }

    public void draw(
        SpriteBatch batch,
        CrackedWall wall
    ) {
        if (wall == null) {
            return;
        }

        if (!wall.isDestroyed()) {
            Texture wallTexture =
                wall.usesBrokenAppearance()
                    ? brokenTexture
                    : initialTexture;

            if (wallTexture != null) {
                batch.draw(
                    wallTexture,
                    wall.getBounds().x
                        + wall.getShakeOffsetX(),
                    wall.getBounds().y,
                    wall.getBounds().width,
                    wall.getBounds().height
                );
            }
        }

        for (
            CrackedWall.DebrisPiece piece
            : wall.getDebrisPieces()
        ) {
            Texture texture =
                piece.getSpriteIndex() == 0
                    ? debrisTextureA
                    : debrisTextureB;

            if (texture == null) {
                continue;
            }

            float width = texture.getWidth();
            float height = texture.getHeight();

            batch.draw(
                texture,
                piece.getPosition().x,
                piece.getPosition().y,
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                piece.getRotation(),
                0,
                0,
                texture.getWidth(),
                texture.getHeight(),
                false,
                false
            );
        }
    }

    public void dispose() {
        disposeTexture(initialTexture);
        disposeTexture(brokenTexture);
        disposeTexture(debrisTextureA);
        disposeTexture(debrisTextureB);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
