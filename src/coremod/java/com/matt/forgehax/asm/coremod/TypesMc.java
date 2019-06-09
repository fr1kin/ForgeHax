package com.matt.forgehax.asm.coremod;

import com.google.common.util.concurrent.ListenableFuture;
import com.matt.forgehax.asm.coremod.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.coremod.utils.asmtype.ASMField;
import com.matt.forgehax.asm.coremod.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.coremod.utils.asmtype.builders.ASMBuilders;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;

/** Created on 5/27/2017 by fr1kin */
public interface TypesMc {
  // classes no longer have any obfuscated name
  interface Classes {
    /*ASMClass Main = // seems to be impossible to transform
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/main/Main")
            .build();*/

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

    ASMClass KeyboardListener =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/KeyboardListener")
            .build();

    ASMClass EnumConnectionState =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/network/EnumConnectionState")
            .build();

    ASMClass VoxelShape =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/shapes/VoxelShape")
            .build();

    ASMClass IBlockReader =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/IBlockReader")
            .build();

    ASMClass RenderHelper =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/RenderHelper")
            .build();

    ASMClass ActiveRenderInfo =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/ActiveRenderInfo")
            .build();

    ASMClass GLAllocation =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/client/renderer/GLAllocation")
            .build();

    ASMClass RayTraceResult =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/math/RayTraceResult")
            .build();

    ASMClass IWorldReader =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/world/IWorldReader")
            .build();

    ASMClass ClientModLoader =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraftforge/fml/client/ClientModLoader")
            .build();

    ASMClass Session =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/Session")
            .build();

    ASMClass Session$Type  =
        ASMBuilders.newClassBuilder()
            .setClassName("net/minecraft/util/Session$Type")
            .build();
  }

  interface Fields {
    ASMField WorldRenderer_viewFrustum =
        Classes.WorldRenderer.childField()
            .setName("viewFrustum")
            .setSrgName("field_175008_n")
            .setType(Classes.ViewFrustum)
            //.autoAssign()
            .build();
    ASMField WorldRenderer_renderDispatcher =
        Classes.WorldRenderer.childField()
            .setName("renderDispatcher")
            .setSrgName("field_174995_M")
            .setType(Classes.ChunkRenderDispatcher)
            //.autoAssign()
            .build();

    ASMField Minecraft_renderChunksMany =
        Classes.Minecraft.childField()
            .setName("renderChunksMany")
            .setSrgName("field_175612_E")
            .setType(boolean.class)
            .build();
  }

  interface Methods {
    /*ASMMethod Main_main =
        Classes.Main.childMethod()
            .setName("main")
            .setReturnType(void.class)
            .beginParameters()
            .add(String[].class)
            .finish()
            //.autoAssign()
            .build();*/
    ASMMethod Minecraft_run =
        Classes.Minecraft.childMethod()
            .setName("run")
            .setSrgName("func_99999_d")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();

    @Deprecated // doesnt seem to exist anymore
    ASMMethod Block_canRenderInLayer =
        Classes.Block.childMethod()
            .setName("canRenderInLayer")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.IBlockState)
            .add(Classes.BlockRenderLayer)
            .finish()
            //.autoAssign()
            .build();
    // TODO: replaced with getCollisionShape
    @Deprecated
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
            //.autoAssign()
            .build();

