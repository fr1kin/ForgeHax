package dev.fiki.forgehax.common;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.common.events.*;
import dev.fiki.forgehax.common.events.movement.*;
import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.common.events.render.ComputeVisibilityEvent;
import dev.fiki.forgehax.common.events.render.HurtCamEffectEvent;
import dev.fiki.forgehax.common.events.render.ProjectionViewMatrixSetupEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
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
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.nio.ByteOrder;
import java.util.*;

public class ForgeHaxHooks {

  private static final List<HookReporter> ALL_REPORTERS = new ArrayList<>();

  public static List<HookReporter> getReporters() {
    return Collections.unmodifiableList(ALL_REPORTERS);
  }

  private static HookReporter.Builder newHookReporter() {
    return HookReporter.Builder.of()
        .parentClass(ForgeHaxHooks.class)
        .finalizeBy(ALL_REPORTERS::add);
  }

  /**
   * static fields
   */
  public static boolean isSafeWalkActivated = false;

  public static boolean isNoSlowDownActivated = false;

  public static boolean isNoBoatGravityActivated = false;
  public static boolean isNoClampingActivated = false;
  public static boolean isBoatSetYawActivated = false;
  public static boolean isNotRowingBoatActivated = false;

  public static boolean doIncreaseTabListSize = false;

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

  /**
   * onDrawBoundingBox
   */
  public static void onDrawBoundingBoxPost() {
    MinecraftForge.EVENT_BUS.post(new DrawBlockBoundingBoxEvent.Post());
  }

  public static void onSetupProjectionViewMatrix(MatrixStack stack, Matrix4f projection) {
    MinecraftForge.EVENT_BUS.post(new ProjectionViewMatrixSetupEvent(stack, projection));
  }

  /**
   * onPushOutOfBlocks
   */
  public static final HookReporter HOOK_onPushOutOfBlocks =
      newHookReporter()
          .hook("onPushOutOfBlocks")
          //.dependsOn(TypesMc.Methods.EntityPlayerSP_pushOutOfBlocks)
          .forgeEvent(PushOutOfBlocksEvent.class)
          .build();

  public static boolean onPushOutOfBlocks() {
    return HOOK_onPushOutOfBlocks.checkState()
        && MinecraftForge.EVENT_BUS.post(new PushOutOfBlocksEvent());
  }

  /**
   * onRenderBoat
   */
  public static final HookReporter HOOK_onRenderBoat =
      newHookReporter()
          .hook("onRenderBoat")
          //.dependsOn(TypesMc.Methods.RenderBoat_doRender)
          .forgeEvent(RenderBoatEvent.class)
          .build();

  public static float onRenderBoat(BoatEntity boat, float entityYaw) {
    if (HOOK_onRenderBoat.checkState()) {
      RenderBoatEvent event = new RenderBoatEvent(boat, entityYaw);
      MinecraftForge.EVENT_BUS.post(event);
      return event.getYaw();
    } else {
      return entityYaw;
    }
  }

  public static boolean onBoatApplyGravity(BoatEntity boat) {
    return true;
  }

  /**
   * onSchematicaPlaceBlock
   */
  public static final HookReporter HOOK_onSchematicaPlaceBlock =
      newHookReporter()
          .hook("onSchematicaPlaceBlock")
          //.dependsOn(TypesSpecial.Methods.SchematicPrinter_placeBlock)
          .forgeEvent(SchematicaPlaceBlockEvent.class)
          .build();

  public static void onSchematicaPlaceBlock(ItemStack itemIn, BlockPos posIn, Vec3d vecIn, Direction sideIn) {
    if (HOOK_onSchematicaPlaceBlock.checkState()) {
      MinecraftForge.EVENT_BUS.post(new SchematicaPlaceBlockEvent(itemIn, posIn, vecIn, sideIn));
    }
  }

