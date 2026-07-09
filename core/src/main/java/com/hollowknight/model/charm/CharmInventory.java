package com.hollowknight.model.charm;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class CharmInventory {
    public static final int DEFAULT_NOTCH_CAPACITY = 3;

    private final EnumSet<CharmType> ownedCharms;
    private final EnumSet<CharmType> equippedCharms;

    private int notchCapacity;

    public CharmInventory() {
        this(DEFAULT_NOTCH_CAPACITY);
    }

    public CharmInventory(
        int notchCapacity
    ) {
        this.notchCapacity = notchCapacity;

        ownedCharms =
            EnumSet.noneOf(
                CharmType.class
            );

        equippedCharms =
            EnumSet.noneOf(
                CharmType.class
            );

        /*
         * For the assignment, all charms are available from
         * the inventory menu immediately.
         */
        unlockAllCharms();
    }

    public void unlockAllCharms() {
        ownedCharms.clear();

        for (CharmType charm : CharmType.values()) {
            ownedCharms.add(charm);
        }
    }

    public void unlockCharm(
        CharmType charm
    ) {
        if (charm == null) {
            return;
        }

        ownedCharms.add(charm);
    }

    public boolean isOwned(
        CharmType charm
    ) {
        return charm != null
            && ownedCharms.contains(charm);
    }

    public boolean isEquipped(
        CharmType charm
    ) {
        return charm != null
            && equippedCharms.contains(charm);
    }

    public boolean toggleCharm(
        CharmType charm
    ) {
        if (charm == null) {
            return false;
        }

        if (isEquipped(charm)) {
            unequipCharm(charm);
            return true;
        }

        return equipCharm(charm);
    }

    public boolean equipCharm(
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

        if (!canEquip(charm)) {
            return false;
        }

        equippedCharms.add(charm);
        return true;
    }

    public void unequipCharm(
        CharmType charm
    ) {
        if (charm == null) {
            return;
        }

        equippedCharms.remove(charm);
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
            usedNotches += charm.getNotchCost();
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

    public void setNotchCapacity(
        int notchCapacity
    ) {
        if (notchCapacity < 0) {
            notchCapacity = 0;
        }

        this.notchCapacity = notchCapacity;

        /*
         * Safety: if capacity is lowered, remove charms
         * until the equipped set becomes valid again.
         */
        while (
            getUsedNotches()
                > this.notchCapacity
                && !equippedCharms.isEmpty()
        ) {
            CharmType lastCharm =
                null;

            for (CharmType charm : equippedCharms) {
                lastCharm = charm;
            }

            if (lastCharm == null) {
                break;
            }

            equippedCharms.remove(lastCharm);
        }
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

    public boolean isFull() {
        return getUsedNotches()
            >= notchCapacity;
    }

    public void clearEquippedCharms() {
        equippedCharms.clear();
    }
}
