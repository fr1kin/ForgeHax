package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.util.draw.SurfaceHelper.drawOutlinedRect;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.Plane;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.draw.RenderUtils;
import java.util.Objects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

@RegisterMod
public class EntityESP extends ToggleMod {

  public enum ESPMode {
    BOX,
    SQUARE
  }

  public final Setting<ESPMode> mode =
    getCommandStub()
      .builders()
      .<ESPMode>newSettingEnumBuilder()
      .name("mode")
      .description("2D or 3D ESP rendering")
      .defaultTo(ESPMode.SQUARE)
      .build();
  
  public final Setting<Boolean> players =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("players")
      .description("Enables players")
      .defaultTo(true)
      .build();

  public final Setting<Boolean> mobs_hostile =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("hostile")
      .description("Enables hostile mobs")
      .defaultTo(true)
      .build();

  public final Setting<Boolean> mobs_friendly =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("friendly")
      .description("Enables friendly mobs")
      .defaultTo(true)
      .build();

  public final Setting<Float> linewidth =
    getCommandStub()
      .builders()
      .<Float>newSettingBuilder()
      .name("width")
      .description("Line width")
      .defaultTo(2.0F)
      .build();

  private final Setting<Integer> alpha =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("alpha")
          .description("alpha")
          .min(0)
          .max(255)
          .defaultTo(255)
          .build();

  private final Setting<Integer> red =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("red")
          .description("red")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();

  private final Setting<Integer> green =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("green")
          .description("green")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();

  private final Setting<Integer> blue =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("blue")
          .description("blue")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();
  
  public EntityESP() {
    super(Category.RENDER, "EntityESP", false, "Draw 2D boxes around entities");
  }
  

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRender2D(final Render2DEvent event) {
    if (mode.get() == ESPMode.BOX) return;
    getWorld()
      .loadedEntityList
      .stream()
      .filter(EntityUtils::isLiving)
      .filter(
        entity ->
          !Objects.equals(getLocalPlayer(), entity) && !EntityUtils.isFakeLocalPlayer(entity))
      .filter(EntityUtils::isAlive)
      .filter(EntityUtils::isValidEntity)
      .map(entity -> (EntityLivingBase) entity)
      .forEach(
        living -> {
          switch (EntityUtils.getRelationship(living)) {
            case PLAYER:
			        if (!players.get()) return;
              break;
            case HOSTILE:
			        if (!mobs_hostile.get()) return;
              break;
            case NEUTRAL:
            case FRIENDLY:
			        if (!mobs_friendly.get()) return;
              break;
          }

	  	    int color = Color.of(red.get(), green.get(), blue.get(), alpha.get()).toBuffer();
          Vec3d bottomPos = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
          Vec3d topPos =
            bottomPos.addVector(0.D, living.getRenderBoundingBox().maxY - living.posY, 0.D);
          
          Plane top = VectorUtils.toScreen(topPos);
          Plane bot = VectorUtils.toScreen(bottomPos);
          
          double topX = top.getX();
          double topY = top.getY() + 1.D;
          double botX = bot.getX();
          double botY = bot.getY() + 1.D;
          double height = (bot.getY() - top.getY());
          double width = height;
  		
		      drawOutlinedRect((int) (topX - (width/2)), (int) topY, (int) width, (int) height,
		  	  			color, linewidth.get());
        });
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRender(final RenderEvent event) {
    if (mode.get() == ESPMode.SQUARE) return;
    getWorld()
      .loadedEntityList
      .stream()
      .filter(EntityUtils::isLiving)
      .filter(
        entity ->
          !Objects.equals(getLocalPlayer(), entity) && !EntityUtils.isFakeLocalPlayer(entity))
      .filter(EntityUtils::isAlive)
      .filter(EntityUtils::isValidEntity)
      .map(entity -> (EntityLivingBase) entity)
      .forEach(
        living -> {
          switch (EntityUtils.getRelationship(living)) {
            case PLAYER:
			        if (!players.get()) return;
              break;
            case HOSTILE:
			        if (!mobs_hostile.get()) return;
              break;
            case NEUTRAL:
            case FRIENDLY:
			        if (!mobs_friendly.get()) return;
              break;
          }

	  	    int color = Color.of(red.get(), green.get(), blue.get(), alpha.get()).toBuffer();
          AxisAlignedBB bb = living.getEntityBoundingBox();
          Vec3d minVec = new Vec3d(bb.minX, bb.minY, bb.minZ);
          Vec3d maxVec = new Vec3d(bb.maxX, bb.maxY, bb.maxZ);

          // GlStateManager.enableDepth();
          // GlStateManager.glLineWidth(linewidth.get());
          // GL11.glEnable(GL11.GL_LINE_SMOOTH);

          RenderUtils.drawBox(minVec, maxVec, color, linewidth.get(), true);

          // GL11.glDisable(GL11.GL_LINE_SMOOTH);
          // GlStateManager.glLineWidth(1.0f);
          // GlStateManager.disableDepth();
        });
  }
}
