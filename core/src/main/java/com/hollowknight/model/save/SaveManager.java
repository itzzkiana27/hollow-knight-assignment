package com.hollowknight.model.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class SaveManager {
    public static final int DEFAULT_SLOT = 1;
    public static final int SLOT_COUNT = 4;

    private static final String LEGACY_SAVE_PATH =
        "save/game_save.json";

    private static final String SAVE_SLOT_PATTERN =
        "save/slot_%d.json";

    private final Json json;

    public SaveManager() {
        json = new Json();
        json.setUsePrototypes(false);
        json.setOutputType(
            JsonWriter.OutputType.json
        );
    }

    public void save(GameData data) {
        save(
            data,
            DEFAULT_SLOT
        );
    }

    public void save(
        GameData data,
        int slotNumber
    ) {
        if (data == null) {
            return;
        }

        FileHandle file =
            Gdx.files.local(
                getSavePath(slotNumber)
            );

        file.parent().mkdirs();

        file.writeString(
            json.prettyPrint(data),
            false
        );

        /*
         * Keep the old single-save file in sync for slot 1
         * so older code/builds can still load the latest save.
         */
        if (normalizeSlot(slotNumber) == DEFAULT_SLOT) {
            FileHandle legacyFile =
                Gdx.files.local(
                    LEGACY_SAVE_PATH
                );

            legacyFile.parent().mkdirs();

            legacyFile.writeString(
                json.prettyPrint(data),
                false
            );
        }
    }

    public GameData load() {
        return load(
            DEFAULT_SLOT
        );
    }

    public GameData load(
        int slotNumber
    ) {
        FileHandle file =
            Gdx.files.local(
                getSavePath(slotNumber)
            );

        if (
            !file.exists()
                && normalizeSlot(slotNumber) == DEFAULT_SLOT
        ) {
            file = Gdx.files.local(
                LEGACY_SAVE_PATH
            );
        }

        if (!file.exists()) {
            return null;
        }

        return json.fromJson(
            GameData.class,
            file.readString()
        );
    }

    public boolean hasSave() {
        return hasSave(
            DEFAULT_SLOT
        );
    }

    public boolean hasSave(
        int slotNumber
    ) {
        if (
            Gdx.files
                .local(
                    getSavePath(slotNumber)
                )
                .exists()
        ) {
            return true;
        }

        return normalizeSlot(slotNumber) == DEFAULT_SLOT
            && Gdx.files.local(
            LEGACY_SAVE_PATH
        ).exists();
    }

    public long getSavedAtMillis(
        int slotNumber
    ) {
        FileHandle file =
            Gdx.files.local(
                getSavePath(slotNumber)
            );

        if (
            !file.exists()
                && normalizeSlot(slotNumber) == DEFAULT_SLOT
        ) {
            file = Gdx.files.local(
                LEGACY_SAVE_PATH
            );
        }

        if (!file.exists()) {
            return 0L;
        }

        return file.lastModified();
    }

    public String getSavePath() {
        return getSavePath(
            DEFAULT_SLOT
        );
    }

    public String getSavePath(
        int slotNumber
    ) {
        return String.format(
            SAVE_SLOT_PATTERN,
            normalizeSlot(slotNumber)
        );
    }

    public static int normalizeSlot(
        int slotNumber
    ) {
        if (slotNumber < 1) {
            return DEFAULT_SLOT;
        }

        if (slotNumber > SLOT_COUNT) {
            return SLOT_COUNT;
        }

        return slotNumber;
    }
}
