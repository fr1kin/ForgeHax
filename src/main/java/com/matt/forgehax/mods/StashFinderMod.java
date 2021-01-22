package com.matt.forgehax.mods;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.EvictingQueue;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.MapUtils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.matt.forgehax.Helper.printInform;

//@ClientPlugin
@RegisterMod
public class StashFinderMod extends ToggleMod { // implements IClientPlugin {

  private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  private final EvictingQueue<Chunk> chunks = EvictingQueue.create(5120);

//  private IClientAPI jmAPI;
  private int currentDimension = -1;
  private File stashFinderDir = null;

  public StashFinderMod() {
    super(Category.MISC, "StashFinder", false,
        "Find storage and log them");
  }

  private final Setting<Boolean> includeChests =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("include chests")
          .description("Clear chunk list on disable")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> onlyTrappedChests =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("only-trapped-chests")
          .description("Clear chunk list on disable")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> includeHoppers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("include-hoppers")
          .description("Include hoppers")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> includeDispensers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("include-dispensers")
          .description("Include dispensers")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> includeFurance =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("include-furnaces")
          .description("Include furnaces")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> includeEchests =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("include-echests")
          .description("Include echests")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> includeShulkers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("include-shulkers")
          .description("Include shulkers")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> includeBeds =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("include-beds")
          .description("Include beds")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> saveSignData =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("save-sign-data")
          .description("Include sign data")
          .defaultTo(true)
          .build();

//  private final Setting<Boolean> includeRareItems =
//      getCommandStub()
//          .builders()
//          .<Boolean>newSettingBuilder()
//          .name("include-rare-items")
//          .description("Include rare items such as resource blocks, beacons, quartz, etc")
//          .defaultTo(true)
//          .build();

  private final Setting<Long> hitThreshold =
      getCommandStub()
          .builders()
          .<Long>newSettingBuilder()
          .name("entity-threshold")
          .description(
              "Number if hits per chunk to trigger a coords log")
          .defaultTo(10L)
          .build();

//  private final Setting<Boolean> screenshot =
//      getCommandStub()
//          .builders()
//          .<Boolean>newSettingBuilder()
//          .name("take-screenshot")
//          .description("Take screenshot of chunk when hit matches")
//          .defaultTo(true)
//          .build();

  @Override
  protected void onEnabled() {
    super.onEnabled();
    stashFinderDir = new File("StashFinder");
    stashFinderDir.mkdirs();
  }

  @Override
  public void disable() {
    super.disable();
    chunks.clear();
  }

  @SubscribeEvent
  public void onPacketInbound(PacketEvent.Incoming.Pre event) {
    if (getWorld() == null) {
      return;
    }
    if (event.getPacket() instanceof SPacketChunkData) {
      SPacketChunkData packet = event.getPacket();
      Chunk chunk = getWorld().getChunkProvider().loadChunk(packet.getChunkX(), packet.getChunkZ());
      if (chunk == null) {
        LOGGER.log(Level.DEBUG, "Could not load chunk " + packet.getChunkX() + " " + packet.getChunkZ());
        return;
      } else {
        chunks.add(chunk);
      }
    }
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    while (chunks.size() > 0) {
      Chunk chunk = chunks.remove();
      LOGGER.debug("chunk.getTileEntityMap() " + chunk.getTileEntityMap().size());
      List<TileEntity> inChunk = getWorld().loadedTileEntityList.stream().filter(tileEntity -> MC.world.getChunkFromBlockCoords(tileEntity.getPos()).getPos().equals(chunk.getPos())).collect(Collectors.toList());
      List<Hit> hits = findHits(inChunk);
      if (hits.size() >= hitThreshold.get()) {
        printInform("Found possible stash at " + chunk.getPos().toString());
        report(chunk, hits);
      } else {
        LOGGER.debug("Only received " + hits.size() + " hits");
      }
    }
  }

