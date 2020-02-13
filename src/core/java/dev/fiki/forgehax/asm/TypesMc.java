package dev.fiki.forgehax.asm;

import dev.fiki.forgehax.common.asmtype.ASMClass;
import dev.fiki.forgehax.common.asmtype.ASMField;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

/**
 * Created on 5/27/2017 by fr1kin
 */
public interface TypesMc {

  interface Classes {

    ASMClass Packet =
        ASMClass.builder()
            .className("net/minecraft/network/IPacket")
            .build();

    ASMClass AxisAlignedBB =
        ASMClass.builder()
            .className("net/minecraft/util/math/AxisAlignedBB")
            .build();

    ASMClass Material =
        ASMClass.builder()
            .className("net/minecraft/block/material/Material")
            .build();

    ASMClass Entity =
        ASMClass.builder()
            .className("net/minecraft/entity/Entity")
            .build();

    ASMClass LivingEntity =
        ASMClass.builder()
            .className("net/minecraft/entity/LivingEntity")
            .build();

    ASMClass Vec3d =
        ASMClass.builder()
            .className("net/minecraft/util/math/Vec3d")
            .build();

    ASMClass BlockState =
        ASMClass.builder()
            .className("net/minecraft/block/BlockState")
            .build();

    ASMClass BlockPos =
        ASMClass.builder()
            .className("net/minecraft/util/math/BlockPos")
            .build();

    ASMClass Block =
        ASMClass.builder()
            .className("net/minecraft/block/Block")
            .build();

    ASMClass VisGraph =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/VisGraph")
            .build();

    ASMClass SetVisibility =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/SetVisibility")
            .build();

    ASMClass Minecraft =
        ASMClass.builder()
            .className("net/minecraft/client/Minecraft")
            .build();

    ASMClass IBlockReader =
        ASMClass.builder()
            .className("net/minecraft/world/IBlockReader")
            .build();

    ASMClass BufferBuilder =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/BufferBuilder")
            .build();

    ASMClass MoverType =
        ASMClass.builder()
            .className("net/minecraft/entity/MoverType")
            .build();

    ASMClass World =
        ASMClass.builder()
            .className("net/minecraft/world/World")
            .build();

    ASMClass IBakedModel =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/model/IBakedModel")
            .build();

    @Deprecated
    ASMClass CompiledChunk =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/CompiledChunk")
            .build();

    @Deprecated
    ASMClass RenderChunk =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/RenderChunk")
            .build();

    @Deprecated
    ASMClass ChunkCompileTaskGenerator =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator")
            .build();

    ASMClass ChunkRenderCache =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/ChunkRenderCache")
            .build();

    ASMClass ViewFrustum =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/ViewFrustum")
            .build();

    ASMClass ChunkRenderDispatcher =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher")
            .build();

    ASMClass WorldRenderer =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/WorldRenderer")
            .build();

    @Deprecated
    ASMClass ChunkRenderContainer =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/ChunkRenderContainer")
            .build();

    @Deprecated
    ASMClass ChunkRenderWorker =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/chunk/ChunkRenderWorker")
            .build();

    ASMClass PlayerEntity =
        ASMClass.builder()
            .className("net/minecraft/entity/player/PlayerEntity")
            .build();

    ASMClass ClientPlayerEntity =
        ASMClass.builder()
            .className("net/minecraft/client/entity/player/ClientPlayerEntity")
            .build();

    ASMClass BoatEntity =
        ASMClass.builder()
            .className("net/minecraft/entity/item/BoatEntity")
            .build();

    ASMClass EntityRenderer =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/entity/EntityRenderer")
            .build();

    ASMClass BoatRenderer =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/entity/BoatRenderer")
            .build();

    ASMClass NetworkManager =
        ASMClass.builder()
            .className("net/minecraft/network/NetworkManager")
            .build();

    ASMClass Screen =
        ASMClass.builder()
            .className("net/minecraft/client/gui/screen/Screen")
            .build();

