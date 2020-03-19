package dev.fiki.forgehax.common;

import dev.fiki.forgehax.common.events.*;
import dev.fiki.forgehax.common.events.boat.ClampBoatEvent;
import dev.fiki.forgehax.common.events.boat.RowBoatEvent;
import dev.fiki.forgehax.common.events.movement.*;
import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.common.events.render.CullCavesEvent;
import dev.fiki.forgehax.common.events.render.HurtCamEffectEvent;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class ForgeHaxHooks {
  private static ClientPlayerEntity clientPlayerEntity() {
    return Minecraft.getInstance().player;
  }

  /** static hooks */

  /**
   * Convenient functions for firing events
   */
  public static void fireEvent_v(Event event) {
    MinecraftForge.EVENT_BUS.post(event);
  }

  public static boolean fireEvent_b(Event event) {
    return MinecraftForge.EVENT_BUS.post(event);
  }

  public static void onDrawBoundingBoxPost() {
    MinecraftForge.EVENT_BUS.post(new DrawBlockBoundingBoxEvent.Post());
  }

  public static float onRenderBoat(BoatEntity boat, float entityYaw) {
    RenderBoatEvent event = new RenderBoatEvent(boat, entityYaw);
    MinecraftForge.EVENT_BUS.post(event);
    return event.getYaw();
  }

  public static boolean onBoatApplyGravity(BoatEntity boat) {
    return true;
  }

  public static void onSchematicaPlaceBlock(ItemStack itemIn, BlockPos posIn, Vec3d vecIn, Direction sideIn) {
    MinecraftForge.EVENT_BUS.post(new SchematicaPlaceBlockEvent(itemIn, posIn, vecIn, sideIn));
  }

  public static boolean shouldStopHurtcamEffect() {
    return MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent());
  }

  public static boolean onPacketOutbound(NetworkManager nm, IPacket<?> packet) {
    return MinecraftForge.EVENT_BUS.post(new PacketOutboundEvent(nm, packet));
  }

  public static boolean onPacketInbound(NetworkManager nm, IPacket<?> packet) {
    return MinecraftForge.EVENT_BUS.post(new PacketInboundEvent(nm, packet));
  }

  public static boolean shouldBePushedByLiquid(PlayerEntity entity) {
    // push player provided that
    // - entity is not the local player
    // - OR hook is not disabled
    return clientPlayerEntity() != entity
        || !MinecraftForge.EVENT_BUS.post(new PushedByLiquidEvent(entity));
  }

  public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity,
      double x, double z) {
    // TODO: fix logic here
    return (clientPlayerEntity() != entity && clientPlayerEntity() != collidedWithEntity)
        || MinecraftForge.EVENT_BUS.post(new ApplyCollisionMotionEvent(entity, collidedWithEntity, x, 0.d, z));
  }

  public static boolean shouldApplyBlockEntityCollisions(Entity entity, BlockState state) {
    return Minecraft.getInstance().player != entity
        || !MinecraftForge.EVENT_BUS.post(new BlockEntityCollisionEvent(entity, state));
  }

  public static boolean shouldDisableCaveCulling() {
    return MinecraftForge.EVENT_BUS.post(new CullCavesEvent());
  }

  public static boolean onUpdateWalkingPlayerPre(ClientPlayerEntity localPlayer) {
    return MinecraftForge.EVENT_BUS.post(new PrePlayerMovementUpdateEvent(localPlayer));
  }

  public static void onUpdateWalkingPlayerPost(ClientPlayerEntity localPlayer) {
    MinecraftForge.EVENT_BUS.post(new PostPlayerMovementUpdateEvent(localPlayer));
  }

  public static int onLeftClickCounterSet(int value, Minecraft minecraft) {
    LeftClickCounterUpdateEvent event = new LeftClickCounterUpdateEvent(minecraft, value);
    MinecraftForge.EVENT_BUS.post(event);
    return event.getValue();
  }

  public static boolean onSendClickBlockToController(Minecraft minecraft, boolean clicked) {
    BlockControllerProcessEvent event = new BlockControllerProcessEvent(minecraft, clicked);
    MinecraftForge.EVENT_BUS.post(event);
    return event.isLeftClicked();
  }

  public static void onPlayerItemSync(PlayerController playerControllerMP) {
    MinecraftForge.EVENT_BUS.post(new PlayerSyncItemEvent(playerControllerMP));
  }

  public static void onPlayerBreakingBlock(PlayerController playerControllerMP,
      BlockPos pos, Direction facing) {
    MinecraftForge.EVENT_BUS.post(new PlayerDamageBlockEvent(playerControllerMP, pos, facing));
  }

  public static void onPlayerAttackEntity(PlayerController playerControllerMP,
      PlayerEntity attacker, Entity victim) {
    MinecraftForge.EVENT_BUS.post(new PlayerAttackEntityEvent(playerControllerMP, attacker, victim));
  }

  public static boolean onPlayerStopUse(PlayerController playerControllerMP, PlayerEntity player) {
    return MinecraftForge.EVENT_BUS.post(new ItemStoppedUsedEvent(playerControllerMP, player));
  }

  public static float onEntityBlockSlipApply(float slipperiness,
      LivingEntity entityLivingBase, BlockPos blockPos) {
    EntityBlockSlipApplyEvent event = new EntityBlockSlipApplyEvent(entityLivingBase,
        entityLivingBase.world.getBlockState(blockPos), slipperiness);
    MinecraftForge.EVENT_BUS.post(event);
    return event.getSlipperiness();
  }

  public static boolean shouldClipBlockEdge(PlayerEntity player) {
    return clientPlayerEntity() == player
        && MinecraftForge.EVENT_BUS.post(new ClipBlockEdgeEvent());
  }

  public static boolean shouldApplyElytraMovement(boolean elytraFlying, LivingEntity living) {
    return clientPlayerEntity() == living
        ? (elytraFlying && !MinecraftForge.EVENT_BUS.post(new ElytraFlyMovementEvent()))
        : elytraFlying;
  }

  public static boolean shouldClampMotion(LivingEntity living) {
    return clientPlayerEntity() != living
        || !MinecraftForge.EVENT_BUS.post(new ClampMotionSpeedEvent());
  }

  public static boolean shouldSlowdownPlayer(ClientPlayerEntity entity) {
    return clientPlayerEntity() != entity
        || !MinecraftForge.EVENT_BUS.post(new PlayerSlowdownEvent());
  }

  public static boolean shouldClampBoat(BoatEntity boat) {
    return clientPlayerEntity() != boat.getControllingPassenger()
        || !MinecraftForge.EVENT_BUS.post(new ClampBoatEvent());
  }

  public static boolean shouldNotRowBoat(ClientPlayerEntity player) {
    return clientPlayerEntity() == player
        && MinecraftForge.EVENT_BUS.post(new RowBoatEvent());
  }

  public static boolean shouldIncreaseTabListSize() {
    return MinecraftForge.EVENT_BUS.post(new RestrictPlayerTablistSizeEvent());
  }
}
