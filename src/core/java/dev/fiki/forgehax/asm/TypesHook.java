package dev.fiki.forgehax.asm;

import dev.fiki.forgehax.common.asmtype.ASMClass;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

/**
 * Created on 5/27/2017 by fr1kin
 */
public interface TypesHook {

  interface Classes {
    ASMClass ForgeHaxHooks =
        ASMClass.builder()
            .className("dev/fiki/forgehax/common/ForgeHaxHooks")
            .build();
  }

  interface Fields {
  }

  interface Methods {
    ASMMethod ForgeHaxHooks_shouldStopHurtcamEffect =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldStopHurtcamEffect")
            .returns(boolean.class)
            .noArguments()
            .build();

    ASMMethod ForgeHaxHooks_onPacketOutbound =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPacketOutbound")
            .returns(boolean.class)
            .argument(TypesMc.Classes.NetworkManager)
            .argument(TypesMc.Classes.Packet)
            .build();

    ASMMethod ForgeHaxHooks_onPacketInbound =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("onPacketInbound")
            .returns(boolean.class)
            .argument(TypesMc.Classes.NetworkManager)
            .argument(TypesMc.Classes.Packet)
            .build();

    ASMMethod ForgeHaxHooks_shouldBePushedByLiquid =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldBePushedByLiquid")
            .returns(boolean.class)
            .argument(TypesMc.Classes.PlayerEntity)
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

    ASMMethod ForgeHaxHooks_shouldApplyBlockEntityCollisions =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldApplyBlockEntityCollisions")
            .returns(boolean.class)
            .argument(TypesMc.Classes.Entity)
            .argument(TypesMc.Classes.BlockState)
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
            .argument(TypesMc.Classes.Vector3d)
            .argument(TypesMc.Classes.Direction)
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

    ASMMethod ForgeHaxHooks_shouldClipBlockEdge =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldClipBlockEdge")
            .returns(boolean.class)
            .argument(TypesMc.Classes.PlayerEntity)
            .build();

    ASMMethod ForgeHaxHooks_shouldApplyElytraMovement =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldApplyElytraMovement")
            .returns(boolean.class)
            .argument(boolean.class)
            .argument(TypesMc.Classes.LivingEntity)
            .build();

    ASMMethod ForgeHaxHooks_shouldClampMotion =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldClampMotion")
            .returns(boolean.class)
            .argument(TypesMc.Classes.LivingEntity)
            .build();

    ASMMethod ForgeHaxHooks_shouldSlowdownPlayer =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldSlowdownPlayer")
            .returns(boolean.class)
            .argument(TypesMc.Classes.ClientPlayerEntity)
            .build();

    ASMMethod ForgeHaxHooks_shouldClampBoat =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldClampBoat")
            .returns(boolean.class)
            .argument(TypesMc.Classes.BoatEntity)
            .build();

    ASMMethod ForgeHaxHooks_shouldNotRowBoat =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldNotRowBoat")
            .returns(boolean.class)
            .argument(TypesMc.Classes.ClientPlayerEntity)
            .build();

    ASMMethod ForgeHaxHooks_shouldIncreaseTabListSize =
        Classes.ForgeHaxHooks.newChildMethod()
            .name("shouldIncreaseTabListSize")
            .returns(boolean.class)
            .noArguments()
            .build();
  }
}
