package com.matt.forgehax.asm2.constants;

import com.fr1kin.asmhelper.types.ASMClass;
import com.fr1kin.asmhelper.types.ASMMethod;
import com.matt.forgehax.asm2.types.ASMObfClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Created on 1/11/2017 by fr1kin
 */
public class McpConstants {
    private static McpConstants instance = null;

    public static McpConstants getInstance() {
        return instance == null ? instance = new McpConstants() : instance;
    }

    //
    // classes
    //
    public final ASMClass CLASS_PACKET = ASMObfClass.getOrCreateClass("net/minecraft/network/Packet");

    public final ASMClass CLASS_NETWORKMANAGER = ASMObfClass.getOrCreateClass("net/minecraft/network/NetworkManager");

    public final ASMClass CLASS_ENTITYRENDERER = ASMObfClass.getOrCreateClass("net/minecraft/client/renderer/EntityRenderer");

    //
    // methods
    //

    public final ASMMethod METHOD_NETWORKMANAGER_DISPATCH_PACKET = CLASS_NETWORKMANAGER.childMethod("dispatchPacket", false,
            void.class,
            CLASS_PACKET, GenericFutureListener[].class
    );

    public final ASMMethod METHOD_NETWORKMANAGER_CHANNELREAD0 = CLASS_NETWORKMANAGER.childMethod("channelRead0", false,
            void.class,
            ChannelHandlerContext.class, CLASS_PACKET
    );

    public final ASMMethod METHOD_ENTITYRENDERER_HURTCAM_EFFECT = CLASS_ENTITYRENDERER.childMethod("hurtCameraEffect", false,
            void.class,
            float.class
    );

    //
    // fields
    //

}
