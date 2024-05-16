public class Main {
    public static void main(String[] args) {
        ModifiedSpritesGenerator generator = new ModifiedSpritesGenerator("res/forbidden_values.txt");
        generator.generate("res/mapsprites.png", "res/modified_sprites.png", 32);

        SpriteConverter spriteConverter = new SpriteConverter();

        int[][] rgbValues = spriteConverter.convert("res/mapsprites.png", "res/modified_sprites.png", 32);

        MapSprite mapSprite = new MapSprite("res/mapsprites.png", 32, rgbValues);
        MapSprite rpgSprite = new MapSprite("res/modified_sprites.png", 32, rgbValues);

        MapSpritePainter mapSpritePainter = new MapSpritePainter(mapSprite, rpgSprite);
        mapSpritePainter.paintAndSave("res/mapsprites-export.png", "res/output.png", 32);
    }
}