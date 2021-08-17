package dev.fiki.forgehax.main.services;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.api.event.Event;
import dev.fiki.forgehax.api.events.ConnectToServerEvent;
import dev.fiki.forgehax.api.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.api.events.entity.LivingUpdateEvent;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.game.*;
import dev.fiki.forgehax.api.events.render.*;
import dev.fiki.forgehax.api.events.world.WorldLoadEvent;
import dev.fiki.forgehax.api.events.world.WorldUnloadEvent;
import dev.fiki.forgehax.api.math.VectorUtil;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionMethod;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
@RequiredArgsConstructor
@Mod.EventBusSubscriber(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public final class ForgeEventListenerService extends ServiceMod {
  @MapMethod(parentClass = GameRenderer.class, value = "bobHurt")
  private final ReflectionMethod<Void> GameRenderer_bobHurt;

  @Override
  protected void onEnabled() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  protected void onDisabled() {
    MinecraftForge.EVENT_BUS.unregister(this);
  }

  private static void setCanceledIfApplicable(net.minecraftforge.eventbus.api.Event event, boolean cancel) {
    if (event.isCancelable()) {
      event.setCanceled(cancel);
    }
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    if (event.getWorld() instanceof ClientWorld) {
      getEventBus().post(new WorldLoadEvent((ClientWorld) event.getWorld()));
    }
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Unload event) {
    if (event.getWorld() instanceof ClientWorld) {
      getEventBus().post(new WorldUnloadEvent((ClientWorld) event.getWorld()));
    }
  }

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    switch (event.phase) {
      case START:
        getEventBus().post(new PreGameTickEvent());
        break;
      case END:
        getEventBus().post(new PostGameTickEvent());
        break;
    }
  }

  @SubscribeEvent
  public void onRenderTick(TickEvent.RenderTickEvent event) {
    switch (event.phase) {
      case START:
        getEventBus().post(new PreRenderTickEvent());
        break;
      case END:
        getEventBus().post(new PostRenderTickEvent());
        break;
    }
  }

  @SubscribeEvent
  public void onRenderWorld(RenderWorldLastEvent event) {
    final GameRenderer gameRenderer = getGameRenderer();
    final ActiveRenderInfo activeRenderInfo = gameRenderer.getMainCamera();
    final float partialTicks = event.getPartialTicks();

    MatrixStack stack = new MatrixStack();
    stack.last().pose().multiply(gameRenderer.getProjectionMatrix(activeRenderInfo, partialTicks, true));
    GameRenderer_bobHurt.invoke(gameRenderer, stack, partialTicks);

    Matrix4f projectionMatrix = stack.last().pose();
    VectorUtil.setProjectionViewMatrix(projectionMatrix, event.getMatrixStack().last().pose());

    RenderSystem.pushMatrix();

    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.disableAlphaTest();
    RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    RenderSystem.shadeModel(GL11.GL_SMOOTH);
    RenderSystem.disableDepthTest();

    RenderSystem.lineWidth(1.f);

    Vector3d projectedView = activeRenderInfo.getPosition();
    getEventBus().post(new RenderSpaceEvent(event.getMatrixStack(), projectedView, partialTicks));

    RenderSystem.lineWidth(1.f);
    RenderSystem.color4f(1.f, 1.f, 1.f, 1.f);
    RenderSystem.shadeModel(GL11.GL_FLAT);
    RenderSystem.disableBlend();
    RenderSystem.enableAlphaTest();
    RenderSystem.enableTexture();
    RenderSystem.enableDepthTest();
    RenderSystem.enableCull();

    RenderSystem.popMatrix();
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRenderGameOverlay(RenderGameOverlayEvent event) {
    switch (event.getType()) {
      case TEXT: {
        setCanceledIfApplicable(event, getEventBus().post(new RenderPlaneEvent.Back(
            event.getMatrixStack(), event.getPartialTicks())));
        break;
      }
      case ALL: {
        setCanceledIfApplicable(event, getEventBus().post(new RenderPlaneEvent.Top(
            event.getMatrixStack(), event.getPartialTicks())));
        break;
      }
      case HELMET: {
        setCanceledIfApplicable(event, getEventBus().post(new RenderPlaneEvent.Helmet(
            event.getMatrixStack(), event.getPartialTicks())));
        break;
      }
      case PORTAL: {
        setCanceledIfApplicable(event, getEventBus().post(new RenderPlaneEvent.Portal(
            event.getMatrixStack(), event.getPartialTicks())));
        break;
      }
    }
    RenderSystem.color4f(1.f, 1.f, 1.f, 1.f); // reset color
  }

  @SubscribeEvent
  public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
    event.setCanceled(getEventBus().post(new BlockOverlayRenderEvent()));
  }

  @SubscribeEvent
  public void onFogRender(EntityViewRenderEvent.FogDensity event) {
    FogDensityRenderEvent e = new FogDensityRenderEvent(event.getDensity());
    event.setCanceled(getEventBus().post(e));
    event.setDensity(e.getDensity());
  }

  @SubscribeEvent
  public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
    if (event.getEntityLiving() == getLocalPlayer()) {
      getEventBus().post(new LocalPlayerUpdateEvent(getLocalPlayer()));
    }
    getEventBus().post(new LivingUpdateEvent(event.getEntityLiving()));
  }

  @SubscribeEvent
  public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    getEventBus().post(new KeyInputEvent(event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers()));
  }

  @SubscribeEvent
  public void onMouseEvent(InputEvent.MouseInputEvent event) {
    getEventBus().post(new MouseInputEvent(event.getButton(), event.getAction(), event.getMods()));
  }

  @SubscribeEvent
  public void onGuiOpened(GuiOpenEvent event) {
    GuiChangedEvent e = new GuiChangedEvent(event.getGui());
    event.setCanceled(getEventBus().post(e));
    event.setGui(e.getGui());
  }

  @SubscribeEvent
  public void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
    Event e;
    if (event instanceof GuiScreenEvent.InitGuiEvent.Pre) {
      e = new GuiInitializeEvent.Pre(event.getGui(), event::addWidget, event::removeWidget, event.getWidgetList());
    } else {
      e = new GuiInitializeEvent.Post(event.getGui(), event::addWidget, event::removeWidget, event.getWidgetList());
    }

    getEventBus().post(e);

    if (event.isCancelable()) {
      event.setCanceled(e.isCanceled());
    }
  }

  @SubscribeEvent
  public void onGuiDraw(GuiScreenEvent.DrawScreenEvent event) {
    Event e = null;
    if (event instanceof GuiScreenEvent.DrawScreenEvent.Pre) {
      e = new GuiRenderEvent.Pre(event.getGui(), event.getMatrixStack(), event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
    } else if (event instanceof GuiScreenEvent.DrawScreenEvent.Post) {
      e = new GuiRenderEvent.Post(event.getGui(), event.getMatrixStack(), event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
    }

    if (e != null) {
      getEventBus().post(e);

      if (event.isCancelable()) {
        event.setCanceled(e.isCanceled());
      }
    }
  }

  @SubscribeEvent
  public void onGuiContainerDraw(GuiContainerEvent.DrawBackground event) {
    getEventBus().post(new GuiContainerRenderEvent.Background(event.getGuiContainer(), event.getMatrixStack(), event.getMouseX(),
        event.getMouseY(), MC.getDeltaFrameTime()));
  }

  @SubscribeEvent
  public void onLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
    getEventBus().post(new ConnectToServerEvent());
  }

  @SubscribeEvent
  public void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
    getEventBus().post(new DisconnectFromServerEvent());
  }

  @SubscribeEvent
  public void onEntityRender(RenderLivingEvent event) {
    Event e = null;
    if (event instanceof RenderLivingEvent.Pre) {
      e = new LivingRenderEvent.Pre(event.getRenderer(), event.getPartialRenderTick(), event.getMatrixStack(),
          event.getBuffers(), event.getLight(), event.getEntity());
    } else if (event instanceof RenderLivingEvent.Post) {
      e = new LivingRenderEvent.Post(event.getRenderer(), event.getPartialRenderTick(), event.getMatrixStack(),
          event.getBuffers(), event.getLight(), event.getEntity());
    }

    if (e != null) {
      getEventBus().post(e);

      if (event.isCancelable()) {
        event.setCanceled(e.isCanceled());
      }
    }
  }

  @SubscribeEvent
  public void onRenderTag(RenderNameplateEvent event) {
    if (getEventBus().post(new NametagRenderEvent(event.getEntity()))) {
      event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
    }
  }

  @SubscribeEvent
  public void onPreTooptipRender(RenderTooltipEvent event) {
    TooltipRenderEvent e = null;
    if (event instanceof RenderTooltipEvent.Pre) {
      e = new TooltipRenderEvent.Pre(event.getStack(), event.getLines(), event.getMatrixStack(),
          event.getX(), event.getY(), event.getFontRenderer());
    } else if (event instanceof RenderTooltipEvent.PostText) {
      e = new TooltipRenderEvent.Post(event.getStack(), event.getLines(), event.getMatrixStack(),
          event.getX(), event.getY(), event.getFontRenderer());
    }

    if (e != null) {
      getEventBus().post(e);

      if (event instanceof RenderTooltipEvent.Pre) {
        RenderTooltipEvent.Pre preEvent = (RenderTooltipEvent.Pre) event;
        preEvent.setFontRenderer(e.getFontRenderer());
        preEvent.setX(e.getX());
        preEvent.setY(e.getY());
      }

      if (event.isCancelable()) {
        event.setCanceled(e.isCanceled());
      }
    }
  }

  @SubscribeEvent
  public void onPlaySound(PlaySoundAtEntityEvent event) {
    EntitySoundEvent e = new EntitySoundEvent(event.getEntity(), event.getSound(), event.getCategory(), event.getVolume(), event.getPitch());
    getEventBus().post(e);

    event.setCategory(e.getCategory());
    event.setPitch(e.getPitch());
    event.setSound(e.getSound());
    event.setVolume(e.getVolume());
    event.setCanceled(e.isCanceled());
  }
}
