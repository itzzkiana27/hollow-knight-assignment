package com.hollowknight.model.charm;

public final class CharmEffects {
    private final CharmInventory inventory;

    public CharmEffects(CharmInventory inventory) {
        this.inventory = inventory;
    }

    public int getSoulGain(int baseSoulGain) {
        if (isEquipped(CharmType.SOUL_CATCHER)) {
            return baseSoulGain + 4;
        }

        return baseSoulGain;
    }

    public float getDashCooldown(float baseDashCooldown) {
        if (isEquipped(CharmType.DASHMASTER)) {
            return baseDashCooldown * 0.55f;
        }

        return baseDashCooldown;
    }

    public int getNailDamage(int baseNailDamage) {
        if (isEquipped(CharmType.UNBREAKABLE_STRENGTH)) {
            return Math.max(baseNailDamage + 1, Math.round(baseNailDamage * 1.5f));
        }

        return baseNailDamage;
    }

    public float getAttackCooldown(float baseAttackCooldown) {
        if (isEquipped(CharmType.QUICK_SLASH)) {
            return baseAttackCooldown * 0.55f;
        }

        return baseAttackCooldown;
    }

    public float getFocusDuration(float baseFocusDuration) {
        if (isEquipped(CharmType.QUICK_FOCUS)) {
            return baseFocusDuration * 0.55f;
        }

        return baseFocusDuration;
    }

    public float getKnockbackMultiplier() {
        if (hasHeavyBlow()) {
            return 2.35f;
        }

        return 1f;
    }

    public boolean hasHeavyBlow() {
        return isEquipped(CharmType.HEAVY_BLOW);
    }

    public float getDashLengthMultiplier() {
        if (isEquipped(CharmType.SHARP_SHADOW)) {
            return 1.2f;
        }

        return 1f;
    }

    public boolean hasSharpShadow() {
        return isEquipped(CharmType.SHARP_SHADOW);
    }

    public int getSharpShadowDamage(int baseNailDamage) {
        if (!hasSharpShadow()) {
            return 0;
        }

        return Math.max(1, baseNailDamage);
    }

    public boolean shouldDashIgnoreEnemyContactDamage() {
        return hasSharpShadow();
    }

    public boolean shouldDashDamageEnemies() {
        return hasSharpShadow();
    }

    public boolean shouldUseSharpShadowAnimation() {
        return hasSharpShadow();
    }

    public int getAbilityDamage(int baseAbilityDamage) {
        if (isEquipped(CharmType.VOID_HEART)) {
            return Math.max(baseAbilityDamage + 1, Math.round(baseAbilityDamage * 1.5f));
        }

        return baseAbilityDamage;
    }

    public boolean hasVoidHeart() {
        return isEquipped(CharmType.VOID_HEART);
    }

    public boolean shouldUseVoidAbilityAnimations() {
        return hasVoidHeart();
    }

    public boolean isEquipped(CharmType charm) {
        return inventory != null && inventory.isEquipped(charm);
    }
}
