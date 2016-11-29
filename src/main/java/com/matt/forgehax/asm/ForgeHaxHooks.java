package com.matt.forgehax.asm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm.events.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ForgeHaxHooks {
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

    public static boolean isSafeWalkActivated = false;

    public static boolean isNoSlowDownActivated = false;

    public static boolean onHurtcamEffect(float partialTicks) {
        reportHook("onHurtcamEffect");
        return MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent(partialTicks));
    }

    public static boolean onSendingPacket(Packet<?> packet) {
        reportHook("onSendingPacket");
        return MinecraftForge.EVENT_BUS.post(new PacketEvent.Send.Pre(packet));
    }

    public static void onSentPacket(Packet<?> packet) {
        reportHook("onSentPacket");
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Send.Post(packet));
    }

    public static boolean onPreReceived(Packet<?> packet) {
        reportHook("onPreReceived");
        return MinecraftForge.EVENT_BUS.post(new PacketEvent.Received.Pre(packet));
    }

    public static void onPostReceived(Packet<?> packet) {
        reportHook("onPostReceived");
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Received.Post(packet));
    }

    public static boolean onWaterMovement(Entity entity, Vec3d moveDir) {
        reportHook("onWaterMovement");
        return MinecraftForge.EVENT_BUS.post(new WaterMovementEvent(entity, moveDir));
    }

    public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity, double x, double z) {
        reportHook("onApplyCollisionMotion");
        return MinecraftForge.EVENT_BUS.post(new ApplyCollisionMotionEvent(entity, collidedWithEntity, x, 0.D, z));
    }

    public static WebMotionEvent onWebMotion(Entity entity, double x, double y, double z) {
        reportHook("onWebMotion");
        WebMotionEvent event = new WebMotionEvent(entity, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
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

    public static BlockRenderLayer onRenderBlockInLayer(Block block, IBlockState state, BlockRenderLayer layer, BlockRenderLayer compareToLayer) {
        reportHook("onRenderBlockInLayer");
        RenderBlockInLayerEvent event = new RenderBlockInLayerEvent(block, state, layer, compareToLayer);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getLayer();
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

    public static boolean onApplyClimbableBlockMovement(EntityLivingBase livingBase) {
        return MinecraftForge.EVENT_BUS.post(new ApplyClimbableBlockMovement(livingBase));
    }

    public static void onBlockRender(BlockPos pos, IBlockState state, IBlockAccess access, VertexBuffer buffer) {
        //MinecraftForge.EVENT_BUS.post(new BlockRenderEvent(pos, state, access, buffer));
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
