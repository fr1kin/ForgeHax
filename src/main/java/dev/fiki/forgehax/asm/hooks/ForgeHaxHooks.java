package dev.fiki.forgehax.asm.hooks;

import dev.fiki.forgehax.api.event.Event;
import dev.fiki.forgehax.api.event.EventBus;
import dev.fiki.forgehax.api.event.EventListener;
import dev.fiki.forgehax.api.event.ListenerFlags;
import dev.fiki.forgehax.asm.events.SchematicaPlaceBlockEvent;
import dev.fiki.forgehax.asm.events.boat.ClampBoatEvent;
import dev.fiki.forgehax.asm.events.boat.RenderBoatEvent;
import dev.fiki.forgehax.asm.events.boat.RowBoatEvent;
import dev.fiki.forgehax.asm.events.game.BlockControllerProcessEvent;
import dev.fiki.forgehax.asm.events.game.ItemStoppedUsedEvent;
import dev.fiki.forgehax.asm.events.game.LeftClickCounterUpdateEvent;
import dev.fiki.forgehax.asm.events.game.RestrictPlayerTablistSizeEvent;
import dev.fiki.forgehax.asm.events.movement.*;
import dev.fiki.forgehax.asm.events.packet.PacketEvent;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.asm.events.player.PlayerAttackEntityEvent;
import dev.fiki.forgehax.asm.events.player.PlayerDamageBlockEvent;
import dev.fiki.forgehax.asm.events.player.PlayerSyncItemEvent;
import dev.fiki.forgehax.asm.events.render.CullCavesEvent;
import dev.fiki.forgehax.asm.events.render.DrawBlockBoundingBoxEvent;
import dev.fiki.forgehax.asm.events.render.HurtCamEffectEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class ForgeHaxHooks {
  static final EventBus EVENT_BUS = new EventBus();

  private static ClientPlayerEntity clientPlayerEntity() {
    return Minecraft.getInstance().player;
  }

  /** static hooks */

  /**
   * Convenient functions for firing events
   */
  public static void fireEvent_v(Event event) {
    EVENT_BUS.post(event);
  }

  public static boolean fireEvent_b(Event event) {
    return EVENT_BUS.post(event);
  }

  public static void onDrawBoundingBoxPost() {
    EVENT_BUS.post(new DrawBlockBoundingBoxEvent.Post());
  }

  public static float onRenderBoat(BoatEntity boat, float entityYaw) {
    RenderBoatEvent event = new RenderBoatEvent(boat, entityYaw);
    EVENT_BUS.post(event);
    return event.getYaw();
  }

  public static boolean onBoatApplyGravity(BoatEntity boat) {
    return true;
  }

  public static void onSchematicaPlaceBlock(ItemStack itemIn, BlockPos posIn, Vector3d vecIn, Direction sideIn) {
    EVENT_BUS.post(new SchematicaPlaceBlockEvent(itemIn, posIn, vecIn, sideIn));
  }

  public static boolean shouldStopHurtcamEffect() {
    return EVENT_BUS.post(new HurtCamEffectEvent());
  }

  public static boolean onPacketOutbound(NetworkManager nm, IPacket<?> packet) {
    PacketOutboundEvent event = new PacketOutboundEvent(nm, packet);
    for (EventListener listener : event.getListenerList()) {
      if (ListenerFlags.present(listener, ListenerFlags.ALLOW_CANCELED) || !event.isCanceled()) {
        if (ListenerFlags.present(listener, ListenerFlags.ALLOW_IGNORED_PACKETS) || !event.isIgnored()) {
          listener.run(event);
        }
      }
    }

    if (PacketEvent.isIgnored(packet)) {
      PacketEvent.remove(packet);
    }

    return event.isCanceled();
  }

  public static boolean onPacketInbound(NetworkManager nm, IPacket<?> packet) {
    PacketInboundEvent event = new PacketInboundEvent(nm, packet);
    for (EventListener listener : event.getListenerList()) {
      if (ListenerFlags.present(listener, ListenerFlags.ALLOW_CANCELED) || !event.isCanceled()) {
        if (ListenerFlags.present(listener, ListenerFlags.ALLOW_IGNORED_PACKETS) || !event.isIgnored()) {
          listener.run(event);
        }
      }
    }

    if (PacketEvent.isIgnored(packet)) {
      PacketEvent.remove(packet);
    }

    return event.isCanceled();
  }

  public static boolean shouldBePushedByLiquid(PlayerEntity entity) {
    // push player provided that
    // - entity is not the local player
    // - OR hook is not disabled
    return clientPlayerEntity() != entity
        || !EVENT_BUS.post(new PushedByLiquidEvent(entity));
  }

  public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity,
      double x, double z) {
    // TODO: fix logic here
    return (clientPlayerEntity() != entity && clientPlayerEntity() != collidedWithEntity)
        || EVENT_BUS.post(new ApplyCollisionMotionEvent(entity, collidedWithEntity, x, 0.d, z));
  }

  public static boolean shouldApplyBlockEntityCollisions(Entity entity, BlockState state) {
    return clientPlayerEntity() != entity
        || !EVENT_BUS.post(new BlockEntityCollisionEvent(entity, state));
  }

  public static boolean shouldDisableCaveCulling() {
    return EVENT_BUS.post(new CullCavesEvent());
  }

  public static boolean onUpdateWalkingPlayerPre(ClientPlayerEntity localPlayer) {
    return EVENT_BUS.post(new PreMovementUpdateEvent(localPlayer));
  }

  public static void onUpdateWalkingPlayerPost(ClientPlayerEntity localPlayer) {
    EVENT_BUS.post(new PostMovementUpdateEvent(localPlayer));
  }

  public static int onLeftClickCounterSet(int value, Minecraft minecraft) {
    LeftClickCounterUpdateEvent event = new LeftClickCounterUpdateEvent(minecraft, value);
    EVENT_BUS.post(event);
    return event.getValue();
  }

  public static boolean onSendClickBlockToController(Minecraft minecraft, boolean clicked) {
    BlockControllerProcessEvent event = new BlockControllerProcessEvent(minecraft, clicked);
    EVENT_BUS.post(event);
    return event.isLeftClicked();
  }

  public static void onPlayerItemSync(PlayerController playerControllerMP) {
    EVENT_BUS.post(new PlayerSyncItemEvent(playerControllerMP));
  }

  public static void onPlayerBreakingBlock(PlayerController playerControllerMP,
      BlockPos pos, Direction facing) {
    EVENT_BUS.post(new PlayerDamageBlockEvent(playerControllerMP, pos, facing));
  }

  public static void onPlayerAttackEntity(PlayerController playerControllerMP,
      PlayerEntity attacker, Entity victim) {
    EVENT_BUS.post(new PlayerAttackEntityEvent(playerControllerMP, attacker, victim));
  }

  public static boolean onPlayerStopUse(PlayerController playerControllerMP, PlayerEntity player) {
    return EVENT_BUS.post(new ItemStoppedUsedEvent(playerControllerMP, player));
  }

  public static float onEntityBlockSlipApply(float slipperiness,
      LivingEntity entityLivingBase, BlockPos blockPos) {
    EntityBlockSlipApplyEvent event = new EntityBlockSlipApplyEvent(entityLivingBase,
        entityLivingBase.level.getBlockState(blockPos), slipperiness);
    EVENT_BUS.post(event);
    return event.getSlipperiness();
  }

  public static boolean shouldClipBlockEdge(PlayerEntity player) {
    return clientPlayerEntity() == player
        && EVENT_BUS.post(new ClipBlockEdgeEvent());
  }

  public static boolean shouldApplyElytraMovement(boolean elytraFlying, LivingEntity living) {
    return clientPlayerEntity() == living
        ? (elytraFlying && !EVENT_BUS.post(new ElytraFlyMovementEvent()))
        : elytraFlying;
  }

  public static boolean shouldClampMotion(LivingEntity living) {
    return clientPlayerEntity() != living
        || !EVENT_BUS.post(new ClampMotionSpeedEvent());
  }

  public static boolean shouldSlowdownPlayer(ClientPlayerEntity entity) {
    return clientPlayerEntity() != entity
        || !EVENT_BUS.post(new PlayerSlowdownEvent());
  }

  public static boolean shouldClampBoat(BoatEntity boat) {
    return clientPlayerEntity() != boat.getControllingPassenger()
        || !EVENT_BUS.post(new ClampBoatEvent());
  }

  public static boolean shouldNotRowBoat(ClientPlayerEntity player) {
    return clientPlayerEntity() == player
        && EVENT_BUS.post(new RowBoatEvent());
  }

  public static boolean shouldIncreaseTabListSize() {
    return EVENT_BUS.post(new RestrictPlayerTablistSizeEvent());
  }
}
