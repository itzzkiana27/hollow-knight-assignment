package com.hollowknight.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hollowknight.model.npc.Zote;

public class ZoteAnimationManager implements Disposable {

    private final Array<Texture> idleTextures = new Array<>();

    private final Array<Texture> talkTextures = new Array<>();

    private final Array<Texture> attackTextures = new Array<>();

    private static final float IDLE_FRAME_TIME = 0.16f;

    private static final float TALK_FRAME_TIME = 0.10f;

    private static final float ATTACK_FRAME_TIME = 0.08f;

    public ZoteAnimationManager() {
        loadFrames(idleTextures, "sprites/effects/npc/zote/idle/idle_", 5);

        loadFrames(talkTextures, "sprites/effects/npc/zote/talk/talk_", 5);

        loadFrames(attackTextures, "sprites/effects/npc/zote/attack/attack_", 4);
    }

    public TextureRegion getFrame(Zote zote) {
        if (zote.getState() == Zote.State.TALKING) {
            return getLoopFrame(talkTextures, zote.getAnimationTime(), TALK_FRAME_TIME);
        }

        if (zote.isAngry()) {
            return getLoopFrame(attackTextures, zote.getAnimationTime(), ATTACK_FRAME_TIME);
        }

        return getLoopFrame(idleTextures, zote.getAnimationTime(), IDLE_FRAME_TIME);
    }

    @Override
    public void dispose() {
        disposeTextures(idleTextures);
        disposeTextures(talkTextures);
        disposeTextures(attackTextures);
    }

    private void loadFrames(Array<Texture> textures, String prefix, int count) {
        for (int i = 0; i < count; i++) {
            String number = String.format("%03d", i);

            textures.add(new Texture(Gdx.files.internal(prefix + number + ".png")));
        }
    }

    private TextureRegion getLoopFrame(
            Array<Texture> textures, float animationTime, float frameTime) {
        int index = (int) (animationTime / frameTime) % textures.size;

        return new TextureRegion(textures.get(index));
    }

    private void disposeTextures(Array<Texture> textures) {
        for (Texture texture : textures) {
            texture.dispose();
        }
    }
}
