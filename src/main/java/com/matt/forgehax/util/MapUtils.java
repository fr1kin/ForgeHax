package com.matt.forgehax.util;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.reflection.FastReflection;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import scala.Int;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.util.ImageUtils.createResizedCopy;

public class MapUtils {

  public static BufferedImage render(World worldIn, int posX, int posZ) {
    MapData data = updateMapData(worldIn, posX, posZ);
    return render(worldIn, data, 512);
  }

  private static MapData updateMapData(World world, int posX, int posZ) {
    MapData data = new MapData("cute-" + System.currentTimeMillis());
    int step = 0;
//    if (world.provider.getDimension() == data.dimension)
    {
      int i = 1 << data.scale;
      int j = data.xCenter;
      int k = data.zCenter;
      int l = MathHelper.floor(posX - (double)j) / i + 64;
      int i1 = MathHelper.floor(posZ - (double)k) / i + 64;
      int j1 = 128 / i;

      if (world.provider.isNether())
      {
        j1 /= 2;
      }

      ++step;
      boolean flag = false;

      for (int k1 = l - j1 + 1; k1 < l + j1; ++k1)
      {
        if ((k1 & 15) == (step & 15) || flag)
        {
          flag = false;
          double d0 = 0.0D;

          for (int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1)
          {
            if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128)
            {
              int i2 = k1 - l;
              int j2 = l1 - i1;
              boolean flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
              int k2 = (j / i + k1 - 64) * i;
              int l2 = (k / i + l1 - 64) * i;
              Multiset<MapColor> multiset = HashMultiset.<MapColor>create();
              Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(k2, 0, l2));

              if (!chunk.isEmpty())
              {
                int i3 = k2 & 15;
                int j3 = l2 & 15;
                int k3 = 0;
                double d1 = 0.0D;

                if (world.provider.isNether())
                {
                  int l3 = k2 + l2 * 231871;
                  l3 = l3 * l3 * 31287121 + l3 * 11;

                  if ((l3 >> 20 & 1) == 0)
                  {
                    multiset.add(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT).getMapColor(world, BlockPos.ORIGIN), 10);
                  }
                  else
                  {
                    multiset.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE).getMapColor(world, BlockPos.ORIGIN), 100);
                  }

                  d1 = 100.0D;
                }
                else
                {
                  BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                  for (int i4 = 0; i4 < i; ++i4)
                  {
                    for (int j4 = 0; j4 < i; ++j4)
                    {
                      int k4 = chunk.getHeightValue(i4 + i3, j4 + j3) + 1;
                      IBlockState iblockstate = Blocks.AIR.getDefaultState();

                      if (k4 <= 1)
                      {
                        iblockstate = Blocks.BEDROCK.getDefaultState();
                      }
                      else
                      {
                        label175:
                        {
                          while (true)
                          {
                            --k4;
                            iblockstate = chunk.getBlockState(i4 + i3, k4, j4 + j3);
                            blockpos$mutableblockpos.setPos((chunk.x << 4) + i4 + i3, k4, (chunk.z << 4) + j4 + j3);

                            if (iblockstate.getMapColor(world, blockpos$mutableblockpos) != MapColor.AIR || k4 <= 0)
                            {
                              break;
                            }
                          }

                          if (k4 > 0 && iblockstate.getMaterial().isLiquid())
                          {
                            int l4 = k4 - 1;

                            while (true)
                            {
                              IBlockState iblockstate1 = chunk.getBlockState(i4 + i3, l4--, j4 + j3);
                              ++k3;

                              if (l4 <= 0 || !iblockstate1.getMaterial().isLiquid())
                              {
                                break label175;
                              }
                            }
                          }
                        }
                      }

                      d1 += (double)k4 / (double)(i * i);
                      multiset.add(iblockstate.getMapColor(world, blockpos$mutableblockpos));
                    }
                  }
                }

                k3 = k3 / (i * i);
                double d2 = (d1 - d0) * 4.0D / (double)(i + 4) + ((double)(k1 + l1 & 1) - 0.5D) * 0.4D;
                int i5 = 1;

                if (d2 > 0.6D)
                {
                  i5 = 2;
                }

                if (d2 < -0.6D)
                {
                  i5 = 0;
                }

                MapColor mapcolor = (MapColor) Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.AIR);

                if (mapcolor == MapColor.WATER)
                {
                  d2 = (double)k3 * 0.1D + (double)(k1 + l1 & 1) * 0.2D;
                  i5 = 1;

                  if (d2 < 0.5D)
                  {
                    i5 = 2;
                  }

                  if (d2 > 0.9D)
                  {
                    i5 = 0;
                  }
                }

                d0 = d1;

                if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0))
                {
                  byte b0 = data.colors[k1 + l1 * 128];
                  byte b1 = (byte)(mapcolor.colorIndex * 4 + i5);

                  if (b0 != b1)
                  {
                    data.colors[k1 + l1 * 128] = b1;
                    data.updateMapData(k1, l1);
                    flag = true;
                  }
                }
              }
            }
          }
        }
      }
    }
    return data;
  }

  private static BufferedImage render(World world, MapData data, Integer scaledRes) {
    world.setData(data.mapName, data);
    MapItemRenderer mapRenderer = MC.entityRenderer.getMapItemRenderer();
    mapRenderer.updateMapTexture(data);
    mapRenderer.renderMap(data, false);
    ResourceLocation location = findResourceLocation(data.mapName);
    if (location == null) {
      Helper.printMessage("Failed to find ResourceLocation");
      return null;
    }
    DynamicTexture texture = (DynamicTexture) MC.getTextureManager().getTexture(location);
    BufferedImage image = dynamicToImage(texture);
    if (scaledRes != null) {
      image = createResizedCopy(image, scaledRes, scaledRes, true);
    }
    return image;
  }

  public static BufferedImage dynamicToImage(DynamicTexture texture) {
    int[] data = texture.getTextureData();
    BufferedImage image = new BufferedImage(128, 128, 2);
    image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, 128);
    return image;
  }

  public static ResourceLocation findResourceLocation(String name) {
    Map<ResourceLocation, ITextureObject> mapTextureObjects =
        FastReflection.Fields.TextureManager_mapTextureObjects.get(MC.getTextureManager());

    return mapTextureObjects
        .keySet()
        .stream()
        .filter(k -> k.getResourcePath().contains(name))
        .findFirst()
        .orElse(null);
  }
}