  /**
   * onHurtcamEffect
   */
  public static final HookReporter HOOK_onHurtcamEffect =
      newHookReporter()
          .hook("onHurtcamEffect")
          //.dependsOn(TypesMc.Methods.EntityRenderer_hurtCameraEffect)
          .forgeEvent(HurtCamEffectEvent.class)
          .build();

  public static boolean onHurtcamEffect() {
    return HOOK_onHurtcamEffect.checkState()
        && MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent());
  }

  /**
   * onSendingPacket
   */
  public static final HookReporter HOOK_onPacketOutbound =
      newHookReporter()
          .hook("onPacketOutbound")
          //.dependsOn(TypesMc.Methods.NetworkManager_dispatchPacket)
          //.dependsOn(TypesMc.Methods.NetworkManager$4_run)
          .forgeEvent(PacketOutboundEvent.class)
          .build();

  public static boolean onPacketOutbound(NetworkManager nm, IPacket<?> packet) {
    return HOOK_onPacketOutbound.checkState()
        && MinecraftForge.EVENT_BUS.post(new PacketOutboundEvent(nm, packet));
  }

  /**
   * onSentPacket
   */
  public static final HookReporter HOOK_onPacketInbound =
      newHookReporter()
          .hook("onPacketInbound")
          //.dependsOn(TypesMc.Methods.NetworkManager_dispatchPacket)
          //.dependsOn(TypesMc.Methods.NetworkManager$4_run)
          .forgeEvent(PacketInboundEvent.class)
          .build();

  public static boolean onPacketInbound(NetworkManager nm, IPacket<?> packet) {
    return HOOK_onPacketInbound.checkState()
        && MinecraftForge.EVENT_BUS.post(new PacketInboundEvent(nm, packet));
  }

  /**
   * onWaterMovement
   */
  public static final HookReporter HOOK_onWaterMovement =
      newHookReporter()
          .hook("onWaterMovement")
          //.dependsOn(TypesMc.Methods.World_handleMaterialAcceleration)
          .forgeEvent(WaterMovementEvent.class)
          .build();

  public static boolean onWaterMovement(Entity entity, Vec3d moveDir) {
    return HOOK_onWaterMovement.checkState()
        && MinecraftForge.EVENT_BUS.post(new WaterMovementEvent(entity, moveDir));
  }

  /**
   * onApplyCollisionMotion
   */
  public static final HookReporter HOOK_onApplyCollisionMotion =
      newHookReporter()
          .hook("onApplyCollisionMotion")
          //.dependsOn(TypesMc.Methods.Entity_applyEntityCollision)
          .forgeEvent(ApplyCollisionMotionEvent.class)
          .build();

  public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity,
      double x, double z) {
    return HOOK_onApplyCollisionMotion.checkState()
        && MinecraftForge.EVENT_BUS.post(new ApplyCollisionMotionEvent(entity,
        collidedWithEntity, x, 0.D, z));
  }

  /**
   * onPutColorMultiplier
   */
  public static final HookReporter HOOK_onPutColorMultiplier =
      newHookReporter()
          .hook("onPutColorMultiplier")
          //.dependsOn(TypesMc.Methods.BufferBuilder_putColorMultiplier)
          .build();

  public static boolean SHOULD_UPDATE_ALPHA = false;
  public static float COLOR_MULTIPLIER_ALPHA = 150.f / 255.f;

  public static int onPutColorMultiplier(float r, float g, float b, int buffer, boolean[] flag) {
    flag[0] = SHOULD_UPDATE_ALPHA;
    if (HOOK_onPutColorMultiplier.checkState() && SHOULD_UPDATE_ALPHA) {
      if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
        int red = (int) ((float) (buffer & 255) * r);
        int green = (int) ((float) (buffer >> 8 & 255) * g);
        int blue = (int) ((float) (buffer >> 16 & 255) * b);
        int alpha = (int) (((float) (buffer >> 24 & 255) * COLOR_MULTIPLIER_ALPHA));
        buffer = alpha << 24 | blue << 16 | green << 8 | red;
      } else {
        int red = (int) ((float) (buffer >> 24 & 255) * r);
        int green = (int) ((float) (buffer >> 16 & 255) * g);
        int blue = (int) ((float) (buffer >> 8 & 255) * b);
        int alpha = (int) (((float) (buffer & 255) * COLOR_MULTIPLIER_ALPHA));
        buffer = red << 24 | green << 16 | blue << 8 | alpha;
      }
    }
    return buffer;
  }

  /**
   * onComputeVisibility
   */
  public static final HookReporter HOOK_onComputeVisibility =
      newHookReporter()
          .hook("onComputeVisibility")
          // no hook exists anymore
          .forgeEvent(ComputeVisibilityEvent.class)
          .build();

  @Deprecated
  public static void onComputeVisibility(VisGraph visGraph, SetVisibility setVisibility) {
    if (HOOK_onComputeVisibility.checkState()) {
      MinecraftForge.EVENT_BUS.post(new ComputeVisibilityEvent(visGraph, setVisibility));
    }
  }

  /**
   * onDoBlockCollisions
   */
  public static final HookReporter HOOK_onDoBlockCollisions =
      newHookReporter()
          .hook("onDoBlockCollisions")
          // no hook exists anymore
          .forgeEvent(DoBlockCollisionsEvent.class)
          .build();

  @Deprecated
  public static boolean onDoBlockCollisions(Entity entity, BlockPos pos, BlockState state) {
    return HOOK_onDoBlockCollisions.checkState()
        && MinecraftForge.EVENT_BUS.post(new DoBlockCollisionsEvent(entity, pos, state));
  }

  /**
   * isBlockFiltered
   */
  public static final HookReporter HOOK_isBlockFiltered =
      newHookReporter()
          .hook("isBlockFiltered")
          //.dependsOn(TypesMc.Methods.Entity_doBlockCollisions)
          .build();

  public static final Set<Class<? extends Block>> LIST_BLOCK_FILTER = new HashSet<>();

  public static boolean isBlockFiltered(Entity entity, BlockState state) {
    return HOOK_isBlockFiltered.checkState()
        && entity instanceof PlayerEntity
        && LIST_BLOCK_FILTER.contains(state.getBlock().getClass());
  }

  /**
   * onApplyClimbableBlockMovement
   */
  public static final HookReporter HOOK_onApplyClimbableBlockMovement =
      newHookReporter()
          .hook("onApplyClimbableBlockMovement")
          // no hook exists
          .forgeEvent(ApplyClimbableBlockMovement.class)
          .build();

  @Deprecated
  public static boolean onApplyClimbableBlockMovement(LivingEntity livingBase) {
    return HOOK_onApplyClimbableBlockMovement.checkState()
        && MinecraftForge.EVENT_BUS.post(new ApplyClimbableBlockMovement(livingBase));
  }

  /**
   * onAddCollisionBoxToList
   */
  public static final HookReporter HOOK_onAddCollisionBoxToList =
      newHookReporter()
          .hook("onGetCollisionShapeEvent")
          //.dependsOn(TypesMc.Methods.Block_addCollisionBoxToList)
          .forgeEvent(GetCollisionShapeEvent.class)
          .build();

  public static boolean onGetCollisionShapeEvent(Block block, BlockState state, IBlockReader reader, BlockPos pos) {
    return HOOK_onAddCollisionBoxToList.checkState()
        && MinecraftForge.EVENT_BUS.post(new GetCollisionShapeEvent(block, state, reader, pos));
  }

  /**
   * shouldDisableCaveCulling
   */
  public static final HookReporter HOOK_shouldDisableCaveCulling =
      newHookReporter()
          .hook("shouldDisableCaveCulling")
          //.dependsOn(TypesMc.Methods.RenderGlobal_setupTerrain)
          //.dependsOn(TypesMc.Methods.VisGraph_setOpaqueCube)
          //.dependsOn(TypesMc.Methods.VisGraph_computeVisibility)
          .build();

  public static boolean shouldDisableCaveCulling() {
    return HOOK_shouldDisableCaveCulling.checkState();
  }

  /**
   * onUpdateWalkingPlayerPre
   */
  public static final HookReporter HOOK_onUpdateWalkingPlayerPre =
      newHookReporter()
          .hook("onUpdateWalkingPlayerPre")
          //.dependsOn(TypesMc.Methods.EntityPlayerSP_onUpdateWalkingPlayer)
          .forgeEvent(PrePlayerMovementUpdateEvent.class)
          .build();

  public static boolean onUpdateWalkingPlayerPre(ClientPlayerEntity localPlayer) {
    return HOOK_onUpdateWalkingPlayerPre.checkState()
        && MinecraftForge.EVENT_BUS.post(new PrePlayerMovementUpdateEvent(localPlayer));
  }

  /**
   * onUpdateWalkingPlayerPost
   */
  public static final HookReporter HOOK_onUpdateWalkingPlayerPost =
      newHookReporter()
          .hook("onUpdateWalkingPlayerPost")
          //.dependsOn(TypesMc.Methods.EntityPlayerSP_onUpdateWalkingPlayer)
          .forgeEvent(PostPlayerMovementUpdateEvent.class)
          .build();

  public static void onUpdateWalkingPlayerPost(ClientPlayerEntity localPlayer) {
    if (HOOK_onUpdateWalkingPlayerPost.checkState()) {
      MinecraftForge.EVENT_BUS.post(new PostPlayerMovementUpdateEvent(localPlayer));
    }
  }

  /**
   * onLeftClickCounterSet
   */
  public static final HookReporter HOOK_onLeftClickCounterSet =
      newHookReporter()
          .hook("onLeftClickCounterSet")
          //.dependsOn(TypesMc.Methods.Minecraft_runTick)
          //.dependsOn(TypesMc.Methods.Minecraft_setIngameFocus)
          .forgeEvent(LeftClickCounterUpdateEvent.class)
          .build();

  public static int onLeftClickCounterSet(int value, Minecraft minecraft) {
    if (HOOK_onLeftClickCounterSet.checkState()) {
      LeftClickCounterUpdateEvent event = new LeftClickCounterUpdateEvent(minecraft, value);
      MinecraftForge.EVENT_BUS.post(event);
      return event.getValue();
    } else {
      return value;
    }
  }

  /**
   * onSendClickBlockToController
   */
  public static final HookReporter HOOK_onSendClickBlockToController =
      newHookReporter()
          .hook("onSendClickBlockToController")
          //.dependsOn(TypesMc.Methods.Minecraft_runTick)
          .forgeEvent(BlockControllerProcessEvent.class)
          .build();

  public static boolean onSendClickBlockToController(Minecraft minecraft, boolean clicked) {
    if (HOOK_onSendClickBlockToController.checkState()) {
      BlockControllerProcessEvent event = new BlockControllerProcessEvent(minecraft, clicked);
      MinecraftForge.EVENT_BUS.post(event);
      return event.isLeftClicked();
    } else {
      return clicked;
    }
  }

  /**
   * onPlayerItemSync
   */
  public static final HookReporter HOOK_onPlayerItemSync =
      newHookReporter()
          .hook("onPlayerItemSync")
          //.dependsOn(Methods.PlayerControllerMC_syncCurrentPlayItem)
          .forgeEvent(PlayerSyncItemEvent.class)
          .build();

  public static void onPlayerItemSync(PlayerController playerControllerMP) {
    if (HOOK_onPlayerItemSync.checkState()) {
      MinecraftForge.EVENT_BUS.post(new PlayerSyncItemEvent(playerControllerMP));
    }
  }

  /**
   * onPlayerBreakingBlock
   */
  public static final HookReporter HOOK_onPlayerBreakingBlock =
      newHookReporter()
          .hook("onPlayerBreakingBlock")
          //.dependsOn(Methods.PlayerControllerMC_onPlayerDamageBlock)
          .forgeEvent(PlayerDamageBlockEvent.class)
          .build();

  public static void onPlayerBreakingBlock(PlayerController playerControllerMP,
      BlockPos pos, Direction facing) {
    if (HOOK_onPlayerBreakingBlock.checkState()) {
      MinecraftForge.EVENT_BUS.post(new PlayerDamageBlockEvent(playerControllerMP, pos, facing));
    }
  }

  /**
   * onPlayerAttackEntity
   */
  public static final HookReporter HOOK_onPlayerAttackEntity =
      newHookReporter()
          .hook("onPlayerAttackEntity")
          //.dependsOn(Methods.PlayerControllerMC_attackEntity)
          .forgeEvent(PlayerAttackEntityEvent.class)
          .build();

  public static void onPlayerAttackEntity(PlayerController playerControllerMP,
      PlayerEntity attacker, Entity victim) {
    if (HOOK_onPlayerAttackEntity.checkState()) {
      MinecraftForge.EVENT_BUS.post(
          new PlayerAttackEntityEvent(playerControllerMP, attacker, victim));
    }
  }

  /**
   * onPlayerStopUse
   */
  public static final HookReporter HOOK_onPlayerStopUse =
      newHookReporter()
          .hook("onPlayerStopUse")
          //.dependsOn(Methods.PlayerControllerMC_onStoppedUsingItem)
          .forgeEvent(ItemStoppedUsedEvent.class)
          .build();

  public static boolean onPlayerStopUse(PlayerController playerControllerMP, PlayerEntity player) {
    return HOOK_onPlayerStopUse.checkState()
        && MinecraftForge.EVENT_BUS.post(new ItemStoppedUsedEvent(playerControllerMP, player));
  }

  /**
   * onPlayerStopUse
   */
  public static final HookReporter HOOK_onEntityBlockSlipApply =
      newHookReporter()
          .hook("onEntityBlockSlipApply")
          //.dependsOn(Methods.PlayerControllerMC_onStoppedUsingItem)
          .forgeEvent(EntityBlockSlipApplyEvent.class)
          .build();

  public static float onEntityBlockSlipApply(float slipperiness,
      LivingEntity entityLivingBase, BlockPos blockPos) {
    if (HOOK_onEntityBlockSlipApply.checkState()) {
      EntityBlockSlipApplyEvent event = new EntityBlockSlipApplyEvent(entityLivingBase,
          entityLivingBase.world.getBlockState(blockPos), slipperiness);
      MinecraftForge.EVENT_BUS.post(event);
      return event.getSlipperiness();
    } else {
      return slipperiness;
    }
  }

  public static final HookReporter HOOK_onPlayerEntitySneakEdgeCheck =
      newHookReporter()
          .hook("onPlayerEntitySneakEdgeCheck")
          .build();

  public static boolean onPlayerEntitySneakEdgeCheck(PlayerEntity player) {
    return Minecraft.getInstance().player == player
        && HOOK_onPlayerEntitySneakEdgeCheck.checkState();
  }

  public static final HookReporter HOOK_shouldApplyElytraMovement =
      newHookReporter()
          .hook("shouldApplyElytraMovement")
          .build();

  public static boolean shouldApplyElytraMovement(boolean elytraFlying, LivingEntity living) {
    if(Minecraft.getInstance().player == living) {
      return elytraFlying && !HOOK_shouldApplyElytraMovement.checkState();
    } else {
      return elytraFlying;
    }
  }

  public static final HookReporter HOOK_shouldClampMotion =
      newHookReporter()
          .hook("shouldClampMotion")
          .build();

  public static boolean shouldClampMotion(LivingEntity living) {
    return Minecraft.getInstance().player == living
        && !HOOK_shouldClampMotion.checkState();
  }
}
