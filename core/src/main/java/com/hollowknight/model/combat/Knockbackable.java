package com.hollowknight.model.combat;

import com.hollowknight.model.world.PlatformWorld;

public interface Knockbackable {
    void applyKnockback(
        int direction,
        PlatformWorld platformWorld
    );
}
