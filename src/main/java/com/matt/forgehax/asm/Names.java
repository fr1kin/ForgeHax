package com.matt.forgehax.asm;

import com.matt.forgehax.asm.helper.AsmClass;
import com.matt.forgehax.asm.helper.AsmField;
import com.matt.forgehax.asm.helper.AsmMethod;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Set;

public class Names {
    public static final Names INSTANCE = new Names();

    // MC classes, methods, and fields

    public final AsmClass PACKET = new AsmClass()
            .setName("net/minecraft/network/Packet")
            .setObfuscatedName("fm");

    public final AsmClass AXISALIGNEDBB = new AsmClass()
            .setName("net/minecraft/util/math/AxisAlignedBB")
            .setObfuscatedName("bdt");

    public final AsmClass MATERIAL = new AsmClass()
            .setName("net/minecraft/block/material/Material")
            .setObfuscatedName("azs");

    public final AsmClass ENTITY = new AsmClass()
            .setName("net/minecraft/entity/Entity")
            .setObfuscatedName("sn");

    public final AsmClass LIVING_BASE = new AsmClass()
            .setName("net/minecraft/entity/EntityLivingBase")
            .setObfuscatedName("sw");

    public final AsmClass VEC3D = new AsmClass()
            .setName("net/minecraft/util/math/Vec3d")
            .setObfuscatedName("bdw");

    public final AsmClass BLOCK_RENDER_LAYER = new AsmClass()
            .setName("net/minecraft/util/BlockRenderLayer")
            .setObfuscatedName("ajk");

    public final AsmClass IBLOCKSTATE = new AsmClass()
            .setName("net/minecraft/block/state/IBlockState")
            .setObfuscatedName("atl");

    public final AsmClass BLOCKPOS = new AsmClass()
            .setName("net/minecraft/util/math/BlockPos")
            .setObfuscatedName("co");

    public final AsmClass BLOCK = new AsmClass()
            .setName("net/minecraft/block/Block")
            .setObfuscatedName("alu");

    public final AsmClass ICAMERA = new AsmClass()
            .setName("net/minecraft/client/renderer/culling/ICamera")
            .setObfuscatedName("btl");

    public final AsmClass VISGRAPH = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/VisGraph")
            .setObfuscatedName("bth");

    public final AsmClass SETVISIBILITY = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/SetVisibility")
            .setObfuscatedName("bti");

    public final AsmClass NETWORK_MANAGER$4 = new AsmClass()
            .setName("net/minecraft/network/NetworkManager$4")
            .setObfuscatedName("er$4");

    public final AsmClass IBLOCKACCESS = new AsmClass()
            .setName("net/minecraft/world/IBlockAccess")
            .setObfuscatedName("ajw");

    public final AsmClass VERTEXBUFFER = new AsmClass()
            .setName("net/minecraft/client/renderer/VertexBuffer")
            .setObfuscatedName("bpy");

    public final AsmClass MOVERTYPE = new AsmClass()
            .setName("net/minecraft/entity/MoverType")
            .setObfuscatedName("tc");

    public final AsmClass WORLD_PROVIDER = new AsmClass()
            .setName("net/minecraft/world/WorldProvider")
            .setObfuscatedName("avf");

    public final AsmClass WORLD = new AsmClass()
            .setName("net/minecraft/world/World")
            .setObfuscatedName("ajs");

    public final AsmClass IBAKEDMODEL = new AsmClass()
            .setName("net/minecraft/client/renderer/block/model/IBakedModel")
            .setObfuscatedName("cbh");

    public final AsmClass COMPILED_CHUNK = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/CompiledChunk")
            .setObfuscatedName("btb");

    public final AsmClass RENDER_CHUNK = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/RenderChunk")
            .setObfuscatedName("bte");

    public final AsmClass CHUNK_COMPILE_TASK_GENERATOR = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator")
            .setObfuscatedName("bsy");

    public final AsmClass CHUNK_CACHE = new AsmClass()
            .setName("net/minecraft/world/ChunkCache")
            .setObfuscatedName("akb");

    public final AsmClass VIEW_FRUSTUM = new AsmClass()
            .setName("net/minecraft/client/renderer/ViewFrustum")
            .setObfuscatedName("bqv");

    public final AsmClass CHUNK_RENDER_DISPATCHER = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher")
            .setObfuscatedName("bsz");

    public final AsmClass RENDER_GLOBAL = new AsmClass()
            .setName("net/minecraft/client/renderer/RenderGlobal")
            .setObfuscatedName("bqm");

    // JOURNYMAP special

    public final AsmClass JOURNYMAP_STRATUM = new AsmClass()
            .setName("journeymap/client/cartography/Stratum");

    // hook names

    // ----event classes----
    public final AsmClass WEB_MOTION_EVENT = new AsmClass()
            .setName("com/matt/forgehax/asm/events/WebMotionEvent");

    public final AsmClass RENDER_BLOCK_IN_LAYER_EVENT = new AsmClass()
            .setName("com/matt/forgehax/asm/events/RenderBlockInLayerEvent");