  private void report(Chunk chunk, List<Hit> hits) {
    String fileName = DATE_FORMATTER.format(new Date());
    File file = new File(stashFinderDir, fileName + "-hit.txt");
    ArrayListMultimap<Block, Hit> hitsMap = ArrayListMultimap.create();
    hits.forEach(hit -> hitsMap.put(hit.blockType, hit));
    try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
      writer.print("Chunk location:");
      writer.print(" ");
      writer.println(chunk.getPos());
      hitsMap.asMap().forEach((key, value) -> writer.println(key.getLocalizedName() + ": " + value.size()));
      List<Hit> signs = hits.stream().filter(hit -> (hit instanceof SignHit)).collect(Collectors.toList());
      if (signs.size() > 0) {
        writer.println("Signs: ");
        signs.forEach(hit -> {
          writer.println(((SignHit) hit).signData);
        });
      }
    } catch (IOException e) {
      LOGGER.error("Couldn't save hit report", e);
    }
    saveMap(chunk, fileName);
    LOGGER.debug("Wrote stash file at " + file.getAbsolutePath());
  }

  private void saveMap(Chunk chunk, String fileName) {
    int x = chunk.x << 4;
    int z = chunk.z << 4;
    BufferedImage image = MapUtils.render(getWorld(), x, z);
    if (image == null) {
      LOGGER.info("Could not render image");
    } else {
      LOGGER.info("Rendered image");
      File mapFile = new File(stashFinderDir, fileName + "-map.png");
      try {
        ImageIO.write(image, "png", mapFile);
      } catch (IOException e) {
        LOGGER.error("Could not save image to " + mapFile, e);
      }
    }
  }

  private List<Hit> findHits(Collection<TileEntity> tileEntities) {
    List<Hit> hits = new ArrayList<>();
    if (includeChests.get()) {
      List<Hit> chests = tileEntities.stream().filter(entity -> {
        if (!(entity instanceof TileEntityChest)) {
          return false;
        }
        return !onlyTrappedChests.get() || entity.getBlockType() == Blocks.TRAPPED_CHEST;
      }).map(Hit::new).collect(Collectors.toList());
      hits.addAll(chests);
    }
    if (includeShulkers.get()) {
      hits.addAll(tileEntities.stream().filter(entity -> entity instanceof TileEntityShulkerBox).map(Hit::new).collect(Collectors.toList()));
    }
    if (includeEchests.get()) {
      hits.addAll(tileEntities.stream().filter(entity -> entity instanceof TileEntityEnderChest).map(Hit::new).collect(Collectors.toList()));
    }
    if (includeFurance.get()) {
      hits.addAll(tileEntities.stream().filter(entity -> entity instanceof TileEntityFurnace).map(Hit::new).collect(Collectors.toList()));
    }
    if (includeHoppers.get()) {
      hits.addAll(tileEntities.stream().filter(entity -> entity instanceof TileEntityHopper).map(Hit::new).collect(Collectors.toList()));
    }
    if (includeDispensers.get()) {
      hits.addAll(tileEntities.stream().filter(entity -> entity instanceof TileEntityDispenser).map(Hit::new).collect(Collectors.toList()));
    }
    if (includeBeds.get()) {
      hits.addAll(tileEntities.stream().filter(entity -> entity instanceof TileEntityBed).map(Hit::new).collect(Collectors.toList()));
    }
    if (saveSignData.get()) {
      hits.addAll(tileEntities.stream().filter(entity -> entity instanceof TileEntitySign).map(tileEntity -> new SignHit((TileEntitySign)tileEntity)).collect(Collectors.toList()));
    }
    return hits;
  }

  private static class Hit {
    public final Block blockType;
    public final BlockPos position;

    public Hit(TileEntity tileEntity) {
      this.blockType = tileEntity.getBlockType();
      this.position = tileEntity.getPos();
    }
  }

  private static class SignHit extends Hit {
    public final String signData;

    public SignHit(TileEntitySign sign) {
      super(sign);
      StringJoiner joiner = new StringJoiner(" / ");
      Arrays.stream(sign.signText).forEach(iTextComponent -> joiner.add(iTextComponent.getUnformattedText()));
      this.signData = joiner.toString();
    }
  }

  @Nullable
  public static WorldClient getWorld() {
    return MC.world;
  }
}
