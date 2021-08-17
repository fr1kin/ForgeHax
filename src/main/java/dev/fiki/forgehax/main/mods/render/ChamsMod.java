package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.LivingRenderEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
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

  @SubscribeListener
  public void onPreRenderLiving(LivingRenderEvent.Pre<?, ?> event) {
    GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
    GlStateManager._enablePolygonOffset();
    GlStateManager._polygonOffset(1.0F, -1000000);
  }

  @SubscribeListener
  public void onPostRenderLiving(LivingRenderEvent.Post<?, ?> event) {
    GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    GlStateManager._polygonOffset(1.0F, 1000000);
    GlStateManager._disablePolygonOffset();
  }
}