    ASMMethod ChunkRenderContainer_addRenderChunk =
        Classes.ChunkRenderContainer.childMethod()
            .setName("addRenderChunk")
            .setSrgName("func_178002_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.RenderChunk)
            .add(Classes.BlockRenderLayer)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod ChunkRenderDispatcher_uploadChunk =
        Classes.ChunkRenderDispatcher.childMethod()
            .setName("uploadChunk")
            .setSrgName("func_188245_a")
            .setReturnType(ListenableFuture.class)
            .beginParameters()
            .add(Classes.BlockRenderLayer)
            .add(Classes.BufferBuilder)
            .add(Classes.RenderChunk)
            .add(Classes.CompiledChunk)
            .add(double.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod ChunkRenderWorker_freeRenderBuilder =
        Classes.ChunkRenderWorker.childMethod()
            .setName("freeRenderBuilder")
            .setSrgName("func_178473_b")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.ChunkRenderTask)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod Entity_applyEntityCollision =
        Classes.Entity.childMethod()
            .setName("applyEntityCollision")
            .setSrgName("func_70108_f")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod Entity_move =
        Classes.Entity.childMethod()
            .setName("move")
            .setSrgName("func_70091_d")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.MoverType)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod Entity_doBlockCollisions =
        Classes.Entity.childMethod()
            .setName("doBlockCollisions")
            .setSrgName("func_145775_I")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod Entity_isBeingRidden =
        Classes.Entity.childMethod()
            .setName("isBeingRidden")
            .setSrgName("func_184207_aI")
            .setReturnType(boolean.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod Entity_isPassenger =
        Classes.Entity.childMethod()
            .setName("isPassenger")
            .setSrgName("func_184218_aH")
            .setReturnType(boolean.class)
            .emptyParameters()
            //.autoAssign()
            .build();

    ASMMethod EntityPlayerSP_livingTick =
        Classes.EntityPlayerSP.childMethod()
            .setName("livingTick")
            .setSrgName("func_70636_d")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod EntityPlayerSP_tick =
        Classes.EntityPlayerSP.childMethod()
            .setName("tick")
            .setSrgName("func_70071_h_")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod EntityPlayerSP_onUpdateWalkingPlayer =
        Classes.EntityPlayerSP.childMethod()
            .setName("onUpdateWalkingPlayer")
            .setSrgName("func_175161_p")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod EntityPlayerSP_pushOutOfBlocks =
        Classes.EntityPlayerSP.childMethod()
            .setName("pushOutOfBlocks")
            .setSrgName("func_145771_j")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod EntityPlayerSP_isRowingBoat =
        Classes.EntityPlayerSP.childMethod()
            .setName("isRowingBoat")
            .setSrgName("func_184838_M")
            .setReturnType(boolean.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod EntityPlayerSP_isHandActive =
        Classes.EntityPlayerSP.childMethod()
            .setName("isHandActive")
            .setSrgName("func_184587_cr")
            .setReturnType(boolean.class)
            .emptyParameters()
            //.autoAssign()
            .build();

    ASMMethod EntityLivingBase_travel =
        Classes.EntityLivingBase.childMethod()
            .setName("travel")
            .setSrgName("func_191986_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod GameRenderer_hurtCameraEffect =
        Classes.GameRenderer.childMethod()
            .setName("hurtCameraEffect")
            .setSrgName("func_78482_e")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod Minecraft_setIngameFocus =
        Classes.Minecraft.childMethod()
            .setName("setIngameFocus")
            .setSrgName("func_71381_h")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod Minecraft_runTick =
        Classes.Minecraft.childMethod()
            .setName("runTick")
            .setSrgName("func_71407_l")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod Minecraft_sendClickBlockToController =
        Classes.Minecraft.childMethod()
            .setName("sendClickBlockToController")
            .setSrgName("func_147115_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(boolean.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod Minecraft_init =
        Classes.Minecraft.childMethod()
            .setName("init")
            .setSrgName("func_71384_a")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();

    ASMMethod NetworkManager_dispatchPacket =
        Classes.NetworkManager.childMethod()
            .setName("dispatchPacket")
            .setSrgName("func_150732_b")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Packet)
            .add(GenericFutureListener.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod NetworkManager_lambda$dispatchPacket$4 =
        Classes.NetworkManager.childMethod()
            .setName("lambda$dispatchPacket$4")
            .setSrgName("lambda$dispatchPacket$4")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EnumConnectionState)
            .add(Classes.EnumConnectionState)
            .add(Classes.Packet)
            .add(GenericFutureListener.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod NetworkManager_channelRead0 =
        Classes.NetworkManager.childMethod()
            .setName("channelRead0")
            .setSrgName("channelRead0")
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
            .setSrgName("func_178581_b")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .add(Classes.ChunkRenderTask)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod RenderChunk_deleteGlResources =
        Classes.RenderChunk.childMethod()
            .setName("deleteGlResources")
            .setSrgName("func_178566_a")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();

    ASMMethod WorldRenderer_loadRenderers =
        Classes.WorldRenderer.childMethod()
            .setName("loadRenderers")
            .setSrgName("func_72712_a")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod WorldRenderer_renderBlockLayer =
        Classes.WorldRenderer.childMethod()
            .setName("renderBlockLayer")
            .setSrgName("func_174977_a")
            .setReturnType(int.class)
            .beginParameters()
            .add(Classes.BlockRenderLayer)
            .add(double.class)
            .add(Classes.Entity)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod WorldRenderer_setupTerrain =
        Classes.WorldRenderer.childMethod()
            .setName("setupTerrain")
            .setSrgName("func_174970_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .add(float.class)
            .add(Classes.ICamera)
            .add(int.class)
            .add(boolean.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod WorldRenderer_drawSelectionBox =
        Classes.WorldRenderer.childMethod()
            .setName("drawSelectionBox")
            .setSrgName("func_72731_b")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EntityPlayer)
            .add(Classes.RayTraceResult)
            .add(int.class)
            .add(float.class)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod WorldRenderer_drawShape =
        Classes.WorldRenderer.childMethod()
            .setName("drawShape")
            .setSrgName("func_195463_b")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.VoxelShape)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod BufferBuilder_putColorMultiplier =
        Classes.BufferBuilder.childMethod()
            .setName("putColorMultiplier")
            .setSrgName("func_178978_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(float.class)
            .add(float.class)
            .add(float.class)
            .add(int.class)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod VisGraph_setOpaqueCube =
        Classes.VisGraph.childMethod()
            .setName("setOpaqueCube")
            .setSrgName("func_178606_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.BlockPos)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod VisGraph_computeVisibility =
        Classes.VisGraph.childMethod()
            .setName("computeVisibility")
            .setSrgName("func_178607_a")
            .setReturnType(Classes.SetVisibility)
            .emptyParameters()
            //.autoAssign()
            .build();

    ASMMethod World_handleMaterialAcceleration =
        Classes.World.childMethod()
            .setName("handleMaterialAcceleration")
            .setSrgName("func_72918_a")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.AxisAlignedBB)
            .add(Classes.Material)
            .add(Classes.Entity)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod EntityBoat_updateMotion =
        Classes.EntityBoat.childMethod()
            .setName("updateMotion")
            .setSrgName("func_184450_w")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod EntityBoat_controlBoat =
        Classes.EntityBoat.childMethod()
            .setName("controlBoat")
            .setSrgName("func_184443_x")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod EntityBoat_applyYawToEntity =
        Classes.EntityBoat.childMethod()
            .setName("applyYawToEntity")
            .setSrgName("func_184454_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod RenderBoat_doRender =
        Classes.RenderBoat.childMethod()
            .setName("doRender")
            .setSrgName("func_76986_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EntityBoat)
            .add(double.class)
            .add(double.class)
            .add(double.class)
            .add(float.class)
            .add(float.class)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod PlayerTabOverlay_renderPlayerList =
        Classes.GuiPlayerTabOverlay.childMethod()
            .setName("renderPlayerlist")
            .setSrgName("func_175249_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(int.class)
            .add(Classes.Scoreboard)
            .add(Classes.ScoreObjective)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod KeyBinding_isKeyDown =
        Classes.KeyBinding.childMethod()
            .setName("isKeyDown")
            .setSrgName("func_151470_d")
            .setReturnType(boolean.class)
            .emptyParameters()
            //.autoAssign()
            .build();

    ASMMethod PlayerControllerMC_syncCurrentPlayItem =
        Classes.PlayerControllerMP.childMethod()
            .setName("syncCurrentPlayItem")
            .setSrgName("func_78750_j")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();
    ASMMethod PlayerControllerMC_attackEntity =
        Classes.PlayerControllerMP.childMethod()
            .setName("attackEntity")
            .setSrgName("func_78764_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EntityPlayer)
            .add(Classes.Entity)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod PlayerControllerMC_onPlayerDamageBlock =
        Classes.PlayerControllerMP.childMethod()
            .setName("onPlayerDamageBlock")
            .setSrgName("func_180512_c")
            .setReturnType(boolean.class)
            .beginParameters()
            .add(Classes.BlockPos)
            .add(Classes.EnumFacing)
            .finish()
            //.autoAssign()
            .build();
    ASMMethod PlayerControllerMC_onStoppedUsingItem =
        Classes.PlayerControllerMP.childMethod()
            .setName("onStoppedUsingItem")
            .setSrgName("func_78766_c")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.EntityPlayer)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod KeyboardListener_onKeyEvent =
        Classes.KeyboardListener.childMethod()
            .setName("onKeyEvent")
            .setSrgName("func_197961_a")
            .setReturnType(void.class)
            .beginParameters()
            .add(long.class)
            .add(int.class)
            .add(int.class)
            .add(int.class)
            .add(int.class)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod Block_getCollisionShape =
        Classes.Block.childMethod()
            .setName("getCollisionShape")
            .setSrgName("func_196268_f")
            .setReturnType(Classes.VoxelShape)
            .beginParameters()
            .add(Classes.IBlockState)
            .add(Classes.IBlockReader)
            .add(Classes.BlockPos)
            .finish()
            //.autoAssign()
            .build();

    ASMMethod RenderHelper_disableStandardItemLighting =
        Classes.RenderHelper.childMethod()
            .setName("disableStandardItemLighting")
            .setSrgName("func_74518_a")
            .setReturnType(void.class)
            .emptyParameters()
            //.autoAssign()
            .build();

    ASMMethod ActiveRenderInfo_updateRenderInfo =
        Classes.ActiveRenderInfo.childMethod()
            .setName("updateRenderInfo")
            .setSrgName("updateRenderInfo")
            .setReturnType(void.class)
            .beginParameters()
            .add(Classes.Entity)
            .add(boolean.class)
            .add(float.class)
            .finish()
            //.autoAssign()
            .build();

    // unsure of this
    ASMMethod IBlockState_getSlipperiness =
        Classes.IBlockState.childMethod()
            .setName("getSlipperiness")
            .setSrgName("getSlipperiness")
            .setReturnType(float.class)
            .beginParameters()
            .add(Classes.IWorldReader)
            .add(Classes.BlockPos)
            .add(Classes.Entity)
            .finish()
            //.autoAssign()
            .build();

  }
}
