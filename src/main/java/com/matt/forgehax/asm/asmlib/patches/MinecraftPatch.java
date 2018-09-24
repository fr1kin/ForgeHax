package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.util.mod.BaseMod;
import net.futureclient.asm.transformer.ASMUtils;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Objects;
import java.util.Optional;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getModManager;
import static org.objectweb.asm.Opcodes.*;

@Transformer(Minecraft.class)
public class MinecraftPatch {

    @Inject(name = "init")
    public void init(AsmMethod method) {
        final AbstractInsnNode setEffectRenderer = ASMUtils.streamMethod(method.method.instructions)
                .filter(insn -> insn.getOpcode() == PUTFIELD)
                .filter(insn -> ((FieldInsnNode)insn).name.matches("j|effectRenderer"))// TODO: get runtime name from mcp name from asmlib
                .findFirst()
                .orElseGet(() -> {
                    System.err.println("Failed to find proper injection point for forgehax initialization, falling back to RETURN");
                    return getReturnNode(method.method.instructions);
                });
        Objects.requireNonNull(setEffectRenderer, "Failed to find injection point for initialization");

        method.setCursor(setEffectRenderer.getNext());
        method.run(ForgeHax::init);
    }

    private AbstractInsnNode getReturnNode(InsnList insns) {
        return ASMUtils.streamMethod(insns)
                .filter(insn -> insn.getOpcode() == RETURN)
                .findFirst()
                .orElse(null);
    }
}
