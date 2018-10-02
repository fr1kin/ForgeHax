package com.matt.forgehax.asm;

import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.asmtype.ASMField;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMBuilders;

import java.util.List;

/**
 * Created on 5/27/2017 by fr1kin
 */
public interface TypesHook {
    interface Classes {
        ASMClass ForgeHaxHooks = ASMBuilders.newClassBuilder()
                .setClassName("com/matt/forgehax/asm/ForgeHaxHooks")
                .build();
    }

    interface Fields {
        ASMField ForgeHaxHooks_isSafeWalkActivated = Classes.ForgeHaxHooks.childField()
                .setName("isSafeWalkActivated")
                .setType(boolean.class)
                .build();

        ASMField ForgeHaxHooks_isNoSlowDownActivated = Classes.ForgeHaxHooks.childField()
                .setName("isNoSlowDownActivated")
                .setType(boolean.class)
                .build();

        ASMField ForgeHaxHooks_isNoBoatGravityActivated = Classes.ForgeHaxHooks.childField()
                .setName("isNoBoatGravityActivated")
                .setType(boolean.class)
                .build();

        ASMField ForgeHaxHooks_isNoClampingActivated = Classes.ForgeHaxHooks.childField()
                .setName("isNoClampingActivated")
                .setType(boolean.class)
                .build();

        ASMField ForgeHaxHooks_isBoatSetYawActivated = Classes.ForgeHaxHooks.childField()
                .setName("isBoatSetYawActivated")
                .setType(boolean.class)
                .build();

        ASMField ForgeHaxHooks_isNotRowingBoatActivated = Classes.ForgeHaxHooks.childField()
                .setName("isNotRowingBoatActivated")
                .setType(boolean.class)
                .build();

        ASMField ForgeHaxHooks_doIncreaseTabListSize = Classes.ForgeHaxHooks.childField()
                .setName("doIncreaseTabListSize")
                .setType(boolean.class)
                .build();

    }