    ASMClass MainMenuScreen =
        ASMClass.builder()
            .className("net/minecraft/client/gui/screen/MainMenuScreen")
            .build();

    ASMClass PlayerTabOverlayGui =
        ASMClass.builder()
            .className("net/minecraft/client/gui/overlay/PlayerTabOverlayGui")
            .build();

    ASMClass Scoreboard =
        ASMClass.builder()
            .className("net/minecraft/scoreboard/Scoreboard")
            .build();

    ASMClass ScoreObjective =
        ASMClass.builder()
            .className("net/minecraft/scoreboard/ScoreObjective")
            .build();

    ASMClass KeyBinding =
        ASMClass.builder()
            .className("net/minecraft/client/settings/KeyBinding")
            .build();

    ASMClass ClientWorld =
        ASMClass.builder()
            .className("net/minecraft/client/world/ClientWorld")
            .build();

    ASMClass ItemStack =
        ASMClass.builder()
            .className("net/minecraft/item/ItemStack")
            .build();

    ASMClass Direction =
        ASMClass.builder()
            .className("net/minecraft/util/Direction")
            .build();

    ASMClass Hand =
        ASMClass.builder()
            .className("net/minecraft/util/Hand")
            .build();

    @Deprecated
    ASMClass EnumSkyBlock =
        ASMClass.builder()
            .className("net/minecraft/world/EnumSkyBlock")
            .build();

    ASMClass PlayerController =
        ASMClass.builder()
            .className("net/minecraft/client/multiplayer/PlayerController")
            .build();

    ASMClass MatrixStack =
        ASMClass.builder()
            .className("com/mojang/blaze3d/matrix/MatrixStack")
            .build();

    ASMClass Matrix4f =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/Matrix4f")
            .build();

    ASMClass IRenderTypeBuffer =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/IRenderTypeBuffer")
            .build();

    ASMClass ISelectionContext =
        ASMClass.builder()
            .className("net/minecraft/util/math/shapes/ISelectionContext")
            .build();

    ASMClass GameRenderer =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/GameRenderer")
            .build();

    ASMClass ActiveRenderInfo =
        ASMClass.builder()
            .className("net/minecraft/client/renderer/ActiveRenderInfo")
            .build();
  }

  interface Fields {
    ASMField Minecraft_leftClickCounter =
        Classes.Minecraft.newChildField()
            .mcp("leftClickCounter")
            .srg("field_71429_W")
            .type(int.class)
            .build();

    ASMField BoatEntity_leftInputDown =
        Classes.BoatEntity.newChildField()
            .mcp("leftInputDown")
            .srg("field_184480_az")
            .type(boolean.class)
            .build();

    ASMField BoatEntity_rightInputDown =
        Classes.BoatEntity.newChildField()
            .mcp("rightInputDown")
            .srg("field_184459_aA")
            .type(boolean.class)
            .build();
  }

  interface Methods {
    @Deprecated
    ASMMethod Block_canRenderInLayer =
        Classes.Block.newChildMethod()
            .mcp("canRenderInLayer")
            .returns(boolean.class)
            .argument(Classes.BlockState)
            //.argument(Classes.BlockRenderLayer)
            .build();

    ASMMethod Block_getCollisionShape =
        Classes.Block.newChildMethod()
            .mcp("getCollisionShape")
            .srg("func_220071_b")
            .returnsVoid()
            .argument(Classes.BlockState)
            .argument(Classes.IBlockReader)
            .argument(Classes.BlockPos)
            .argument(Classes.AxisAlignedBB)
            .argument(Classes.ISelectionContext)
            .build();

    @Deprecated
    ASMMethod ChunkRenderContainer_addRenderChunk =
        Classes.ChunkRenderContainer.newChildMethod()
            .mcp("addRenderChunk")
            .returnsVoid()
            .argument(Classes.RenderChunk)
//            .argument(Classes.BlockRenderLayer)
            .build();

