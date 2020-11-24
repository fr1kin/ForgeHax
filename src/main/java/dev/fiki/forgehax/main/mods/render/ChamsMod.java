package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@RegisterMod(
    name = "Chams",
    description = "Render living models behind walls",
    category = Category.RENDER
)
@ExtensionMethod({EntityEx.class})
public class ChamsMod extends ToggleMod {

  public final BooleanSetting players = newBooleanSetting()
      .name("players")
      .description("Enables players")
      .defaultTo(true)
      .build();

  public final BooleanSetting mobs_hostile = newBooleanSetting()
      .name("mobs_hostile")
      .description("Enables hostile mobs")
      .defaultTo(true)
      .build();

  public final BooleanSetting mobs_friendly = newBooleanSetting()
      .name("mobs_friendly")
      .description("Enables friendly mobs")
      .defaultTo(true)
      .build();

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
