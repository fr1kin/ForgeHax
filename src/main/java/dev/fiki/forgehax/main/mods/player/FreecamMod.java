package dev.fiki.forgehax.main.mods.player;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.util.Switch.Handle;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.events.ClientWorldEvent;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.events.RenderEvent;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.mock.MockClientEntityPlayer;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.ReflectionTools;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "Freecam",
    description = "Freely look around you",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class FreecamMod extends ToggleMod {
  private final ReflectionTools reflection;

  @FieldMapping(parentClass = NetworkPlayerInfo.class, value = "gameType")
  private final ReflectionField<GameType> NetworkPlayerInfo_gameType;

  private final FloatSetting speed = newFloatSetting()
      .name("speed")
      .description("Movement speed")
      .defaultTo(0.05f)
      .build();

  private final Handle flying = LocalPlayerUtils.getFlySwitch().createHandle(getName());

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

    if (isRidingEntity = self.getRidingEntity() != null) {
      ridingEntity = self.getRidingEntity();
      self.stopRiding();
    } else {
      pos = self.getPositionVec();
    }

    pos = self.getPositionVec();
    angle = LocalPlayerUtils.getViewAngles();

    mockPlayer = new MockClientEntityPlayer(self);
    mockPlayer.mockFields();
    mockPlayer.mockInventory();

    mockPlayer.setVelocity(0, 0, 0);

    mockPlayer.disableSwing();
    mockPlayer.disableInterpolation();

    previousGameType = getPlayerController().getCurrentGameType();
    getPlayerController().setGameType(GameType.SPECTATOR);

    if (MC.getConnection() != null) {
      NetworkPlayerInfo info = MC.getConnection().getPlayerInfo(self.getGameProfile().getId());
      NetworkPlayerInfo_gameType.set(info, GameType.SPECTATOR);
    }

    self.abilities.setFlySpeed(speed.getValue());
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

    getLocalPlayer().setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), angle.getYaw(), angle.getPitch());

    getLocalPlayer().noClip = false;
    getLocalPlayer().setVelocity(0, 0, 0);

    if (isRidingEntity) {
      getLocalPlayer().startRiding(ridingEntity, true);
    }

    getPlayerController().setGameType(previousGameType);
    getLocalPlayer().setGameType(previousGameType);

    if (MC.getConnection() != null) {
      NetworkPlayerInfo info = MC.getConnection().getPlayerInfo(getLocalPlayer().getGameProfile().getId());
      NetworkPlayerInfo_gameType.set(info, previousGameType);
    }

    // cleanup
    mockPlayer = null;
    ridingEntity = null;
    previousGameType = null;
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (mockPlayer == null) {
      setupMockPlayer();
    }

    flying.enable();

    getLocalPlayer().noClip = true;
    reflection.Entity_onGround.set(getLocalPlayer(), false);
    getLocalPlayer().fallDistance = 0;
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if (mockPlayer != null) {
      // mock player cant move so no need to lerp its pos and yaw
      Vector3d pos = mockPlayer.getPositionVec().subtract(event.getProjectedPos());

      IRenderTypeBuffer.Impl buffer = MC.getRenderTypeBuffers().getBufferSource();

      RenderSystem.enableBlend();
      RenderSystem.color4f(1.f ,1.f ,1.f, 0.5f);

      MC.getRenderManager().renderEntityStatic(mockPlayer,
          pos.getX(), pos.getY(), pos.getZ(), mockPlayer.rotationYaw,
          event.getPartialTicks(), new MatrixStack(),
          buffer, MC.getRenderManager().getPackedLight(mockPlayer, event.getPartialTicks()));

//      buffer.finish(RenderType.entitySolid(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//      buffer.finish(RenderType.entityCutout(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//      buffer.finish(RenderType.entityCutoutNoCull(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//      buffer.finish(RenderType.entitySmoothCutout(PlayerContainer.LOCATION_BLOCKS_TEXTURE));

//      RenderSystem.depthMask(true);

      RenderSystem.color4f(1.f ,1.f ,1.f, 1.0f);

      buffer.finish();
    }
  }

  @SubscribeEvent
  public void onWorldLoad(ClientWorldEvent.Load event) {
    mockPlayer = null;
  }

  @SubscribeEvent
  public void onPacketSend(PacketOutboundEvent event) {
    if(mockPlayer == null) return;

    if (event.getPacket() instanceof CPlayerPacket || event.getPacket() instanceof CInputPacket) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (mockPlayer == null || getLocalPlayer() == null) {
      return;
    }

    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();
      pos = new Vector3d(packet.getX(), packet.getY(), packet.getZ());
      angle = Angle.degrees(packet.getPitch(), packet.getYaw());
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
    if (mockPlayer != null
        && mockPlayer != event.getEntity()
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onRenderTag(RenderNameplateEvent event) {
    if (mockPlayer != null
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) {
      event.setResult(Event.Result.DENY);
    }
  }
}
