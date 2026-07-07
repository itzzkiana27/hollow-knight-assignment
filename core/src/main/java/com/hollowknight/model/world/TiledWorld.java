package com.hollowknight.model.world;

import com.badlogic.gdx.maps.MapLayer;
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

    private final TiledMap tiledMap;

    private final float mapWidth;
    private final float mapHeight;

    private final Rectangle crossroadsBounds;
    private final Rectangle hiddenRoomBounds;
    private final Rectangle cityTransitionBounds;

    private final Vector2 crossroadsStart;
    private final Vector2 crossroadsReturnFromCity;

    private final Array<Platform> collisionPlatforms;
    private final Array<SpikeHazard> spikeHazards;
    private final Array<EnemySpawn> enemySpawns;

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

        Rectangle startObject = requireRectangle(
            "Spawns",
            "crossroads_start"
        );

        Rectangle returnObject = requireRectangle(
            "Spawns",
            "crossroads_return_from_city"
        );

        crossroadsStart = new Vector2(
            startObject.x,
            startObject.y
        );

        crossroadsReturnFromCity = new Vector2(
            returnObject.x,
            returnObject.y
        );

        collisionPlatforms = new Array<>();
        crackedWall = loadCollisionObjects();

        spikeHazards = loadHazards();
        enemySpawns = loadEnemySpawns();

        backgroundLayerIndices =
            collectTopLevelLayerIndices(
                "Crossroads_Background",
                "Background",
                "Terrain_Back",
                "Terrain"
            );

        foregroundLayerIndices =
            collectTopLevelLayerIndices(
                "Foreground"
            );
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

            if (!touchesRoom(rectangle, crossroadsBounds)) {
                continue;
            }

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

            if (!touchesRoom(bounds, crossroadsBounds)) {
                continue;
            }

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

            if (
                !"forgotten_crossroads".equals(roomId)
                    && !touchesRoom(
                    rectangle,
                    crossroadsBounds
                )
            ) {
                continue;
            }

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
        int[] indices = new int[names.length];

        for (
            int nameIndex = 0;
            nameIndex < names.length;
            nameIndex++
        ) {
            indices[nameIndex] = -1;

            for (
                int layerIndex = 0;
                layerIndex
                    < tiledMap.getLayers().getCount();
                layerIndex++
            ) {
                MapLayer layer =
                    tiledMap.getLayers().get(
                        layerIndex
                    );

                if (
                    names[nameIndex]
                        .equals(layer.getName())
                ) {
                    indices[nameIndex] = layerIndex;
                    break;
                }
            }

            if (indices[nameIndex] < 0) {
                throw new IllegalStateException(
                    "Missing top-level Tiled layer: "
                        + names[nameIndex]
                );
            }
        }

        return indices;
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
