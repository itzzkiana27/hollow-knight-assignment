package com.hollowknight.model.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Optional real SQLite save backend.
 *
 * The normal JSON save files still exist. This class stores the same GameData
 * object inside a real SQLite database file so the project also has a true
 * database-backed save/load path.
 */
public class DatabaseSaveManager {
    private static final String DATABASE_FILE_PATH =
        ".hollowknight/save/hollow_knight_save.db";

    private static final String OLD_LOCAL_DATABASE_FILE_PATH =
        "save/hollow_knight_save.db";

    private static final String LEGACY_SAVE_SLOT_ID =
        "main";

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS game_saves ("
            + "slot_id TEXT PRIMARY KEY, "
            + "game_data_json TEXT NOT NULL, "
            + "saved_at_millis INTEGER NOT NULL"
            + ")";

    private static final String UPSERT_SAVE_SQL =
        "INSERT INTO game_saves "
            + "(slot_id, game_data_json, saved_at_millis) "
            + "VALUES (?, ?, ?) "
            + "ON CONFLICT(slot_id) DO UPDATE SET "
            + "game_data_json = excluded.game_data_json, "
            + "saved_at_millis = excluded.saved_at_millis";

    private static final String LOAD_SAVE_SQL =
        "SELECT game_data_json "
            + "FROM game_saves "
            + "WHERE slot_id = ?";

    private static final String HAS_SAVE_SQL =
        "SELECT 1 "
            + "FROM game_saves "
            + "WHERE slot_id = ? "
            + "LIMIT 1";

    private static final String SAVED_AT_SQL =
        "SELECT saved_at_millis "
            + "FROM game_saves "
            + "WHERE slot_id = ?";

    private static final String COUNT_SAVES_SQL =
        "SELECT COUNT(*) AS save_count "
            + "FROM game_saves";

    private final Json json;
    private final String databaseUrl;

    public DatabaseSaveManager() {
        json = new Json();
        json.setUsePrototypes(false);
        json.setOutputType(
            JsonWriter.OutputType.json
        );

        loadSqliteDriver();

        File databaseFile = prepareExternalDatabaseFile();
        databaseUrl = buildDatabaseUrl(databaseFile);

        createTablesIfNecessary();
    }

    public void save(GameData data) {
        save(
            data,
            SaveManager.DEFAULT_SLOT
        );
    }

    public void save(
        GameData data,
        int slotNumber
    ) {
        if (data == null) {
            return;
        }

        saveToSlotId(
            data,
            getSlotId(slotNumber)
        );

        /*
         * Keep the original SQLite row in sync for slot 1,
         * so old builds that looked for "main" can still load it.
         */
        if (
            SaveManager.normalizeSlot(slotNumber)
                == SaveManager.DEFAULT_SLOT
        ) {
            saveToSlotId(
                data,
                LEGACY_SAVE_SLOT_ID
            );
        }
    }

    public GameData load() {
        return load(
            SaveManager.DEFAULT_SLOT
        );
    }

    public GameData load(
        int slotNumber
    ) {
        GameData data = loadFromSlotId(
            getSlotId(slotNumber)
        );

        if (
            data == null
                && SaveManager.normalizeSlot(slotNumber)
                == SaveManager.DEFAULT_SLOT
        ) {
            data = loadFromSlotId(
                LEGACY_SAVE_SLOT_ID
            );
        }

        return data;
    }

    public boolean hasSave() {
        return hasSave(
            SaveManager.DEFAULT_SLOT
        );
    }

    public boolean hasSave(
        int slotNumber
    ) {
        if (hasSaveSlotId(getSlotId(slotNumber))) {
            return true;
        }

        return SaveManager.normalizeSlot(slotNumber)
            == SaveManager.DEFAULT_SLOT
            && hasSaveSlotId(
            LEGACY_SAVE_SLOT_ID
        );
    }

    public long getSavedAtMillis() {
        return getSavedAtMillis(
            SaveManager.DEFAULT_SLOT
        );
    }

    public long getSavedAtMillis(
        int slotNumber
    ) {
        long savedAtMillis = getSavedAtMillisForSlotId(
            getSlotId(slotNumber)
        );

        if (
            savedAtMillis <= 0L
                && SaveManager.normalizeSlot(slotNumber)
                == SaveManager.DEFAULT_SLOT
        ) {
            savedAtMillis = getSavedAtMillisForSlotId(
                LEGACY_SAVE_SLOT_ID
            );
        }

        return savedAtMillis;
    }

    private void saveToSlotId(
        GameData data,
        String slotId
    ) {
        long savedAtMillis =
            System.currentTimeMillis();

        try (
            Connection connection = openConnection();
            PreparedStatement statement =
                connection.prepareStatement(
                    UPSERT_SAVE_SQL
                )
        ) {
            statement.setString(
                1,
                slotId
            );

            statement.setString(
                2,
                json.toJson(data)
            );

            statement.setLong(
                3,
                savedAtMillis
            );

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not save game to SQLite database.",
                exception
            );
        }
    }

