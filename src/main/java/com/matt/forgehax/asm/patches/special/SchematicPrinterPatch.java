package com.matt.forgehax.asm.patches.special;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.TypesSpecial;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.environment.State;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created on 9/20/2017 by Babbaj TODO: Fix obfuscation problem so this can work
 */
public class SchematicPrinterPatch extends ClassTransformer {
  
  public SchematicPrinterPatch() {
    super(TypesSpecial.Classes.SchematicPrinter);
  }
  
  @Override
  public State getClassObfuscationState() {
    return State.NORMAL;
  }
  
  @RegisterMethodTransformer
  private class PlaceBlock extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesSpecial.Methods.SchematicPrinter_placeBlock;
    }
    
    @Inject(description = "Add hook for schematica block placing event")
    public void inject(MethodNode main) {
      AbstractInsnNode start = main.instructions.getFirst();
      
      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 3)); // load ItemStack
      insnList.add(new VarInsnNode(ALOAD, 4)); // load BlockPos
      insnList.add(new VarInsnNode(ALOAD, 6)); // load Vec
      insnList.add(new VarInsnNode(ALOAD, 5)); // load EnumFacing
      insnList.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSchematicaPlaceBlock));
      
      main.instructions.insertBefore(start, insnList);
    }
  }
}
