package com.matt.forgehax;

import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.events.WorldChangeEvent;
import com.matt.forgehax.events.listeners.WorldListener;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.entity.EntityUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.opengl.GL11;

import static com.matt.forgehax.Wrapper.getLocalPlayer;

import static com.matt.forgehax.Wrapper.*;

public class ForgeHaxEventHandler implements Globals {
    private static GeometryTessellator tessellator = new GeometryTessellator(0x200);

    private static final WorldListener WORLD_LISTENER = new WorldListener();

    /**
     * Called when the local player updates
     */
    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if(MC.world != null &&
                event.getEntityLiving().equals(MC.player)) {
            Event ev = new LocalPlayerUpdateEvent(event.getEntityLiving());
            MinecraftForge.EVENT_BUS.post(ev);
            event.setCanceled(ev.isCanceled());
        } else if(event.getEntityLiving() instanceof EntityPigZombie) {
            // update pigmens anger level
            if(((EntityPigZombie) event.getEntityLiving()).isAngry())
                FastReflection.Fields.EntityPigZombie_angerLevel.set(event.getEntity(), FastReflection.Fields.EntityPigZombie_angerLevel.get(event.getEntity()) - 1);
        }
    }

    /**
     * For the world listener (adding/removing entity events)
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(WORLD_LISTENER);
        MinecraftForge.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        MinecraftForge.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
    }

    /**
     * Mod key bind handling
     */
    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        getModManager().getMods().forEach(mod -> mod.getKeyBinds().forEach(bind -> {
            if(bind.isPressed()) mod.onBindPressed(bind);
            if(bind.isKeyDown()) mod.onBindKeyDown(bind);
        }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSentPacket(PacketEvent.Outgoing.Post event) {
        if(Utils.OUTGOING_PACKET_IGNORE_LIST.contains(event.getPacket())) {
            // remove packet from list (we wont be seeing it ever again)
            Utils.OUTGOING_PACKET_IGNORE_LIST.remove(event.getPacket());
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();

        GlStateManager.glLineWidth(1.f);

        Vec3d renderPos = EntityUtils.getInterpolatedPos(getLocalPlayer(), event.getPartialTicks());
        tessellator.getBuffer().setTranslation(-renderPos.xCoord, -renderPos.yCoord, -renderPos.zCoord);

        MinecraftForge.EVENT_BUS.post(new RenderEvent(tessellator, renderPos));

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
