package com.matt.forgehax.asm2;

import com.fr1kin.asmhelper.types.ASMClass;
import com.fr1kin.asmhelper.types.ASMMethod;
import com.matt.forgehax.asm2.types.ASMObfClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Created on 1/11/2017 by fr1kin
 */
public interface ConstantMc {
    //private static ConstantMc instance = null;

    /*public static ConstantMc getInstance() {
        return instance == null ? instance = new ConstantMc() : instance;
    }*/

    //
    // classes
    //
    ASMClass CLASS_AXISALIGNEDBB                       = ASMObfClass.getOrCreateClass("net/minecraft/util/math/AxisAlignedBB");

    ASMClass CLASS_BLOCK                               = ASMObfClass.getOrCreateClass("net/minecraft/block/Block");
    ASMClass CLASS_BLOCKPOS                            = ASMObfClass.getOrCreateClass("net/minecraft/util/math/BlockPos");
    ASMClass CLASS_BLOCKRENDERLAYER                    = ASMObfClass.getOrCreateClass("net/minecraft/util/BlockRenderLayer");

    ASMClass CLASS_ENTITY                              = ASMObfClass.getOrCreateClass("net/minecraft/entity/Entity");
    ASMClass CLASS_ENTITYLIVINGBASE                    = ASMObfClass.getOrCreateClass("net/minecraft/entity/EntityLivingBase");
    ASMClass CLASS_ENTITYPLAYERSP                      = ASMObfClass.getOrCreateClass("net/minecraft/client/entity/EntityPlayerSP");
    ASMClass CLASS_ENTITYRENDERER                      = ASMObfClass.getOrCreateClass("net/minecraft/client/renderer/EntityRenderer");

    ASMClass CLASS_IBLOCKACCESS                        = ASMObfClass.getOrCreateClass("net/minecraft/world/IBlockAccess");
    ASMClass CLASS_IBLOCKSTATE                         = ASMObfClass.getOrCreateClass("net/minecraft/block/state/IBlockState");
    ASMClass CLASS_ICAMERA                             = ASMObfClass.getOrCreateClass("net/minecraft/client/renderer/culling/ICamera");

    ASMClass CLASS_MATERIAL                            = ASMObfClass.getOrCreateClass("net/minecraft/block/material/Material");
    ASMClass CLASS_MOVERTYPE                           = ASMObfClass.getOrCreateClass("net/minecraft/entity/MoverType");

    ASMClass CLASS_NETWORKMANAGER                      = ASMObfClass.getOrCreateClass("net/minecraft/network/NetworkManager");
    ASMClass CLASS_NETWORKMANAGER$4                    = ASMObfClass.getOrCreateClass("net/minecraft/network/NetworkManager$4");

    ASMClass CLASS_PACKET                              = ASMObfClass.getOrCreateClass("net/minecraft/network/Packet");

    ASMClass CLASS_RENDERGLOBAL                        = ASMObfClass.getOrCreateClass("net/minecraft/client/renderer/RenderGlobal");

    ASMClass CLASS_SETVISIBILITY                       = ASMObfClass.getOrCreateClass("net/minecraft/client/renderer/chunk/SetVisibility");

    ASMClass CLASS_VEC3D                               = ASMObfClass.getOrCreateClass("net/minecraft/util/math/Vec3d");
    ASMClass CLASS_VERTEXBUFFER                        = ASMObfClass.getOrCreateClass("net/minecraft/client/renderer/VertexBuffer");
    ASMClass CLASS_VISGRAPH                            = ASMObfClass.getOrCreateClass("net/minecraft/client/renderer/chunk/VisGraph");

    ASMClass CLASS_WORLD                               = ASMObfClass.getOrCreateClass("net/minecraft/world/World");

    //
    // methods
    //

    ASMMethod METHOD_BLOCK_CANRENDERINLAYER = CLASS_BLOCK.childMethod("canRenderInLayer", false,
            boolean.class,
            CLASS_IBLOCKSTATE, CLASS_BLOCKRENDERLAYER
    );

    ASMMethod METHOD_ENTITY_APPLYENTITYCOLLISIONS = CLASS_ENTITY.childMethod("applyEntityCollision", false,
            void.class,
            CLASS_ENTITY
    );
    ASMMethod METHOD_ENTITY_DOBLOCKCOLLISIONS = CLASS_ENTITY.childMethod("doBlockCollisions", false,
            void.class
    );
    ASMMethod METHOD_ENTITY_MOVE = CLASS_ENTITY.childMethod("move", false,
            void.class,
            CLASS_MOVERTYPE, double.class, double.class, double.class
    );

    ASMMethod METHOD_ENTITYLIVINGBASE_ONLIVINGUPDATE = CLASS_ENTITYLIVINGBASE.childMethod("onLivingUpdate", false,
            void.class
    );

    ASMMethod METHOD_ENTITYRENDERER_HURTCAMEFFECT = CLASS_ENTITYRENDERER.childMethod("hurtCameraEffect", false,
            void.class,
            float.class
    );

    ASMMethod METHOD_NETWORKMANAGER_DISPATCHPACKET = CLASS_NETWORKMANAGER.childMethod("dispatchPacket", false,
            void.class,
            CLASS_PACKET, GenericFutureListener[].class
    );
    ASMMethod METHOD_NETWORKMANAGER_CHANNELREAD0 = CLASS_NETWORKMANAGER.childMethod("channelRead0", false,
            void.class,
            ChannelHandlerContext.class, CLASS_PACKET
    );

    ASMMethod METHOD_RENDERGLOBAL_RENDERBLOCKLAYER = CLASS_RENDERGLOBAL.childMethod("renderBlockLayer", false,
            int.class,
            CLASS_BLOCKRENDERLAYER, double.class, int.class, CLASS_ENTITY
    );

    ASMMethod METHOD_VERTEXBUFFER_PUTCOLORMULTIPLIER = CLASS_VERTEXBUFFER.childMethod("putColorMultiplier", false,
            void.class,
            float.class, float.class, float.class, int.class
    );

    ASMMethod METHOD_VISGRAPH_COMPUTEVISIBILITY = CLASS_VISGRAPH.childMethod("computeVisibility", false,
            CLASS_SETVISIBILITY
    );

    ASMMethod METHOD_WORLD_HANDLEMATERIALACCELERATION = CLASS_WORLD.childMethod("handleMaterialAcceleration", false,
            boolean.class,
            CLASS_AXISALIGNEDBB, CLASS_MATERIAL, CLASS_ENTITY
    );

    //
    // fields
    //

}
