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
  // classes no longer have any obfuscated name
  interface Classes {
    ASMClass Packet =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/network/Packet")
            .build();

    ASMClass AxisAlignedBB =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/AxisAlignedBB")
            .build();

    ASMClass Material =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/block/material/Material")
            .build();

    ASMClass Entity =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/Entity")
            .build();

    ASMClass EntityLivingBase =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/EntityLivingBase")
            .build();

    ASMClass Vec3d =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/Vec3d")
            .build();

    ASMClass BlockRenderLayer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/BlockRenderLayer")
            .build();

    ASMClass IBlockState =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/block/state/IBlockState")
            .build();

    ASMClass BlockPos =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/BlockPos")
            .build();

    ASMClass Block =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/block/Block")
            .build();

    ASMClass ICamera =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/culling/ICamera")
            .build();

    ASMClass VisGraph =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/VisGraph")
            .build();

    ASMClass SetVisibility =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/SetVisibility")
            .build();

    ASMClass Minecraft =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/Minecraft")
            .build();

    ASMClass BufferBuilder =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/BufferBuilder")
            .build();

    ASMClass MoverType =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/MoverType")
            .build();

    ASMClass World =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/World")
            .build();

    ASMClass IBakedModel =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/model/IBakedModel")
            .build();

    ASMClass CompiledChunk =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/CompiledChunk")
            .build();

    ASMClass RenderChunk =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/RenderChunk")
            .build();

    ASMClass ChunkRenderTask =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/ChunkRenderTask")
            .build();

    ASMClass ViewFrustum =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/ViewFrustum")
            .build();

    ASMClass ChunkRenderDispatcher =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher")
            .build();

    ASMClass WorldRenderer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/WorldRenderer")
            .build();

    ASMClass ChunkRenderContainer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/ChunkRenderContainer")
            .build();

    ASMClass ChunkRenderWorker =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/chunk/ChunkRenderWorker")
            .build();

    ASMClass EntityPlayer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/player/EntityPlayer")
            .build();

    ASMClass EntityPlayerSP =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/entity/EntityPlayerSP")
            .build();

    ASMClass EntityBoat =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/entity/item/EntityBoat")
            .build();

    ASMClass GameRenderer =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/GameRenderer")
            .build();

    ASMClass RenderBoat =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/entity/RenderBoat")
            .build();

    ASMClass NetworkManager =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/network/NetworkManager")
            .build();

    ASMClass GuiScreen =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/gui/GuiScreen")
            .build();

    ASMClass GuiMainMenu =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/gui/GuiMainMenu")
            .build();

    ASMClass GuiPlayerTabOverlay =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/gui/GuiPlayerTabOverlay")
            .build();

    ASMClass Scoreboard =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/scoreboard/Scoreboard")
            .build();

    ASMClass ScoreObjective =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/scoreboard/ScoreObjective")
            .build();

    ASMClass KeyBinding =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/settings/KeyBinding")
            .build();

    ASMClass WorldClient =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/multiplayer/WorldClient")
            .build();

    ASMClass ItemStack =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/item/ItemStack")
            .build();

    ASMClass EnumFacing =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/EnumFacing")
            .build();

    ASMClass EnumHand =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/EnumHand")
            .build();

    ASMClass PlayerControllerMP =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/multiplayer/PlayerControllerMP")
            .build();
  }

  interface Fields {
    ASMField WorldRenderer_viewFrustum =
        Classes.WorldRenderer.childField()
            .setName("viewFrustum")
            .setType(Classes.ViewFrustum)
            .autoAssign()
            .build();
    ASMField WorldRenderer_renderDispatcher =
        Classes.WorldRenderer.childField()
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
    // TODO: replaced with getCollisionShape
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
            .add(Classes.ChunkRenderTask)
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
    ASMMethod EntityPlayerSP_onUpdate =
        Classes.EntityPlayerSP.childMethod()
            .setName("onUpdate")
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

    ASMMethod EntityLivingBase_travel =
        Classes.EntityLivingBase.childMethod()
            .setName("travel")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .finish()
            .autoAssign()
            .build();

    ASMMethod GameRenderer_hurtCameraEffect =
        Classes.GameRenderer.childMethod()
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
            .beginParameters()
            .add(float.class)
            .finish()
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
            .setReturnType(void.class)
            .beginParameters()
            .add(ChannelHandlerContext.class)
            .add(Classes.Packet)
            .finish()
             //.autoAssign()
            .build();

    ASMMethod RenderChunk_rebuildChunk =
        Classes.RenderChunk.childMethod()
            .setName("rebuildChunk")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .add(Classes.ChunkRenderTask)
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

    ASMMethod WorldRenderer_loadRenderers =
        Classes.WorldRenderer.childMethod()
            .setName("loadRenderers")
            .setReturnType(void.class)
            .emptyParameters()
            .autoAssign()
            .build();
    ASMMethod WorldRenderer_renderBlockLayer =
        Classes.WorldRenderer.childMethod()
            .setName("renderBlockLayer")
            .setReturnType(int.class)
            .beginParameters()
            .add(Classes.BlockRenderLayer)
            .add(double.class)
            .add(Classes.Entity)
            .finish()
            .autoAssign()
            .build();
    ASMMethod WorldRenderer_setupTerrain =
        Classes.WorldRenderer.childMethod()
            .setName("setupTerrain")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .add(float.class)
            .add(Classes.ICamera)
            .add(int.class)
            .add(boolean.class)
            .finish()
            .autoAssign()
            .build();
    ASMMethod WorldRenderer_drawBoundingBox =
        Classes.WorldRenderer.childMethod()
            .setName("drawBoundingBox")
            .setReturnType(void.class)
            .beginParameters()
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .add(float.class)
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
            .autoAssign()
            .build();
    ASMMethod PlayerControllerMC_onPlayerDamageBlock =
        Classes.PlayerControllerMP.childMethod()
            .setName("onPlayerDamageBlock")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.BlockPos)
            .add(Classes.EnumFacing)
            .finish()
            .autoAssign()
            .build();
    ASMMethod PlayerControllerMC_onStoppedUsingItem =
        Classes.PlayerControllerMP.childMethod()
            .setName("onStoppedUsingItem")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EntityPlayer)
            .finish()
            .autoAssign()
            .build();
  }
}
