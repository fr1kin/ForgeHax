package dev.fiki.forgehax.main.mods.player;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.Switch.Handle;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.render.LivingRenderEvent;
import dev.fiki.forgehax.api.events.render.NametagRenderEvent;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.events.world.WorldLoadEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.mock.MockClientEntityPlayer;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "Freecam",
    description = "Freely look around you",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({LocalPlayerEx.class})
public class FreecamMod extends ToggleMod {
  private final ReflectionTools reflection;

  @MapField(parentClass = NetworkPlayerInfo.class, value = "gameMode")
  private final ReflectionField<GameType> NetworkPlayerInfo_gameMode;

  private final FloatSetting speed = newFloatSetting()
      .name("speed")
      .description("Movement speed")
      .defaultTo(0.05f)
      .build();

  private final Handle flying = LocalPlayerEx.getFlySwitch().createHandle(getName());

  private Vector3d pos = Vector3d.ZERO;
  private Angle angle = Angle.ZERO;

  private boolean isRidingEntity;
  private Entity ridingEntity;

  private MockClientEntityPlayer mockPlayer;

  private GameType previousGameType;

  private void setupMockPlayer() {
    if (!isInWorld() || mockPlayer != null) {
      return;
    }

    ClientPlayerEntity self = getLocalPlayer();

    if (isRidingEntity = self.getVehicle() != null) {
      ridingEntity = self.getVehicle();
      self.stopRiding();
    } else {
      pos = self.position();
    }

    pos = self.position();
    angle = self.getViewAngles();

    mockPlayer = new MockClientEntityPlayer(self);
    mockPlayer.mockFields();
    mockPlayer.mockInventory();

    mockPlayer.setDeltaMovement(0, 0, 0);

    mockPlayer.disableSwing();
    mockPlayer.disableInterpolation();

    previousGameType = getPlayerController().getPlayerMode();
    getPlayerController().setLocalMode(GameType.SPECTATOR);

    if (MC.getConnection() != null) {
      NetworkPlayerInfo info = MC.getConnection().getPlayerInfo(self.getGameProfile().getId());
      NetworkPlayerInfo_gameMode.set(info, GameType.SPECTATOR);
    }

    self.abilities.setFlyingSpeed(speed.getValue());
  }

  @Override
  public void onEnabled() {
    setupMockPlayer();
  }

  @Override
  public void onDisabled() {
    flying.disable();

    if (getLocalPlayer() == null || mockPlayer == null) {
      return;
    }

    getLocalPlayer().moveTo(pos.x(), pos.y(), pos.z(), angle.getYaw(), angle.getPitch());

    getLocalPlayer().noPhysics = false;
    getLocalPlayer().setDeltaMovement(0, 0, 0);

    if (isRidingEntity) {
      getLocalPlayer().startRiding(ridingEntity, true);
    }

    getPlayerController().setLocalMode(previousGameType);
    getLocalPlayer().setGameMode(previousGameType);

    if (MC.getConnection() != null) {
      NetworkPlayerInfo info = MC.getConnection().getPlayerInfo(getLocalPlayer().getGameProfile().getId());
      NetworkPlayerInfo_gameMode.set(info, previousGameType);
    }

    // cleanup
    mockPlayer = null;
    ridingEntity = null;
    previousGameType = null;
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (mockPlayer == null) {
      setupMockPlayer();
    }

    flying.enable();

    getLocalPlayer().noPhysics = true;
    reflection.Entity_onGround.set(getLocalPlayer(), false);
    getLocalPlayer().fallDistance = 0;
  }

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    if (mockPlayer != null) {
      MatrixStack stack = event.getStack();
      stack.pushPose();
      // mock player cant move so no need to lerp its pos and yaw
      Vector3d pos = mockPlayer.position().subtract(event.getProjectedPos());

      IRenderTypeBuffer.Impl buffer = MC.renderBuffers().bufferSource();

      RenderSystem.enableBlend();
      RenderSystem.color4f(1.f ,1.f ,1.f, 0.5f);

      MC.getEntityRenderDispatcher().render(mockPlayer,
          pos.x(), pos.y(), pos.z(), mockPlayer.yRot,
          event.getPartialTicks(), stack,
          buffer, MC.getEntityRenderDispatcher().getPackedLightCoords(mockPlayer, event.getPartialTicks()));

//      buffer.finish(RenderType.entitySolid(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//      buffer.finish(RenderType.entityCutout(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//      buffer.finish(RenderType.entityCutoutNoCull(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//      buffer.finish(RenderType.entitySmoothCutout(PlayerContainer.LOCATION_BLOCKS_TEXTURE));

//      RenderSystem.depthMask(true);

      RenderSystem.color4f(1.f ,1.f ,1.f, 1.0f);

      buffer.endBatch();
      stack.popPose();
//      RenderSystem.popMatrix();
    }
  }

  @SubscribeListener
  public void onWorldLoad(WorldLoadEvent event) {
    mockPlayer = null;
  }

  @SubscribeListener
  public void onPacketSend(PacketOutboundEvent event) {
    if(mockPlayer == null) return;

    if (event.getPacket() instanceof CPlayerPacket || event.getPacket() instanceof CInputPacket) {
      event.setCanceled(true);
    }
  }

  @SubscribeListener
  public void onPacketReceived(PacketInboundEvent event) {
    if (mockPlayer == null || getLocalPlayer() == null) {
      return;
    }

    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();
      pos = new Vector3d(packet.getX(), packet.getY(), packet.getZ());
      angle = Angle.degrees(packet.getXRot(), packet.getYRot());
      event.setCanceled(true);
    }
  }

  @SubscribeListener
  public void onEntityRender(LivingRenderEvent.Pre<?, ?> event) {
    if (mockPlayer != null
        && mockPlayer != event.getLiving()
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getLiving())) {
      event.setCanceled(true);
    }
  }

  @SubscribeListener
  public void onRenderTag(NametagRenderEvent event) {
    if (mockPlayer != null
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) {
      event.setCanceled(true);
    }
  }
}
