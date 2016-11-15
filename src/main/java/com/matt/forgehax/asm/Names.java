package com.matt.forgehax.asm;

import com.matt.forgehax.asm.helper.AsmClass;
import com.matt.forgehax.asm.helper.AsmField;
import com.matt.forgehax.asm.helper.AsmMethod;
import org.objectweb.asm.Type;

public class Names {
    public static final Names INSTANCE = new Names();

    // MC classes, methods, and fields

    public final AsmClass PACKET = new AsmClass()
            .setName("net/minecraft/network/Packet")
            .setObfuscatedName("fj");

    public final AsmClass AXISALIGNEDBB = new AsmClass()
            .setName("net/minecraft/util/math/AxisAlignedBB")
            .setObfuscatedName("bby");

    public final AsmClass MATERIAL = new AsmClass()
            .setName("net/minecraft/block/material/Material")
            .setObfuscatedName("axx");

    public final AsmClass ENTITY = new AsmClass()
            .setName("net/minecraft/entity/Entity")
            .setObfuscatedName("rw");

    public final AsmClass LIVING_BASE = new AsmClass()
            .setName("net/minecraft/entity/EntityLivingBase")
            .setObfuscatedName("sf");

    public final AsmClass VEC3D = new AsmClass()
            .setName("net/minecraft/util/math/Vec3d")
            .setObfuscatedName("bcb");

    public final AsmClass BLOCK_RENDER_LAYER = new AsmClass()
            .setName("net/minecraft/util/BlockRenderLayer")
            .setObfuscatedName("ahv");

    public final AsmClass IBLOCKSTATE = new AsmClass()
            .setName("net/minecraft/block/state/IBlockState")
            .setObfuscatedName("ars");

    public final AsmClass BLOCKPOS = new AsmClass()
            .setName("net/minecraft/util/math/BlockPos")
            .setObfuscatedName("cm");

    public final AsmClass BLOCK = new AsmClass()
            .setName("net/minecraft/block/Block")
            .setObfuscatedName("akf");

    public final AsmClass ICAMERA = new AsmClass()
            .setName("net/minecraft/client/renderer/culling/ICamera")
            .setObfuscatedName("brf");

    public final AsmClass VISGRAPH = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/VisGraph")
            .setObfuscatedName("brb");

    public final AsmClass SETVISIBILITY = new AsmClass()
            .setName("net/minecraft/client/renderer/chunk/SetVisibility")
            .setObfuscatedName("brc");

    public final AsmClass NETWORK_MANAGER$4 = new AsmClass()
            .setName("net/minecraft/network/NetworkManager$4")
            .setObfuscatedName("eo$4");

    public final AsmClass IBLOCKACCESS = new AsmClass()
            .setName("net/minecraft/world/IBlockAccess")
            .setObfuscatedName("aih");

    public final AsmClass VERTEXBUFFER = new AsmClass()
            .setName("net/minecraft/client/renderer/VertexBuffer")
            .setObfuscatedName("bnt");

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
            .setArgumentTypes(BLOCK, IBLOCKSTATE, BLOCK_RENDER_LAYER, boolean.class)
            .setReturnType(RENDER_BLOCK_IN_LAYER_EVENT);

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

    public final AsmMethod ON_APPLY_CLIMBABLE_BLOCK_MOVEMENT = FORGEHAX_HOOKS.childMethod()
            .setName("onApplyClimbableBlockMovement")
            .setArgumentTypes(LIVING_BASE)
            .setReturnType(boolean.class);

    public final AsmMethod ON_RENDER_BLOCK = FORGEHAX_HOOKS.childMethod()
            .setName("onBlockRender")
            .setArgumentTypes(BLOCKPOS, IBLOCKSTATE, IBLOCKACCESS, VERTEXBUFFER)
            .setReturnType(void.class);
}
