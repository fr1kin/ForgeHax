package dev.fiki.forgehax.asm;

import dev.fiki.forgehax.common.asmtype.ASMClass;
import dev.fiki.forgehax.common.asmtype.ASMField;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.List;

/**
 * Created on 5/27/2017 by fr1kin
 */
public interface TypesHook {

  interface Classes {

    ASMClass ForgeHaxHooks =
        ASMClass.builder()
            .className("dev/fiki/forgehax/common/ForgeHaxHooks")
            .build();

    ASMClass GetCollisionShapeEvent =
        ASMClass.builder()
            .className("dev/fiki/forgehax/common/events/movement/GetCollisionShapeEvent")
            .build();
  }

  interface Fields {

    ASMField ForgeHaxHooks_isSafeWalkActivated =
        Classes.ForgeHaxHooks.newChildField()
            .name("isSafeWalkActivated")
            .type(boolean.class)
            .build();

    ASMField ForgeHaxHooks_isNoSlowDownActivated =
        Classes.ForgeHaxHooks.newChildField()
            .name("isNoSlowDownActivated")
            .type(boolean.class)
            .build();

    ASMField ForgeHaxHooks_isNoBoatGravityActivated =
        Classes.ForgeHaxHooks.newChildField()
            .name("isNoBoatGravityActivated")
            .type(boolean.class)
            .build();

    ASMField ForgeHaxHooks_isNoClampingActivated =
        Classes.ForgeHaxHooks.newChildField()
            .name("isNoClampingActivated")
            .type(boolean.class)
            .build();

    ASMField ForgeHaxHooks_isBoatSetYawActivated =
        Classes.ForgeHaxHooks.newChildField()
            .name("isBoatSetYawActivated")
            .type(boolean.class)
            .build();

    ASMField ForgeHaxHooks_isNotRowingBoatActivated =
        Classes.ForgeHaxHooks.newChildField()
            .name("isNotRowingBoatActivated")
            .type(boolean.class)
            .build();

    ASMField ForgeHaxHooks_doIncreaseTabListSize =
        Classes.ForgeHaxHooks.newChildField()
            .name("doIncreaseTabListSize")
            .type(boolean.class)
            .build();
  }

  interface Methods {

    ASMMethod ForgeHaxHooks_onHurtcamEffect =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onHurtcamEffect")
            .returns(boolean.class)
            .argument(float.class)
            .build();

    ASMMethod ForgeHaxHooks_onSendingPacket =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onSendingPacket")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Packet)
            .build();

    ASMMethod ForgeHaxHooks_onSentPacket =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onSentPacket")
            .returns(void.class)
            .argument(TypesMc.Classes.Packet)
            .build();

    ASMMethod ForgeHaxHooks_onPreReceived =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPreReceived")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Packet)
            .build();

    ASMMethod ForgeHaxHooks_onPostReceived =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPostReceived")
            .returns(void.class)
            .argument(TypesMc.Classes.Packet)
            .build();

    ASMMethod ForgeHaxHooks_onWaterMovement =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onWaterMovement")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Entity)
            .argument(TypesMc.Classes.Vec3d)
            .build();

    ASMMethod ForgeHaxHooks_onApplyCollisionMotion =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onApplyCollisionMotion")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Entity)
            .argument(TypesMc.Classes.Entity)
            .argument(double.class)
            .argument(double.class)
            .build();

