import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MapSpritePainter {
    private MapSprite mapSprite;
    private MapSprite rgbSprite;

    public MapSpritePainter(MapSprite mapSprite, MapSprite rgbSprite) {
        this.mapSprite = mapSprite;
        this.rgbSprite = rgbSprite;
    }

    public void paintAndSave(String inputPath, String outputPath, int tileSize) {
        try {
            BufferedImage inputImage = ImageIO.read(new File(inputPath));
            BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            int numTilesWide = inputImage.getWidth() / tileSize;
            int numTilesHigh = inputImage.getHeight() / tileSize;

            BufferedImage[][] inputTiles = new BufferedImage[numTilesWide][numTilesHigh];

            for (int y = 0; y < numTilesHigh; y++) {
                for (int x = 0; x < numTilesWide; x++) {
                    inputTiles[x][y] = inputImage.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }

            for (int y = 0; y < numTilesHigh; y++) {
                for (int x = 0; x < numTilesWide; x++) {
                    BufferedImage inputTile = inputTiles[x][y];
                    boolean matchFound = false;

                    for (int my = 0; my < mapSprite.tiles[0].length && !matchFound; my++) {
                        for (int mx = 0; mx < mapSprite.tiles.length && !matchFound; mx++) {
                            BufferedImage mapTile = mapSprite.getTile(mx, my);

                            if (areImagesEqual(inputTile, mapTile, tileSize)) {
                                int argb = rgbSprite.getRGB(mx * 32, my * 32);

                                int alpha = (argb >> 24) & 0xFF;
                                int red = (argb >> 16) & 0xFF;
                                int green = (argb >> 8) & 0xFF;
                                int blue = argb & 0xFF;

                                for (int ty = 0; ty < tileSize; ty++) {
                                    for (int tx = 0; tx < tileSize; tx++) {
                                        int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                                        outputImage.setRGB(x * tileSize + tx, y * tileSize + ty, rgb);
                                    }
                                }

                                matchFound = true;
                            }
                        }
                    }

                    if (!matchFound && isTileTransparent(inputTile)) {
                        int argb = rgbSprite.getRGB(11 * 32, 0);

                        int alpha = (argb >> 24) & 0xFF;
                        int red = (argb >> 16) & 0xFF;
                        int green = (argb >> 8) & 0xFF;
                        int blue = argb & 0xFF;

                        for (int ty = 0; ty < tileSize; ty++) {
                            for (int tx = 0; tx < tileSize; tx++) {
                                int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                                outputImage.setRGB(x * tileSize + tx, y * tileSize + ty, rgb);
                            }
                        }
                    }
                }
            }

            ImageIO.write(outputImage, "png", new File(outputPath));

            BufferedImage shrunkImage = shrinkImage(outputImage, tileSize);
            ImageIO.write(shrunkImage, "png", new File("res/shrunk_output.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean areImagesEqual(BufferedImage img1, BufferedImage img2, int tileSize) {
        int numTilesWide1 = img1.getWidth() / tileSize;
        int numTilesHigh1 = img1.getHeight() / tileSize;

        int numTilesWide2 = img2.getWidth() / tileSize;
        int numTilesHigh2 = img2.getHeight() / tileSize;

        for (int y1 = 0; y1 < numTilesHigh1; y1++) {
            for (int x1 = 0; x1 < numTilesWide1; x1++) {
                BufferedImage tile1 = img1.getSubimage(x1 * tileSize, y1 * tileSize, tileSize, tileSize);

                boolean matchFound = false;

                BufferedImage tile2 = null;
                for (int y2 = 0; y2 < numTilesHigh2 && !matchFound; y2++) {
                    for (int x2 = 0; x2 < numTilesWide2 && !matchFound; x2++) {
                        tile2 = img2.getSubimage(x2 * tileSize, y2 * tileSize, tileSize, tileSize);

                        if (areTilesEqual(tile1, tile2)) {
                            matchFound = true;
                        }
                    }
                }


                if (!matchFound && isTileTransparent(tile1) && isTileTransparent(tile2)) {
                    matchFound = true;
                }

                if (!matchFound) {
                    return false;
                }

            }
        }

        return true;
    }

    private boolean areTilesEqual(BufferedImage tile1, BufferedImage tile2) {
        if (tile1.getWidth() == tile2.getWidth() && tile1.getHeight() == tile2.getHeight()) {
            for (int y = 0; y < tile1.getHeight(); y++) {
                for (int x = 0; x < tile1.getWidth(); x++) {
                    int argb1 = tile1.getRGB(x, y);
                    int argb2 = tile2.getRGB(x, y);

                    if (argb1 != argb2) {
                        int alpha1 = (argb1 >> 24) & 0xFF;
                        int alpha2 = (argb2 >> 24) & 0xFF;

                        if (!(alpha1 == 0 && alpha2 == 0)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    private boolean isTileTransparent(BufferedImage tile) {
        for (int y = 0; y < tile.getHeight(); y++) {
            for (int x = 0; x < tile.getWidth(); x++) {
                int argb = tile.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;

                if (alpha != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public BufferedImage shrinkImage(BufferedImage outputImage, int tileSize) {
        int numTilesWide = outputImage.getWidth() / tileSize;
        int numTilesHigh = outputImage.getHeight() / tileSize;

        BufferedImage shrunkImage = new BufferedImage(numTilesWide, numTilesHigh, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < numTilesHigh; y++) {
            for (int x = 0; x < numTilesWide; x++) {
                int sumAlpha = 0;
                int sumRed = 0;
                int sumGreen = 0;
                int sumBlue = 0;

                for (int ty = 0; ty < tileSize; ty++) {
                    for (int tx = 0; tx < tileSize; tx++) {
                        int argb = outputImage.getRGB(x * tileSize + tx, y * tileSize + ty);

                        int alpha = (argb >> 24) & 0xFF;
                        int red = (argb >> 16) & 0xFF;
                        int green = (argb >> 8) & 0xFF;
                        int blue = argb & 0xFF;

                        sumAlpha += alpha;
                        sumRed += red;
                        sumGreen += green;
                        sumBlue += blue;
                    }
                }

                int avgAlpha = sumAlpha / (tileSize * tileSize);
                int avgRed = sumRed / (tileSize * tileSize);
                int avgGreen = sumGreen / (tileSize * tileSize);
                int avgBlue = sumBlue / (tileSize * tileSize);

                int avgRgb = (avgAlpha << 24) | (avgRed << 16) | (avgGreen << 8) | avgBlue;
                shrunkImage.setRGB(x, y, avgRgb);
            }
        }

        return shrunkImage;
    }
}