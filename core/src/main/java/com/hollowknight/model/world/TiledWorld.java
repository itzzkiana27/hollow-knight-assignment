package com.hollowknight.model.world;

import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hollowknight.model.combat.SpikeHazard;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads world.tmx and converts its object layers into runtime data.
 *
 * TmxMapLoader uses a y-up coordinate system by default, which matches
 * the movement and camera code in this project.
 */
public final class TiledWorld implements Disposable {

    public static final String DEFAULT_MAP_PATH =
        "maps/world.tmx";

    public static final class EnemySpawn {
        private final String id;
        private final String enemyType;
        private final String roomId;
        private final float x;
        private final float y;
        private final boolean facingRight;
        private final float respawnDistance;
        private final boolean respawnOnRoomEntry;

        private EnemySpawn(
            String id,
            String enemyType,
            String roomId,
            float x,
            float y,
            boolean facingRight,
            float respawnDistance,
            boolean respawnOnRoomEntry
        ) {
            this.id = id;
            this.enemyType = enemyType;
            this.roomId = roomId;
            this.x = x;
            this.y = y;
            this.facingRight = facingRight;
            this.respawnDistance = respawnDistance;
            this.respawnOnRoomEntry =
                respawnOnRoomEntry;
        }

        public String getId() {
            return id;
        }

        public String getEnemyType() {
            return enemyType;
        }

        public String getRoomId() {
            return roomId;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public boolean isFacingRight() {
            return facingRight;
        }

        public float getRespawnDistance() {
            return respawnDistance;
        }

        public boolean isRespawnOnRoomEntry() {
            return respawnOnRoomEntry;
        }
    }

    public static final class BossSpawn {
        private final String id;
        private final String bossType;
        private final String roomId;
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        private BossSpawn(
            String id,
            String bossType,
            String roomId,
            float x,
            float y,
            float width,
            float height
        ) {
            this.id = id;
            this.bossType = bossType;
            this.roomId = roomId;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public String getId() {
            return id;
        }

        public String getBossType() {
            return bossType;
        }

        public String getRoomId() {
            return roomId;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }
    }

    public static final class NpcSpawn {
        private final String id;
        private final String npcType;
        private final String roomId;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final boolean facingRight;
        private final float interactionRadius;
        private final float moveMinX;
        private final float moveMaxX;

        private NpcSpawn(
            String id,
            String npcType,
            String roomId,
            float x,
            float y,
            float width,
            float height,
            boolean facingRight,
            float interactionRadius,
            float moveMinX,
            float moveMaxX
        ) {
            this.id = id;
            this.npcType = npcType;
            this.roomId = roomId;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.facingRight = facingRight;
            this.interactionRadius = interactionRadius;
            this.moveMinX = moveMinX;
            this.moveMaxX = moveMaxX;
        }

        public String getId() {
            return id;
        }

        public String getNpcType() {
            return npcType;
        }

        public String getRoomId() {
            return roomId;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        public boolean isFacingRight() {
            return facingRight;
        }

        public float getInteractionRadius() {
            return interactionRadius;
        }
        public float getMoveMinX() {
            return moveMinX;
        }

        public float getMoveMaxX() {
            return moveMaxX;
        }
    }

    public static final class RoomTransition {
        private final String id;
        private final String fromRoom;
        private final String targetRoom;
        private final String targetSpawn;
        private final Rectangle bounds;

        private RoomTransition(
            String id,
            String fromRoom,
            String targetRoom,
            String targetSpawn,
            Rectangle bounds
        ) {
            this.id = id;
            this.fromRoom = fromRoom;
            this.targetRoom = targetRoom;
            this.targetSpawn = targetSpawn;
            this.bounds = new Rectangle(bounds);
        }

        public String getId() {
            return id;
        }

        public String getFromRoom() {
            return fromRoom;
        }

        public String getTargetRoom() {
            return targetRoom;
        }