    ASMMethod ForgeHaxHooks_onPutColorMultiplier =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPutColorMultiplier")
            .returns(int.class)
            .argument(float.class)
            .argument(float.class)
            .argument(float.class)
            .argument(int.class)
            .argument(boolean[].class)
            .build();

//    ASMMethod ForgeHaxHooks_onPreRenderBlockLayer =
//        Classes.ForgeHaxHooks.newChildMethod()
//            .name("onPreRenderBlockLayer")
//            .returns(boolean.class)
//            .argument(TypesMc.Classes.BlockRenderLayer)
//            .argument(double.class)
//            .build();
//
//    ASMMethod ForgeHaxHooks_onPostRenderBlockLayer =
//        Classes.ForgeHaxHooks.newChildMethod()
//            .name("onPostRenderBlockLayer")
//            .returns(void.class)
//            .argument(TypesMc.Classes.BlockRenderLayer)
//            .argument(double.class)
//            .build();
//
//    ASMMethod ForgeHaxHooks_onRenderBlockInLayer =
//        Classes.ForgeHaxHooks.newChildMethod()
//            .name("onRenderBlockInLayer")
//            .returns(TypesMc.Classes.BlockRenderLayer)
//            .argument(TypesMc.Classes.Block)
//            .argument(TypesMc.Classes.BlockState)
//            .argument(TypesMc.Classes.BlockRenderLayer)
//            .argument(TypesMc.Classes.BlockRenderLayer)
//            .build();

    ASMMethod ForgeHaxHooks_onSetupTerrain =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onSetupTerrain")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Entity)
            .argument(boolean.class)
            .build();

    ASMMethod ForgeHaxHooks_isBlockFiltered =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("isBlockFiltered")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Entity)
            .argument(TypesMc.Classes.BlockState)
            .build();

    ASMMethod ForgeHaxHooks_onAddCollisionBoxToList =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onAddCollisionBoxToList")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Block)
            .argument(TypesMc.Classes.BlockState)
            .argument(TypesMc.Classes.World)
            .argument(TypesMc.Classes.BlockPos)
            .argument(TypesMc.Classes.AxisAlignedBB)
            .argument(List.class)
            .argument(TypesMc.Classes.Entity)
            .argument(boolean.class)
            .build();

    ASMMethod ForgeHaxHooks_onBlockRenderInLoop =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onBlockRenderInLoop")
            .returns(void.class)
            .argument(TypesMc.Classes.RenderChunk)
            .argument(TypesMc.Classes.Block)
            .argument(TypesMc.Classes.BlockState)
            .argument(TypesMc.Classes.BlockPos)
            .build();

    ASMMethod ForgeHaxHooks_onPreBuildChunk =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPreBuildChunk")
            .returns(void.class)
            .argument(TypesMc.Classes.RenderChunk)
            .build();

    ASMMethod ForgeHaxHooks_onPostBuildChunk =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPostBuildChunk")
            .returns(void.class)
            .argument(TypesMc.Classes.RenderChunk)
            .build();

    ASMMethod ForgeHaxHooks_onDeleteGlResources =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onDeleteGlResources")
            .returns(void.class)
            .argument(TypesMc.Classes.RenderChunk)
            .build();

    ASMMethod ForgeHaxHooks_onChunkUploaded =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onChunkUploaded")
            .returns(void.class)
            .argument(TypesMc.Classes.RenderChunk)
            .argument(TypesMc.Classes.BufferBuilder)
            .build();

//    ASMMethod ForgeHaxHooks_onAddRenderChunk =
//        Classes.ForgeHaxHooks.newChildMethod()
//            .name("onAddRenderChunk")
//            .returns(void.class)
//            .argument(TypesMc.Classes.RenderChunk)
//            .argument(TypesMc.Classes.BlockRenderLayer)
//            .build();

    ASMMethod ForgeHaxHooks_onLoadRenderers =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onLoadRenderers")
            .returns(void.class)
            .argument(TypesMc.Classes.ViewFrustum)
            .argument(TypesMc.Classes.ChunkRenderDispatcher)
            .build();

    ASMMethod ForgeHaxHooks_onWorldRendererDeallocated =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onWorldRendererDeallocated")
            .returns(void.class)
            .argument(TypesMc.Classes.ChunkCompileTaskGenerator)
            .build();

    ASMMethod ForgeHaxHooks_shouldDisableCaveCulling =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldDisableCaveCulling")
            .returns(boolean.class)
            .noArguments()
            .build();

