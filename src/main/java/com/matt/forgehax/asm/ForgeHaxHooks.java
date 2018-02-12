package com.matt.forgehax.asm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.matt.forgehax.asm.events.*;
import com.matt.forgehax.asm.events.listeners.BlockModelRenderListener;
import com.matt.forgehax.asm.events.listeners.Listeners;
import com.matt.forgehax.asm.utils.MultiBoolean;
import com.matt.forgehax.asm.utils.debug.HookReporter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ForgeHaxHooks implements ASMCommon {
    private static final List<HookReporter> ALL_REPORTERS = Lists.newArrayList();

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

    /**
     * static hooks
     */

    /**
     * onPushOutOfBlocks
     */
    public static final HookReporter HOOK_onPushOutOfBlocks = newHookReporter()
            .hook("onPushOutOfBlocks")
            .dependsOn(TypesMc.Methods.EntityPlayerSP_pushOutOfBlocks)
            .forgeEvent(PushOutOfBlocksEvent.class)
            .build();
    public static boolean onPushOutOfBlocks() {
        return HOOK_onPushOutOfBlocks.reportHook() && MinecraftForge.EVENT_BUS.post(new PushOutOfBlocksEvent());
    }

    /**
     * onRenderBoat
     */
    public static final HookReporter HOOK_onRenderBoat = newHookReporter()
            .hook("onRenderBoat")
            .dependsOn(TypesMc.Methods.RenderBoat_doRender)
            .forgeEvent(RenderBoatEvent.class)
            .build();
    public static float onRenderBoat(EntityBoat boat, float entityYaw) {
        if(HOOK_onRenderBoat.reportHook()) {
            RenderBoatEvent event = new RenderBoatEvent(boat, entityYaw);
            MinecraftForge.EVENT_BUS.post(event);
            return event.getYaw();
        } else return entityYaw;
    }

    /**
     * onSchematicaPlaceBlock
     */
    public static final HookReporter HOOK_onSchematicaPlaceBlock = newHookReporter()
            .hook("onSchematicaPlaceBlock")
            .dependsOn(TypesSpecial.Methods.SchematicPrinter_placeBlock)
            .forgeEvent(SchematicaPlaceBlockEvent.class)
            .build();
    public static void onSchematicaPlaceBlock(ItemStack itemIn, BlockPos posIn, Vec3d vecIn) {
        if(HOOK_onSchematicaPlaceBlock.reportHook()) MinecraftForge.EVENT_BUS.post(new SchematicaPlaceBlockEvent(itemIn, posIn, vecIn));
    }

    /**
     * onHurtcamEffect
     */
    public static final HookReporter HOOK_onHurtcamEffect = newHookReporter()
            .hook("onHurtcamEffect")
            .dependsOn(TypesMc.Methods.EntityRenderer_hurtCameraEffect)
            .forgeEvent(HurtCamEffectEvent.class)
            .build();
    public static boolean onHurtcamEffect(float partialTicks) {
        return HOOK_onHurtcamEffect.reportHook() && MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent(partialTicks));
    }

    /**
     * onSendingPacket
     */
    public static final HookReporter HOOK_onSendingPacket = newHookReporter()
            .hook("onSendingPacket")
            .dependsOn(TypesMc.Methods.NetworkManager_dispatchPacket)
            .dependsOn(TypesMc.Methods.NetworkManager$4_run)
            .forgeEvent(PacketEvent.Outgoing.Pre.class)
            .build();
    public static boolean onSendingPacket(Packet<?> packet) {
        return HOOK_onSendingPacket.reportHook() && MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Pre(packet));
    }

    /**
     * onSentPacket
     */
    public static final HookReporter HOOK_onSentPacket = newHookReporter()
            .hook("onSentPacket")
            .dependsOn(TypesMc.Methods.NetworkManager_dispatchPacket)
            .dependsOn(TypesMc.Methods.NetworkManager$4_run)
            .forgeEvent(PacketEvent.Outgoing.Post.class)
            .build();
    public static void onSentPacket(Packet<?> packet) {
        if(HOOK_onSentPacket.reportHook()) MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Post(packet));
    }

    /**
     * onPreReceived
     */
    public static final HookReporter HOOK_onPreReceived = newHookReporter()
            .hook("onPreReceived")
            .dependsOn(TypesMc.Methods.NetworkManager_channelRead0)
            .forgeEvent(PacketEvent.Incoming.Pre.class)
            .build();
    public static boolean onPreReceived(Packet<?> packet) {
        return HOOK_onPreReceived.reportHook() && MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Pre(packet));
    }

    /**
     * onPostReceived
     */
    public static final HookReporter HOOK_onPostReceived = newHookReporter()
            .hook("onPostReceived")
            .dependsOn(TypesMc.Methods.NetworkManager_channelRead0)
            .forgeEvent(PacketEvent.Incoming.Post.class)
            .build();
    public static void onPostReceived(Packet<?> packet) {
        if(HOOK_onPostReceived.reportHook()) MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Post(packet));
    }

    /**
     * onWaterMovement
     */
    public static final HookReporter HOOK_onWaterMovement = newHookReporter()
            .hook("onWaterMovement")
            .dependsOn(TypesMc.Methods.World_handleMaterialAcceleration)
            .forgeEvent(WaterMovementEvent.class)
            .build();
    public static boolean onWaterMovement(Entity entity, Vec3d moveDir) {
        return HOOK_onWaterMovement.reportHook() && MinecraftForge.EVENT_BUS.post(new WaterMovementEvent(entity, moveDir));
    }

    /**
     * onApplyCollisionMotion
     */
    public static final HookReporter HOOK_onApplyCollisionMotion = newHookReporter()
            .hook("onApplyCollisionMotion")
            .dependsOn(TypesMc.Methods.Entity_applyEntityCollision)
            .forgeEvent(ApplyCollisionMotionEvent.class)
            .build();
    public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity, double x, double z) {
        return HOOK_onApplyCollisionMotion.reportHook() && MinecraftForge.EVENT_BUS.post(new ApplyCollisionMotionEvent(entity, collidedWithEntity, x, 0.D, z));
    }

    /**
     * onPutColorMultiplier
     */
    public static final HookReporter HOOK_onPutColorMultiplier = newHookReporter()
            .hook("onPutColorMultiplier")
            .dependsOn(TypesMc.Methods.BufferBuilder_putColorMultiplier)
            .build();

    public static boolean SHOULD_UPDATE_ALPHA = false;
    public static float COLOR_MULTIPLIER_ALPHA = 150.f / 255.f;

    public static int onPutColorMultiplier(float r, float g, float b, int buffer, boolean[] flag) {
        flag[0] = SHOULD_UPDATE_ALPHA;
        if(HOOK_onPutColorMultiplier.reportHook() && SHOULD_UPDATE_ALPHA) {
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
     * onPreRenderBlockLayer
     */
    public static final HookReporter HOOK_onPreRenderBlockLayer = newHookReporter()
            .hook("onPreRenderBlockLayer")
            .dependsOn(TypesMc.Methods.RenderGlobal_renderBlockLayer)
            .forgeEvent(RenderBlockLayerEvent.Pre.class)
            .build();
    public static boolean onPreRenderBlockLayer(BlockRenderLayer layer, double partialTicks) {
        return HOOK_onPreRenderBlockLayer.reportHook() && MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Pre(layer, partialTicks));
    }

    /**
     * onPostRenderBlockLayer
     */
    public static final HookReporter HOOK_onPostRenderBlockLayer = newHookReporter()
            .hook("onPostRenderBlockLayer")
            .dependsOn(TypesMc.Methods.RenderGlobal_renderBlockLayer)
            .forgeEvent(RenderBlockLayerEvent.Post.class)
            .build();
    public static void onPostRenderBlockLayer(BlockRenderLayer layer, double partialTicks) {
        if(HOOK_onPostRenderBlockLayer.reportHook()) MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Post(layer, partialTicks));
    }

    /**
     * onSetupTerrain
     */
    public static final HookReporter HOOK_onSetupTerrain = newHookReporter()
            .hook("onSetupTerrain")
            .dependsOn(TypesMc.Methods.RenderGlobal_setupTerrain)
            .forgeEvent(SetupTerrainEvent.class)
            .build();
    public static boolean onSetupTerrain(Entity renderEntity, boolean playerSpectator) {
        if(HOOK_onSetupTerrain.reportHook()) {
            SetupTerrainEvent event = new SetupTerrainEvent(renderEntity, playerSpectator);
            MinecraftForge.EVENT_BUS.post(event);
            return event.isCulling();
        } else return playerSpectator;
    }

    /**
     * onComputeVisibility
     */
    public static final HookReporter HOOK_onComputeVisibility = newHookReporter()
            .hook("onComputeVisibility")
            // no hook exists anymore
            .forgeEvent(ComputeVisibilityEvent.class)
            .build();
    @Deprecated
    public static void onComputeVisibility(VisGraph visGraph, SetVisibility setVisibility) {
        if(HOOK_onComputeVisibility.reportHook()) MinecraftForge.EVENT_BUS.post(new ComputeVisibilityEvent(visGraph, setVisibility));
    }

    /**
     * onDoBlockCollisions
     */
    public static final HookReporter HOOK_onDoBlockCollisions = newHookReporter()
            .hook("onDoBlockCollisions")
            // no hook exists anymore
            .forgeEvent(DoBlockCollisionsEvent.class)
            .build();
    @Deprecated
    public static boolean onDoBlockCollisions(Entity entity, BlockPos pos, IBlockState state) {
        return HOOK_onDoBlockCollisions.reportHook() && MinecraftForge.EVENT_BUS.post(new DoBlockCollisionsEvent(entity, pos, state));
    }

    /**
     * isBlockFiltered
     */
    public static final HookReporter HOOK_isBlockFiltered = newHookReporter()
            .hook("isBlockFiltered")
            .dependsOn(TypesMc.Methods.Entity_doBlockCollisions)
            .build();

    public static final Set<Class<? extends Block>> LIST_BLOCK_FILTER = Sets.newHashSet();

    public static boolean isBlockFiltered(Entity entity, IBlockState state) {
        return HOOK_isBlockFiltered.reportHook() && entity instanceof EntityPlayer && LIST_BLOCK_FILTER.contains(state.getBlock().getClass());
    }

    /**
     * onApplyClimbableBlockMovement
     */
    public static final HookReporter HOOK_onApplyClimbableBlockMovement = newHookReporter()
            .hook("onApplyClimbableBlockMovement")
            // no hook exists
            .forgeEvent(ApplyClimbableBlockMovement.class)
            .build();
    @Deprecated
    public static boolean onApplyClimbableBlockMovement(EntityLivingBase livingBase) {
        return HOOK_onApplyClimbableBlockMovement.reportHook() && MinecraftForge.EVENT_BUS.post(new ApplyClimbableBlockMovement(livingBase));
    }

    /**
     * onRenderBlockInLayer
     */
    public static final HookReporter HOOK_onRenderBlockInLayer = newHookReporter()
            .hook("onRenderBlockInLayer")
            .dependsOn(TypesMc.Methods.Block_canRenderInLayer)
            .forgeEvent(RenderBlockInLayerEvent.class)
            .build();
    public static BlockRenderLayer onRenderBlockInLayer(Block block, IBlockState state, BlockRenderLayer layer, BlockRenderLayer compareToLayer) {
        if(HOOK_onRenderBlockInLayer.reportHook()) {
            RenderBlockInLayerEvent event = new RenderBlockInLayerEvent(block, state, layer, compareToLayer);
            MinecraftForge.EVENT_BUS.post(event);
            return event.getLayer();
        } else return layer;
    }

    /**
     * onBlockRender
     */
    public static final HookReporter HOOK_onBlockRender = newHookReporter()
            .hook("onBlockRender")
            // no hook exists
            .forgeEvent(BlockRenderEvent.class)
            .build();
    @Deprecated
    public static void onBlockRender(BlockPos pos, IBlockState state, IBlockAccess access, BufferBuilder buffer) {
        if(HOOK_onBlockRender.reportHook()) MinecraftForge.EVENT_BUS.post(new BlockRenderEvent(pos, state, access, buffer));
    }

    /**
     * onAddCollisionBoxToList
     */
    public static final HookReporter HOOK_onAddCollisionBoxToList = newHookReporter()
            .hook("onAddCollisionBoxToList")
            .dependsOn(TypesMc.Methods.Block_addCollisionBoxToList)
            .forgeEvent(AddCollisionBoxToListEvent.class)
            .build();
    public static boolean onAddCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean bool) {
        return HOOK_onAddCollisionBoxToList.reportHook() && MinecraftForge.EVENT_BUS.post(new AddCollisionBoxToListEvent(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, bool));
    }

    /**
     * onBlockRenderInLoop
     */
    public static final HookReporter HOOK_onBlockRenderInLoop = newHookReporter()
            .hook("onBlockRenderInLoop")
            .dependsOn(TypesMc.Methods.RenderChunk_rebuildChunk)
            .listenerEvent(BlockModelRenderListener.class)
            .build();
    public static void onBlockRenderInLoop(RenderChunk renderChunk, Block block, IBlockState state, BlockPos pos) {
        // faster hook
        if(HOOK_onBlockRenderInLoop.reportHook())
            for(BlockModelRenderListener listener : Listeners.BLOCK_MODEL_RENDER_LISTENER.getAll())
                listener.onBlockRenderInLoop(renderChunk, block, state, pos);
    }

    /**
     * onPreBuildChunk
     */
    public static final HookReporter HOOK_onPreBuildChunk = newHookReporter()
            .hook("onPreBuildChunk")
            .dependsOn(TypesMc.Methods.RenderChunk_rebuildChunk)
            .forgeEvent(BuildChunkEvent.Pre.class)
            .build();
    public static void onPreBuildChunk(RenderChunk renderChunk) {
        if(HOOK_onPreBuildChunk.reportHook()) MinecraftForge.EVENT_BUS.post(new BuildChunkEvent.Pre(renderChunk));
    }

    /**
     * onPostBuildChunk
     */
    public static final HookReporter HOOK_onPostBuildChunk = newHookReporter()
            .hook("onPostBuildChunk")
            .dependsOn(TypesMc.Methods.RenderChunk_rebuildChunk)
            .forgeEvent(BuildChunkEvent.Post.class)
            .build();
    public static void onPostBuildChunk(RenderChunk renderChunk) {
        // i couldn't place a post block render hook within the if label so I have to do this
        if(HOOK_onPostBuildChunk.reportHook()) MinecraftForge.EVENT_BUS.post(new BuildChunkEvent.Post(renderChunk));
    }

    /**
     * onDeleteGlResources
     */
    public static final HookReporter HOOK_onDeleteGlResources = newHookReporter()
            .hook("onDeleteGlResources")
            .dependsOn(TypesMc.Methods.RenderChunk_deleteGlResources)
            .forgeEvent(DeleteGlResourcesEvent.class)
            .build();
    public static void onDeleteGlResources(RenderChunk renderChunk) {
        if(HOOK_onDeleteGlResources.reportHook()) MinecraftForge.EVENT_BUS.post(new DeleteGlResourcesEvent(renderChunk));
    }

    /**
     * onAddRenderChunk
     */
    public static final HookReporter HOOK_onAddRenderChunk = newHookReporter()
            .hook("onAddRenderChunk")
            .dependsOn(TypesMc.Methods.ChunkRenderContainer_addRenderChunk)
            .forgeEvent(AddRenderChunkEvent.class)
            .build();
    public static void onAddRenderChunk(RenderChunk renderChunk, BlockRenderLayer layer) {
        if(HOOK_onAddRenderChunk.reportHook()) MinecraftForge.EVENT_BUS.post(new AddRenderChunkEvent(renderChunk, layer));
    }

    /**
     * onChunkUploaded
     */
    public static final HookReporter HOOK_onChunkUploaded = newHookReporter()
            .hook("onChunkUploaded")
            .dependsOn(TypesMc.Methods.ChunkRenderDispatcher_uploadChunk)
            .forgeEvent(ChunkUploadedEvent.class)
            .build();
    public static void onChunkUploaded(RenderChunk chunk, BufferBuilder buffer) {
        if(HOOK_onChunkUploaded.reportHook()) MinecraftForge.EVENT_BUS.post(new ChunkUploadedEvent(chunk, buffer));
    }

    /**
     * onLoadRenderers
     */
    public static final HookReporter HOOK_onLoadRenderers = newHookReporter()
            .hook("onLoadRenderers")
            .dependsOn(TypesMc.Methods.RenderGlobal_loadRenderers)
            .forgeEvent(LoadRenderersEvent.class)
            .build();
    public static void onLoadRenderers(ViewFrustum viewFrustum, ChunkRenderDispatcher renderDispatcher) {
        if(HOOK_onLoadRenderers.reportHook()) MinecraftForge.EVENT_BUS.post(new LoadRenderersEvent(viewFrustum, renderDispatcher));
    }

    /**
     * onWorldRendererDeallocated
     */
    public static final HookReporter HOOK_onWorldRendererDeallocated = newHookReporter()
            .hook("onWorldRendererDeallocated")
            .dependsOn(TypesMc.Methods.ChunkRenderWorker_freeRenderBuilder)
            .forgeEvent(WorldRendererDeallocatedEvent.class)
            .build();
    public static void onWorldRendererDeallocated(ChunkCompileTaskGenerator generator) {
        if(HOOK_onWorldRendererDeallocated.reportHook()) MinecraftForge.EVENT_BUS.post(new WorldRendererDeallocatedEvent(generator, generator.getRenderChunk()));
    }

    /**
     * shouldDisableCaveCulling
     */
    public static final HookReporter HOOK_shouldDisableCaveCulling = newHookReporter()
            .hook("shouldDisableCaveCulling")
            .dependsOn(TypesMc.Methods.RenderGlobal_setupTerrain)
            .dependsOn(TypesMc.Methods.VisGraph_setOpaqueCube)
            .dependsOn(TypesMc.Methods.VisGraph_computeVisibility)
            .build();

    public static final MultiBoolean SHOULD_DISABLE_CAVE_CULLING = new MultiBoolean();

    public static boolean shouldDisableCaveCulling() {
        return HOOK_shouldDisableCaveCulling.reportHook() && SHOULD_DISABLE_CAVE_CULLING.isEnabled();
    }

    /**
     * onUpdateWalkingPlayerPre
     */
    public static final HookReporter HOOK_onUpdateWalkingPlayerPre = newHookReporter()
            .hook("onUpdateWalkingPlayerPre")
            .dependsOn(TypesMc.Methods.EntityPlayerSP_onUpdateWalkingPlayer)
            .forgeEvent(LocalPlayerUpdateMovementEvent.Pre.class)
            .build();
    public static void onUpdateWalkingPlayerPre() {
        if(HOOK_onUpdateWalkingPlayerPre.reportHook()) MinecraftForge.EVENT_BUS.post(new LocalPlayerUpdateMovementEvent.Pre());
    }

    /**
     * onUpdateWalkingPlayerPost
     */
    public static final HookReporter HOOK_onUpdateWalkingPlayerPost = newHookReporter()
            .hook("onUpdateWalkingPlayerPost")
            .dependsOn(TypesMc.Methods.EntityPlayerSP_onUpdateWalkingPlayer)
            .forgeEvent(LocalPlayerUpdateMovementEvent.Post.class)
            .build();
    public static void onUpdateWalkingPlayerPost() {
        if(HOOK_onUpdateWalkingPlayerPost.reportHook()) MinecraftForge.EVENT_BUS.post(new LocalPlayerUpdateMovementEvent.Post());
    }

    /**
     * onWorldCheckLightFor
     */
    public static final HookReporter HOOK_onWorldCheckLightFor = newHookReporter()
            .hook("onWorldCheckLightFor")
            .dependsOn(TypesMc.Methods.World_checkLightFor)
            .forgeEvent(WorldCheckLightForEvent.class)
            .build();
    public static boolean onWorldCheckLightFor(EnumSkyBlock enumSkyBlock, BlockPos pos) {
        return HOOK_onWorldCheckLightFor.reportHook() && MinecraftForge.EVENT_BUS.post(new WorldCheckLightForEvent(enumSkyBlock, pos));
    }
}
