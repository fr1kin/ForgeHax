package com.matt.forgehax.mods;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.mod.ToggleMod;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@RegisterMod
public class WaifuEsp extends ToggleMod {
    public WaifuEsp() { super ("WaifuESP", false, "overlay cute animes over players"); }

    public final Setting<Boolean> renderPlayers = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("RenderPlayers")
            .description("render other players")
            .defaultTo(false)
            .build();

      private final ResourceLocation waifu = new ResourceLocation(ForgeHax.MOD_ID,"waifu1.png");

    private boolean shouldDraw(EntityLivingBase entity) {
        return LocalPlayerUtils.isTargetEntity(entity) || (
                !entity.equals(MC.player) &&
                        EntityUtils.isAlive(entity) &&
                        EntityUtils.isValidEntity(entity) && (
                        EntityUtils.isPlayer(entity))
                );
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGameOverlayEvent (RenderGameOverlayEvent.Text event) {
        for (Entity entity : MC.world.loadedEntityList) {
            if (EntityUtils.isLiving(entity) && shouldDraw((EntityLivingBase) entity)) {
                EntityLivingBase living = (EntityLivingBase) (entity);
                Vec3d bottomVec = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
                Vec3d topVec = bottomVec.add(new Vec3d(0, (entity.getRenderBoundingBox().maxY - entity.posY), 0));
                VectorUtils.ScreenPos top = VectorUtils.toScreen(topVec.x, topVec.y, topVec.z);
                VectorUtils.ScreenPos bot = VectorUtils.toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
                if (top.isVisible || bot.isVisible) {

                    int height = (bot.y - top.y);
                    int width = height;

                    int x = (int)(top.x - (width / 1.8)); // normally 2.0 but lowering it shifts it to the left
                    int y = top.y;

                    // draw waifu
                    GlStateManager.enableTexture2D(); // not sure if this is necessary
                    MC.renderEngine.bindTexture(waifu);
                    GlStateManager.scale(1,1,1);
                    GlStateManager.color(1,1,1);

                    Gui.drawScaledCustomSizeModalRect(x, y, 0, 0, width, height, width, height, width, height);
                    GlStateManager.disableTexture2D();
                }
            }
        }
    }


    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if(!renderPlayers.getAsBoolean() &&!event.getEntity().equals(MC.player)) {
            event.setCanceled(true);
        }
    }
}
