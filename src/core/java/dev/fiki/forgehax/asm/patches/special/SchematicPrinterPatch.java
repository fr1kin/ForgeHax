package dev.fiki.forgehax.asm.patches.special;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesSpecial;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created on 9/20/2017 by Babbaj TODO: Fix obfuscation problem so this can work
 */
public class SchematicPrinterPatch {
  

  @RegisterTransformer
  public static class PlaceBlock extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesSpecial.Methods.SchematicPrinter_placeBlock;
    }

    @Override
    public void transform(MethodNode main) {
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