    ASMMethod ForgeHaxHooks_onUpdateWalkingPlayerPre =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onUpdateWalkingPlayerPre")
            .returns(boolean.class)
            .argument(TypesMc.Classes.ClientPlayerEntity)
            .build();

    ASMMethod ForgeHaxHooks_onUpdateWalkingPlayerPost =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onUpdateWalkingPlayerPost")
            .returns(void.class)
            .argument(TypesMc.Classes.ClientPlayerEntity)
            .build();

    ASMMethod ForgeHaxHooks_onPushOutOfBlocks =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPushOutOfBlocks")
            .returns(boolean.class)
            .noArguments()
            .build();

    ASMMethod ForgeHaxHooks_onRenderBoat =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onRenderBoat")
            .returns(float.class)
            .argument(TypesMc.Classes.BoatEntity)
            .argument(float.class)
            .build();

    ASMMethod ForgeHaxHooks_onSchematicaPlaceBlock =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onSchematicaPlaceBlock")
            .returns(void.class)
            .argument(TypesMc.Classes.ItemStack)
            .argument(TypesMc.Classes.BlockPos)
            .argument(TypesMc.Classes.Vec3d)
            .argument(TypesMc.Classes.Direction)
            .build();

    ASMMethod ForgeHaxHooks_onWorldCheckLightFor =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onWorldCheckLightFor")
            .returns(boolean.class)
            .argument(TypesMc.Classes.EnumSkyBlock)
            .argument(TypesMc.Classes.BlockPos)
            .build();

    ASMMethod ForgeHaxHooks_onLeftClickCounterSet =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onLeftClickCounterSet")
            .returns(int.class)
            .argument(int.class)
            .argument(TypesMc.Classes.Minecraft)
            .build();

    ASMMethod ForgeHaxHooks_onSendClickBlockToController =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onSendClickBlockToController")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Minecraft)
            .argument(boolean.class)
            .build();

    ASMMethod ForgeHaxHooks_onPlayerItemSync =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPlayerItemSync")
            .returns(void.class)
            .argument(TypesMc.Classes.PlayerController)
            .build();

    ASMMethod ForgeHaxHooks_onPlayerBreakingBlock =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPlayerBreakingBlock")
            .returns(void.class)
            .argument(TypesMc.Classes.PlayerController)
            .argument(TypesMc.Classes.BlockPos)
            .argument(TypesMc.Classes.Direction)
            .build();

    ASMMethod ForgeHaxHooks_onPlayerAttackEntity =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPlayerAttackEntity")
            .returns(void.class)
            .argument(TypesMc.Classes.PlayerController)
            .argument(TypesMc.Classes.PlayerEntity)
            .argument(TypesMc.Classes.Entity)
            .build();

    ASMMethod ForgeHaxHooks_onPlayerStopUse =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPlayerStopUse")
            .returns(boolean.class)
            .argument(TypesMc.Classes.PlayerController)
            .argument(TypesMc.Classes.PlayerEntity)
            .build();

    ASMMethod ForgeHaxHooks_onEntityBlockSlipApply =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onEntityBlockSlipApply")
            .returns(float.class)
            .argument(float.class)
            .argument(TypesMc.Classes.LivingEntity)
            .argument(TypesMc.Classes.BlockPos)
            .build();

    ASMMethod ForgeHaxHooks_fireEvent_v =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("fireEvent_v")
            .returns(void.class) // return nothing
            .argument("net/minecraftforge/eventbus/api/Event")
            .build();

    ASMMethod ForgeHaxHooks_fireEvent_b =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("fireEvent_b")
            .returns(boolean.class) // return nothing
            .argument("net/minecraftforge/eventbus/api/Event")
            .build();

    ASMMethod ForgeHaxHooks_onDrawBoundingBox_Post =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onDrawBoundingBoxPost")
            .returns(void.class)
            .noArguments()
            .build();
  }
}
