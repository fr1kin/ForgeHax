package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.item.ItemMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;

import static com.matt.forgehax.util.MapColors.*;


/**
 * Created by Babbaj on 8/18/2017.
 */
@RegisterMod
public class MapMod extends ToggleMod {
    public MapMod() {
        super(Category.MISC, "MapMod", false, "custom map images");
    }

    private enum Mode {
        DATA,
        TEXTURE
    }

    public final Setting<MapMod.Mode> mode = getCommandStub().builders().<MapMod.Mode>newSettingEnumBuilder()
            .name("mode")
            .description("[DATA]change map data or [TEXTURE]render full image")
            .defaultTo(MapMod.Mode.DATA)
            .build();


    private BufferedImage getImageFromUrl(String link) {
        BufferedImage image = new BufferedImage(128, 128, 1);
        try {
            URL url = new URL(link);
            image = ImageIO.read(url);
        } catch (Exception e) {
            Helper.printMessage("Failed to download image");
        }
        return image;
    }

    private BufferedImage createResizedCopy(Image originalImage,
                                    int scaledWidth, int scaledHeight,
                                    boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    private int[][] imageToArray(BufferedImage imageIn) {
        int width = imageIn.getWidth();
        int height = imageIn.getHeight();

        int[][] data = new int[height][width]; // array of rows

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data[i][j] = imageIn.getRGB(i, j);
            }
        }
        return data;
    }

    private byte closest_color_RGB(int colorIn) {
        int[] RGB_Array = Utils.toRGBAArray(colorIn); // [0] red [1] green [2] blue [3] alpha

        double closestDistance = 500; // create a starting point
        int closestColorIndex = 4; // index of COLOR_LIST that is the closest color we've found to the input color - start at 4 so we dont ever use air
        for (int i = 4; i < colorListLength(); i++) {
            int[] currentColor = Utils.toRGBAArray(getColor(i));
            double distance = distanceBetweenColors(currentColor, RGB_Array);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestColorIndex = i;
            }
        }

        return (byte) closestColorIndex;
    }

    private double distanceBetweenColors(int[] a, int[] b) {
        return Math.sqrt((a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) + (a[2] - b[2]) * (a[2] - b[2]));
    }


    private void updateHeldMap(String url) {
        if (MC.player == null || !(MC.player.getHeldItemMainhand().getItem() instanceof ItemMap)) return;

        BufferedImage image = getImageFromUrl(url);
        image = createResizedCopy(image, 128, 128, false);
        int[][] imageColors = imageToArray(image); // convert image into a 2d array of rgba integers

        byte[] convertedMapColors = new byte[128 * 128]; // create a 1d array 128^2 in length that will be used to hold the final map data

        int count = 0;
        for (int i = 0; i < 128; i++) { // iterate vertically
            for (int j = 0; j < 128; j++) { // iterate through row of pixels
                imageColors[j][i] = closest_color_RGB(imageColors[j][i]); // each color in the image data is now an index of COLOR_LIST that most closely matches in color
                convertedMapColors[count] = (byte) imageColors[j][i]; // convert the 2d array into a 1d array
                count++;
            } // normally would do [i][j] but that appears to cause a rotation problem that is fixed by doing [j][i]
        }


        ItemMap map = (ItemMap) MC.player.getHeldItemMainhand().getItem();

        MapData heldMapData = map.getMapData(MC.player.getHeldItemMainhand(), MC.world);

        heldMapData.colors = convertedMapColors; // set the colors of the map to the colors of the image

    }

    private void updateHeldMapTexture(String url) {
        if (MC.player == null || !(MC.player.getHeldItemMainhand().getItem() instanceof ItemMap)) return;

        MC.addScheduledTask(() -> { // allows DynamicTexture to work
            ItemMap map = (ItemMap) MC.player.getHeldItemMainhand().getItem();
            MapData heldMapData = map.getMapData(MC.player.getHeldItemMainhand(), MC.world);

            try {
                BufferedImage image = getImageFromUrl(url);

                DynamicTexture dynamicTexture = new DynamicTexture(image);
                dynamicTexture.loadTexture(MC.getResourceManager());

                Map<ResourceLocation, ITextureObject> mapTextureObjects = FastReflection.Fields.TextureManager_mapTextureObjects.get(MC.getTextureManager());

                ResourceLocation textureLocation =
                        mapTextureObjects.keySet()
                                         .stream()
                                         .filter(k -> k.getResourcePath().contains(heldMapData.mapName))
                                         .findFirst()
                                         .orElse(null);

                mapTextureObjects.put(textureLocation, dynamicTexture); // overwrite old texture with our custom one

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void onLoad() {
        getCommandStub().builders().newCommandBuilder()
                .name("updatemap")
                .description("Update held map with image from internet")
                .processor(data -> {
                    data.requiredArguments(1);
                    // do stuff
                    String url = data.getArgumentAsString(0);
                    switch (mode.get()) {
                        case DATA:
                            updateHeldMap(url);
                            break;
                        case TEXTURE:
                            updateHeldMapTexture(url);
                            break;
                    }
                })
                .build();
    }
}
