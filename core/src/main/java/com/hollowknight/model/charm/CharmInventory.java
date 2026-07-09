package com.hollowknight.model.charm;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class CharmInventory {
    private static final int DEFAULT_NOTCH_CAPACITY = 3;

    private final EnumSet<CharmType> ownedCharms;
    private final EnumSet<CharmType> equippedCharms;

    private final int notchCapacity;

    public CharmInventory() {
        this.notchCapacity =
            DEFAULT_NOTCH_CAPACITY;

        this.ownedCharms =
            EnumSet.noneOf(
                CharmType.class
            );

        this.equippedCharms =
            EnumSet.noneOf(
                CharmType.class
            );

        /*
         * All charms are available at the start
         * except Void Heart.
         *
         * Void Heart is unlocked later when the
         * hidden room wall reward is collected.
         */
        for (CharmType charm : CharmType.values()) {
            if (charm != CharmType.VOID_HEART) {
                ownedCharms.add(
                    charm
                );
            }
        }
    }

    public boolean isOwned(
        CharmType charm
    ) {
        return charm != null
            && ownedCharms.contains(
            charm
        );
    }

    public void unlockCharm(
        CharmType charm
    ) {
        if (charm == null) {
            return;
        }

        ownedCharms.add(
            charm
        );
    }

    public void unlockAllCharms() {
        ownedCharms.addAll(
            EnumSet.allOf(
                CharmType.class
            )
        );
    }

    public boolean isEquipped(
        CharmType charm
    ) {
        return charm != null
            && equippedCharms.contains(
            charm
        );
    }

    public boolean toggleCharm(
        CharmType charm
    ) {
        if (charm == null) {
            return false;
        }

        if (isEquipped(charm)) {
            unequipCharm(
                charm
            );

            return true;
        }

        return equipCharm(
            charm
        );
    }

    public boolean equipCharm(
        CharmType charm
    ) {
        if (!canEquip(charm)) {
            return false;
        }

        equippedCharms.add(
            charm
        );

        return true;
    }

    public void unequipCharm(
        CharmType charm
    ) {
        if (charm == null) {
            return;
        }

        equippedCharms.remove(
            charm
        );
    }

    public boolean canEquip(
        CharmType charm
    ) {
        if (charm == null) {
            return false;
        }

        if (!isOwned(charm)) {
            return false;
        }

        if (isEquipped(charm)) {
            return true;
        }

        return getUsedNotches()
            + charm.getNotchCost()
            <= notchCapacity;
    }

    public int getUsedNotches() {
        int usedNotches = 0;

        for (CharmType charm : equippedCharms) {
            usedNotches +=
                charm.getNotchCost();
        }

        return usedNotches;
    }

    public int getRemainingNotches() {
        return notchCapacity
            - getUsedNotches();
    }

    public int getNotchCapacity() {
        return notchCapacity;
    }

    public boolean isFull() {
        return getRemainingNotches() <= 0;
    }

    public Set<CharmType> getOwnedCharms() {
        return Collections.unmodifiableSet(
            ownedCharms
        );
    }

    public Set<CharmType> getEquippedCharms() {
        return Collections.unmodifiableSet(
            equippedCharms
        );
    }

    public CharmType[] getAllCharms() {
        return CharmType.values();
    }
}
