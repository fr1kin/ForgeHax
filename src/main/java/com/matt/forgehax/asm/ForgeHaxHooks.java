package com.matt.forgehax.asm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.matt.forgehax.asm.events.*;
import com.matt.forgehax.asm.events.listeners.BlockModelRenderListener;
import com.matt.forgehax.asm.events.listeners.Listeners;
import com.matt.forgehax.asm.reflection.FastReflectionSpecial;
import com.matt.forgehax.asm.utils.MultiSwitch;
import journeymap.client.cartography.RGB;
import journeymap.client.cartography.Stratum;
import journeymap.client.cartography.render.BaseRenderer;
import journeymap.client.model.ChunkMD;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ForgeHaxHooks implements ASMCommon {
    public static boolean isInDebugMode = true;

    public final static Map<String, DebugData> responding = Maps.newLinkedHashMap();

    static {
        responding.put("onHurtcamEffect",                   new DebugData("net.minecraft.client.renderer.EntityRenderer"));
        responding.put("onSendingPacket",                   new DebugData("net.minecraft.network.NetworkManager", "net.minecraft.network.NetworkManager$4"));
        responding.put("onSentPacket",                      new DebugData("net.minecraft.network.NetworkManager", "net.minecraft.network.NetworkManager$4"));
        responding.put("onPreReceived",                     new DebugData("net.minecraft.network.NetworkManager"));
        responding.put("onPostReceived",                    new DebugData("net.minecraft.network.NetworkManager"));
        responding.put("onWaterMovement",                   new DebugData("net.minecraft.world.World"));
        responding.put("onApplyCollisionMotion",            new DebugData("net.minecraft.entity.Entity"));
        responding.put("onWebMotion",                       new DebugData("net.minecraft.entity.Entity"));
        responding.put("onDoBlockCollisions",               new DebugData("net.minecraft.entity.Entity"));
        responding.put("onPutColorMultiplier",              new DebugData("net.minecraft.client.renderer.VertexBuffer"));
        responding.put("onPreRenderBlockLayer",             new DebugData("net.minecraft.client.renderer.RenderGlobal"));
        responding.put("onPostRenderBlockLayer",            new DebugData("net.minecraft.client.renderer.RenderGlobal"));
        responding.put("onRenderBlockInLayer",              new DebugData("net.minecraft.block.Block"));
        responding.put("onSetupTerrain",                    new DebugData("net.minecraft.client.renderer.RenderGlobal"));
        responding.put("onComputeVisibility",               new DebugData("net.minecraft.client.renderer.chunk.VisGraph"));
    }

    private static void reportHook(String name) {
        if(isInDebugMode) {
            DebugData debug = responding.get(name);
            if (debug != null) {
                debug.hasResponded = true;
                debug.isResponding = true;
            }
        }
    }

    public static void setHooksLog(String className, final List<String> log, int methodCount) {
        for(DebugData data : responding.values()) {
            if(data.parentClassNames.contains(className)) {
                data.log = log;
                data.targetCount = methodCount;
            }
        }
    }

    /**
     * static fields
     */

    public static boolean isSafeWalkActivated = false;
    public static boolean isNoSlowDownActivated = false;

    public static final Set<Class<? extends Block>> LIST_BLOCK_FILTER = Sets.newHashSet();

    /**
     * static hooks
     */

    public static boolean onHurtcamEffect(float partialTicks) {
        reportHook("onHurtcamEffect");
        return MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent(partialTicks));
    }

    public static boolean onSendingPacket(Packet<?> packet) {
        reportHook("onSendingPacket");
        return MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Pre(packet));
    }

    public static void onSentPacket(Packet<?> packet) {
        reportHook("onSentPacket");
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Post(packet));
    }

    public static boolean onPreReceived(Packet<?> packet) {
        reportHook("onPreReceived");
        return MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Pre(packet));
    }

    public static void onPostReceived(Packet<?> packet) {
        reportHook("onPostReceived");
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Post(packet));
    }

    public static boolean onWaterMovement(Entity entity, Vec3d moveDir) {
        reportHook("onWaterMovement");
        return MinecraftForge.EVENT_BUS.post(new WaterMovementEvent(entity, moveDir));
    }

    public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity, double x, double z) {
        reportHook("onApplyCollisionMotion");
        return MinecraftForge.EVENT_BUS.post(new ApplyCollisionMotionEvent(entity, collidedWithEntity, x, 0.D, z));
    }

    public static boolean SHOULD_UPDATE_ALPHA = false;
    public static float COLOR_MULTIPLIER_ALPHA = 150.f / 255.f;

    public static int onPutColorMultiplier(float r, float g, float b, int buffer, boolean[] flag) {
        reportHook("onPutColorMultiplier");
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
        reportHook("onPreRenderBlockLayer");
        return MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Pre(layer, partialTicks));
    }

    public static void onPostRenderBlockLayer(BlockRenderLayer layer, double partialTicks) {
        reportHook("onPostRenderBlockLayer");
        MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Post(layer, partialTicks));
    }

    public static boolean onSetupTerrain(Entity renderEntity, boolean playerSpectator) {
        reportHook("onSetupTerrain");
        SetupTerrainEvent event = new SetupTerrainEvent(renderEntity, playerSpectator);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCulling();
    }

    public static void onComputeVisibility(VisGraph visGraph, SetVisibility setVisibility) {
        reportHook("onComputeVisibility");
        MinecraftForge.EVENT_BUS.post(new ComputeVisibilityEvent(visGraph, setVisibility));
    }

    public static boolean onDoBlockCollisions(Entity entity, BlockPos pos, IBlockState state) {
        reportHook("onDoBlockCollisions");
        return MinecraftForge.EVENT_BUS.post(new DoBlockCollisionsEvent(entity, pos, state));
    }

    public static boolean isBlockFiltered(Entity entity, IBlockState state) {
        return entity instanceof EntityPlayer && LIST_BLOCK_FILTER.contains(state.getBlock().getClass());
    }

    public static boolean onApplyClimbableBlockMovement(EntityLivingBase livingBase) {
        return MinecraftForge.EVENT_BUS.post(new ApplyClimbableBlockMovement(livingBase));
    }

    public static BlockRenderLayer onRenderBlockInLayer(Block block, IBlockState state, BlockRenderLayer layer, BlockRenderLayer compareToLayer) {
        reportHook("onRenderBlockInLayer");
        RenderBlockInLayerEvent event = new RenderBlockInLayerEvent(block, state, layer, compareToLayer);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getLayer();
    }

    public static void onBlockRender(BlockPos pos, IBlockState state, IBlockAccess access, VertexBuffer buffer) {
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

    public static void onChunkUploaded(RenderChunk chunk, VertexBuffer buffer) {
        MinecraftForge.EVENT_BUS.post(new ChunkUploadedEvent(chunk, buffer));
    }

    public static void onLoadRenderers(ViewFrustum viewFrustum, ChunkRenderDispatcher renderDispatcher) {
        MinecraftForge.EVENT_BUS.post(new LoadRenderersEvent(viewFrustum, renderDispatcher));
    }

    public static void onWorldRendererDeallocated(ChunkCompileTaskGenerator generator) {
        MinecraftForge.EVENT_BUS.post(new WorldRendererDeallocatedEvent(generator, generator.getRenderChunk()));
    }

    public static final MultiSwitch SHOULD_DISABLE_CAVE_CULLING = new MultiSwitch();

    public static boolean shouldDisableCaveCulling() {
        return SHOULD_DISABLE_CAVE_CULLING.isEnabled();
    }

    public static boolean ENABLE_JOURNEYMAP_LIGHTING_FIX = false;

    public static boolean onJournyMapSetStratumColor(Object thisPtr, Object stratumPtr, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting) {
        if(!ENABLE_JOURNEYMAP_LIGHTING_FIX) return false;
        try {
            BaseRenderer baseRenderer = (BaseRenderer)thisPtr;
            Stratum stratum = (Stratum) stratumPtr;

            int basicColor;
            if (stratum.isUninitialized()) {
                throw new IllegalStateException("Stratum wasn't initialized for setStratumColors");
            }
            float daylightDiff = (float)Math.max(1, Math.max(stratum.getLightLevel(), 15 - lightAttenuation)) / 15.0f;
            daylightDiff +=  FastReflectionSpecial.Fields.BaseRenderer_tweakBrightenDaylightDiff.get(baseRenderer, 0.f);
            float moonLightLevel = FastReflectionSpecial.Fields.BaseRenderer_tweakMoonlightLevel.get(baseRenderer, 0.f);
            float nightLightDiff = Math.max(moonLightLevel, Math.max((float)stratum.getLightLevel(), moonLightLevel - (float)lightAttenuation)) / 15.0f;
            if (stratum.isWater()) {
                basicColor = waterColor;
            } else {
                ChunkMD chunkMD = stratum.getChunkMd();
                basicColor = stratum.getBlockMD().getColor(chunkMD, stratum.getBlockPos());
            }
            Block block = stratum.getBlockMD().getBlockState().getBlock();
            if (block == Blocks.GLOWSTONE || block == Blocks.LIT_REDSTONE_LAMP) {
                basicColor = RGB.adjustBrightness(basicColor, FastReflectionSpecial.Fields.BaseRenderer_tweakBrightenLightsourceBlock.get(baseRenderer, 0.f));
            }
            if (waterAbove && waterColor != null) {
                int adjustedWaterColor = RGB.multiply(waterColor, FastReflectionSpecial.Fields.BaseRenderer_tweakDarkenWaterColorMultiplier.get(baseRenderer, 0));
                int adjustedBasicColor = RGB.adjustBrightness(basicColor, Math.max(daylightDiff, nightLightDiff));
                stratum.setDayColor(RGB.blendWith(adjustedBasicColor, adjustedWaterColor, FastReflectionSpecial.Fields.BaseRenderer_tweakWaterColorBlend.get(baseRenderer, 0.f)));
                stratum.setNightColor(RGB.adjustBrightness(stratum.getDayColor(), Math.max(nightLightDiff, FastReflectionSpecial.Fields.BaseRenderer_tweakMinimumDarkenNightWater.get(baseRenderer, 0.f))));
            } else {
                stratum.setDayColor(RGB.adjustBrightness(basicColor, daylightDiff));
                stratum.setNightColor(RGB.darkenAmbient(basicColor, nightLightDiff, baseRenderer.getAmbientColor()));
            }
            if (underground) {
                stratum.setCaveColor(mapCaveLighting ? stratum.getNightColor() : stratum.getDayColor());
            }
            return true;
        } catch (Exception e) {}
        // cancel older calculation
        return false;
    }

    public static void onUpdateWalkingPlayerPre() {
        MinecraftForge.EVENT_BUS.register(new LocalPlayerUpdateMovementEvent.Pre());
    }

    public static void onUpdateWalkingPlayerPost() {
        MinecraftForge.EVENT_BUS.register(new LocalPlayerUpdateMovementEvent.Post());
    }

    public static class DebugData {
        public final List<String> parentClassNames = Lists.newArrayList();
        /**
         * If the hook has ever responded
         */
        public boolean hasResponded = false;

        /**
         * If the hook has responded since last refresh
         */
        public boolean isResponding = false;

        /**
         * Reference to log from class transformer
         */
        public List<String> log;

        public int targetCount = 0;

        public DebugData(String... parentClasses) {
            Collections.addAll(parentClassNames, parentClasses);
        }
    }
}
