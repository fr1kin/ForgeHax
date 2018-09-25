package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.ReplacementHooks.WorldEvent;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Transformer(WorldClient.class)
public class WorldClientPatch {

    @Inject(name = "<init>", args = {NetHandlerPlayClient.class, WorldSettings.class, int.class, EnumDifficulty.class, Profiler.class},
    description = "Add hook for WorldEvent.Load")
    public void constructorHook(AsmMethod method) {
        method.stream()
                .filter(insn -> insn.getOpcode() == RETURN)
                .forEach(node -> {
                    method.setCursor(node);
                    method.visitInsn(new VarInsnNode(ALOAD, 0)); // push this
                    method.<WorldClient>consume(world -> ForgeHax.EVENT_BUS.post(new WorldEvent.Load(world)));
                });

    }
}
