package com.matt.forgehax.asm;

import com.google.common.collect.Sets;
import com.matt.forgehax.asm.events.*;
import com.matt.forgehax.asm.events.listeners.BlockModelRenderListener;
import com.matt.forgehax.asm.events.listeners.Listeners;
import com.matt.forgehax.asm.utils.MultiBoolean;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Set;

public class ForgeHaxHooks implements ASMCommon {
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

    public static final Set<Class<? extends Block>> LIST_BLOCK_FILTER = Sets.newHashSet();

    /**
     * static hooks
     */

    public static boolean onPushOutOfBlocks() {
        return MinecraftForge.EVENT_BUS.post(new PushOutOfBlocksEvent());
    }

    public static float onRenderBoat(EntityBoat boat, float entityYaw) {
        RenderBoatEvent event = new RenderBoatEvent(boat, entityYaw);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getYaw();
    }

    public static void onSchematicaPlaceBlock(ItemStack itemIn, BlockPos posIn, Vec3d vecIn) {
        MinecraftForge.EVENT_BUS.post(new SchematicaPlaceBlockEvent(itemIn, posIn, vecIn));
    }

    public static boolean onHurtcamEffect(float partialTicks) {
        return MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent(partialTicks));
    }

    public static boolean onSendingPacket(Packet<?> packet) {
        return MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Pre(packet));
    }

    public static void onSentPacket(Packet<?> packet) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Post(packet));
    }

    public static boolean onPreReceived(Packet<?> packet) {
        return MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Pre(packet));
    }

    public static void onPostReceived(Packet<?> packet) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Post(packet));
    }

    public static boolean onWaterMovement(Entity entity, Vec3d moveDir) {
        return MinecraftForge.EVENT_BUS.post(new WaterMovementEvent(entity, moveDir));
    }

    public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity, double x, double z) {
        return MinecraftForge.EVENT_BUS.post(new ApplyCollisionMotionEvent(entity, collidedWithEntity, x, 0.D, z));
    }

    public static boolean SHOULD_UPDATE_ALPHA = false;
    public static float COLOR_MULTIPLIER_ALPHA = 150.f / 255.f;

    public static int onPutColorMultiplier(float r, float g, float b, int buffer, boolean[] flag) {
        flag[0] = SHOULD_UPDATE_ALPHA;
        if(SHOULD_UPDATE_ALPHA) {
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

    public static boolean onPreRenderBlockLayer(BlockRenderLayer layer, double partialTicks) {
        return MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Pre(layer, partialTicks));
    }

    public static void onPostRenderBlockLayer(BlockRenderLayer layer, double partialTicks) {
        MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Post(layer, partialTicks));
    }

    public static boolean onSetupTerrain(Entity renderEntity, boolean playerSpectator) {
        SetupTerrainEvent event = new SetupTerrainEvent(renderEntity, playerSpectator);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCulling();
    }

    public static void onComputeVisibility(VisGraph visGraph, SetVisibility setVisibility) {
        MinecraftForge.EVENT_BUS.post(new ComputeVisibilityEvent(visGraph, setVisibility));
    }

    public static boolean onDoBlockCollisions(Entity entity, BlockPos pos, IBlockState state) {
        return MinecraftForge.EVENT_BUS.post(new DoBlockCollisionsEvent(entity, pos, state));
    }

    public static boolean isBlockFiltered(Entity entity, IBlockState state) {
        return entity instanceof EntityPlayer && LIST_BLOCK_FILTER.contains(state.getBlock().getClass());
    }

    public static boolean onApplyClimbableBlockMovement(EntityLivingBase livingBase) {
        return MinecraftForge.EVENT_BUS.post(new ApplyClimbableBlockMovement(livingBase));
    }

    public static BlockRenderLayer onRenderBlockInLayer(Block block, IBlockState state, BlockRenderLayer layer, BlockRenderLayer compareToLayer) {
        RenderBlockInLayerEvent event = new RenderBlockInLayerEvent(block, state, layer, compareToLayer);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getLayer();
    }

    public static void onBlockRender(BlockPos pos, IBlockState state, IBlockAccess access, BufferBuilder buffer) {
        //MinecraftForge.EVENT_BUS.post(new BlockRenderEvent(pos, state, access, buffer));
    }

    public static boolean onAddCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean bool) {
        return MinecraftForge.EVENT_BUS.post(new AddCollisionBoxToListEvent(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, bool));
    }

    public static void onBlockRenderInLoop(RenderChunk renderChunk, Block block, IBlockState state, BlockPos pos) {
        // faster hook
        for(BlockModelRenderListener listener : Listeners.BLOCK_MODEL_RENDER_LISTENER.getAll())
            listener.onBlockRenderInLoop(renderChunk, block, state, pos);
    }

    public static void onPreBuildChunk(RenderChunk renderChunk) {
        MinecraftForge.EVENT_BUS.post(new BuildChunkEvent.Pre(renderChunk));
    }

    public static void onPostBuildChunk(RenderChunk renderChunk) {
        // i couldn't place a post block render hook within the if label so I have to do this
        MinecraftForge.EVENT_BUS.post(new BuildChunkEvent.Post(renderChunk));
    }

    public static void onDeleteGlResources(RenderChunk renderChunk) {
        MinecraftForge.EVENT_BUS.post(new DeleteGlResourcesEvent(renderChunk));
    }

    public static void onAddRenderChunk(RenderChunk renderChunk, BlockRenderLayer layer) {
        MinecraftForge.EVENT_BUS.post(new AddRenderChunkEvent(renderChunk, layer));
    }

    public static void onChunkUploaded(RenderChunk chunk, BufferBuilder buffer) {
        MinecraftForge.EVENT_BUS.post(new ChunkUploadedEvent(chunk, buffer));
    }

    public static void onLoadRenderers(ViewFrustum viewFrustum, ChunkRenderDispatcher renderDispatcher) {
        MinecraftForge.EVENT_BUS.post(new LoadRenderersEvent(viewFrustum, renderDispatcher));
    }

    public static void onWorldRendererDeallocated(ChunkCompileTaskGenerator generator) {
        MinecraftForge.EVENT_BUS.post(new WorldRendererDeallocatedEvent(generator, generator.getRenderChunk()));
    }

    public static final MultiBoolean SHOULD_DISABLE_CAVE_CULLING = new MultiBoolean();

    public static boolean shouldDisableCaveCulling() {
        return SHOULD_DISABLE_CAVE_CULLING.isEnabled();
    }

    public static void onUpdateWalkingPlayerPre() {
        MinecraftForge.EVENT_BUS.register(new LocalPlayerUpdateMovementEvent.Pre());
    }

    public static void onUpdateWalkingPlayerPost() {
        MinecraftForge.EVENT_BUS.register(new LocalPlayerUpdateMovementEvent.Post());
    }
}
