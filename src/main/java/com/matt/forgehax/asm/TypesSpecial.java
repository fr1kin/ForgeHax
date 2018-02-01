package com.matt.forgehax.asm;

import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMBuilders;

/**
 * Created on 5/29/2017 by fr1kin
 */
public interface TypesSpecial {
    interface Classes {
        ASMClass SchematicPrinter = ASMBuilders.newClassBuilder()
                .setClassName("com/github/lunatrius/schematica/client/printer/SchematicPrinter")
                .build();

        ASMClass WorldClient = ASMBuilders.newClassBuilder()
                .setClassName("net/minecraft/client/multiplayer/WorldClient")
                .build();
        ASMClass EntityPlayerSP = ASMBuilders.newClassBuilder()
                .setClassName("net/minecraft/client/entity/EntityPlayerSP")
                .build();
        ASMClass ItemStack = ASMBuilders.newClassBuilder()
                .setClassName("net/minecraft/item/ItemStack")
                .build();
        ASMClass EnumFacing = ASMBuilders.newClassBuilder()
                .setClassName("net/minecraft/util/EnumFacing")
                .build();
        ASMClass BlockPos = ASMBuilders.newClassBuilder()
                .setClassName("net/minecraft/util/math/BlockPos")
                .build();
        ASMClass Vec3d = ASMBuilders.newClassBuilder()
                .setClassName("net/minecraft/util/math/Vec3d")
                .build();
        ASMClass EnumHand = ASMBuilders.newClassBuilder()
                .setClassName("net/minecraft/util/EnumHand")
                .build();
    }

    interface Fields {

    }

    interface Methods {
        ASMMethod SchematicPrinter_placeBlock = Classes.SchematicPrinter.childMethod()
                .setName("placeBlock")
                .setReturnType(boolean.class)
                .beginParameters()
                /*.add(TypesMc.Classes.WorldClient)
                .add(TypesMc.Classes.EntityPlayerSP)
                .add(TypesMc.Classes.ItemStack)
                .add(TypesMc.Classes.BlockPos)
                .add(TypesMc.Classes.EnumFacing)
                .add(TypesMc.Classes.Vec3d)
                .add(TypesMc.Classes.EnumHand)*/
                .add(Classes.WorldClient)
                .add(Classes.EntityPlayerSP)
                .add(Classes.ItemStack)
                .add(Classes.BlockPos)
                .add(Classes.EnumFacing)
                .add(Classes.Vec3d)
                .add(Classes.EnumHand)
                .finish()
                .build();
    }
}