        public String getTargetSpawn() {
            return targetSpawn;
        }

        public Rectangle getBounds() {
            return bounds;
        }
    }

    private final TiledMap tiledMap;

    private final float mapWidth;
    private final float mapHeight;

    private final Map<String, Rectangle> roomBoundsById;
    private final Map<String, Vector2> playerSpawnsById;
    private final Map<String, String> playerSpawnRoomsById;
    private final Map<String, Rectangle> cameraBoundsByRoomId;
    private final Array<RoomTransition> roomTransitions;

    private final Rectangle crossroadsBounds;
    private final Rectangle hiddenRoomBounds;
    private final Rectangle cityTransitionBounds;

    private final Vector2 crossroadsStart;
    private final Vector2 crossroadsReturnFromCity;

    private final Array<Platform> collisionPlatforms;
    private final Array<SpikeHazard> spikeHazards;
    private final Array<EnemySpawn> enemySpawns;
    private final Array<NpcSpawn> npcSpawns;
    private final Array<BossSpawn> bossSpawns;

    private final CrackedWall crackedWall;

    private final int[] backgroundLayerIndices;
    private final int[] foregroundLayerIndices;

    public TiledWorld() {
        this(DEFAULT_MAP_PATH);
    }

    public TiledWorld(String mapPath) {
        tiledMap = new TmxMapLoader().load(mapPath);

        MapProperties mapProperties =
            tiledMap.getProperties();

        int mapTileWidth = getInt(
            mapProperties,
            "width",
            0
        );

        int mapTileHeight = getInt(
            mapProperties,
            "height",
            0
        );

        int tileWidth = getInt(
            mapProperties,
            "tilewidth",
            1
        );

        int tileHeight = getInt(
            mapProperties,
            "tileheight",
            1
        );

        mapWidth = mapTileWidth * tileWidth;
        mapHeight = mapTileHeight * tileHeight;

        roomBoundsById = loadRoomBounds();
        playerSpawnsById = new HashMap<>();
        playerSpawnRoomsById = new HashMap<>();
        loadPlayerSpawns();
        cameraBoundsByRoomId = loadCameraBounds();
        roomTransitions = loadRoomTransitions();

        crossroadsBounds = requireRectangle(
            "Room",
            "forgotten_crossroads"
        );

        hiddenRoomBounds = findRectangle(
            "Room",
            "crossroads_secret_01"
        );

        cityTransitionBounds = findRectangle(
            "Transitions",
            "crossroads_to_city"
        );

        Vector2 startObject = requirePlayerSpawn(
            "crossroads_start"
        );

        Vector2 returnObject = requirePlayerSpawn(
            "crossroads_return_from_city"
        );

        crossroadsStart = new Vector2(startObject);

        crossroadsReturnFromCity = new Vector2(
            returnObject
        );

        collisionPlatforms = new Array<>();
        crackedWall = loadCollisionObjects();

        spikeHazards = loadHazards();
        enemySpawns = loadEnemySpawns();
        npcSpawns = loadNpcSpawns();
        bossSpawns = loadBossSpawns();

        backgroundLayerIndices =
            collectTopLevelLayerIndices(
                "Crossroads_Background",
                "Background",
                "City_Skyline",
                "Arena",
                "Terrain_Back",
                "Terrain"
            );

        foregroundLayerIndices =
            collectTopLevelLayerIndices(
                "Foreground"
            );
    }

    private Map<String, Rectangle> loadRoomBounds() {
        Map<String, Rectangle> result = new HashMap<>();

        MapLayer roomLayer = requireLayer("Room");

        for (MapObject object : roomLayer.getObjects()) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            Rectangle rectangle = new Rectangle(
                ((RectangleMapObject) object)
                    .getRectangle()
            );

            /*
             * Room bounds must be keyed by the Room object name, not by
             * the roomId property. The hidden room object uses
             * roomId = forgotten_crossroads to say it belongs to that room;
             * if we use that property here, it overwrites the real
             * forgotten_crossroads bounds and the camera clamps to the
             * hidden room instead.
             */
            String roomId = object.getName();

            if (roomId == null || roomId.isBlank()) {
                roomId = getString(
                    object.getProperties(),
                    "roomId",
                    ""
                );
            }

            if (roomId == null || roomId.isBlank()) {
                continue;
            }

            result.put(roomId, rectangle);
        }

