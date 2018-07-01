package com.matt.forgehax.asm.asmlib.patches.special;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

@Transformer(target = "com.github.lunatrius.schematica.client.printer.SchematicPrinter")
public class SchematicPrinterPatch {

    @Inject(name = "placeBlock",
            args = {WorldClient.class, EntityPlayerSP.class, ItemStack.class, BlockPos.class, EnumFacing.class, Vec3d.class, EnumHand.class},
            ret = boolean.class,
    description = "Add hook for schematica block placing event"
    )
    public void placeBlock(MethodNode main) {
        AbstractInsnNode start = main.instructions.getFirst();

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 3)); // load ItemStack
        insnList.add(new VarInsnNode(ALOAD, 4)); // load BlockPos
        insnList.add(new VarInsnNode(ALOAD, 6)); // load Vec
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSchematicaPlaceBlock));

        main.instructions.insertBefore(start, insnList);
    }
}