    private GameData loadFromSlotId(
        String slotId
    ) {
        try (
            Connection connection = openConnection();
            PreparedStatement statement =
                connection.prepareStatement(
                    LOAD_SAVE_SQL
                )
        ) {
            statement.setString(
                1,
                slotId
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                String storedJson =
                    resultSet.getString(
                        "game_data_json"
                    );

                if (storedJson == null || storedJson.isEmpty()) {
                    return null;
                }

                return json.fromJson(
                    GameData.class,
                    storedJson
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not load game from SQLite database.",
                exception
            );
        }
    }

    private boolean hasSaveSlotId(
        String slotId
    ) {
        try (
            Connection connection = openConnection();
            PreparedStatement statement =
                connection.prepareStatement(
                    HAS_SAVE_SQL
                )
        ) {
            statement.setString(
                1,
                slotId
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not check SQLite save database.",
                exception
            );
        }
    }

    private long getSavedAtMillisForSlotId(
        String slotId
    ) {
        try (
            Connection connection = openConnection();
            PreparedStatement statement =
                connection.prepareStatement(
                    SAVED_AT_SQL
                )
        ) {
            statement.setString(
                1,
                slotId
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0L;
                }

                return resultSet.getLong(
                    "saved_at_millis"
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not read SQLite save timestamp.",
                exception
            );
        }
    }

    private void createTablesIfNecessary() {
        try (
            Connection connection = openConnection();
            Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(
                CREATE_TABLE_SQL
            );
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not create SQLite save table.",
                exception
            );
        }
    }

    private Connection openConnection()
        throws SQLException {
        return DriverManager.getConnection(
            databaseUrl
        );
    }

    private File prepareExternalDatabaseFile() {
        File externalDatabaseFile =
            Gdx.files
                .external(DATABASE_FILE_PATH)
                .file();

        File parentFolder =
            externalDatabaseFile.getParentFile();

        if (
            parentFolder != null
                && !parentFolder.exists()
        ) {
            parentFolder.mkdirs();
        }

        migrateOldLocalDatabaseIfNecessary(
            externalDatabaseFile
        );

        return externalDatabaseFile;
    }

    private void migrateOldLocalDatabaseIfNecessary(
        File externalDatabaseFile
    ) {
        if (
            externalDatabaseFile.isFile()
                && countSaveRows(externalDatabaseFile) > 0
        ) {
            return;
        }

        File oldDatabaseFile = findBestOldLocalDatabase();

        if (
            oldDatabaseFile == null
                || countSaveRows(oldDatabaseFile) <= 0
        ) {
            return;
        }

        new FileHandle(oldDatabaseFile).copyTo(
            new FileHandle(externalDatabaseFile)
        );
    }

    private File findBestOldLocalDatabase() {
        File workingDirectory = new File(
            System.getProperty("user.dir", ".")
        );

        File directCandidate = new File(
            workingDirectory,
            OLD_LOCAL_DATABASE_FILE_PATH
        );

        File alternateCandidate;

        if ("assets".equals(workingDirectory.getName())) {
            File parent = workingDirectory.getParentFile();
            alternateCandidate = parent == null
                ? null
                : new File(
                    parent,
                    OLD_LOCAL_DATABASE_FILE_PATH
                );
        } else {
            alternateCandidate = new File(
                workingDirectory,
                "assets/" + OLD_LOCAL_DATABASE_FILE_PATH
            );
        }

        return betterDatabaseCandidate(
            directCandidate,
            alternateCandidate
        );
    }

    private File betterDatabaseCandidate(
        File first,
        File second
    ) {
        int firstRows = countSaveRows(first);
        int secondRows = countSaveRows(second);

        if (firstRows <= 0 && secondRows <= 0) {
            return null;
        }

        if (firstRows != secondRows) {
            return firstRows > secondRows
                ? first
                : second;
        }

        long firstModified = first == null
            ? 0L
            : first.lastModified();

        long secondModified = second == null
            ? 0L
            : second.lastModified();

        return firstModified >= secondModified
            ? first
            : second;
    }

    private int countSaveRows(File databaseFile) {
        if (databaseFile == null || !databaseFile.isFile()) {
            return 0;
        }

        String candidateUrl = buildDatabaseUrl(
            databaseFile
        );

        try (
            Connection connection = DriverManager.getConnection(
                candidateUrl
            );
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                COUNT_SAVES_SQL
            )
        ) {
            return resultSet.next()
                ? resultSet.getInt("save_count")
                : 0;
        } catch (SQLException exception) {
            return 0;
        }
    }

    private String buildDatabaseUrl(
        File databaseFile
    ) {
        return "jdbc:sqlite:"
            + databaseFile.getAbsolutePath();
    }

    private String getSlotId(
        int slotNumber
    ) {
        return "slot_"
            + SaveManager.normalizeSlot(slotNumber);
    }

    private void loadSqliteDriver() {
        try {
            Class.forName(
                "org.sqlite.JDBC"
            );
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                "SQLite JDBC driver was not found. "
                    + "Make sure sqlite-jdbc is added to core/build.gradle.",
                exception
            );
        }
    }
}