    // ----fields----
    public final AsmField NETMANAGER$4__val$inPacket = NETWORK_MANAGER$4.childField()
            .setName("val$inPacket")
            .setType(PACKET);


    //----forgehax hooks----
    public final AsmClass FORGEHAX_HOOKS = new AsmClass()
            .setName(Type.getInternalName(ForgeHaxHooks.class));

    public final AsmField IS_SAFEWALK_ACTIVE = FORGEHAX_HOOKS.childField()
            .setName("isSafeWalkActivated")
            .setType(boolean.class);

    public final AsmField IS_NOSLOWDOWN_ACTIVE = FORGEHAX_HOOKS.childField()
            .setName("isNoSlowDownActivated")
            .setType(boolean.class);

    public final AsmMethod ON_HURTCAMEFFECT = FORGEHAX_HOOKS.childMethod()
            .setName("onHurtcamEffect")
            .setArgumentTypes(float.class)
            .setReturnType(boolean.class);

    public final AsmMethod ON_SENDING_PACKET = FORGEHAX_HOOKS.childMethod()
            .setName("onSendingPacket")
            .setArgumentTypes(PACKET)
            .setReturnType(boolean.class);

    public final AsmMethod ON_SENT_PACKET = FORGEHAX_HOOKS.childMethod()
            .setName("onSentPacket")
            .setArgumentTypes(PACKET)
            .setReturnType(void.class);

    public final AsmMethod ON_PRE_RECEIVED = FORGEHAX_HOOKS.childMethod()
            .setName("onPreReceived")
            .setArgumentTypes(PACKET)
            .setReturnType(boolean.class);

    public final AsmMethod ON_POST_RECEIVED = FORGEHAX_HOOKS.childMethod()
            .setName("onPostReceived")
            .setArgumentTypes(PACKET)
            .setReturnType(void.class);

    public final AsmMethod ON_WATER_MOVEMENT = FORGEHAX_HOOKS.childMethod()
            .setName("onWaterMovement")
            .setArgumentTypes(ENTITY, VEC3D)
            .setReturnType(boolean.class);

    public final AsmMethod ON_APPLY_COLLISION = FORGEHAX_HOOKS.childMethod()
            .setName("onApplyCollisionMotion")
            .setArgumentTypes(ENTITY, ENTITY, double.class, double.class)
            .setReturnType(boolean.class);

    public final AsmMethod ON_WEB_MOTION = FORGEHAX_HOOKS.childMethod()
            .setName("onWebMotion")
            .setArgumentTypes(ENTITY, double.class, double.class, double.class)
            .setReturnType(WEB_MOTION_EVENT);

    public final AsmMethod ON_COLOR_MULTIPLIER = FORGEHAX_HOOKS.childMethod()
            .setName("onPutColorMultiplier")
            .setArgumentTypes(float.class, float.class, float.class, int.class, boolean[].class)
            .setReturnType(int.class);

    public final AsmMethod ON_PRERENDER_BLOCKLAYER = FORGEHAX_HOOKS.childMethod()
            .setName("onPreRenderBlockLayer")
            .setArgumentTypes(BLOCK_RENDER_LAYER, double.class)
            .setReturnType(boolean.class);

    public final AsmMethod ON_POSTRENDER_BLOCKLAYER = FORGEHAX_HOOKS.childMethod()
            .setName("onPostRenderBlockLayer")
            .setArgumentTypes(BLOCK_RENDER_LAYER, double.class)
            .setReturnType(void.class);

    public final AsmMethod ON_RENDERBLOCK_INLAYER = FORGEHAX_HOOKS.childMethod()
            .setName("onRenderBlockInLayer")
            .setArgumentTypes(BLOCK, IBLOCKSTATE, BLOCK_RENDER_LAYER, BLOCK_RENDER_LAYER)
            .setReturnType(BLOCK_RENDER_LAYER);

    public final AsmMethod ON_SETUP_TERRAIN = FORGEHAX_HOOKS.childMethod()
            .setName("onSetupTerrain")
            .setArgumentTypes(ENTITY, boolean.class)
            .setReturnType(boolean.class);

    public final AsmMethod ON_COMPUTE_VISIBILITY = FORGEHAX_HOOKS.childMethod()
            .setName("onComputeVisibility")
            .setArgumentTypes(VISGRAPH, SETVISIBILITY)
            .setReturnType(void.class);

    public final AsmMethod ON_DO_BLOCK_COLLISIONS = FORGEHAX_HOOKS.childMethod()
            .setName("onDoBlockCollisions")
            .setArgumentTypes(ENTITY, BLOCKPOS, IBLOCKSTATE)
            .setReturnType(boolean.class);

    public final AsmMethod IS_BLOCK_COLLISION_FILTERED = FORGEHAX_HOOKS.childMethod()
            .setName("isBlockFiltered")
            .setArgumentTypes(ENTITY, IBLOCKSTATE)
            .setReturnType(boolean.class);