        return result;
    }

    private void loadPlayerSpawns() {
        MapLayer spawnLayer = requireLayer("Spawns");

        for (MapObject object : spawnLayer.getObjects()) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            String objectType = getString(
                object.getProperties(),
                "type",
                ""
            );

            if (!"PlayerSpawn".equals(objectType)) {
                continue;
            }

            Rectangle rectangle = new Rectangle(
                ((RectangleMapObject) object)
                    .getRectangle()
            );

            String spawnId = getString(
                object.getProperties(),
                "spawnId",
                object.getName()
            );

            if (spawnId == null || spawnId.isBlank()) {
                continue;
            }

            playerSpawnsById.put(
                spawnId,
                new Vector2(
                    rectangle.x,
                    rectangle.y
                )
            );

            playerSpawnRoomsById.put(
                spawnId,
                getString(
                    object.getProperties(),
                    "roomId",
                    ""
                )
            );
        }
    }

    private Map<String, Rectangle> loadCameraBounds() {
        Map<String, Rectangle> result = new HashMap<>();

        MapLayer cameraLayer =
            tiledMap.getLayers().get("CameraBounds");

        if (cameraLayer == null) {
            return result;
        }

        for (MapObject object : cameraLayer.getObjects()) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            String roomId = getString(
                object.getProperties(),
                "roomId",
                ""
            );

            if (roomId == null || roomId.isBlank()) {
                continue;
            }

            result.put(
                roomId,
                new Rectangle(
                    ((RectangleMapObject) object)
                        .getRectangle()
                )
            );
        }

        return result;
    }

    private Array<RoomTransition> loadRoomTransitions() {
        Array<RoomTransition> result = new Array<>();

        MapLayer transitionsLayer =
            requireLayer("Transitions");

        for (MapObject object : transitionsLayer.getObjects()) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            String objectType = getString(
                object.getProperties(),
                "type",
                ""
            );

            if (!"RoomTransition".equals(objectType)) {
                continue;
            }

            result.add(
                new RoomTransition(
                    object.getName(),
                    getString(
                        object.getProperties(),
                        "fromRoom",
                        ""
                    ),
                    getString(
                        object.getProperties(),
                        "targetRoom",
                        ""
                    ),
                    getString(
                        object.getProperties(),
                        "targetSpawn",
                        ""
                    ),
                    new Rectangle(
                        ((RectangleMapObject) object)
                            .getRectangle()
                    )
                )
            );
        }

