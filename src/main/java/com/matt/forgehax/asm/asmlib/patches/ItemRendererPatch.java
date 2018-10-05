package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.RenderBlockOverlayEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.InsnPattern;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Objects;

import static com.matt.forgehax.Globals.MC;
import static org.objectweb.asm.Opcodes.*;

@Transformer(ItemRenderer.class)
public class ItemRendererPatch {

    @Inject(name = "renderOverlays", args = {float.class})
    public void renderOverlayHook(AsmMethod method) {
        InsnPattern renderBlockInHandNode = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
                ALOAD, ALOAD, GETFIELD, INVOKEVIRTUAL, INVOKEVIRTUAL, ALOAD, INVOKEVIRTUAL, INVOKESPECIAL
        }, "xxxxxxxx");
        Objects.requireNonNull(renderBlockInHandNode);

        InsnPattern renderWaterOverlayTextureNode = ASMHelper._findPattern(renderBlockInHandNode.getLast(), new int[] {
                ALOAD, FLOAD, INVOKESPECIAL
        }, "xxx");
        Objects.requireNonNull(renderWaterOverlayTextureNode);

        InsnPattern renderFireInFirstPersonNode = ASMHelper._findPattern(renderWaterOverlayTextureNode.getLast(), new int[] {
                ALOAD, INVOKESPECIAL
        }, "xx");
        Objects.requireNonNull(renderFireInFirstPersonNode);

        renderBlockInHand(method, renderBlockInHandNode);
        renderWaterOverlayTexture(method, renderWaterOverlayTextureNode);
        renderFireInFirstPerson(method, renderFireInFirstPersonNode);
    }

    private void renderBlockInHand(AsmMethod method, InsnPattern pattern) {
        method.setCursor(pattern.getFirst());
        method.visitInsn(new VarInsnNode(FLOAD, 1)); // partialTicks
        method.visitInsn(new VarInsnNode(ALOAD, 2)); // iblockstate
        method.visitInsn(new VarInsnNode(ALOAD, 3)); // overlayPos
        method.<BlockOverlayPredicate>invoke((partialTicks, iblockstate, overlayPos) ->
                ForgeHax.EVENT_BUS.post(new RenderBlockOverlayEvent(RenderBlockOverlayEvent.OverlayType.BLOCK, partialTicks, iblockstate, overlayPos))
        );
        method.jumpIf(IFNE, pattern.getLast().getNext());
    }


    private void renderWaterOverlayTexture(AsmMethod method, InsnPattern pattern) {
        method.setCursor(pattern.getFirst());
        method.visitInsn(new VarInsnNode(FLOAD, 1)); // partialTicks
        method.<FloatPredicate>invoke(partialTicks ->
                ForgeHax.EVENT_BUS.post(new RenderBlockOverlayEvent(RenderBlockOverlayEvent.OverlayType.WATER, partialTicks, Blocks.WATER.getDefaultState(), null)
        ));
        method.jumpIf(IFNE, pattern.getLast().getNext());
    }

    private void renderFireInFirstPerson(AsmMethod method, InsnPattern pattern) {
        method.setCursor(pattern.getFirst());
        method.visitInsn(new VarInsnNode(FLOAD, 1)); // partialTicks
        method.<FloatPredicate>invoke(partialTicks ->
                ForgeHax.EVENT_BUS.post(new RenderBlockOverlayEvent(RenderBlockOverlayEvent.OverlayType.FIRE, partialTicks, Blocks.FIRE.getDefaultState(), null)
        ));
        method.jumpIf(IFNE, pattern.getLast().getNext());
    }

    private interface BlockOverlayPredicate {
        boolean test(float partialTicks, IBlockState iblockstate, BlockPos overlayPos);
    }

    private interface FloatPredicate {
        boolean test(float f);
    }
}
