package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.events.Render2DEvent;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.tesselation.GeometryTessellator;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class RenderEventService extends ServiceMod {
  
  private static final GeometryTessellator TESSELLATOR = new GeometryTessellator();
  
  public RenderEventService() {
    super("RenderEventService");
  }
  
  @SubscribeEvent
  public void onRenderWorld(RenderWorldLastEvent event) {
    GlStateManager.pushMatrix();
    GlStateManager.disableTexture();
    GlStateManager.enableBlend();
    GlStateManager.disableAlphaTest();
    GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    GlStateManager.disableDepthTest();
    
    GlStateManager.lineWidth(1.f);
    
    Vec3d renderPos = EntityUtils.getInterpolatedPos(Common.getLocalPlayer(), event.getPartialTicks());
    
    RenderEvent e = new RenderEvent(TESSELLATOR, renderPos, event.getPartialTicks());
    e.resetTranslation();
    MinecraftForge.EVENT_BUS.post(e);
    
    GlStateManager.lineWidth(1.f);
    
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.disableBlend();
    GlStateManager.enableAlphaTest();
    GlStateManager.enableTexture();
    GlStateManager.enableDepthTest();
    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }
  
  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Text event) {
    if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
      MinecraftForge.EVENT_BUS.post(new Render2DEvent(event.getPartialTicks()));
      GlStateManager.color4f(1.f, 1.f, 1.f, 1.f); // reset color
    }
  }
}