    @Deprecated
    ASMMethod ChunkRenderDispatcher_uploadChunk =
        Classes.ChunkRenderDispatcher.newChildMethod()
            .mcp("uploadChunk")
            .returns("com/google/common/util/concurrent/ListenableFuture")
//            .argument(Classes.BlockRenderLayer)
            .argument(Classes.BufferBuilder)
            .argument(Classes.RenderChunk)
            .argument(Classes.CompiledChunk)
            .argument(double.class)
            .build();

    @Deprecated
    ASMMethod ChunkRenderWorker_freeRenderBuilder =
        Classes.ChunkRenderWorker.newChildMethod()
            .mcp("freeRenderBuilder")
            .returnsVoid()
            .argument(Classes.ChunkCompileTaskGenerator)
            .build();

    ASMMethod Entity_applyEntityCollision =
        Classes.Entity.newChildMethod()
            .mcp("applyEntityCollision")
            .srg("func_70108_f")
            .returnsVoid()
            .argument(Classes.Entity)
            .build();

    ASMMethod Entity_move =
        Classes.Entity.newChildMethod()
            .mcp("move")
            .srg("func_213315_a")
            .returnsVoid()
            .argument(Classes.MoverType)
            .argument(double.class)
            .argument(double.class)
            .argument(double.class)
            .build();

    ASMMethod Entity_isSteppingCarefully =
        Classes.Entity.newChildMethod()
            .mcp("isSteppingCarefully")
            .srg("func_226271_bk_")
            .returns(boolean.class)
            .noArguments()
            .build();

    ASMMethod Entity_doBlockCollisions =
        Classes.Entity.newChildMethod()
            .mcp("doBlockCollisions")
            .srg("func_145775_I")
            .returnsVoid()
            .noArguments()
            .build();

    ASMMethod ClientPlayerEntity_livingTick =
        Classes.ClientPlayerEntity.newChildMethod()
            // mcp 1.12 -> onLivingUpdate
            .mcp("livingTick")
            .srg("func_70636_d")
            .returnsVoid()
            .noArguments()
            .build();

    ASMMethod ClientPlayerEntity_tick =
        Classes.ClientPlayerEntity.newChildMethod()
            // mcp 1.12 -> onUpdate
            .mcp("tick")
            .srg("func_70071_h_")
            .returnsVoid()
            .noArguments()
            .build();

    ASMMethod ClientPlayerEntity_onUpdateWalkingPlayer =
        Classes.ClientPlayerEntity.newChildMethod()
            .mcp("onUpdateWalkingPlayer")
            .srg("func_175161_p")
            .returnsVoid()
            .noArguments()
            .build();

    ASMMethod ClientPlayerEntity_pushOutOfBlocks =
        Classes.ClientPlayerEntity.newChildMethod()
            .mcp("pushOutOfBlocks")
            .srg("func_213282_i")
            .returnsVoid()
            .argument(double.class)
            .argument(double.class)
            .argument(double.class)
            .build();

    ASMMethod ClientPlayerEntity_isRowingBoat =
        Classes.ClientPlayerEntity.newChildMethod()
            .mcp("isRowingBoat")
            .srg("func_184838_M")
            .returns(boolean.class)
            .noArguments()
            .build();

    ASMMethod LivingEntity_travel =
        Classes.LivingEntity.newChildMethod()
            .mcp("travel")
            .srg("func_213352_e")
            .returnsVoid()
            .argument(Classes.Vec3d)
            .build();

    // MOVED to GameRenderer
    @Deprecated
    ASMMethod EntityRenderer_hurtCameraEffect =
        Classes.EntityRenderer.newChildMethod()
            .mcp("hurtCameraEffect")
            .srg("troll")
            .returnsVoid()
            .argument(float.class)
            .build();

    @Deprecated
    ASMMethod Minecraft_setIngameFocus =
        Classes.Minecraft.newChildMethod()
            .mcp("setIngameFocus")
            .returnsVoid()
            .build();

