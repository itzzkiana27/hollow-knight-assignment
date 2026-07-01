package com.hollowknight.model.player;

import com.badlogic.gdx.math.Rectangle;

/**
 * Calculates the Knight's physical body bounds.
 *
 * The complete animation image is much wider than
 * the Knight's actual body, so collision should not
 * use the entire rendered frame.
 */
public final class PlayerBody {

    private static final float BODY_X_RATIO =
        131f / 349f;

    private static final float BODY_Y_RATIO =
        4f / 186f;

    private static final float BODY_WIDTH_RATIO =
        74f / 349f;

    private static final float BODY_HEIGHT_RATIO =
        120f / 186f;

    private final Rectangle bounds;

    public PlayerBody() {
        bounds = new Rectangle();
    }

    public void update(
        Player player,
        float drawWidth,
        float drawHeight
    ) {
        bounds.set(
            player.getPosition().x
                + drawWidth * BODY_X_RATIO,

            player.getPosition().y
                + drawHeight * BODY_Y_RATIO,

            drawWidth * BODY_WIDTH_RATIO,

            drawHeight * BODY_HEIGHT_RATIO
        );
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