    interface Methods {
        ASMMethod ForgeHaxHooks_onHurtcamEffect = Classes.ForgeHaxHooks.childMethod()
                .setName("onHurtcamEffect")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(float.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onSendingPacket = Classes.ForgeHaxHooks.childMethod()
                .setName("onSendingPacket")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.Packet)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onSentPacket = Classes.ForgeHaxHooks.childMethod()
                .setName("onSentPacket")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.Packet)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onPreReceived = Classes.ForgeHaxHooks.childMethod()
                .setName("onPreReceived")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.Packet)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onPostReceived = Classes.ForgeHaxHooks.childMethod()
                .setName("onPostReceived")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.Packet)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onWaterMovement = Classes.ForgeHaxHooks.childMethod()
                .setName("onWaterMovement")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.Entity)
                .add(TypesMc.Classes.Vec3d)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onApplyCollisionMotion = Classes.ForgeHaxHooks.childMethod()
                .setName("onApplyCollisionMotion")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.Entity)
                .add(TypesMc.Classes.Entity)
                .add(double.class)
                .add(double.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onPutColorMultiplier = Classes.ForgeHaxHooks.childMethod()
                .setName("onPutColorMultiplier")
                .setReturnType(int.class)
                .beginParameters()
                .add(float.class)
                .add(float.class)
                .add(float.class)
                .add(int.class)
                .add(boolean[].class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onPreRenderBlockLayer = Classes.ForgeHaxHooks.childMethod()
                .setName("onPreRenderBlockLayer")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.BlockRenderLayer)
                .add(double.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onPostRenderBlockLayer = Classes.ForgeHaxHooks.childMethod()
                .setName("onPostRenderBlockLayer")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.BlockRenderLayer)
                .add(double.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onRenderBlockInLayer = Classes.ForgeHaxHooks.childMethod()
                .setName("onRenderBlockInLayer")
                .setReturnType(TypesMc.Classes.BlockRenderLayer)
                .beginParameters()
                .add(TypesMc.Classes.Block)
                .add(TypesMc.Classes.IBlockState)
                .add(TypesMc.Classes.BlockRenderLayer)
                .add(TypesMc.Classes.BlockRenderLayer)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onSetupTerrain = Classes.ForgeHaxHooks.childMethod()
                .setName("onSetupTerrain")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.Entity)
                .add(boolean.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_isBlockFiltered = Classes.ForgeHaxHooks.childMethod()
                .setName("isBlockFiltered")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.Entity)
                .add(TypesMc.Classes.IBlockState)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onAddCollisionBoxToList = Classes.ForgeHaxHooks.childMethod()
                .setName("onAddCollisionBoxToList")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.Block)
                .add(TypesMc.Classes.IBlockState)
                .add(TypesMc.Classes.World)
                .add(TypesMc.Classes.BlockPos)
                .add(TypesMc.Classes.AxisAlignedBB)
                .add(List.class)
                .add(TypesMc.Classes.Entity)
                .add(boolean.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onBlockRenderInLoop = Classes.ForgeHaxHooks.childMethod()
                .setName("onBlockRenderInLoop")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.RenderChunk)
                .add(TypesMc.Classes.Block)
                .add(TypesMc.Classes.IBlockState)
                .add(TypesMc.Classes.BlockPos)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onPreBuildChunk = Classes.ForgeHaxHooks.childMethod()
                .setName("onPreBuildChunk")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.RenderChunk)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onPostBuildChunk = Classes.ForgeHaxHooks.childMethod()
                .setName("onPostBuildChunk")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.RenderChunk)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onDeleteGlResources = Classes.ForgeHaxHooks.childMethod()
                .setName("onDeleteGlResources")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.RenderChunk)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onChunkUploaded = Classes.ForgeHaxHooks.childMethod()
                .setName("onChunkUploaded")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.RenderChunk)
                .add(TypesMc.Classes.BufferBuilder)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onAddRenderChunk = Classes.ForgeHaxHooks.childMethod()
                .setName("onAddRenderChunk")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.RenderChunk)
                .add(TypesMc.Classes.BlockRenderLayer)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onLoadRenderers = Classes.ForgeHaxHooks.childMethod()
                .setName("onLoadRenderers")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.ViewFrustum)
                .add(TypesMc.Classes.ChunkRenderDispatcher)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onWorldRendererDeallocated = Classes.ForgeHaxHooks.childMethod()
                .setName("onWorldRendererDeallocated")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.ChunkCompileTaskGenerator)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_shouldDisableCaveCulling = Classes.ForgeHaxHooks.childMethod()
                .setName("shouldDisableCaveCulling")
                .setReturnType(boolean.class)
                .emptyParameters()
                .build();

        ASMMethod ForgeHaxHooks_onJournyMapSetStratumColor = Classes.ForgeHaxHooks.childMethod()
                .setName("onJournyMapSetStratumColor")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(Object.class)
                .add(Object.class)
                .add(int.class)
                .add(Integer.class)
                .add(boolean.class)
                .add(boolean.class)
                .add(boolean.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onUpdateWalkingPlayerPre = Classes.ForgeHaxHooks.childMethod()
                .setName("onUpdateWalkingPlayerPre")
                .setReturnType(void.class)
                .emptyParameters()
                .build();

        ASMMethod ForgeHaxHooks_onUpdateWalkingPlayerPost = Classes.ForgeHaxHooks.childMethod()
                .setName("onUpdateWalkingPlayerPost")
                .setReturnType(void.class)
                .emptyParameters()
                .build();

        ASMMethod ForgeHaxHooks_onPushOutOfBlocks = Classes.ForgeHaxHooks.childMethod()
                .setName("onPushOutOfBlocks")
                .setReturnType(boolean.class)
                .emptyParameters()
                .build();

        ASMMethod ForgeHaxHooks_onRenderBoat = Classes.ForgeHaxHooks.childMethod()
                .setName("onRenderBoat")
                .setReturnType(float.class)
                .beginParameters()
                .add(TypesMc.Classes.EntityBoat)
                .add(float.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onSchematicaPlaceBlock = Classes.ForgeHaxHooks.childMethod()
                .setName("onSchematicaPlaceBlock")
                .setReturnType(void.class)
                .beginParameters()
                .add(TypesMc.Classes.ItemStack)
                .add(TypesMc.Classes.BlockPos)
                .add(TypesMc.Classes.Vec3d)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onWorldCheckLightFor = Classes.ForgeHaxHooks.childMethod()
                .setName("onWorldCheckLightFor")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(TypesMc.Classes.EnumSkyBlock)
                .add(TypesMc.Classes.BlockPos)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onLeftClickCounterSet = Classes.ForgeHaxHooks.childMethod()
                .setName("onLeftClickCounterSet")
                .setReturnType(int.class)
                .beginParameters()
                .add(int.class)
                .finish()
                .build();

        ASMMethod ForgeHaxHooks_onSendClickBlockToController = Classes.ForgeHaxHooks.childMethod()
                .setName("onSendClickBlockToController")
                .setReturnType(boolean.class)
                .beginParameters()
                .add(boolean.class)
                .finish()
                .build();
    }
}