    ASMMethod Minecraft_runTick =
        Classes.Minecraft.newChildMethod()
            .mcp("runTick")
            .srg("func_71407_l")
            .returnsVoid()
            .build();

    ASMMethod Minecraft_sendClickBlockToController =
        Classes.Minecraft.newChildMethod()
            .mcp("sendClickBlockToController")
            .srg("func_147115_a")
            .returnsVoid()
            .argument(boolean.class)
            .build();

//    ASMMethod NetworkManager$4_run =
//        Classes.NetworkManager$4.newChildMethod()
//            .name("run")
//            .returnsVoid()
//            .emptyParameters()
//            .build(); // does not have an obfuscated or an srg name

    ASMMethod NetworkManager_dispatchPacket =
        Classes.NetworkManager.newChildMethod()
            .mcp("dispatchPacket")
            .srg("func_150732_b")
            .returnsVoid()
            .argument(Classes.Packet)
            .argument("io/netty/util/concurrent/GenericFutureListener")
            .build();

    ASMMethod NetworkManager_channelRead0 =
        Classes.NetworkManager.newChildMethod()
            .name("channelRead0")
            // does not appear to have a searge name
            .returnsVoid()
            .argument("io/netty/channel/ChannelHandlerContext")
            .argument(Classes.Packet)
            .build();

    @Deprecated
    ASMMethod RenderChunk_rebuildChunk =
        Classes.RenderChunk.newChildMethod()
            .mcp("rebuildChunk")
            .returnsVoid()
            .argument(float.class)
            .argument(float.class)
            .argument(float.class)
            .argument(Classes.ChunkCompileTaskGenerator)
            .build();

    @Deprecated
    ASMMethod RenderChunk_deleteGlResources =
        Classes.RenderChunk.newChildMethod()
            .mcp("deleteGlResources")
            .returnsVoid()
            .build();

    @Deprecated
    ASMMethod RenderGlobal_loadRenderers =
        Classes.WorldRenderer.newChildMethod()
            .mcp("loadRenderers")
            .returnsVoid()
            .build();

    @Deprecated
    ASMMethod RenderGlobal_renderBlockLayer =
        Classes.WorldRenderer.newChildMethod()
            .mcp("renderBlockLayer")
            .returns(int.class)
//            .argument(Classes.BlockRenderLayer)
            .argument(double.class)
            .argument(int.class)
            .argument(Classes.Entity)
            .build();

    @Deprecated // hooking this probably wont give the desired effect anymore
    ASMMethod WorldRenderer_drawBoundingBox =
        Classes.WorldRenderer.newChildMethod()
            .mcp("drawBoundingBox")
            .returnsVoid()
            .argument(double.class)
            .argument(double.class)
            .argument(double.class)
            .argument(double.class)
            .argument(double.class)
            .argument(double.class)
            .argument(float.class)
            .argument(float.class)
            .argument(float.class)
            .argument(float.class)
            .build();

    // does not exist anymore
    @Deprecated
    ASMMethod BufferBuilder_putColorMultiplier =
        Classes.BufferBuilder.newChildMethod()
            .mcp("putColorMultiplier")
            .returnsVoid()
            .argument(float.class)
            .argument(float.class)
            .argument(float.class)
            .argument(int.class)
            .build();


    ASMMethod VisGraph_setOpaqueCube =
        Classes.VisGraph.newChildMethod()
            .mcp("setOpaqueCube")
            .srg("func_178606_a")
            .returnsVoid()
            .argument(Classes.BlockPos)
            .build();

    ASMMethod VisGraph_computeVisibility =
        Classes.VisGraph.newChildMethod()
            .mcp("computeVisibility")
            .srg("func_178607_a")
            .returns(Classes.SetVisibility)
            .build();

    // does not exist anymore or is renamed
    @Deprecated
    ASMMethod World_handleMaterialAcceleration =
        Classes.World.newChildMethod()
            .mcp("handleMaterialAcceleration")
            .srg("troll")
            .returns(boolean.class)
            .argument(Classes.AxisAlignedBB)
            .argument(Classes.Material)
            .argument(Classes.Entity)
            .build();

