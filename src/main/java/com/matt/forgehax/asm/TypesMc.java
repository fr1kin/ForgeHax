package com.matt.forgehax.asm;

import com.google.common.util.concurrent.ListenableFuture;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.asmtype.ASMField;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMBuilders;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.List;

/** Created on 5/27/2017 by fr1kin */
public interface TypesMc {
  interface Classes {
    ASMClass Packet =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/network/Packet")
            .autoAssign()
            .build();

    ASMClass AxisAlignedBB =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/AxisAlignedBB")
            .autoAssign()
            .build();

    ASMClass Material =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/block/material/Material")
            .autoAssign()
            .build();

    ASMClass Entity =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/Entity")
            .autoAssign()
            .build();

    ASMClass EntityLivingBase =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/EntityLivingBase")
            .autoAssign()
            .build();

    ASMClass Vec3d =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/Vec3d")
            .autoAssign()
            .build();

    ASMClass BlockRenderLayer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/BlockRenderLayer")
            .autoAssign()
            .build();

    ASMClass IBlockState =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/block/state/IBlockState")
            .autoAssign()
            .build();

    ASMClass BlockPos =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/BlockPos")
            .autoAssign()
            .build();

    ASMClass Block =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/block/Block")
            .autoAssign()
            .build();

    ASMClass ICamera =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/culling/ICamera")
            .autoAssign()
            .build();

    ASMClass VisGraph =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/VisGraph")
            .autoAssign()
            .build();

    ASMClass SetVisibility =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/SetVisibility")
            .autoAssign()
            .build();

    ASMClass Minecraft =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/Minecraft")
            .autoAssign()
            .build();

    ASMClass NetworkManager$4 =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/network/NetworkManager$4")
            .autoAssign()
            .build();

    ASMClass IBlockAccess =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/IBlockAccess")
            .autoAssign()
            .build();

    ASMClass BufferBuilder =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/BufferBuilder")
            .autoAssign()
            .build();

    ASMClass MoverType =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/MoverType")
            .autoAssign()
            .build();

    ASMClass WorldProvider =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/WorldProvider")
            .autoAssign()
            .build();

    ASMClass World =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/World")
            .autoAssign()
            .build();

    ASMClass IBakedModel =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/block/model/IBakedModel")
            .autoAssign()
            .build();

    ASMClass CompiledChunk =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/CompiledChunk")
            .autoAssign()
            .build();

    ASMClass RenderChunk =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/RenderChunk")
            .autoAssign()
            .build();

    ASMClass ChunkCompileTaskGenerator =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator")
            .autoAssign()
            .build();

    ASMClass ChunkCache =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/ChunkCache")
            .autoAssign()
            .build();

    ASMClass ViewFrustum =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/ViewFrustum")
            .autoAssign()
            .build();

    ASMClass ChunkRenderDispatcher =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher")
            .autoAssign()
            .build();

    ASMClass RenderGlobal =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/RenderGlobal")
            .autoAssign()
            .build();

    ASMClass ChunkRenderContainer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/ChunkRenderContainer")
            .autoAssign()
            .build();

    ASMClass ChunkRenderWorker =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/ChunkRenderWorker")
            .autoAssign()
            .build();

    ASMClass EntityPlayer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/player/EntityPlayer")
            .autoAssign()
            .build();

    ASMClass EntityPlayerSP =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/entity/EntityPlayerSP")
            .autoAssign()
            .build();

    ASMClass EntityBoat =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/item/EntityBoat")
            .autoAssign()
            .build();

    ASMClass EntityRenderer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/EntityRenderer")
            .autoAssign()
            .build();

    ASMClass RenderBoat =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/entity/RenderBoat")
            .autoAssign()
            .build();

    ASMClass NetworkManager =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/network/NetworkManager")
            .autoAssign()
            .build();

    ASMClass GuiPlayerTabOverlay =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/gui/GuiPlayerTabOverlay")
            .autoAssign()
            .build();

    ASMClass Scoreboard =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/scoreboard/Scoreboard")
            .autoAssign()
            .build();

    ASMClass ScoreObjective =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/scoreboard/ScoreObjective")
            .autoAssign()
            .build();

    ASMClass KeyBinding =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/settings/KeyBinding")
            .autoAssign()
            .build();

    ASMClass WorldClient =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/multiplayer/WorldClient")
            .autoAssign()
            .build();

    ASMClass ItemStack =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/item/ItemStack")
            .autoAssign()
            .build();

    ASMClass EnumFacing =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/EnumFacing")
            .autoAssign()
            .build();

    ASMClass EnumHand =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/EnumHand")
            .autoAssign()
            .build();

