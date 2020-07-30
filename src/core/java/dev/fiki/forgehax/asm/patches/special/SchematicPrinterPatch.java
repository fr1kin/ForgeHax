package dev.fiki.forgehax.asm.patches.special;

/**
 * Created on 9/20/2017 by Babbaj
 */
public class SchematicPrinterPatch {

//  @RegisterTransformer("ForgeHaxHooks::onSchematicaPlaceBlock")
//  public static class PlaceBlock extends MethodTransformer {
//
//    @Override
//    public ASMMethod getMethod() {
//      return TypesSpecial.Methods.SchematicPrinter_placeBlock;
//    }
//
//    @Override
//    public void transform(MethodNode main) {
//      AbstractInsnNode start = main.instructions.getFirst();
//
//      InsnList insnList = new InsnList();
//      insnList.add(new VarInsnNode(ALOAD, 3)); // load ItemStack
//      insnList.add(new VarInsnNode(ALOAD, 4)); // load BlockPos
//      insnList.add(new VarInsnNode(ALOAD, 6)); // load Vec
//      insnList.add(new VarInsnNode(ALOAD, 5)); // load EnumFacing
//      insnList.add(
//          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSchematicaPlaceBlock));
//
//      main.instructions.insertBefore(start, insnList);
//    }
//  }
}
