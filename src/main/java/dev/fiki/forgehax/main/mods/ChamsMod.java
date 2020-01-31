package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@RegisterMod
public class ChamsMod extends ToggleMod {
  
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
          .name("mobs_hostile")
          .description("Enables hostile mobs")
          .defaultTo(true)
          .build();
  
  public final Setting<Boolean> mobs_friendly =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("mobs_friendly")
          .description("Enables friendly mobs")
          .defaultTo(true)
          .build();
  
  public ChamsMod() {
    super(Category.RENDER, "Chams", false, "Render living models behind walls");
  }
  
  public boolean shouldDraw(LivingEntity entity) {
    return !entity.equals(Globals.MC.player)
        && entity.isAlive()
        && ((mobs_hostile.get() && EntityUtils.isHostileMob(entity))
        || // check this first
        (players.get() && EntityUtils.isPlayer(entity))
        || (mobs_friendly.get() && EntityUtils.isFriendlyMob(entity)));
  }
  
  @SubscribeEvent
  public void onPreRenderLiving(RenderLivingEvent.Pre event) {
    GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
    GlStateManager.enablePolygonOffset();
    GlStateManager.polygonOffset(1.0F, -1000000);
  }
  
  @SubscribeEvent
  public void onPostRenderLiving(RenderLivingEvent.Post event) {
    GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    GlStateManager.polygonOffset(1.0F, 1000000);
    GlStateManager.disablePolygonOffset();
  }
}
