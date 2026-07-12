package com.hollowknight.model.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.File;

public class SaveManager {
    public static final int DEFAULT_SLOT = 1;
    public static final int SLOT_COUNT = 4;

    private static final String EXTERNAL_SAVE_DIRECTORY =
        ".hollowknight/save";

    private static final String LEGACY_SAVE_PATH =
        EXTERNAL_SAVE_DIRECTORY + "/game_save.json";

    private static final String SAVE_SLOT_PATTERN =
        EXTERNAL_SAVE_DIRECTORY + "/slot_%d.json";

    private static final String OLD_LOCAL_LEGACY_SAVE_PATH =
        "save/game_save.json";

    private static final String OLD_LOCAL_SAVE_SLOT_PATTERN =
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

        FileHandle file = getSlotFile(slotNumber);

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
            FileHandle legacyFile = getLegacySaveFile();

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
        FileHandle file = getSlotFile(slotNumber);

        if (
            !file.exists()
                && normalizeSlot(slotNumber) == DEFAULT_SLOT
        ) {
            file = getLegacySaveFile();
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
        if (getSlotFile(slotNumber).exists()) {
            return true;
        }

        return normalizeSlot(slotNumber) == DEFAULT_SLOT
            && getLegacySaveFile().exists();
    }

    public long getSavedAtMillis(
        int slotNumber
    ) {
        FileHandle file = getSlotFile(slotNumber);

        if (
            !file.exists()
                && normalizeSlot(slotNumber) == DEFAULT_SLOT
        ) {
            file = getLegacySaveFile();
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

    private FileHandle getSlotFile(
        int slotNumber
    ) {
        int normalizedSlot = normalizeSlot(slotNumber);

        FileHandle externalFile = Gdx.files.external(
            getSavePath(normalizedSlot)
        );

        migrateOldLocalFileIfNecessary(
            externalFile,
            String.format(
                OLD_LOCAL_SAVE_SLOT_PATTERN,
                normalizedSlot
            )
        );

        return externalFile;
    }

    private FileHandle getLegacySaveFile() {
        FileHandle externalFile = Gdx.files.external(
            LEGACY_SAVE_PATH
        );

        migrateOldLocalFileIfNecessary(
            externalFile,
            OLD_LOCAL_LEGACY_SAVE_PATH
        );

        return externalFile;
    }

    private void migrateOldLocalFileIfNecessary(
        FileHandle externalFile,
        String oldRelativePath
    ) {
        if (externalFile.exists()) {
            return;
        }

        FileHandle oldFile = findNewestOldLocalFile(
            oldRelativePath
        );

        if (oldFile == null) {
            return;
        }

        externalFile.parent().mkdirs();
        oldFile.copyTo(externalFile);
    }

    private FileHandle findNewestOldLocalFile(
        String oldRelativePath
    ) {
        File workingDirectory = new File(
            System.getProperty("user.dir", ".")
        );

        File directCandidate = new File(
            workingDirectory,
            oldRelativePath
        );

        File alternateCandidate;

        if ("assets".equals(workingDirectory.getName())) {
            File parent = workingDirectory.getParentFile();
            alternateCandidate = parent == null
                ? null
                : new File(parent, oldRelativePath);
        } else {
            alternateCandidate = new File(
                workingDirectory,
                "assets/" + oldRelativePath
            );
        }

        File newest = newestExistingFile(
            directCandidate,
            alternateCandidate
        );

        return newest == null
            ? null
            : new FileHandle(newest);
    }

    private File newestExistingFile(
        File first,
        File second
    ) {
        boolean firstExists = first != null && first.isFile();
        boolean secondExists = second != null && second.isFile();

        if (!firstExists && !secondExists) {
            return null;
        }

        if (!firstExists) {
            return second;
        }

        if (!secondExists) {
            return first;
        }

        return first.lastModified() >= second.lastModified()
            ? first
            : second;
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
