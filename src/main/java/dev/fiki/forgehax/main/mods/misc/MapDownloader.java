package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.mod.ToggleMod;

import java.io.File;

// TODO: 1.15
//@RegisterMod(
//    name = "MapDownloader",
//    description = "Saves map items as images",
//    category = Category.MISC
//)
public class MapDownloader extends ToggleMod {
  
  private File outputDir;
  
  /*
  private void saveImage(String fileName, BufferedImage image) {
    
    if (outputDir == null) {
      outputDir = FileManager.getInstance().getBaseResolve("maps").toFile();
    }
    if (!outputDir.exists()) {
      outputDir.mkdir();
    }
    try {
      File file = new File(outputDir, fileName + ".png");
      ImageIO.write(image, "png", file);
    } catch (Exception e) {
      getLogger().error(e);
    }
  }
  
  private void downloadMap(String fileName, Integer scaledRes) {
    if (getLocalPlayer() == null || !(getLocalPlayer().getHeldItemMainhand().getItem() instanceof MapItem)) {
      return;
    }

    MapItem map = (MapItem) getLocalPlayer().getHeldItemMainhand().getItem();
    MapData heldMapData = map.get(getLocalPlayer().getHeldItemMainhand(), getWorld());
    
    if (fileName == null) {
      fileName = heldMapData.mapName;
    }
    
    ResourceLocation location = findResourceLocation(heldMapData.mapName);
    if (location == null) {
      printError("Failed to find ResourceLocation");
      return;
    }
    
    DynamicTexture texture = (DynamicTexture) MC.getTextureManager().getTexture(location);
    BufferedImage image = dynamicToImage(texture);
    if (scaledRes != null) {
      image = createResizedCopy(image, scaledRes, scaledRes, true);
    }
    
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
    if (data.length != 128 * 128) {
      return null;
    }
    
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
                if (data.getArgument(1) != null) {
                  scaledRes = Integer.valueOf(data.getArgument(1));
                }
              } catch (NumberFormatException e) {
                Helper.printMessage("Failed to parse resolution");
              }
              
              downloadMap(fileName, scaledRes);
            })
        .build();
  }*/
}
