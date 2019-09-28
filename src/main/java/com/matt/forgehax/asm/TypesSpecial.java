package com.matt.forgehax.asm;

import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMBuilders;

/**
 * Created on 5/29/2017 by fr1kin
 */
public interface TypesSpecial {
  
  interface Classes {
    
    ASMClass SchematicPrinter =
      ASMBuilders.newClassBuilder()
        .setClassName("com/github/lunatrius/schematica/client/printer/SchematicPrinter")
        .build();
  }
  
  interface Fields {
  
  }
  
  interface Methods {
    
    ASMMethod SchematicPrinter_placeBlock =
      Classes.SchematicPrinter.childMethod()
        .setName("placeBlock")
        .setReturnType(boolean.class)
        .beginParameters()
        .unobfuscated()
        .add(TypesMc.Classes.WorldClient)
        .add(TypesMc.Classes.EntityPlayerSP)
        .add(TypesMc.Classes.ItemStack)
        .add(TypesMc.Classes.BlockPos)
        .add(TypesMc.Classes.EnumFacing)
        .add(TypesMc.Classes.Vec3d)
        .add(TypesMc.Classes.EnumHand)
        .finish()
        .build();
  }
}