    // appears to have moved to WorldLightManager::checkBlock
    @Deprecated
    ASMMethod World_checkLightFor =
        Classes.World.newChildMethod()
            .mcp("checkLightFor")
            .returns(boolean.class)
            .argument(Classes.EnumSkyBlock)
            .argument(Classes.BlockPos)
            .build();

    ASMMethod BoatEntity_updateMotion =
        Classes.BoatEntity.newChildMethod()
            .mcp("updateMotion")
            .srg("func_184450_w")
            .returnsVoid()
            .noArguments()
            .build();

    ASMMethod BoatEntity_controlBoat =
        Classes.BoatEntity.newChildMethod()
            .mcp("controlBoat")
            .srg("func_184443_x")
            .returnsVoid()
            .noArguments()
            .build();

    ASMMethod BoatEntity_applyYawToEntity =
        Classes.BoatEntity.newChildMethod()
            .mcp("applyYawToEntity")
            .srg("func_184454_a")
            .returnsVoid()
            .argument(Classes.Entity)
            .build();

    ASMMethod BoatRenderer_render =
        Classes.BoatRenderer.newChildMethod()
            // mcp 1.12 -> doRender
            .mcp("render")
            .srg("func_225623_a_")
            .returnsVoid()
            .argument(Classes.BoatEntity)
            .argument(float.class)
            .argument(float.class)
            .argument(Classes.MatrixStack)
            .argument(Classes.IRenderTypeBuffer)
            .argument(int.class)
            .build();

    ASMMethod PlayerTabOverlayGui_renderPlayerList =
        Classes.PlayerTabOverlayGui.newChildMethod()
            // mcp 1.12 -> renderPlayerlist
            .mcp("render")
            .srg("func_175249_a")
            .returnsVoid()
            .argument(int.class)
            .argument(Classes.Scoreboard)
            .argument(Classes.ScoreObjective)
            .build();

    ASMMethod KeyBinding_isKeyDown =
        Classes.KeyBinding.newChildMethod()
            .mcp("isKeyDown")
            .srg("func_151470_d")
            .returns(boolean.class)
            .noArguments()
            .build();

    ASMMethod PlayerController_syncCurrentPlayItem =
        Classes.PlayerController.newChildMethod()
            .name("syncCurrentPlayItem")
            .srg("func_78750_j")
            .returnsVoid()
            .noArguments()
            .build();

    ASMMethod PlayerController_attackEntity =
        Classes.PlayerController.newChildMethod()
            .name("attackEntity")
            .srg("func_78764_a")
            .returnsVoid()
            .argument(Classes.PlayerEntity)
            .argument(Classes.Entity)
            .build();

    ASMMethod PlayerController_onPlayerDamageBlock =
        Classes.PlayerController.newChildMethod()
            .name("onPlayerDamageBlock")
            .srg("func_180512_c")
            .returns(boolean.class)
            .argument(Classes.BlockPos)
            .argument(Classes.Direction)
            .build();

    ASMMethod PlayerController_onStoppedUsingItem =
        Classes.PlayerController.newChildMethod()
            .name("onStoppedUsingItem")
            .srg("func_78766_c")
            .returnsVoid()
            .argument(Classes.PlayerEntity)
            .build();

    ASMMethod GameRenderer_renderWorld =
        Classes.GameRenderer.newChildMethod()
            .name("renderWorld")
            .srg("func_228378_a_")
            .returnsVoid()
            .argument(float.class)
            .argument(long.class)
            .argument(Classes.MatrixStack)
            .build();

    ASMMethod GameRenderer_getProjectionMatrix =
        Classes.GameRenderer.newChildMethod()
            .name("getProjectionMatrix")
            .srg("func_228382_a_")
            .returns(Classes.Matrix4f)
            .argument(Classes.ActiveRenderInfo)
            .argument(float.class)
            .argument(boolean.class)
            .build();
  }
}
