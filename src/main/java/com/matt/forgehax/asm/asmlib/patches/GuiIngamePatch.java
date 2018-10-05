package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.events.Render2DEvent;
import net.futureclient.asm.function.FloatConsumer;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Transformer(GuiIngame.class)
public class GuiIngamePatch {

  @Inject(name = "renderGameOverlay", args = {float.class})
  public void renderOverlayHook(AsmMethod method) {
    final AbstractInsnNode ret = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
        RETURN
    }, "x");

    method.setCursor(ret);
    method.visitInsn(new VarInsnNode(FLOAD, 1)); // partialTicks
    method.<FloatConsumer>invoke(partialTicks -> {
      GlStateManager.color(1.f, 1.f, 1.f, 1.f); // reset color
      ForgeHax.EVENT_BUS.post(new Render2DEvent(partialTicks));
      GlStateManager.color(1.f, 1.f, 1.f, 1.f);
    });
  }

}
