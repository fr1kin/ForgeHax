package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.events.Render2DEvent;
import net.futureclient.asm.function.FloatConsumer;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Transformer(GuiIngameForge.class)
public class GuiIngameForgePatch {

  @Inject(name = "renderGameOverlay", args = {float.class})
  public void overlayHook(AsmMethod method) {
    final MethodInsnNode renderHudText = method.stream()
        .filter(MethodInsnNode.class::isInstance)
        .map(MethodInsnNode.class::cast)
        .filter(m -> "renderHUDText".equals(m.name))
        .findFirst()
        .orElseThrow(() -> new NullPointerException("Failed to find renderHUDText"));
    method.setCursor(renderHudText);
    method.visitInsn(new VarInsnNode(FLOAD, 1)); // partialTicks
    method.<FloatConsumer>invoke(partialTicks -> { // copy/pasted from GuiIngamePatch :-)
      GlStateManager.color(1.f, 1.f, 1.f, 1.f); // reset color
      ForgeHax.EVENT_BUS.post(new Render2DEvent(partialTicks));
      GlStateManager.color(1.f, 1.f, 1.f, 1.f);
    });
  }

}
