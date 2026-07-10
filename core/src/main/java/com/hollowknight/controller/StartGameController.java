package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.save.DatabaseSaveManager;
import com.hollowknight.model.save.SaveManager;

public class StartGameController {

    private final HollowKnightGame game;
    private final SaveManager saveManager;
    private final DatabaseSaveManager databaseSaveManager;

    public StartGameController(HollowKnightGame game) {
        this.game = game;
        saveManager = new SaveManager();
        databaseSaveManager =
            new DatabaseSaveManager();
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public String format(String key, Object... arguments) {
        return game.getLocalization().format(key, arguments);
    }

    public String getSaveSlotLabel(
        int slotNumber
    ) {
        int normalizedSlot =
            SaveManager.normalizeSlot(slotNumber);

        if (hasSaveSlot(normalizedSlot)) {
            return format(
                "start.saveSlotSaved",
                normalizedSlot
            );
        }

        return format(
            "start.saveSlot",
            normalizedSlot
        );
    }

    public void startNewGame() {
        game.setActiveSaveSlot(
            SaveManager.DEFAULT_SLOT
        );

        game.showGameScreenForSlot(
            SaveManager.DEFAULT_SLOT
        );
    }

    public void selectSaveSlot(int slotNumber) {
        int normalizedSlot =
            SaveManager.normalizeSlot(slotNumber);

        Gdx.app.log(
            "StartGame",
            "Save slot " + normalizedSlot + " selected"
        );

        game.setActiveSaveSlot(
            normalizedSlot
        );

        if (hasSaveSlot(normalizedSlot)) {
            game.showLoadedGameScreenForSlot(
                normalizedSlot
            );
        } else {
            game.showGameScreenForSlot(
                normalizedSlot
            );
        }
    }

    public void backToMainMenu() {
        game.showMainMenu();
    }

    public boolean hasSaveSlot(
        int slotNumber
    ) {
        return saveManager.hasSave(slotNumber)
            || databaseSaveManager.hasSave(slotNumber);
    }
}