        return result;
    }

    private CrackedWall loadCollisionObjects() {
        MapLayer collisionLayer =
            requireLayer("Collision");

        CrackedWall result = null;

        for (
            MapObject object
            : collisionLayer.getObjects()
        ) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            Rectangle rectangle = new Rectangle(
                ((RectangleMapObject) object)
                    .getRectangle()
            );

            String objectType = getString(
                object.getProperties(),
                "type",
                ""
            );

            if (
                "CrackedWall".equals(objectType)
                    || "crossroads_cracked_wall_01"
                    .equals(object.getName())
            ) {
                result = new CrackedWall(
                    object.getName(),
                    getString(
                        object.getProperties(),
                        "hiddenRoomId",
                        ""
                    ),
                    rectangle,
                    getInt(
                        object.getProperties(),
                        "maxHits",
                        3
                    ),
                    getInt(
                        object.getProperties(),
                        "brokenAppearanceHit",
                        2
                    ),
                    getInt(
                        object.getProperties(),
                        "destroyHit",
                        3
                    ),
                    getString(
                        object.getProperties(),
                        "initialSprite",
                        ""
                    ),
                    getString(
                        object.getProperties(),
                        "brokenSprite",
                        ""
                    ),
                    getString(
                        object.getProperties(),
                        "debrisSpriteA",
                        ""
                    ),
                    getString(
                        object.getProperties(),
                        "debrisSpriteB",
                        ""
                    ),
                    getBoolean(
                        object.getProperties(),
                        "persistent",
                        true
                    ),
                    getBoolean(
                        object.getProperties(),
                        "revealsRoom",
                        true
                    )
                );

                continue;
            }

            collisionPlatforms.add(
                new Platform(
                    rectangle.x,
                    rectangle.y,
                    rectangle.width,
                    rectangle.height
                )
            );
        }

        return result;
    }

    private Array<SpikeHazard> loadHazards() {
        Array<SpikeHazard> result =
            new Array<>();

        MapLayer hazardsLayer =
            requireLayer("Hazards");

        for (
            MapObject object
            : hazardsLayer.getObjects()
        ) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            String hazardType = getString(
                object.getProperties(),
                "hazardType",
                ""
            );

            if (!"SPIKES".equals(hazardType)) {
                continue;
            }

            Rectangle bounds = new Rectangle(
                ((RectangleMapObject) object)
                    .getRectangle()
            );

            result.add(
                new SpikeHazard(
                    object.getName(),
                    bounds,
                    getInt(
                        object.getProperties(),
                        "damage",
                        1
                    ),
                    getBoolean(
                        object.getProperties(),
                        "pogoable",
                        true
                    )
                )
            );
        }

        return result;
    }

    private Array<EnemySpawn> loadEnemySpawns() {
        Array<EnemySpawn> result =
            new Array<>();

        MapLayer enemyLayer =
            requireLayer("Enemies");

        for (
            MapObject object
            : enemyLayer.getObjects()
        ) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            Rectangle rectangle =
                ((RectangleMapObject) object)
                    .getRectangle();

            String roomId = getString(
                object.getProperties(),
                "roomId",
                ""
            );

            result.add(
                new EnemySpawn(
                    object.getName(),
                    getString(
                        object.getProperties(),
                        "enemyType",
                        ""
                    ),
                    roomId,
                    rectangle.x,
                    rectangle.y,
                    getBoolean(
                        object.getProperties(),
                        "facingRight",
                        false
                    ),
                    getFloat(
                        object.getProperties(),
                        "respawnDistance",
                        900f
                    ),
                    getBoolean(
                        object.getProperties(),
                        "respawnOnRoomEntry",
                        true
                    )
                )
            );
        }

        return result;
    }

    private Array<NpcSpawn> loadNpcSpawns() {
        Array<NpcSpawn> result =
            new Array<>();

        MapLayer npcLayer =
            tiledMap.getLayers().get("NPCs");

        if (npcLayer == null) {
            return result;
        }

        for (
            MapObject object
            : npcLayer.getObjects()
        ) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            Rectangle rectangle =
                ((RectangleMapObject) object)
                    .getRectangle();

            String objectType = getString(
                object.getProperties(),
                "type",
                ""
            );

            if (!"NPC".equals(objectType)) {
                continue;
            }

            String npcType = getString(
                object.getProperties(),
                "npcType",
                ""
            );

            if (npcType == null || npcType.isBlank()) {
                continue;
            }

            result.add(
                new NpcSpawn(
                    object.getName(),
                    npcType,
                    getString(
                        object.getProperties(),
                        "roomId",
                        ""
                    ),
                    rectangle.x,
                    rectangle.y,
                    rectangle.width,
                    rectangle.height,
                    getBoolean(
                        object.getProperties(),
                        "facingRight",
                        false
                    ),
                    getFloat(
                        object.getProperties(),
                        "interactionRadius",
                        140f
                    ),
                    getFloat(
                        object.getProperties(),
                        "moveMinX",
                        rectangle.x - 220f
                    ),
                    getFloat(
                        object.getProperties(),
                        "moveMaxX",
                        rectangle.x + 520f
                    )
                )
            );
        }

        return result;
    }

    private Array<BossSpawn> loadBossSpawns() {
        Array<BossSpawn> result =
            new Array<>();

        MapLayer bossLayer =
            tiledMap.getLayers().get("Bosses");

        if (bossLayer == null) {
            return result;
        }

        for (
            MapObject object
            : bossLayer.getObjects()
        ) {
            if (
                !(object
                    instanceof RectangleMapObject)
            ) {
                continue;
            }

            Rectangle rectangle =
                ((RectangleMapObject) object)
                    .getRectangle();

            String bossType =
                getString(
                    object.getProperties(),
                    "bossType",
                    ""
                );

            if (
                bossType == null
                    || bossType.isBlank()
            ) {
                continue;
            }

            result.add(
                new BossSpawn(
                    object.getName(),
                    bossType,
                    getString(
                        object.getProperties(),
                        "roomId",
                        ""
                    ),
                    rectangle.x,
                    rectangle.y,
                    rectangle.width,
                    rectangle.height
                )
            );
        }

        return result;
    }


    private static boolean touchesRoom(
        Rectangle objectBounds,
        Rectangle roomBounds
    ) {
        return objectBounds.x
            < roomBounds.x + roomBounds.width
            && objectBounds.x + objectBounds.width
            > roomBounds.x
            && objectBounds.y
            < roomBounds.y + roomBounds.height
            && objectBounds.y + objectBounds.height
            > roomBounds.y;
    }

    private Rectangle requireRectangle(
        String layerName,
        String objectName
    ) {
        Rectangle rectangle = findRectangle(
            layerName,
            objectName
        );

        if (rectangle == null) {
            throw new IllegalStateException(
                "Missing rectangle object '"
                    + objectName
                    + "' on Tiled layer '"
                    + layerName
                    + "'."
            );
        }

        return rectangle;
    }

    private Rectangle findRectangle(
        String layerName,
        String objectName
    ) {
        MapLayer layer = requireLayer(layerName);

        for (MapObject object : layer.getObjects()) {
            if (
                objectName.equals(object.getName())
                    && object
                    instanceof RectangleMapObject
            ) {
                return new Rectangle(
                    ((RectangleMapObject) object)
                        .getRectangle()
                );
            }
        }

        return null;
    }

    private MapLayer requireLayer(String name) {
        MapLayer layer =
            tiledMap.getLayers().get(name);

        if (layer == null) {
            throw new IllegalStateException(
                "Missing Tiled layer: " + name
            );
        }

        return layer;
    }

    private int[] collectTopLevelLayerIndices(
        String... names
    ) {
        /*
         * OrthogonalTiledMapRenderer.render(int[]) accepts only
         * top-level layer indices. Some of our important visual
         * layers, such as City_Skyline and Arena, are image layers
         * inside the Crossroads_Background group.
         *
         * So this method searches recursively through group layers.
         * When a requested layer is found inside a group, it returns
         * the top-level parent index for that group. Rendering the
         * group lets LibGDX draw the nested image layers correctly.
         */
        MapLayers topLevelLayers = tiledMap.getLayers();
        int[] foundIndices = new int[
            topLevelLayers.getCount()
        ];
        int foundCount = 0;

        for (
            int layerIndex = 0;
            layerIndex < topLevelLayers.getCount();
            layerIndex++
        ) {
            MapLayer layer = topLevelLayers.get(
                layerIndex
            );

            if (layerOrChildMatches(layer, names)) {
                boolean alreadyAdded = false;

                for (int i = 0; i < foundCount; i++) {
                    if (foundIndices[i] == layerIndex) {
                        alreadyAdded = true;
                        break;
                    }
                }

                if (!alreadyAdded) {
                    foundIndices[foundCount] =
                        layerIndex;
                    foundCount++;
                }
            }
        }

        int[] indices = new int[foundCount];
        System.arraycopy(
            foundIndices,
            0,
            indices,
            0,
            foundCount
        );

        return indices;
    }

    private boolean layerOrChildMatches(
        MapLayer layer,
        String... names
    ) {
        if (matchesAnyName(layer.getName(), names)) {
            return true;
        }

        if (layer instanceof MapGroupLayer) {
            MapLayers childLayers =
                ((MapGroupLayer) layer).getLayers();

            for (
                int childIndex = 0;
                childIndex < childLayers.getCount();
                childIndex++
            ) {
                if (
                    layerOrChildMatches(
                        childLayers.get(childIndex),
                        names
                    )
                ) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchesAnyName(
        String layerName,
        String... names
    ) {
        if (layerName == null) {
            return false;
        }

        for (String name : names) {
            if (layerName.equals(name)) {
                return true;
            }
        }

        return false;
    }

    public EnemySpawn findEnemySpawn(
        String enemyType
    ) {
        for (EnemySpawn spawn : enemySpawns) {
            if (
                enemyType.equals(
                    spawn.getEnemyType()
                )
            ) {
                return spawn;
            }
        }

        return null;
    }

    public EnemySpawn findEnemySpawn(
        String enemyType,
        String roomId
    ) {
        for (EnemySpawn spawn : enemySpawns) {
            if (
                enemyType.equals(
                    spawn.getEnemyType()
                )
                    && roomId.equals(
                    spawn.getRoomId()
                )
            ) {
                return spawn;
            }
        }

        return null;
    }

    public BossSpawn findBossSpawn(
        String bossType,
        String roomId
    ) {
        for (BossSpawn spawn : bossSpawns) {
            if (
                bossType.equals(spawn.getBossType())
                    && roomId.equals(spawn.getRoomId())
            ) {
                return spawn;
            }
        }

        return null;
    }

    public Array<BossSpawn> getBossSpawns() {
        return bossSpawns;
    }

    public NpcSpawn findNpcSpawn(
        String npcType,
        String roomId
    ) {
        for (NpcSpawn spawn : npcSpawns) {
            if (
                npcType.equals(spawn.getNpcType())
                    && roomId.equals(spawn.getRoomId())
            ) {
                return spawn;
            }
        }

        return null;
    }

    public Array<NpcSpawn> getNpcSpawns() {
        return npcSpawns;
    }

    /**
     * Finds the logical room containing a world-space point.
     *
     * Some maps contain smaller helper/secret room rectangles inside a main
     * room rectangle. In that case, prefer the largest containing rectangle
     * so the result remains the main gameplay room used by transitions,
     * camera bounds, enemies, and music.
     *
     * @return the containing room id, or {@code null} when the point is not
     * inside any declared room
     */
    public String findRoomIdAt(
        float worldX,
        float worldY
    ) {
        String containingRoomId = null;
        float containingRoomArea = -1f;

        for (
            Map.Entry<String, Rectangle> entry
            : roomBoundsById.entrySet()
        ) {
            Rectangle bounds = entry.getValue();

            if (!bounds.contains(worldX, worldY)) {
                continue;
            }

            float area = bounds.width * bounds.height;

            if (area > containingRoomArea) {
                containingRoomId = entry.getKey();
                containingRoomArea = area;
            }
        }

        return containingRoomId;
    }

    public Rectangle getRoomBounds(
        String roomId
    ) {
        Rectangle bounds =
            roomBoundsById.get(roomId);

        if (bounds == null) {
            throw new IllegalArgumentException(
                "Unknown roomId: " + roomId
            );
        }

        return new Rectangle(bounds);
    }

    public Rectangle getCameraBoundsForRoom(
        String roomId
    ) {
        Rectangle bounds =
            cameraBoundsByRoomId.get(roomId);

        if (bounds == null) {
            return getRoomBounds(roomId);
        }

        return new Rectangle(bounds);
    }

    public Vector2 getPlayerSpawn(
        String spawnId
    ) {
        Vector2 spawn =
            playerSpawnsById.get(spawnId);

        return spawn == null
            ? null
            : new Vector2(spawn);
    }

    private Vector2 requirePlayerSpawn(
        String spawnId
    ) {
        Vector2 spawn = getPlayerSpawn(spawnId);

        if (spawn == null) {
            throw new IllegalStateException(
                "Missing player spawn: "
                    + spawnId
            );
        }

        return spawn;
    }

    public Array<Platform> getCollisionPlatformsForRoom(
        String roomId
    ) {
        Rectangle roomBounds = getRoomBounds(roomId);
        Array<Platform> result = new Array<>();

        for (Platform platform : collisionPlatforms) {
            if (
                touchesRoom(
                    platform.getBounds(),
                    roomBounds
                )
            ) {
                result.add(platform);
            }
        }

        return result;
    }

    public Array<SpikeHazard> getSpikeHazardsForRoom(
        String roomId
    ) {
        Rectangle roomBounds = getRoomBounds(roomId);
        Array<SpikeHazard> result = new Array<>();

        for (SpikeHazard hazard : spikeHazards) {
            if (
                touchesRoom(
                    hazard.getBounds(),
                    roomBounds
                )
            ) {
                result.add(hazard);
            }
        }

        return result;
    }

    private static Object getRaw(
        MapProperties properties,
        String name
    ) {
        return properties.get(name);
    }

    private static String getString(
        MapProperties properties,
        String name,
        String fallback
    ) {
        Object value = getRaw(properties, name);
        return value == null
            ? fallback
            : String.valueOf(value);
    }

    private static int getInt(
        MapProperties properties,
        String name,
        int fallback
    ) {
        Object value = getRaw(properties, name);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value != null) {
            try {
                return Integer.parseInt(
                    String.valueOf(value)
                );
            } catch (NumberFormatException ignored) {
                // Fall through to fallback.
            }
        }

        return fallback;
    }

    private static float getFloat(
        MapProperties properties,
        String name,
        float fallback
    ) {
        Object value = getRaw(properties, name);

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value != null) {
            try {
                return Float.parseFloat(
                    String.valueOf(value)
                );
            } catch (NumberFormatException ignored) {
                // Fall through to fallback.
            }
        }

        return fallback;
    }

    private static boolean getBoolean(
        MapProperties properties,
        String name,
        boolean fallback
    ) {
        Object value = getRaw(properties, name);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value != null) {
            return Boolean.parseBoolean(
                String.valueOf(value)
            );
        }

        return fallback;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    public float getMapHeight() {
        return mapHeight;
    }

    public Rectangle getCrossroadsBounds() {
        return crossroadsBounds;
    }

    public Rectangle getHiddenRoomBounds() {
        return hiddenRoomBounds;
    }

    public Rectangle getCityTransitionBounds() {
        return cityTransitionBounds;
    }

    public Vector2 getCrossroadsStart() {
        return crossroadsStart;
    }

    public Vector2 getCrossroadsReturnFromCity() {
        return crossroadsReturnFromCity;
    }

    public Array<Platform> getCollisionPlatforms() {
        return collisionPlatforms;
    }

    public Array<SpikeHazard> getSpikeHazards() {
        return spikeHazards;
    }

    public Array<EnemySpawn> getEnemySpawns() {
        return enemySpawns;
    }

    public Array<RoomTransition> getRoomTransitions() {
        return roomTransitions;
    }

    public CrackedWall getCrackedWall() {
        return crackedWall;
    }

    public int[] getBackgroundLayerIndices() {
        return backgroundLayerIndices;
    }

    public int[] getForegroundLayerIndices() {
        return foregroundLayerIndices;
    }

    @Override
    public void dispose() {
        tiledMap.dispose();
    }
}