    ASMClass EnumSkyBlock =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/EnumSkyBlock")
            .autoAssign()
            .build();

    ASMClass PlayerControllerMP =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/multiplayer/PlayerControllerMP")
            .autoAssign()
            .build();
  }

  interface Fields {
    ASMField NetworkManager$4_val$inPacket =
        Classes.NetworkManager$4.childField()
            .setName("val$inPacket")
            .setType(Classes.Packet)
            .build();

    ASMField RenderGlobal_viewFrustum =
        Classes.RenderGlobal.childField()
            .setName("viewFrustum")
            .setType(Classes.ViewFrustum)
            .autoAssign()
            .build();
    ASMField RenderGlobal_renderDispatcher =
        Classes.RenderGlobal.childField()
            .setName("renderDispatcher")
            .setType(Classes.ChunkRenderDispatcher)
            .autoAssign()
            .build();
  }

  interface Methods {
    ASMMethod Block_canRenderInLayer =
        Classes.Block.childMethod()
            .setName("canRenderInLayer")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.IBlockState)
            .add(Classes.BlockRenderLayer)
            .finish()
            .autoAssign()
            .build();
    ASMMethod Block_addCollisionBoxToList =
        Classes.Block.childMethod()
            .setName("addCollisionBoxToList")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.IBlockState)
            .add(Classes.World)
            .add(Classes.BlockPos)
            .add(Classes.AxisAlignedBB)
            .add(List.class)
            .add(Classes.Entity)
            .add(boolean.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod ChunkRenderContainer_addRenderChunk =
        Classes.ChunkRenderContainer.childMethod()
            .setName("addRenderChunk")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.RenderChunk)
            .add(Classes.BlockRenderLayer)
            .finish()
            .autoAssign()
            .build();

    ASMMethod ChunkRenderDispatcher_uploadChunk =
        Classes.ChunkRenderDispatcher.childMethod()
            .setName("uploadChunk")
            .setReturnType(ListenableFuture.class)
            .beginParameters()
            .add(Classes.BlockRenderLayer)
            .add(Classes.BufferBuilder)
            .add(Classes.RenderChunk)
            .add(Classes.CompiledChunk)
            .add(double.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod ChunkRenderWorker_freeRenderBuilder =
        Classes.ChunkRenderWorker.childMethod()
            .setName("freeRenderBuilder")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.ChunkCompileTaskGenerator)
            .finish()
            .autoAssign()
            .build();

    ASMMethod Entity_applyEntityCollision =
        Classes.Entity.childMethod()
            .setName("applyEntityCollision")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .finish()
            .autoAssign()
            .build();
    ASMMethod Entity_move =
        Classes.Entity.childMethod()
            .setName("move")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.MoverType)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .finish()
            .autoAssign()
            .build();
    ASMMethod Entity_doBlockCollisions =
        Classes.Entity.childMethod()
            .setName("doBlockCollisions")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();

    ASMMethod EntityPlayerSP_onLivingUpdate =
        Classes.EntityPlayerSP.childMethod()
            .setName("onLivingUpdate")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();
    ASMMethod EntityPlayerSP_onUpdateWalkingPlayer =
        Classes.EntityPlayerSP.childMethod()
            .setName("onUpdateWalkingPlayer")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();
    ASMMethod EntityPlayerSP_pushOutOfBlocks =
        Classes.EntityPlayerSP.childMethod()
            .setName("pushOutOfBlocks")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod EntityPlayerSP_isRowingBoat =
        Classes.EntityPlayerSP.childMethod()
            .setName("isRowingBoat")
            .setReturnType(boolean.class)
            .emptyParameters()
            .autoAssign()
            .build();

    ASMMethod EntityRenderer_hurtCameraEffect =
        Classes.EntityRenderer.childMethod()
            .setName("hurtCameraEffect")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod Minecraft_setIngameFocus =
        Classes.Minecraft.childMethod()
            .setName("setIngameFocus")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();
    ASMMethod Minecraft_runTick =
        Classes.Minecraft.childMethod()
            .setName("runTick")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();
    ASMMethod Minecraft_sendClickBlockToController =
        Classes.Minecraft.childMethod()
            .setName("sendClickBlockToController")
            .setReturnType(void.class)
            .beginParameters()
            .add(boolean.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod NetworkManager$4_run =
        Classes.NetworkManager$4.childMethod()
            .setName("run")
            .setReturnType(void.class)
            .emptyParameters()
            .build(); // does not have an obfuscated or an srg name

    ASMMethod NetworkManager_dispatchPacket =
        Classes.NetworkManager.childMethod()
            .setName("dispatchPacket")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Packet)
            .add(GenericFutureListener[].class)
            .finish()
            .autoAssign()
            .build();
    ASMMethod NetworkManager_channelRead0 =
        Classes.NetworkManager.childMethod()
            .setName("channelRead0")
            .setObfuscatedName("a") // manually set because this isn't a vanilla method
            .setReturnType(void.class)
            .beginParameters()
            .add(ChannelHandlerContext.class)
            .add(Classes.Packet)
            .finish()
            // .autoAssign()
            .build();

    ASMMethod RenderChunk_rebuildChunk =
        Classes.RenderChunk.childMethod()
            .setName("rebuildChunk")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .add(Classes.ChunkCompileTaskGenerator)
            .finish()
            .autoAssign()
            .build();
    ASMMethod RenderChunk_deleteGlResources =
        Classes.RenderChunk.childMethod()
            .setName("deleteGlResources")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();

    ASMMethod RenderGlobal_loadRenderers =
        Classes.RenderGlobal.childMethod()
            .setName("loadRenderers")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();
    ASMMethod RenderGlobal_renderBlockLayer =
        Classes.RenderGlobal.childMethod()
            .setName("renderBlockLayer")
            .setReturnType(int.class)
            .beginParameters()
            .add(Classes.BlockRenderLayer)
            .add(double.class)
            .add(int.class)
            .add(Classes.Entity)
            .finish()
            .autoAssign()
            .build();
    ASMMethod RenderGlobal_setupTerrain =
        Classes.RenderGlobal.childMethod()
            .setName("setupTerrain")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .add(double.class)
            .add(Classes.ICamera)
            .add(int.class)
            .add(boolean.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod BufferBuilder_putColorMultiplier =
        Classes.BufferBuilder.childMethod()
            .setName("putColorMultiplier")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .add(int.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod VisGraph_setOpaqueCube =
        Classes.VisGraph.childMethod()
            .setName("setOpaqueCube")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.BlockPos)
            .finish()
            .autoAssign()
            .build();
    ASMMethod VisGraph_computeVisibility =
        Classes.VisGraph.childMethod()
            .setName("computeVisibility")
            .setReturnType(Classes.SetVisibility)
            .emptyParameters()
            .autoAssign()
            .build();

    ASMMethod World_handleMaterialAcceleration =
        Classes.World.childMethod()
            .setName("handleMaterialAcceleration")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.AxisAlignedBB)
            .add(Classes.Material)
            .add(Classes.Entity)
            .finish()
            .autoAssign()
            .build();
    ASMMethod World_checkLightFor =
        Classes.World.childMethod()
            .setName("checkLightFor")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.EnumSkyBlock)
            .add(Classes.BlockPos)
            .finish()
            .autoAssign()
            .build();

    ASMMethod EntityBoat_updateMotion =
        Classes.EntityBoat.childMethod()
            .setName("updateMotion")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();

    ASMMethod EntityBoat_controlBoat =
        Classes.EntityBoat.childMethod()
            .setName("controlBoat")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();

    ASMMethod EntityBoat_applyYawToEntity =
        Classes.EntityBoat.childMethod()
            .setName("applyYawToEntity")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .finish()
            .autoAssign()
            .build();

    ASMMethod RenderBoat_doRender =
        Classes.RenderBoat.childMethod()
            .setName("doRender")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EntityBoat)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .add(float.class)
            .add(float.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod PlayerTabOverlay_renderPlayerList =
        Classes.GuiPlayerTabOverlay.childMethod()
            .setName("renderPlayerlist")
            .setReturnType(void.class)
            .beginParameters()
            .add(int.class)
            .add(Classes.Scoreboard)
            .add(Classes.ScoreObjective)
            .finish()
            .autoAssign()
            .build();

    ASMMethod KeyBinding_isKeyDown =
        Classes.KeyBinding.childMethod()
            .setName("isKeyDown")
            .setReturnType(boolean.class)
            .emptyParameters()
            .autoAssign()
            .build();

    ASMMethod PlayerControllerMC_syncCurrentPlayItem =
        Classes.PlayerControllerMP.childMethod()
            .setName("syncCurrentPlayItem")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();
    ASMMethod PlayerControllerMC_attackEntity =
        Classes.PlayerControllerMP.childMethod()
            .setName("attackEntity")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EntityPlayer)
            .add(Classes.Entity)
            .finish()
            .build();
    ASMMethod PlayerControllerMC_onPlayerDamageBlock =
        Classes.PlayerControllerMP.childMethod()
            .setName("onPlayerDamageBlock")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.BlockPos)
            .add(Classes.EnumFacing)
            .finish()
            .build();
  }
}
