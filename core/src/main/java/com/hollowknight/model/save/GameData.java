package com.hollowknight.model.save;

import java.util.ArrayList;
import java.util.List;

public class GameData {
    public String currentRoomId = "forgotten_crossroads";
    public float playerX = 0f;
    public float playerY = 0f;

    public int currentMasks = 5;
    public int currentSoul = 0;

    public boolean crackedWallDestroyed = false;
    public boolean falseKnightDefeated = false;
    public boolean godModeEnabled = false;
    public boolean noclipModeEnabled = false;

    public float elapsedGameSeconds = 0f;
    public int deathCount = 0;
    public int totalEnemiesKilled = 0;

    public List<String> ownedCharms = new ArrayList<>();

    public List<String> equippedCharms = new ArrayList<>();

    public List<String> unlockedAchievements = new ArrayList<>();

    public List<String> killedEnemyTypes = new ArrayList<>();

    public GameData() {}
}
