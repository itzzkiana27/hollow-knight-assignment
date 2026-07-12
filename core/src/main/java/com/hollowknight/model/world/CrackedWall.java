package com.hollowknight.model.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public final class CrackedWall {

    public enum HitResult {
        DAMAGED,
        BROKEN_APPEARANCE,
        DESTROYED
    }

    private static final float DEBRIS_GRAVITY = -900f;

    private static final float DEBRIS_LIFETIME = 1.25f;

    private static final float SHAKE_DURATION = 0.16f;

    public static final class DebrisPiece {
        private final int spriteIndex;

        private final Vector2 position;

        private final Vector2 velocity;

        private final float angularVelocity;

        private float rotation;

        private float age;

        private DebrisPiece(
                int spriteIndex,
                float x,
                float y,
                float velocityX,
                float velocityY,
                float rotation,
                float angularVelocity) {
            this.spriteIndex = spriteIndex;
            this.position = new Vector2(x, y);
            this.velocity = new Vector2(velocityX, velocityY);
            this.rotation = rotation;
            this.angularVelocity = angularVelocity;
            this.age = 0f;
        }

        public int getSpriteIndex() {
            return spriteIndex;
        }

        public Vector2 getPosition() {
            return position;
        }

        public float getRotation() {
            return rotation;
        }

        private void update(float delta) {
            age += delta;
            velocity.y += DEBRIS_GRAVITY * delta;
            position.mulAdd(velocity, delta);
            rotation += angularVelocity * delta;
        }

        private boolean isExpired() {
            return age >= DEBRIS_LIFETIME;
        }
    }

    private final String id;

    private final String hiddenRoomId;

    private final Rectangle bounds;

    private final int maxHits;

    private final int brokenAppearanceHit;

    private final int destroyHit;

    private final String initialSpritePath;

    private final String brokenSpritePath;

    private final String debrisSpriteAPath;

    private final String debrisSpriteBPath;

    private final boolean persistent;

    private final boolean revealsRoom;

    private final Array<DebrisPiece> debrisPieces;

    private int hitCount;

    private float shakeTimeRemaining;

    public CrackedWall(
            String id,
            String hiddenRoomId,
            Rectangle bounds,
            int maxHits,
            int brokenAppearanceHit,
            int destroyHit,
            String initialSpritePath,
            String brokenSpritePath,
            String debrisSpriteAPath,
            String debrisSpriteBPath,
            boolean persistent,
            boolean revealsRoom) {
        this.id = id;
        this.hiddenRoomId = hiddenRoomId;
        this.bounds = new Rectangle(bounds);
        this.maxHits = Math.max(1, maxHits);
        this.brokenAppearanceHit = Math.max(1, brokenAppearanceHit);
        this.destroyHit = Math.max(this.brokenAppearanceHit, destroyHit);
        this.initialSpritePath = initialSpritePath;
        this.brokenSpritePath = brokenSpritePath;
        this.debrisSpriteAPath = debrisSpriteAPath;
        this.debrisSpriteBPath = debrisSpriteBPath;
        this.persistent = persistent;
        this.revealsRoom = revealsRoom;

        debrisPieces = new Array<>();
        hitCount = 0;
        shakeTimeRemaining = 0f;
    }

    public HitResult hit() {
        if (isDestroyed()) {
            return HitResult.DESTROYED;
        }

        hitCount = Math.min(maxHits, hitCount + 1);

        shakeTimeRemaining = SHAKE_DURATION;

        if (hitCount >= brokenAppearanceHit) {
            spawnDebris();
        }

        if (hitCount >= destroyHit) {
            return HitResult.DESTROYED;
        }

        if (hitCount >= brokenAppearanceHit) {
            return HitResult.BROKEN_APPEARANCE;
        }

        return HitResult.DAMAGED;
    }

    public void update(float delta) {
        float safeDelta = Math.min(Math.max(delta, 0f), 1f / 30f);

        shakeTimeRemaining = Math.max(0f, shakeTimeRemaining - safeDelta);

        for (int index = debrisPieces.size - 1; index >= 0; index--) {
            DebrisPiece piece = debrisPieces.get(index);

            piece.update(safeDelta);

            if (piece.isExpired()) {
                debrisPieces.removeIndex(index);
            }
        }
    }

    public boolean usesBrokenAppearance() {
        return !isDestroyed() && hitCount >= brokenAppearanceHit;
    }

    public float getShakeOffsetX() {
        if (shakeTimeRemaining <= 0f || isDestroyed()) {
            return 0f;
        }

        return (float) Math.sin(shakeTimeRemaining * 145f) * 3f;
    }

    public boolean revealsRoom() {
        return revealsRoom;
    }

    public String getId() {
        return id;
    }

    public String getHiddenRoomId() {
        return hiddenRoomId;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getHitCount() {
        return hitCount;
    }

    public boolean isDestroyed() {
        return hitCount >= destroyHit;
    }

    public String getInitialSpritePath() {
        return initialSpritePath;
    }

    public String getBrokenSpritePath() {
        return brokenSpritePath;
    }

    public String getDebrisSpriteAPath() {
        return debrisSpriteAPath;
    }

    public String getDebrisSpriteBPath() {
        return debrisSpriteBPath;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public Array<DebrisPiece> getDebrisPieces() {
        return debrisPieces;
    }

    private void spawnDebris() {
        float centerX = bounds.x + bounds.width * 0.55f;

        float centerY = bounds.y + bounds.height * 0.55f;

        debrisPieces.add(new DebrisPiece(0, centerX - 18f, centerY + 14f, -105f, 175f, -8f, -180f));

        debrisPieces.add(new DebrisPiece(1, centerX + 8f, centerY - 3f, 95f, 145f, 10f, 210f));
    }
}
