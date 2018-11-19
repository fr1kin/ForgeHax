package com.matt.forgehax.mods;

import static com.matt.forgehax.util.ImageUtils.*;

import com.matt.forgehax.FileManager;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.item.ItemMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;

/** Created by Babbaj on 11/6/2017. */
@RegisterMod
public class MapDownloader extends ToggleMod {

  private File outputDir;

  public MapDownloader() {
    super(Category.MISC, "MapDownloader", false, "Saves map items as images");
  }

  private void saveImage(String fileName, BufferedImage image) {

    if (outputDir == null) {
      outputDir = new File(FileManager.getInstance().getBaseDirectory(), "maps");
    }
    if (!outputDir.exists()) {
      outputDir.mkdir();
    }
    try {
      File file = new File(outputDir, fileName + ".png");
      ImageIO.write(image, "png", file);
    } catch (Exception e) {
      Helper.printStackTrace(e);
    }
  }

  private void downloadMap(String fileName, Integer scaledRes) {
    if (MC.player == null || !(MC.player.getHeldItemMainhand().getItem() instanceof ItemMap))
      return;

    ItemMap map = (ItemMap) MC.player.getHeldItemMainhand().getItem();
    MapData heldMapData = map.getMapData(MC.player.getHeldItemMainhand(), MC.world);

    if (fileName == null) fileName = heldMapData.mapName;

    ResourceLocation location = findResourceLocation(heldMapData.mapName);
    if (location == null) {
      Helper.printMessage("Failed to find ResourceLocation");
      return;
    }

    DynamicTexture texture = (DynamicTexture) MC.getTextureManager().getTexture(location);
    BufferedImage image = dynamicToImage(texture);
    if (scaledRes != null) image = createResizedCopy(image, scaledRes, scaledRes, true);

    saveImage(fileName, image);
  }

  private ResourceLocation findResourceLocation(String name) {
    Map<ResourceLocation, ITextureObject> mapTextureObjects =
        FastReflection.Fields.TextureManager_mapTextureObjects.get(MC.getTextureManager());

    return mapTextureObjects
        .keySet()
        .stream()
        .filter(k -> k.getResourcePath().contains(name))
        .findFirst()
        .orElse(null);
  }

  // TODO: generalize this
  private BufferedImage dynamicToImage(DynamicTexture texture) {
    int[] data = texture.getTextureData();
    if (data.length != 128 * 128) return null;

    BufferedImage image = new BufferedImage(128, 128, 2);

    image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, 128);
    return image;
  }

  @Override
  public void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("Download")
        .description("Download the held map as an image")
        .processor(
            data -> {
              data.requiredArguments(0);
              // do stuff
              String fileName = data.getArgument(0);
              Integer scaledRes = null;
              try {
                if (data.getArgument(1) != null) scaledRes = Integer.valueOf(data.getArgument(1));
              } catch (NumberFormatException e) {
                Helper.printMessage("Failed to parse resolution");
              }

              downloadMap(fileName, scaledRes);
            })
        .build();
  }
}