    public final AsmMethod ON_APPLY_CLIMBABLE_BLOCK_MOVEMENT = FORGEHAX_HOOKS.childMethod()
            .setName("onApplyClimbableBlockMovement")
            .setArgumentTypes(LIVING_BASE)
            .setReturnType(boolean.class);

    public final AsmMethod ON_RENDER_BLOCK = FORGEHAX_HOOKS.childMethod()
            .setName("onBlockRender")
            .setArgumentTypes(BLOCKPOS, IBLOCKSTATE, IBLOCKACCESS, VERTEXBUFFER)
            .setReturnType(void.class);

    public final AsmMethod ON_BLOCK_ADD_COLLISION = FORGEHAX_HOOKS.childMethod()
            .setName("onAddCollisionBoxToList")
            .setArgumentTypes(BLOCK, IBLOCKSTATE, WORLD, BLOCKPOS, AXISALIGNEDBB, List.class, ENTITY, boolean.class)
            .setReturnType(boolean.class);

    public final AsmMethod ON_BLOCK_RENDER_IN_LOOP = FORGEHAX_HOOKS.childMethod()
            .setName("onBlockRenderInLoop")
            .setArgumentTypes(RENDER_CHUNK, BLOCK, IBLOCKSTATE, BLOCKPOS)
            .setReturnType(void.class);

    public final AsmMethod ON_BLOCK_MODEL_RENDER = FORGEHAX_HOOKS.childMethod()
            .setName("onBlockModelRender")
            .setArgumentTypes(IBLOCKACCESS, IBAKEDMODEL, IBLOCKSTATE, BLOCKPOS, VERTEXBUFFER, boolean.class, long.class)
            .setReturnType(void.class);

    public final AsmMethod ON_PRE_BUILD_CHUNK = FORGEHAX_HOOKS.childMethod()
            .setName("onPreBuildChunk")
            .setArgumentTypes(RENDER_CHUNK)
            .setReturnType(void.class);

    public final AsmMethod ON_POST_BUILD_CHUNK = FORGEHAX_HOOKS.childMethod()
            .setName("onPostBuildChunk")
            .setArgumentTypes(RENDER_CHUNK)
            .setReturnType(void.class);

    public final AsmMethod ON_DELETE_GL_RESOURCES = FORGEHAX_HOOKS.childMethod()
            .setName("onDeleteGlResources")
            .setArgumentTypes(RENDER_CHUNK)
            .setReturnType(void.class);

    public final AsmMethod ON_PRE_BLOCK_MODEL_RENDER = FORGEHAX_HOOKS.childMethod()
            .setName("onPreBlockModelRender")
            .setArgumentTypes(RENDER_CHUNK, VERTEXBUFFER, BLOCKPOS)
            .setReturnType(void.class);

    public final AsmMethod ON_POST_BLOCK_MODEL_RENDER = FORGEHAX_HOOKS.childMethod()
            .setName("onPostBlockModelRender")
            .setArgumentTypes(RENDER_CHUNK, VERTEXBUFFER, float.class, float.class, float.class)
            .setReturnType(void.class);

    public final AsmMethod ON_CHUNK_UPLOADED = FORGEHAX_HOOKS.childMethod()
            .setName("onChunkUploaded")
            .setArgumentTypes(RENDER_CHUNK, VERTEXBUFFER)
            .setReturnType(void.class);

    public final AsmMethod ON_ADD_RENDERCHUNK = FORGEHAX_HOOKS.childMethod()
            .setName("onAddRenderChunk")
            .setArgumentTypes(RENDER_CHUNK, BLOCK_RENDER_LAYER)
            .setReturnType(void.class);

    public final AsmMethod ON_LOAD_RENDERERS = FORGEHAX_HOOKS.childMethod()
            .setName("onLoadRenderers")
            .setArgumentTypes(VIEW_FRUSTUM, CHUNK_RENDER_DISPATCHER)
            .setReturnType(void.class);

    public final AsmMethod ON_WORLDRENDERER_ALLOCATED = FORGEHAX_HOOKS.childMethod()
            .setName("onWorldRendererAllocated")
            .setArgumentTypes(CHUNK_COMPILE_TASK_GENERATOR)
            .setReturnType(void.class);

    public final AsmMethod ON_WORLDRENDERER_DEALLOCATED = FORGEHAX_HOOKS.childMethod()
            .setName("onWorldRendererDeallocated")
            .setArgumentTypes(CHUNK_COMPILE_TASK_GENERATOR)
            .setReturnType(void.class);

    public final AsmMethod SHOULD_DISABLE_CULLING = FORGEHAX_HOOKS.childMethod()
            .setName("shouldDisableCaveCulling")
            .setArgumentTypes()
            .setReturnType(boolean.class);

    public final AsmMethod JOURNYMAP_ON_SET_STRATUM_COLOR = FORGEHAX_HOOKS.childMethod()
            .setName("onJournyMapSetStratumColor")
            .setArgumentTypes(Object.class, Object.class, int.class, Integer.class, boolean.class, boolean.class, boolean.class)
            .setReturnType(boolean.class);
}
