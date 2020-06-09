package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

/**
 * Created on 9/4/2016 by fr1kin
 * Updated by OverFloyd, may 2020
 */
@RegisterMod
public class UpdateESP extends ToggleMod {

  private final Setting<Float> width =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("width")
          .description("The width value for the outline")
          .min(0.5f)
          .defaultTo(1.0f)
          .build();

  public final Setting<Boolean> antialias =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("antialias")
          .description("Makes lines and triangles more smooth, may hurt performance")
          .defaultTo(false)
          .build();

  public final Setting<Long> timeout =
      getCommandStub()
          .builders()
          .<Long>newSettingBuilder()
          .name("timeout")
          .description("Milliseconds after which to clear")
          .defaultTo(100L)
          .min(1L)
          .max(10000L)
          .build();

  private final Setting<Integer> alpha =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("alpha")
          .description("Transparency, 0-255")
          .min(0)
          .max(255)
          .defaultTo(80)
          .build();
  private final Setting<Integer> red =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("red")
          .description("Red amount, 0-255")
          .min(0)
          .max(255)
          .defaultTo(255)
          .build();
  private final Setting<Integer> green =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("green")
          .description("Green amount, 0-255")
          .min(0)
          .max(255)
          .defaultTo(255)
          .build();
  private final Setting<Integer> blue =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("blue")
          .description("Blue amount, 0-255")
          .min(0)
          .max(255)
          .defaultTo(255)
          .build();


  public UpdateESP() {
    super(Category.RENDER, "UpdateESP", false, "Shows block updates");
  }

  private ConcurrentHashMap<BlockPos, Long> block_updates = new ConcurrentHashMap<BlockPos, Long>();

  public void addBlockUpdate(BlockPos pos, long time) {
    block_updates.put(pos, time);
  }

  @SubscribeEvent
  public void onRender(final RenderEvent event) {
    if(MC.gameSettings.hideGUI || getWorld() == null) {
      return;
    }

    if (antialias.get()) {
      GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    long now = Instant.now().toEpochMilli();
    int clr = Color.of(red.get(), green.get(), blue.get(), alpha.get()).toBuffer();


    for (BlockPos pos : block_updates.keySet()) {
      GlStateManager.glLineWidth(width.get());
      event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
      GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, clr);
      event.getTessellator().draw();
      if (now > (block_updates.get(pos) + timeout.get())) {
        block_updates.remove(pos);
      }
    }

    if (antialias.get()) { 
      GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
      GL11.glDisable(GL11.GL_LINE_SMOOTH);
      GlStateManager.glLineWidth(1.0f);
    }
  }
}
