Hidden-room background cover fix

1. Extract this bundle into the project root (beside build.gradle).
2. Test:
   patch --dry-run -p1 < hidden_room_background_cover_fix.patch
3. Apply:
   patch -p1 < hidden_room_background_cover_fix.patch

The included cover texture is installed at:
assets/maps/environment/forgotten_crossroads/background/crossroads_secret_01_cover.png

It is cropped from cd_room_BG_02.png using the original world.tmx values:
- hidden room: x=1984, y=640, width=576, height=312
- middle background offset: x=1336, y=648
